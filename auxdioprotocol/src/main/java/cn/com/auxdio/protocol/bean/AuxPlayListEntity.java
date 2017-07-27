package cn.com.auxdio.protocol.bean;

/**
 * Created by Auxdio on 2017/3/9 0009.
 * 歌曲目录实体类
 */

import java.util.ArrayList;

public class AuxPlayListEntity extends Object{

    private int contentsID;//目录ID
    private String contentsName = "";//目录名称
    private int contentsPageCount;//目录下包数量

    private ArrayList<AuxSongEntity> mMusicEntities;

    public int getContentsID() {
        return contentsID;
    }

    public void setContentsID(int contentsID) {
        this.contentsID = contentsID;
    }

    public String getContentsName() {
        return contentsName;
    }

    public String getPlayListName(){
        String name = "";
        if (contentsName != null && contentsName.length() > 0){
            if (contentsName.startsWith("/mnt/yaffs2/")){
                name = contentsName.substring(12,contentsName.length()-1);
            }else if (contentsName.startsWith("/mnt/udisk/")){
                if (contentsName.length() > 11)
                    name = contentsName.substring(11,contentsName.length()-1);
                else
                    name = "Root";
            }else
                name = contentsName;
        }
        return name;
    }

    public void setContentsName(String contentsName) {
        this.contentsName = contentsName;
    }

    public int getContentsPageCount() {
        return contentsPageCount;
    }

    public void setContentsPageCount(int contentsPageCount) {
        this.contentsPageCount = contentsPageCount;
    }

    public ArrayList<AuxSongEntity> getMusicEntities() {
        return mMusicEntities;
    }

    public void setMusicEntities(ArrayList<AuxSongEntity> musicEntities) {
        mMusicEntities = musicEntities;
    }

    @Override
        public String toString() {
            return "ContentsEntity{" +
                    "contentsID=" + contentsID +
                    ", contentsName='" + contentsName + '\'' +
                    ", contentsPageCount=" + contentsPageCount +
                    '}';
        }
    }
