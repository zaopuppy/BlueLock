package com.example.zero.androidskeleton.state;

import android.os.Bundle;

/**
 * Created by zero on 5/14/16.
 */
public class Context {

    private final StateMachine machine;
    private final Bundle bundle = new Bundle();

    public Context(StateMachine machine) {
        this.machine = machine;
    }

    public void handle(int event, int arg, Object o) {
        this.machine.handle(event, arg, o);
    }

    public void setState(State newState) {
        this.machine.setState(newState);
    }

    public void putString(String key, String val) {
        bundle.putString(key, val);
    }

    public String getString(String key, String defaultVal) {
        return bundle.getString(key, defaultVal);
    }
}
