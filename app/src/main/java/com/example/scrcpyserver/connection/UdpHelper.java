package com.example.scrcpyserver.connection;

import android.os.Handler;
import android.util.Log;

import com.example.scrcpyserver.ServerMainActivity;

public class UdpHelper {
    private static final String TAG = UdpHelper.class.getSimpleName();

    public static String clientIP;
    public static int clientPort;
    private UdpReceiveThread udpReceiveThread;
    private UdpSendThread udpSendThread;
    private Handler handler;

    public UdpHelper(Handler handler) {
        this.handler = handler;
    }

    public void init() {
        udpReceiveThread = new UdpReceiveThread(handler);
        udpReceiveThread.start();
        udpSendThread = new UdpSendThread();
        udpSendThread.start();
    }

    public void sendData(byte[] data) {
        udpSendThread.sendUdpData(data);
    }

    public static void saveClientInfo(String ip, int port) {
        clientIP = ip;
        clientPort = port;
    }

    public void releaseResource() {
        try {
            if (udpReceiveThread != null) {
                udpReceiveThread.stopRunning();
                udpReceiveThread.join();
                udpReceiveThread = null;
                Log.d(TAG, "udpReceiveThread releaseResource()");
            }
            if (udpSendThread != null) {
                udpSendThread.stopRunning();
                udpSendThread.join();
                udpSendThread = null;
                Log.d(TAG, "udpSendThread releaseResource()");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
