package cn.com.auxdio.protocol.bean;

/**
 * Created by 歌曲实体类 on 2017/3/17 0017.
 * 音效实体类
 */

public class AuxSoundEffectEntity {
    private int soundID;//音效ID
    private String soundName;//音效名称

    public AuxSoundEffectEntity(int soundID, String soundName) {
        this.soundID = soundID;
        this.soundName = soundName;
    }

    public int getSoundID() {
        return soundID;
    }

    public void setSoundID(int soundID) {
        this.soundID = soundID;
    }

    public String getSoundName() {
        return soundName;
    }

    public void setSoundName(String soundName) {
        this.soundName = soundName;
    }
}
