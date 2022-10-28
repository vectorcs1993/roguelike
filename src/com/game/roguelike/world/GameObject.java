package com.game.roguelike.world;

import java.awt.Color;

public class GameObject {
    public int type;
    int id;
    public final static int PLAYER = 0, WALL=1, FLOOR=2;

    public final static int MEDKIT = 101;
    public GameObject(int type) {
        this.type = type;
        id = -1;
    }
    public char getView() {
        return switch (type) {
            case PLAYER -> '@';
            case WALL -> '#';
            case FLOOR -> '.';
            case MEDKIT -> '+';
            default -> ' ';
        };
    }
    public Color getColorBg() {
        return switch (type) {
            case PLAYER -> Color.GRAY;
            case WALL -> Color.DARK_GRAY;
            case MEDKIT -> Color.RED;
            default -> Color.BLACK;
        };
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
