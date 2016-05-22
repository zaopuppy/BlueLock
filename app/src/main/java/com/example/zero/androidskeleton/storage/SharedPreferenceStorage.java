package com.example.zero.androidskeleton.storage;

import android.content.Context;
import android.content.SharedPreferences;
import com.alibaba.fastjson.JSON;

/**
 * Created by zero on 5/22/16.
 */
class SharedPreferenceStorage implements Storage {

    private final SharedPreferences mPerferences;

    SharedPreferenceStorage(Context context, String name) {
        mPerferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    @Override
    public <T extends Savable> boolean put(String key, T o) {
        mPerferences.edit().putString(key, JSON.toJSONString(o)).apply();
        return true;
    }

    @Override
    public <T extends Savable> T get(String key, Class<T> clazz) {
        String jstr = mPerferences.getString(key, null);
        if (jstr == null) {
            return null;
        }
        return JSON.parseObject(jstr, clazz);
    }

    @Override
    public boolean contains(String key) {
        return mPerferences.contains(key);
    }
}