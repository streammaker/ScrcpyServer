package com.example.scrcpyserver;

import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.scrcpyserver.connection.TcpHelper;
import com.example.scrcpyserver.connection.UdpHelper;
import com.example.scrcpyserver.connection.UdpSendThread;
import com.example.scrcpyserver.util.Constant;

public class ServerMainActivity extends AppCompatActivity {
    private static final String TAG = ServerMainActivity.class.getSimpleName();

    private TextView deviceName;
    private Button startCapture;
    private Button stopCapture;
    private SurfaceView surfaceView;
    private TcpHelper tcpHelper;
    private UdpHelper udpHelper;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server_layout);
        init();
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if (msg.what == Constant.CLIENT_CONNECTED) {
                    String clientName = (String) msg.obj;
                    deviceName.setText(clientName);
                }
            }
        };
        tcpHelper = new TcpHelper(surfaceView);
        udpHelper = new UdpHelper(handler);
        tcpHelper.init();
        udpHelper.init();
        startCapture.setOnClickListener(view -> udpHelper.sendData(Constant.startCapture));
        stopCapture.setOnClickListener(view -> udpHelper.sendData(Constant.stopCapture));
    }

    private void init() {
        deviceName = findViewById(R.id.deviceName);
        startCapture = findViewById(R.id.startCapture);
        stopCapture = findViewById(R.id.stopCapture);
        surfaceView = findViewById(R.id.surfaceView);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
        if (tcpHelper != null) {
            tcpHelper.releaseResource();
            tcpHelper = null;
        }
        if (udpHelper != null) {
            udpHelper.releaseResource();
            udpHelper = null;
        }
    }
}