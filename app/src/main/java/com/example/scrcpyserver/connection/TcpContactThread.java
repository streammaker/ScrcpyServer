package com.example.scrcpyserver.connection;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.example.scrcpyserver.util.Constant;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TcpContactThread extends Thread {

    private static final String TAG = TcpContactThread.class.getSimpleName();
    //主线程handler
    private Handler mainHandler;
    private ServerSocket serverSocket;
    private Socket contactSocket;
    private InputStream contactInputStream;
    private OutputStream contactOutputStream;
    private Handler handler;

    public TcpContactThread(Handler mainHandler) {
        this.mainHandler = mainHandler;
    }

    @Override
    public void run() {
        try {
            Looper.prepare();
            handler = new Handler(Looper.myLooper());
            serverSocket = new ServerSocket(Constant.TCP_CONTACT_PORT);
            contactSocket = serverSocket.accept();
            String clientIP = contactSocket.getInetAddress().getHostAddress();
            int clientPort = contactSocket.getPort();
            Log.d(TAG, "clientIP : " + clientIP + " clientPort : " + clientPort);
            contactInputStream = contactSocket.getInputStream();
            byte[] receiveData = new byte[1024];
            int len = contactInputStream.read(receiveData);
            String deviceName = new String(receiveData, 0, len);
            Log.d(TAG, "客户端设备名称 : " + deviceName);
            Message message = new Message();
            message.what = Constant.CLIENT_CONNECTED;
            message.obj = deviceName;
            mainHandler.sendMessage(message);
            contactOutputStream = contactSocket.getOutputStream();

            //发送video服务器ip
            contactOutputStream.write(Constant.VIDEO_SERVER_IP.getBytes());
            Log.d(TAG, "发送video服务器ip");

            Looper.loop();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Log.d(TAG, "run() releaseResource");
            releaseResource();
        }
    }

    public void sendTcpData(byte[] data) {
        if (handler != null) {
            handler.post(() -> {
                byte[] sendData = packData(data);
                try {
                    contactOutputStream.write(sendData);
                    Log.d(TAG, "TcpContactThread 数据发送成功");
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

    public void stopRunning() {
        if (handler != null) {
            handler.post(() -> {
                releaseResource();
                if (Looper.myLooper() != null) {
                    Looper.myLooper().quit();
                }
            });
        }
    }

    private void releaseResource() {
        try {
            Log.d(TAG, "releaseResource()");
            if (contactInputStream != null) {
                contactInputStream.close();
                contactInputStream = null;
            }
            if (contactOutputStream != null) {
                contactOutputStream.close();
                contactOutputStream = null;
            }
            if (contactSocket != null && !contactSocket.isClosed()) {
                contactSocket.close();
                contactSocket = null;
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                serverSocket = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
