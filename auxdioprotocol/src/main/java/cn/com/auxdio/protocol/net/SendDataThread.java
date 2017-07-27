package cn.com.auxdio.protocol.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import cn.com.auxdio.protocol.util.AuxByteToStringUtils;
import cn.com.auxdio.protocol.util.AuxLog;

/**
 * Created by Auxdio on 2017/4/26.
 */
 class SendDataThread extends Thread {
    DatagramSocket mUnicastSocket;
    private String devIP;
    private byte[] daBytes;
    private int len;
    public SendDataThread(DatagramSocket datagramSocket) {
        this.mUnicastSocket = datagramSocket;
    }

    public void sendDate(String devIP, byte[] data, int len){
        this.devIP = devIP;
        this.daBytes = data;
        this.len = len;
        run();
    }

    @Override
    public void run() {
        try {
            sendData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendData() throws IOException {
        if (devIP != null) {
            DatagramPacket datagramPacket = new DatagramPacket(daBytes, len, InetAddress.getByName(devIP), AuxNetConstant.UICAST_PORT);
            if (mUnicastSocket != null) {
                mUnicastSocket.send(datagramPacket);
                AuxLog.i("UnicastRunnable", "sendData:" + AuxByteToStringUtils.bytesToHexString(daBytes, len));
            } else
                AuxLog.e("UnicastRunnable", "mUnicastSocket != null   " + (mUnicastSocket != null));
        }
    }
}
