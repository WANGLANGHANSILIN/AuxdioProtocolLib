package cn.com.auxdio.protocol.util;

/**
 * Created by Auxdio on 2017/3/8 0008.
 */

public class AuxByteToStringUtils {

    public static String bytesToHexString(byte[] src,int len)
    {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || len <= 0)
        {
            return null;
        }
        for (int i = 0; i < len; i++)
        {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() == 1) {
                hv = "0"+hv;
            }
            String s="0x"+hv+"  ";
            stringBuilder.append(s);
        }
        return stringBuilder.toString();
    }
}
