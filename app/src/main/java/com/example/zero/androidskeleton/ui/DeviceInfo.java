package com.example.zero.androidskeleton.ui;

/**
 * Created by zero on 2016/4/4.
 */
public class DeviceInfo {
    private final String name;
    private final String address;

    DeviceInfo(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }
}
