package com.game.roguelike.world;

public interface arealable {
    String getName();
    void setName(String name);
    void clear();
    int getX();
    int getY();
    int getWidth();
    int getHeight();
}
