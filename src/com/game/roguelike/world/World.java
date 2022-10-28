package com.game.roguelike.world;

import java.util.HashMap;

public class World {
    private GameObject[][] objects;

    public World(int sizeX, int sizeY) {
        objects = new GameObject[sizeX][sizeY];
    }
    public void addObject(GameObject object, int x, int y) {
        this.objects[x][y] = object;
    }

    public int getSizeX() {
        return this.objects.length;
    }

    public int getSizeY() {
        return this.objects[0].length;
    }

    // возвращает список всех объектов
    public GameObjects getAllObjects() {
        GameObjects objects = new GameObjects();
        for (int ix = 0; ix < getSizeX(); ix++) {
            for (int iy = 0; iy < getSizeY(); iy++) {
                if (this.objects[ix][iy] != null)
                    objects.add(this.objects[ix][iy]);
            }
        }
        return objects;
    }
    public GameObject getObject(int x, int y) {
        for (int ix = 0; ix < getSizeX(); ix++) {
            for (int iy = 0; iy < getSizeY(); iy++) {
                if (this.objects[ix][iy] != null) {
                    if (ix == x && iy == y) {
                        return this.objects[ix][iy];
                    }
                }

            }
        }
        return null;
    }
    public void clear() {
        for (int ix = 0; ix < getSizeX(); ix++) {
            for (int iy = 0; iy < getSizeY(); iy++) {
                this.objects[ix][iy] = null;
            }
        }
    }
    public void fill(int type) {
        for (int ix = 0; ix < getSizeX(); ix++) {
            for (int iy = 0; iy < getSizeY(); iy++) {
                this.objects[ix][iy] = new GameObject(type);
            }
        }
    }
    // залить определенную область объектами
    public void fill(int x, int y, int width, int height, int type) {
        for (int ix = 0; ix < width; ix++) {
            for (int iy = 0; iy < height; iy++) {
                this.objects[x + ix][ y + iy] = new GameObject(type);
            }
        }
    }
    // проверка на возможность перемещения в указанные координаты
    public boolean isMove(int x, int y) {
        if (x >= 0 && y >= 0 && x < getSizeX() && y < getSizeY()) {
            if (this.objects[x][y] != null) {
                if (this.objects[x][y].type == GameObject.FLOOR) {
                    // движение разрешено
                    return true;
                }
            }
        }
        // движение запрещено
        return false;
    }
    public void removeObject(GameObject object) {
        for (int ix = 0; ix < getSizeX(); ix++) {
            for (int iy = 0; iy < getSizeY(); iy++) {
                if (this.objects[ix][iy] != null) {
                    if (this.objects[ix][iy].equals(object)) {
                        this.objects[ix][iy] = null;
                    }
                }
            }
        }
    }
    public void placeRoom(int x, int y, int width, int height) {
        fill(x, y, width, height, GameObject.FLOOR);
    }
    public boolean isClearOfObject( int x, int y, int width, int height, int type) {
        if (x < 0 || y < 0 || x + width > objects.length || y + height > objects[0].length)
            return false;
        for (int ix=0; ix<width; ix++) {
            for (int iy=0; iy<height; iy++) {
                if (objects[x+ix][y+iy].type != type)
                    return false;
            }
        }
        return true;
    }
    public void moveObject(GameObject object, int x, int y) {
        int prevX = getPositionX(object);
        int prevY = getPositionY(object);
        int newX = (int) limit(x, 0, getSizeX() - 1);
        int newY = (int) limit(y, 0, getSizeY() - 1);
        removeObject(object);
        objects[newX][newY] = object;
        objects[prevX][prevY] = new GameObject(GameObject.FLOOR);
    }
    public static double limit(double d, double min, double max) {
        return Math.min(Math.max(d, min), max);
    }
    public HashMap<GameObject, int[]> getIteratorObjects() {
        HashMap<GameObject, int[]> map = new HashMap<>();
        for (int ix = 0; ix < getSizeX(); ix++) {
            for (int iy = 0; iy < getSizeY(); iy++) {
                if (this.objects[ix][iy] != null)
                    map.put(this.objects[ix][iy], new int[]{ix, iy});
            }
        }
        return map;
    }

    public int getPositionX(GameObject object) {
        return getPosition(object)[0];
    }

    public int getPositionY(GameObject object) {
        return getPosition(object)[1];
    }

    private int[] getPosition(GameObject object) {
        for (int ix = 0; ix < getSizeX(); ix++) {
            for (int iy = 0; iy < getSizeY(); iy++) {
                    if (this.objects[ix][iy] != null)
                        if (this.objects[ix][iy].equals(object))
                            return new int[] { ix, iy };
            }
        }
        return new int[] { -1, -1 };
    }
}
