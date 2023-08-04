package com.game.roguelike;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Data {
    ArrayList<JSONObject> objects;
    private final HashMap<String, Image> imageData;
    Roguelike game;
    int size;

    Data(Roguelike game) {
        imageData = new HashMap<>();
        try {
            objects = new ArrayList<>();
            JSONObject data = new JSONObject(game.readJSONToString("data.json"));
            size = (int) game.getSettings().get("gridSize");
            putObjects(data.getJSONArray("entities"));
            putObjects(data.getJSONArray("floors"));
            putObjects(data.getJSONArray("environments"));
            putObjects(data.getJSONArray("items"), size / 2, size / 2);
            putImage("arrowAttack", "hud/arrow_attack.png", size / 2, size / 2);
            putImage("ground", "terrain/floor4.png");
            putImage("trace", "objects/trace.png");
            putImage("cursorAction", "hud/cursor_action.png");
            putImage("wall1_top", "objects/fence_steel2.png");
            putImage("roof", "objects/roof.png");

        } catch (JSONException | IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    JSONObject getObject(String id) {
        try {
            for (JSONObject object : objects) {
                if (object.getString("id").equals(id)) {
                    return object;
                }
            }
            return null;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    void putObjects(JSONArray array) {
        putObjects(array, size, size);
    }

    void putObjects(JSONArray array, int w, int h) {
        try {
            for (int i = 0; i < array.length(); i++) {
                JSONObject obj = array.getJSONObject(i);
                objects.add(obj);
                if (obj.has("imageWidth") && obj.has("imageHeight")) {
                    putImage(obj.getString("id"), obj.getString("image"),
                            obj.getInt("imageWidth"), obj.getInt("imageHeight"));
                } else {
                    putImage(obj.getString("id"), obj.getString("image"), w, h);
                }
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<JSONObject> getJSONObjectsIsType(int type) {
        ArrayList<JSONObject> arrayList = new ArrayList<>();
        for (JSONObject object : objects) {
            try {
                if (object.getInt("type") == type) {
                    arrayList.add(object);
                }
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return arrayList;
    }

    public ArrayList<JSONObject> getJSONObjects(JSONArray array) {
        ArrayList<JSONObject> arrayList = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            try {
                arrayList.add(array.getJSONObject(i));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
        return arrayList;
    }

    void putImage(String name, String path) {
        putImage(name, path, size, size);
    }

    void putImage(String name, String path, int w, int h) {
        imageData.put(name, createImageIcon(path, w, h).getImage());
    }

    public ImageIcon createImageIcon(String image, int width, int height) {
        Image imageT = new ImageIcon(Objects.requireNonNull(getClass().getResource("/" + image))).getImage();
        Image newimgT = imageT.getScaledInstance(width, height, java.awt.Image.SCALE_FAST);
        return new ImageIcon(newimgT);
    }

    public Image getImage(String id) {
        return imageData.get(id);
    }
}
