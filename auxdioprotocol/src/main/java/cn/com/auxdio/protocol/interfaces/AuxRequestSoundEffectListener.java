package cn.com.auxdio.protocol.interfaces;

import java.util.List;

import cn.com.auxdio.protocol.bean.AuxSoundEffectEntity;

/**
 * Created by Auxdio on 2017/3/20 0020.
 * 请求音效列表
 */

public interface AuxRequestSoundEffectListener {
    //查询设备音效列表
    void OnSoundEffetList(List<AuxSoundEffectEntity> auxSoundEffectEntities);
}
