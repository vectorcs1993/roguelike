package com.game.roguelike.matrix;

import java.util.ArrayList;
import java.util.Collections;

public class IntegerList extends ArrayList<Integer> {
    public void appendUnique(int element) {
        if (!this.contains(element)) {
            this.add(element);
        }
    }
    public void append(IntegerList integers) {
        this.addAll(integers);
    }
    public void append(int num) {
        this.add(num);
    }
    public int[] values() {
        int [] array = new int [this.size()];
        for (int i = 0; i < this.size(); i++) {
            array[i] = this.get(i);
        }
        return array;
    }
    public void shuffle() {
        Collections.shuffle(this);
    }
}
