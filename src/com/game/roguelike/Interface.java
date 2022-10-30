package com.game.roguelike;
import asciiPanel.AsciiPanel;
import javax.swing.*;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;
import java.awt.*;
import java.awt.event.KeyListener;

public class Interface extends JFrame {

    final private AsciiPanel terminal;
    public JTextPane text;
    public Interface(int screenWidth, int screenHeight) {
        super("Roguelike Game v.0.0.1");
        terminal = new AsciiPanel(screenWidth, screenHeight);
        terminal.setMaximumSize(new Dimension(Math.round((screenWidth + 1.7f)  * terminal.getCharWidth()), Math.round((screenHeight + 2.3f)  * terminal.getCharHeight())));
        JPanel info = new JPanel();
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
         text = new JTextPane();
         text.setEditable(false);
         text.setForeground(Color.WHITE);
         text.setBackground(Color.BLACK);
         text.setFont(new Font(Font.MONOSPACED, Font.BOLD, 18));
        info.add(text);
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.X_AXIS));
        controlPanel.add(terminal);
        controlPanel.add(info);
        super.add(controlPanel);
        super.setSize(Math.round((screenWidth + 1.7f)  * terminal.getCharWidth()) + 320, Math.round((screenHeight + 2.3f)  * terminal.getCharHeight()));
        super.setVisible(true);
        super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        super.repaint();
    }
    public void addKeyListener(KeyListener listener) {
        text.addKeyListener(listener);
    }
    public void clear() {
        terminal.clear();
    }
    public void writeText(int x, int y, String text, Color colorBg, Color colorFg) {
        terminal.setCursorPosition(x, y);
        terminal.write(text, colorFg, colorBg);
    }
    public void writeText(int x, int y, String text) {
        writeText(x, y, text, Color.BLACK, Color.WHITE);
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
