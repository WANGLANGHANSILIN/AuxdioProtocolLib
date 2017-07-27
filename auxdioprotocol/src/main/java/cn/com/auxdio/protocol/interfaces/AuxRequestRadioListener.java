package cn.com.auxdio.protocol.interfaces;

import java.util.List;

import cn.com.auxdio.protocol.bean.AuxNetRadioTypeEntity;

/**
 * Created by Auxdio on 2017/3/20 0020.
 * 请求电台数据
 */

public interface AuxRequestRadioListener {
    void onRadioList(List<AuxNetRadioTypeEntity> netRadioEntities);
}
