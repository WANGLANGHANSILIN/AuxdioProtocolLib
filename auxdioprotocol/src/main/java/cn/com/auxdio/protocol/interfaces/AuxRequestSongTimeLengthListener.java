package cn.com.auxdio.protocol.interfaces;

/**
 * Created by wang l on 2017/6/8.
 */

public interface AuxRequestSongTimeLengthListener {
    void onSongTimeLength(int totalLength, int currentLength, int percent);
}
