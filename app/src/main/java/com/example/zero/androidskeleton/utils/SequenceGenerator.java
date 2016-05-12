package com.example.zero.androidskeleton.utils;

/**
 * thread-safe sequence generator
 * Created by zero on 5/11/16.
 */
public class SequenceGenerator {

    private final int MAX;

    private int current = 0;
    private int last = 0;

    public SequenceGenerator(int start, int max) {
        this.current = start;
        this.last = current;
        MAX = max;
        // FIXME: use BuildConfig.DEBUG instead
        assert start > 0 && max > 0;
        assert max > start;
    }

    public synchronized int next() {
        last = current;
        if (current >= MAX) {
            current = 0;
            return MAX;
        } else {
            return current++;
        }
    }

    public synchronized int last() {
        return last;
    }
}
