package com.example.zero.androidskeleton.sort;

import com.example.zero.androidskeleton.bt.BtLeDevice;

import java.util.Comparator;

public class PinyinComparator implements Comparator<BtLeDevice> {

    @Override
    public int compare(BtLeDevice o1, BtLeDevice o2) {
        if (o1.getName().equals("@")
                || o2.getName().equals("#")) {
            return -1;
        } else if (o1.getName().equals("#")
                || o2.getName().equals("@")) {
            return 1;
        } else {
            return o1.getName().compareTo(o2.getName());
        }
    }
}
