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

    boolean contains(String key);
}
