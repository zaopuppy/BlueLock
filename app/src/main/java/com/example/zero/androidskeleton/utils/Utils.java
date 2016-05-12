package com.example.zero.androidskeleton.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import java.util.UUID;

/**
 * Created by zero on 2016/4/4.
 */
public class Utils {
    public static void makeToast(Context context, String s) {
        Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
    }

    public static String b16encode(byte[] value) {
        if (value == null) {
            return "<null>";
        }

        final char[] m = "0123456789ABCDEF".toCharArray();
        StringBuilder builder = new StringBuilder(16);
        for (byte b: value) {
            byte hi = (byte) ((b >> 4) & 0xF);
            byte lo = (byte)        (b & 0xF);
            builder.append(m[hi]);
            builder.append(m[lo]);
        }

        return builder.toString();
    }

    public static boolean isFlagSet(int prop, int flag) {
        return (prop & flag) == flag;
    }

    public static void checkPermissions(Activity activity, String... permissions) {
        for (String permission: permissions) {
            checkMyPermission(activity, permission);
        }
    }

    private static void checkMyPermission(Activity activity, String permission) {
        int result = ContextCompat.checkSelfPermission(activity.getApplicationContext(), permission);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return;
        }

        ActivityCompat.requestPermissions(activity, new String[] { permission }, 0);
    }
}
