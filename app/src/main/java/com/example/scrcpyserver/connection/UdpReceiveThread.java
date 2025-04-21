package com.example.scrcpyserver.connection;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.example.scrcpyserver.ServerMainActivity;
import com.example.scrcpyserver.util.Constant;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

public class UdpReceiveThread extends Thread {

    private static final String TAG = UdpReceiveThread.class.getSimpleName();
    private volatile boolean isRunning = true;
    private Boolean isFirst;
    private DatagramSocket datagramSocket;
    private DatagramPacket datagramPacket;
    private Handler handler;

    public UdpReceiveThread(Handler handler) {
        isFirst = true;
        this.handler = handler;
    }

    @Override
    public void run() {
        try {
            datagramSocket = new DatagramSocket(Constant.UDP_RECEIVE_PORT);
            while (isRunning) {
                byte[] container = new byte[1024];
                datagramPacket = new DatagramPacket(container, container.length);
                datagramSocket.receive(datagramPacket);
                byte[] data = datagramPacket.getData();
                int len = datagramPacket.getLength();
                if (isFirst) {
                    String clientIP = datagramPacket.getAddress().getHostAddress();
                    int clientPort = datagramPacket.getPort();
                    UdpHelper.saveClientInfo(clientIP, clientPort);
                    isFirst = false;
                    String msg = new String(data, 0, len);
                    Log.d(TAG, "receive data : " + msg);
                    Message message = new Message();
                    message.what = Constant.CLIENT_CONNECTED;
                    message.obj = msg;
                    handler.sendMessage(message);
                } else {
                    checkData(data, len);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Log.d(TAG, "run() releaseResource");
            releaseResource();
        }
    }

    private void checkData(byte[] data, int length) {
        if (data[0] == 0x07 && data[length - 1] == 0x07) {
            Log.d(TAG, "UdpReceiveThread 数据包检查正确");
            byte[] realData = Arrays.copyOfRange(data, 1, length - 1);
            onReceive(realData, length - 2);
        } else {
            Log.d(TAG, "UdpReceiveThread 数据包检查错误");
        }
    }
    private void onReceive(byte[] data, int length) {
        int position = 0;
        if (data[position++] == 0x01) {
            byte content = data[position++];
            if (content == 0x01) {
                //开启屏幕捕获
            }
        }
    }

    public void stopRunning() {
        isRunning = false;
        releaseResource();
    }

    private void releaseResource() {
        Log.d(TAG, "releaseResource()");
        if (datagramSocket != null && !datagramSocket.isClosed()) {
            datagramSocket.close();
            datagramSocket = null;
        }
    }

}
