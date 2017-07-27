package com.auxdio.protocol.demo.bean;

import cn.com.auxdio.protocol.bean.AuxSourceEntity;

/**
 * Created by wangl on 2017/3/23 0023.
 */

public class SourceEntity extends AuxSourceEntity {

    public SourceEntity(int sourceID, String sourceName) {
        super(sourceID, sourceName);
    }
    private boolean isChecked;


    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public static SourceEntity conVerter(AuxSourceEntity entity){
        return new SourceEntity(entity.getSourceID(),entity.getSourceName());
    }
}
