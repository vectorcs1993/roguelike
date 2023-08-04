package com.game.roguelike.world;

import org.json.JSONException;
import org.json.JSONObject;

public class Item extends GameObject {
    private final int weight;
    public Item(JSONObject model) {
        super(model);
        try {
            this.weight = model.getInt("weight");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public int getWeight() {
        return weight;
    }
}
