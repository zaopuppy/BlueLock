package com.example.zero.androidskeleton;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import com.example.zero.androidskeleton.bt.BtService;
import com.example.zero.androidskeleton.storage.BtDeviceStorage;

/**
 * Created by zero on 2016/4/4.
 */
public class Application extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();

        BtDeviceStorage.INSTANCE.init(getApplicationContext());
        BtService.INSTANCE.init(getApplicationContext());
    }

}
