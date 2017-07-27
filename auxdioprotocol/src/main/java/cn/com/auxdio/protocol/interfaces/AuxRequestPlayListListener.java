package cn.com.auxdio.protocol.interfaces;

import java.util.List;

import cn.com.auxdio.protocol.bean.AuxPlayListEntity;

/**
 * Created by Auxdio on 2017/3/10 0010.
 * 播放列表回调
 */

public interface AuxRequestPlayListListener {
    void onMusicList(String devIP, List<AuxPlayListEntity> contentsEntities);
}
