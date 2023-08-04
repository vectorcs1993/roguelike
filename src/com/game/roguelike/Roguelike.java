package com.game.roguelike;

import com.formdev.flatlaf.FlatDarkLaf;
import com.game.roguelike.matrix.Matrix;
import com.game.roguelike.matrix.Node;
import com.game.roguelike.matrix.Nodes;
import com.game.roguelike.world.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Timer;

public class Roguelike {

    private final Interface screenMainGame;
    World world;
    private final ArrayList<World> worlds;
    private final int mapWidth;
    private final int mapHeight;
    // private GameObject viewPlayer, subPlayer;
    final Player player;
    int currentTick = 0;

    public arealable currentArea;
    private Entity currentStep;
    private final ArrayList<Entity> steps;
    private int tick;
    public final Matrix matrix;

    public Data data;
    JSONObject settings;
    int rate;
    public Roguelike() {
        tick = 0;
        worlds = new ArrayList<>();
        currentArea = null;

        try {

            settings = new JSONObject(readJSONToString("settings.json"));
            System.out.println("Файл настроек загружен");
            rate = (1000 / (int) getSettings().get("speed"));
            data = new Data(this);
            player = new Player(data.getObject("humanCivil2"));
            player.setItemSlot(5, new Item(data.getObject("gun_pistol1")));
            steps = new ArrayList<>();
            mapWidth = (int) getSettings(new String[]{"map"}).get("width");
            mapHeight = (int) getSettings(new String[]{"map"}).get("height");
            //объект обработки мира и рисования экрана
            screenMainGame = new Interface(this, mapWidth, mapHeight);
            //объект матрицы для вычислений
            matrix = new Matrix((int) getSettings().get("maxViewRadius"));
            // генерация комнат

            generateNewRoom();

            screenMainGame.centerAlign(player);
            final long[] time = {0};
            // главный цикл отрисовки, запускается каждые 100 мс
            new Timer().scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    currentTick++;
                    if (++time[0] %  rate == 0) {
                        update();
                        currentTick = 0;
                    }
                    drawScreenGame();
                }
            }, 0, 1000 / (int) getSettings().get("frameRate"));
        } catch (IOException | URISyntaxException | JSONException e) {
            throw new RuntimeException(e);
        }

        updateAllObjects();
        updateStats();
    }

    HashMap<String, Object> getSettings() {
        return getSettings(new String[]{});
    }

    HashMap<String, Object> getSettings(String[] entryObject) {
        HashMap<String, Object> setts = new HashMap<>();
        JSONObject obj = settings;
        try {
            Iterator keys;
            if (entryObject.length > 0) {
                obj = ((JSONObject) settings.get(entryObject[0]));
            }
            keys = obj.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                setts.put(key, obj.get(key));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return setts;
    }

    private void generateNewRoom() {
        generateWorld((int) getSettings(new String[]{"map"}).get("rooms"),
                (int) getSettings(new String[]{"map"}).get("border"),
                (int) getSettings(new String[]{"map"}).get("minWidth"),
                (int) getSettings(new String[]{"map"}).get("maxWidth"),
                (int) getSettings(new String[]{"map"}).get("minHeight"),
                (int) getSettings(new String[]{"map"}).get("maxHeight"));
    }

    int getSizeGrid() {
        return data.size;
    }

    int getSizeIzoGridX() {
        return (int) (data.size / 1.45);
    }

    int getSizeIzoGridY() {
        return (int) (data.size / 2.9);
    }

    Entity getCurrentStep() {
        return currentStep;
    }

    private void generateWorld(int rooms, int border, int minW, int maxW, int minH, int maxH) {
        worlds.add(new World(mapWidth, mapHeight));
        int level = worlds.indexOf(world) + 1;
        world = worlds.get(worlds.size() - 1);
        world.clear();
        steps.clear();
        world.fill(data.getObject("wall1"));
        // rooms - количество комнат
        // border - рамка размещаемых комнат (минимальное расстояние между ними)
        // максимальное количество ошибок
        int errors = 700;
        Room oldRoom;
        Room newRoom = null;
        while (rooms > 0 && errors > 0) {
            int widthRoom = getRandom(minW, maxW);
            int heightRoom = getRandom(minH, maxH);
            int rx = getRandom(world.getSizeX() - widthRoom);
            int ry = getRandom(world.getSizeY() - heightRoom);
            int placeX = rx - border;
            int placeY = ry - border;
            int placeW = widthRoom + border * 2;
            int placeH = heightRoom + border * 2;
            // проверка на возможность размещения постройки
            if (world.isClearOfObject(placeX, placeY, placeW, placeH, GameObject.WALL)) {
                // запоминаем координаты середины старой комнаты
                oldRoom = newRoom;
                newRoom = world.placeRoom(rx, ry, widthRoom, heightRoom, data.getObject("floor1"));
                newRoom.setName("Комната " + world.getRooms().size());
                if (oldRoom != null) {
                    Corridor corridor = world.placeRoad(
                            oldRoom.getAbsoluteCenterX(),
                            oldRoom.getAbsoluteCenterY(),
                            newRoom.getAbsoluteCenterX(),
                            newRoom.getAbsoluteCenterY(), data.getObject("floor1"));
                    corridor.setName("Корридор " + world.getCorridors().size());
                }
                // уменьшаем счетчик комнаты
                rooms--;
            } else {
                errors--;
            }

        }

        // установка лестницы
        int[] placeLadderDown = placeFreeNeighbor(world.getLastRoomObjects());
        world.addEnvironment(new GameObject(data.getObject("ladderDown1")), placeLadderDown[0], placeLadderDown[1]);

        // перемещение игрока
        currentArea = world.getRooms().get(0);
        int[] placePlayer = placeFreeNeighbor(world.getRoomObjects(0));
        world.addEntity(player, placePlayer[0], placePlayer[1]);

        // если уровень выше первого
        if (level > 0) {
            // то добавляется лестница вниз
            world.addEnvironment(new GameObject(data.getObject("ladderUp1")), placePlayer[0], placePlayer[1]);
        }

        try {
            // формирование списка допустимых монстров на уровне
            ArrayList<JSONObject> tempMonsters = new ArrayList<>();
            for (JSONObject monster : data.getJSONObjectsIsType(GameObject.MONSTER)) {
                if (level >= monster.getInt("level")) {
                    tempMonsters.add(monster);
                }
            }
            // если список доступных к размещению монстров не пустой
            if (!tempMonsters.isEmpty()) {
                // максимальное число монстров на уровне
                int maxCountMonsters = (int) getSettings(new String[]{"map"}).get("maxCountMonsters");
                // случайное количество монстров, но минимум 1
                int randomCountMonsters = getRandom(1, maxCountMonsters);
                for (int i = 0; i < randomCountMonsters; i++) {
                    JSONObject monster = tempMonsters.get(getRandom(tempMonsters.size()));
                    boolean place = false;
                    int errorsPlace = 0;

                    while (!place) {
                        int[] placeMonster = placeFreeNeighbor(world.getRoomObjects(getRandom(world.getRooms().size())));
                        // если в комнате никого нет, то
                        if (world.getObjectsFromRoom(placeMonster[0], placeMonster[1], 2).isEmpty()) {
                            JSONObject model = data.getObject(monster.getString("id"));
                            Npc npc = new Npc(model);
                            world.addEntity(npc, placeMonster[0], placeMonster[1]);
                            JSONArray items = model.getJSONArray("items");
                            for (int ii = 0; ii < items.length(); ii++) {
                                npc.getItems().add(new Item(data.getObject(items.getString(ii))));
                            }
                            place = true;
                        } else {
                            errorsPlace++;
                            if (errorsPlace >= world.getRooms().size()) {
                                break;
                            }
                        }
                    }
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        addItem("medkit1");
        addItem("gun_pistol1");
        toNewLevel();
        updatePlayerView();
    }

    public void addItem(String item) {
        int[] place = placeFreeNeighbor(world.getRandomRoomAndRoadObjects(), false);
        world.addEnvironment(new Item(data.getObject(item)), place[0], place[1]);
    }

    public void endStep() {
        player.setOd(0);
        stopAndEndStep();
    }

    private void toNewLevel() {
        // формируется список существ в отдельной переменной
        steps.clear();
        currentStep = player;
        steps.add(player);
        for (GameObject object : world.getAllObjects().getObjectsType(GameObject.MONSTER)) {
            steps.add((Entity) object);
        }
        log(player.getName() + " попадает на уровень: " + getLevel(), "blue_white");
    }

    // вызывается при нажатии по игровому канвасу
    public void clickCanvas(int x, int y) {
        if (getCurrentStep().equals(player)) {
            GameObject clickEntity = world.getObject(x, y, 2);
            boolean playerDetected = isPlayerDetected();
            if (clickEntity != null) {
                if (clickEntity instanceof Entity entity) {
                    if (clickEntity.getType() == GameObject.MONSTER) {
                        boolean attack = true;
                        // дистанция до монстра
                        int distance = player.getView()[getX(entity)][getY(entity)];
                        // если в основной руке нет оружия
                        if (distance > 0 && !player.isItemSlot(5)) {
                            attack = false;
                            log("Цель слишком далеко", "green_white");
                        }
                        if (attack) {
                            if (!playerDetected) {
                                player.setOd((int) getSettings().get("costODAction"));
                            }
                            if (isOD(player, (int) getSettings().get("costODAction"), true, true)) {
                                tick++;
                                if (tick >= player.getStamina() && tick % player.getStamina() == 0) {
                                    // отнимается энергия
                                    player.setEnergy(player.getEnergy() - (int) getSettings().get("costODAction"));
                                }

                                // проверка вероятности атаки
                                if (getFactAttack(player, entity)) {
                                    // атака монстра
                                    log(player.attackEntity(entity, getLevelAttack(player.getItemSlot(5))), "green_black");
                                    if (entity instanceof Npc monster) {
                                        if (monster.getHp() <= 0) {
                                            Item item = monster.getItems().get(getRandom(monster.getItems().size()));
                                            int xm = getX(monster);
                                            int ym = getY(monster);
                                            monster.destroy();
                                            world.removeEntity(monster);
                                            steps.remove(monster);
                                            log(monster.getName() + " уничтожен", "green_black");
                                            if (world.getEnvironment(xm, ym) == null) {
                                                world.addEnvironment(item,
                                                        xm, ym);
                                            } else {
                                                log(item.getName() + " добавлен в инвентарь "+player.getName(), "green_black");
                                                player.getItems().add(item);
                                            }
                                        } else {
                                            // если монстр выжил, то атакующий становится его целью
                                            monster.setTarget(player);
                                        }
                                    }
                                } else {
                                    log("Промах", "green_white");
                                }
                            }
                            if (player.getOd() == 0) {
                                stopAndEndStep();
                            }
                        }
                    }
                }
            } else {
                //if (screenMainGame.canvas.isPlayerVisible(x, y)) {
                if (screenMainGame.visible[x][y]) {
                    if (world.isMove(x, y)) {
                        if (!playerDetected && player.getOd() < (int) getSettings().get("costODMove")) {
                            player.setOd((int) getSettings().get("costODMove"));
                        }
                        if (isOD(player, (int) getSettings().get("costODMove"), false, true)) {
                            if (player.getItemsWeight() < player.getMaxCp()) {
                                int px = getX(player);
                                int py = getY(player);
//                                player.path = matrix.getPathTo(world.getNodes(), world.getNode(px, py), world.getNode(x, y));
//                                if (player.path.isEmpty()) {
//                                    log("Путь не найден");
//                                }
                                System.out.println();
                                log("as");
                            } else {
                                log("Слишком большой груз на плечах!");
                            }
                        }
                    } else {
                        log("Путь не найден");
                    }
                } else {
                    log("Путь не найден");
                }
            }
            updatePlayerView();
            updateAllObjects();
            updateStats();
        }
    }

    private void update() {
        if (currentStep.equals(player)) {
            updateStepPlayer();
        } else {
            updateStepMonster();
        }
    }

    boolean isPlayerDetected() {
        boolean playerDetected = false;
        for (GameObject npc : world.getAllObjects().getObjectsType(GameObject.MONSTER)) {
            Npc npcNotPlayer = (Npc) npc;
            if (npcNotPlayer.isTarget()) {
                playerDetected = npcNotPlayer.getTarget().equals(player);
            }
            for (GameObject obj : getSeeAllObjects(npcNotPlayer)) {
                if (obj.equals(player)) {
                    playerDetected = true;
                    break;
                }
            }
            if (playerDetected) {
                break;
            }
        }


        return playerDetected;
    }

    private void updateStepPlayer() {
        if (currentStep.equals(player)) {
            if (!player.path.isEmpty()) {
                Node next = player.path.last();
                if (world.isMove(next.x, next.y)) {
                    boolean playerDetected = isPlayerDetected();
                    if (!playerDetected && player.getOd() < (int) getSettings().get("costODMove")) {
                        player.setOd((int) getSettings().get("costODMove"));
                    }
                    if (isOD(player, (int) getSettings().get("costODMove"), true, true)) {
                        tick++;
                        world.moveEntity(player, next.x, next.y);
                        if (tick >= player.getStamina() && tick % player.getStamina() == 0) {
                            // отнимается энергия
                            player.setEnergy(player.getEnergy() - (int) getSettings().get("costODMove"));
                        }
                        currentArea = world.getArea(next.x, next.y);
                        player.path.removeLast();

                        if (player.path.isEmpty()) {
                            GameObject objectEhdPoint = world.getEnvironment(next.x, next.y);
                            if (objectEhdPoint != null) {
                                if (objectEhdPoint.getType() == GameObject.LADDER_DOWN
                                        || objectEhdPoint.getType() == GameObject.LADDER_UP) {
                                    if (objectEhdPoint.getType() == GameObject.LADDER_DOWN) {
                                        if (worlds.size() == 1) {
                                            generateNewRoom();
                                        } else {
                                            if (worlds.indexOf(world) == worlds.size() - 1) {
                                                generateNewRoom();
                                            } else {
                                                world = worlds.get(worlds.indexOf(world) + 1);
                                                toNewLevel();
                                            }
                                        }
                                    } else if (objectEhdPoint.getType() == GameObject.LADDER_UP) {
                                        world = worlds.get(worlds.indexOf(world) - 1);
                                        toNewLevel();
                                    }
                                    player.setEnergy(player.getEnergy() - 4);
                                    screenMainGame.centerAlign(player);
                                } else if (objectEhdPoint instanceof Item item) {
                                    playerAddItem(item);
                                    world.removeEnvironment(item);
                                    log(player.getName() + " подобрал " + item.getName(), "blue_white");
//                                    player.setEnergy(player.getEnergy() - 4);
//                                    int prevHp = player.getHp();
//                                    player.setHp(player.getHp() + 50);
//                                    world.removeEnvironment(objectEhdPoint);
//                                    log(player.getName() + " излечился на " + Math.abs(prevHp - 50) +
//                                            " единиц здоровья", "green_black");
                                }
                            }
                        }

                    } else {
                        if (!playerDetected) {
                            player.setOd(0);
                        } else {
                            player.path.clear();
                        }
                    }
                    if (player.getOd() == 0) {
                        if (!playerDetected) {
                            player.setOd(player.getMaxOd());
                        } else {
                            stopAndEndStep();
                        }
                    }
                    updatePlayerView();
                    updateAllObjects();
                    updateStats();
                } else {
                    player.path.clear();
                }
            }
        }
    }

    private void stopAndEndStep() {
        player.path.clear();
        nextStepEntity();
    }

    public int getLevelAttack(Item item) {
        int damageMin = 5;
        int damageMax = 10;
        int guns = 100;

        // расчёт урона, наносимого определённым средством
        double k = (double) guns / 100;
        return damageMin + getRandom((int) (Math.abs(damageMax - damageMin) * k) + 1);
    }

    public double getPercentAttack(Entity entity, Entity target) {
        int accuracy = 50;
        int radius = 3;
        int distance = entity.getView()[getX(target)][getY(target)];
        int de = (int) Roguelike.map(accuracy, 0, 100, 10, 0);
        return limit(accuracy -
                        ((distance - radius) * de),
                (distance == 0) ? 95 : 0, 95);
    }

    public boolean getFactAttack(Entity entity, Entity target) {
        return getRandom(100) + 1 <= getPercentAttack(entity, target);
    }

    private void updateStepMonster() {
        if (!currentStep.equals(player)) {
            Npc npcNotPlayer = (Npc) getCurrentStep();
            // флажок разрешает npc перемещаться если до врага необходимо идти
            boolean moveNpc = true;
            boolean attackNpc = false;
            // ищет объекты в своей зоне видимости
            for (GameObject obj : getSeeAllObjects(npcNotPlayer)) {
                // если монстр обнаруживает игрока (т.е. врага)
                if (obj.equals(player)) {
                    npcNotPlayer.setTarget(player);
                    log(npcNotPlayer.getName() + " обнаружила " + player.getName());
                    // перебирает все соседствующие клетки,
                    for (GameObject neighborEntity : getSeeAllObjects(npcNotPlayer, 1)) {
                        //  есть ли враг в непосредственной близости, его можно атаковать
                        if (neighborEntity.equals(npcNotPlayer.getTarget()) && Matrix.allowDiagonalMove(world.getNodes(),
                                getX(npcNotPlayer), getY(npcNotPlayer),
                                getX(neighborEntity), getY(neighborEntity))) {
                            attackNpc = true;
                            moveNpc = false;
                            break;
                        }
                    }
                    // на этом перебор объектов заканчивается, так как цель обнаружена
                    break;
                }
            }
            if (npcNotPlayer.isTarget()) {
                if (moveNpc) {
                    // монстр строит путь до цели
                    Nodes path = getPath(npcNotPlayer, npcNotPlayer.getTarget());
                    // если до цели необходимо переместиться
                    if (!path.isEmpty()) {
                        // определяет самую ближайшую клетку для перемещения до цели
                        Node lastNode = path.get(path.size() - 1);
                        // то монстр начинает движение в сторону цели
                        if (isOD(npcNotPlayer, (int) getSettings().get("costODMove"), true, true)) {
                            log(npcNotPlayer.getName() + " преследует " + player.getName());
                            world.moveEntity(npcNotPlayer, lastNode.x, lastNode.y);
                        } else {
                            npcNotPlayer.setOd(0);
                        }
                    } else {
                        // если путь не найден то пропускаем ход
                        npcNotPlayer.setOd(0);
                    }
                }
                if (attackNpc) {
                    // то атакует если хватает очков дейвствий
                    if (isOD(npcNotPlayer, (int) getSettings().get("costODAction"), true, true)) {
                        log(npcNotPlayer.attackEntity(npcNotPlayer.getTarget()), "red_black");
                    } else {
                        // если ОД не хватает то пропускает ход
                        npcNotPlayer.setOd(0);
                    }
                }
            } else {
                npcNotPlayer.setOd(0);
            }
            updatePlayerView();
            updateAllObjects();
            updateStats();
            if (npcNotPlayer.getOd() == 0) {
                nextStepEntity();
            }

            //if (npcNotPlayer.getTarget() == null) {
//                int[] randomTarget = placeFreeNeighbor(world.getObjectsFromRoom(getX(npcNotPlayer), getY(npcNotPlayer), 0), false);
//                Nodes path = matrix.getPathTo(world.getNodes(), world.getNode(getX(npcNotPlayer), getY(npcNotPlayer)),
//                        world.getNode(randomTarget[0], randomTarget[1]));
//                System.out.println(path.size());
//                if (!path.isEmpty()) {
//                    if (isOD(npcNotPlayer, (int) getSettings().get("costODMove"), true, false)) {
//                        Node lastNode = path.get(path.size() - 1);
//                        world.moveEntity(npcNotPlayer, lastNode.x, lastNode.y);
//                        updatePlayerView();
//                    } else {
//                        npcNotPlayer.setOd(0);
//                    }
//                } else {
//                    npcNotPlayer.setOd(0);
//                }

//            // если цель монстра не определена (т.е. по близости нет врагов)
//            if (npcNotPlayer.getHp() < npcNotPlayer.getHpMax()) {
//                // то он может может зализывать свои раны
//                if (npcNotPlayer.getOd() >= 1) {
//                    npcNotPlayer.setOd(npcNotPlayer.getOd() - 1);
//                    npcNotPlayer.recoveryHp();
//                    log(npcNotPlayer.getName() + " излечивается от ранений");
//                } else {
//                    npcNotPlayer.setOd(0);
//                }
//            } else {
//                npcNotPlayer.setOd(0);
//            }
            //}
        }
    }

    private void playerAddItem(Item item) {
        player.getItems().add(item);
        syncInventory();
    }

    void nextStepEntity() {
        int currentIndexStep = steps.indexOf(currentStep);
        currentIndexStep++;
        if (currentIndexStep >= steps.size()) {
            currentIndexStep = 0;
        }
        currentStep = steps.get(currentIndexStep);
        currentStep.setOd(currentStep.getMaxOd());
        updateStats();
    }

    void updatePlayerView() {
        adjView(player);
        setOpenObjectsView();
    }


    private boolean isOD(Entity entity, int costAction, boolean action, boolean log) {
        if (entity.getOd() >= costAction) {
            if (action) {
                entity.setOd(entity.getOd() - costAction);
            }
            return true;
        } else {
            if (log) {
                log("Не хватает очков действия");
            }
        }
        return false;
    }

    private void updateStats() {
        screenMainGame.updateTextStatPrimary(
                new String[]{
                        "ЗДОРОВЬЕ",
                        "ОЧКИ ДЕЙСТВИЙ",
                        "ГРУЗ",
                        "ОБЗОР"
                }, new int[]{
                        player.getHp(),
                        player.getOd(),
                        player.getItemsWeight(),
                        player.getRadius(),
                }, new int[]{
                        player.getHpMax(),
                        player.getMaxOd(),
                        player.getMaxCp(),
                        (int) getSettings().get("maxViewRadius")
                }
                , new int[]{
                        Interface.CanvasStats.FORWARD,
                        Interface.CanvasStats.FORWARD,
                        Interface.CanvasStats.BACKWARD,
                        Interface.CanvasStats.FORWARD
                });
        screenMainGame.updateTextStatsSecondary(
                new String[]{
                        "ЭНЕРГИЯ",
                        "ГОЛОД",
                        "ЖАЖДА",
                        "РАДИАЦИЯ"
                }, new int[]{
                        player.getEnergy(),
                        player.getHunger(),
                        player.getThirst(),
                        player.getRadiation(),
                }, new int[]{
                        100,
                        100,
                        100,
                        100
                }
                , new int[]{
                        Interface.CanvasStats.FORWARD,
                        Interface.CanvasStats.BACKWARD,
                        Interface.CanvasStats.BACKWARD,
                        Interface.CanvasStats.BACKWARD
                });
        screenMainGame.updateTextChar(player.getName() +
                "\nСИЛА: " + player.getStrength() +
                "\nЛОВКОСТЬ: " + player.getDexterity() +
                "\nВЫНОСЛИВОСТЬ: " + player.getStamina() +
                "\nВОСПРИЯТИЕ: " + player.getPerception());
    }
    // алгоритм обновления всех entity на уровне

    private void updateAllObjects() {
        drawObjectsInHashMap(world.getIteratorObjects(0));
        drawObjectsInHashMap(world.getIteratorObjects(1));
        drawObjectsInHashMap(world.getIteratorObjects(2));
    }

    private void drawScreenGame() {
        screenMainGame.draw();
    }

    private void log(String message) {
        log(message, "dark_gray_white");
    }

    private void log(String message, String style) {
        screenMainGame.addLog(message, tick, style);
    }

    //устанавливает объект в свободную от других объектов область
    private int[] placeFreeNeighbor(GameObjects objects, boolean border) {
        int[] placeXY = new int[2];
        boolean place = false;
        GameObjects floors = objects.getObjectsType(GameObject.FLOOR);

        while (!place) {
            GameObject placeObject = floors.getRandomObject();
            placeXY[0] = getX(placeObject);
            placeXY[1] = getY(placeObject);
            if (!border || matrix.getNeighboring(world.getNodes(), world.getNode(placeXY[0], placeXY[1])).size() == 8) {
                place = true;
            }
        }
        return placeXY;
    }

    private int[] placeFreeNeighbor(GameObjects objects) {
        return placeFreeNeighbor(objects, true);
    }

    // возвращает все видимые соседние объекты относительно entity
    public GameObjects getSeeAllObjects(Entity entity) {
        return getSeeAllObjects(entity, entity.getRadius());
    }

    // возвращает все видимые объекты относительно entity в радиусе radius
    public GameObjects getSeeAllObjects(Entity entity, int radius) {
        GameObjects neighbors = new GameObjects();
        for (int l = 0; l < matrix.matrixLine.length; l++) {
            int px = getX(entity), py = getY(entity);
            for (int i = 0; i < limit(radius, 1, matrix.maxRadius); i++) {
                int ix = matrix.matrixShearch[matrix.matrixLine[l][i]][0];
                int iy = matrix.matrixShearch[matrix.matrixLine[l][i]][1];
                int gx = (int) limit(getX(entity) + ix, 0, world.getSizeX() - 1);
                int gy = (int) limit(getY(entity) + iy, 0, world.getSizeY() - 1);
                if (!Matrix.allowDiagonalView(world.getNodes(), gx, gy, px, py))
                    break;
                else {
                    px = gx;
                    py = gy;
                }
                neighbors.addUnique(world.getObject(gx, gy));
                Entity neighborEntity = world.getEntity(gx, gy);
                if (neighborEntity != null) {
                    if (!neighborEntity.equals(entity)) {
                        neighbors.addUnique(neighborEntity);
                    }
                }
                if (world.getNode(gx, gy).isSolid())
                    break;
            }
        }
        return neighbors;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }
        new Roguelike();
    }

    public static int getRandom(int min, int max) {
        if (min == max)
            return max;
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    // возвращает рандомное число
    public static int getRandom(int num) {
        Random random = new Random();
        return random.nextInt(num);
    }

    // возвращает число в заданном лимите min max
    public static double limit(double d, double min, double max) {
        return Math.min(Math.max(d, min), max);
    }

    // интерполяция числа
    public static float map(float value, float istart, float istop, float ostart, float ostop) {
        return ostart + (ostop - ostart) * ((value - istart) / (istop - istart));
    }

    // служебная функция для отрисовки
    private void drawObjectsInHashMap(HashMap<GameObject, int[]> objects) {
        for (HashMap.Entry<GameObject, int[]> entry : objects.entrySet()) {
            screenMainGame.setOpen(entry.getValue()[0], entry.getValue()[1], world.getNode(entry.getValue()[0], entry.getValue()[1]).isOpen());
            boolean dark = false;
            if (!entry.getKey().equals(player)) {
                if (player.getView()[entry.getValue()[0]][entry.getValue()[1]] == -1) {
                    if (!entry.getKey().getPermanent()) {
                        dark = true;
                    }
                }
            }
            screenMainGame.setDark(entry.getValue()[0], entry.getValue()[1], dark);
        }
    }

    private void setOpenObjectsView() {
        for (GameObject obj : getSeeAllObjects(player)) {
            world.getNode(getX(obj), getY(obj)).setOpen(true);
        }
        world.getNode(getX(player), getY(player)).setOpen(true);
    }

    private void adjView(Entity entity) { // корректирует обзор
        Matrix.clearView(entity.getView());
        int ax = getX(entity), ay = getY(entity);
        entity.getView()[ax][ay] = -1;
        for (int l = 0; l < matrix.matrixLine.length; l++) {
            int px = getX(entity), py = getY(entity);
            for (int i = 0; i < limit(entity.getRadius(), 1, matrix.matrixLine[l].length); i++) {
                int ix = matrix.matrixShearch[matrix.matrixLine[l][i]][0];
                int iy = matrix.matrixShearch[matrix.matrixLine[l][i]][1];
                if (ax + ix > world.getSizeX() - 1 || ay + iy > world.getSizeY() - 1 || ax + ix < 0 || ay + iy < 0)
                    break;
                int gx = (int) limit(ax + ix, 0, world.getSizeX() - 1),
                        gy = (int) limit(ay + iy, 0, world.getSizeY() - 1);
                if (!Matrix.allowDiagonalView(world.getNodes(), gx, gy, px, py))
                    break;
                px = gx;
                py = gy;


                if (entity.getView()[gx][gy] == Matrix.NULL) {
                    entity.getView()[gx][gy] = i;
                }
                if (world.getNode(gx, gy).isSolid()) {
                    break;
                }
            }
        }
    }

    // служебная функция возвращает Х координату объекта
    int getX(GameObject object) {
        return world.getPositionX(object);
    }

    // служебная функция возвращает Y координату объекта
    int getY(GameObject object) {
        return world.getPositionY(object);
    }

    public int getLevel() {
        return worlds.indexOf(world) + 1;
    }

    // возвращает ближайший к entity узел target
    public Node getNearNeighboring(Entity entity, Entity target) {
        return matrix.getNeighboring(world.getNodes(), world.getNode(getX(entity), getY(entity))).getNearest(getX(target), getY(target));
    }

    public Nodes getPath(Entity entity, Entity target) {
        return matrix.getPathTo(world.getNodes(), world.getNode(getX(entity), getY(entity)), getNearNeighboring(target, entity));
    }

    // чтение JSON файла из папки "data"
    public String readJSONToString(String filename) throws IOException, URISyntaxException {
        try (InputStream in = getClass().getResourceAsStream("/" + filename)) {
            assert in != null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line);
                    content.append(System.lineSeparator());
                }
                return content.toString();
            }
        }
    }

    // событие по выбору определенной вкладки в панели управления
    public void selectTab(int idx) {
        if (idx == 1) {
            syncInventory();
        }
    }

    private void syncInventory() {
        screenMainGame.getListInventory().clear();
        for (Item item : player.getItems()) {
            screenMainGame.getListInventory().addElement(item);
        }
        screenMainGame.getInventoryTextLabel().setText("Общий вес: "+player.getItemsWeight()+"/"+player.getMaxCp());
        screenMainGame.getInventoryButtonUse().setVisible(false);
        screenMainGame.getInventoryInfoOfItem().setText("");
    }

    public void selectItem(Item item) {
        screenMainGame.getInventoryButtonUse().setVisible(true);
        screenMainGame.getInventoryInfoOfItem().setText("Вес: "+item.getWeight());
    }
}



