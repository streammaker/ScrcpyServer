package com.example.scrcpyserver.util;

public class Constant {
    public static final int SCREEN_WIDTH = 1920;
    public static final int SCREEN_HEIGHT = 1080;
    public static final int DECODER_TIMEOUT_US = 10000;

    public static final int TCP_VIDEO_RECEIVE_PORT = 8888;
    public static final int TCP_CONTACT_PORT = 8889;
    public static final int UDP_VIDEO_RECEIVE_PORT = 9000;

    public static final byte[] startCapture = new byte[] {0x01, 0x01};
    public static final byte[] stopCapture = new byte[] {0x01, 0x02};

    public static final int CLIENT_CONNECTED = 1;

    public static final String VIDEO_SERVER_IP = "192.168.0.143";
}
