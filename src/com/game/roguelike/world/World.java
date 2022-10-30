package com.game.roguelike.world;

import com.game.roguelike.Direction;

import java.util.ArrayList;
import java.util.HashMap;

public class World {
    private final GameObject[][][] objects;
    private final ArrayList<Room> rooms;

    public World(int sizeX, int sizeY) {
        objects = new GameObject[sizeX][sizeY][2];
        rooms = new ArrayList<Room>();
    }

    public void addObject(GameObject object, int x, int y) {
        this.objects[x][y][0] = object;
    }

    public void addEntity(Entity entity, int x, int y) {
        this.objects[x][y][1] = entity;
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
                if (this.objects[ix][iy][0] != null)
                    objects.add(this.objects[ix][iy][0]);
            }
        }
        return objects;
    }

    public GameObjects getAllObjects(int x0, int y0, int x1, int y1) {
        GameObjects objects = new GameObjects();
        for (int ix = 0; ix < getSizeX(); ix++) {
            for (int iy = 0; iy < getSizeY(); iy++) {
                if (ix >= x0 && iy >= y0 && ix < x1 && iy < y1) {
                    if (this.objects[ix][iy][0] != null) {
                        objects.add(this.objects[ix][iy][0]);
                    }
                }
            }
        }
        return objects;
    }

    public GameObject[][] getObjects(int x0, int y0, int x1, int y1) {
        GameObject[][] objects = new GameObject[x1 - x0][y1 - y0];
        for (int ix = 0; ix < x1 - x0; ix++) {
            for (int iy = 0; iy < y1 - y0; iy++) {
                if (this.objects[x0 + ix][y0 + iy][0] != null) {
                    objects[ix][iy] = this.objects[x0 + ix][y0 + iy][0];
                }
            }
        }
        return objects;
    }

    public GameObject getObject(int x, int y) {
        for (int ix = 0; ix < getSizeX(); ix++) {
            for (int iy = 0; iy < getSizeY(); iy++) {
                if (ix == x && iy == y) {
                    if (this.objects[ix][iy][0] != null) {
                        return this.objects[ix][iy][0];
                    }
                }

            }
        }
        return null;
    }

    // полное удаление объектов
    public void clear() {
        for (Room room : rooms) {
            room.clear();
        }
        rooms.clear();
        for (int ix = 0; ix < getSizeX(); ix++) {
            for (int iy = 0; iy < getSizeY(); iy++) {
                for (int iz = 0; iz < 2; iz++) {
                    this.objects[ix][iy][iz] = null;
                }
            }
        }
    }

    public void fill(int type) {
        for (int ix = 0; ix < getSizeX(); ix++) {
            for (int iy = 0; iy < getSizeY(); iy++) {
                this.objects[ix][iy][0] = new GameObject(type);
            }
        }
    }

    // залить определенную область объектами
    public int[][][] fill(int x, int y, int width, int height, int type) {
        int[][][] matrix = new int[width][height][2];
        for (int ix = 0; ix < width; ix++) {
            for (int iy = 0; iy < height; iy++) {
                matrix[ix][iy] = new int[]{x + ix, y + iy};
                addObject(new GameObject(type), x + ix, y + iy);
            }
        }
        return matrix;
    }

    // проверка на возможность перемещения в указанные координаты
    public boolean isMove(int x, int y) {
        if (x >= 0 && y >= 0 && x < getSizeX() && y < getSizeY()) {
            return !getObject(x, y).getSolid();
        }
        // движение запрещено
        return false;
    }

    public void removeObject(GameObject object) {
        for (int ix = 0; ix < getSizeX(); ix++) {
            for (int iy = 0; iy < getSizeY(); iy++) {
                if (this.objects[ix][iy][0] != null) {
                    if (this.objects[ix][iy][0].equals(object)) {
                        this.objects[ix][iy][0] = null;
                        this.objects[ix][iy][0] = new GameObject(GameObject.FLOOR);
                    }
                }
            }
        }
    }

    public void removeEntity() {
        for (int ix = 0; ix < getSizeX(); ix++) {
            for (int iy = 0; iy < getSizeY(); iy++) {
                if (this.objects[ix][iy][1] != null) {
                    this.objects[ix][iy][1] = null;
                }
            }
        }
    }

    public Room placeRoom(int x, int y, int width, int height) {
        Room room = new Room(fill(x, y, width, height, GameObject.FLOOR), x, y);
        rooms.add(room);
        return room;
    }

    public ArrayList<Room> getRooms() {
        return rooms;
    }

    public Room getRoom(int x, int y) {
        for (Room room : rooms) {
            if ((x >= room.getX() && x < room.getX() + room.getWidth()) &&
                    (y >= room.getY() && y < room.getY() + room.getHeight())) {
                return room;
            }
        }
        return null;
    }

    public Room getLastRoom() {
        return rooms.get(rooms.size() - 1);
    }

    public GameObjects getRoomObjects(int index) {
        int[][][] matrix = getRooms().get(index).getMatrix();
        GameObjects objects = new GameObjects();
        for (int[][] ints : matrix) {
            for (int iy = 0; iy < matrix[0].length; iy++) {
                int[] cell = ints[iy];
                objects.add(this.objects[cell[0]][cell[1]][0]);
            }
        }
        return objects;
    }

    public GameObjects getLastRoomObjects() {
        return getRoomObjects(getRooms().size() - 1);
    }

    public void placeRoad(int x0, int y0, int x1, int y1) {
        if (x1 > x0) {
            placeCorridor(x0, y0, x1 - x0, Direction.RIGHT, GameObject.FLOOR);
        } else if (x1 < x0) {
            placeCorridor(x0, y0, x0 - x1, Direction.LEFT, GameObject.FLOOR);
        }
        if (y1 > y0) {
            placeCorridor(x0 + (x1 - x0), y0, y1 - y0, Direction.DOWN, GameObject.FLOOR);
        }
        if (y1 < y0) {
            placeCorridor(x0 - (x0 - x1), y0, y0 - y1, Direction.UP, GameObject.FLOOR);
        }
    }

    public void placeCorridor(int x, int y, int s, int direction, int type) {
        if (direction == Direction.RIGHT) {
            for (int i = 0; i < s; i++) {
                addObject(new GameObject(type), x + i, y);
            }
        } else if (direction == Direction.LEFT) {
            for (int i = 0; i < s; i++) {
                addObject(new GameObject(type), x - i, y);
            }
        } else if (direction == Direction.DOWN) {
            for (int i = 0; i < s; i++) {
                addObject(new GameObject(type), x, y + i);
            }
        } else if (direction == Direction.UP) {
            for (int i = 0; i < s; i++) {
                addObject(new GameObject(type), x, y - i);
            }
        }
    }

    public boolean isClearOfObject(int x, int y, int width, int height, int type) {
        if (x < 0 || y < 0 || x + width > objects.length || y + height > objects[0].length)
            return false;
        for (int ix = 0; ix < width; ix++) {
            for (int iy = 0; iy < height; iy++) {
                if (objects[x + ix][y + iy][0].type != type)
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
        objects[newX][newY][0] = object;
        objects[prevX][prevY][0] = new GameObject(GameObject.FLOOR);
    }

    public void moveEntity(Entity entity, int x, int y) {
        int prevX = getPositionX(entity);
        int prevY = getPositionY(entity);
        objects[x][y][1] = entity;
        objects[prevX][prevY][1] = null;
    }

    public static double limit(double d, double min, double max) {
        return Math.min(Math.max(d, min), max);
    }

    public HashMap<GameObject, int[]> getIteratorObjects(int z) {
        HashMap<GameObject, int[]> map = new HashMap<>();
        for (int ix = 0; ix < getSizeX(); ix++) {
            for (int iy = 0; iy < getSizeY(); iy++) {
                if (this.objects[ix][iy][z] != null)
                    map.put(this.objects[ix][iy][z], new int[]{ix, iy});
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
                for (int iz = 0; iz < 2; iz++) {
                    if (this.objects[ix][iy][iz] != null) {
                        if (this.objects[ix][iy][iz].equals(object))
                            return new int[]{ix, iy};
                    }
                }
            }
        }
        return new int[]{-1, -1};
    }
}
