package com.game.roguelike.matrix;

import com.game.roguelike.Roguelike;

import java.util.Arrays;

public class Matrix {
    public int[][] matrixShearch;
    public int[][] matrixLine;
    public int[] matrixRadius;

    private final int gridX;
    private final int gridY;
    public static int NULL = -1;
    private final IntegerList[] radiusList;
    final public int maxRadius; // максимальный радиус

    public Matrix(int maxRadius, int gridX, int gridY) {
        this.gridX = gridX;
        this.gridY = gridY;
        this.maxRadius = maxRadius;
        int center = maxRadius + 1;
        int size = center * 2 - 1;
        matrixShearch = new int[(int) Math.floor(Math.pow(size, 2)) + 1][2];

        // формирование матриц поиска
        int n = 0;
        for (int ix = -maxRadius; ix < maxRadius + 1; ix++) {
            for (int iy = -maxRadius; iy < maxRadius + 1; iy++) {
                matrixShearch[n][0] = iy;
                matrixShearch[n][1] = ix;
                n++;
            }
        }

        radiusList = new IntegerList[maxRadius];
        for (int i = 0; i < maxRadius; i++)
            radiusList[i] = new IntegerList();
        int start = center * size - center;
        int current;

        for (int q = 0; q < 4; q++) {
            int zx, zy;
            if (q == 0) {
                zx = 1;
                zy = -1;

            } else if (q == 1) {
                zx = 1;
                zy = 1;

            } else if (q == 2) {
                zx = -1;
                zy = -1;

            } else {

                zx = -1;
                zy = 1;
            }

            for (int ih = 0; ih < maxRadius; ih++) {
                int currentRadius = ih;
                current = start + ((size + zx * zy) * (ih + 1)) * zy;
                radiusList[currentRadius].appendUnique(current);
                radiusList[currentRadius].appendUnique(start + ((ih + 1) * zx));
                radiusList[currentRadius].appendUnique(start + size * (ih + 1) * zy);

                currentRadius++;
                for (int i = 1; i < maxRadius - ih; i++) {
                    radiusList[currentRadius].appendUnique(current + i * zx);
                    radiusList[currentRadius].appendUnique(current + size * i * zy);
                    currentRadius++;
                }
            }
        }
        IntegerList allCells = new IntegerList();
        for (int i = 0; i < maxRadius; i++)
            allCells.append(radiusList[i]);
        matrixRadius = allCells.values();
        matrixLine = generateLOS(matrixShearch, radiusList[maxRadius - 1]);


    }

    // не статические методы
    private int[] getArrayDirection() {
        return radiusList[0].values();
    }

    private float getG(Node start, Node end) {
//        if (start.x == end.x || start.y == end.y)
//            return 1;
//        else
//            return 1.1f;
    return 1;
    }

    // методы класса
    private static int[][] generateLOS(int[][] matrix, IntegerList list) {
        int[] borders = list.values();
        int[][] lineLOS = new int[borders.length][];
        for (int i = 0; i < borders.length; i++)
            lineLOS[i] = getLineLOS(matrix, matrix[borders[i]][0], matrix[borders[i]][1]);
        return lineLOS;
    }

    private static int[] getLineLOS(int[][] matrix, int xend, int yend) { // формирует массив
        // координат
        // линии
        // прямого взгляда (взято с
        // википедии)
        IntegerList line = new IntegerList();
        int x, y, dx, dy, incx, incy, pdx, pdy, es, el, err;
        dx = xend;
        dy = yend;
        incx = sign(dx);
        incy = sign(dy);
        if (dx < 0)
            dx = -dx;
        if (dy < 0)
            dy = -dy;
        if (dx > dy) {
            pdx = incx;
            pdy = 0;
            es = dy;
            el = dx;
        } else {
            pdx = 0;
            pdy = incy;
            es = dx;
            el = dy;
        }
        x = 0;
        y = 0;
        err = el / 2;
        for (int t = 0; t < el; t++) {
            err -= es;
            if (err < 0) {
                err += el;
                x += incx;
                y += incy;
            } else {
                x += pdx;
                y += pdy;
            }
            int index = getMatrixIndex(matrix, x, y);
            if (index != -1)
                line.append(index);
        }
        return line.values();
    }

    private static int getMatrixIndex(int[][] matrix, int x, int y) {
        for (int i = 0; i < matrix.length; i++) {
            if (matrix[i][0] == x && matrix[i][1] == y)
                return i;
        }
        return -1;
    }

    private static int sign(int x) {
        return Integer.compare(x, 0);
    }

    public Nodes getPathTo(Node[][] nodes, Node start, Node target) {
        Nodes open = new Nodes();
        if (target == null)
            return open;
        if (start == target) {
            open.add(target);
            return open;
        }
        Nodes close = new Nodes();
        Node current;
        start.g = 0;
        start.h = getHeuristic(start, target);
        start.f = start.g + start.h;
        start.setParent(null);
        open.add(start);
        while (!open.isEmpty()) {
            current = open.getMinF();
            if (close.size() > 10000)
                break;
            if (current.x == target.x && current.y == target.y) {
                return getReconstructPath(target);
            }
            open.remove(current);
            close.add(current);
            for (Node part : getNeighboring(nodes, current, target, true, false)) {
                if (!close.contains(part)) {
                    if (open.contains(part)) {
                        if (part.g > current.g)
                            updateF(current, part, target);
                    } else {
                        updateF(current, part, target);
                        open.add(part);
                    }
                }
            }
        }
        return new Nodes();
    }
    public float getHeuristic(Node start, Node target) {
        return Nodes.dist(start.x, start.y,
                target.x, target.y);
    }

    void updateF(Node current, Node neighbor, Node target) {
        neighbor.setParent(current);
        neighbor.g = current.g + getG(current, neighbor);
        neighbor.h = getHeuristic(neighbor, target);
        neighbor.f = neighbor.g + neighbor.h;
    }

    // возвращает список координат соседних клеток
    public Nodes getNeighboring(Node[][] nodes, Node start, Node target, boolean checkSolid, boolean light) {
        Nodes cells = new Nodes();
        int[] neighbor;
        if (target != null)
            neighbor = getArrayDirection();
        else
            neighbor = radiusList[0].values();
        for (int i = 0; i < neighbor.length; i++) {
            int tempX = start.x + matrixShearch[neighbor[i]][0];
            int tempY = start.y + matrixShearch[neighbor[i]][1];
            if (tempX >= 0 && tempX < nodes.length && tempY >= 0 && tempY < nodes[0].length) {
                if (!checkSolid
                        || (!nodes[tempX][tempY].isSolid() && (light || allowDiagonalMove(nodes, tempX, tempY, start.x, start.y)))) {
                    cells.add(nodes[tempX][tempY]);
                }
            }
        }
        return cells;
    }

    public Nodes getNeighboring(Node[][] nodes, Node start) {
        return getNeighboring(nodes, start, null, true, false);
    }
    public static int getDirection(int x0, int y0, int x1, int y1) {
        if (x0 < x1 && y0 < y1)
            return 0;
        else if (x0 < x1 && y0 == y1)
            return 1;
        else if (x0 < x1 && y0 > y1)
            return 2;
        else if (x0 == x1 && y0 > y1)
            return 3;
        else if (x0 > x1 && y0 > y1)
            return 4;
        else if (x0 > x1 && y0 == y1)
            return 5;
        else if (x0 > x1 && y0 < y1)
            return 6;
        else if (x0 == x1 && y0 < y1)
            return 7;
        return -1;
    }

    public static int[] getDirectionXY(int direction) {
        if (direction == 0)
            return new int[]{1, 1};
        else if (direction == 1)
            return new int[]{1, 0};
        else if (direction == 2)
            return new int[]{1, -1};
        else if (direction == 3)
            return new int[]{0, -1};
        else if (direction == 4)
            return new int[]{-1, -1};
        else if (direction == 5)
            return new int[]{-1, 0};
        else if (direction == 6)
            return new int[]{-1, 1};
        else if (direction == 7)
            return new int[]{0, 1};
        return new int[]{0, 0};
    }

    static Nodes getReconstructPath(Node start) {
        Nodes map = new Nodes();
        Node current = start;
        while (current.getParent() != null) {
            map.add(current);
            current = current.getParent();
        }
        return map;
    }

    static boolean getDiagonal(int startX, int startY, int endX, int endY) {
        return startX != endX && startY != endY;
    }

    // разрешает или запрещает перемещение по диагонали
    public static boolean allowDiagonalMove(Node[][] node, int x, int y, int curX, int curY) { // функция
        // разрешающая,
        // запрещающая
        // диагональное перемещение луча
        // света, либо перемещение
        if (getDiagonal(curX, curY, x, y)) {
            int resX1, resY1, resX2, resY2;
            resX1 = resX2 = curX;
            resY1 = resY2 = curY;
            if (curX < x && curY < y) {
                resX1 = curX + 1;
                resY2 = curY + 1;
            } else if (curX > x && curY > y) {
                resX1 = curX - 1;
                resY2 = curY - 1;
            } else if (curX > x && curY < y) {
                resX1 = curX - 1;
                resY2 = curY + 1;
            } else if (curX < x && curY > y) {
                resX1 = curX + 1;
                resY2 = curY - 1;
            }
            if (node[resX1][resY1].isSolid() || node[resX2][resY2].isSolid())
                return false;
            else
                return true;
        } else
            return true;
    }

    public static boolean allowDiagonalView(Node[][] node, int x, int y, int curX, int curY) { // функция
        // разрешающая,
        // запрещающая
        // диагональное перемещение луча
        // света, либо перемещение
        if (getDiagonal(curX, curY, x, y)) {
            int resX1, resY1, resX2, resY2;
            resX1 = resX2 = curX;
            resY1 = resY2 = curY;
            if (curX < x && curY < y) {
                resX1 = curX + 1;
                resY2 = curY + 1;
            } else if (curX > x && curY > y) {
                resX1 = curX - 1;
                resY2 = curY - 1;
            } else if (curX > x && curY < y) {
                resX1 = curX - 1;
                resY2 = curY + 1;
            } else if (curX < x && curY > y) {
                resX1 = curX + 1;
                resY2 = curY - 1;
            }
            if (!node[resX1][resY1].through && !node[resX2][resY2].through)
                return false;
            else
                return true;
        } else
            return true;
    }

    public static float getDiagonalAdj(int dir) {
        if (dir == 0 || dir == 2 || dir == 4 || dir == 6)
            return 1.4f;
        else
            return 1;
    }

    public int[] getAdjMoveXY(int tick, int dir, int moveCount) {
        int[] txy = getDirectionXY(dir);
        return new int[]{
                (int) (Roguelike.map(tick, 0, moveCount * getDiagonalAdj(dir), 0, gridX) * txy[0]),
                (int) (Roguelike.map(tick, 0, moveCount * getDiagonalAdj(dir), 0, gridY) * txy[1])};
    }

    // возвращает чистую матрицу обзора
    public static int[][] clearView(int[][] view) {
        for (int ix = 0; ix < view.length; ix++) {
            Arrays.fill(view[ix], NULL);
        }
        return view;
    }

    // создает матрицу обзора
    public static int[][] createView(int sizeX, int sizeY) {
        int[][] view = new int[sizeX][sizeY];
        for (int ix = 0; ix < sizeX; ix++) {
            for (int iy = 0; iy < sizeY; iy++)
                view[ix][iy] = NULL;
        }
        return view;
    }

    public int getGridY() {
        return gridY;
    }

    public int getGridX() {
        return gridX;
    }
}
