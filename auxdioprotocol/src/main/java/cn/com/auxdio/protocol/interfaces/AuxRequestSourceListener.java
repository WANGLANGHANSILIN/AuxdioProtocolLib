package cn.com.auxdio.protocol.interfaces;

import java.util.List;

import cn.com.auxdio.protocol.bean.AuxSourceEntity;

/**
 * Created by Auxdio on 2017/3/17 0017.
 * 请求音源列表监听
 */

public interface AuxRequestSourceListener {
    void onSourceList(String hostName, List<AuxSourceEntity> sourceEntities);
}
