package com.example.scrcpyserver.connection;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.example.scrcpyserver.device.Device;

import java.io.IOException;

public class ServerSocketThread extends Thread {

    private final int port;
    private Handler handler;

    public ServerSocketThread(int port, Handler handler) {
        this.port = port;
        this.handler = handler;
    }
    @Override
    public void run() {
        try {
            ServerSocketHelper connection = ServerSocketHelper.open(port, handler);
            connection.sendDeviceMeta(Device.getDeviceName());
            while (true) {

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
