package com.example.zero.androidskeleton.bt;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;
import com.example.zero.androidskeleton.utils.CRC16;

import java.nio.ByteBuffer;

/**
 * Created by zero on 4/11/16.
 */
public class BlueLockProtocol {

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

    public static String getCodeDesc(byte code) {
        switch (code) {
            case RESULT_PASSWORD_CORRECT:
                return "开门密码正确";
            case RESULT_PASSWORD_WRONG:
                return "开门密码错误";
            case RESULT_ADMIN_PASSWORD_CORRECT:
                return "管理员身份验证通过";
            case RESULT_ADMIN_PASSWORD_WRONG:
                return "管理员身份验证失败";
            case RESULT_PASSWORD_CHANGED:
                return "修改密码成功";
            case RESULT_PHONE_NUMBER_PASSED:
                return "手机号码透传成功";
            case RESULT_PHONE_NUMBER_NO_PASSED:
                return "手机号码透传失败";
            default:
                return "<unknown: " + code + '>';
        }
    }

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
    public static byte[] unlock(String password) {
        Log.i(TAG, "unlock: [" + password + "]");
        if (password == null || password.length() != 6) {
            Log.e(TAG, "bad password");
            return null;
        }

        synchronized (BUFFER) {
            BUFFER.clear();

            BUFFER.put((byte) 0x0A);
            BUFFER.put(encode(password));
            BUFFER.put((byte) 0x0B);

            BUFFER.flip();
            byte[] bs = new byte[BUFFER.remaining()];
            BUFFER.get(bs);
            return bs;
        }
    }

    public static byte[] verify(String adminPassword) {
        Log.i(TAG, "verify: [" + adminPassword + "]");
        if (adminPassword == null || adminPassword.length() != 6) {
            Log.e(TAG, "bad password");
            return null;
        }

        synchronized (BUFFER) {
            BUFFER.clear();

            BUFFER.put((byte) 0x0C);
            BUFFER.put(encode(adminPassword));
            BUFFER.put((byte) 0x0D);

            BUFFER.flip();
            byte[] bs = new byte[BUFFER.remaining()];
            BUFFER.get(bs);
            return bs;
        }
    }

    public static byte[] modify(String newPassword) {
        Log.i(TAG, "modify: [" + newPassword + "]");
        if (newPassword == null || newPassword.length() != 6) {
            Log.e(TAG, "bad password");
            return null;
        }

        synchronized (BUFFER) {
            BUFFER.clear();

            BUFFER.put((byte) 0x0E);
            BUFFER.put(encode(newPassword));
            BUFFER.put((byte) 0x0F);

            BUFFER.flip();
            byte[] bs = new byte[BUFFER.remaining()];
            BUFFER.get(bs);
            return bs;
        }
    }

    public static byte[] passPhone(String phone) {
        Log.i(TAG, "passPhone: [" + phone + "]");
        if (phone == null || phone.length() != 11) {
            Log.e(TAG, "bad phone number");
            return null;
        }

        synchronized (BUFFER) {
            BUFFER.clear();

            BUFFER.put((byte) 0xA0);
            BUFFER.put(encode(phone));
            BUFFER.put((byte) 0xB0);

            BUFFER.flip();
            byte[] bs = new byte[BUFFER.remaining()];
            BUFFER.get(bs);
            return bs;
        }
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
