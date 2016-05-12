package com.example.zero.androidskeleton;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.zero.androidskeleton.ui.SelectDeviceActivity;
import com.example.zero.androidskeleton.utils.Utils;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                return;
            }

            String action = intent.getAction();
            Bundle bundle = intent.getExtras();
            if (action == null || bundle == null) {
                return;
            }

            if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                int old_state = bundle.getInt(
                        BluetoothAdapter.EXTRA_PREVIOUS_STATE,
                        BluetoothAdapter.STATE_OFF);
                int new_state = bundle.getInt(
                        BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.STATE_OFF);
                Utils.makeToast(getApplicationContext(), String.format(
                        Locale.US, "%d --> %d", old_state, new_state));
            } // else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
            //    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            //    log("device: " + device.getName() + ", " + device.getAddress());
            //}
        }
    };

    private TextView mLogView;

    private void log(String msg) {
        mLogView.append(msg + '\n');
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        setupUiComp();

        {
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mReceiver, filter);
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private void setupUiComp() {
        mLogView = (TextView) findViewById(R.id.log_view);

        Button start_bt_button = (Button) findViewById(R.id.start_bt_service_button);
        assert start_bt_button != null;
        start_bt_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SelectDeviceActivity.class);
                startActivity(intent);
            }
        });


        Button stop_bt_button = (Button) findViewById(R.id.stop_bt_service_button);
        assert stop_bt_button != null;
        stop_bt_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopBtService();
            }
        });
    }

    private void startBtService() {
        //log("start scan: " + mBtService.startDiscovery());
        //BtService.Code code = mBtService.start();
        //if (code == BtService.Code.BT_NOT_ENABLED) {
        //    makeToast("Bluetooth is not enabled");
        //    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        //    startActivity(intent);
        //    return;
        //}
    }

    private void stopBtService() {
        //
    }


}
