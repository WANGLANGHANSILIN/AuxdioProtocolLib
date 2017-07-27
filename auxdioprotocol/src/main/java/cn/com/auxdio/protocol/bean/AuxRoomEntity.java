package cn.com.auxdio.protocol.bean;

/**
 * Created by Auxdio on 2017/3/9 0009.
 * 分区实体类
 */

public class AuxRoomEntity {
    private String roomName;//分区名称
    private int RoomID;//分区ID
    private String roomIP;//房间IP

    private int srcID;//音源ID
    private int volumeID;//音量
    private int oNOffState ;//开关机状态
    private int highPitch;//高音
    private int lowPitch ;//低音

    private String roomSrcName = "";

    private long timeOut = -1;//超时

    public AuxRoomEntity() {
    }

    public AuxRoomEntity(String roomName, int RoomID, String roomIP, int srcID, int volumeID, int oNOffState, int highPitch, int lowPitch,String roomSrcName) {
        this.roomName = roomName;
        this.RoomID = RoomID;
        this.roomIP = roomIP;
        this.srcID = srcID;
        this.volumeID = volumeID;
        this.oNOffState = oNOffState;
        this.highPitch = highPitch;
        this.lowPitch = lowPitch;
        this.roomSrcName = roomSrcName;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public int getRoomID() {
        return RoomID;
    }

    public void setRoomID(int roomID) {
        this.RoomID = roomID;
    }

    public int getSrcID() {
        return srcID;
    }

    public void setSrcID(int srcID) {
        this.srcID = srcID;
    }

    public int getVolumeID() {
        return volumeID;
    }

    public void setVolumeID(int volumeID) {
        this.volumeID = volumeID;
    }

    public int getoNOffState() {
        return oNOffState;
    }

    public void setoNOffState(int oNOffState) {
        this.oNOffState = oNOffState;
    }

    public int getHighPitch() {
        return highPitch;
    }

    public void setHighPitch(int highPitch) {
        this.highPitch = highPitch;
    }

    public int getLowPitch() {
        return lowPitch;
    }

    public void setLowPitch(int lowPitch) {
        this.lowPitch = lowPitch;
    }

    public String getRoomIP() {
        return roomIP;
    }

    public void setRoomIP(String roomIP) {
        this.roomIP = roomIP;
    }

    public String getRoomSrcName() {
        return roomSrcName;
    }

    public void setRoomSrcName(String roomSrcName) {
        this.roomSrcName = roomSrcName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AuxRoomEntity that = (AuxRoomEntity) o;

        if (RoomID != that.RoomID) return false;
        if (!roomName.equals(that.roomName)) return false;
        return roomIP.equals(that.roomIP);

    }

    @Override
    public int hashCode() {
        int result = roomName.hashCode();
        result = 31 * result + RoomID;
        result = 31 * result + roomIP.hashCode();
        return result;
    }

    public long getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(long timeOut) {
        this.timeOut = timeOut;
    }

    @Override
    public String toString() {
        return "AuxRoomEntity{" +
                "roomName='" + roomName + '\'' +
                ", RoomID=" + RoomID +
                ", roomIP='" + roomIP + '\'' +
                ", srcID=" + srcID +
                ", volumeID=" + volumeID +
                ", oNOffState=" + oNOffState +
                ", highPitch=" + highPitch +
                ", lowPitch=" + lowPitch +
                ", roomSrcName='" + roomSrcName + '\'' +
                ", timeOut=" + timeOut +
                '}';
    }
}
