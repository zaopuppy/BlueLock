package com.example.zero.androidskeleton.ui;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.example.zero.androidskeleton.R;
import com.example.zero.androidskeleton.bt.BtLeDevice;
import com.example.zero.androidskeleton.bt.BtLeService;
import com.example.zero.androidskeleton.bt.BlueLockProtocol;
import com.example.zero.androidskeleton.log.Log;
import com.example.zero.androidskeleton.state.Context;
import com.example.zero.androidskeleton.state.State;
import com.example.zero.androidskeleton.state.StateMachine;
import com.example.zero.androidskeleton.utils.Utils;


public class ModifyPasswordActivity extends AppCompatActivity implements BtLeDevice.DeviceListener {

    private static final String TAG = "ModifyPasswordActivity";

    private BtLeDevice mDevice = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_password);

        setupUiComp();


    }

    private void setupUiComp() {
        EditText managerPasswordEdit = (EditText) findViewById(R.id.admin_password_edit);
        assert managerPasswordEdit != null;

        EditText oldPasswordEdit = (EditText) findViewById(R.id.password_edit);
        assert oldPasswordEdit != null;

        EditText newPasswordEdit = (EditText) findViewById(R.id.new_password_edit);
        assert newPasswordEdit != null;

        Button cancelButton = (Button) findViewById(R.id.cancel_button);
        assert cancelButton != null;
        Button confirmButton = (Button) findViewById(R.id.confirm_button);
        assert confirmButton != null;

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.makeToast(getApplicationContext(), "cancelled");
                finish();
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //
            }
        });
    }

    private final ModifyPasswordSM modifyPasswordSM = new ModifyPasswordSM();

    private static class ModifyPasswordData {
        final String addr;
        final String adminPassword;
        final String password;
        final String newPassword;

        ModifyPasswordData(String addr, String adminPassword, String password, String newPassword) {
            this.addr = addr;
            this.adminPassword = adminPassword;
            this.password = password;
            this.newPassword = newPassword;
        }
    }

    private class IdleState implements State {
        @Override
        public void handle(Context context, int event, int arg, Object o) {
            switch (event) {
                case EVENT_MODIFY:
                    handleModify(context, arg, o);
                default:
                    // ignore
                    break;
            }
        }

        private void handleModify(Context context, int arg, Object o) {
            if (!(o instanceof ModifyPasswordData)) {
                Log.e(TAG, "bad argument, string password expected");
                return;
            }

            // then connect
            connect(context, arg, o);
        }

        private void connect(Context context, int arg, Object o) {
            ModifyPasswordData data = (ModifyPasswordData) o;

            // save info first
            context.putObject("modify-data", data);

            BtLeDevice device = BtLeService.INSTANCE.getDevice(data.addr);
            if (device == null) {
                Log.e(TAG, "not such device: " + data.addr);
                return;
            }

            BtLeDevice.State state = device.getState();
            Log.w(TAG, "connect: current-state=" + state);

            switch (state) {
                case READY:
                    context.setState(READY);
                    context.handle(EVENT_MODIFY, arg, o);
                    break;
                case DISCONNECTED:
                    device.connectGatt(ModifyPasswordActivity.this);
                    context.setState(WAIT_FOR_CONNECT);
                    break;
                default:
                    mDevice.disconnectGatt();
                    context.setState(WAIT_FOR_DISCONNECT);
                    break;
            }
        }
    }

    private class WaitForDisconnectState implements State {
        @Override
        public void handle(Context context, int event, int arg, Object o) {
            switch (event) {
                case EVENT_DEV_STATE_CHANGED:
                    BtLeDevice.State state = (BtLeDevice.State) o;
                    if (state == BtLeDevice.State.DISCONNECTED) {
                        mDevice.connectGatt(ModifyPasswordActivity.this);
                        context.setState(WAIT_FOR_CONNECT);
                    } else {
                        Log.w(TAG, "expect disconnect event, but received: " + state);
                    }
                    break;
                default:
                    // ignore
                    break;
            }
        }
    }

    private class WaitForConnectState implements State {
        @Override
        public void handle(Context context, int event, int arg, Object o) {
            switch (event) {
                case EVENT_DEV_STATE_CHANGED:
                    BtLeDevice.State state = (BtLeDevice.State) o;
                    handleStateChanged(context, state);
                    break;
                default:
                    // ignore
                    break;
            }
        }

        private void handleStateChanged(Context context, BtLeDevice.State state) {
            switch (state) {
                case READY:
                    context.setState(READY);
                    Object o = context.getObject("modify-data");
                    Log.w(TAG, "context password: " + o);
                    if (o != null) {
                        context.handle(EVENT_MODIFY, -1, o);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private class ReadyState implements State {
        @Override
        public void handle(Context context, int event, int arg, Object o) {
            switch (event) {
                case EVENT_DEV_STATE_CHANGED:
                    BtLeDevice.State state = (BtLeDevice.State) o;
                    if (state != BtLeDevice.State.READY) {
                        context.setState(IDLE);
                    }
                    break;
                case EVENT_MODIFY:
                    ModifyPasswordData data = (ModifyPasswordData) o;
                    BluetoothGattCharacteristic char1 = mDevice.getCharacteristic(0xfff1);
                    if (char1 == null) {
                        Log.e(TAG, "failed to get characteristic 0xfff1");
                        return;
                    }
                    mDevice.writeCharacteristic(char1, BlueLockProtocol.verify(data.adminPassword), new BtLeDevice.ResultListener<Boolean>() {
                        @Override
                        public void onResult(Boolean result) {
                            Log.w(TAG, "wrote admin-password: " + result);
                        }
                    });
                    break;
                default:
                    // ignore
                    break;
            }
        }
    }


    private static final int EVENT_MODIFY = 1;
    private static final int EVENT_DEV_STATE_CHANGED = 2;
    private static final int EVENT_DEV_NOTIFY = 3;

    private final State IDLE = new IdleState();
    private final State WAIT_FOR_DISCONNECT = new WaitForDisconnectState();
    private final State WAIT_FOR_CONNECT = new WaitForConnectState();
    private final State READY = new ReadyState();

    private class ModifyPasswordSM extends StateMachine {
        ModifyPasswordSM() {
            init(IDLE);
        }
    }

    @Override
    public void onDeviceStateChanged(BtLeDevice.State state) {
        modifyPasswordSM.handle(EVENT_DEV_STATE_CHANGED, -1, state);
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        byte[] values = characteristic.getValue();
        if (values == null || values.length < 1) {
            Log.e(TAG, "null values");
            return;
        }
        final byte result = values[0];
        modifyPasswordSM.handle(EVENT_DEV_NOTIFY, result, null);
    }

}
