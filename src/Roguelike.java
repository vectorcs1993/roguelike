import com.game.roguelike.Direction;
import com.game.roguelike.Interface;
import com.game.roguelike.world.*;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Random;

public class Roguelike {

    private final Interface screenMainGame;
    private final World world;
    private static final int mapWidth = 60;
    private static final int mapHeight = 40;

    private final Entity player;
    private Room currentRoom;
    private int tick;
    private final int controlX = 20;
    public Roguelike() {
        tick = 0;
        //объекты мира
        world = new World(mapWidth - 2, mapHeight - 2);
        player = new Entity(GameObject.PLAYER);
        currentRoom = null;
        //объект обработки мира и рисования экрана
        screenMainGame = new Interface(mapWidth, mapHeight);
        //controlX = mapWidth - 38;
        // генерация комнат
        generateWorld();
        screenMainGame.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent keyEvent) {


            }

            @Override
            public void keyPressed(KeyEvent keyEvent) {
                // перемещение персонажа
                int newX = world.getPositionX(player);
                int newY = world.getPositionY(player);
                boolean move = false;
                switch (keyEvent.getKeyCode()) {
                    case 68 -> { // нажата клавиша D
                        if (world.isMove(newX + 1, newY)) {
                            move = true;
                        }
                        newX += 1;
                    }
                    case 65 -> { // нажата клавиша A
                        if (world.isMove(newX - 1, newY)) {
                            move = true;
                        }
                        newX -= 1;
                    }
                    case 87 -> {  // нажата клавиша W
                        if (world.isMove(newX, newY - 1)) {
                            move = true;
                        }
                        newY -= 1;
                    }
                    case 83 -> {  // нажата клавиша S
                        if (world.isMove(newX, newY + 1)) {
                            move = true;
                        }
                        newY += 1;
                    }
                    case 32 -> generateWorld();
                }
                System.out.println(move);
                if (move) {

                    world.moveEntity(player, newX, newY);
                    currentRoom = world.getRoom(newX, newY);
                    if (world.getObject(newX,newY).type == GameObject.LADDER) {
                        generateWorld();
                    }
                    tick ++;
                }

                drawScreenGame();
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {
            }
        });
        drawScreenGame();
    }

    private void generateWorld() {
        world.clear();
        world.fill(GameObject.WALL);
        // количество комнат
        int rooms = 10;
        // рамка размещаемых комнат (минимальное расстояние между ними)
        int border = 3;
        // максимальное количество ошибок
        int errors = 700;
        Room oldRoom = null;
        Room newRoom = null;
        while (rooms > 0 && errors > 0) {
            int widthRoom = getRandom(8, 25);
            int heightRoom = getRandom(5, 10);
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
                System.out.println(world.getRooms().size());
                newRoom.setName("Room " + world.getRooms().size());
                if (oldRoom != null) {
                    world.placeRoad(oldRoom.getAbsoluteCenterX(), oldRoom.getAbsoluteCenterY(), newRoom.getAbsoluteCenterX(), newRoom.getAbsoluteCenterY());
                }
                // уменьшаем счетчик комнаты
                rooms--;
            } else {
                errors--;
            }
        }

        // установка лестницы
        GameObject placeLadder = world.getLastRoomObjects().getObjectsType(GameObject.FLOOR).getRandomObject();
        world.addObject(new GameObject(GameObject.LADDER), world.getPositionX(placeLadder), world.getPositionY(placeLadder));
        // перемещение игрока
        currentRoom = world.getRooms().get(0);
        GameObject placePlayer = world.getRoomObjects(0).getObjectsType(GameObject.FLOOR).getRandomObject();
        world.addEntity(player, world.getPositionX(placePlayer), world.getPositionY(placePlayer));

        drawScreenGame();
    }

    private void drawScreenGame() {
        // очищает экран
        screenMainGame.clear();
        // рисует границы экрана
        screenMainGame.writeRect(0, 0, mapWidth, mapHeight, '+');
        // рисует разделитель экрана
        // screenMainGame.writeLine(mapWidth - 39, 1, mapHeight - 2, Direction.DOWN, '+');
        // рисует мир (world)
        drawObjectsInHashMap(world.getIteratorObjects(0));
        drawObjectsInHashMap(world.getIteratorObjects(1));
        // прорисовка интерфейса
        if (currentRoom != null) {
            screenMainGame.text.setText("Dlvl: "+currentRoom.getName());
        } else {
            screenMainGame.text.setText("Dlvl: Corridor");
        }
        screenMainGame.text.setText(screenMainGame.text.getText()+"\nTick: "+tick);
        // перерисовать экран
        screenMainGame.repaint();
    }

    public static void main(String[] args) {
        new Roguelike();
    }

    public static int getRandom(int min, int max) {
        Random random = new Random();
        return random.nextInt(max - min) + min;
    }

    public static int getRandom(int num) {
        Random random = new Random();
        return random.nextInt(num);
    }

    private void drawObjectsInHashMap(HashMap<GameObject, int[]> objects) {
        int x = 1, y = 1;
        for (HashMap.Entry<GameObject, int[]> entry : objects.entrySet()) {
            screenMainGame.writeSymbol(x + entry.getValue()[0], y + entry.getValue()[1], entry.getKey().getView(),
                    entry.getKey().getColorBg(), entry.getKey().getColorFg());
        }
    }
}