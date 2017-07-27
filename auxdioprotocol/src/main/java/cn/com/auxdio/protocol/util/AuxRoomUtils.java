package cn.com.auxdio.protocol.util;

import java.util.List;

import cn.com.auxdio.protocol.bean.AuxRoomEntity;

/**
 * Created by Auxdio on 2017/3/9 0009.
 */

public class AuxRoomUtils {

    //根据房间ID，获取房间（针对AM8318和AM8328）
    public static AuxRoomEntity getChannnelByID(List<AuxRoomEntity> roomEntities, int roomID){
        for (AuxRoomEntity auxChannelEntity : roomEntities) {
            if (auxChannelEntity.getRoomID() == roomID){
                return auxChannelEntity;
            }
        }
        return null;
    }

    //根据房间IP，获取房间（针对DM838和DM836II）
    public static AuxRoomEntity getChannnelByIP(List<AuxRoomEntity> roomEntities, String roomIP){
        for (AuxRoomEntity auxChannelEntity : roomEntities) {
            if (auxChannelEntity.getRoomIP().equals(roomIP)){
                return auxChannelEntity;
            }
        }
        return null;
    }

    //根据房间ID，获取房间（针对AM8318和AM8328）,特别注意：不能用getChannnelIndexByIP()来获取，因为AM8318和AM8328的每个房间IP都是一样的
    public static int getChannnelIndexByID(List<? extends AuxRoomEntity> roomEntities, int roomID){

        for (int i = 0; i < roomEntities.size(); i++) {
            if (roomEntities.get(i).getRoomID() == roomID)
                return i;
        }
        return -1;
    }

    //根据房间IP，获取房间（针对DM838和DM836II）
    public static int getChannnelIndexByIP(List<? extends AuxRoomEntity> roomEntities, String roomIP){
        for (int i = 0; i < roomEntities.size(); i++) {
            if (roomEntities.get(i).getRoomIP().equals(roomIP))
                return i;
        }
        return -1;
    }


    public static AuxRoomEntity getChannnelIndexByIP(AuxRoomEntity[] roomEntities,String roomIP){
        for (int i = 0; i < roomEntities.length; i++) {
            if(roomEntities[i].getRoomIP().equals(roomIP)){
                return roomEntities[i];
            }
        }
        return null;
    }

    public static AuxRoomEntity getChannnelIndexByID(AuxRoomEntity[] roomEntities,int roomID){
        for (int i = 0; i < roomEntities.length; i++) {
            if(roomEntities[i].getRoomID() == roomID){
                return roomEntities[i];
            }
        }
        return null;
    }

    //控制房间是否相同
    public static boolean isControlRoomSame(AuxRoomEntity[] roomEntities){
        if (roomEntities.length <= 0)
            return false;
        else if (roomEntities.length == 1)
            return true;
        else {
            for (int i = 1; i < roomEntities.length; i++) {
                if (roomEntities[i].getSrcID() != roomEntities[i].getSrcID()) {
                    return false;
                }
            }
            return true;
        }
    }

}
