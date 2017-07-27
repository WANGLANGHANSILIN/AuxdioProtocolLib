package com.auxdio.protocol.demo.utils;

import java.util.List;
import java.util.Set;

import cn.com.auxdio.protocol.bean.AuxDeviceEntity;

/**
 * Created by wangl on 2017/3/24 0024.
 */

public class DeviceUtils {
    public static boolean isDeviceModelExist(Set<Integer> keySet, AuxDeviceEntity auxDeviceEntity){
        for (Integer integer : keySet) {
            if (integer == auxDeviceEntity.getDevModel())
                return true;
        }
        return false;
    }

    public static boolean isDeviceIPExist(List<AuxDeviceEntity> deviceEntities, AuxDeviceEntity auxDeviceEntity){
        for (AuxDeviceEntity entity : deviceEntities) {
            if (entity.getDevIP().equals(auxDeviceEntity.getDevIP())){
                return true;
            }
        }
        return false;
    }
}
