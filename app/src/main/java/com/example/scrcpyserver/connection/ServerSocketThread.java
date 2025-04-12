package com.example.scrcpyserver.connection;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;

import com.example.scrcpyserver.device.Device;

import java.io.IOException;

public class ServerSocketThread extends Thread {

    private static final String TAG = ServerSocketThread.class.getSimpleName();
    private final int port;
    // 主线程handler
    private Handler handler;
    // 当前线程handler
    private Handler serverThreadHandler;
    ServerSocketHelper connection;

    public ServerSocketThread(int port, Handler handler) {
        this.port = port;
        this.handler = handler;
    }
    @Override
    public void run() {
        try {
            Looper.prepare();
            serverThreadHandler = new Handler(Looper.myLooper());
            connection = ServerSocketHelper.open(port, handler);
            connection.sendDeviceMeta(Device.getDeviceName());
            Looper.loop();
            Log.d(TAG, "ServerSocketThread 结束");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopServer() {
        ServerSocketHelper.releaseResource();
        if (serverThreadHandler != null) {
            serverThreadHandler.post(() -> {
                if (Looper.myLooper() != null) {
                    Looper.myLooper().quit();
                }
            });
        }
    }
}
