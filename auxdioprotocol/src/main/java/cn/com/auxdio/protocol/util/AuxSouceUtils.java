package cn.com.auxdio.protocol.util;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import cn.com.auxdio.protocol.bean.AuxRoomEntity;
import cn.com.auxdio.protocol.bean.AuxSoundEffectEntity;
import cn.com.auxdio.protocol.bean.AuxSourceEntity;
import cn.com.auxdio.protocol.net.AuxUdpUnicast;
import cn.com.auxdio.protocol.protocol.AuxConfig;

/**
 * Created by Auxdio on 2017/4/13.
 */

public class AuxSouceUtils {

    public static int get_SD_USB_Index(List<AuxSourceEntity> sourceEntities){
        int index = 0;
        for (AuxSourceEntity sourceEntity : sourceEntities) {
            if (sourceEntity.getSourceName().equals("SD/USB")){
                return index;
            }
            index++;
        }
        return -1;
    }

    public static String getSourceNameByID(List<AuxSourceEntity> sourceEntities, int sourceID){
        for (AuxSourceEntity entity : sourceEntities) {
            if (entity.getSourceID() == sourceID)
                return entity.getSourceName();
        }
        return "";
    }

    private static List<AuxSourceEntity> mAuxSourceEntities = new ArrayList<>();
    public static AuxSourceEntity getSourceEntityByID(List<AuxSourceEntity> sourceEntities, int sourceID){
        if (sourceID > 0xB0){
            if (getAuxSourceEntityByID(sourceID) == null){
                return getNewAuxSourceEntity(sourceID);
            }else{
                return getAuxSourceEntityByID(sourceID);
            }
        }else
        {
            for (AuxSourceEntity entity : sourceEntities) {
                if (entity.getSourceID() == sourceID)
                    return entity;
            }
        }
        return null;
    }

    @Nullable
    private static AuxSourceEntity getAuxSourceEntityByID(int sourceID) {
        if (mAuxSourceEntities.size() == 0){
            return getNewAuxSourceEntity(sourceID);
        }
        for (AuxSourceEntity entity : mAuxSourceEntities) {
            if (entity.getSourceID() == sourceID)
                return entity;
        }
        return null;
    }

    @NonNull
    private static AuxSourceEntity getNewAuxSourceEntity(int sourceID) {
        String sourceName = "";
        if(sourceID < 0xC0 && sourceID - 0xB0 > 0){
            sourceName = "网络音乐";
        }else if(sourceID < 0xD0 && sourceID - 0xC0 > 0){
            sourceName = "网络电台";
        }else if(sourceID - 0xD0 > 0){
            if (AuxUdpUnicast.getInstance().getControlDeviceEntity().getDevModel() == AuxConfig.DeciveModel.DEVICE_DM838)
                sourceName = "网络音乐";
            else
                sourceName = "SD/USB";
        }
        AuxSourceEntity auxSourceEntity = new AuxSourceEntity(sourceID, sourceName);
        return auxSourceEntity;
    }


    public static AuxSoundEffectEntity getSoundEffectByID(List<AuxSoundEffectEntity> auxSoundEffectEntities, int soundID){
        for (AuxSoundEffectEntity entity : auxSoundEffectEntities) {
            if (entity.getSoundID() == soundID)
                return entity;
        }
        return null;
    }


    private static AuxSourceEntity callBackCommon(int srcID){
        if (AuxUdpUnicast.getInstance() == null || AuxUdpUnicast.getInstance().getUnicastRunnable() == null)
            return null;
        List<AuxSourceEntity> sourceEntities = AuxUdpUnicast.getInstance().getUnicastRunnable().getSourceEntities();
        if (sourceEntities != null) {
            AuxSourceEntity sourceEntityByID = AuxSouceUtils.getSourceEntityByID(sourceEntities, srcID);
            if (sourceEntityByID != null){
                return sourceEntityByID;
            }
        }
        return null;
    }

    public static void callBackPlayModel(int modelID, int modeValue) {
        if (AuxUdpUnicast.getInstance().getUnicastRunnable() == null)
            return;
        List<AuxRoomEntity> channelEntities = AuxUdpUnicast.getInstance().getUnicastRunnable().getChannelEntities();
        if (channelEntities == null)
            return;
        for (AuxRoomEntity channelEntity : channelEntities) {
            if(channelEntity.getSrcID() > 0xD0 && (channelEntity.getSrcID() - 0xD0) == modelID){
                AuxLog.i("callBackPlayModel",""+channelEntity.toString());
                AuxSouceUtils.callBackPlayMode(channelEntity.getSrcID(),modeValue);
            }
        }
    }

    public static void callBackPlayMode(int srcID,int modeValue){
        if (srcID == 0x01 || srcID == 0x81 || srcID == 0x91 || srcID > 0xD0){
            AuxSourceEntity auxSourceEntity = callBackCommon(srcID);
            AuxLog.i("callBackPlayMode","srcID:"+srcID+"   modeValue:"+modeValue+"   auxSourceEntity != null  "+(auxSourceEntity != null));
            if (auxSourceEntity != null){
                AuxRoomEntity[] auxRoomEntities = AuxUdpUnicast.getInstance().getControlRoomEntities();
                if (auxRoomEntities == null) {
                    AuxLog.e("callBackPlayMode","ControlRoom is null");
                    return;
                }
                if (auxRoomEntities.length > 0){
                    for (int i = 0; i < auxRoomEntities.length; i++) {
                        if (auxRoomEntities[i].getSrcID() == auxSourceEntity.getSourceID()){
                            auxSourceEntity.setPlayMode(modeValue);
                            replaceSourceEntity(auxSourceEntity);
                            if (AuxUdpUnicast.getInstance().getPlayModeListener() != null)
                                AuxUdpUnicast.getInstance().getPlayModeListener().onPlayModel(auxSourceEntity,modeValue);
                        }
                    }
                }
            }
        }
    }

    public static void callBackPlayState(int srcID,int stateValue){
        if (srcID == 0x01 || srcID == 0x81 || srcID == 0x91 || srcID == 0xA1 || srcID > 0xD0 || (srcID > 0xB0 || srcID < 0xC0)){
            AuxSourceEntity auxSourceEntity = callBackCommon(srcID);
            if (auxSourceEntity != null){
                AuxLog.i("callBackPlayState","callBackPlayState: "+auxSourceEntity.toString()+",stateValue:"+stateValue);
                if (AuxUdpUnicast.getInstance().getPlayStateListener() != null) {
                    auxSourceEntity.setPlayState(stateValue);
                    replaceSourceEntity(auxSourceEntity);
                    AuxUdpUnicast.getInstance().getPlayStateListener().onPlayState(auxSourceEntity,stateValue);
                }
            }
        }
    }

    public static void callBackProgramName(int srcID,String newProgramName){
        AuxSourceEntity auxSourceEntity = callBackCommon(srcID);
        if (auxSourceEntity != null){
            if (newProgramName == null || auxSourceEntity.getProgramName() == null)
                return;
            if (AuxUdpUnicast.getInstance().getProgramNameListener() != null) {
                auxSourceEntity.setProgramName(newProgramName);
                replaceSourceEntity(auxSourceEntity);
                AuxUdpUnicast.getInstance().getProgramNameListener().onProgramName(auxSourceEntity,newProgramName);
            }
        }
    }

    private static int findAuxSourceEntity(AuxSourceEntity auxSourceEntity){
        List<AuxSourceEntity> sourceEntities = AuxUdpUnicast.getInstance().getUnicastRunnable().getSourceEntities();
        if (sourceEntities != null){
            for (int i = 0; i < sourceEntities.size(); i++) {
                if (auxSourceEntity.getSourceID() == sourceEntities.get(i).getSourceID())
                    return i;
            }
        }
        return -1;
    }
    private static void replaceSourceEntity(AuxSourceEntity auxSourceEntity){
        int auxSourceEntityIndex = findAuxSourceEntity(auxSourceEntity);
        if (auxSourceEntityIndex == -1)
            return;
        List<AuxSourceEntity> sourceEntities = AuxUdpUnicast.getInstance().getUnicastRunnable().getSourceEntities();
        if (sourceEntities != null){
            sourceEntities.set(auxSourceEntityIndex,auxSourceEntity);
        }
        AuxUdpUnicast.getInstance().getUnicastRunnable().setSourceEntities(sourceEntities);
    }




}
