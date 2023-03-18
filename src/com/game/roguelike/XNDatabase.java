package com.game.roguelike;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.swing.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class XNDatabase {
    private XNDataTable database;
    Roguelike game;

    public XNDatabase(final Roguelike game, String fileJSON)
            throws JSONException, IOException, URISyntaxException {
        this.game = game;
        setData(new XNDataTable(fileJSON));
    }

    public XNDataTable getData() {
        return database;
    }

    public void setData(XNDataTable database) {
        this.database = database;
    }

    public class XNDataTable {
        HashMap<String, HashMap<Integer, ObjectData>> categories = new HashMap<String, HashMap<Integer, ObjectData>>();

        public XNDataTable(String jsonData) throws JSONException,
                IOException, URISyntaxException {
            JSONObject data = new JSONObject(game.readJSONToString(jsonData));
            Iterator<?> iter = data.keys();

            while (iter.hasNext()) {
                String key = (String) iter.next();
                JSONArray category = data.getJSONArray(key);
                HashMap<Integer, ObjectData> objects = new HashMap<Integer, ObjectData>();
                for (int i = 0; i < category.length(); i++) {
                    JSONObject object = category.getJSONObject(i);
//                    if (object.has("sprite"))
//                        objects.put(object.getInt("id"), new ObjectData(object.toString(),
//                                XNCanvas.createImageIcon(object.getString("sprite"))));
//                    else
//                        objects.put(object.getInt("id"), new ObjectData(object.toString()));
                }
                categories.put(key, objects);
            }

        }

        public ObjectData getObject(String category, int id) {
            return categories.get(category).get(id);
        }

    }

    public class ObjectData extends JSONObject {
        ImageIcon sprite;

        public ObjectData(String source, ImageIcon sprite) throws JSONException {
            super(source);
            this.sprite = sprite;
        }

        public ObjectData(String source) throws JSONException {
            this(source, null);

        }

        public ArrayList<Object> getArray(String key) {
            ArrayList<Object> array = new ArrayList<Object>();
            if (has(key)) {
                try {
                    JSONArray jarray = getJSONArray(key);
                    for (int i = 0; i < jarray.length(); i++)
                        array.add(jarray.get(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return array;
        }

    }
}
