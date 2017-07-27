package cn.com.auxdio.protocol.interfaces;

/**
 * Created by Auxdio on 2017/3/17 0017.
 * 控制操作监听器
 */

import cn.com.auxdio.protocol.bean.AuxRoomEntity;
import cn.com.auxdio.protocol.bean.AuxSoundEffectEntity;
import cn.com.auxdio.protocol.bean.AuxSourceEntity;

public interface AuxControlActionListener {

    /**
     * 音量监听
     */
    interface ControlVolumeListener{
        void onVolume(AuxRoomEntity auxRoomEntity,int volumuteValue);
    }

    /**
     * 播放状态监听
     */
    interface ControlPlayStateListener{
        void onPlayState(AuxSourceEntity sourceEntity,int playStateValue);
    }

    /**
     * 播放模式监听
     */
    interface ControlPlayModeListener {
        void onPlayModel(AuxSourceEntity sourceEntity,int playModelValue);
    }

    /**
     * 静音状态监听
     */
    interface ControlMuteStateListener{
        void onMuteState(AuxRoomEntity auxRoomEntity,boolean ismute);
    }

    /**
     * 音源ID监听
     */
    interface ControlSourceEntityListener {
        void onSourceEntity(AuxRoomEntity auxRoomEntity, AuxSourceEntity sourceEntity);
    }

    /**
     * 节目名称监听
     */
    interface ControlProgramNameListener{
        void onProgramName(AuxSourceEntity sourceEntity,String programName);
    }

    /**
     * 音效监听
     */
    interface SoundEffectListener {
        //查询当前房间音效
        void onCurrentSoundEffect(AuxRoomEntity auxRoomEntity, AuxSoundEffectEntity soundEffectEntity);
    }
}
