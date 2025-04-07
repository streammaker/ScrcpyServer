package com.example.scrcpyserver;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.scrcpyserver.connection.ServerSocketHelper;
import com.example.scrcpyserver.device.Device;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private Button startServer;
    private EditText ipText;
    private EditText portText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
        startServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    ServerSocketHelper connection = ServerSocketHelper.open(Integer.valueOf(portText.getText().toString()));
                    connection.sendDeviceMeta(Device.getDeviceName());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void init() {
        startServer = findViewById(R.id.startServer);
        ipText = findViewById(R.id.ipText);
        portText = findViewById(R.id.portText);
    }
}