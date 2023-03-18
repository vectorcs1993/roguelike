package com.game.roguelike.world;

import com.game.roguelike.Roguelike;

import java.awt.*;

public class GameObject {
    private final int type;
    public Image sprite;
    public final static int PLAYER = 0, WALL=1, FLOOR=2, LADDER_DOWN =3, LADDER_UP=4, MONSTER=10;

    public final static int MEDKIT = 101;
    public GameObject(int type) {
        this.type = type;
        // Roguelike.setSprite(room.game.data.getData().getObject(type, id).sprite);
    }
    public int getType() {
        return type;
    }
    public char getChar() {
        return switch (type) {
            case PLAYER -> '@';
            case WALL -> '#';
            case FLOOR -> '.';
            case MEDKIT -> '+';
            case LADDER_DOWN -> '>';
            case LADDER_UP -> '<';
            case MONSTER -> '!';
            default -> ' ';
        };
    }
    public String getColorBg() {
        return switch (type) {
            case PLAYER -> "blue";
            case WALL -> "dark_gray";
            case LADDER_DOWN, LADDER_UP -> "white";
            case MEDKIT, MONSTER -> "red";
            default -> "black";
        };
    }
    public boolean getSolid() {
        return type == WALL || type==MONSTER;
    }
    public boolean getPermanent() {
        return type == WALL;
    }
    public String getColorFg() {
        return switch (type) {
            case WALL, FLOOR -> "light_gray";
            case LADDER_DOWN, LADDER_UP, MONSTER -> "black";
            default -> "white";
        };
    }
    public String getName() {
        return switch (type) {
            case PLAYER -> "игрок";
            case WALL -> "стена";
            case FLOOR -> "пол";
            case LADDER_DOWN -> "лестница вниз";
            case LADDER_UP -> "лестница наверх";
            default -> "нет данных";
        };
    }
}
