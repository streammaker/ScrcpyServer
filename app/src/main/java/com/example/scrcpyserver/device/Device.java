package com.example.scrcpyserver.device;

import android.os.Build;

public class Device {

    public static String getDeviceName() {
        return Build.MODEL;
    }
}
