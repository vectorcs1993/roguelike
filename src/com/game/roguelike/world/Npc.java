package com.game.roguelike.world;

import com.game.roguelike.Roguelike;

public class Npc extends Entity {
    // флаг пробуждения пробуждается после первого обнаружения врага
    public boolean awake;
    // указатель текущей цели для объекта
    private Entity target;
    // флажок прикрепленный к области или нет
    public boolean attachedToArea;
    private int harassment = 0;

    public Npc(int type) {
        super(type);
        awake = false;
        target = null;
        attachedToArea = false;
    }
    public void recoveryHp() {
        if (awake && getHp() < getHpMax()) {
            setHp(getHp() + 1);
            System.out.println(getName() + "  восстанавливает здоровье");
        }
    }

    public Entity getTarget() {
        return target;
    }

    public void setTarget(Entity target) {
        if (target != null) {
            if (!awake) {
                awake = true;
            }
            if (getTarget() == null) {
                this.target = target;
                harassment = 0;

                System.out.println(getName() + "  начинает преследовать " + target.getName());
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
}
