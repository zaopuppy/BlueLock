package com.example.zero.androidskeleton.storage;

import com.alibaba.fastjson.JSON;

/**
 * Created by zero on 5/15/16.
 */
public interface Storage {

    interface Savable {
    }

    <T extends Savable> boolean put(String key, T o);

    <T extends Savable> T get(String key, Class<T> clazz);

    boolean put(String key, boolean v);

    boolean getBoolean(String key, boolean defaultValue);

    boolean put(String key, int v);

    int getInt(String key, int defaultValue);

    String getString(String key, String defaultValue);

    void put(String key, String value);

    boolean contains(String key);
}
