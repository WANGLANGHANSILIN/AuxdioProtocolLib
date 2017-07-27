package cn.com.auxdio.protocol.bean;

/**
 * Created by Auxdio on 2017/3/20 0020.
 * 网络电台实体类
 */

public class AuxNetRadioEntity {

    private String radioName;
    private String radioAddress;

    public String getRadioName() {
        return radioName;
    }

    public void setRadioName(String radioName) {
        this.radioName = radioName;
    }

    public String getRadioAddress() {
        return radioAddress;
    }

    public void setRadioAddress(String radioAddress) {
        this.radioAddress = radioAddress;
    }

    @Override
    public String toString() {
        return "NetRadioEntity{" +
                "radioName='" + radioName + '\'' +
                '}';
    }
}
