package com.example.zero.androidskeleton.ui;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import com.example.zero.androidskeleton.R;
import com.example.zero.androidskeleton.bt.BlueLockProtocol;
import com.example.zero.androidskeleton.bt.BtLeDevice;
import com.example.zero.androidskeleton.bt.BtLeService;
import com.example.zero.androidskeleton.concurrent.Function;
import com.example.zero.androidskeleton.concurrent.Promise;
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
    private Button unlockButton;

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

        mDevice.addDeviceListener(this);
    }

    @Override
    protected void onDestroy() {
        if (mDevice != null) {
            mDevice.removeDeviceListener(this);
            mDevice.disconnectGatt();
        }
        super.onDestroy();
    }

    private void setupUiComp() {
        final EditText phoneNumText = (EditText) findViewById(R.id.phone_num_text);
        assert phoneNumText != null;

        unlockButton = (Button) findViewById(R.id.confirm_button);
        assert unlockButton != null;
        unlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNum = phoneNumText.getText().toString();
                if (phoneNum.length() != 11) {
                    Utils.makeToast(getApplicationContext(), "手机号长度不正确");
                    return;
                }
                unlockButton.setEnabled(false);
                InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (manager != null) {
                    manager.hideSoftInputFromWindow(phoneNumText.getWindowToken(), 0);
                }
                phoneUnlockSM.handle(PhoneUnlockSM.EVENT_UNLOCK, -1, phoneNum);
            }
        });
    }

    @Override
    public void onDeviceStateChanged(BtLeDevice.State state) {
        Log.i(TAG, "onDeviceStateChanged: " + state);
        phoneUnlockSM.handle(PhoneUnlockSM.EVENT_DEV_STATE_CHANGED, -1, state);
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        byte[] value = characteristic.getValue();
        if (value == null || value.length <= 0) {
            // ignore
            return;
        }

        final byte result = characteristic.getValue()[0];
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.makeToast(getApplicationContext(), BlueLockProtocol.getCodeDesc(result));
                finish();
            }
        });
    }
}
