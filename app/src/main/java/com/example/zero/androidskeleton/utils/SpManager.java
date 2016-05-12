package com.example.zero.androidskeleton.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by zero on 4/14/16.
 */
public class SpManager {

    private final SharedPreferences mPerferences;

    public SpManager(Context context, String name) {
        mPerferences = context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public int getInt(String key, int defValue) {
        return mPerferences.getInt(key, defValue);
    }

    public void putInt(String key, int value) {
        mPerferences.edit().putInt(key, value).apply();
    }

    public String getString(String key, String defValue) {
        return mPerferences.getString(key, defValue);
    }

    public void putString(String key, String value) {
        mPerferences.edit().putString(key, value).apply();
    }
}
