package com.game.roguelike.world;
import com.game.roguelike.Roguelike;
import com.game.roguelike.matrix.Nodes;

public class Entity extends GameObject {
    // текущее здоровье
    private int hp;
    // максимальное здоровье
    private int maxHp;
    // базовая защита
    private int armor;
    // базовая атака
    private int attack;
    // направление взгляда
    private int direction;
    // радиус обзора
    private int radius;
    // матрица обзора (служебная)
    private int [][] view;
    // количество атак за 1 ход
    private int countAttack;
    public Nodes path;

    public Entity(int type) {
        super(type);
        maxHp = 100;
        setHp(maxHp);
        attack = 5;
        armor = 4;
        radius = 5;
        countAttack = 1;
        setDirection(0);
        path = new Nodes();
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
    public char getChar() {
        int percent = (hp * 100/maxHp) / 10;
        if (percent <= 5) {
            return Character.forDigit(percent, 10);
        }
        return super.getChar();
    }
    public void setView(int [][] view) {
        this.view = view;
    }
    public int [][] getView() {
        return view;
    }
    public int getViewXY(int x, int y) {
        if (x > 0 && y > 0 && x < getView().length && y<getView()[0].length) {
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
    public void setAttack(int attack) {
        this.attack = (int) Roguelike.limit(attack, 0, 999);
    }
    public int getAttack() {
        return attack;
    }
    public String getName() {
        return switch (getType()) {
            case MONSTER -> "монстр";
            case PLAYER-> "игрок";
            default -> super.getName();
        };
    }
    public int getRadius() {
        return radius;
    }

    // функции взаимодействия с другими сущностями
    public String attackEntity(Entity entity) {
        int sumAttack = 0;
        for (int i = 0; i < countAttack; i++) {
            int valueAttack = (int) Roguelike.limit(Math.abs(entity.getArmor() - (Roguelike.getRandom(getAttack())+ 1)),
                    0, 99999);
            entity.setHp(entity.getHp() - valueAttack);
            sumAttack += valueAttack;
        }
        return getName() + " атакует "+entity.getName()+" на " + sumAttack + " единиц";
    }

    public int getDirection() {
        return direction;
    }

    public int nextDirection() {
        setDirection(getDirection() + 1);
        return getDirection();
    }
    public void setDirection(int direction) {
        if (direction > 7) {
            this.direction = 0;
        } else {
            this.direction = direction;
        }
    }
}
