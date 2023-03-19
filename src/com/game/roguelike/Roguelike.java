package com.game.roguelike;

import com.game.roguelike.matrix.Matrix;
import com.game.roguelike.matrix.Node;
import com.game.roguelike.matrix.Nodes;
import com.game.roguelike.world.*;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.*;

public class Roguelike {

    private final Interface screenMainGame;
    World world;
    private final ArrayList<World> worlds;
    private final int mapWidth;
    private final int mapHeight;
    String[] console = new String[4];
    // private GameObject viewPlayer, subPlayer;
    final Entity player;
    private arealable currentArea;
    private int tick;
    private final Matrix matrix;

    private boolean inputDialog;
    private Runnable[] actionDialog;
    JSONObject settings;

    public static final String DB_URL = "jdbc:h2:/data/data.db";
    public static final String DB_Driver = "org.h2.Driver";

    Connection connection;

    public Roguelike() {
        inputDialog = false;
        actionDialog = null;
        tick = 0;
        worlds = new ArrayList<>();
        player = new Player(GameObject.PLAYER);
        currentArea = null;
        try {
            Class.forName(DB_Driver); //Проверяем наличие JDBC драйвера для работы с БД
            connection = DriverManager.getConnection(DB_URL);//соединениесБД
            System.out.println("Соединение с СУБД выполнено.");
        } catch (ClassNotFoundException e) {
            e.printStackTrace(); // обработка ошибки  Class.forName
            System.out.println("JDBC драйвер для СУБД не найден!");
        } catch (SQLException e) {
            e.printStackTrace(); // обработка ошибок  DriverManager.getConnection
            System.out.println("Ошибка SQL !");
        }

        try {
            settings = new JSONObject(readJSONToString("settings.json"));
            System.out.println("Файл настроек загружен");
            mapWidth = (int) getSettings(new String[]{"map"}).get("width");
            mapHeight = (int) getSettings(new String[]{"map"}).get("height");
            //объект обработки мира и рисования экрана
            screenMainGame = new Interface(this, mapWidth, mapHeight);
            //объект матрицы для вычислений
            matrix = new Matrix((int) getSettings().get("maxViewRadius"));
            // генерация комнат

            generateWorld((int) getSettings(new String[]{"map"}).get("rooms"),
                    (int) getSettings(new String[]{"map"}).get("border"),
                    (int) getSettings(new String[]{"map"}).get("minWidth"),
                    (int) getSettings(new String[]{"map"}).get("maxWidth"),
                    (int) getSettings(new String[]{"map"}).get("minHeight"),
                    (int) getSettings(new String[]{"map"}).get("maxHeight"));
            screenMainGame.centerAlign(player);
            log("Добро пожаловать");
            // главный цикл отрисовки, запускается каждые 100 мс
//            new Timer().scheduleAtFixedRate(new TimerTask() {
//                @Override
//                public void run() {
//
//                }
//            }, 0, (int) getSettings().get("speed";))
            final long[] time = {0};
            // главный цикл отрисовки, запускается каждые 100 мс
            new Timer().scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    if (++time[0] %(1000 / (int) getSettings().get("speed"))==0) {
                        movePlayerNextCell();
                    }

                    drawScreenGame();
                }
            }, 0, 1000 / (int) getSettings().get("frameRate"));
            screenMainGame.canvas.addKeyListener(new KeyListener() {
                @Override
                public void keyTyped(KeyEvent keyEvent) {
                    if (inputDialog) {
                        if (keyEvent.getKeyChar() == '1') {
                            if (actionDialog != null) {
                                if (actionDialog[0] != null) {
                                    actionDialog[0].run();
                                    actionDialog = null;
                                }
                            }
                        } else if (keyEvent.getKeyChar() == '2') {
                            if (actionDialog != null) {
                                if (actionDialog[1] != null) {
                                    actionDialog[1].run();
                                    actionDialog = null;
                                }
                            }
                        }
                    } else {
//                        if (keyEvent.getKeyChar() == '1') {
//                            screenMainGame.canvas.dK += 1;
//
//                        }
//                        if (keyEvent.getKeyChar() == '2') {
//                            screenMainGame.canvas.dK -= 1;
//                        }
//                        if (keyEvent.getKeyChar() == '3') {
//                            screenMainGame.canvas.dJ += 1;
//
//                        }
//                        if (keyEvent.getKeyChar() == '4') {
//                            screenMainGame.canvas.dJ -= 1;
//                        }
//                        drawScreenGame();
                    }

                }

                @Override
                public void keyPressed(KeyEvent keyEvent) {

//                if (!lockInput) {
//                    if (moveCount > 1) {
//                        moveCount = 0;
//                        return;
//                    }
//                    if (moveCount == 0) {
//
//
//                        actionPlayer(keyEvent.getKeyCode());
//
//                    }
//                    moveCount++;
//                }
                }

                @Override
                public void keyReleased(KeyEvent keyEvent) {
                    // moveCount = 0;
                }
            });
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                try {
                    connection.close();       // отключение от БД
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }));
        } catch (IOException | URISyntaxException | JSONException e) {
            throw new RuntimeException(e);
        }
        updateAllObjects();
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

    // данные
    private void actionPlayer(int keyCode) {
        // действия персонажа
        int newX = getX(player);
        int newY = getY(player);
        int viewX = newX;
        int viewY = newY;
        final int prevX = newX;
        final int prevY = newY;
        boolean move = false;
        boolean attack = false;
        boolean allowSpeed = true;

        switch (keyCode) {
            case 68 -> { // нажата клавиша D
                if (world.isMove(newX + 1, newY)) {
                    move = true;
                }
                newX += 1;
                viewX = newX + 1;
            }
            case 65 -> { // нажата клавиша A
                if (world.isMove(newX - 1, newY)) {
                    move = true;
                }
                newX -= 1;
                viewX = newX - 1;
            }
            case 87 -> {  // нажата клавиша W
                if (world.isMove(newX, newY - 1)) {
                    move = true;
                }
                newY -= 1;
                viewY = newY - 1;
            }
            case 83 -> {  // нажата клавиша S
                if (world.isMove(newX, newY + 1)) {
                    move = true;
                }
                newY += 1;
                viewY = newY + 1;
            }
            // движение по диагонали
            case 67 -> {  // нажата клавиша C
                move = isMove(newX, newY, newX + 1, newY + 1);
                newX += 1;
                newY += 1;
                viewY = newY + 1;
                viewX = newX + 1;
            }
            case 81 -> {  // нажата клавиша Q
                move = isMove(newX, newY, newX - 1, newY - 1);
                newX -= 1;
                newY -= 1;
                viewY = newY - 1;
                viewX = newX - 1;
            }
            case 90 -> {  // нажата клавиша Z
                move = isMove(newX, newY, newX - 1, newY + 1);
                newX -= 1;
                newY += 1;
                viewY = newY + 1;
                viewX = newX - 1;
            }
            case 69 -> {  // нажата клавиша E
                move = isMove(newX, newY, newX + 1, newY - 1);
                newX += 1;
                newY -= 1;
                viewY = newY - 1;
                viewX = newX + 1;
            }
            case 82 -> {  // нажата клавиша R
                player.nextDirection();
                drawScreenGame();

            }
            case 32 -> {
                if (world.getObject(newX, newY).getType() == GameObject.LADDER_DOWN) {
                    drawScreenDialog("Спуститься вниз по лестнице?", () -> {
                        inputDialog = false;
                        if (worlds.size() == 1) {
                            generateWorld(15, 3, 3, 8, 3, 8);
                        } else {
                            if (worlds.indexOf(world) == worlds.size() - 1) {
                                generateWorld(15, 3, 3, 8, 3, 8);
                            } else {
                                world = worlds.get(worlds.indexOf(world) + 1);
                            }
                        }
                        drawScreenGame();
                    }, () -> {
                        inputDialog = false;
                        drawScreenGame();
                    });
                } else if (world.getObject(newX, newY).getType() == GameObject.LADDER_UP) {
                    drawScreenDialog("Подняться наверх по лестнице?", () -> {
                        inputDialog = false;
                        world = worlds.get(worlds.indexOf(world) - 1);
                        drawScreenGame();
                    }, () -> {
                        inputDialog = false;
                        drawScreenGame();
                    });
                }
            }
        }
        if (move) {
            tick++;
            player.setDirection(Matrix.getDirection(newX, newY, viewX, viewY));
            world.moveEntity(player, newX, newY);
            currentArea = world.getArea(newX, newY);
        } else {
            Entity entity = world.getEntity(newX, newY);
            if (entity != null) {
                if (!entity.equals(player) && entity.getType() == GameObject.MONSTER) {
                    attack = true;
                }
            }
            if (attack) {
                tick++;
                log(player.attackEntity(entity));
                if (entity.getHp() <= 0) {
                    if (entity instanceof Npc) {
                        ((Npc) entity).destroy();
                    }
                    world.removeEntity(entity);
                    log(entity.getName() + " уничтожен");
                }
                drawScreenGame();

            }
        }
        if (move || attack) {
            // обновление хода для монстров
            // обход всех сущностей на карте (исключительно npc)
            for (GameObject npc : world.getAllObjects().getObjectsType(GameObject.MONSTER)) {
                boolean moveNpc = true;
                Npc npcNotPlayer = (Npc) npc;
                // npcNotPlayer.setTarget(null);
                // ищет объекты в своей зоне видимости
                for (GameObject obj : getSeeAllObjects(npcNotPlayer)) {
                    // если монстр обнаруживает игрока (т.е. врага)
                    if (obj.equals(player)) {
                        npcNotPlayer.setTarget(player);
                        allowSpeed = false;
                        break;
                    }
                }
                // если цель монстра определена (т.е. обнаружен враг)
                if (npcNotPlayer.getTarget() != null) {
                    boolean attackNpc = false;
                    // перебирает все соседствующие клетки
                    for (GameObject neighborEntity : getSeeAllObjects(npcNotPlayer, 1)) {
                        // если находит сущность и она является целью
                        if (neighborEntity.equals(npcNotPlayer.getTarget())) {
                            // то атакует
                            if (Matrix.allowDiagonalMove(world.getNodes(),
                                    getX(npcNotPlayer), getY(npcNotPlayer),
                                    getX(neighborEntity), getY(neighborEntity))) {
                                log(npcNotPlayer.attackEntity(npcNotPlayer.getTarget()));
                                moveNpc = false;
                                break;
                            }
                        }
                    }
                    if (moveNpc) {
                        // монстр строит путь до цели
                        Nodes path = getPath(npcNotPlayer, npcNotPlayer.getTarget());
                        // если до цели необходимо переместиться
                        if (!path.isEmpty()) {
                            // определяет самую ближайшую клетку для перемещения до цели
                            Node lastNode = path.get(path.size() - 1);
                            // флажок разрешения для преследования
                            boolean goLastNode = true;
                            // проверяет закреплён ли Npc к территории
                            if (((Npc) npc).attachedToArea) {
                                arealable currentArea = world.getArea(getX(npc), getY(npc));
                                arealable newArea = world.getArea(lastNode.x, lastNode.y);
                                if (currentArea != newArea) {
                                    goLastNode = false;
                                    System.out.println(npc.getName() + " больше не преследует игрока");
                                }
                            }
                            if (goLastNode) {
                                // то монстр начинает движение в сторону цели
                                log(npc.getName() + " преследует " + player.getName());
                                world.moveEntity(npcNotPlayer, lastNode.x, lastNode.y);
                            }

                        }
                    }
                } else {
                    // если цель монстра не определена (т.е. по близости нет врагов)
                    // то он может может зализывать свои раны
                    npcNotPlayer.recoveryHp();
                }
            }
            // визуальное обновление окружения
            adjView(player);
            setOpenObjectsView();
            drawScreenGame();

        }

        if (allowSpeed) {
            Nodes directionNeighbors = new Nodes();
            for (Node node : matrix.getNeighboring(world.getNodes(),
                    world.getNode(getX(player), getY(player)))) {
                if (node.x != prevX || node.y != prevY) {
                    directionNeighbors.add(node);
                }
            }
            if (directionNeighbors.size() == 1) {
                Node nextNode = directionNeighbors.last();
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        actionPlayer(getKeyCodeXY(getX(player), getY(player), nextNode.x, nextNode.y));
                    }
                }, 15);
            }
        }
    }

    private int getKeyCodeXY(int x0, int y0, int x1, int y1) {
        if (x0 < x1 && y0 < y1)
            return 0;
        else if (x0 < x1 && y0 == y1)
            return 68;
        else if (x0 < x1 && y0 > y1)
            return 2;
        else if (x0 == x1 && y0 > y1)
            return 87;
        else if (x0 > x1 && y0 > y1)
            return 4;
        else if (x0 > x1 && y0 == y1)
            return 65;
        else if (x0 > x1 && y0 < y1)
            return 6;
        else if (x0 == x1 && y0 < y1)
            return 83;
        return 32;
    }

    // разрешает или запрещает перемещение по координатам
    private boolean isMove(int x0, int y0, int x1, int y1) {
        return world.isMove(x1, y1) && Matrix.allowDiagonalMove(world.getNodes(), x0, y0, x1, y1) &&
                !matrix.getPathTo(world.getNodes(), world.getNode(x0, y0), world.getNode(x1, y1)).isEmpty();
    }

    private void generateWorld(int rooms, int border, int minW, int maxW, int minH, int maxH) {
        worlds.add(new World(mapWidth, mapHeight));
        world = worlds.get(worlds.size() - 1);
        world.clear();
        world.fill(GameObject.WALL);
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
                newRoom = world.placeRoom(rx, ry, widthRoom, heightRoom);
                newRoom.setName("Комната " + world.getRooms().size());
                if (oldRoom != null) {
                    Corridor corridor = world.placeRoad(oldRoom.getAbsoluteCenterX(), oldRoom.getAbsoluteCenterY(), newRoom.getAbsoluteCenterX(), newRoom.getAbsoluteCenterY());
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
        world.addEnvironment(new GameObject(GameObject.LADDER_DOWN), placeLadderDown[0], placeLadderDown[1]);

        // перемещение игрока
        currentArea = world.getRooms().get(0);
        int[] placePlayer = placeFreeNeighbor(world.getRoomObjects(0));
        world.addEntity(player, placePlayer[0], placePlayer[1]);
        adjView(player);
        // если уровень ниже первого
        if (worlds.indexOf(world) > 0) {
            // то добавляется лестница наверх
            world.addEnvironment(new GameObject(GameObject.LADDER_UP), placePlayer[0], placePlayer[1]);
        }
        for (int i = 0; i < 1; i++) {
            int[] placeMonster = placeFreeNeighbor(world.getRoomObjects(getRandom(world.getRooms().size())));
            world.addEntity(new Npc(GameObject.MONSTER), placeMonster[0], placeMonster[1]);
        }

        setOpenObjectsView();
    }

    //    private void drawScreenGame() {
//        if (inputDialog)
//            return;
//        viewPlayer = world.getObjectWithEntity(getX(player) + Matrix.getDirectionXY(player.getDirection())[0],
//                getY(player) + Matrix.getDirectionXY(player.getDirection())[1]);
//        subPlayer = world.getObject(getX(player), getY(player));
//        screenMainGame.clear();
//        // рисует мир (world)
//        drawObjectsInHashMap(world.getIteratorObjects(0));
//        drawObjectsInHashMap(world.getIteratorObjects(1));
//
//        // прорисовка интерфейса
//        String text = "Уровень: " + (worlds.indexOf(world) + 1) +
//                "\nЛокация: " + currentArea.getName() + " x: " + getX(player) + " y: " + getY(player) +
//                "\nХод: " + tick +
//                "\nНаправление: " + player.getDirection() +
//                "\nВзляд: " + getViewInfo() +
//                "\nОбласть: " + getSubInfo();
//        // screenMainGame.text.setText(text);
//
//        screenMainGame.writeText(mapWidth + 1, 0, "уровень: " + (worlds.indexOf(world) + 1))
//                //.writeTextы(mapWidth + 1, 1, "Локация: " + currentArea.getName() + " x: " + getX(player) + " y: " + getY(player))
//                .writeText(mapWidth + 1, 1, "ЗД: " + player.getHp() + "/" + player.getHpMax())
//                .writeText(mapWidth + 1, 2, "поверхность: " + getSubInfo())
//                .writeText(0, mapHeight + 1, console[0])
//                .writeText(0, mapHeight + 2, console[1])
//                .writeText(0, mapHeight + 3, console[2])
//                .writeText(0, mapHeight + 4, console[3]);
//

//    }
    public void clickCanvas(int x, int y) {
        if (screenMainGame.canvas.isPlayerVisible(x, y) && world.isMove(x, y)) {
            if (world.isMove(x, y)) {
                int px = getX(player);
                int py = getY(player);
                player.path = matrix.getPathTo(world.getNodes(), world.getNode(px, py), world.getNode(x, y));
            }
        }
    }

    private void movePlayerNextCell() {
        if (!player.path.isEmpty()) {
            Node next = player.path.last();
            if (world.isMove(next.x, next.y)) {
                world.moveEntity(player, next.x, next.y);
                currentArea = world.getArea(next.x, next.y);
                // визуальное обновление окружения
                adjView(player);
                setOpenObjectsView();
                player.path.removeLast();
                tick++;
                updateAllMonsters();
                updateAllObjects();
//                if (!player.path.isEmpty()) {
//
//                   taskMove = new TimerTask() {
//                        @Override
//                        public void run() {
//                            System.out.println(taskMove.scheduledExecutionTime());
//                            tick++;
//                            updateAllMonsters();
//                            movePlayerNextCell();
//                        }
//                    };
//                    timerMove.schedule(taskMove, 100);
//                } else {
//                    tick++;
//                    updateAllMonsters();
//                }

            } else {
                player.path.clear();
            }
        }
    }

    private void updateAllMonsters() {
        // обновление хода для монстров
        // обход всех сущностей на карте (исключительно npc)
        for (GameObject npc : world.getAllObjects().getObjectsType(GameObject.MONSTER)) {
            boolean moveNpc = true;
            Npc npcNotPlayer = (Npc) npc;
            // npcNotPlayer.setTarget(null);
            // ищет объекты в своей зоне видимости
            for (GameObject obj : getSeeAllObjects(npcNotPlayer)) {
                // если монстр обнаруживает игрока (т.е. врага)
                if (obj.equals(player)) {
                    npcNotPlayer.setTarget(player);
                    break;
                }
            }
            // если цель монстра определена (т.е. обнаружен враг)
            if (npcNotPlayer.getTarget() != null) {
                boolean attackNpc = false;
                // перебирает все соседствующие клетки
                for (GameObject neighborEntity : getSeeAllObjects(npcNotPlayer, 1)) {
                    // если находит сущность и она является целью
                    if (neighborEntity.equals(npcNotPlayer.getTarget())) {
                        // то атакует
                        if (Matrix.allowDiagonalMove(world.getNodes(),
                                getX(npcNotPlayer), getY(npcNotPlayer),
                                getX(neighborEntity), getY(neighborEntity))) {
                            log(npcNotPlayer.attackEntity(npcNotPlayer.getTarget()));
                            moveNpc = false;
                            break;
                        }
                    }
                }
                if (moveNpc) {
                    // монстр строит путь до цели
                    Nodes path = getPath(npcNotPlayer, npcNotPlayer.getTarget());
                    // если до цели необходимо переместиться
                    if (!path.isEmpty()) {
                        // определяет самую ближайшую клетку для перемещения до цели
                        Node lastNode = path.get(path.size() - 1);
                        // флажок разрешения для преследования
                        boolean goLastNode = true;
                        // проверяет закреплён ли Npc к территории
                        if (((Npc) npc).attachedToArea) {
                            arealable currentArea = world.getArea(getX(npc), getY(npc));
                            arealable newArea = world.getArea(lastNode.x, lastNode.y);
                            if (currentArea != newArea) {
                                goLastNode = false;
                                System.out.println(npc.getName() + " больше не преследует игрока");
                            }
                        }
                        if (goLastNode) {
                            // то монстр начинает движение в сторону цели
                            log(npc.getName() + " преследует " + player.getName());
                            world.moveEntity(npcNotPlayer, lastNode.x, lastNode.y);
                        }

                    }
                }
            } else {
                // если цель монстра не определена (т.е. по близости нет врагов)
                // то он может может зализывать свои раны
                npcNotPlayer.recoveryHp();
            }
        }
    }
    private void updateAllObjects() {
        drawObjectsInHashMap(world.getIteratorObjects(0));
        drawObjectsInHashMap(world.getIteratorObjects(1));
        drawObjectsInHashMap(world.getIteratorObjects(2));
    }
    private void drawScreenGame() {
        screenMainGame.canvas.constrainMoveCanvas();
        screenMainGame.canvas.repaint();
    }

    private void log(String message) {
        for (int i = console.length - 1; i > 0; i--) {
            if (console[i - 1] != null) {
                console[i] = console[i - 1];
            }

        }
        console[0] = "[ход " + tick + "] " + message;
    }

    private void drawScreenDialog(String question, Runnable actionConfirm, Runnable actionCancel) {
        inputDialog = true;
        actionDialog = new Runnable[]{actionConfirm, actionCancel};
//        screenMainGame.clear();
//        screenMainGame.writeText(5, 5, question);
//        screenMainGame.writeText(6, 6, "1 - Да");
//        screenMainGame.writeText(6, 7, "2 - Нет");
//        screenMainGame.redrawDialog();
        // перерисовать экран
        screenMainGame.repaint();
    }

//    private String getViewInfo() {
//        return (viewPlayer != null) ? viewPlayer.getName() : "ничего";
//    }
//
//    private String getSubInfo() {
//        return (subPlayer != null) ? subPlayer.getName() : "ничего";
//    }

    //устанавливает объект в свободную от других объектов область
    private int[] placeFreeNeighbor(GameObjects objects) {
        int[] placeXY = new int[2];
        boolean place = false;
        while (!place) {
            GameObject placeObject = objects.getObjectsType(GameObject.FLOOR).getRandomObject();
            placeXY[0] = getX(placeObject);
            placeXY[1] = getY(placeObject);
            if (matrix.getNeighboring(world.getNodes(),
                    world.getNode(placeXY[0], placeXY[1])).size() == 8) {
                place = true;
            }
        }
        return placeXY;
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

    // возвращает ближайший к entity узел target
    public Node getNearNeighboring(Entity entity, Entity target) {
        return matrix.getNeighboring(world.getNodes(), world.getNode(getX(entity), getY(entity))).getNearest(getX(target), getY(target));
    }

    public Nodes getPath(Entity entity, Entity target) {
        return matrix.getPathTo(world.getNodes(), world.getNode(getX(entity), getY(entity)), getNearNeighboring(target, entity));
    }

    // чтение JSON файла из папки "data"
    public String readJSONToString(String filename) throws IOException, URISyntaxException {
        final String dir = System.getProperty("user.dir");
        FileInputStream in = new FileInputStream(dir + "\\data\\" + filename);
        return IOUtils.toString(in, StandardCharsets.UTF_8.name());
    }
}



