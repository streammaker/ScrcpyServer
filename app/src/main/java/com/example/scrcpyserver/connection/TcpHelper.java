package com.example.scrcpyserver.connection;

import android.os.Handler;
import android.util.Log;
import android.view.SurfaceView;

public class TcpHelper {
    private static final String TAG = TcpHelper.class.getSimpleName();

    private TcpContactThread tcpContactThread;
    private TcpVideoThread tcpVideoThread;
    private SurfaceView surfaceView;
    private Handler handler;

    public TcpHelper(SurfaceView surfaceView, Handler handler) {
        this.surfaceView = surfaceView;
        this.handler = handler;
    }

    public void init() {
        tcpContactThread = new TcpContactThread(handler, surfaceView);
        tcpContactThread.start();
//        tcpVideoThread = new TcpVideoThread(surfaceView);
//        tcpVideoThread.start();

    }

    public void sendData(byte[] data) {
        tcpContactThread.sendTcpData(data);
    }

    public void releaseResource() {
        try {
            if (tcpContactThread != null) {
                tcpContactThread.stopRunning();
                tcpContactThread.join();
                tcpContactThread = null;
                Log.d(TAG, "tcpContactThread releaseResource()");
            }
            if (tcpVideoThread != null) {
                tcpVideoThread.stopRunning();
                tcpVideoThread.join();
                tcpVideoThread = null;
                Log.d(TAG, "tcpVideoThread releaseResource()");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
