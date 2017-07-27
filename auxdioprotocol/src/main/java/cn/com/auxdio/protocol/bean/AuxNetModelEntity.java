package cn.com.auxdio.protocol.bean;

/**
 * Created by Auxdio on 2017/3/20 0020.
 * 网络模块实体类
 */

public class AuxNetModelEntity {
    private int modelID;
    private String modelIP;
    private int workMode;
    private String modelName = "";



    public int getModelID() {
        return modelID;
    }

    public void setModelID(int modelID) {
        this.modelID = modelID;
    }

    public int getWorkMode() {
        return workMode;
    }

    public void setWorkMode(int workMode) {
        this.workMode = workMode;
    }

    public String getModelIP() {
        return modelIP;
    }

    public void setModelIP(String modelIP) {
        this.modelIP = modelIP;
    }

    @Override
    public String toString() {
        return "AuxNetModelEntity{" +
                "modelID=" + modelID +
                ", modelIP='" + modelIP + '\'' +
                ", workMode=" + workMode +
                ", modelName='" + modelName + '\'' +
                '}';
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
}
