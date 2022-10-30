package com.game.roguelike.world;

import java.awt.Color;

public class GameObject {
    public int type;
    public final static int PLAYER = 0, WALL=1, FLOOR=2, LADDER=3;

    public final static int MEDKIT = 101;
    public GameObject(int type) {
        this.type = type;
    }
    public char getView() {
        return switch (type) {
            case PLAYER -> '@';
            case WALL -> '#';
            case FLOOR -> '.';
            case MEDKIT -> '+';
            case LADDER -> '>';
            default -> ' ';
        };
    }
    public Color getColorBg() {
        return switch (type) {
            case PLAYER -> Color.GRAY;
            case WALL -> Color.DARK_GRAY;
            case LADDER -> Color.LIGHT_GRAY;
            case MEDKIT -> Color.RED;
            default -> Color.BLACK;
        };
    }
    public boolean getSolid() {
        return type == WALL;
    }
    public Color getColorFg() {
        return switch (type) {
            case PLAYER -> Color.YELLOW;
            case WALL, MEDKIT -> Color.WHITE;
            case FLOOR -> Color.LIGHT_GRAY;
            default -> Color.RED;
        };
    }
    public String getName() {
        return switch (type) {
            case PLAYER -> "игрок";
            case WALL -> "стена";
            case FLOOR -> "пол";
            default -> "нет данных";
        };
    }
}
