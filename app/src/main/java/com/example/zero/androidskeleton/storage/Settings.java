package com.example.zero.androidskeleton.storage;

import android.content.Context;

/**
 * Created by zero on 5/22/16.
 */
public class Settings {
    public static final int UNLOCK_MODE_MANUNAL = 0;
    public static final int UNLOCK_MODE_AUTO = 1;
    public static final int UNLOCK_MODE_SHAKE = 2;

    public static final Settings INSTANCE = new Settings();

    private Storage storage;

    public boolean init(Context context) {
        storage = new SharedPreferenceStorage(context, "settings");
        return true;
    }

    //public boolean isAutoUnlock() {
    //    return storage.getBoolean("auto-unlock", false);
    //}
    //
    //public void setAutoUnlock(boolean autoUnlock) {
    //    storage.put("auto-unlock", autoUnlock);
    //}
    //
    public int getUnlockMode() {
        return storage.getInt("unlock-mode", UNLOCK_MODE_MANUNAL);
    }

    public void setUnlockMode(int unlockMode) {
        storage.put("unlock-mode", unlockMode);
    }
}
