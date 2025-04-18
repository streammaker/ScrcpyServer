package com.example.scrcpyserver;

import android.os.Bundle;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.scrcpyserver.connection.TcpHelper;
import com.example.scrcpyserver.connection.UdpHelper;
import com.example.scrcpyserver.connection.UdpSendThread;
import com.example.scrcpyserver.util.Constant;

public class ServerMainActivity extends AppCompatActivity {
    private static final String TAG = ServerMainActivity.class.getSimpleName();

    private Button startCapture;
    private SurfaceView surfaceView;
    private TcpHelper tcpHelper;
    private UdpHelper udpHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.server_layout);
        init();
        tcpHelper = new TcpHelper(surfaceView);
        udpHelper = new UdpHelper();
        tcpHelper.init();
        udpHelper.init();
        startCapture.setOnClickListener(view -> {
            udpHelper.sendData(Constant.startCapture);
        });
    }

    private void init() {
        startCapture = findViewById(R.id.startCapture1);
        surfaceView = findViewById(R.id.surfaceView);
    }

}