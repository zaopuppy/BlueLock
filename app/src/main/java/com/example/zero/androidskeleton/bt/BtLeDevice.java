package com.example.zero.androidskeleton.bt;

import android.bluetooth.*;
import android.content.Context;
import android.util.Log;
import com.example.zero.androidskeleton.utils.Utils;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by zero on 2016/4/8.
 */
public class BtLeDevice extends BluetoothGattCallback {

    private static final String TAG = "BtLeDevice";

    public enum State {
        DISCONNECTED,
        CONNECTING,
        CONNECTED,
        DISCOVERING_SERVICE,
        READY,
        DISCONNECTING,
    }

    public interface ResultListener<T> {
        void onResult(T result);
    }

    public interface DeviceListener {
        void onDeviceStateChanged(State state);
        void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic);
    }

    private final BluetoothDevice mDevice;

    private State mState = State.DISCONNECTED;
    private BluetoothGatt mGatt = null;

    private interface Task {

        boolean exec();

        // void onResult(characteristic, descriptor, status);
        void onReadResult(
            BluetoothGattCharacteristic characteristic,
            BluetoothGattDescriptor descriptor,
            int status);

        void onWriteResult(
            BluetoothGattCharacteristic characteristic,
            BluetoothGattDescriptor descriptor,
            int status);
    }

    private static class ReadTask implements Task {
        private final BluetoothGattCharacteristic characteristic;
        private final ResultListener<byte[]> listener;
        private final BluetoothGatt gatt;

        private ReadTask(
            BluetoothGatt gatt,
            BluetoothGattCharacteristic characteristic,
            ResultListener<byte[]> listener) {
            this.gatt = gatt;
            this.characteristic = characteristic;
            this.listener = listener;
        }

        @Override
        public boolean exec() {
            if (!innerReadCharacteristic(gatt, characteristic)) {
                return false;
            }

            return true;
        }

        @Override
        public void onReadResult(
            BluetoothGattCharacteristic characteristic,
            BluetoothGattDescriptor descriptor, int status) {

            if (!this.characteristic.equals(characteristic)) {
                throw new IllegalStateException(
                    "expect=" + BtLeUtil.uuidStr(this.characteristic.getUuid())
                        + ", actual=" + BtLeUtil.uuidStr(characteristic.getUuid()));
            }
            if (status != BluetoothGatt.GATT_SUCCESS) {
                listener.onResult(null);
                return;
            }

            listener.onResult(characteristic.getValue());
        }

        @Override
        public void onWriteResult(
            BluetoothGattCharacteristic characteristic,
            BluetoothGattDescriptor descriptor, int status) {

            throw new IllegalStateException("unexpected write result");
        }
    }

    private static class WriteTask implements Task {

        private final BluetoothGatt gatt;
        private final BluetoothGattCharacteristic characteristic;
        private final byte[] data;
        private final ResultListener<Boolean> listener;

        private WriteTask(
            BluetoothGatt gatt,
            BluetoothGattCharacteristic characteristic,
            byte[] data,
            ResultListener<Boolean> listener) {
            this.gatt = gatt;
            this.characteristic = characteristic;
            this.data = data;
            this.listener = listener;
        }

        @Override
        public boolean exec() {
            return innerWriteCharacteristic(gatt, characteristic, data);
        }

        @Override
        public void onReadResult(
            BluetoothGattCharacteristic characteristic,
            BluetoothGattDescriptor descriptor, int status) {

            throw new IllegalStateException("unexpected read result");
        }

        @Override
        public void onWriteResult(
            BluetoothGattCharacteristic characteristic,
            BluetoothGattDescriptor descriptor, int status) {

            if (!this.characteristic.equals(characteristic)) {
                throw new IllegalStateException(
                    "expect=" + BtLeUtil.uuidStr(this.characteristic.getUuid())
                        + ", actual=" + BtLeUtil.uuidStr(characteristic.getUuid()));
            }

            if (status != BluetoothGatt.GATT_SUCCESS) {
                listener.onResult(false);
                return;
            }

            listener.onResult(true);
        }
    }

    private static class WriteDescriptorTask implements Task {

        private final BluetoothGatt gatt;
        private final BluetoothGattCharacteristic characteristic;
        private final BluetoothGattDescriptor descriptor;
        private final byte[] data;
        private final ResultListener<Boolean> listener;

        public WriteDescriptorTask(
            BluetoothGatt gatt,
            BluetoothGattCharacteristic characteristic,
            BluetoothGattDescriptor descriptor, byte[] data,
            ResultListener<Boolean> listener) {
            this.gatt = gatt;
            this.characteristic = characteristic;
            this.descriptor = descriptor;
            this.data = data;
            this.listener = listener;
        }

        @Override
        public boolean exec() {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            return gatt.writeDescriptor(descriptor);
        }

        @Override
        public void onReadResult(
            BluetoothGattCharacteristic characteristic,
            BluetoothGattDescriptor descriptor, int status) {

            throw new IllegalStateException("unexpected descriptor read result");
        }

        @Override
        public void onWriteResult(
            BluetoothGattCharacteristic characteristic,
            BluetoothGattDescriptor descriptor, int status) {

            if (!this.descriptor.equals(descriptor)) {
                throw new IllegalStateException(
                    "expect=" + BtLeUtil.uuidStr(this.descriptor.getUuid())
                        + ", actual=" + BtLeUtil.uuidStr(descriptor.getUuid()));
            }

            if (status != BluetoothGatt.GATT_SUCCESS) {
                listener.onResult(false);
                return;
            }

            listener.onResult(true);
        }
    }

    private final Object mTaskLock = new Object();
    private final Queue<Task> mTaskQueue = new ConcurrentLinkedQueue<>();
    private Task mCurrentTask = null;

    private void processTask() {
        synchronized (mTaskLock) {
            while (mCurrentTask == null) {
                Task task = mTaskQueue.poll();
                if (task == null) {
                    Log.d(TAG, "empty task queue");
                    return;
                }

                if (task.exec()) {
                    mCurrentTask = task;
                } else {
                    Log.e(TAG, "failed to execute task");
                }
            }
        }
    }

    public BtLeDevice(BluetoothDevice device) {
        mDevice = device;
    }

    public String getName() {
        return mDevice.getName();
    }

    public String getAddress() {
        return mDevice.getAddress();
    }

    private final ConcurrentLinkedQueue<DeviceListener> mDeviceListenerList = new ConcurrentLinkedQueue<>();

    public void addDeviceListener(DeviceListener l) {
        mDeviceListenerList.add(l);
    }

    public void removeDeviceListener(DeviceListener l) {
        mDeviceListenerList.remove(l);
    }

    public void connectGatt(Context context) {
        mGatt = mDevice.connectGatt(context, false, this);
    }

    public void disconnectGatt() {
        if (mGatt != null) {
            mGatt.disconnect();
            mGatt = null;
        }
    }

    public State getState() {
        return mState;
    }

    private void setState(State state) {
        if (state == mState) {
            return;
        }
        this.mState = state;

        notifyDeviceStateChanged(state);
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        Log.d(TAG, "onConnectionStateChange: status=" + status + ", newState=" + newState);
        switch (newState) {
            case BluetoothProfile.STATE_DISCONNECTED:
                setState(State.DISCONNECTED);
                break;
            case BluetoothProfile.STATE_CONNECTING:
                setState(State.CONNECTING);
                break;
            case BluetoothProfile.STATE_CONNECTED:
                gatt.discoverServices();
                setState(State.CONNECTING);
                break;
            case BluetoothProfile.STATE_DISCONNECTING:
                setState(State.DISCONNECTING);
                break;
            default:
                // ignore
                break;
        }
    }

    public List<BluetoothGattService> getServiceList() {
        return mGatt.getServices();
    }

    public BluetoothGattCharacteristic getCharacteristic(int uuid16) {
        for (BluetoothGattService service: mGatt.getServices()) {
            for (BluetoothGattCharacteristic characteristic: service.getCharacteristics()) {
                int tmpUuid16 = BtLeUtil.extractBtUuid(characteristic.getUuid());
                if (uuid16 == tmpUuid16) {
                    return characteristic;
                }
            }
        }

        return null;
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (status != BluetoothGatt.GATT_SUCCESS) {
            Log.e(TAG, "failed to discover service");
            gatt.disconnect();
            return;
        }

        List<BluetoothGattService> service_list = gatt.getServices();

        //if (service_list.size() <= 0) {
        //    Log.e(TAG, "no service was discovered");
        //    gatt.disconnect();
        //    return;
        //}

        Log.e(TAG, "device is ready, service count: " + service_list.size());

        setState(State.READY);
    }

    public boolean makeNotify(BluetoothGattCharacteristic characteristic, ResultListener<Boolean> listener) {
        //int prop = characteristic.getProperties();
        //if (Utils.isFlagSet(prop, BluetoothGattCharacteristic.PROPERTY_NOTIFY)) {
        //    Log.d(TAG, BtLeService.uuidStr(characteristic.getUuid()) + " already notify");
        //    return true;
        //}
        if (mGatt == null) {
            return false;
        }

        mGatt.setCharacteristicNotification(characteristic, true);
        List<BluetoothGattDescriptor> descriptor_list = characteristic.getDescriptors();
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(descriptor_list.get(0).getUuid());
        mTaskQueue.offer(new WriteDescriptorTask(
            mGatt, characteristic, descriptor,
            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE, listener));
        processTask();
        return true;
    }

    private static boolean checkProperties(
        BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int expectProp) {

        int prop = characteristic.getProperties();

        Log.d(TAG,
            "checkProperties uuid=" + BtLeUtil.uuidStr(characteristic.getUuid())
                + ", prop=" + prop
                + ", expect-prop=" + expectProp);

        Log.d(TAG, "current prop=" + prop);
        if (!Utils.isFlagSet(prop, expectProp)) {
            Log.d(TAG, "not expect prop: " + BtLeUtil.uuidStr(characteristic.getUuid()));
            return false;
        }

        //if (!Utils.isFlagSet(prop, BluetoothGattCharacteristic.PROPERTY_NOTIFY)) {
        //    Log.d(TAG, "no notify property, modify");
        //    if (!gatt.setCharacteristicNotification(characteristic, true)) {
        //        Log.e(TAG, "failed to set notify");
        //        return false;
        //    }
        //}

        return true;
    }

    private static boolean innerReadCharacteristic(
        BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {

        if (!checkProperties(gatt, characteristic, BluetoothGattCharacteristic.PROPERTY_READ)) {
            Log.e(TAG, "failed to check properties");
            return false;
        }

        return gatt.readCharacteristic(characteristic);
    }

    private static boolean innerWriteCharacteristic(
        BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, byte[] data) {

        if (!checkProperties(gatt, characteristic, BluetoothGattCharacteristic.PROPERTY_WRITE)) {
            Log.e(TAG, "failed to check properties");
            return false;
        }

        if (!characteristic.setValue(data)) {
            Log.e(TAG, "failed to store the data to write");
            return false;
        }

        return gatt.writeCharacteristic(characteristic);
    }

    public void readCharacteristic(BluetoothGattCharacteristic characteristic, ResultListener<byte[]> listener) {
        Log.d(TAG, "readCharacteristic: " + BtLeUtil.uuidStr(characteristic.getUuid()));
        mTaskQueue.offer(new ReadTask(mGatt, characteristic, listener));
        processTask();
    }

    // 128bit
    // 0x00000000000000000000000000020304
    //
    // 64bit, 8bytes
    // 0x0A0000000000000B
    //
    // FIXME: Are all these callback called in the same thread? if not, there will be some problem of this
    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Log.d(TAG, "onCharacteristicRead: " + BtLeUtil.uuidStr(characteristic.getUuid()) + " status=" + status);

        synchronized (mTaskLock) {
            if (mCurrentTask == null) {
                throw new IllegalStateException("no waiting task for characteristic reading");
            }
            mCurrentTask.onReadResult(characteristic, null, status);
            mCurrentTask = null;
            processTask();
        }
    }

    public void writeCharacteristic(BluetoothGattCharacteristic characteristic, byte[] data, ResultListener<Boolean> listener) {
        Log.d(TAG, "writeCharacteristic: " + BtLeUtil.uuidStr(characteristic.getUuid()));
        mTaskQueue.offer(new WriteTask(mGatt, characteristic, data, listener));
        processTask();
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Log.d(TAG, "onCharacteristicWrite: " + BtLeUtil.uuidStr(characteristic.getUuid()) + " status=" + status);

        synchronized (mTaskLock) {
            if (mCurrentTask == null) {
                throw new IllegalStateException("no waiting task for characteristic writing");
            }
            mCurrentTask.onWriteResult(characteristic, null, status);
            mCurrentTask = null;
            processTask();
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        Log.d(TAG, "onCharacteristicChanged, uuid=" + BtLeUtil.uuidStr(characteristic.getUuid()) + ", value-length=" + characteristic.getValue().length);
        ByteBuffer buffer = ByteBuffer.allocate(2);
        buffer.put(characteristic.getValue());
        buffer.flip();
        Log.e(TAG, BtLeUtil.uuidStr(characteristic.getUuid()) + " new value: " + (int)buffer.get());
        notifyCharacteristicChanged(gatt, characteristic);
    }

    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        Log.d(TAG, "onDescriptorRead");
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        Log.d(TAG, "onDescriptorWrite");
        synchronized (mTaskLock) {
            if (mCurrentTask == null) {
                throw new IllegalStateException("no waiting task for descriptor writing");
            }
            mCurrentTask.onWriteResult(null, descriptor, status);
            mCurrentTask = null;
            processTask();
        }
    }

    private void notifyDeviceStateChanged(State state) {
        for (DeviceListener l: mDeviceListenerList) {
            try {
                l.onDeviceStateChanged(state);
            } catch (Exception e) {
                // ignore
            }
        }
    }

    private void notifyCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        for (DeviceListener l: mDeviceListenerList) {
            try {
                l.onCharacteristicChanged(gatt, characteristic);
            } catch (Exception e) {
                // ignore
            }
        }
    }

}
