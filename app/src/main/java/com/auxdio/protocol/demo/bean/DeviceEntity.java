package com.auxdio.protocol.demo.bean;

import cn.com.auxdio.protocol.bean.AuxDeviceEntity;

/**
 * Created by wangl on 2017/3/14 0014.
 */

public class DeviceEntity extends AuxDeviceEntity {

    public static DeviceEntity CastToDeviceEntity(AuxDeviceEntity entity){
        DeviceEntity deviceEntity = new DeviceEntity();
        deviceEntity.setDevIP(entity.getDevIP());
        deviceEntity.setDevModel(entity.getDevModel());
        deviceEntity.setDevName(entity.getDevName());
        deviceEntity.setDevID(entity.getDevID());
        deviceEntity.setDevMAC(entity.getDevMAC());
        deviceEntity.setDevZoneOrGroup(entity.getDevZoneOrGroup());
        return deviceEntity;
    }

    private boolean isChecked;

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
