package com.example.zero.androidskeleton.bt;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Created by zero on 4/4/16.
 */
public class BtService {

    private static class MyBroadcastReceiver extends BroadcastReceiver {

        private final BtService mService;

        public MyBroadcastReceiver(BtService service) {
            mService = service;
        }

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
                //makeToast(String.format(
                //        Locale.US, "%d --> %d", old_state, new_state));
            }
        }
    }

    //private final Context mContext;

    public enum Code {
        BT_NOT_ENABLED,
        BT_ENABLED,
    }

    public static final BtService INSTANCE = new BtService();

    public BtService() {
        //mContext = context.getApplicationContext();
    }

    public boolean init(Context context) {
        return true;
    }

    public Code start() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();

        assert adapter != null;

        if (!adapter.isEnabled()) {
            return Code.BT_NOT_ENABLED;
        }

        return Code.BT_ENABLED;
    }

    public boolean startDiscovery() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        return adapter.startDiscovery();
    }

    public boolean cancelDiscovery() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        return adapter.cancelDiscovery();
    }

}
