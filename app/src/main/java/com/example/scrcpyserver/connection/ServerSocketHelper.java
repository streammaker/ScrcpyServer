package com.example.scrcpyserver.connection;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerSocketHelper {

    private static final String TAG = ServerSocketHelper.class.getSimpleName();
    private final Socket videoSocket;
    public static InputStream videoInputStream;
    public static OutputStream videoOutputStream;
    private static final int START_SERVER = 1;
    private static final int CLIENT_CONNECTED = 2;


    public ServerSocketHelper(Socket videoSocket) {
        this.videoSocket = videoSocket;
    }

    public static ServerSocketHelper open(int port, Handler handler) throws IOException {
        Socket videoSocket = null;
        ServerSocket serverSocket = new ServerSocket(port);
        Message message1 = new Message();
        message1.what = START_SERVER;
        handler.sendMessage(message1);
        videoSocket = serverSocket.accept();
        String clientIP = videoSocket.getInetAddress().getHostAddress();
        int clientPort = videoSocket.getPort();
        Message message2 = new Message();
        message2.what = CLIENT_CONNECTED;
        message2.obj = clientIP;
        message2.arg1 = clientPort;
        handler.sendMessage(message2);
        videoInputStream = videoSocket.getInputStream();
        videoOutputStream = videoSocket.getOutputStream();
        return new ServerSocketHelper(videoSocket);
    }

    public void sendDeviceMeta(String deviceName) throws IOException {
        if (videoSocket == null) {
            Log.d(TAG, "videoSocket == null !!!");
            return;
        }
        videoOutputStream.write(deviceName.getBytes());
        Log.d(TAG, "send deviceName success !!! ");
    }
}
