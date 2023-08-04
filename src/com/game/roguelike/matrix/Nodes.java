package com.game.roguelike.matrix;

import java.util.ArrayList;

public class Nodes extends ArrayList<Node> {
    public Node last() {
        if (isEmpty())
            return null;
        else
            return get(size() - 1);
    }
    public Node[] values() {
        Node[] array = new Node [this.size()];
        for (int i = 0; i < this.size(); i++) {
            array[i] = this.get(i);
        }
        return array;
    }
    public void removeLast() {
        if (!isEmpty())
            remove(size() - 1);
    }
    Nodes sortRandom() {
        IntegerList sort = new IntegerList();
        Nodes cells = new Nodes();
        for (int i = 0; i < this.size(); i++)
            sort.append(i);
        sort.shuffle();
        for (int i : sort)
            cells.add(this.get(i));
        return cells;
    }

    Node getMinF() {
        float[] s = new float[this.size()];
        for (int i = 0; i < this.size(); i++)
            s[i] = this.get(i).f;
        for (Node part : this) {
            if (part.f == min(s))
                return part;
        }
        return null;
    }

    // возвращает самый ближний объект относительно tx, ty
    public Node getNearest(int tx, int ty) {
        float[] dist = new float[this.size()];
        for (int i = 0; i < this.size(); i++)
            dist[i] = dist(this.get(i).x, this.get(i).y, tx, ty);
        for (Node part : this) {
            float tdist = dist(part.x, part.y, tx, ty);
            if (tdist == min(dist))
                return part;
        }
        return null;
    }

    // возвращает самый дальний объект относительно tx, ty
    Node getFar(int tx, int ty) {
        float[] dist = new float[this.size()];
        for (int i = 0; i < this.size(); i++)
            dist[i] = dist(this.get(i).x, this.get(i).y, tx, ty);
        for (Node part : this) {
            float tdist = dist(part.x, part.y, tx, ty);
            if (tdist == max(dist))
                return part;
        }
        return null;
    }

    // возвращает объекты начиная с самого ближайшего
    public Nodes getSortNear(int x, int y) {
        Nodes cells = new Nodes();
        Nodes temp = new Nodes();
        temp.addAll(this);
        while (!temp.isEmpty()) {
            Node cell = temp.getNearest(x, y);
            temp.remove(cell);
            cells.add(cell);
        }
        return cells;
    }

    // возвращает все объекты по краям карты
    Nodes getCellsIsBorders(int sizeX, int sizeY) {
        Nodes cells = new Nodes();
        for (Node cell : this) {
            if (cell.x == 0 || cell.y == 0 || cell.x == sizeX - 1 || cell.y == sizeY - 1)
                cells.add(cell);
        }
        return cells;
    }

    // возвращает все объекты входящие в определенный диапазон координат
    // (включительно)
    Nodes getCellsEntryCoord(int x1, int y1, int x2, int y2) {
        Nodes cells = new Nodes();
        for (Node cell : this) {
            if (cell.x >= x1 && cell.x <= x2 && cell.y >= y1 && cell.y <= y2)
                cells.add(cell);
        }
        return cells;
    }

    public static float dist(
            double x1,
            double y1,
            double x2,
            double y2) {
        return (float) Math.sqrt((y2 - y1) * (y2 - y1) + (x2 - x1) * (x2 - x1));
    }

    public static float max(float[] intArray) {
        float maxNum = intArray[0];
        for (float j : intArray) {
            if (j > maxNum)
                maxNum = j;
        }
        return maxNum;
    }

    public static float min(float[] intArray) {
        float minNum = intArray[0];
        for (float j : intArray) {
            if (j < minNum)
                minNum = j;
        }
        return minNum;
    }
}
