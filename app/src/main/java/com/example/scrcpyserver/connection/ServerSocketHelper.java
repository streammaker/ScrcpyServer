package com.example.scrcpyserver.connection;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.example.scrcpyserver.util.Constant;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerSocketHelper {

    private static final String TAG = ServerSocketHelper.class.getSimpleName();
    private static ServerSocket serverSocket;
    private static Socket videoSocket;
    public static InputStream videoInputStream;
    public static OutputStream videoOutputStream;

    public ServerSocketHelper(Socket videoSocket) {
        this.videoSocket = videoSocket;
    }

    public static ServerSocketHelper open(int port, Handler handler) {
        Socket videoSocket = null;
        try {
            serverSocket = new ServerSocket(port);
            Message message1 = new Message();
            message1.what = Constant.START_SERVER;
            handler.sendMessage(message1);
            Log.d(TAG, "start Server success, wait client connect!!!");
            videoSocket = serverSocket.accept();
            String clientIP = videoSocket.getInetAddress().getHostAddress();
            int clientPort = videoSocket.getPort();
            Message message2 = new Message();
            message2.what = Constant.CLIENT_CONNECTED;
            message2.obj = clientIP;
            message2.arg1 = clientPort;
            handler.sendMessage(message2);
            videoInputStream = videoSocket.getInputStream();
            videoOutputStream = videoSocket.getOutputStream();
        } catch (Exception e) {
            Log.d(TAG, "ServerSocketHelper releaseResource");
            releaseResource();
            e.printStackTrace();
        }
        return new ServerSocketHelper(videoSocket);
    }

    public void sendDeviceMeta(String deviceName) throws IOException {
        try {
            if (videoSocket == null) {
                Log.d(TAG, "videoSocket == null !!!");
                return;
            }
            videoOutputStream.write(deviceName.getBytes());
            Log.d(TAG, "send deviceName success !!! ");
        } catch (IOException e) {
            Log.d(TAG, "sendDeviceMeta releaseResource");
            releaseResource();
            e.printStackTrace();
        }
    }

    public static void releaseResource() {
        try {
            Log.d(TAG, "releaseResource");
            if (videoInputStream != null) {
                videoInputStream.close();
                videoInputStream = null;
            }
            if (videoOutputStream != null) {
                videoOutputStream.close();
                videoOutputStream = null;
            }
            if (videoSocket != null && !videoSocket.isClosed()) {
                videoSocket.close();
                videoSocket = null;
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
