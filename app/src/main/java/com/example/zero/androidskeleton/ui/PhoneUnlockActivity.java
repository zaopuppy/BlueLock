package com.example.zero.androidskeleton.ui;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.example.zero.androidskeleton.R;
import com.example.zero.androidskeleton.bt.BtLeDevice;
import com.example.zero.androidskeleton.bt.BtLeService;
import com.example.zero.androidskeleton.log.Log;
import com.example.zero.androidskeleton.state.impl.PhoneUnlockSM;
import com.example.zero.androidskeleton.utils.Utils;

/**
 * Created by zhaoyi on 7/7/16.
 */
public class PhoneUnlockActivity extends BaseActivity implements BtLeDevice.DeviceListener {

    private static final String TAG = "PhoneUnlockActivity";

    private PhoneUnlockSM phoneUnlockSM = null;

    private BtLeDevice mDevice = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_unlock);

        setupUiComp();

        Intent intent = getIntent();
        if (intent == null || intent.getExtras() == null) {
            finish();
            return;
        }
        Bundle bundle = intent.getExtras();

        Log.i(TAG, "modify password for device=" + bundle.getString("addr"));
        mDevice = BtLeService.INSTANCE.getDevice(bundle.getString("addr"));
        if (mDevice == null) {
            Utils.makeToast(getApplicationContext(), "no device supplied");
            finish();
            return;
        }

        phoneUnlockSM = new PhoneUnlockSM(this, mDevice);
    }

    private void setupUiComp() {
        final EditText phoneNumText = (EditText) findViewById(R.id.phone_num_text);
        assert phoneNumText != null;

        Button button = (Button) findViewById(R.id.confirm_button);
        assert button != null;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNum = phoneNumText.getText().toString();

            }
        });
    }

    @Override
    public void onDeviceStateChanged(BtLeDevice.State state) {
        // TODO
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        // TODO
    }
}
