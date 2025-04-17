# ScrcpyServer
这是投屏服务端程序，通过开启tcp套接字等待客户端连接，然后在此应用可以点击开始捕获屏幕，随后会通过输出流将屏幕数据传输到客户端显示

项目目前状态（2025/04/17）：
目前项目存在一个资源释放的问题：
private void sendEncodedData(int index, MediaCodec.BufferInfo info) {
        ByteBuffer buffer = encoder.getOutputBuffer(index);
        if (buffer == null) return;
        byte[] packet = new byte[info.size];
        buffer.get(packet);
        try {
            dos.writeInt(packet.length);
            dos.write(packet);
            dos.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            encoder.releaseOutputBuffer(index, false);
        }
    }
这段代码由于stopServer或者stopCapture操作会释放掉encoder，低概率导致encoder.getOutputBuffer(index)和encoder.releaseOutputBuffer(index, false)会出现
IllegalStateException进而服务端闪退，后续开发需要继续处理这个资源释放异常，或者捕获异常忽略等操作