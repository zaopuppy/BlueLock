package com.example.zero.androidskeleton.storage;

import android.content.Context;

/**
 * Created by zero on 4/14/16.
 */
public class BtDeviceStorage {

    public static class DeviceInfo implements Storage.Savable {
        private String name;
        private String addr;
        private String password;

        public DeviceInfo() {}

        public DeviceInfo(String name, String addr) {
            this.name = name;
            this.addr = addr;
        }

        public String getName() {
            return name;
        }

        public String getAddr() {
            return addr;
        }

        public void setAddr(String addr) {
            this.addr = addr;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static BtDeviceStorage INSTANCE = new BtDeviceStorage();

    private Storage storage;

    public boolean init(Context context) {
        storage = new SharedPreferenceStorage(context, "bt_devices");
        return true;
    }

    public boolean put(DeviceInfo deviceInfo) {
        if (deviceInfo == null) {
            return false;
        }

        return storage.put(deviceInfo.getAddr(), deviceInfo);
    }

    public DeviceInfo get(String addr) {
        if (addr == null) {
            return null;
        }

        return storage.get(addr, DeviceInfo.class);
    }
}
