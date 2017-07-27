package cn.com.auxdio.protocol.interfaces;

/**
 * Created by Auxdio on 2017/3/20 0020.
 * 电台连接状态监听
 */

public interface AuxRadioActionListener {

    interface RadioConnectListener{
        void onConnectState(int connect);
    }
}
