package cn.com.auxdio.protocol.interfaces;

import cn.com.auxdio.protocol.bean.AuxRoomEntity;

/**
 * Created by Auxdio on 2017/3/9 0009.
 * 分区改变监听
 */

public interface AuxRoomStateChangedListener {

    //房间上线、上线房间改变
    void onRoomChange(AuxRoomEntity auxroomEntity);

    //房间下线
    void OnRoomOffLine(AuxRoomEntity auxroomEntity);

    /**
     * 分区开关机监听
     */
    interface RoomOnOffListener {
        void onOnOffState(boolean state);
    }
}
