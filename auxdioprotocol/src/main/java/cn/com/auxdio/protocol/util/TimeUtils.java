package cn.com.auxdio.protocol.util;

import android.util.Log;

/**
 * Created by wang l on 2017/6/8.
 */

public class TimeUtils {
    public static int getSongCurrentTime(byte[] data){
        return getSongTime(data, 11);
    }

    public static int getSongTotleTime(byte[] data){
        return getSongTime(data, 9);
    }

    private static int getSongTime(byte[] data, int index){
        int i = data[index++] & 0xFF;
        int i1 = data[index] & 0xFF;
        int i2 = (i << 8 ) + i1;
        Log.e("getPercent", "getPercent:"+i+","+i1+","+i2);
        return i2;
    }

    public static byte[] getSongTimeByte(int dataTimeLength){
        int i = dataTimeLength >> 8;
        int i1 = dataTimeLength - i;
        return new byte[]{(byte) i, (byte) i1};
        //sendData:0x00  0x0f
    }
}
