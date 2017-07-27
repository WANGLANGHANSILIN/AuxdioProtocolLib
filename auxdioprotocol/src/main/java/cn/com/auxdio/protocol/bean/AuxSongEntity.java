package cn.com.auxdio.protocol.bean;

/**
 * Created by Auxdio on 2017/3/9 0009.
 * 歌曲实体类
 */

public class AuxSongEntity extends Object{
    private String SongName;//歌名
    private int contentID;//目录ID

    private String songTag;//歌曲标识（DM838）有效

    public String getSongName() {
        return SongName;
    }

    public void setSongName(String songName) {
        this.SongName = songName;
    }

    public int getContentID() {
        return contentID;
    }

    public void setContentID(int contentID) {
        this.contentID = contentID;
    }

    public String getSongTag() {
        return songTag;
    }

    public void setSongTag(String songTag) {
        this.songTag = songTag;
    }

    @Override
    public String toString() {
        return "MusicEntity{" +
                "SongName='" + SongName + '\'' +
                ", contentID=" + contentID +
                '}';
    }
}
