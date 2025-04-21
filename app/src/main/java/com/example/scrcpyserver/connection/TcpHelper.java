package com.example.scrcpyserver.connection;

import android.util.Log;
import android.view.SurfaceView;

public class TcpHelper {
    private static final String TAG = TcpHelper.class.getSimpleName();

    private TcpSocketThread tcpSocketThread;
    private SurfaceView surfaceView;

    public TcpHelper(SurfaceView surfaceView) {
        this.surfaceView = surfaceView;
    }

    public void init() {
        tcpSocketThread = new TcpSocketThread(surfaceView);
        tcpSocketThread.start();
    }

    public void releaseResource() {
        try {
            if (tcpSocketThread != null) {
                tcpSocketThread.stopRunning();
                tcpSocketThread.join();
                tcpSocketThread = null;
                Log.d(TAG, "tcpSocketThread releaseResource()");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
