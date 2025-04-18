package com.example.scrcpyserver.connection;

import android.view.SurfaceView;

public class TcpHelper {

    private TcpSocketThread tcpSocketThread;
    private SurfaceView surfaceView;

    public TcpHelper(SurfaceView surfaceView) {
        this.surfaceView = surfaceView;
    }

    public void init() {
        tcpSocketThread = new TcpSocketThread(surfaceView);
        tcpSocketThread.start();
    }

}
