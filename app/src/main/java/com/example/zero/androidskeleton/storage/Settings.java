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

    public int getUnlockMode() {
        return storage.getInt("unlock-mode", UNLOCK_MODE_MANUNAL);
    }

    public void setUnlockMode(int unlockMode) {
        storage.put("unlock-mode", unlockMode);
    }

    public String getVisitorUrl() {
        return storage.getString("visitor-url", "http://transee.net:81/");
    }

    public void setVisitorUrl() {
        storage.put("visitor-url", "http://transee.net:81/");
    }
}
