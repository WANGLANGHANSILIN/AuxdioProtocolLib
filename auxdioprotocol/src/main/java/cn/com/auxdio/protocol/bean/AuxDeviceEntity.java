package cn.com.auxdio.protocol.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Auxdio on 2017/3/8 0008.
 * Auxdio设备实体类
 */
public class AuxDeviceEntity implements Parcelable{

    private String devIP; //设备IP
    private String devMAC; //设备MAC
    private int devID;//设备识别码
    private int devModel;//设备类别（模式）
    private String devName;//设备名称
    private int devZoneOrGroup;//组

    public int getDevZoneOrGroup() {
        return devZoneOrGroup;
    }

    public void setDevZoneOrGroup(int devZoneOrGroup) {
        this.devZoneOrGroup = devZoneOrGroup;
    }

    public AuxDeviceEntity() {
    }

    protected AuxDeviceEntity(Parcel in) {
        devIP = in.readString();
        devMAC = in.readString();
        devID = in.readInt();
        devModel = in.readInt();
    }

    public static final Creator<AuxDeviceEntity> CREATOR = new Creator<AuxDeviceEntity>() {
        @Override
        public AuxDeviceEntity createFromParcel(Parcel in) {
            return new AuxDeviceEntity(in);
        }

        @Override
        public AuxDeviceEntity[] newArray(int size) {
            return new AuxDeviceEntity[size];
        }
    };

    public String getDevName() {
        return devName;
    }

    public void setDevName(String devName) {
        this.devName = devName;
    }

    public String getDevIP() {
        return devIP;
    }

    public void setDevIP(String devIP) {
        this.devIP = devIP;
    }

    public String getDevMAC() {
        return devMAC;
    }

    public void setDevMAC(String devMAC) {
        this.devMAC = devMAC;
    }

    public int getDevID() {
        return devID;
    }

    public void setDevID(int devID) {
        this.devID = devID;
    }

    public int getDevModel() {
        return devModel;
    }

    public void setDevModel(int devModel) {
        this.devModel = devModel;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(devIP);
        dest.writeString(devMAC);
        dest.writeInt(devID);
        dest.writeInt(devModel);
    }

    @Override
    public String toString() {
        return "AuxDeviceEntity{" +
                "devIP='" + devIP + '\'' +
                ", devMAC='" + devMAC + '\'' +
                ", devName='" + devName + '\'' +
                '}';
    }
}
