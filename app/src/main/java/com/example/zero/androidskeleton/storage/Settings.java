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
        String url = storage.getString("visitor-url", "");
        if (url.isEmpty()) {
            return "http://transee.net:82/door/verify";
        }
        return url;
    }

    public void setVisitorUrl(String url) {
        storage.put("visitor-url", url);
    }
}
