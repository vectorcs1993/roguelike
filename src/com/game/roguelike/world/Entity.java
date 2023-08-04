package com.game.roguelike.world;

import com.game.roguelike.Roguelike;
import com.game.roguelike.matrix.IntegerList;
import com.game.roguelike.matrix.Nodes;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Entity extends GameObject {
    // текущее здоровье
    private int hp;
    // максимальное здоровье
    // текущее количество очков действий
    private int od;
    private int maxHp;
    //макс количество очков действий
    private int maxOd;
    // максимальная грузоподъёмность
    private int maxCp;
    // сила
    private int strength;
    // ловкость
    private int dexterity;
    // восприятие
    private int perception;
    private int stamina;
    // базовая защита
    private int armor;

    // направление взгляда
    private int direction;
    // радиус обзора
    private int radius;
    // матрица обзора (служебная)
    private int[][] view;
    public Nodes path;

    private ArrayList<Item> items;

    public Entity(JSONObject model) {
        super(model);
        setDirection(0);
        path = new Nodes();
        items = new ArrayList<>();
        try {
            this.strength = model.getInt("strength");
            this.dexterity = model.getInt("dexterity");
            this.perception = model.getInt("perception");
            this.stamina = model.getInt("stamina");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        update();
    }

    public void update() {
        maxHp = strength * 5;
        setHp(maxHp);
        radius = perception;
        armor = 0;
        setMaxOd(dexterity);
        od = getMaxOd();
        setMaxCp(strength * 5);
    }

    public void recoveryHp() {
        hp = (int) Roguelike.limit(hp + 1, 1, maxHp);
    }

    public int getHpMax() {
        return maxHp;
    }

    public int getHp() {
        return hp;
    }

    public void setView(int[][] view) {
        this.view = view;
    }

    public void setPerception(int value) {
        this.perception = value;
        update();
    }

    public int[][] getView() {
        return view;
    }

    public int getViewXY(int x, int y) {
        if (x > 0 && y > 0 && x < getView().length && y < getView()[0].length) {
            return getView()[x][y];
        }
        return -1;
    }

    public void setHp(int hp) {
        this.hp = (int) Roguelike.limit(hp, 0, maxHp);
    }

    public int getArmor() {
        return armor;
    }

    public int getRadius() {
        return radius;
    }

    // функции взаимодействия с другими сущностями
    public String attackEntity(Entity entity, int value) {
        int sumAttack = 0;
        int valueAttack = (int) Roguelike.limit(Math.abs(entity.getArmor() - value),
                0, 99999);
        entity.setHp(entity.getHp() - valueAttack);
        sumAttack += valueAttack;
        return getName() + " атакует " + entity.getName() + " на " + sumAttack + " единиц";
    }
    public String attackEntity(Entity entity) {
        return attackEntity(entity, getStrength());
    }
    public int getDirection() {
        return direction;
    }

    public int nextDirection() {
        setDirection(getDirection() + 1);
        return getDirection();
    }

    public int getStrength() {
        return strength;
    }

    public int getDexterity() {
        return dexterity;
    }

    public int getPerception() {
        return perception;
    }

    public int getStamina() {
        return stamina;
    }

    public void setDirection(int direction) {
        if (direction > 7) {
            this.direction = 0;
        } else {
            this.direction = direction;
        }
    }

    public int getMaxCp() {
        return maxCp;
    }

    public int getMaxOd() {
        return maxOd;
    }

    public void setMaxOd(int maxOd) {
        this.maxOd = maxOd;
    }

    public void setMaxCp(int maxCp) {
        this.maxCp = maxCp;
    }

    public int getOd() {
        return od;
    }

    public void setOd(int od) {
        this.od = (int) Roguelike.limit(od, 0, getMaxOd());
    }

    public ArrayList<Item> getItems() {
        return items;
    }


}
