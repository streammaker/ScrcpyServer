package com.example.scrcpyserver.connection;

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
    private static InputStream videoInputStream;
    private static OutputStream videoOutputStream;


    public ServerSocketHelper(Socket videoSocket) {
        this.videoSocket = videoSocket;
    }

    public static ServerSocketHelper open(int port) throws IOException {
        Socket videoSocket = null;
        ServerSocket serverSocket = new ServerSocket(port);
        videoSocket = serverSocket.accept();
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
