package com.game.roguelike.world;

import org.json.JSONException;
import org.json.JSONObject;

public class Npc extends Entity {
    // указатель текущей цели для объекта
    private Entity target;
    // флажок прикрепленный к области или нет
    private int harassment = 0;

    public Npc(JSONObject model) {
        super(model);
        target = null;
    }
    public void recoveryHp() {
        if (getHp() < getHpMax()) {
            setHp(getHp() + 1);
            System.out.println(getName() + "  восстанавливает здоровье");
        }
    }

    public Entity getTarget() {
        return target;
    }

    public void setTarget(Entity target) {
        if (target != null) {
            if (getTarget() == null) {
                this.target = target;
                harassment = 0;
            }
        }
    }

    public int getHarassment() {
        return harassment;
    }

    public void setHarassment(int harassment) {
        this.harassment = harassment;
    }

    public void destroy() {
        setTarget(null);
    }

    public boolean isTarget() {
        return getTarget() != null;
    }
}
