package com.example.zero.androidskeleton.ui;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import com.example.zero.androidskeleton.R;
import com.example.zero.androidskeleton.bt.BtLeDevice;
import com.example.zero.androidskeleton.bt.BtLeService;
import com.example.zero.androidskeleton.bt.BtLeUtil;
import com.example.zero.androidskeleton.bt.BlueLockProtocol;
import com.example.zero.androidskeleton.state.Context;
import com.example.zero.androidskeleton.state.State;
import com.example.zero.androidskeleton.state.StateMachine;
import com.example.zero.androidskeleton.utils.Utils;

/**
 *
 * * check if system support bluetooth-le
 * * auto in range unlock
 * * report mobile phone number
 */
public class ShowDeviceActivity extends AppCompatActivity implements BtLeDevice.DeviceListener {
    private static final String TAG = "ShowDeviceActivity";

    private void log(String msg) {
        Log.i(TAG, msg + '\n');
    }

    private BtLeDevice mDevice = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_show_device);

        Intent intent = getIntent();
        if (intent == null || intent.getExtras() == null) {
            finish();
            return;
        }
        Bundle bundle = intent.getExtras();

        mDevice = BtLeService.INSTANCE.getDevice(bundle.getString("addr"));
        if (mDevice == null) {
            Utils.makeToast(this, "no device supplied");
            finish();
            return;
        }

        ActionBar actionBar = getSupportActionBar();
        Log.e(TAG, "action bar: " + actionBar);
        if (actionBar != null) {
            actionBar.setTitle(mDevice.getName());
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        setUiComp();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.show_device_menu, menu);

        final BtLeDevice.State state;
        if (mDevice == null) {
            state = BtLeDevice.State.DISCONNECTED;
        } else {
            state = mDevice.getState();
        }

        switch (state) {
            case DISCONNECTED:
                menu.findItem(R.id.menu_connect).setVisible(true);
                menu.findItem(R.id.menu_disconnect).setVisible(false);
                menu.findItem(R.id.menu_refresh).setActionView(null);
                break;
            case READY:
                menu.findItem(R.id.menu_connect).setVisible(false);
                menu.findItem(R.id.menu_disconnect).setVisible(true);
                menu.findItem(R.id.menu_refresh).setActionView(null);
                break;
            case CONNECTING:
            case CONNECTED:
            case DISCOVERING_SERVICE:
                menu.findItem(R.id.menu_connect).setVisible(false);
                menu.findItem(R.id.menu_disconnect).setVisible(true);
                menu.findItem(R.id.menu_refresh).setActionView(
                        R.layout.actionbar_indeterminate_progress);
                break;
            case DISCONNECTING:
                menu.findItem(R.id.menu_connect).setVisible(true);
                menu.findItem(R.id.menu_connect).setEnabled(false);
                menu.findItem(R.id.menu_disconnect).setVisible(false);
                menu.findItem(R.id.menu_refresh).setActionView(
                        R.layout.actionbar_indeterminate_progress);
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.menu_connect:
                mDevice.connectGatt(getApplicationContext());
                invalidateOptionsMenu();
                break;
            case R.id.menu_disconnect:
                mDevice.disconnectGatt();
                invalidateOptionsMenu();
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onResume() {
        if (mDevice != null) {
            mDevice.addDeviceListener(this);
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mDevice != null) {
            mDevice.removeDeviceListener(this);
            mDevice.disconnectGatt();
        }
        super.onPause();
    }

    private void setUiComp() {
        final EditText passwordEdit = (EditText) findViewById(R.id.password_edit);
        assert passwordEdit != null;

        ImageView unlockImg = (ImageView) findViewById(R.id.icon_mode_img);
        assert unlockImg != null;
        unlockImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String password = passwordEdit.getText().toString();
                if (password.length() != 6) {
                    Utils.makeToast(getApplicationContext(), "password incorrect");
                    return;
                }
                unlock(password);
            }
        });
    }

    private void unlock(String password) {
        // FIXME: delete this line
        log("unlock: " + password);
        unlockSM.handle(EVENT_UNLOCK, -1, password);
    }

    private static final int EVENT_DEV_STATE_CHANGED = 1;
    private static final int EVENT_UNLOCK = 2;

    private final State IDLE = new IdleState();
    private final State WAIT_FOR_DISCONNECT = new WaitForDisconnectState();
    private final State WAIT_FOR_CONNECT = new WaitForConnectState();
    private final State READY = new ReadyState();

    private class IdleState implements State {
        @Override
        public void handle(Context context, int event, int arg, Object o) {
            switch (event) {
                case EVENT_UNLOCK:
                    handleUnlock(context, arg, o);
                default:
                    // ignore
                    break;
            }
        }

        private void handleUnlock(Context context, int arg, Object o) {
            if (!(o instanceof String)) {
                Log.e(TAG, "bad argument, string password expected");
                return;
            }

            String password = (String) o;

            // save password first
            context.putString("password", password);

            // then connect
            connect(context, arg, o);
        }

        private void connect(Context context, int arg, Object o) {
            BtLeDevice.State state = mDevice.getState();
            log("connect: current-state=" + state);

            switch (state) {
                case READY:
                    context.setState(READY);
                    context.handle(EVENT_UNLOCK, arg, o);
                    break;
                case DISCONNECTED:
                    mDevice.connectGatt(ShowDeviceActivity.this);
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
                        mDevice.connectGatt(ShowDeviceActivity.this);
                        context.setState(WAIT_FOR_CONNECT);
                    } else {
                        log("expect disconnect event, received: " + state);
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
                    String password = context.getString("password", null);
                    log("context password: " + password);
                    if (password != null) {
                        context.handle(EVENT_UNLOCK, -1, password);
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
                    // BtLeDevice.State state = (BtLeDevice.State) o;
                    // handleStateChanged(context, state);
                    context.setState(IDLE);
                    break;
                case EVENT_UNLOCK:
                    //String password = context.getString("password", null);
                    //if (password == null) {
                    //    log("no password , don't unlock");
                    //    return;
                    //}
                    String password = (String) o;
                    BluetoothGattCharacteristic char1 = mDevice.getCharacteristic(0xfff1);
                    if (char1 == null) {
                        log("failed to get characteristic 0xfff1");
                        return;
                    }
                    mDevice.writeCharacteristic(char1, BlueLockProtocol.unlock(password), new BtLeDevice.ResultListener<Boolean>() {
                        @Override
                        public void onResult(Boolean result) {
                            log("wrote password: " + result);
                            if (result) {
                                /* TODO: pass phone number */
                                /* mDevice.writeCharacteristic(); */
                            }
                        }
                    });
                    break;
                default:
                    // ignore
                    break;
            }
        }
    }

    /**
     * CONNECT --> OPEN --> TRANSFER-NUM -> DONE
     */
    private class UnlockSM extends StateMachine {
        public UnlockSM() {
            init(IDLE);
        }
    }

    private final UnlockSM unlockSM = new UnlockSM();

    private String mPassword = "";
    private AlertDialog createPasswordDialog() {
        // create password view
        View passView = getLayoutInflater().inflate(R.layout.input_password, null);
        final EditText passText = (EditText) passView.findViewById(R.id.password_edit);
        assert passText != null;

        // create dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        return builder
            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        mPassword = passText.getText().toString();
                        open(mPassword);
                    } catch (NumberFormatException e) {
                        log("invalid password");
                    }
                }
            })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Utils.makeToast(ShowDeviceActivity.this, "Cancel");
                }
            })
            .setView(passView)
            .create();
    }

    private void open(final String password) {
        if (mDevice.getState() != BtLeDevice.State.READY) {
            Utils.makeToast(ShowDeviceActivity.this, "device is not yet ready");
            return;
        }

        BluetoothGattCharacteristic characteristic1 = null;
        BluetoothGattCharacteristic characteristic2 = null;
        BluetoothGattCharacteristic characteristic3 = null;
        BluetoothGattCharacteristic characteristic4 = null;

        for (BluetoothGattService service : mDevice.getServiceList()) {
            Log.i(TAG, "service: " + BtLeUtil.uuidStr(service.getUuid()));

            for (final BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                final int uuid16 = BtLeUtil.extractBtUuid(characteristic.getUuid());
                if (BtLeUtil.isReservedUuid(uuid16)) {
                    continue;
                }

                switch (uuid16) {
                    case 0xfff1:
                        characteristic1 = characteristic;
                        break;
                    case 0xfff2:
                        characteristic2 = characteristic;
                        break;
                    case 0xfff3:
                        characteristic3 = characteristic;
                        break;
                    case 0xfff4:
                        characteristic4 = characteristic;
                        break;
                    default:
                        // ignore
                        break;
                }
            }
        }

        mDevice.makeNotify(characteristic4, new BtLeDevice.ResultListener<Boolean>() {
            @Override
            public void onResult(Boolean result) {
                Log.e(TAG, "make notify: " + result);
            }
        });

        byte[] msg = BlueLockProtocol.unlock(password);
        if (msg == null) {
            log("invalid password?");
            return;
        }
        mDevice.writeCharacteristic(characteristic1, msg, new BtLeDevice.ResultListener<Boolean>() {
            @Override
            public void onResult(Boolean result) {
                Log.e(TAG, "write result: " + result);
            }
        });
    }


    @Override
    public void onDeviceStateChanged(final BtLeDevice.State state) {
        Log.e(TAG, "new state: " + state);

        unlockSM.handle(EVENT_DEV_STATE_CHANGED, -1, state);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                invalidateOptionsMenu();
            }
        });
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        byte[] value = characteristic.getValue();
        if (value == null || value.length <= 0) {
            // ignore
            return;
        }

        final byte result = characteristic.getValue()[0];
        final String resultStr;
        switch (result) {
            case BlueLockProtocol.RESULT_PASSWORD_CORRECT: {
                resultStr = "开门密码正确";
                Log.d(TAG, "save password: " + mPassword);
                // BtDeviceStorage.INSTANCE.put(mDevice.getAddress(), mPassword);
                break;
            }
            case BlueLockProtocol.RESULT_PASSWORD_WRONG: {
                resultStr = "开门密码错误";
                Log.d(TAG, "bad password clear: " + mPassword);
                // BtDeviceStorage.INSTANCE.put(mDevice.getAddress(), -1);
                break;
            }
            case BlueLockProtocol.RESULT_PASSWORD_CHANGED: {
                resultStr = "修改密码成功";
                Log.d(TAG, "password changed: " + mPassword);
                // BtDeviceStorage.INSTANCE.put(mDevice.getAddress(), mPassword);
                break;
            }
            default: {
                resultStr = "" + result;
                break;
            }
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                log("result: " + resultStr);
            }
        });
    }
}
