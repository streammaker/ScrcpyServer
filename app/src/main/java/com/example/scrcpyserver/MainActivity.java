package com.example.scrcpyserver;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.scrcpyserver.capture.ScreenCaptureService;
import com.example.scrcpyserver.connection.ServerSocketHelper;
import com.example.scrcpyserver.connection.ServerSocketThread;
import com.example.scrcpyserver.device.Device;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Context context;
    private Button startServer;
    private Button startCapture;
    private Button stopCapture;
    private EditText ipText;
    private EditText portText;
    private TextView tv_status;
    private TextView tv_debug;

    private MediaProjectionManager mediaProjectionManager;
    private ActivityResultLauncher launcher;
    private Handler handler;
    private static final int START_SERVER = 1;
    private static final int CLIENT_CONNECTED = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_main);
        init();
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                if (msg.what == START_SERVER) {
                    tv_status.setText("Server has Started !!!");
                } else if (msg.what == CLIENT_CONNECTED) {
                    String clientIP = (String) msg.obj;
                    int clientPort = msg.arg1;
                    tv_status.setText("Client Connected --- Client IP : " + clientIP + " Client Port : " + clientPort);
                }
            }
        };
        launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Intent serviceIntent = new Intent(context, ScreenCaptureService.class);
                serviceIntent.setAction("ACTION_START_CAPTURE");
                serviceIntent.putExtra("result_code", result.getResultCode());
                serviceIntent.putExtra("result_data", result.getData());
                startService(serviceIntent);
            } else {
                Toast.makeText(context, "授权失败", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "授权失败");
            }
        });

        startServer.setOnClickListener(view -> {
            ServerSocketThread serverSocketThread = new ServerSocketThread(Integer.valueOf(portText.getText().toString()), handler);
            serverSocketThread.start();
        });
        startCapture.setOnClickListener(view -> {
            requestScreenCapturePermission();
        });
        stopCapture.setOnClickListener(view -> {
            stopCapture();
        });

    }

    private void requestScreenCapturePermission() {
        mediaProjectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        launcher.launch(mediaProjectionManager.createScreenCaptureIntent());
    }

    private void stopCapture() {
        Intent serviceIntent = new Intent(context, ScreenCaptureService.class);
        stopService(serviceIntent);
    }

    private void init() {
        startServer = findViewById(R.id.startServer);
        startCapture = findViewById(R.id.startCapture);
        stopCapture = findViewById(R.id.stopCapture);
        ipText = findViewById(R.id.ipText);
        portText = findViewById(R.id.portText);
        tv_status = findViewById(R.id.tv_status);
        tv_debug = findViewById(R.id.tv_debug);
    }

}