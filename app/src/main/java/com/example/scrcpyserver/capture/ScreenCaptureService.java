package com.example.scrcpyserver.capture;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.BitmapFactory;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.scrcpyserver.MainActivity;
import com.example.scrcpyserver.R;
import com.example.scrcpyserver.connection.ServerSocketHelper;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class ScreenCaptureService extends Service {

    private static final String TAG = ScreenCaptureService.class.getSimpleName();
    private static final int NOTIFICATION_ID = 999;
    private static final String CHANNEL_ID = "screen_capture_channel";
    private static final int SCREEN_WIDTH = 1920;
    private static final int SCREEN_HEIGHT = 1080;
    private static final int BIT_RATE = 2_000_000;
    private MediaProjection mediaProjection;
    private MediaCodec encoder;
    private Surface encoderSurface;
    private VirtualDisplay virtualDisplay;
    DataOutputStream dos;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate !!!");
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand !!!");
        if (intent.getAction().equals("ACTION_START_CAPTURE")) {
            createNotificationChannel();
            Notification notification = buildNotification();
            Log.d(TAG, "aaa");
            startForeground(NOTIFICATION_ID, notification);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION);
//            } else {
//                startForeground(NOTIFICATION_ID, notification);
//            }
//            dos = new DataOutputStream(ServerSocketHelper.videoOutputStream);

            int resultCode = intent.getIntExtra("result_code", Activity.RESULT_CANCELED);
            Intent resultData = intent.getParcelableExtra("result_data");
            MediaProjectionManager projectionManager = (MediaProjectionManager) getSystemService(Context.MEDIA_PROJECTION_SERVICE);
            mediaProjection = projectionManager.getMediaProjection(resultCode, resultData);
            startScreenCapture();
            return START_STICKY;
        } else {
            stopSelf();
            return START_NOT_STICKY;
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy !!!");
        if (encoder != null) {
            encoder.stop();
            encoder.release();
        }
        if (virtualDisplay != null) virtualDisplay.release();
        if (mediaProjection != null) mediaProjection.stop();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void createNotificationChannel() {
        NotificationManager mannager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Screen Capture",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            mannager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification() {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Screen Capture Active")
                .setContentText("Streaming device screen...")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.small_icon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.large_icon))
                .build();
    }

    public void startScreenCapture() {
        try {
            initVideoEncoder();
            createVirtualDisplay();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initVideoEncoder() throws IOException {
        MediaFormat format = MediaFormat.createVideoFormat(
                MediaFormat.MIMETYPE_VIDEO_AVC, SCREEN_WIDTH, SCREEN_HEIGHT);
        format.setInteger(MediaFormat.KEY_BIT_RATE, BIT_RATE);
        format.setInteger(MediaFormat.KEY_FRAME_RATE, 60);
        format.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);
        format.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            format.setInteger(MediaFormat.KEY_COLOR_RANGE, MediaFormat.COLOR_RANGE_LIMITED);
//        }
        encoder = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        encoder.setCallback(createEncoderCallback());
        encoderSurface = encoder.createInputSurface();
        encoder.start();
    }

    private MediaCodec.Callback createEncoderCallback() {
        return new MediaCodec.Callback() {
            @Override
            public void onInputBufferAvailable(@NonNull MediaCodec mediaCodec, int i) {

            }

            @Override
            public void onOutputBufferAvailable(@NonNull MediaCodec mediaCodec, int i, @NonNull MediaCodec.BufferInfo bufferInfo) {
//                sendEncodedData(i, bufferInfo);
            }

            @Override
            public void onError(@NonNull MediaCodec mediaCodec, @NonNull MediaCodec.CodecException e) {

            }

            @Override
            public void onOutputFormatChanged(@NonNull MediaCodec mediaCodec, @NonNull MediaFormat mediaFormat) {

            }
        };
    }

    private void createVirtualDisplay() {
        int density = getResources().getDisplayMetrics().densityDpi;
        virtualDisplay = mediaProjection.createVirtualDisplay(
                "ScreenCast",
                SCREEN_WIDTH, SCREEN_HEIGHT, density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                encoderSurface, null, null);
    }

    private void sendEncodedData(int index, MediaCodec.BufferInfo info) {
        ByteBuffer buffer = encoder.getOutputBuffer(index);
        if (buffer == null) return;

        byte[] packet = new byte[info.size];
        buffer.get(packet);

        try {
            dos.writeInt(packet.length);
            dos.write(packet);
            dos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            encoder.releaseOutputBuffer(index, false);
        }
    }

}
