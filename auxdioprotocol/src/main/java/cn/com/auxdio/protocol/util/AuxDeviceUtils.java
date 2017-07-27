package cn.com.auxdio.protocol.util;

import java.util.List;
import java.util.Set;

import cn.com.auxdio.protocol.bean.AuxDeviceEntity;

/**
 * Created by Auxdio on 2017/3/24 0024.
 */

public class AuxDeviceUtils {
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


    public static AuxDeviceEntity getDevicebyIP(List<AuxDeviceEntity> deviceEntities, String devIP){
        for (AuxDeviceEntity entity : deviceEntities) {
            if (entity.getDevIP().equals(devIP)){
                return entity;
            }
        }
        return null;
    }
}
