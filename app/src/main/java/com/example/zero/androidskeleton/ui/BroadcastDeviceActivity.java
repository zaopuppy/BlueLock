package com.example.zero.androidskeleton.ui;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.*;
import android.content.Intent;
import android.os.ParcelUuid;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.example.zero.androidskeleton.R;
import com.example.zero.androidskeleton.bt.BlueLockProtocol;

/**
 * http://stackoverflow.com/questions/18315508/is-it-possible-in-android-to-transmit-broadcast-mode-in-ble
 *
 * Look like the answer for android 4.3 and 4.4 is no.
 * Android 4.3 and 4.4 does not support BLE peripheral/broadcaster role
 * see https://code.google.com/p/android/issues/detail?id=59693 and https://code.google.com/p/android/issues/detail?id=58582
 * allow see this stackoverflow thread about the same issue
 * Android 4.3 as a Bluetooth LE Peripheral (http://stackoverflow.com/questions/18008507/android-4-3-as-a-bluetooth-le-peripheral)
 */
public class BroadcastDeviceActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_broadcast_device);

        //logView_ = (TextView) findViewById(R.id.log_view);
        //assert logView_ != null;
        //
        //Button openButton = (Button) findViewById(R.id.open_button);
        //assert openButton != null;
        //openButton.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        // showMessage(Utils.b16encode(DoorProtocol.openDoorV2("123456", "18600091651")));
        //        openDoor();
        //    }
        //});
        //
        //Button gotoButton = (Button) findViewById(R.id.goto_button);
        //assert gotoButton != null;
        //gotoButton.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View v) {
        //        Intent intent = new Intent(BroadcastDeviceActivity.this, SelectDeviceActivity.class);
        //        startActivity(intent);
        //    }
        //});

    }
}
