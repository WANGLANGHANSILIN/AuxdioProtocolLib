package cn.com.auxdio.protocol.interfaces;

/**
 * Created by Auxdio on 2017/3/20 0020.
 * 查询网络模块监听
 */

import java.util.List;
import java.util.Map;

import cn.com.auxdio.protocol.bean.AuxNetModelEntity;

public interface AuxRequestNetModelListener {

    //网络模块列表监听
    void onNetModelList(List<AuxNetModelEntity> modelEntities);

    //关联类型监听
    interface NetModelBindTypeListener {
        void onRelevanceType(int type);
    }
    //模块绑定列表监听
    interface NetModelBindListListener {
        void onBindList(Map<Integer,AuxNetModelEntity> mapList);
    }

    //模块点播模式列表监听
    interface NetModelPlayModeListListener{
        void onPlayModeList(Map<Integer,Integer> integerMap);
    }

}
