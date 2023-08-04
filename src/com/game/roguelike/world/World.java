package com.game.roguelike.world;

import com.game.roguelike.Direction;
import com.game.roguelike.Roguelike;
import com.game.roguelike.matrix.Matrix;
import com.game.roguelike.matrix.Node;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class World {
    public final GameObject[][][] objects;
    private final Node[][] nodes;
    private final ArrayList<Room> rooms;
    private final ArrayList<Corridor> roads;
    int sizeX, sizeY;

    public World(int sizeX, int sizeY) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        objects = new GameObject[sizeX][sizeY][3];
        rooms = new ArrayList<>();
        roads = new ArrayList<>();
        nodes = new Node[sizeX][sizeY];
        for (int ix = 0; ix < sizeX; ix++) {
            for (int iy = 0; iy < sizeY; iy++) {
                nodes[ix][iy] = new Node(ix, iy);
            }
        }
    }

    public Node[][] getNodes() {
        return nodes;
    }

    public Node getNode(int x, int y) {
        if (x < 0 || y < 0 || x > getNodes().length - 1 || y > getNodes()[0].length - 1)
            return null;
        return getNodes()[x][y];
    }

    public void addObject(GameObject object, int x, int y) {
        this.objects[x][y][0] = object;
        this.nodes[x][y].setSolid(object.getSolid());
    }

    public void addEnvironment(GameObject object, int x, int y) {
        this.objects[x][y][1] = object;
        this.nodes[x][y].setSolid(object.getSolid());
    }

    public void addEntity(Entity entity, int x, int y) {
        entity.setView(Matrix.createView(getSizeX(), getSizeY()));
        this.objects[x][y][2] = entity;
        this.nodes[x][y].setSolid(entity.getSolid());
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
                for (int iz = 0; iz < 3; iz++) {
                    if (this.objects[ix][iy][iz] != null)
                        objects.add(this.objects[ix][iy][iz]);
                }
            }
        }
        return objects;
    }

    public GameObject getObject(int x, int y, int z) {
        for (int ix = 0; ix < getSizeX(); ix++) {
            for (int iy = 0; iy < getSizeY(); iy++) {
                if (ix == x && iy == y) {
                    if (this.objects[ix][iy][z] != null) {
                        return this.objects[ix][iy][z];
                    }
                }

            }
        }
        return null;
    }

    public GameObjects getObjects(int x, int y) {
        GameObjects objects = new GameObjects();
        for (int iz = 0; iz < 3; iz++) {
            if (this.objects[x][y][iz] != null)
                objects.add(this.objects[x][y][iz]);
        }
        return objects;
    }

    public GameObject getObject(int x, int y) {
        return getObject(x, y, 0);
    }

    public GameObject getEnvironment(int x, int y) {
        return getObject(x, y, 1);
    }

    public Entity getEntity(int x, int y) {
        for (int ix = 0; ix < getSizeX(); ix++) {
            for (int iy = 0; iy < getSizeY(); iy++) {
                if (ix == x && iy == y) {
                    if (this.objects[ix][iy][2] != null) {
                        return (Entity) getObject(ix, iy, 2);
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
        for (Corridor corridor : roads) {
            corridor.clear();
        }
        rooms.clear();
        roads.clear();
        for (int ix = 0; ix < getSizeX(); ix++) {
            for (int iy = 0; iy < getSizeY(); iy++) {
                for (int iz = 0; iz < 3; iz++) {
                    this.objects[ix][iy][iz] = null;
                }
            }
        }
    }

    public void fill(JSONObject model) {
        for (int ix = 0; ix < getSizeX(); ix++) {
            for (int iy = 0; iy < getSizeY(); iy++) {
                addObject(new GameObject(model), ix, iy);
            }
        }
    }

    // залить определенную область объектами
    public int[][][] fill(int x, int y, int width, int height, JSONObject model) {
        int[][][] matrix = new int[width][height][2];
        for (int ix = 0; ix < width; ix++) {
            for (int iy = 0; iy < height; iy++) {
                matrix[ix][iy] = new int[]{x + ix, y + iy};
                addObject(new GameObject(model), x + ix, y + iy);
            }
        }
        return matrix;
    }

    // проверка на возможность перемещения в указанные координаты
    public boolean isMove(int x, int y) {
        if (x >= 0 && y >= 0 && x < getSizeX() && y < getSizeY()) {
            //return !getObject(x, y).getSolid();
            return !getNode(x, y).isSolid();
        }
        // движение запрещено
        return false;
    }

    public void removeEnvironment(GameObject object) {
        for (int ix = 0; ix < getSizeX(); ix++) {
            for (int iy = 0; iy < getSizeY(); iy++) {
                if (this.objects[ix][iy][1] != null) {
                    if (this.objects[ix][iy][1].equals(object)) {
                        this.objects[ix][iy][1] = null;
                        this.nodes[ix][iy].setSolid(false);
                    }
                }
            }
        }
    }

    public void removeEntity(Entity entity) {
        for (int ix = 0; ix < getSizeX(); ix++) {
            for (int iy = 0; iy < getSizeY(); iy++) {
                if (this.objects[ix][iy][2] != null) {
                    if (this.objects[ix][iy][2].equals(entity)) {
                        this.objects[ix][iy][2] = null;
                        this.nodes[ix][iy].setSolid(false);
                    }
                }
            }
        }
    }

//    public void writeRectWall(int x, int y, int w, int h) {
//        placeWall(x, y, w, Direction.RIGHT, GameObject.WALL);
//        placeWall(x + w - 1, y + 1, h - 1, Direction.DOWN, GameObject.WALL);
//        placeWall(x, y + h - 1, w - 1, Direction.RIGHT, GameObject.WALL);
//        placeWall(x, y + 1, h - 1, Direction.DOWN, GameObject.WALL);
//    }

    public Room placeRoom(int x, int y, int width, int height, JSONObject model) {
        Room room = new Room(fill(x, y, width, height, model), x, y);
        //writeRectWall(x - 1, y - 1, width + 2, height + 2);
        rooms.add(room);
        return room;
    }

    public ArrayList<Room> getRooms() {
        return rooms;
    }

    public ArrayList<Corridor> getCorridors() {
        return roads;
    }

    public arealable getArea(int x, int y) {
        for (Room room : rooms) {
            if ((x >= room.getX() && x < room.getX() + room.getWidth()) &&
                    (y >= room.getY() && y < room.getY() + room.getHeight())) {
                return room;
            }
        }
        for (Corridor road : roads) {
            int[][] cor = road.getMatrix();
            for (int[] ints : cor) {
                if (ints[0] == x && ints[1] == y) {
                    return road;
                }
            }
        }
        return null;
    }

//    public arealable getLastRoom() {
//        return rooms.get(rooms.size() - 1);
//    }

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

    public GameObjects getRandomRoomAndRoadObjects() {
        ArrayList<arealable> areas = new ArrayList<>();
        areas.addAll(rooms);
        areas.addAll(roads);
        arealable area = areas.get(Roguelike.getRandom(areas.size()));
        GameObjects objects = new GameObjects();
        if (area instanceof Room room) {
            int[][][] matrix = room.getMatrix();
            for (int[][] ints : matrix) {
                for (int iy = 0; iy < matrix[0].length; iy++) {
                    int[] cell = ints[iy];
                    objects.add(this.objects[cell[0]][cell[1]][0]);
                }
            }
        } else if (area instanceof Corridor corridor) {
            int[][] matrix = corridor.getMatrix();
            for (int[] ints : matrix) {
                objects.add(this.objects[ints[0]][ints[1]][0]);
            }
        }

        return objects;
    }

    public GameObjects getObjectsFromRoom(int x, int y, int layer) {
        GameObjects objects = new GameObjects();
        if (layer < 0 || layer > 2) {
            return objects;
        }
        arealable area = getArea(x, y);
        if (area instanceof Room) {
            if (getRooms().contains(area)) {
                int[][][] matrix = getRooms().get(getRooms().indexOf(area)).getMatrix();
                for (int[][] ints : matrix) {
                    for (int iy = 0; iy < matrix[0].length; iy++) {
                        int[] cell = ints[iy];
                        if (this.objects[cell[0]][cell[1]][layer] != null) {
                            objects.add(this.objects[cell[0]][cell[1]][layer]);
                        }
                    }
                }
            }
        }
        return objects;
    }

    public GameObjects getLastRoomObjects() {
        return getRoomObjects(getRooms().size() - 1);
    }

    public Corridor placeRoad(int x0, int y0, int x1, int y1, JSONObject model) {
        ArrayList<int[]> corridors = new ArrayList<>();
        if (x1 > x0) {
            corridors.addAll(placeCorridor(x0, y0, x1 - x0, Direction.RIGHT, model));
        } else if (x1 < x0) {
            corridors.addAll(placeCorridor(x0, y0, x0 - x1, Direction.LEFT, model));
        }
        if (y1 > y0) {
            corridors.addAll(placeCorridor(x0 + (x1 - x0), y0, y1 - y0, Direction.DOWN, model));
        }
        if (y1 < y0) {
            corridors.addAll(placeCorridor(x0 - (x0 - x1), y0, y0 - y1, Direction.UP, model));
        }
        int[][] arrayCorridors = new int[corridors.size()][2];
        for (int i = 0; i < corridors.size(); i++) {
            arrayCorridors[i] = corridors.get(i);
        }
        Corridor corridor = new Corridor(arrayCorridors);
        roads.add(corridor);
        return corridor;
    }

    public ArrayList<int[]> placeCorridor(int x, int y, int s, int direction, JSONObject model) {
        ArrayList<int[]> corridor = new ArrayList<>();
        if (direction == Direction.RIGHT) {
            for (int i = 0; i < s; i++) {
                if (getObject(x + i, y).getType() != GameObject.FLOOR) {
                    addObject(new GameObject(model), x + i, y);
                    corridor.add(new int[]{x + i, y});
                }
            }
        } else if (direction == Direction.LEFT) {
            for (int i = 0; i < s; i++) {
                if (getObject(x - i, y).getType() != GameObject.FLOOR) {
                    addObject(new GameObject(model), x - i, y);
                    corridor.add(new int[]{x - i, y});
                }
            }
        } else if (direction == Direction.DOWN) {
            for (int i = 0; i < s; i++) {
                if (getObject(x, y + i).getType() != GameObject.FLOOR) {
                    addObject(new GameObject(model), x, y + i);
                    corridor.add(new int[]{x, y + i});
                }
            }
        } else if (direction == Direction.UP) {
            for (int i = 0; i < s; i++) {
                if (getObject(x, y - i).getType() != GameObject.FLOOR) {
                    addObject(new GameObject(model), x, y - i);
                    corridor.add(new int[]{x, y - i});
                }
            }
        }
        return corridor;
    }

//    public void placeWall(int x, int y, int s, int direction, int type) {
//        if (direction == Direction.RIGHT) {
//            for (int i = 0; i < s; i++) {
//                addObject(new GameObject(type), x + i, y);
//            }
//        } else if (direction == Direction.LEFT) {
//            for (int i = 0; i < s; i++) {
//                addObject(new GameObject(type), x - i, y);
//            }
//        } else if (direction == Direction.DOWN) {
//            for (int i = 0; i < s; i++) {
//                addObject(new GameObject(type), x, y + i);
//            }
//        } else if (direction == Direction.UP) {
//            for (int i = 0; i < s; i++) {
//                addObject(new GameObject(type), x, y - i);
//            }
//        }
//    }

    public boolean isClearOfObject(int x, int y, int width, int height, int type) {
        if (x < 0 || y < 0 || x + width > objects.length || y + height > objects[0].length)
            return false;
        for (int ix = 0; ix < width; ix++) {
            for (int iy = 0; iy < height; iy++) {
                if (objects[x + ix][y + iy][0] != null) {
                    if (objects[x + ix][y + iy][0].getType() != type)
                        return false;
                }
            }
        }
        return true;
    }

//    public void moveObject(GameObject object, int x, int y) {
//        int prevX = getPositionX(object);
//        int prevY = getPositionY(object);
//        int newX = (int) Roguelike.limit(x, 0, getSizeX() - 1);
//        int newY = (int) Roguelike.limit(y, 0, getSizeY() - 1);
//        addEnvironmentoveObject(object);
//        objects[newX][newY][0] = object;
//        objects[prevX][prevY][0] = new GameObject(GameObject.FLOOR);
//    }

    public void moveEntity(Entity entity, int x, int y) {
        int prevX = getPositionX(entity);
        int prevY = getPositionY(entity);
        objects[x][y][2] = entity;
        objects[prevX][prevY][2] = null;
        this.nodes[x][y].setSolid(true);
        this.nodes[prevX][prevY].setSolid(false);
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
                for (int iz = 0; iz < 3; iz++) {
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
