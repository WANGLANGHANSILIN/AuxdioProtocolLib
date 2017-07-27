package cn.com.auxdio.protocol.interfaces;

/**
 * Created by Auxdio on 2017/3/21 0021.
 * 设备版本回调监听
 */

public interface AuxRequestDeviceVersionListener {
    void onDeviceVersion(String softwareVersion,String protocolVersion);
}
