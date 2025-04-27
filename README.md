# ScrcpyServer
这是投屏服务端程序，通过开启tcp套接字等待客户端连接，然后在此应用可以点击开始捕获屏幕，随后会通过输出流将屏幕数据传输到客户端显示

项目目前状态（2025/04/17）：
目前项目 (main分支) 存在一个资源释放的问题：
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

分支说明：
main:                      Video数据传输都采用Tcp,项目基本功能已具备
further-main:              添加CS通信并且采用Udp,Video数据传输采用Tcp,反转CS部分逻辑
further-main-2.0:          Udp跨网段通信失败，切换为通信方式为Tcp
further-main-2.1:          Wifi网络环境下视频传输丢包率较高导致tcp传输队头阻塞,服务端效果不佳,切换Video数据传输方式为Udp