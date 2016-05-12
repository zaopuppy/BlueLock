package com.example.zero.androidskeleton.storage;

import android.content.Context;
import com.example.zero.androidskeleton.utils.SpManager;

/**
 * Created by zero on 4/14/16.
 */
public class BtDeviceStorage {
    public static BtDeviceStorage INSTANCE = new BtDeviceStorage();

    //public static final int FLAG_NOT_SAVED = 0;
    //public static final int FLAG_SAVED = 1;

    private SpManager mSpManager;

    private BtDeviceStorage() {
    }

    public void init(Context context) {
        mSpManager = new SpManager(context, "saved_devices");
    }

    public int get(String address) {
        return mSpManager.getInt(address, -1);
    }

    public void put(String address, int password) {
        mSpManager.putInt(address, password);
    }
}
