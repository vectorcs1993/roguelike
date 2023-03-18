package com.game.roguelike.matrix;

public class Node {
    public final int x, y;
    public float g, f, h;
    private Node parent;
    private boolean solid;
    public boolean through;
    private boolean open;
    public Node(int x, int y) {
        this.x = x;
        this.y = y;
        g = f = h = 0;
        parent = null;
        setOpen(false);
        reset();
    }

    public void reset() {
        solid = false;
        through = true;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public boolean isSolid() {
        return solid;
    }

    public void setSolid(boolean solid) {
        this.solid = solid;
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
    }
}
