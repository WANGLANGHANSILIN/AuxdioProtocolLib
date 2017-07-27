package cn.com.auxdio.protocol.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cn.com.auxdio.protocol.bean.AuxNetModelEntity;
import cn.com.auxdio.protocol.bean.AuxRoomEntity;
import cn.com.auxdio.protocol.net.AuxUdpUnicast;

/**
 * Created by Auxdio on 2017/4/12.
 */

public class AuxNetModelUtils {
    //通过模块ID获取网络模块
    public static AuxNetModelEntity getNetModelbyID(List<AuxNetModelEntity> netModelEntities,int modelID){
        for (AuxNetModelEntity netModelEntity : netModelEntities) {
            if (netModelEntity.getModelID() == modelID)
                return netModelEntity;
        }
        return null;
    }

    //获取
    public static  List<AuxNetModelEntity> isCheckAuxNetModelListExist(AuxRoomEntity[] roomEntities) {
        if (roomEntities == null || roomEntities.length <= 0) {
            AuxLog.e("控制的房间为空");
            return null;
        }
        if (AuxUdpUnicast.getInstance().getUnicastRunnable() == null)
            return null;

        Map<Integer, AuxNetModelEntity> netModelEntityMap = AuxUdpUnicast.getInstance().getUnicastRunnable().getNetModelEntityMap();
        if (netModelEntityMap == null) {
            AuxLog.e("未获取房间与模块绑定的列表");
            return null;
        }

        List<AuxNetModelEntity> auxNetModelEntities = new ArrayList<>();
        for (int i = 0; i < roomEntities.length; i++) {
            AuxNetModelEntity auxNetModelEntity = netModelEntityMap.get(roomEntities[i].getRoomID());
            if (auxNetModelEntity != null)
                auxNetModelEntities.add(auxNetModelEntity);
        }
        return auxNetModelEntities;
    }
}
