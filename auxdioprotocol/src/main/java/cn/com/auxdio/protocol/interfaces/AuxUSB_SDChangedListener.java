package cn.com.auxdio.protocol.interfaces;

import cn.com.auxdio.protocol.bean.AuxSoundEffectEntity;

/**
 * Created by Auxdio on 2017/3/13 0013.
 * usb/sd 改变监听
 */

public interface AuxUSB_SDChangedListener {

    /*
    /**
     * 节目源状态改变时
     * @param auxProgramStateEntity
     */
//    void onProgramStateChanged(AuxProgramStateEntity auxProgramStateEntity);

    /**
     * 播放模式改变
     */
//    void onPlayModeChanged(String roomIP, AuxSourceEntity sourceEntity, int palyModel);



    /**
     * USB插入或拔出
     */
    void onUSBChanged(boolean isInsert);

    //SD卡改变
    void onSDChanged(String sdChanged);

    /**
     * 音效改变监听
     */
    interface SoundEffectChangedListener {
        void onSoundEffectChanged(String roomIP, AuxSoundEffectEntity soundEffectEntity);
    }

}
