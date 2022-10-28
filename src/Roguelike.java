import com.game.roguelike.Direction;
import com.game.roguelike.Interface;
import com.game.roguelike.world.GameObject;
import com.game.roguelike.world.World;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Random;

public class Roguelike {

    private final Interface screenMainGame;
    private final World world;
    private static final int mapWidth = 100;
    private static final int mapHeight = 40;

    private final GameObject player;

    public Roguelike() {
        //объекты мира
        world = new World(mapWidth - 40, mapHeight - 2);
        player = new GameObject(GameObject.PLAYER);
        world.fill(GameObject.WALL);
        world.placeRoom(10, 10, 10, 6);
        GameObject placePlayer = world.getAllObjects().getObjectsType(GameObject.FLOOR).getRandomObject();
        world.addObject(player, world.getPositionX(placePlayer), world.getPositionY(placePlayer));

        //

        //объект обработки мира и рисования экрана
        screenMainGame = new Interface(mapWidth, mapHeight);
        int controlX = mapWidth - 38;

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
                    case 68: // нажата клавиша D
                        if (world.isMove(newX + 1, newY)) {
                            newX += 1;
                            move = true;
                        }
                        break;
                    case 65: // нажата клавиша A
                        if (world.isMove(newX - 1, newY)) {
                            newX -= 1;
                            move = true;
                        }
                        break;
                    case 87:  // нажата клавиша W
                        if (world.isMove(newX, newY - 1)) {
                            newY -= 1;
                            move = true;
                        }
                        break;
                    case 83:  // нажата клавиша S
                        if (world.isMove(newX, newY + 1)) {
                            newY += 1;
                            move = true;
                        }
                        break;
                    case 32:
                        world.clear();
                        world.fill(GameObject.WALL);
                        // количество комнат
                        int rooms = 5;
                        // рамка размещаемых комнат (минимальное расстояние между ними)
                        int border = 3;
                        // максимальное количество ошибок
                        int errors = 700;
                        while (rooms > 0 && errors > 0) {
                            int widthRoom = getRandom(8, 40);
                            int heightRoom = getRandom(5, 15);
                            int rx = getRandom(world.getSizeX() - widthRoom);
                            int ry = getRandom(world.getSizeY() - heightRoom);
                            int placeX = rx - border;
                            int placeY = ry - border;
                            int placeW = widthRoom + border * 2;
                            int placeH = heightRoom + border * 2;
                            if (world.isClearOfObject(placeX, placeY, placeW,
                                    placeH, GameObject.WALL)) {
                                world.placeRoom(rx, ry, widthRoom, heightRoom);
                                rooms--;
                            } else {
                                errors--;
                                if (errors == 0)
                                    System.out.println("Ошибка");
                            }
                        }

                        GameObject placePlayer = world.getAllObjects().getObjectsType(GameObject.FLOOR).getRandomObject();
                        world.addObject(player, world.getPositionX(placePlayer), world.getPositionY(placePlayer));
                        drawScreenGame();
                        break;
                }

                if (move) {
                    world.moveObject(player, newX, newY);
                    drawScreenGame();
                    screenMainGame.writeText(controlX, 1, "d");
                }
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {
            }
        });
        drawScreenGame();
    }

    private void drawScreenGame() {
        // очищает экран
        screenMainGame.clear();
        // рисует границы экрана
        screenMainGame.writeRect(0, 0, mapWidth, mapHeight, '+');
        // рисует разделитель экрана
        screenMainGame.writeLine(mapWidth - 39, 1, mapHeight - 2, Direction.DOWN, '+');

        // рисует мир (world)
        int x = 1, y = 1;
        HashMap<GameObject, int[]> objects = world.getIteratorObjects();
        for (HashMap.Entry<GameObject, int[]> entry : objects.entrySet()) {
            screenMainGame.writeSymbol(x + entry.getValue()[0], y + entry.getValue()[1], entry.getKey().getView(),
                    entry.getKey().getColorBg(), entry.getKey().getColorFg());
        }

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
}