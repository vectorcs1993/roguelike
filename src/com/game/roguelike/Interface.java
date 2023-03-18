package com.game.roguelike;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.KeyListener;

public class Interface extends JFrame {
    final boolean[][] visible;
    final boolean[][] dark;
    public JTextPane field;
    Document doc;
    Color primary = new Color(50), secondary = Color.DARK_GRAY;
    public Image cursor, floor, wall, wall_top, monster, ladder_down;
//    int mouseX, mouseY;
    XNCanvas canvas;
    public Interface(Roguelike game, int mapWidth, int mapHeight) {
        super("Roguelike Game v.0.0.1");
        visible = new boolean[mapWidth][mapHeight];
        dark = new boolean[mapWidth][mapHeight];
        Font font = new Font(Font.MONOSPACED, Font.BOLD, 16);
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));

        field = new JTextPane();
        field.setEditable(false);
        field.setForeground(Color.GRAY);
        field.setBackground(Color.BLACK);
        field.setPreferredSize(new Dimension(800, 100));
        field.setFont(font);
        createStyle("gray_white", Color.GRAY, Color.white);
        createStyle("blue_gray", Color.BLUE, Color.gray);
        createStyle("black_white", Color.BLACK, Color.white);
        createStyle("dark_gray_white", Color.DARK_GRAY, Color.white);
        createStyle("dark_gray_light_gray", Color.DARK_GRAY, Color.GRAY);
        createStyle("blue_white", Color.BLUE, Color.white);
        createStyle("red_white", Color.RED, Color.white);
        createStyle("red_black", Color.RED, Color.BLACK);
        createStyle("white_black", Color.WHITE, Color.black);
        createStyle("black_black", Color.BLACK, Color.BLACK);
        doc = field.getDocument();
        canvas = new XNCanvas(game, this, mapWidth, mapHeight);
        canvas.setPreferredSize(new Dimension(800, 500));
        cursor = canvas.createImageIcon("human_maroder.png", 32, 32).getImage();
        floor = canvas.createImageIcon("terrain/floor3.png", 32, 32).getImage();
        monster = canvas.createImageIcon("entities/entity_rat.png", 32, 32).getImage();
        wall = canvas.createImageIcon("objects/wall1.png", 32, 32).getImage();
        wall_top = canvas.createImageIcon("objects/wall2.png", 32, 32).getImage();
        ladder_down = canvas.createImageIcon("objects/ladder_down.png", 32, 32).getImage();
        setBackground(primary);
        canvas.setBackground(primary);
        field.setBackground(secondary);
        controlPanel.add(canvas);
        controlPanel.add(field);

        super.setMinimumSize(new Dimension(800, 700));
        super.add(controlPanel);
        super.setLocationRelativeTo(null);
        super.setVisible(true);
        super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        super.repaint();
    }

    public void setOpen(int x, int y, boolean visible) {
        this.visible[x][y] = visible;
    }

    public void setDark(int x, int y, boolean value) {
        this.dark[x][y] = value;
    }

    private void createStyle(String name, Color bg, Color fg) {
        field.addStyle(name, null);
        StyleConstants.setBackground(field.getStyle(name), bg);
        StyleConstants.setForeground(field.getStyle(name), fg);
    }

    public void addKeyListener(KeyListener listener) {
        canvas.addKeyListener(listener);
    }

    public void centerAlignPlayer() { // центрировать по игроку
        canvas.moveCanvas(-300, 0);
    }
}
