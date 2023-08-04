package com.game.roguelike.world;

import com.game.roguelike.Roguelike;
import org.json.JSONObject;

public class Player extends Entity {
    private int hunger;
    private int radiation;
    private int energy;
    private int thirst;

    Item [] itemSlot;
    public Player(JSONObject model) {
        super(model);
        hunger = 0;
        radiation = 0;
        energy = 100;
        thirst = 0;
        setPerception(6);
        itemSlot = new Item [8];
        // 0-голова
        // 1-тело
        // 2-ноги
        // 3-руки
        // 4-обувка
        // 5-левая рука
        // 6-правая рука
        // 7-слот для рюкзака
        // 8-пояс
    }

    public int getHunger() {
        return hunger;
    }

    public void setHunger(int hunger) {
        this.hunger = hunger;
    }

    public int getRadiation() {
        return radiation;
    }

    public void setRadiation(int radiation) {
        this.radiation = radiation;
    }

    public int getEnergy() {
        return energy;
    }

    public void setEnergy(int energy) {
        this.energy = (int) Roguelike.limit(energy, 0, 100);
    }

    public int getThirst() {
        return thirst;
    }

    public void setThirst(int thirst) {
        this.thirst = thirst;
    }
    public int getItemsWeight() {
        int weight = 0;
        for (Item item: getItems()) {
            weight += item.getWeight();
        }
        return weight;
    }

    public boolean isItemSlot(int i) {
        if (i>= 0 && i < itemSlot.length) {
            return itemSlot[i] != null;
        }
        return false;
    }
    public void setItemSlot(int i, Item item) {
        if (i>= 0 && i < itemSlot.length) {
            if (itemSlot[i] != null) {
                getItems().add(itemSlot[i]);
                itemSlot[i] = null;
            }
            itemSlot[i] = item;
        }
    }

    public Item getItemSlot(int i) {
        if (i>= 0 && i < itemSlot.length) {
            return itemSlot[i];
        }
        return null;
    }
}
