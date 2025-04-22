package com.example.scrcpyserver.connection;

import android.media.MediaCodec;
import android.media.MediaFormat;
import android.util.Log;
import android.view.SurfaceView;

import com.example.scrcpyserver.util.Constant;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

public class TcpSocketThread extends Thread {

    private static final String TAG = TcpSocketThread.class.getSimpleName();
    private volatile boolean isRunning = true;
    private SurfaceView surfaceView;
    private ServerSocket serverSocket;
    private Socket videoSocket;
    private InputStream videoInputStream;
    private DataInputStream dis;
    private MediaCodec mDecoder;

    public TcpSocketThread(SurfaceView surfaceView) {
        this.surfaceView = surfaceView;
    }

    @Override
    public void run() {
        try {
            serverSocket = new ServerSocket(Constant.TCP_RECEIVE_PORT);
            videoSocket = serverSocket.accept();
            String clientIP = videoSocket.getInetAddress().getHostAddress();
            int clientPort = videoSocket.getPort();
            Log.d(TAG, "clientIP : " + clientIP + " clientPort : " + clientPort);
            videoInputStream = videoSocket.getInputStream();
            dis = new DataInputStream(videoInputStream);

            //tcp传输测试
            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(videoSocket.getOutputStream()));
            bufferedWriter.write("这是服务器发来的tcp测试数据");
            bufferedWriter.newLine();
            bufferedWriter.flush();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(videoInputStream));
            String line = "";
            Log.d(TAG, "tcp传输测试");
            if ((line = bufferedReader.readLine()) != null) {
                Log.d(TAG, "data : " + line);
            }


//            initializeDecoder();
//            while (isRunning) {
//                Log.d(TAG, "prepare receive video data");
//                processNetworkPacket();
//            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Log.d(TAG, "run() releaseResource");
            releaseResource();
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

    private void processNetworkPacket() {
        try {
            int packetSize = dis.readInt();
            if (packetSize <= 0) {
                Log.d(TAG, "packetSize <= 0");
                return;
            }
            Log.d(TAG, "packetSize : " + packetSize);
            byte[] frameData = new byte[packetSize];
            dis.readFully(frameData, 0, packetSize);
            feedDataToDecoder(frameData);
            Log.d(TAG, "1111");
        } catch (EOFException e) {

        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void feedDataToDecoder(byte[] data) {
        if (mDecoder == null) return;
        Log.d(TAG, "2222");
        try {
            int inputBufferIndex = mDecoder.dequeueInputBuffer(Constant.DECODER_TIMEOUT_US);
            Log.d(TAG, "3333" + " inputBufferIndex : " + inputBufferIndex);
            if (inputBufferIndex >= 0) {
                Log.d(TAG, "4444");
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

    public void stopRunning() {
        isRunning = false;
        releaseResource();
    }

    private void releaseResource() {
        Log.d(TAG, "releaseResource()");
        if (mDecoder != null) {
            mDecoder.stop();
            mDecoder.release();
            mDecoder = null;
        }
        try {
            if (dis != null) {
                dis.close();
                dis = null;
            }
            if (videoInputStream != null) {
                videoInputStream.close();
                videoInputStream = null;
            }
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                serverSocket = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
