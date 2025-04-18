package com.example.scrcpyserver.connection;

public class UdpHelper {

    public static String clientIP;
    public static int clientPort;
    private UdpReceiveThread udpReceiveThread;
    private UdpSendThread udpSendThread;

    public void init() {
        udpReceiveThread = new UdpReceiveThread();
        udpReceiveThread.start();
        udpSendThread = new UdpSendThread();
        udpSendThread.start();
    }

    public void sendData(byte[] data) {
        udpSendThread.sendUdpData(data);
    }

    public static void saveClientInfo(String ip, int port) {
        clientIP = ip;
        clientPort = port;
    }

}
