package cn.com.auxdio.protocol.bean;

/**
 * Created by Auxdio on 2017/3/11 0011.
 * 音源实体类
 */

public class AuxSourceEntity {
    private int sourceID;//音源ID
    private String sourceName;//音源名称

    private int playMode;//播放模式
    private int playState;//播放状态
    private String programName = "";//节目名称

    public AuxSourceEntity(int sourceID, String sourceName) {
        this.sourceID = sourceID;
        this.sourceName = sourceName;
    }



    public int getSourceID() {
        return sourceID;
    }

    public void setSourceID(int sourceID) {
        this.sourceID = sourceID;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    @Override
    public String toString() {
        return "SourceEntity{" +
                "sourceID=" + sourceID +
                ", sourceName='" + sourceName + '\'' +
                '}';
    }

    public int getPlayMode() {
        return playMode;
    }

    public void setPlayMode(int playMode) {
        this.playMode = playMode;
    }

    public int getPlayState() {
        return playState;
    }

    public void setPlayState(int playState) {
        this.playState = playState;
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }
}
