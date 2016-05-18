package com.example.zero.androidskeleton;

import java.util.Timer;

/**
 * Created by zero on 5/11/16.
 */
public class GlobalObjects {

    public static final int UNLOCK_MODE_MANUNAL = 0;
    public static final int UNLOCK_MODE_AUTO = 1;
    public static final int UNLOCK_MODE_SHAKE = 2;

    public static Timer timer = new Timer();

    public static volatile int unlockMode = UNLOCK_MODE_AUTO;

    public static int senseLevel = 40;
}
