package com.example.zero.androidskeleton.state;

import android.os.Bundle;
import android.util.ArrayMap;

/**
 * Created by zero on 5/14/16.
 */
public class Context {

    private final StateMachine machine;

    private final ArrayMap<String, Object> dataMap = new ArrayMap<>();

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
        dataMap.put(key, val);
    }

    public String getString(String key, String defaultVal) {
        Object v = dataMap.get(key);
        if (v == null) {
            return defaultVal;
        }
        return (String)v;
    }

    // TODO: support object saving
    public void putObject(String key, Object o) {
        dataMap.put(key, o);
    }

    public Object getObject(String key) {
        return dataMap.get(key);
    }
}
