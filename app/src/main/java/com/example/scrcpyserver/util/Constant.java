package com.example.scrcpyserver.util;

public class Constant {
    public static final int SCREEN_WIDTH = 1920;
    public static final int SCREEN_HEIGHT = 1080;
    public static final int DECODER_TIMEOUT_US = 10000;

    public static final int TCP_RECEIVE_PORT = 8888;
    public static final int LOCAL_SEND_PORT = 8889;
    public static final int UDP_RECEIVE_PORT = 9000;
    public static final int UDP_SEND_PORT = 9001;

    public static final byte[] startCapture = new byte[] {0x01, 0x01};
}
