import com.game.roguelike.screen.ScreenMainGame;
import com.game.roguelike.world.GameObject;
import com.game.roguelike.world.World;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class Roguelike {

    private ScreenMainGame screenMainGame;
    private World world;
    private static final int mapWidth = 100;
    private static final int mapHeight = 40;

    private GameObject player;

    public Roguelike(int width, int height) {
        //объекты мира
        world = new World(mapWidth - 40, mapHeight - 2);
        player = new GameObject(0);
        world.addObject(player, 0, 10);
        //

        //объект обработки мира и рисования экрана
        screenMainGame = new ScreenMainGame(world, mapWidth, mapHeight, 40);
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
                }
                if (move) {
                    world.moveObject(player, newX, newY);
                    screenMainGame.update();
                }
            }

            @Override
            public void keyReleased(KeyEvent keyEvent) {
            }
        });
        screenMainGame.update();
    }

    public static void main(String[] args) {
        Roguelike game = new Roguelike(mapWidth, mapHeight);
    }
}