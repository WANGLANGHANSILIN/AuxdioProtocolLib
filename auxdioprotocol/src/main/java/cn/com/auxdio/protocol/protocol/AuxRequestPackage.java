package cn.com.auxdio.protocol.protocol;



import java.io.IOException;

import cn.com.auxdio.protocol.bean.AuxDeviceEntity;
import cn.com.auxdio.protocol.bean.AuxPlayListEntity;
import cn.com.auxdio.protocol.bean.AuxRoomEntity;
import cn.com.auxdio.protocol.net.AuxUdpBroadcast;
import cn.com.auxdio.protocol.net.AuxUdpUnicast;
import cn.com.auxdio.protocol.net.BroadCastRunnable;
import cn.com.auxdio.protocol.net.UnicastRunnable;
import cn.com.auxdio.protocol.util.AuxLog;


/**
 * Created by Auxdio on 2017/3/8 0008.
 */

public class AuxRequestPackage {

    private UnicastRunnable sUnicastRunnable;
    private BroadCastRunnable sBroadCastRunnable;
    private AuxDeviceEntity deviceEntity;

    private static AuxRequestPackage instance;

    public static AuxRequestPackage getInstance() {
        if (instance == null) {
            synchronized (AuxRequestPackage.class) {
                if (instance == null) {
                    instance = new AuxRequestPackage();
                }
            }
        }
        return instance;
    }

    private AuxRequestPackage() {
        sBroadCastRunnable = AuxUdpBroadcast.getInstace().getBroadCastRunnable();
        sUnicastRunnable = AuxUdpUnicast.getInstance().getUnicastRunnable();
    }

    //搜索设备
    public void searchDevice() throws IOException {
        byte[] searchDeviceData = AuxConfig.requestPackage(AuxConfig.ResOrReqCommand.CMD_SEARCH_HOST_RESPONSE, 0xFFF, 0x00, 0x00, 0x00, 0x00, null);
        if (sBroadCastRunnable != null){
            AuxLog.e("searchDevice","searchDevice...");
            sBroadCastRunnable.sendDataToDevice(searchDeviceData, searchDeviceData.length);
        }else{
            AuxLog.e("searchDevice","sBroadCastRunnable == null   "+(sBroadCastRunnable == null));
        }
    }

    //查询全部分区的状态
    public void queryChannnelState(String hostName) throws IOException {
        sendDataToDeviceByIP(hostName, AuxConfig.ResOrReqCommand.CMD_CHANORDEVSTATE_QUERY, new byte[0]);
    }

    //查询设备目录
    public void queryContainer(String hostName) throws IOException {
        sendDataToDeviceByIP(hostName,AuxConfig.ResOrReqCommand.CMD_MUSICCONTAINER_QUERY,new byte[0]);
    }

    //查询设备指定目录下的歌曲
    public synchronized void queryMusic(String hostName,AuxPlayListEntity containeEntity) throws IOException {
            byte[] containerNameByte = containeEntity.getContentsName().getBytes("gb2312");
            int    containerNameLen  = containerNameByte.length;
            byte[] payLoad           = new byte[3+containerNameLen];
            int contentsPageCount = containeEntity.getContentsPageCount();
            AuxLog.i("queryMusic","contentsPageCount:"+containeEntity.toString());
            if (contentsPageCount > 0) {
                for (int i = 0; i < contentsPageCount; i++) {
                    byte[] payLoadPart1 = {(byte) containeEntity.getContentsID(), (byte) (i + 1), (byte) containerNameLen};
                    System.arraycopy(payLoadPart1, 0, payLoad, 0, payLoadPart1.length);
                    System.arraycopy(containerNameByte, 0, payLoad, payLoadPart1.length, containerNameByte.length);
                    sendDataToDeviceByIP(hostName, AuxConfig.ResOrReqCommand.CMD_MUSICMSG_QUERY, payLoad);
                }
            }else{
                AuxLog.e("AuxdioProtocolSDK","queryMusic ---- "+containeEntity.getPlayListName()+" 目录下没有歌曲...请检查设备...");
            }
    }

    //查询分区名称（获取设备全部分区）
    public void queryRoomName(String devIP) throws IOException {
        queryDeviceInfoByIP(devIP, AuxConfig.ResOrReqCommand.CMD_QUERY_CHANNELNAME);
    }

    //设置分区名称
    public void setRoomName(String newRoomName) throws IOException {
        byte[] roomNameBytes = newRoomName.getBytes("gb2312");
        int roomLen = roomNameBytes.length;
        int devModel = AuxUdpUnicast.getInstance().getControlDeviceEntity().getDevModel();
        if (devModel == AuxConfig.DeciveModel.DEVICE_DM836 || devModel == AuxConfig.DeciveModel.DEVICE_DM838 || devModel == AuxConfig.DeciveModel.DEVICE_DM858){
            if (roomLen > 16)
                roomLen = 16;
        }else{
            if (roomLen > 6)
                roomLen = 6;
        }
        byte[] payLoad = new byte[1+roomLen];
        payLoad[0] = (byte) roomLen;
        System.arraycopy(roomNameBytes,0,payLoad,1,roomLen);
        setRoomInfo(AuxConfig.ResOrReqCommand.CMD_QUERY_CHANNELNAME,payLoad);
//        setDeviceInfoByIP(entity.getRoomIP(),AuxConfig.ResOrReqCommand.CMD_QUERY_CHANNELNAME,payLoad);
    }

    //查询分区开关机
    public void queryChannelOnOrOff() throws IOException {
        queryRoomInfo(AuxConfig.ResOrReqCommand.CMD_ONOFFSTATE_SET_QUERY);
    }

    //设置分区开关机
    public void setChannelOnOrOff(AuxRoomEntity roomEntity, int changeChannelOnOrOff) throws IOException {
        byte[] payLoad = {(byte)AuxConfig.SetOrQuery.REQUEST_SET,(byte)changeChannelOnOrOff};
        sendDataToRoom(AuxConfig.ResOrReqCommand.CMD_ONOFFSTATE_SET_QUERY,payLoad,new AuxRoomEntity[]{roomEntity});
//        sendDataToDeviceByIP(roomEntity.getRoomIP(),AuxConfig.ResOrReqCommand.CMD_ONOFFSTATE_SET_QUERY,payLoad);
    }

    //查询分区高低音
    public void queryHighLowPitch() throws IOException {
        queryRoomInfo(AuxConfig.ResOrReqCommand.CMD_EQ_HIGH_LOW_SET_QUERY);
    }

    //设置分区高低音
    public void setHighLowPitch(int highPitchValue,int lowPitchValue) throws IOException {

        setRoomInfo(AuxConfig.ResOrReqCommand.CMD_EQ_HIGH_LOW_SET_QUERY,new byte[]{(byte) (highPitchValue+11), (byte) (lowPitchValue+11)});
    }

    //查询分区音量
    public void queryVolume() throws IOException {
        queryRoomInfo(AuxConfig.ResOrReqCommand.CMD_VOLUME_SET_QUERY);
    }

    //设置分区音量
    public void setVolume(int volumeValue) throws IOException {
        setRoomInfo(AuxConfig.ResOrReqCommand.CMD_VOLUME_SET_QUERY,new byte[]{(byte) volumeValue});
    }

    //查询分区音源
    public void querySrcID() throws IOException {
        queryRoomInfo(AuxConfig.ResOrReqCommand.CMD_PROSRC_SET_QUERY);
    }

    //设置分区音源
    public void setSrcID(int prosrcValue) throws IOException {
        setRoomInfo(AuxConfig.ResOrReqCommand.CMD_PROSRC_SET_QUERY,new byte[]{(byte) prosrcValue});
    }

    //查询设备音源列表
    public void queryDeviceSourceName(String devIP) throws IOException {
        queryDeviceInfoByIP(devIP,AuxConfig.ResOrReqCommand.CMD_SOURCE_NAME);
    }

    //设置设备音源别名
    public void setDeviceSourceName(int srcID, String newSrcName) throws IOException {
        byte[] gb2312s = newSrcName.getBytes("gb2312");
        int    srcNameLen = gb2312s.length;
        byte[] payLoadPart1   = {(byte) srcID, (byte) srcNameLen};
        byte[] payLoad        = new byte[srcNameLen + payLoadPart1.length];

        System.arraycopy(payLoadPart1, 0, payLoad, 0, payLoadPart1.length);
        System.arraycopy(gb2312s, 0, payLoad, payLoadPart1.length, srcNameLen);
        setDeviceInfo(AuxConfig.ResOrReqCommand.CMD_SOURCE_NAME,payLoad);
    }

    //网络电台操作（添加或删除）
    public void netRadioOperation(byte addOrDel,String radioName,String radioAddress) throws IOException {
        byte[] radioNameBytes = radioName.getBytes("gb2312");
        byte[] radioAddressBytes = radioAddress.getBytes("gb2312");
        int radioNameLen = radioNameBytes.length;
        byte[] payLoad = new byte[3+radioNameLen+radioAddress.length()];
        payLoad[0] = addOrDel;
        payLoad[1] = (byte) radioNameLen;
        System.arraycopy(radioNameBytes,0,payLoad,2,radioNameLen);

        payLoad[2+radioNameLen] = (byte) radioAddressBytes.length;
        System.arraycopy(radioAddressBytes,0,payLoad,3+radioNameLen,radioAddress.length());
        sendDataToDevice(AuxConfig.ResOrReqCommand.CMD_ADDORDEL_RADIOADDRESS,payLoad);
    }

    //查询网络模块工作模式
    public void queryNetModelWorkModel() throws IOException {
        queryDeviceInfo(AuxConfig.ResOrReqCommand.CMD_NETWORKMODE_SET_QUERY);
    }

    //设置网络模块工作模式
    public void setNetModelWorkModel(int modleID, int workModel) throws IOException {
        setDeviceInfo(AuxConfig.ResOrReqCommand.CMD_NETWORKMODE_SET_QUERY,new byte[]{(byte) modleID, (byte) workModel});
    }

    //查询网络模块名称
    public void queryNetModelName(String devIP) throws IOException {
        queryDeviceInfoByIP(devIP,AuxConfig.ResOrReqCommand.CMD_NETMODE_NAME);
    }

    //设置网络模块名称
    public void setNetModelName(int modleID, byte[] modelName) throws IOException {
        byte[] payload = new byte[2+modelName.length];
        payload[0] = (byte) modleID;
        payload[1] = (byte) modelName.length;
        System.arraycopy(modelName,0,payload,2,modelName.length);
        setDeviceInfo(AuxConfig.ResOrReqCommand.CMD_NETMODE_NAME,payload);
    }

    //播放网络电台
    public void playRadio(int modelID,String radioName,String radioAddress) throws IOException {
        byte[] radioNameBytes = radioName.getBytes("gb2312");
        byte[] radioAddressBytes = radioAddress.getBytes("gb2312");
        int radioNameLen = radioNameBytes.length;
        byte[] payLoad = new byte[3+radioNameLen+radioAddress.length()];
        payLoad[0] = (byte) modelID;
        payLoad[1] = (byte) radioNameLen;
        System.arraycopy(radioNameBytes,0,payLoad,2,radioNameLen);
        payLoad[radioNameLen+2] = (byte) radioAddressBytes.length;
        System.arraycopy(radioAddressBytes,0,payLoad,radioNameLen+3,radioAddressBytes.length);
        sendDataToRoom(AuxConfig.ResOrReqCommand.CMD_PLAY_NETRADIO,payLoad);
    }

    public void playRadio_xima(int i, String ablumID, String trackID) throws IOException {
        byte[] ablumIDBytes = ablumID.getBytes("gb2312");
        int ablumIDLen = ablumIDBytes.length;
        byte[] trackIDBytes = trackID.getBytes("gb2312");
        int length = trackIDBytes.length;
        byte[] bytes = new byte[1+ablumIDLen+1+length+1];
        bytes[0] = (byte) i;
        bytes[1] = (byte) ablumIDLen;
        System.arraycopy(ablumIDBytes,0,bytes,2,ablumIDLen);
        bytes[ablumIDLen+2] = (byte) length;
        System.arraycopy(trackIDBytes,0,bytes,ablumIDLen+3,length);
        sendDataToRoom(AuxConfig.ResOrReqCommand.CMD_PLAY_RADIO_DM858,bytes);
    }

    //播放本地歌曲
    public void playMusic(String container,String musicName) throws IOException {
        byte[] containerByte = container.getBytes("gb2312");
        byte[] musicNameByte = musicName.getBytes("gb2312");
        byte[] payLoad       = new byte[2+containerByte.length+musicNameByte.length];
        byte[] payLoadPart1  = {(byte)containerByte.length};
        byte[] partLoadPart2 = {(byte)musicNameByte.length};
        System.arraycopy(payLoadPart1, 0, payLoad, 0, payLoadPart1.length);
        System.arraycopy(containerByte, 0, payLoad, payLoadPart1.length, containerByte.length);

        System.arraycopy(partLoadPart2, 0, payLoad, payLoadPart1.length+containerByte.length, partLoadPart2.length);
        System.arraycopy(musicNameByte, 0, payLoad, payLoadPart1.length+containerByte.length+partLoadPart2.length, musicNameByte.length);
        sendDataToRoom(AuxConfig.ResOrReqCommand.CMD_MUSICAPPIONT_PLAY,payLoad);
    }

    //查询设备版本
    public void queryDeviceVersion(String devIP) throws IOException {
        sendDataToDeviceByIP(devIP,AuxConfig.ResOrReqCommand.CMD_DEVICE_VERSION_QUERY,new byte[]{});
    }

    //查询网络模块关联
    public void queryModelRelevanceType() throws IOException {
        queryDeviceInfo(AuxConfig.ResOrReqCommand.CMD_CHANELMODEL_TYPE_QUERY_SET);
    }

    public void setNetModelRelevanceType(int type) throws IOException {
        setDeviceInfo(AuxConfig.ResOrReqCommand.CMD_CHANELMODEL_TYPE_QUERY_SET,new byte[]{(byte) type});
    }

    //查询全部房间关联网络模块
    public void queryBindAllRoomForNetModel() throws IOException {
        queryDeviceInfo(AuxConfig.ResOrReqCommand.CMD_CUSTOM_CHANELMODEL_ATTR_QUERY_SET);
    }

    public void setbindRoomforNetModel(AuxRoomEntity auxRoomEntity, int modelID) throws IOException {
        sendDataToRoom(AuxConfig.ResOrReqCommand.CMD_CUSTOM_CHANELMODEL_ATTR_QUERY_SET,new byte[]{(byte) 0x08,(byte) modelID},new AuxRoomEntity[]{auxRoomEntity});
    }

    //查询设备的点播模式
    public void queryPointPlayMode(int modelID) throws IOException {
        sendDataToDevice(AuxConfig.ResOrReqCommand.CMD_NETPLAY_MODEL,new byte[]{(byte) modelID,(byte)AuxConfig.SetOrQuery.REQUEST_QUERY});
    }

    //设置设备的点播模式
    public void setPointPlayMode(int modelID,int playMode) throws IOException {
        sendDataToDevice(AuxConfig.ResOrReqCommand.CMD_NETPLAY_MODEL,new byte[]{(byte) modelID,(byte)AuxConfig.SetOrQuery.REQUEST_SET, (byte) playMode});
    }

    //点播歌曲
    public void pointPlaySong(int modelID,String container,String songName) throws IOException {
        byte[] bytes1 = container.getBytes("gb2312");
        byte[] gb2312s = songName.getBytes("gb2312");
        byte[] bytes = new byte[3+bytes1.length+gb2312s.length];

        bytes[0] = (byte) modelID;
        bytes[1] = (byte) bytes1.length;
        System.arraycopy(bytes1,0,bytes,2,bytes1.length);
        bytes[2+bytes1.length] = (byte) gb2312s.length;
        System.arraycopy(gb2312s,0,bytes,3+bytes1.length,gb2312s.length);
        sendDataToDevice(AuxConfig.ResOrReqCommand.CMD_NETPLAY_MUSIC,bytes);
    }

    /**
     * 设置房间信息
     * @param cmd 请求命令
     * @param bytes 参数...
     * @throws IOException
     */
    public synchronized void setRoomInfo(int cmd, byte[] bytes) throws IOException {
        byte[] payLoad = new byte[bytes.length+1];
        payLoad[0] = (byte)AuxConfig.SetOrQuery.REQUEST_SET;
        System.arraycopy(bytes,0,payLoad,1,bytes.length);
        sendDataToRoom(cmd,payLoad);
//        sendDataToDevice(cmd,payLoad);
    }

    /**
     * 查询房间信息
     * @param cmd 请求命令
     * @throws IOException
     */
    public synchronized void queryRoomInfo(int cmd) throws IOException {
        byte[] payLoad = {(byte)AuxConfig.SetOrQuery.REQUEST_QUERY};
        sendDataToRoom(cmd,payLoad);
    }

    /**
     * 构成发送请求包,发送至分区
     * @param cmd 请求命令
     * @param payLoad 请求发送的数据
     * @throws IOException
     */
    public synchronized void sendDataToRoom(int cmd, byte[] payLoad) throws IOException {
        AuxRoomEntity[] roomEntities = AuxUdpUnicast.getInstance().getControlRoomEntities();
        sendDataToRoom(cmd, payLoad, roomEntities);

    }

    private void sendDataToRoom(int cmd, byte[] payLoad, AuxRoomEntity[] roomEntities) throws IOException {
        deviceEntity = AuxUdpUnicast.getInstance().getControlDeviceEntity();
        if (deviceEntity == null || roomEntities == null || roomEntities.length <= 0 )
        {
            AuxLog.e("sendDataToRoom","deviceEntity is null or Channel is null");
            return;
        }

        for (int i = 0; i < roomEntities.length; i++) {
            byte[] setChannelOnOrOff = AuxConfig.requestPackage(cmd, deviceEntity.getDevModel(), deviceEntity.getDevID(), deviceEntity.getDevZoneOrGroup(),roomEntities[i].getRoomID(), payLoad.length, payLoad);
            if (AuxUdpUnicast.getInstance().getUnicastRunnable() != null)
                AuxUdpUnicast.getInstance().getUnicastRunnable().sendData(roomEntities[i].getRoomIP(), setChannelOnOrOff, setChannelOnOrOff.length);
        }
    }

    /**
     * 设置设备信息
     * @param cmd 请求命令
     * @param bytes 参数...
     * @throws IOException
     */
    public synchronized void setDeviceInfo(int cmd, byte[] bytes) throws IOException {
        byte[] payLoad = new byte[bytes.length+1];
        payLoad[0] = (byte)AuxConfig.SetOrQuery.REQUEST_SET;
        System.arraycopy(bytes,0,payLoad,1,bytes.length);
        sendDataToDevice(cmd,payLoad);
    }

    /**
     * 查询设备信息
     * @param cmd 请求命令
     * @throws IOException
     */
    public synchronized void queryDeviceInfo(int cmd) throws IOException {
        byte[] payLoad = {(byte)AuxConfig.SetOrQuery.REQUEST_QUERY};
        sendDataToDevice(cmd,payLoad);
    }

    /**
     * 构成发送请求包,发送至设备
     *
     * @param cmd 请求命令
     * @param payLoad 请求发送的数据
     * @throws IOException
     */
    public synchronized void sendDataToDevice(int cmd, byte[] payLoad) throws IOException {
        deviceEntity = AuxUdpUnicast.getInstance().getControlDeviceEntity();
        if (deviceEntity != null)
            sendDataToDeviceByIP(deviceEntity.getDevIP(), cmd, payLoad);

    }

    /**
     * 设置设备信息
     * @param cmd 请求命令
     * @param bytes 参数...
     * @throws IOException
     */
    public synchronized void setDeviceInfoByIP(String devIP, int cmd, byte[] bytes) throws IOException {
        byte[] payLoad = new byte[bytes.length+1];
        payLoad[0] = (byte)AuxConfig.SetOrQuery.REQUEST_SET;
        System.arraycopy(bytes,0,payLoad,1,bytes.length);
        sendDataToDeviceByIP(devIP,cmd,payLoad);
    }

    /**
     * 查询设备信息
     * @param cmd 请求命令
     * @throws IOException
     */
    public synchronized void queryDeviceInfoByIP(String devIP, int cmd) throws IOException {
        byte[] payLoad = {(byte)AuxConfig.SetOrQuery.REQUEST_QUERY};
        sendDataToDeviceByIP(devIP,cmd,payLoad);
    }



    /**
     * 构成发送请求包,通过指定IP发送至设备
     * @param cmd 请求命令
     * @param payLoad 请求发送的数据
     * @throws IOException
     */
    private synchronized void sendDataToDeviceByIP(String devIP, int cmd, byte[] payLoad) throws IOException {
        deviceEntity = AuxUdpUnicast.getInstance().getControlDeviceEntity();
        if (deviceEntity == null){
            AuxLog.e("sendDataToDevice","deviceEntity is null");
            return;
        }

        if (deviceEntity.getDevModel() == AuxConfig.DeciveModel.DEVICE_DM836 || deviceEntity.getDevModel() == AuxConfig.DeciveModel.DEVICE_DM838 || deviceEntity.getDevModel() == AuxConfig.DeciveModel.DEVICE_DM858)
        {
            if (devIP == null || devIP.equals("")) {
                AuxLog.e("sendDataToDevice", "device  IP is null");
                return;
            }
        }else{
            if (!devIP.equals(deviceEntity.getDevIP()))
                devIP = deviceEntity.getDevIP();
        }

        byte[] setChannelOnOrOff = AuxConfig.requestPackage(cmd, deviceEntity.getDevModel(), deviceEntity.getDevID(), deviceEntity.getDevZoneOrGroup(),0xFF, payLoad.length, payLoad);
        if (AuxUdpUnicast.getInstance().getUnicastRunnable() != null)
            AuxUdpUnicast.getInstance().getUnicastRunnable().sendData(devIP,setChannelOnOrOff, setChannelOnOrOff.length);
        else
            AuxLog.e("sendDataToDevice","send runnable is null");
    }

    public void setPlayTime(byte[] totalTime, byte[] currentTime) {
        byte[] bytes = new byte[totalTime.length+currentTime.length];
        System.arraycopy(totalTime,0,bytes,0,totalTime.length);
        System.arraycopy(currentTime,0,bytes,totalTime.length,currentTime.length);
        try {
            setRoomInfo(AuxConfig.ResOrReqCommand.CMD_QUERY_PLAYTIME,bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
