package com.example.zero.androidskeleton.state.impl;

import android.bluetooth.BluetoothGattCharacteristic;
import com.example.zero.androidskeleton.bt.BlueLockProtocol;
import com.example.zero.androidskeleton.bt.BtLeDevice;
import com.example.zero.androidskeleton.log.Log;
import com.example.zero.androidskeleton.state.Context;
import com.example.zero.androidskeleton.state.State;
import com.example.zero.androidskeleton.state.StateMachine;
import com.example.zero.androidskeleton.utils.Utils;

/**
 * Created by zhaoyi on 7/7/16.
 */
public class PhoneUnlockSM extends StateMachine {
    private static final String TAG = "PhoneUnlockSM";


    public static final int EVENT_DEV_STATE_CHANGED = 1;
    public static final int EVENT_UNLOCK = 2;

    private final State IDLE = new IdleState();
    private final State WAIT_FOR_DISCONNECT = new WaitForDisconnectState();
    private final State WAIT_FOR_CONNECT = new WaitForConnectState();
    private final State READY = new ReadyState();

    private final BtLeDevice mDevice;
    private final android.content.Context mContext;

    public PhoneUnlockSM(android.content.Context context, BtLeDevice device) {
        mContext = context;
        mDevice = device;
        init(IDLE);
    }

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

            String phoneNum = (String) o;

            // save password first
            context.putString("phone-num", phoneNum);

            // then connect
            connect(context, arg, o);
        }

        private void connect(Context context, int arg, Object o) {
            BtLeDevice.State state = mDevice.getState();
            Log.d(TAG, "connect: current-state=" + state);

            switch (state) {
                case READY:
                    context.setState(READY);
                    context.handle(EVENT_UNLOCK, arg, o);
                    break;
                case DISCONNECTED:
                    mDevice.connectGatt(mContext);
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
                        mDevice.connectGatt(mContext);
                        context.setState(WAIT_FOR_CONNECT);
                    } else {
                        Log.w(TAG, "expect disconnect event, received: " + state);
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
                    String phoneNum = context.getString("phoneNum", null);
                    Log.d(TAG, "context phone number: " + phoneNum);
                    if (phoneNum != null) {
                        context.handle(EVENT_UNLOCK, -1, phoneNum);
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
                    context.setState(IDLE);
                    break;
                case EVENT_UNLOCK:
                    String phoneNum = (String) o;
                    BluetoothGattCharacteristic char1 = mDevice.getCharacteristic(0xfff1);
                    if (char1 == null) {
                        Log.e(TAG, "failed to get characteristic 0xfff1");
                        return;
                    }
                    if (phoneNum == null || phoneNum.length() != 11) {
                        Utils.makeToast(mContext, "invalid phone number read: " + phoneNum);
                        return;
                    }
                    mDevice.writeCharacteristic(char1, BlueLockProtocol.phoneUnlock(phoneNum), new BtLeDevice.ResultListener<Boolean>() {
                        @Override
                        public void onResult(Boolean result) {
                            Log.d(TAG, "wrote phone num: " + result);
                        }
                    });

                    break;
                default:
                    // ignore
                    break;
            }
        }
    }

}
