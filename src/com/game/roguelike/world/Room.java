package com.game.roguelike.world;

public class Room implements arealable {
    private final int[][][] matrix;
    private String name;
    private final int x;
    private final int y;
    public Room(int[][][] matrix, int x, int y) {
        this.matrix = matrix;
        this.name = "Комната";
        this.x = x;
        this.y = y;
    }

    public int[][][] getMatrix() {
        return matrix;
    }
    public void clear() {
        for (int ix = 0; ix < this.matrix.length; ix++) {
            for (int iy = 0; iy < this.matrix[0].length; iy++) {
                this.matrix[ix][iy][0] = -1;
                this.matrix[ix][iy][1] = -1;
            }
        }
    }
    public String getName() {
        return name;
    }


    public void setName(String name) {
        this.name = name;
    }
    public int getWidth(){
        return this.matrix.length;
    }
    public int getHeight(){
        return this.matrix[0].length;
    }
    public int[] getCenter() {
        return new int [] {  (int) (Math.floor((float)getWidth()/2)),
        (int) (Math.floor((float)getHeight()/2)) };
    }
    public int getCenterX() {
        return getCenter()[0];
    }

    public int getCenterY() {
        return getCenter()[1];
    }
    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    public int getAbsoluteCenterX() {
        return getX() + getCenterX();
    }
    public int getAbsoluteCenterY() {
        return getY() + getCenterY();
    }
}
