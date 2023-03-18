package com.game.roguelike.world;

public class Corridor  implements arealable {
    private final int[][] matrix;
    private String name;

    public Corridor(int[][] matrix) {
//        int[][][] newMatrix = new int[matrix.length][2][1];
//        for (int i = 0; i < matrix.length; i++) {
//            for (int j = 0; j < matrix[0].length; j++) {
//                newMatrix[i][j][0] = matrix[i][j];
//            }
//        }
        this.matrix = matrix;
        setName("Корридор");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void clear() {

    }

    @Override
    public int getX() {
        return 0;
    }

    @Override
    public int getY() {
        return 0;
    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    public int[][] getMatrix() {
        return matrix;
    }
}
