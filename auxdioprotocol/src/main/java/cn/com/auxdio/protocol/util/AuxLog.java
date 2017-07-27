package cn.com.auxdio.protocol.util;

import android.util.Log;

/**
 * Created by Auxdio on 2017/3/10 0010.
 */

public class AuxLog {

    public static int  mCurrentLevel = 6;

    public static final int  INFO_LEVEL = 5;
    public static final int  ERR_LEVEL = 3;

    public static void i(String tag,String msg){
        if (mCurrentLevel >= INFO_LEVEL)
            Log.i(tag,msg);
    }

    public static void e(String tag,String msg){
        if (mCurrentLevel >= ERR_LEVEL)
            Log.e(tag,msg);
    }

    public static void e(String msg){
        e("AuxdioSDK",msg);
    }

}
