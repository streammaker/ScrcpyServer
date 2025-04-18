package com.example.scrcpyserver.connection;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.scrcpyserver.util.Constant;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class UdpSendThread extends Thread {

    private static final String TAG = UdpSendThread.class.getSimpleName();
    private Handler handler;
    private DatagramSocket datagramSocket;
    private DatagramPacket datagramPacket;

    public UdpSendThread() {

    }

    @Override
    public void run() {
        try {
            Looper.prepare();
            handler = new Handler(Looper.myLooper());
            datagramSocket = new DatagramSocket();
            Looper.loop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendUdpData(byte[] data) {
        if (handler != null) {
            handler.post(() -> {
                byte[] sendData = packData(data);
                try {
                    datagramPacket = new DatagramPacket(sendData, 0, sendData.length, InetAddress.getByName(UdpHelper.clientIP), Constant.UDP_SEND_PORT);
                    datagramSocket.send(datagramPacket);
                    Log.d(TAG, "UdpSendThread 数据发送成功");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public byte[] packData(byte[] data) {
        int position = 0;
        byte[] sendData = new byte[data.length + 2];
        sendData[position++] = 0x07;
        System.arraycopy(data, 0 , sendData, position, data.length);
        sendData[sendData.length - 1] = 0x07;
        return sendData;
    }

}
