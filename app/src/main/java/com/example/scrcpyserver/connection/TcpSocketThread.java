package com.example.scrcpyserver.connection;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.SurfaceView;

import com.example.scrcpyserver.util.Constant;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class TcpSocketThread extends Thread {

    private static final String TAG = TcpSocketThread.class.getSimpleName();
    private SurfaceView surfaceView;
    private ServerSocket serverSocket;
    private Socket videoSocket;
    private InputStream videoInputStream;
    DataInputStream dis;
    private MediaCodec mDecoder;

    public TcpSocketThread(SurfaceView surfaceView) {
        this.surfaceView = surfaceView;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(Constant.TCP_RECEIVE_PORT);
            videoSocket = serverSocket.accept();
            videoInputStream = videoSocket.getInputStream();
            dis = new DataInputStream(videoInputStream);
            initializeDecoder();
            while (true) {
                Log.d(TAG, "prepare receive video data");
                processNetworkPacket();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeDecoder() {
        try {
            MediaFormat format = MediaFormat.createVideoFormat(
                    MediaFormat.MIMETYPE_VIDEO_AVC, Constant.SCREEN_WIDTH, Constant.SCREEN_HEIGHT);
            mDecoder = MediaCodec.createDecoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
            mDecoder.configure(format, surfaceView.getHolder().getSurface(), null, 0);
            mDecoder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processNetworkPacket() throws IOException {
        int packetSize = dis.readInt();
        if (packetSize <= 0) return;
        byte[] frameData = new byte[packetSize];
        dis.readFully(frameData, 0, packetSize);
        feedDataToDecoder(frameData);
    }

    private void feedDataToDecoder(byte[] data) {
        if (mDecoder == null) return;
        try {
            int inputBufferIndex = mDecoder.dequeueInputBuffer(Constant.DECODER_TIMEOUT_US);
            if (inputBufferIndex >= 0) {
                ByteBuffer inputBuffer = mDecoder.getInputBuffer(inputBufferIndex);
                inputBuffer.put(data);
                mDecoder.queueInputBuffer(
                        inputBufferIndex,
                        0,
                        data.length,
                        System.nanoTime() / 1000,
                        0
                );
                renderDecodedFrames();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void renderDecodedFrames() {
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex;
        while ((outputBufferIndex = mDecoder.dequeueOutputBuffer(bufferInfo, Constant.DECODER_TIMEOUT_US)) >= 0) {
            mDecoder.releaseOutputBuffer(outputBufferIndex, true);
        }
    }

}
