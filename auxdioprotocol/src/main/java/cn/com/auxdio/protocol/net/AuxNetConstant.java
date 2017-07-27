package cn.com.auxdio.protocol.net;

/**
 * Created by Auxdio on 2017/3/8 0008.
 * 网络参数配置
 */

class AuxNetConstant {

    /*广播端口*/
    protected static final int BROADCAST_PORT = 40189;

    /*单播端口*/
    protected static final int UICAST_PORT = 40188;

    /*广播地址*/
    protected static final String BROADCAST_ADRESS = "255.255.255.255";

    /*超时时间*/
    protected static final int TIME_OUT = 8000;

    /*最大缓存*/
    protected static final int MAX_BUFFER = 1024;


    /*服务器中Radio地址*/
    protected static final String RADIO_URL = "http://auxdio.senbaudio.com:65500/update/radio.xml";
}
