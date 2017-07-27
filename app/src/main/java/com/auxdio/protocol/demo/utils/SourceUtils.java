package com.auxdio.protocol.demo.utils;

import com.auxdio.protocol.demo.bean.SourceEntity;

import java.util.List;

/**
 * Created by wangl on 2017/3/24 0024.
 */

public class SourceUtils {

    public static String getSourceNameByID(List<SourceEntity> sourceEntities, int sourceID){
        for (SourceEntity entity : sourceEntities) {
            if (entity.getSourceID() > 0xB0){
                return getNamebyID(entity.getSourceID());
            }else if (entity.getSourceID() == sourceID)
                return entity.getSourceName();
        }
        return "";
    }

    private static String getNamebyID(int sourceID) {



        return null;
    }
}
