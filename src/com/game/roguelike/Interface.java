package com.game.roguelike;
import asciiPanel.AsciiPanel;
import javax.swing.*;
import java.awt.Color;

public class Interface extends JFrame {

    final private AsciiPanel terminal;

    public Interface(int screenWidth, int screenHeight) {
        super("Roguelike Game v.0.0.1");
        terminal = new AsciiPanel(screenWidth, screenHeight);
        super.add(terminal);
        super.setSize(Math.round((screenWidth + 1.7f)  * terminal.getCharWidth()), Math.round((screenHeight + 2.3f)  * terminal.getCharHeight()));
        super.setVisible(true);
        super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        super.repaint();
    }
    public void clear() {
        terminal.clear();
    }
    public void writeText(int x, int y, String text, int colorBg, int colorFg) {
        terminal.setCursorPosition(x, y);
        terminal.write(text, colorBg,colorFg);
    }
    public void writeText(int x, int y, String text) {
        terminal.setCursorPosition(x, y);
        terminal.write(text);
    }
    public void writeSymbol(int x, int y, char symbol) {
        terminal.setCursorPosition(x, y);
        terminal.write(symbol);
    }
    public void writeSymbol(int x, int y, char symbol, Color colorBg, Color colorFg) {
        terminal.setCursorPosition(x, y);
        terminal.write(symbol, colorFg, colorBg );
    }
    public void writeRect(int x, int y, int w, int h, char symbol) {
        writeLine(x, y, w, Direction.RIGHT, symbol);
        writeLine(x + w - 1, y + 1, h - 1, Direction.DOWN, symbol);
        writeLine(x, y + h - 1, w - 1, Direction.RIGHT, symbol);
        writeLine(x , y + 1, h - 2, Direction.DOWN, symbol);
    }
    public void writeLine(int x, int y, int s, int direction, char symbol) {
        if (direction == Direction.RIGHT) {
            for (int i = 0; i < s; i++) {
                writeSymbol(x+i, y, symbol);
            }
        } else if (direction == Direction.LEFT) {
            for (int i = 0; i < s; i++) {
                writeSymbol(x - i, y, symbol);
            }
        } else  if (direction == Direction.DOWN) {
            for (int i = 0; i < s; i++) {
                writeSymbol(x, y+ i, symbol);
            }
        } else  if (direction == Direction.UP) {
            for (int i = 0; i < s; i++) {
                writeSymbol(x, y- i, symbol);
            }
        }
    }
}
