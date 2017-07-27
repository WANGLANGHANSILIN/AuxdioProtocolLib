package com.auxdio.protocol.demo.bean;

import cn.com.auxdio.protocol.bean.AuxSongEntity;

/**
 * Created by wangl on 2017/3/29 0029.
 */

public class SongEntity extends AuxSongEntity {
    private boolean isChecked;

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
