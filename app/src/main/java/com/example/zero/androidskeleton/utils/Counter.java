package com.example.zero.androidskeleton.utils;

/**
 * Created by zero on 5/15/16.
 */
public class Counter {
    private final int max;
    private int current = 0;
    private boolean check = false;

    public Counter(int max) {
        this.max = max;
    }

    public void update(int delta) {
        check = false;
        current += delta;
        if (current > max) {
            check = true;
            current = 0;
        }
    }

    public boolean check() {
        return check;
    }
}
