package com.game.roguelike;


import com.game.roguelike.world.GameObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.util.Objects;

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

    XNCanvas(Roguelike game, Interface i, int w, int h) {
        this.game = game;
        this.i = i;
        this.w = w;
        this.h = h;
        setFocusable(true);
        g2d = (Graphics2D) this.getGraphics();
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
                shape[j][k].addPoint(x0 + 22, y0);
                shape[j][k].addPoint(x0 + 44, y0 + 11);
                shape[j][k].addPoint(x0 + 22, y0 + 22);
                shape[j][k].addPoint(x0, y0 + 11);
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
    int [] getBorderDimensions() {
        int border =(int) game.getSettings().get("borderMovePlayer") * 32;
        int[] px = getCoordinateMatrix(game.getX(game.player), game.getY(game.player));
        return new int[] {
                -(px[0] - canvasX) - 22 + border,
                -(px[0] - canvasX) -22 + getWidth() - border,
                -(px[1] - canvasY) + border,
                -(px[1] - canvasY)  + getHeight() - border
        };
    }
    // возвращает координаты матрицы для изометрии
    int[] getCoordinateMatrix(int j, int k) {
        return new int[]{
                j * 32 + j * -11 + k * -22 + 22 * w + canvasX, k * 32 + j * 11 + k * -22 + canvasY
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
        g2d = (Graphics2D) g;
        g2d.setColor(Color.GRAY);
        tClear = (AffineTransform) g2d.getTransform().clone();
        for (int j = 0; j < game.world.getSizeX(); j++) {
            for (int k = 0; k < game.world.getSizeY(); k++) {
                if (getCoordinateMatrix(j, k)[0] + 22 > -32 && getCoordinateMatrix(j, k)[0] + 22 < getWidth() + 32 &&
                        getCoordinateMatrix(j, k)[1] + 11 > -32 && getCoordinateMatrix(j, k)[1] + 11 < getHeight() + 32) {

                    int pX = -16, pY = -16;
                    t = g2d.getTransform();
                    g2d.translate(getCoordinateMatrix(j, k)[0] + 22, getCoordinateMatrix(j, k)[1] + 11);

                    for (int l = 0; l < 3; l++) {

                        GameObject obj = game.world.objects[j][k][l];
                        if (obj != null) {

                            if (isPlayerVisible(j, k)) {

                                if (obj.getType() == GameObject.FLOOR) {
                                    drawFloor(pX, pY, i.floor);
                                    if (j == boundX && k == boundY && !drag) {
                                        drawMatrixShape(j, k, -2, -3, Color.WHITE);
                                    }
                                } else if (obj.getType() == GameObject.WALL) {
                                    if (isPlayerNear(j, k) || (boolean)game.getSettings().get("hideWalls")) {
                                        drawFloor(pX, pY, i.wall_top);
                                    } else {
                                        drawBlock(i.wall, i.wall_top);
                                    }
                                } else if (obj.getType() == GameObject.LADDER_UP ||
                                        obj.getType() == GameObject.LADDER_DOWN) {
                                    drawObject(pX, pY * 2 + 3, i.ladder_down);
                                } else if (obj.getType() == GameObject.PLAYER) {
                                    drawObject(pX, pY * 2, i.cursor);
                                } else if (obj.getType() == GameObject.MONSTER) {
                                    drawObject(pX, pY * 2, i.monster);
                                }
                            } else {
                                drawRectDarkOrNotVisible(j, k);
                            }
                            if (!game.player.path.isEmpty()) {
                                if (game.player.path.contains(game.world.getNode(j, k))
                                        && !(j == boundX && k == boundY)) {
                                    drawMatrixShape(j, k, -2, -3, Color.GREEN);
                                }
                            }
                        }

                    }

                    g2d.setTransform(t);
                }
            }
        }
        g2d.setColor(Color.WHITE);
        g2d.drawString("boundX:" + boundX + " boundY: " + boundY, 20, 40);
        g2d.drawString("pX: " + game.getX(game.player) + " pY: " + game.getY(game.player), 20, 60);
        g2d.drawString("mouseX: " + mouseX + " mouseY: " + mouseY, 20, 80);
        g2d.drawString("dJ:" + dJ + " dK: " + dK, 20, 100);
        g2d.drawString("px0:" + px0 + " dK: " + dK, 20, 120);
        g2d.drawString("Memory Used: " + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024) / 1024
                + "/"+ (Runtime.getRuntime().maxMemory()/ 1024) / 1024+ " MB", 20, 130);
    }

    private void drawRectDarkOrNotVisible(int x, int y) {
       // drawMatrixShape(x, y, 0, 0, (x == boundX && y == boundY && !drag) ? Color.RED : Color.DARK_GRAY);
    }

    public boolean isPlayerNear(int x, int y) {
        return game.player.getViewXY(x - 1, y - 1) != -1
                || (game.getX(game.player) == x - 1 && game.getY(game.player) == y - 1)
                || (x == boundX + 1 && y == boundY + 1)
                || (x == boundX + 1 && y == boundY)
                || (x == boundX && y == boundY + 1);
    }

    public boolean isPlayerVisible(int x, int y) {
        return i.visible[x][y];
    }

//    public void drawMatrixRect(int x, int y) {
//        AffineTransform t = g2d.getTransform();
//        g2d.scale(1, 0.5);
//        g2d.rotate(Math.toRadians(45));
//        g2d.drawRect(x, y, 32, 32);
//        g2d.setTransform(t);
//    }

    public void drawMatrixShape(int x, int y, int px, int py, Color color) {
        AffineTransform t = g2d.getTransform();
        g2d.setTransform(tClear);
        g2d.setColor(color);
        g2d.translate(canvasX + px, canvasY + py);
        g2d.draw(shape[x][y]);
        g2d.setTransform(t);
    }

    public void drawObject(int x, int y, Image sprite) {
        AffineTransform t = g2d.getTransform();
        g2d.drawImage(sprite, x, y + 4, null);
        g2d.setTransform(t);
    }

    public void drawFloor(int x, int y, Image texture) {
        AffineTransform t = g2d.getTransform();
        g2d.scale(1, 0.5);
        g2d.rotate(Math.toRadians(45));
        g2d.drawImage(texture, x, y, null);
        g2d.setTransform(t);
    }

    public void drawBlock(Image texture, Image textureTop) {
        AffineTransform t = g2d.getTransform();
        g2d.scale(1, 0.5);
        g2d.rotate(Math.toRadians(45));
        g2d.drawImage(textureTop, -48, -48, null);
        g2d.setTransform(t);
        g2d.shear(0, 0.5);
        g2d.scale(0.75, 0.75);
        g2d.drawImage(texture, -28, -16, null);
        g2d.shear(0, -1);
        g2d.scale(1, 0.95);
        g2d.drawImage(texture, 0, -16, null);
        g2d.setTransform(t);
    }

    public ImageIcon createImageIcon(String image, int width, int height) {
        Image imageT = new ImageIcon(Objects.requireNonNull(getClass().getResource("/" + image))).getImage();
        Image newimgT = imageT.getScaledInstance(width, height, java.awt.Image.SCALE_FAST);
        return new ImageIcon(newimgT);
    }

//    public ImageIcon createImageIcon(Image imageT, int width, int height) {
//        Image newimgT = imageT.getScaledInstance(width, height, java.awt.Image.SCALE_FAST);
//        return new ImageIcon(newimgT);
//    }
}
