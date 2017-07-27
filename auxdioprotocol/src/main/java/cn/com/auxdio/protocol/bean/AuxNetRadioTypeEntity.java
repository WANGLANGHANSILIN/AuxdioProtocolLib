package cn.com.auxdio.protocol.bean;

import java.util.List;

/**
 * Created by Auxdio on 2017/3/20 0020.
 * 电台类型实体类
 */

public class AuxNetRadioTypeEntity {
    private String radioType;
    private int radioCount;
    private String radioBelong;

    private List<AuxNetRadioEntity> mNetRadioEntities;

    public String getRadioType() {
        return radioType;
    }

    public void setRadioType(String radioType) {
        this.radioType = radioType;
    }

    public int getRadioCount() {
        return radioCount;
    }

    public void setRadioCount(int radioCount) {
        this.radioCount = radioCount;
    }

    public String getRadioBelong() {
        return radioBelong;
    }

    public void setRadioBelong(String radioBelong) {
        this.radioBelong = radioBelong;
    }

    public List<AuxNetRadioEntity> getNetRadioList() {
        return mNetRadioEntities;
    }

    public void setNetRadioList(List<AuxNetRadioEntity> netRadioEntities) {
        mNetRadioEntities = netRadioEntities;
    }

    @Override
    public String toString() {
        return "RadioTypeEntity{" +
                "radioCount=" + radioCount +
                ", radioType='" + radioType + '\'' +
                '}';
    }
}
