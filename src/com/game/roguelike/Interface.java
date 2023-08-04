package com.game.roguelike;

import com.game.roguelike.world.GameObject;
import com.game.roguelike.world.Item;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;

public class Interface extends JFrame {
    final boolean[][] visible;
    final boolean[][] dark;
    private final JLabel statusInv;
    private final JButton panelInvItemUse;
    public JTextPane field;
    private JTextPane textStatLeft, textChar, textStatCenter;
    private CanvasStats canvasStatsPrimary, canvasStatsSecondary;
    private final JList<Item> invList;
    private final DefaultListModel<Item> inv;
    private final JTextArea infoOfItem;
    private JScrollPane paneScroll;
    JButton endStep;
    Document doc;
    Font fontTitle = new Font(Font.DIALOG, Font.BOLD, 22);
    Font font = new Font(Font.DIALOG, Font.PLAIN, 10);

    Font fontUI = new Font(Font.DIALOG, Font.PLAIN, 14);
    Color primary = Color.DARK_GRAY, secondary = Color.DARK_GRAY;
    private final XNCanvas canvas;
    private final Roguelike game;

    public Interface(Roguelike game, int mapWidth, int mapHeight) {
        super("Roguelike Game v.0.0.1");
        this.game = game;
        visible = new boolean[mapWidth][mapHeight];
        dark = new boolean[mapWidth][mapHeight];

        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
        field = new JTextPane();
        field.setEditable(false);
        field.setBackground(primary);
        //field.setFont(font);
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
        createStyle("green_white", Color.GREEN, Color.WHITE);
        createStyle("green_black", Color.GREEN, Color.BLACK);
        doc = field.getDocument();
        canvas = new XNCanvas(game, this, mapWidth, mapHeight);
        canvas.setPreferredSize(new Dimension(800, 400));
        canvas.setBackground(primary);
        canvas.setLayout(null);
        controlPanel.add(canvas);
        JPanel infoPanel = new JPanel();
        infoPanel.setPreferredSize(new Dimension(800, 200));
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.X_AXIS));
        paneScroll = new JScrollPane(field);
        paneScroll.setViewportView(field);
        field.setPreferredSize(new Dimension(300, 200));
        JPanel statusPanel = new JPanel();
        statusPanel.setBackground(primary);
        statusPanel.setPreferredSize(new Dimension(500, 200));
        statusPanel.setMinimumSize(new Dimension(500, 200));
        statusPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.Y_AXIS));
        JTabbedPane statusTabs = new JTabbedPane(JTabbedPane.LEFT);
        JPanel panelStat = new JPanel();
        panelStat.setLayout(new BoxLayout(panelStat, BoxLayout.X_AXIS));
        textStatLeft = new JTextPane();
        textStatLeft.setFont(fontUI);
        textStatLeft.setEditable(false);
        textStatCenter = new JTextPane();
        textStatCenter.setFont(fontUI);
        textStatCenter.setEditable(false);
        canvasStatsPrimary = new CanvasStats();
        canvasStatsPrimary.setPreferredSize(new Dimension(200, 200));
        panelStat.add(canvasStatsPrimary);
        canvasStatsSecondary = new CanvasStats();
        canvasStatsSecondary.setPreferredSize(new Dimension(200, 200));
        panelStat.add(canvasStatsSecondary);
        panelStat.add(textStatLeft);
        panelStat.add(textStatCenter);
        endStep = new JButton("Конец хода");
        endStep.addActionListener(e -> {
            game.endStep();
        });
        panelStat.add(endStep);
        statusTabs.addTab("СТАТ", panelStat);
        JPanel panelMainInventory = new JPanel();
        panelMainInventory.setLayout(new BoxLayout(panelMainInventory, BoxLayout.Y_AXIS));
        statusInv = new JLabel("");
        panelMainInventory.add(statusInv);
        JPanel panelInv = new JPanel();
        panelInv.setLayout(new BoxLayout(panelInv, BoxLayout.X_AXIS));
        inv = new DefaultListModel<>();
        invList = new JList<>(inv);
        invList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        invList.addListSelectionListener(e -> {
            int firstIndex = e.getFirstIndex();
            int lastIndex = e.getLastIndex();
            boolean isAdjusting = e.getValueIsAdjusting();
            if (!isAdjusting) {
                if (!invList.isSelectionEmpty()) {
                    game.selectItem(invList.getSelectedValue());

                }
            }
        });
        invList.setCellRenderer(new CountryRenderer());
        JScrollPane scrollPaneInv = new JScrollPane();
        scrollPaneInv.setViewportView(invList);
        invList.setLayoutOrientation(JList.VERTICAL);
        JPanel panelInvItemManual = new JPanel();
        panelInvItemManual.setLayout(new BoxLayout(panelInvItemManual, BoxLayout.Y_AXIS));
        JPanel panelInvActions = new JPanel();
        panelInvItemUse = new JButton("Применить");
        panelInvActions.add(panelInvItemUse);
        panelInvItemManual.add(panelInvActions);
        infoOfItem = new JTextArea();
        infoOfItem.setEditable(false);
        panelInvItemManual.add(infoOfItem);
        panelInv.add(scrollPaneInv);
        panelInv.add(new JScrollPane(panelInvItemManual));
        panelMainInventory.add(panelInv);
        statusTabs.addTab("ИНВ", panelMainInventory);
        JPanel panelChar = new JPanel();
        textChar = new JTextPane();
        textChar.setFont(fontUI);
        textChar.setEditable(false);
        panelChar.add(textChar);
        statusTabs.addTab("ХАР", panelChar);
        JPanel panelSkills = new JPanel();
        statusTabs.addTab("НАВ", panelSkills);
        JPanel panelMap = new JPanel();
        statusTabs.addTab("КАРТ", panelMap);
        // Подключение слушателя мыши
        statusTabs.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                // Определяем индекс выделенной мышкой вкладки
                int idx = ((JTabbedPane) e.getSource()).indexAtLocation(e.getX(), e.getY());
                if (idx != -1) {
                    game.selectTab(idx);
                    JDialog dialog = new JDialog(getFrame(), "sdsd", true);
                    dialog.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                    dialog.setSize(180, 90);
                    dialog.setLocationRelativeTo(getFrame());
                    dialog.setVisible(true);
                }
            }
        });
        statusPanel.add(statusTabs);
        infoPanel.add(statusPanel);
        infoPanel.add(paneScroll);
        controlPanel.add(infoPanel);

        setBackground(primary);
        super.setMinimumSize(new Dimension(800, 600));
        super.add(controlPanel);
        super.setLocationRelativeTo(null);
        super.setVisible(true);
        super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        super.repaint();
    }
    JFrame getFrame() {
        return this;
    }
    public DefaultListModel<Item> getListInventory() {
        return inv;
    }
    public JLabel getInventoryTextLabel() {
        return statusInv;
    }
    public JButton getInventoryButtonUse() {
        return panelInvItemUse;
    }


    public JTextArea getInventoryInfoOfItem() {
        return infoOfItem;
    }

    private class CountryRenderer extends JLabel implements ListCellRenderer<Item> {
        public CountryRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Item> list, Item item, int index,
                                                      boolean isSelected, boolean cellHasFocus) {

            String code = item.getName();
            ImageIcon imageIcon = new ImageIcon(game.data.getImage(item.getId()));

            setIcon(imageIcon);
            setText(code);
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            return this;
        }

    }
    public static class CanvasStats extends JPanel {
        private String[] parameters;
        private int[] values;
        private int[] maxValues;

        private int[] type;

        public static final int FORWARD = 1, BACKWARD = 2;

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            if (parameters != null && values != null && maxValues != null) {
                if (parameters.length > 0) {
                    int ax = 5, ay = 5;
                    int widthBar = 100;
                    for (int i = 0; i < parameters.length; i++) {
                        int heightText = g.getFontMetrics().getHeight();
                        int py = ay + (i * heightText) + (i + 1) * heightText;
                        g.setColor(Color.WHITE);
                        g.drawString(parameters[i], ax, py);
                        int w = (int) Roguelike.map(values[i], 0, maxValues[i], 0, widthBar);
                        if (type[i] == FORWARD) {
                            g.setColor(Color.GREEN);
                            g.fillRect(ax, py + 2, w, heightText);
                            g.setColor(Color.RED);
                            g.fillRect(ax + w, py + 2, widthBar - w, heightText);
                        } else if (type[i] == BACKWARD) {
                            g.setColor(Color.RED);
                            g.fillRect(ax, py + 2, w, heightText);
                            g.setColor(Color.GREEN);
                            g.fillRect(ax + w, py + 2, widthBar - w, heightText);

                        }
                        g.setColor(Color.BLACK);
                        String label = values[i] + "/" + maxValues[i];
                        g.drawString(label, ax + widthBar / 2 - g.getFontMetrics().stringWidth(label) / 2, py + heightText);
                    }
                }
            }
        }

        public void setData(String[] parameters, int[] values, int[] maxValues, int[] types) {
            this.parameters = parameters;
            this.values = values;
            this.maxValues = maxValues;
            this.type = types;
        }
    }

    public void addLog(String string, int tick, String style) {
        try {
            String date = "[ход " + tick + "]";
            doc.insertString(doc.getLength(), date + " ", field.getStyle("dark_gray_white"));
            doc.insertString(doc.getLength(), string + "\n", field.getStyle(style));
            field.setCaretPosition(doc.getLength());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void updateTextStatPrimary(String[] parameters, int[] values, int[] maxValues, int[] types) {
        canvasStatsPrimary.setData(parameters, values, maxValues, types);
        endStep.setEnabled(game.getCurrentStep().equals(game.player) && game.isPlayerDetected());
    }

    public void updateTextStatsSecondary(String[] parameters, int[] values, int[] maxValues, int[] types) {
        canvasStatsSecondary.setData(parameters, values, maxValues, types);
    }

    public void updateTextChar(String text) {
        textChar.setText(text);
    }

    public void draw() {
        canvas.constrainMoveCanvas();
        canvas.repaint();
        canvasStatsPrimary.repaint();
        canvasStatsSecondary.repaint();
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

    public void centerAlign(GameObject object) { // центрировать по игроку
        canvas.canvasX = 0;
        canvas.canvasY = 0;
        canvas.px0 = 0;
        canvas.py0 = 0;
        canvas.moveCanvas(
                -canvas.getCoordinateMatrix(game.getX(object),
                        game.getY(object))[0] - 22 + canvas.getWidth() / 2,
                -canvas.getCoordinateMatrix(game.getX(object),
                        game.getY(object))[1] - 11 + canvas.getHeight() / 2);

    }
}
