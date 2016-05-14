package com.example.zero.androidskeleton.log;

/**
 * Created by zero on 5/14/16.
 */
public class Log {
    private static final String TAG = "bluelock|";

    public static void d(String tag, String msg) {
        android.util.Log.d(TAG + tag, msg);
    }

    public static void i(String tag, String msg) {
        android.util.Log.i(TAG + tag, msg);
    }

    public static void w(String tag, String msg) {
        android.util.Log.w(TAG + tag, msg);
    }

    public static void e(String tag, String msg) {
        android.util.Log.e(TAG + tag, msg);
    }

}
