package com.example.zero.androidskeleton.bt;

import android.bluetooth.*;
import android.content.Context;
import com.example.zero.androidskeleton.concurrent.Promise;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Created by zhaoyi on 30/10/2016.
 */
public class BtLeDeviceEx extends BluetoothGattCallback {

    void foo() {
        BtLeDeviceEx device = new BtLeDeviceEx(null);
        Promise<Boolean> promise = device.connect(null);
    }

    private final Executor mExecutor = Executors.newSingleThreadExecutor();

    private void submit(Runnable task) {
        mExecutor.execute(task);
    }

    public enum State {
        UNKNOWN,
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        // DISCOVERING_SERVICE,
        // READY,
        DISCONNECTING,
    }

    private final BluetoothDevice mDevice;

    // private final Object mDevLock = new Object();
    private BluetoothGatt mGatt;
    private State mState = State.UNKNOWN;

    public BtLeDeviceEx(BluetoothDevice device) {
        mDevice = device;
    }

    public String getName() {
        return mDevice.getName();
    }

    public String getAddress() {
        return mDevice.getAddress();
    }

    public Promise<Boolean> connect(Context context) {
        final Promise<Boolean> promise = new Promise<>();

        submit(() -> {

            addCallback(new BluetoothGattCallback() {
                @Override
                public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                    switch (newState) {
                        case BluetoothProfile.STATE_CONNECTED:
                            promise.complete(Promise.CODE_SUCCESS, true);
                            // don't forget to remove this callback
                            removeCallback(this);
                            break;
                        case BluetoothProfile.STATE_DISCONNECTED:
                            promise.complete(Promise.CODE_SUCCESS, false);
                            // don't forget to remove this callback
                            removeCallback(this);
                            break;
                        default:
                            break;
                    }
                }
            });

            if (mGatt == null) {
                mGatt = mDevice.connectGatt(context, false, this);
            }
        });

        return promise;

        final BluetoothGattCallback callback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (status)
            }
        }

        synchronized (mDevLock) {
            if (mGatt == null) {
                mGatt = mDevice.connectGatt(context, false, this);
                if (mGatt == null) {
                    promise.complete(Promise.CODE_FAIL, false);
                } else {
                    addCallback(new BluetoothGattCallback() {
                        @Override
                        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                            if (status != )
                        }
                    });
                }
            } else {
                if (!mGatt.connect()) {
                    promise.complete(Promise.CODE_FAIL, false);
                } else {
                    // TODO
                }
            }
        }

        return promise;
    }

    public State getState() {
        return mState;
    }


    private final List<WeakReference<BluetoothGattCallback>> mCallbackList = new ArrayList<>();

    /**
     * Add callback which will be deteled automatically if released.
     *
     * @param callback    the callback we wanna add
     */
    public void addCallback(BluetoothGattCallback callback) {
        synchronized (mCallbackList) {
            mCallbackList.add(new WeakReference<>(callback));
        }
    }

    public void removeCallback(BluetoothGattCallback cb) {
        synchronized (mCallbackList) {
            ArrayList<WeakReference<BluetoothGattCallback>> toBeRemoved = new ArrayList<>();
            for (WeakReference<BluetoothGattCallback> ref : mCallbackList) {
                BluetoothGattCallback callback = ref.get();
                if (callback == null) {
                    toBeRemoved.add(ref);
                    continue;
                }
                if (callback == cb) {
                    toBeRemoved.add(ref);
                    break;
                }
            }
            mCallbackList.removeAll(toBeRemoved);
        }
    }

    // ----------------------------------------------

    private ArrayList<BluetoothGattCallback> getCallbackList() {
        ArrayList<BluetoothGattCallback> toBeCalled = new ArrayList<>();

        synchronized (mCallbackList) {
            ArrayList<WeakReference<BluetoothGattCallback>> toBeRemoved = new ArrayList<>();
            for (WeakReference<BluetoothGattCallback> ref : mCallbackList) {
                BluetoothGattCallback callback = ref.get();
                if (callback == null) {
                    toBeRemoved.add(ref);
                    continue;
                }
                toBeCalled.add(callback);
            }
            mCallbackList.removeAll(toBeRemoved);
        }

        return toBeCalled;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        for (BluetoothGattCallback callback: getCallbackList()) {
            callback.onConnectionStateChange(gatt, status, newState);
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        for (BluetoothGattCallback callback: getCallbackList()) {
            callback.onServicesDiscovered(gatt, status);
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        for (BluetoothGattCallback callback: getCallbackList()) {
            callback.onCharacteristicRead(gatt, characteristic, status);
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        for (BluetoothGattCallback callback: getCallbackList()) {
            callback.onCharacteristicWrite(gatt, characteristic, status);
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        for (BluetoothGattCallback callback: getCallbackList()) {
            callback.onCharacteristicChanged(gatt, characteristic);
        }
    }

    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        for (BluetoothGattCallback callback: getCallbackList()) {
            callback.onDescriptorRead(gatt, descriptor, status);
        }
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        for (BluetoothGattCallback callback: getCallbackList()) {
            callback.onDescriptorWrite(gatt, descriptor, status);
        }
    }

    @Override
    public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
        for (BluetoothGattCallback callback: getCallbackList()) {
            callback.onReliableWriteCompleted(gatt, status);
        }
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        for (BluetoothGattCallback callback: getCallbackList()) {
            callback.onReadRemoteRssi(gatt, rssi, status);
        }
    }

    @Override
    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
        for (BluetoothGattCallback callback: getCallbackList()) {
            callback.onMtuChanged(gatt, mtu, status);
        }
    }
}
