package cn.com.auxdio.protocol.util;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.com.auxdio.protocol.bean.AuxPlayListEntity;

/**
 * Created by Auxdio on 2017/3/10 0010.
 */

public class AuxPlayListUtils {

    public static AuxPlayListEntity getContentByID(int contentID, List<AuxPlayListEntity> contentsEntities){
        if (contentsEntities == null)
            return null;
        for (AuxPlayListEntity entity : contentsEntities) {
            if (entity.getContentsID() == contentID){
                return entity;
            }
        }
        return null;
    }

    public static List<AuxPlayListEntity> containsKey(String hostName, Map<String, List<AuxPlayListEntity>> contentsEntities) {
        if (contentsEntities != null && hostName != null) {
            Iterator<Map.Entry<String, List<AuxPlayListEntity>>> iterator = contentsEntities.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<String, List<AuxPlayListEntity>> entry = iterator.next();
                if (hostName.equals(entry.getKey()))
                    return entry.getValue();

            }
        }
        return null;
    }
}
