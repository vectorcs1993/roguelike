package com.game.roguelike;


import com.game.roguelike.matrix.Matrix;
import com.game.roguelike.matrix.Node;
import com.game.roguelike.world.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class XNCanvas extends JPanel {
    Interface i;
    Roguelike game;
    int w, h;
    double dK = 0;
    double dJ = 0;

    Graphics2D g2d;
    AffineTransform t, tClear;
    int boundX = -1, boundY = -1;
    public Polygon[][] shape;
    public Polygon shapeBounds;
    public int mouseX, mouseY, canvasX, canvasY;
    boolean drag = false;
    boolean allowDrag = false;
    int px0, py0;

    Label cursorPercent;

    XNCanvas(Roguelike game, Interface i, int w, int h) {
        this.game = game;
        this.i = i;
        this.w = w;
        this.h = h;
        setFocusable(true);
        g2d = (Graphics2D) this.getGraphics();
        cursorPercent = new Label();
        shape = new Polygon[w][h];
        canvasX = mouseX = 0;
        canvasY = mouseY = 0;
        px0 = 0;
        py0 = 0;
        for (int j = 0; j < w; j++) {
            for (int k = 0; k < h; k++) {
                int x0 = getCoordinateMatrix(j, k)[0];
                int y0 = getCoordinateMatrix(j, k)[1];
                shape[j][k] = new Polygon();
                shape[j][k].addPoint(x0 + game.getSizeIzoGridX(), y0);
                shape[j][k].addPoint(x0 + game.getSizeIzoGridX() * 2, y0 + game.getSizeIzoGridY());
                shape[j][k].addPoint(x0 + game.getSizeIzoGridX(), y0 + game.getSizeIzoGridX());
                shape[j][k].addPoint(x0, y0 + game.getSizeIzoGridY());
            }
        }
        shapeBounds = new Polygon();
        shapeBounds.addPoint((int) shape[0][0].getBounds2D().getCenterX(), (int) shape[0][0].getBounds2D().getMinY());
        shapeBounds.addPoint((int) shape[0][h - 1].getBounds2D().getMinX(), (int) shape[0][h - 1].getBounds2D().getCenterY());
        shapeBounds.addPoint((int) shape[w - 1][h - 1].getBounds2D().getCenterX(), (int) shape[w - 1][h - 1].getBounds2D().getMaxY());
        shapeBounds.addPoint((int) shape[w - 1][0].getBounds2D().getMaxX(), (int) shape[w - 1][0].getBounds2D().getCenterY());
        addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent mouseEvent) {
                mouseX = mouseEvent.getX();
                mouseY = mouseEvent.getY();
                if (allowDrag) {
                    drag = true;
                    moveCanvas(mouseX, mouseY);
                }

            }

            @Override
            public void mouseMoved(MouseEvent mouseEvent) {
                mouseX = mouseEvent.getX();
                mouseY = mouseEvent.getY();
                drag = false;
                boolean isObject = false;
                if (shapeBounds.contains(getMouseX(), getMouseY())) {
                    for (int j = 0; j < w; j++) {
                        if (!isObject) {
                            for (int k = 0; k < h; k++) {
                                if (shape[j][k].contains(getMouseX(), getMouseY())) {
                                    boundX = j;
                                    boundY = k;
                                    isObject = true;
                                    break;
                                }
                            }
                        } else {
                            break;
                        }
                    }
                } else {
                    boundX = -1;
                    boundY = -1;
                }
            }
        });
        addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                //System.out.println(game.world.objects[boundX][boundY][1]);
            }

            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                px0 = getMouseX();
                py0 = getMouseY();
                if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
                    if (shapeBounds.contains(getMouseX(), getMouseY())) {
                        for (int j = 0; j < w; j++) {
                            for (int k = 0; k < h; k++) {


                                if (shape[j][k].contains(getMouseX(), getMouseY())) {
                                    boundX = j;
                                    boundY = k;
                                    game.clickCanvas(boundX, boundY);
                                    return;
                                }

                            }
                        }
                    }
                } else if (mouseEvent.getButton() == MouseEvent.BUTTON2) {
                    allowDrag = true;
                }
            }

            @Override
            public void mouseReleased(MouseEvent mouseEvent) {
                if (mouseEvent.getButton() == MouseEvent.BUTTON2) {
                    allowDrag = false;
                }
            }

            @Override
            public void mouseEntered(MouseEvent mouseEvent) {

            }

            @Override
            public void mouseExited(MouseEvent mouseEvent) {
                boundX = -1;
                boundY = -1;
            }
        });
    }

    private class Label {
        public int x, y;
        public String text;
        private boolean visible;

        Label() {
            x = y = -1;
            text = "";
            visible = false;
        }

        public void update(int x, int y, String text) {
            this.x = x;
            this.y = y;
            this.text = text;
            setVisible(true);
        }

        public boolean isVisible() {
            return visible;
        }

        public void setVisible(boolean visible) {
            this.visible = visible;
        }
    }

    void moveCanvas(int newX, int newY) {
        int newCanvasX = (int) (newX - (px0 - shapeBounds.getBounds2D().getMinX()) - shapeBounds.getBounds2D().getMinX());
        int newCanvasY = (int) (newY - (py0 - shapeBounds.getBounds2D().getMinY()) - shapeBounds.getBounds2D().getMinY());
        moveTo(newCanvasX, newCanvasY);
    }

    void moveTo(int x, int y) {
        int[] d = getBorderDimensions();
        canvasX = (int) Roguelike.limit(
                x,
                d[0],
                d[1]
        );
        canvasY = (int) Roguelike.limit(
                y,
                d[2],
                d[3]
        );

    }

    void constrainMoveCanvas() {
        int[] d = getBorderDimensions();
        if (canvasX < d[0] || canvasX > d[1]
                || canvasY < d[2] || canvasY > d[3]) {
            moveTo(canvasX, canvasY);
        }
    }

    int[] getBorderDimensions() {
        int borderX = (int) game.getSettings().get("borderMovePlayerX") * game.getSizeGrid();
        int borderY = (int) game.getSettings().get("borderMovePlayerY") * game.getSizeGrid();
        int[] px = getCoordinateMatrix(game.getX(game.player), game.getY(game.player));
        return new int[]{
                -(px[0] - canvasX) - game.getSizeIzoGridX() + borderX,
                -(px[0] - canvasX) - game.getSizeIzoGridX() + getWidth() - borderX,
                -(px[1] - canvasY) + borderY,
                -(px[1] - canvasY) + getHeight() - borderY
        };
    }

    // возвращает координаты матрицы для изометрии
    int[] getCoordinateMatrix(int j, int k) {
        return new int[]{
                j * game.getSizeGrid() + j * -game.getSizeIzoGridY() + k * -game.getSizeIzoGridX() + game.getSizeIzoGridX() * w + canvasX,
                k * game.getSizeGrid() + j * game.getSizeIzoGridY() + k * -game.getSizeIzoGridX() + canvasY
        };
    }

    int getMouseX() {
        return mouseX - canvasX;
    }

    int getMouseY() {
        return mouseY - canvasY;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        int rateAdj = (int) Roguelike.map(game.currentTick, 0, game.rate, 0, game.getSizeGrid());
        cursorPercent.setVisible(false);
        g2d = (Graphics2D) g;
        g2d.setColor(Color.GRAY);
        tClear = (AffineTransform) g2d.getTransform().clone();
        int pX = -game.getSizeGrid() / 2, pY = -game.getSizeGrid() / 2;
        for (int j = 0; j < game.world.getSizeX(); j++) {
            for (int k = 0; k < game.world.getSizeY(); k++) {
                if (getCoordinateMatrix(j, k)[0] + game.getSizeIzoGridX() > -game.getSizeGrid() && getCoordinateMatrix(j, k)[0] + game.getSizeIzoGridX() < getWidth() + game.getSizeGrid() &&
                        getCoordinateMatrix(j, k)[1] + game.getSizeIzoGridY() > -game.getSizeGrid() && getCoordinateMatrix(j, k)[1] + game.getSizeIzoGridY() < getHeight() + game.getSizeGrid()) {
                    t = g2d.getTransform();
                    int x = getCoordinateMatrix(j, k)[0] + game.getSizeIzoGridX();
                    int y = getCoordinateMatrix(j, k)[1] + game.getSizeIzoGridY();
                    g2d.translate(x, y);
                    for (int l = 0; l < 3; l++) {
                        GameObject obj = game.world.objects[j][k][l];
                        GameObject objEntity = game.world.objects[j][k][2];
                        GameObject objEnvironment = game.world.objects[j][k][1];
                        if (obj != null) {
                            Image image = game.data.getImage(obj.getId());
                            if (isPlayerVisible(j, k)) {
                                if (obj.getType() == GameObject.FLOOR) {
                                    drawFloor(pX, pY, image);
                                    if (isCursorVisible(j, k)) {
                                        if (objEntity == null && objEnvironment == null) {
                                            drawMatrixShape(j, k, -2, -3, Color.WHITE);
                                        }
                                    }
                                } else if (obj.getType() == GameObject.WALL) {
                                    if (isPlayerNear(j, k) || (boolean) game.getSettings().get("hideWalls")) {
                                        drawFloor(pX, pY, game.data.getImage("roof"));
                                    } else {
                                        drawBlock(pX, pY, image, game.data.getImage("roof"));
                                    }
                                } else if (obj.getType() == GameObject.LADDER_DOWN) {
                                    drawImage(pX, pY - game.getSizeIzoGridY(), image);
                                    if (isCursorVisible(j, k) && objEntity == null) {
                                        drawImage(pX, pY - game.getSizeIzoGridY(), game.data.getImage("cursorAction"));
                                    }
                                } else if (obj.getType() == GameObject.LADDER_UP) {
                                    drawImage(pX, pY - game.getSizeIzoGridY(), image);
                                    if (isCursorVisible(j, k) && objEntity == null) {
                                        drawImage(pX, pY - game.getSizeIzoGridY(), game.data.getImage("cursorAction"));
                                    }
                                } else if (obj instanceof Player) {
                                    drawImage(pX, pY * 2, image);
                                } else if (obj instanceof Item) {
                                    drawImage(pX + image.getWidth(null) / 2,
                                            pY + image.getHeight(null) / 2
                                                    - game.getSizeIzoGridY() / 2, image);
                                    if (isCursorVisible(j, k)) {
                                        drawImage(pX, pY - 4, game.data.getImage("cursorAction"));
                                    }
                                } else if (obj.getType() == GameObject.MONSTER) {
                                    drawImage(pX, pY - game.getSizeIzoGridY(), image);
                                    Entity monster = (Entity) obj;
                                    if (monster.getHp() < monster.getHpMax()) {
                                        g2d.setColor(Color.GREEN);
                                        int curW = (int) Roguelike.map(monster.getHp(), 0, monster.getHpMax(),
                                                0, game.getSizeGrid());
                                        g2d.fillRect(pX, pY, curW, game.getSizeGrid() / 10);
                                        g2d.setColor(Color.RED);
                                        g2d.fillRect(pX + curW, pY, game.getSizeGrid() - curW, game.getSizeGrid() / 10);
                                    }
                                    if (isCursorVisible(j, k)) {
                                        drawImage(pX + game.data.getImage("arrowAttack").getWidth(null) / 2,
                                                pY + game.data.getImage("arrowAttack").getHeight(null) / 2
                                                        - game.getSizeIzoGridY() / 2, game.data.getImage("arrowAttack"));
                                        cursorPercent.update(x, y, game.getPercentAttack(game.player, monster) + "%");
                                    }
                                }
                            } else {
                                // drawRectDarkOrNotVisible(j, k);
                                if (i.visible[j][k]) {
                                    drawMatrixShapeFill(j, k, 0, 0, Color.DARK_GRAY);
                                }
                                if (isCursorVisible(j, k)) {
                                    if (objEntity == null && objEnvironment == null) {
                                        drawMatrixShape(j, k, -2, -3, Color.WHITE);
                                    }
                                }
                            }
                            if (!game.player.path.isEmpty()) {
                                if (game.player.path.contains(game.world.getNode(j, k))
                                        && !(j == boundX && k == boundY)) {
                                    drawImage(pX, pY * 2, game.data.getImage("trace"));
                                }
                            }
                        }

                    }
                    g2d.setTransform(t);
                }
            }
        }
        // лейблы
        if (cursorPercent.isVisible()) {
            drawLabel(cursorPercent, pX, pY);
        }
        g2d.setColor(Color.WHITE);
        g2d.setFont(i.fontTitle);
        String currentLevel = "Уровень: " + game.getLevel();
        g2d.drawString(currentLevel, getWidth() / 2 - g2d.getFontMetrics().stringWidth(currentLevel) / 2, 20);
        String currentStep = "Ход: " + game.getCurrentStep().getName();
        g2d.drawString(currentStep, getWidth() / 2 - g2d.getFontMetrics().stringWidth(currentStep) / 2, 40);
        g2d.setFont(i.font);
        g2d.drawString("boundX:" + boundX + " boundY: " + boundY, 20, 40);
        g2d.drawString("pX: " + game.getX(game.player) + " pY: " + game.getY(game.player), 20, 60);
        g2d.drawString("mouseX: " + mouseX + " mouseY: " + mouseY, 20, 80);
        g2d.drawString("dJ:" + dJ + " dK: " + dK, 20, 100);
        g2d.drawString("px0:" + px0 + " dK: " + dK, 20, 120);
        g2d.drawString("Memory Used: " + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024) / 1024
                + "/" + (Runtime.getRuntime().maxMemory() / 1024) / 1024 + " MB", 20, 130);
    }

    private void drawRectDarkOrNotVisible(int x, int y) {
        drawMatrixShape(x, y, 0, 0, (x == boundX && y == boundY && !drag) ? Color.RED : Color.LIGHT_GRAY);
    }

    private boolean isCursorVisible(int x, int y) {
        return x == boundX && y == boundY && !drag && game.getCurrentStep().equals(game.player);
    }

    //    public boolean isPlayerNear(int x, int y) {
//        return game.player.getViewXY(x - 1, y - 1) != -1
//                || (game.getX(game.player) == x - 1 && game.getY(game.player) == y - 1)
//                || (x == boundX + 1 && y == boundY + 1)
//                || (x == boundX + 1 && y == boundY)
//                || (x == boundX && y == boundY + 1);
//    }
    public boolean isPlayerNear(int x, int y) {
        boolean res = false;
        if (x < game.getX(game.player) && y < game.getY(game.player)) {
            return false;
        }
        for (Node node : game.matrix.getNeighboring(game.world.getNodes(), game.world.getNode(x, y), null, false, true)) {
            if (game.player.getViewXY(node.x, node.y) != -1) {
                res = true;
            }
        }
        return res;
    }

    public boolean isPlayerVisible(int x, int y) {
        return i.visible[x][y] && !i.dark[x][y];
    }

    public void drawLabel(Label label, int pX, int pY) {
        AffineTransform t = g2d.getTransform();
        g2d.translate(label.x, label.y);
        g2d.setColor(Color.WHITE);
        g2d.setFont(i.fontUI);
        g2d.drawString(label.text, pX + game.getSizeGrid() - game.getSizeGrid() / 4, pY + g2d.getFontMetrics().getHeight());
        g2d.setTransform(t);
    }

    public void drawMatrixShape(int x, int y, int px, int py, Color color) {
        AffineTransform t = g2d.getTransform();
        g2d.setTransform(tClear);
        g2d.setColor(color);
        g2d.translate(canvasX + px, canvasY + py);
        g2d.draw(shape[x][y]);
        g2d.setTransform(t);
    }

    public void drawMatrixShapeFill(int x, int y, int px, int py, Color color) {
        AffineTransform t = g2d.getTransform();
        g2d.setTransform(tClear);
        g2d.setColor(color);
        g2d.translate(canvasX + px, canvasY + py);
        g2d.fill(shape[x][y]);
        g2d.setTransform(t);
    }

    public void drawImage(int x, int y, Image sprite) {
        AffineTransform t = g2d.getTransform();
        g2d.drawImage(sprite, x, y + 4, null);
        g2d.setTransform(t);
    }

    public void drawFloor(int x, int y, Image texture) {
        AffineTransform t = g2d.getTransform();
        g2d.scale(1, 0.5);
        g2d.rotate(Math.toRadians(45));
        g2d.drawImage(texture, x, y + 1, null);
        g2d.setTransform(t);
    }

    public void drawBlock(int x, int y, Image texture, Image textureTop) {
        AffineTransform t = g2d.getTransform();
        g2d.scale(1, 0.5);
        g2d.rotate(Math.toRadians(45));
        g2d.drawImage(textureTop, x * 3 - 1, y * 3 - 2, null);
        g2d.setTransform(t);
        g2d.shear(0, 0.5);
        g2d.scale(0.7, 0.7);
        g2d.drawImage(texture, x * 2 - 1, y - 2, null);
        g2d.shear(0, -1);
        g2d.scale(1, 1);
        g2d.drawImage(texture, -1, y - 2, null);
        g2d.setTransform(t);
    }

    public void drawRoof(int x, int y, Image textureTop) {
        AffineTransform t = g2d.getTransform();
        g2d.scale(1, 0.5);
        g2d.rotate(Math.toRadians(45));
        g2d.drawImage(textureTop, x * 3 - 1, y * 3 - 2, null);
        g2d.setTransform(t);
    }
}
