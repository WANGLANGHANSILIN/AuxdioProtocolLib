package cn.com.auxdio.protocol.protocol;

/**
 * Created by Auxdio on 2017/3/23 0023.
 */

public class AuxConfig {

    /**
     * 设备模型
     **/
    public static class DeciveModel {
        public static final int DEVICE_DM836 = 0x02;
        public static final int DEVICE_DM838 = 0x07;
        public static final int DEVICE_DM858 = 0x08;
        public static final int DEVICE_AM8318 = 0x06;
        public static final int DEVICE_AM8328 = 0x05;
    }


    /*
	 *USB插入拨出定义
	 */
    public static class USBState {
        public static final int USB_IN  = 0x01;//USB插入
        public static final int USB_OUT = 0x00;//USB拔出
    }

    /*
	 * 播放模式定义
	 */
    public static class PlayMode {
        public static final int PLAY_ONLY_ONE      = 0x01; //单曲播放
        public static final int PLAY_ONE_CYCLE     = 0x02; //单曲循环
        public static final int PLAY_ORDER_NOCYCLE = 0x03; //顺序播放
        public static final int PLAY_LIST_CYCLE    = 0x04; //列表循环
        public static final int PLAY_RANDOM        = 0x05; //随机播放
    }

    /*
	 *开关机状态
	 */
    public static class ChannelState {
        public static final int DEVICE_ON  = 0x01; //开机
        public static final int DEVICE_OFF = 0x00; //关机
    }

    //静音状态
    public static class MuteStatus {
        public static final int isMute  = 0x10;//静音
        public static final int notMute = 0x01; //非静音
    }

    /*
	 * 设置和查询定义
	 */
    public static class SetOrQuery  {
        public static final int REQUEST_SET   = 0x08;
        public static final int REQUEST_QUERY = 0x80;
    }

    /*
     *上一曲/频率   下一曲/频率
     */
    public static class PreOrNext {
        public static final int REQUEST_PRE  = 0x01;
        public static final int REQUEST_NEXT = 0x10;
    }

    /*
	 * 添加/删除
	 */
    public static class AddOrDel {
        public static final int REQUEST_ADD = 0x01;
        public static final int REQUEST_DEL = 0x00;
    }

    /*
	 * EQ定义
	 */
    public static class PlayEQ {
        public static final int EQ_NORMAL    = 0x01;//标准
        public static final int EQ_POP       = 0x02;//流行
        public static final int EQ_CLASSIC   = 0x03;//古典
        public static final int EQ_JAZZ      = 0x04;//爵士
        public static final int EQ_ROCK      = 0x05;//摇滚
        public static final int EQ_VOCAL     = 0x06;//人声
        public static final int EQ_METAL     = 0x07;//金属
        public static final int EQ_SENTIMENTAL    = 0x08;//伤感
        public static final int EQ_DANCE     = 0x09;//舞曲
        public static final int EQ_CUSTOM     = 0x0A;//自定义
    }

    /*
	 *节目源（音源）定义:
	 */
    public static class ProgramSource {
        public static final int PROGRAM_NONE    = 0x00;
        public static final int PROGRAM_INTERNAL    = 0x01;
        public static final int PROGRAM_FM      = 0x11;//收音机
        public static final int PROGRAM_TUNER   = 0x21;
        public static final int PROGRAM_TV      = 0x31;
        public static final int PROGRAM_DVD     = 0x41;

        public static final int PROGRAM_AUX     = 0x51;
        public static final int PROGRAM_AUX2    = 0x52;

        public static final int PROGRAM_PC      = 0x61;
        public static final int PROGRAM_IPOD    = 0x71;
        public static final int PROGRAM_MP3_USB = 0x81;
        public static final int PROGRAM_SD      = 0x91;
        public static final int PROGRAM_BLUET   = 0xA1; //蓝牙

        public static final int PROGRAM_DLNA	 = 0xB0;
        public static final int PROGRAM_DLNA1    = 0xB1;//DLNA
        public static final int PROGRAM_DLNA2    = 0xB2;
        public static final int PROGRAM_DLNA3    = 0xB3;
        public static final int PROGRAM_DLNA4    = 0xB4;
        public static final int PROGRAM_DLNA5    = 0xB5;
        public static final int PROGRAM_DLNA6    = 0xB6;
        public static final int PROGRAM_DLNA7    = 0xB7;
        public static final int PROGRAM_DLNA8    = 0xB8;
        public static final int PROGRAM_DLNA9    = 0xB9;
        public static final int PROGRAM_DLNA10   = 0xBA;
        public static final int PROGRAM_DLNA11   = 0xBB;
        public static final int PROGRAM_DLNA12   = 0xBC;

        public static final int PROGRAM_INT_RAD  = 0xC0; //网络收音机
        public static final int PROGRAM_INT_RAD1 = 0xC1; //网络收音机
        public static final int PROGRAM_INT_RAD2 = 0xC2;
        public static final int PROGRAM_INT_RAD3 = 0xC3;
        public static final int PROGRAM_INT_RAD4 = 0xC4;
        public static final int PROGRAM_INT_RAD5 = 0xC5;
        public static final int PROGRAM_INT_RAD6 = 0xC6;
        public static final int PROGRAM_INT_RAD7 = 0xC7;
        public static final int PROGRAM_INT_RAD8 = 0xC8;
        public static final int PROGRAM_INT_RAD9 = 0xC9;
        public static final int PROGRAM_INT_RAD10 = 0xCA;
        public static final int PROGRAM_INT_RAD11 = 0xCB;
        public static final int PROGRAM_INT_RAD12 = 0xCC;

        public static final int PROGRAM_NETRADIO = 0xC1;
        public static final int PROGRAM_NETMUSIC = 0xD1;

        public static final int PROGRAM_INT_AOD  = 0xD0;
        public static final int PROGRAM_INT_AOD1 = 0xD1;
        public static final int PROGRAM_INT_AOD2 = 0xD2;
        public static final int PROGRAM_INT_AOD3 = 0xD3;
        public static final int PROGRAM_INT_AOD4 = 0xD4;
        public static final int PROGRAM_INT_AOD5 = 0xD5;
        public static final int PROGRAM_INT_AOD6 = 0xD6;
        public static final int PROGRAM_INT_AOD7 = 0xD7;
        public static final int PROGRAM_INT_AOD8 = 0xD8;
        public static final int PROGRAM_INT_AOD9 = 0xD9;
        public static final int PROGRAM_INT_AOD10 = 0xDA;
        public static final int PROGRAM_INT_AOD11 = 0xDB;
        public static final int PROGRAM_INT_AOD12 = 0xDC;
    }

    public static class ResOrReqCommand {
        public static final int CMD_VOLUME_SET_QUERY      = 0x0001; //音量设置/查询
        public static final int CMD_PROSRC_SET_QUERY      = 0x0002; //音源设置/查询
        public static final int CMD_SOUNDTRACK_SET_QUERY  = 0x0003; //音效设置/查询
        public static final int CMD_PLAYMODE_SET_QUERY    = 0x0004; //播放模式设置/查询
        public static final int CMD_PLAYSTATE_SET_QUERY   = 0x0005; //播放状态设置/查询
        public static final int CMD_ONOFFSTATE_SET_QUERY  = 0x0006; //开关机状态设置/查询
        public static final int CMD_MUTESTATE_SET_QUERY   = 0x0007; //静音状态设置/查询
        public static final int CMD_PREORNEXT_REQUEST     = 0x000A; //上一曲/频率、下一曲/频率（非网络电台）
        public static final int CMD_CHANORDEVSTATE_QUERY  = 0x0010; //在线设备或所有分区当前状态
        public static final int CMD_DEVICE_VERSION_QUERY  = 0x0011; //查询设备版本
        public static final int CMD_NETWORKMODE_SET_QUERY = 0x0019; //设置/查询网络模块工作模式
        public static final int CMD_QUERY_PROGRAM         = 0x000D; //查询当前节目名称
        public static final int CMD_QUERY_CHANNELNAME     = 0x000E; //查询/设置分区别名
        public static final int CMD_QUERY_PLAYTIME    = 0x000F; //查询/设置分区别名
        public static final int CMD_DEVICE_ISONLINE       = 0xFFFB; //设备/分区是否在线
        public static final int CMD_CHANNEL_ON            = 0xFFFC; //分区上线通知
        public static final int CMD_DEVICE_ON             = 0xFFFD; //设备上线通知
        public static final int CMD_SEARCH_HOST_RESPONSE  = 0xFFFE; //搜索设备
        public static final int CMD_MUSICCONTAINER_QUERY  = 0x001B; //获取歌曲目录信息
        public static final int CMD_ADDORDEL_RADIOADDRESS = 0x0020; //增加/删除网络电台
        public static final int CMD_PLAY_NETRADIO         = 0x0021; //播放网络电台
        public static final int CMD_SOURCE_NAME			  = 0x0022; // 音源名称
        public static final int CMD_MUSICMSG_QUERY        = 0x0024; //获取指定目录下歌曲信息
        public static final int CMD_MUSICAPPIONT_PLAY     = 0x0023; //播放从列表中选定的音乐
        public static final int CMD_SRCCHANGE_BROADCAST   = 0x0025; //节目源状态改变时广播
        public static final int CMD_PLAYMODECHANGE_RES    = 0x0026; //播放模式改变
        public static final int CMD_USB_STATE_RES         = 0x0027; //USB插入拔出时广播
        public static final int CMD_NETMODE_BROADCAST     = 0x002A; //网络模块数量/工作模式改变时，设备端的主动广播
        public static final int CMD_NETMODE_NAME		  = 0x002B;	// 网络模块别名
        public static final int CMD_SD_CHANGED			  = 0x002C; // SD卡目录歌曲发生变动
        public static final int CMD_EQ_HIGH_LOW_SET_QUERY = 0x002F; // 查询/设置 高低音
        public static final int CMD_RADIO_STATE = 0x0033; // 网络电台的状态变化

        public static final int CMD_EQ_STATE = 0x0034;//音效(EQ)改变时广播
        public static final int CMD_NETPLAY_MODEL = 0x0035; //查询设置网络点播模式
        public static final int CMD_NETPLAY_MODEL_BROADCAST  = 0x0036; //点播播放模式改变主动通知
        public static final int CMD_NETPLAY_MUSIC = 0x0037; //网络点播播放歌曲

        public static final int CMD_BULE_SETTING = 0x0038; //蓝牙名称和密码设置

        public static final int CMD_CHANELMODEL_TYPE_QUERY_SET = 0x0039; //查询/设置分区模块关联类型
        public static final int CMD_CUSTOM_CHANELMODEL_ATTR_QUERY_SET = 0x003A; //查询/设置自定义分区模块关联属性

        public static final int CMD_GATE_XIAOHUA_SERAIL_NUM_QUERY_SET = 0x003B; //小华序列号查询设置
        public static final int CMD_PLAY_RADIO_DM858 = 0x003C; //播放DM858网络电台
    }


    //合成命令（在JAVA中字面整数量默认值为int,小于int时按int计数）
    public static int combineCommand(byte command1 ,byte command2){
        int command11  = (command1 << 8) & 0x0000FFFF;
        int command22  = command2 & 0x000000FF;
        return (command11 | command22);
    }

    //将命令拆分
    public static byte[] separateCommand(int command) {
        byte[] data  = new byte[2];
        data[0] = (byte) (command >>> 8);
        data[1] = (byte)(command & 0x00FF);
        return data;
    }

    public static byte	    cmdSequence	 		= 0;       	// 包序号
    public static int       packageHeaderSize  	= 9; 		// 包头长度


    /************************************************请求包部分**********************************************/

    /**
     * @param command                             请求命令
     * @param deviceId                            设备ID
     * @param deviceIdentifier                    设备识别码
     * @param groupOrChannelIdentifer             组/分区识别码
     * @param groupOrChannelId                    组/分区ID
     * @param payLoadSize                         数据负载长度
     * @param payLoad                             数据负载(如果无数据负载，数据负载可为任意值)
     * @return									     请求包
     */
    public static byte[] requestPackage(int command,int deviceId,int deviceIdentifier,
                                        int groupOrChannelIdentifer,int groupOrChannelId,int payLoadSize,byte[] payLoad) {
        byte[] requestPackage = new byte[packageHeaderSize + payLoadSize + 1];
        byte[] commandData    = separateCommand(command);//拆分命令
        requestPackage[0] =  commandData[0];
        requestPackage[1] =  commandData[1];
        requestPackage[2] = (byte)++cmdSequence;//包序号
        requestPackage[3] = (byte)(deviceId >> 4);//设备ID      前12位为设备ID，后4位为设备识别码
        requestPackage[4] = (byte)((deviceId << 4)|deviceIdentifier);//设备识别码
        requestPackage[5] = (byte)0x00;//保留位
        requestPackage[6] = getBuf6Data(groupOrChannelIdentifer);//分区标识
        requestPackage[7] = (byte)groupOrChannelId;//分区ID
        requestPackage[8] = (byte)payLoadSize;//包长
        if(payLoadSize > 0) {
            for (int i = 0; i < payLoadSize; i++) {
                requestPackage[8+i+1] = payLoad[i];
            }
        }
        requestPackage[9+payLoadSize] = getCheckSum(requestPackage, 9, payLoadSize);//当payloadsize=0时，requestPackage[9]为校验和为
        return requestPackage;
    }

    /**
     * 获取requestPackage[6]
     * @param groupOrChannelIdentifer
     * @return
     */
    public static byte getBuf6Data(int groupOrChannelIdentifer) {
        int data1 = 0x20;
        int data2 = groupOrChannelIdentifer;
        return (byte) (data1 | data2);
    }

    /**
     * 获取返回的设备ID
     * @param data1
     * @param data2
     * @return
     */
    public static int getDeveiceIDResponse(byte data1,byte data2){
        int data11 = (data1 << 4) & 0x00000FFF;
        int data22 = (data2 >> 4) & 0x0000000F;
        return data11 | data22;
    }

    /**
     * 获取校验和
     * @param data
     * @param pos
     * @param len
     * @return
     */
    public static byte getCheckSum(byte[] data,int pos, int len)
    {
        byte lrc = 0;

        lrc = data[pos];

        for (int i = pos+1; i < (pos+len); i++)
        {
            lrc = (byte)(lrc ^ data[i]);//异或
        }

        return lrc;
    }
}
