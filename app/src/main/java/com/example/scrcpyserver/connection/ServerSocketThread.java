package com.example.scrcpyserver.connection;

import com.example.scrcpyserver.device.Device;

import java.io.IOException;

public class ServerSocketThread extends Thread {

    private final int port;

    public ServerSocketThread(int port) {
        this.port = port;
    }
    @Override
    public void run() {
        try {
            ServerSocketHelper connection = ServerSocketHelper.open(port);
            connection.sendDeviceMeta(Device.getDeviceName());
            while (true) {

            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
