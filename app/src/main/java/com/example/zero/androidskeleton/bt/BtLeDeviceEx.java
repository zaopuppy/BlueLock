package com.example.zero.androidskeleton.bt;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Context;
import com.example.zero.androidskeleton.concurrent.Promise;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by zhaoyi on 30/10/2016.
 */
public class BtLeDeviceEx extends BluetoothGattCallback {

    public enum State {
        UNKNOWN,
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        DISCOVERING_SERVICE,
        READY,
        DISCONNECTING,
    }

    private final BluetoothDevice mDevice;

    private final Object mDevLock = new Object();
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

        synchronized (mDevLock) {
            if (mGatt == null) {
                mGatt = mDevice.connectGatt(context, false, this);
                if (mGatt == null) {
                    promise.complete(Promise.CODE_FAIL, false);
                } else {
                    // TODO
                }
            } else {
                if (!mGatt.connect()) {
                    promise.complete(Promise.CODE_FAIL, false);
                } else {
                    //
                }
            }
        }

        return promise;
    }

    public State getState() {
        return mState;
    }

    private final ConcurrentLinkedQueue<BluetoothGattCallback> mCallbackQueue = new ConcurrentLinkedQueue<>();
    public void addCallback(BluetoothGattCallback callback) {
        mCallbackQueue.add(callback);
    }

    // ----------------------------------------------

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
    }
}
