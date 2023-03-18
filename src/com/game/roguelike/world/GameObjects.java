package com.game.roguelike.world;

import java.util.ArrayList;
import java.util.Random;

public class GameObjects extends ArrayList<GameObject> {
    public GameObjects getObjectsType(int type) {
        GameObjects objects = new GameObjects();
        for (GameObject object : this) {
            if (object.getType() == type) {
                objects.add(object);
            }
        }
        return objects;
    }
    public GameObject getRandomObject() {
        Random random = new Random();
        int randomIndex  = random.nextInt(this.size());
        return this.get(randomIndex);
    }

    public void addUnique(GameObject object) {
        if (object!=null) {
            if (!this.contains(object)) {
                this.add(object);
            }
        }
    }
}
