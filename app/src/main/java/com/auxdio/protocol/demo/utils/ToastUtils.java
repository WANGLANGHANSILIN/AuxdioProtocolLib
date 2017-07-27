package com.auxdio.protocol.demo.utils;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by wangl on 2017/3/22 0022.
 */

public class ToastUtils {
    private static Toast toast = null;
    public static void showToast(Context context,String msg){
        if (toast == null)
            toast = Toast.makeText(context,msg,Toast.LENGTH_SHORT);

        toast.setText(msg);
        toast.show();
    }
}
