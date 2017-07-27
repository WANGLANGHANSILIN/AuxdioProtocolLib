package cn.com.auxdio.protocol.interfaces;

import java.util.List;
import java.util.Map;

import cn.com.auxdio.protocol.bean.AuxDeviceEntity;

/**
 * Created by Auxdio on 2017/3/8 0008.
 * 设备版本监听
 */

public interface AuxSreachDeviceListener {

    void onSreachDevice(Map<Integer, List<AuxDeviceEntity>> auxDeviceEntity);

}
