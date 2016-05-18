package com.example.zero.androidskeleton.ui;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import com.example.zero.androidskeleton.R;

/**
 * http://stackoverflow.com/questions/18315508/is-it-possible-in-android-to-transmit-broadcast-mode-in-ble
 *
 * Look like the answer for android 4.3 and 4.4 is no.
 * Android 4.3 and 4.4 does not support BLE peripheral/broadcaster role
 * see https://code.google.com/p/android/issues/detail?id=59693 and https://code.google.com/p/android/issues/detail?id=58582
 * allow see this stackoverflow thread about the same issue
 * Android 4.3 as a Bluetooth LE Peripheral (http://stackoverflow.com/questions/18008507/android-4-3-as-a-bluetooth-le-peripheral)
 */
public class BroadcastDeviceActivity extends BaseActivity implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast_device);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        return false;
    }
}
