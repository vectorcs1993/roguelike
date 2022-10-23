package com.game.roguelike.world;

public class GameObject {
    int type;
    int id;

    public GameObject(int type) {
        this.type = type;
        id = -1;
    }
    public char getView() {
        return '@';
    }
}
