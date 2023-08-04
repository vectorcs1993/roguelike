package com.game.roguelike.world;

import org.json.JSONException;
import org.json.JSONObject;

import java.awt.*;

public class GameObject {
    private final int type;
    private final String id;
    private final String name;
    public final static int PLAYER = 0, WALL=1, FLOOR=2, LADDER_DOWN =3, LADDER_UP=4, MONSTER=10;

    public final static int MEDKIT = 101;
    public GameObject(JSONObject model) {
        try {

            this.id = model.getString("id");
            this.type = model.getInt("type");
            this.name = model.getString("name");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
    public int getType() {
        return type;
    }
    public boolean getSolid() {
        return type == WALL || type==MONSTER;
    }
    public boolean getPermanent() {
        return type == WALL;
    }
    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }
}
