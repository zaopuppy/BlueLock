package com.example.zero.androidskeleton.bt;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;
import com.example.zero.androidskeleton.utils.CRC16;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * Created by zero on 4/11/16.
 */
public class DoorProtocol {

    private static final String TAG = "DoorProtocol";

    // shared byte buffer
    private static final ByteBuffer BUFFER = ByteBuffer.allocate(10*1024);

    public static final byte RESULT_PASSWORD_CORRECT       = 0x01;
    public static final byte RESULT_PASSWORD_WRONG         = 0x02;
    public static final byte RESULT_ADMIN_PASSWORD_CORRECT = 0x03;
    public static final byte RESULT_ADMIN_PASSWORD_WRONG   = 0x04;
    public static final byte RESULT_PASSWORD_CHANGED       = 0x05;
    public static final byte RESULT_PHONE_NUMBER_PASSED    = 0x06;
    public static final byte RESULT_PHONE_NUMBER_NO_PASSED = 0x07;

    /**
     * 1234 -> b'01020304'
     *
     * @param s
     * @return
     */
    private static byte[] encode(String s) {
        byte[]bs = s.getBytes();

        for (int i = 0; i < bs.length; ++i) {
            bs[i] -= '0';
        }

        return bs;
    }

    /**
     *
     * |0x0A|p1 p2 p3 p4 p5 p6|0x0B|
     *
     * @param password
     * @return
     */
    public static byte[] openDoor(int password) {
        if (password < 0 || password >= 1000000) {
            return null;
        }

        synchronized (BUFFER) {
            BUFFER.clear();
            BUFFER.put((byte) 0x0A);
            for (int i = 0; i < 6; ++i) {
                byte b = (byte) (password % 10);
                BUFFER.put(b);
                password = password / 10;
            }
            BUFFER.put((byte) 0x0B);
            BUFFER.flip();
            byte[] b = new byte[BUFFER.remaining()];
            BUFFER.get(b);
            return b;
        }
    }

    public static void openDoor(final BtLeDevice device, final int password) {
        if (device.getState() != BtLeDevice.State.READY) {
            Log.w(TAG, "device is not ready yet");
            return;
        }

        BluetoothGattCharacteristic characteristic1 = device.getCharacteristic(0xfff1);
        BluetoothGattCharacteristic characteristic4 = device.getCharacteristic(0xfff4);
        device.makeNotify(characteristic4, new BtLeDevice.ResultListener<Boolean>() {
            @Override
            public void onResult(Boolean result) {
                Log.e(TAG, "make notify: " + result);
            }
        });
        byte[] msg = DoorProtocol.openDoor(password);
        if (msg == null) {
            // showMsg("invalid password?");
            return;
        }
        device.writeCharacteristic(characteristic1, msg, new BtLeDevice.ResultListener<Boolean>() {
            @Override
            public void onResult(Boolean result) {
                Log.e(TAG, "write result: " + result);
            }
        });
    }

    /**
     * 1 byte checksum 168
     * CRC-16 0xE085
     * CRC-16 (Modbus) 0xFB9E      <---
     * CRC-16 (Sick) 0x3800
     * CRC-CCITT (XModem) 0x2389
     * CRC-CCITT (0xFFFF) 0x1450
     * CRC-CCITT (0x1D0F) 0xE5DF
     * CRC-CCITT (Kermit) 0x721B
     * CRC-DNP 0x34BC
     * CRC-32 0x74B27241
     *
     * @param buffer
     * @param offset
     * @param len
     * @return
     */
    private static short crc16(final byte[] buffer, int offset, int len) {
        CRC16 crc16 = new CRC16();
        crc16.update(buffer, offset, len);
        return (short) crc16.getValue();
    }

    public static class Frame {

        public final short ctrl;
        public final byte seq;
        public final byte[] data;

        public Frame(short ctrl, byte seq, byte[] data) {
            this.ctrl = ctrl;
            this.seq = seq;
            this.data = data;
        }

        // TODO: field verification
        public byte[] encode() {
            // ctrl + seq + data + crc16
            byte len = (byte) (2 + 1 + this.data.length + 2);
            synchronized (BUFFER) {
                BUFFER.clear();
                BUFFER.put(len);
                BUFFER.putShort(this.ctrl);
                BUFFER.put(this.seq);
                BUFFER.put(this.data);
                short crc16 = crc16(BUFFER.array(), 0, BUFFER.position());
                BUFFER.putShort(crc16);
                BUFFER.flip();
                byte[] b = new byte[BUFFER.remaining()];
                BUFFER.get(b);
                return b;
            }
        }

        public static Frame decode(byte[] data) {
            return null;
        }
    }

    /**
     * len|frame-ctrl|seq|data|crc16
     *
     * len = len(frame-ctrl|seq|data|crc16)
     *
     * @param data
     * @return
     */
    private static byte SEQ = 1;
    public static byte[] frame(byte[] data) {
        return new Frame((short) 0x4301, SEQ++, data).encode();
    }

    public static byte[] openDoorV2(String password, String phone) {
        byte[] pass = encode(password);
        byte[] ph = encode(phone);
        byte[] data = new byte[pass.length+ph.length];
        System.arraycopy(pass, 0, data, 0, pass.length);
        System.arraycopy(ph, 0, data, pass.length, ph.length);
        return frame(data);
    }
}
