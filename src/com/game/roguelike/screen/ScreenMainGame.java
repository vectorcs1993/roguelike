package com.game.roguelike.screen;

import com.game.roguelike.Direction;
import com.game.roguelike.Interface;
import com.game.roguelike.world.GameObject;
import com.game.roguelike.world.World;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;

public class ScreenMainGame extends Interface implements KeyListener {
    World world;
    int width, height, controlWidth;

    public ScreenMainGame(World world, int width, int height, int controlWidth) {
        super(width, height);
        this.world = world;
        this.width = width;
        this.height = height;
        this.controlWidth = controlWidth;
        update();
    }

    public void update() {
        // очищает экран
        clear();
        // рисует границы экрана
        writeRect(0, 0, width, height, '+');
        // рисует разделитель экрана
        writeLine(width - controlWidth + 1, 1, height - 2, Direction.DOWN, '+');

        // рисует мир (world)
        int x = 1, y = 1;
        HashMap<GameObject, int[]> objects = world.getIteratorObjects();
        for (HashMap.Entry<GameObject, int[]> entry : objects.entrySet()) {
            writeSymbol(x + entry.getValue()[0], y + entry.getValue()[1], entry.getKey().getView());
        }
        // перерисовать экран
        repaint();
    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {

    }

    @Override
    public void keyPressed(KeyEvent keyEvent) {

    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {

    }
}


