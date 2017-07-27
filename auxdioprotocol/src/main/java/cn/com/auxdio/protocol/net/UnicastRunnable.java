package cn.com.auxdio.protocol.net;


import android.util.SparseIntArray;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.com.auxdio.protocol.bean.AuxDeviceEntity;
import cn.com.auxdio.protocol.bean.AuxNetModelEntity;
import cn.com.auxdio.protocol.bean.AuxPlayListEntity;
import cn.com.auxdio.protocol.bean.AuxRoomEntity;
import cn.com.auxdio.protocol.bean.AuxSongEntity;
import cn.com.auxdio.protocol.bean.AuxSoundEffectEntity;
import cn.com.auxdio.protocol.bean.AuxSourceEntity;
import cn.com.auxdio.protocol.protocol.AuxConfig;
import cn.com.auxdio.protocol.util.AuxByteToStringUtils;
import cn.com.auxdio.protocol.util.AuxLog;
import cn.com.auxdio.protocol.util.AuxNetModelUtils;
import cn.com.auxdio.protocol.util.AuxPlayListUtils;
import cn.com.auxdio.protocol.util.AuxRoomUtils;
import cn.com.auxdio.protocol.util.AuxSouceUtils;
import cn.com.auxdio.protocol.util.TimeUtils;

/**
 * Created by Auxdio on 2017/3/8 0008.
 */

public class UnicastRunnable implements Runnable {

    private DatagramSocket mUnicastSocket;//发送/接受数据报
    private List<AuxRoomEntity> mChannelEntities;//房间列表
    private Map<String,List<AuxPlayListEntity>> mContentsEntities;//播放列表
    private List<AuxNetModelEntity> mNetModelEntities;//网络模块列表
    private List<AuxSourceEntity> sourceEntities;//音源列表
    private Map<Integer,AuxNetModelEntity> netModelEntityMap;
//    private List<Map<Integer,AuxNetModelEntity>> mMapList;

    private boolean isDeviceVersion_V_0_3_3 = false;


    protected UnicastRunnable() {
        try {
             init();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    private void init() throws SocketException {
        if (mChannelEntities == null)
            mChannelEntities = new CopyOnWriteArrayList<>();
        else
            mChannelEntities.clear();

        mUnicastSocket = new DatagramSocket(null);
        mUnicastSocket.setReuseAddress(true);
        mUnicastSocket.bind(new InetSocketAddress(AuxNetConstant.UICAST_PORT));

        mContentsEntities = new Hashtable<>();

    }

    protected void onDestory(){
        if (mUnicastSocket != null) {
//            mUnicastSocket.disconnect();
//            mUnicastSocket.close();
//            mUnicastSocket = null;
        }
        mContentsEntities.clear();
        mContentsEntities = null;

        mChannelEntities.clear();
        mChannelEntities = null;
    }

    public void sendData(final String devIP, final byte[] data, final int len) throws IOException {
        if (devIP != null) {
            if (mUnicastSocket != null){
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            DatagramPacket datagramPacket = new DatagramPacket(data, len, InetAddress.getByName(devIP), AuxNetConstant.UICAST_PORT);
                            mUnicastSocket.send(datagramPacket);
                            AuxLog.i("UnicastRunnable", "sendData:" + AuxByteToStringUtils.bytesToHexString(data,len));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }else
                AuxLog.e("UnicastRunnable", "mUnicastSocket == null   "+(mUnicastSocket == null));
        }
    }


    public Map<String, List<AuxPlayListEntity>> getContentsEntities() {
        return mContentsEntities;
    }

    public List<AuxRoomEntity> getChannelEntities() {
        return mChannelEntities;
    }

    protected List<AuxNetModelEntity> getNetModelEntities() {
        return mNetModelEntities;
    }

    public Map<Integer, AuxNetModelEntity> getNetModelEntityMap() {
        return netModelEntityMap;
    }

    public List<AuxSourceEntity> getSourceEntities() {
        return sourceEntities;
    }

    public void setSourceEntities(List<AuxSourceEntity> sourceEntities) {
        this.sourceEntities = sourceEntities;
    }

    @Override
    public void run() {


        while (!AuxUdpUnicast.getInstance().isStop()) {
            byte[] bytes = new byte[AuxNetConstant.MAX_BUFFER];
            DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length);
            try {
                if (mUnicastSocket != null) {
                    mUnicastSocket.receive(datagramPacket);
                    String hostName = datagramPacket.getAddress().getHostAddress();
                    int port = datagramPacket.getPort();
                    int length = datagramPacket.getLength();
                    byte[] data = datagramPacket.getData();
                    AuxLog.i("UnicastRunnable", "run---  " +((data[0] & 0xFF) +"    "+(data[1] & 0xFF))+"   hostName:"+hostName +"   port:"+port+"  data8: "+data[8]);
                    handleAuxdioCmd(hostName,data, length);
                }
            } catch (IOException e) {
                e.printStackTrace();
//                AuxLog.e("run","单播是否停止："+(Thread.interrupted()));
            }
        }

    }

    /**
     * 处理协议命令
     *
     * @param hostName
     * @param data
     * @param length
     */
    private void handleAuxdioCmd(String hostName, byte[] data, int length) {
        if (data[8] == 0)
            return;
//UnicastRunnable---data:0x00  0x0d
        AuxLog.i("handleAuxdioCmd", "UnicastRunnable---data:" + AuxByteToStringUtils.bytesToHexString(data, length));
        switch (AuxConfig.combineCommand(data[0], data[1])) {
            case AuxConfig.ResOrReqCommand.CMD_SEARCH_HOST_RESPONSE://搜索到设备应答
                try {
                    if (AuxUdpBroadcast.getInstace().getBroadCastRunnable() != null)
                        AuxUdpBroadcast.getInstace().getBroadCastRunnable().listDeviceHandle(hostName, data);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
            case AuxConfig.ResOrReqCommand.CMD_CHANORDEVSTATE_QUERY: //查询全部分区状态
                listRoomState(hostName,data);
                break;

            case AuxConfig.ResOrReqCommand.CMD_QUERY_CHANNELNAME: //查询分区名称(获取分区列表)
                try {
                    listRoomName(data,hostName);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;

            case AuxConfig.ResOrReqCommand.CMD_MUSICCONTAINER_QUERY: //查询设备歌曲目录
                listMusicContents(hostName,data);
                break;

            case AuxConfig.ResOrReqCommand.CMD_MUSICMSG_QUERY://查询设备歌曲
                listMusicInfos(hostName,data);
                break;

            case AuxConfig.ResOrReqCommand.CMD_ONOFFSTATE_SET_QUERY://查询分区开关机状态
                if (AuxUdpUnicast.getInstance().getRoomOnOffListener() != null) {
                    AuxUdpUnicast.getInstance().getRoomOnOffListener().onOnOffState((data[9] & 0xFF) == 0x01?true:false);
                }
                break;

            case AuxConfig.ResOrReqCommand.CMD_SOURCE_NAME://查询设备音源列表
                try {
                    listSourceInfos(hostName,data);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
            case AuxConfig.ResOrReqCommand.CMD_VOLUME_SET_QUERY://查询音量值
                handleCommon(hostName,data);
                break;

            case AuxConfig.ResOrReqCommand.CMD_PROSRC_SET_QUERY://查询当前音源
                AuxLog.i("","查询当前音源");
                handleCommon(hostName, data);
                break;

            case AuxConfig.ResOrReqCommand.CMD_SOUNDTRACK_SET_QUERY://查询音效
                handleCommon(hostName, data);
                break;

            case AuxConfig.ResOrReqCommand.CMD_PLAYMODE_SET_QUERY://查询播放模式
                handleCommon(hostName, data);
                break;

            case AuxConfig.ResOrReqCommand.CMD_PLAYSTATE_SET_QUERY://查询播放状态
                handleCommon(hostName, data);
                break;

            case AuxConfig.ResOrReqCommand.CMD_MUTESTATE_SET_QUERY://查询静音状态
                AuxLog.i("","查询静音状态");
                handleCommon(hostName, data);
                break;

            case AuxConfig.ResOrReqCommand.CMD_QUERY_PROGRAM://查询节目名称
                handleCommon(hostName, data);
                break;

            case AuxConfig.ResOrReqCommand.CMD_NETWORKMODE_SET_QUERY://查询网络模块工作模式
                try {
                    listNetModel(data);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
            case AuxConfig.ResOrReqCommand.CMD_NETMODE_NAME://查询网络模块名称（列表）
                try {
                    listNetModelNameHandle(data);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;

            case AuxConfig.ResOrReqCommand.CMD_DEVICE_VERSION_QUERY://查询设备版本
                handleDeviceVersion(hostName,data);
                break;
            case AuxConfig.ResOrReqCommand.CMD_EQ_HIGH_LOW_SET_QUERY://查询高低音
                if (AuxUdpUnicast.getInstance().getHighLowPitchListener() != null)
                    AuxUdpUnicast.getInstance().getHighLowPitchListener().onHighLowPitch(((data[9] & 0xFF) -11),((data[10] & 0xFF) -11));
                break;

            case AuxConfig.ResOrReqCommand.CMD_CHANELMODEL_TYPE_QUERY_SET://查询分区和模块绑定类型
                if (AuxUdpUnicast.getInstance().getNetModelBindTypeListener() != null)
                    AuxUdpUnicast.getInstance().getNetModelBindTypeListener().onRelevanceType(data[9] & 0xFF);
                break;

            case AuxConfig.ResOrReqCommand.CMD_CUSTOM_CHANELMODEL_ATTR_QUERY_SET://查询分区和模块绑定列表
                handleRoomModelBind(data);
                break;

            case AuxConfig.ResOrReqCommand.CMD_NETPLAY_MODEL://查询网络点播模式
                if (AuxUdpUnicast.getInstance().getPlayModeListener() != null){
                    int modelID = (data[9] & 0xFF);
                    int playMode = data[10] & 0xFF;
                    AuxSouceUtils.callBackPlayModel(modelID, playMode);
                }

//                handleNetModelPlayMode(data);
                break;
            case AuxConfig.ResOrReqCommand.CMD_QUERY_PLAYTIME:

                int songTotleTime = TimeUtils.getSongTotleTime(data);
                int songCurrentTime = TimeUtils.getSongCurrentTime(data);
                if (songTotleTime == 0)
                    break;
                AuxUdpUnicast.getInstance().getSongTimeLengthListener().onSongTimeLength(songTotleTime,songCurrentTime,(100*songCurrentTime)/songTotleTime);
                break;

        }
    }

    //网络模块的点播模式获取
    private void handleNetModelPlayMode(byte[] data) {
        int index = 8;
        int modeCount = data[index++] / 2;//模式数量
        Map<Integer,Integer> integerMap = new Hashtable<>();
        for (int i = 0; i < modeCount; i++) {
            int modelID = data[index++];
            int playMode = data[index++];
            integerMap.put(modelID,playMode);
        }

        if (AuxUdpUnicast.getInstance().getNetModelPlayModeListListener() != null) {
            AuxUdpUnicast.getInstance().getNetModelPlayModeListListener().onPlayModeList(integerMap);
        }
    }

    //room和model绑定列表
    protected void handleRoomModelBind(byte[] data) {
//        mMapList = new CopyOnWriteArrayList<>();
        int index = 8;
        int roomCount = (data[index++] & 0xFF) /2 ;
        netModelEntityMap = new Hashtable<>();
        for (int i = 0; i < roomCount; i++) {
//            Map<Integer,AuxNetModelEntity> netModelEntityMap = new Hashtable<>();
            int roomID = data[index++];
            int modelID = data[index++];
            if (mNetModelEntities == null){
                if (AuxUdpUnicast.getInstance().getNetModelListener() != null) {
                    AuxUdpUnicast.getInstance().requestNetModelWorkModel(AuxUdpUnicast.getInstance().getNetModelListener());
                }
                return;
            }
            AuxNetModelEntity netModelbyID = AuxNetModelUtils.getNetModelbyID(mNetModelEntities, modelID);
            if (netModelbyID == null)
                break;
            AuxLog.i("handleRoomModelBind","roomID:"+roomID+"      "+netModelbyID.toString());
            netModelEntityMap.put(roomID,netModelbyID);
//            mMapList.add(netModelEntityMap);
        }

        if (AuxUdpUnicast.getInstance().getNetModelBindListListener() != null)
            AuxUdpUnicast.getInstance().getNetModelBindListListener().onBindList(netModelEntityMap);
    }

    //网络模块名称查询（查询网络模块列表）
    private void listNetModelNameHandle(byte[] data) throws UnsupportedEncodingException {
        mNetModelEntities = new ArrayList<>();
        int index = 9;
        int  modelCount = data[index++] & 0xFF;
        for (int i = 0; i < modelCount; i++) {
            int modelID = data[index++];
            int modelLen = data[index++];
            byte[] bytes = new byte[modelLen];
            System.arraycopy(data,index,bytes,0,modelLen);
            String modelName = new String(bytes,"gbk");
            AuxNetModelEntity auxNetModelEntity = new AuxNetModelEntity();
            auxNetModelEntity.setModelID(modelID);
            auxNetModelEntity.setModelName(modelName);
            index+=modelLen;
            mNetModelEntities.add(auxNetModelEntity);
            AuxLog.i("listNetModelNameHandle",auxNetModelEntity.toString());
        }

        if (isDeviceVersion_V_0_3_3){
            isDeviceVersion_V_0_3_3 = false;
            AuxUdpUnicast.getInstance().requestBindAllRoomForNetModel();
        }

        if (AuxUdpUnicast.getInstance().getNetModelListener() != null) {
            AuxUdpUnicast.getInstance().requestNetModelWorkModel(AuxUdpUnicast.getInstance().getNetModelListener());
        }
    }

    //设备版本处理
    private void handleDeviceVersion(String hostName, byte[] data) {
        try {
            int index = 9;
            int softWareVersionLen = data[index++];
            byte[] bytes = new byte[softWareVersionLen];
            System.arraycopy(data,index,bytes,0,softWareVersionLen);
            String softWareVersion = new String(bytes,"gbk");
            index+=softWareVersionLen;
            int protocolVersionLen = data[index++];
            byte[] bytes1 = new byte[protocolVersionLen];
            System.arraycopy(data,index,bytes1,0,protocolVersionLen);
            String protocolVersion = new String(bytes1,"gbk");
            int indexOf = softWareVersion.indexOf("_V");
            String substring = softWareVersion.substring(indexOf+2);
            String substring1 = substring.substring(0, 5);
            int deveiceIDResponse = AuxConfig.getDeveiceIDResponse(data[3], data[4]);
            if (deveiceIDResponse == AuxConfig.DeciveModel.DEVICE_AM8318 || deveiceIDResponse == AuxConfig.DeciveModel.DEVICE_AM8328){
                if (substring1.substring(0,4).equals("0.3.")){
                    if(substring1.toCharArray()[4] >= 3){
                        AuxUdpUnicast.getInstance().requestNetModelList(hostName);
                        isDeviceVersion_V_0_3_3 = true;
                    }
                }
            }
            AuxLog.i("handleDeviceVersion","deviceMode:"+deveiceIDResponse+"   softWareVersion:"+softWareVersion+"   protocolVersion:"+protocolVersion );

            if (AuxUdpUnicast.getInstance().getDeviceVersionListener() != null)
                AuxUdpUnicast.getInstance().getDeviceVersionListener().onDeviceVersion(softWareVersion,protocolVersion);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    //网络模块处理
    private void listNetModel(byte[] data) throws UnsupportedEncodingException {
        int index = 9;
        int modelCount = data[index++] & 0xFF;
        for (int i = 0; i < modelCount; i++) {
            int modelID = data[index++] & 0xFF;
            int workMode = data[index++] & 0xFF;
            int ipLen = data[index++] & 0xFF;
            byte[] bytes = new byte[ipLen];
            System.arraycopy(data,index,bytes,0,ipLen);
            String modelIp =  new String(bytes,"gbk");
            index+=ipLen;
            if (mNetModelEntities  == null)
                break;
            AuxNetModelEntity netModelbyID = AuxNetModelUtils.getNetModelbyID(mNetModelEntities, modelID);
            if (netModelbyID == null) {
                break;
            }
            netModelbyID.setWorkMode(workMode);
            netModelbyID.setModelIP(modelIp);
        }

        if (AuxUdpUnicast.getInstance().getNetModelListener() != null)
            AuxUdpUnicast.getInstance().getNetModelListener().onNetModelList(mNetModelEntities);
    }

    //查询当前播放的节目名称
    private void handleProgramName(AuxRoomEntity auxRoomEntity, byte[] data) {
        AuxLog.i("","handleProgramName  "+data[1]+"  "+data[8]);
        try {
            int len =  data[8];
            byte[] bytes = new byte[len];
            System.arraycopy(data,9,bytes,0,len);
            String proName = new String(bytes,"gbk");
            AuxLog.i("handleProgramName","proName:"+proName);
            if (proName.equals("DVD") || proName.equals("AUX1") || proName.equals("AUX2"))
                return;
            AuxSouceUtils.callBackProgramName(auxRoomEntity.getSrcID(),proName);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 音量处理，回调音量
     * @param hostName
     * @param data
     */
    private void handleCommon(String hostName, byte[] data) {

        int devModel = AuxConfig.getDeveiceIDResponse(data[3], data[4]);
        AuxLog.i("handleCommon","---devModel:----"+(devModel != AuxUdpUnicast.getInstance().getControlDeviceEntity().getDevModel()));
        if (devModel != AuxUdpUnicast.getInstance().getControlDeviceEntity().getDevModel())
            return;

        int  roomID = data[7] & 0xFF;//分区ID
        AuxRoomEntity[] roomEntities = AuxUdpUnicast.getInstance().getControlRoomEntities();
        AuxLog.i("handleCommon","auxRoomEntity:----"+(roomEntities == null)+"   devModel:"+devModel+"   roomID:"+roomID);
        if (roomEntities != null && roomEntities.length > 0){
            AuxRoomEntity auxRoomEntity = null;
            if (devModel == AuxConfig.DeciveModel.DEVICE_DM838 || devModel == AuxConfig.DeciveModel.DEVICE_DM836 || devModel == AuxConfig.DeciveModel.DEVICE_DM858){
                auxRoomEntity = AuxRoomUtils.getChannnelIndexByIP(roomEntities, hostName);
            }else
                auxRoomEntity = AuxRoomUtils.getChannnelIndexByID(roomEntities, roomID);

            AuxLog.i("handleCommon","auxRoomEntity:"+(auxRoomEntity == null)+"  "+AuxConfig.combineCommand(data[0], data[1]));
            if (auxRoomEntity == null)
                auxRoomEntity = roomEntities[0];

            switch (AuxConfig.combineCommand(data[0], data[1])) {
                case AuxConfig.ResOrReqCommand.CMD_QUERY_PROGRAM://查询节目名称
                    AuxLog.i("","handleProgramName  "+data[1]+"  "+data[8]);
                    handleProgramName(auxRoomEntity,data);
                    break;

                case AuxConfig.ResOrReqCommand.CMD_PLAYMODE_SET_QUERY://查询播放模式
                    AuxSouceUtils.callBackPlayMode(auxRoomEntity.getSrcID(),data[9] & 0xFF);
                    break;

                case AuxConfig.ResOrReqCommand.CMD_PLAYSTATE_SET_QUERY://查询播放状态
                    AuxSouceUtils.callBackPlayState(auxRoomEntity.getSrcID(),data[9] & 0xFF);
                    break;

                case AuxConfig.ResOrReqCommand.CMD_MUTESTATE_SET_QUERY://查询静音状态
                    if (AuxUdpUnicast.getInstance().getMuteStateListener() != null)
                        AuxUdpUnicast.getInstance().getMuteStateListener().onMuteState(auxRoomEntity,((data[9] & 0xFF) == 0x10));
                    break;

                case AuxConfig.ResOrReqCommand.CMD_VOLUME_SET_QUERY://查询音量值
                    if (AuxUdpUnicast.getInstance().getVolumeListener() != null)
                        AuxUdpUnicast.getInstance().getVolumeListener().onVolume(auxRoomEntity,data[9] & 0xFF);
                    break;

                case AuxConfig.ResOrReqCommand.CMD_SOUNDTRACK_SET_QUERY://查询音效
                    if (AuxUdpUnicast.getInstance().getSoundEffectListener() != null) {
                        List<AuxSoundEffectEntity> soundEffectEntities = AuxUdpUnicast.getInstance().getSoundEffectEntities();
                        if (soundEffectEntities != null){
                            AuxSoundEffectEntity auxSoundEffectEntity = AuxSouceUtils.getSoundEffectByID(soundEffectEntities, data[9] & 0xFF);
                            AuxUdpUnicast.getInstance().getSoundEffectListener().onCurrentSoundEffect(auxRoomEntity,auxSoundEffectEntity);
                        }
                    }
                    break;

                case AuxConfig.ResOrReqCommand.CMD_PROSRC_SET_QUERY://查询当前音源ID
                    if (AuxUdpUnicast.getInstance().getSrcIDListener() != null){
                        int srcID = data[9] & 0xFF;
                        AuxSourceEntity sourceEntityByID = AuxSouceUtils.getSourceEntityByID(sourceEntities, srcID);
                        if (sourceEntityByID != null)
                            AuxUdpUnicast.getInstance().getSrcIDListener().onSourceEntity(auxRoomEntity,sourceEntityByID);
                    }
                    break;
            }

        }
    }

    private void listSourceInfos(String hostName, byte[] data) throws UnsupportedEncodingException {
        sourceEntities = new ArrayList<>();
            int index = 9;
            while (index < data[8]+9){
                int srcID = data[index++] & 0xFF;
                int srcNameLen = data[index++] & 0xFF;
                byte[] bytes = new byte[srcNameLen];
                System.arraycopy(data,index,bytes,0,srcNameLen);
                String srcName = new String(bytes,"gbk");
                index+=srcNameLen;
                AuxLog.i("listSourceInfos","index:"+index);
                sourceEntities.add(new AuxSourceEntity(srcID,srcName));
            }

            if (AuxConfig.getDeveiceIDResponse(data[3],data[4]) == AuxConfig.DeciveModel.DEVICE_AM8318)
            {
                sourceEntities.add(new AuxSourceEntity(0xB1,"网络音乐"));
                sourceEntities.add(new AuxSourceEntity(0xC1,"网络电台"));
            }else if (AuxConfig.getDeveiceIDResponse(data[3],data[4]) == AuxConfig.DeciveModel.DEVICE_AM8328){
                sourceEntities.add(new AuxSourceEntity(0xB1,"网络音乐"));
                sourceEntities.add(new AuxSourceEntity(0xC1,"网络电台"));

                int sd_usb_index = AuxSouceUtils.get_SD_USB_Index(sourceEntities);
                if (sd_usb_index == -1)
                    sourceEntities.add(0,new AuxSourceEntity(0xD1,"SD/USB"));
                else{
                    AuxSourceEntity auxSourceEntity = sourceEntities.get(sd_usb_index);
                    auxSourceEntity.setSourceID(0xD1);
                }
            }

            if (AuxUdpUnicast.getInstance().getAuxRequestSourceListener() != null) {
                AuxUdpUnicast.getInstance().getAuxRequestSourceListener().onSourceList(hostName,sourceEntities);
            }

    }

    private SparseIntArray mIntArray = new SparseIntArray();
    /**
     * 获取歌曲信息
     * @param hostName
     * @param data
     */
    private synchronized void listMusicInfos(String hostName, byte[] data) {
        try {
            int index = 8;
            int dataLen = data[index++] & 0xFF;//包长
            int containerID = data[index++] & 0xFF;//目录ID
            int packageID = data[index++] & 0xFF;//数据包...
            int i = mIntArray.get(containerID);
            mIntArray.put(containerID,++i);
            AuxLog.i("listMusicInfos","dataLen:"+dataLen+"   containerID:"+containerID+",packageID:"+packageID);

            ArrayList<AuxSongEntity> musicEntities = new ArrayList<>();
            while (index < dataLen + 9){
                AuxSongEntity auxSongEntity = new AuxSongEntity();
                auxSongEntity.setContentID(containerID);

                int musicNameLen = data[index++];
                byte[] bytes = new byte[musicNameLen];

                System.arraycopy(data,index,bytes,0,musicNameLen);
                String musicName = new String(bytes,"gbk");
                auxSongEntity.setSongName(musicName);
                AuxLog.i("listMusicInfos",""+ auxSongEntity.toString());
                int devModel = AuxConfig.getDeveiceIDResponse(data[3], data[4]);
                if (devModel == 0x07 || devModel == 0x08)//机型为DM858或者为DM838
                {
                    index+=musicNameLen;
                    int tagLen = data[index++];//歌曲标示
                    byte[] bytes1 = new byte[tagLen];
                    System.arraycopy(data,index,bytes1,0,tagLen);
                    String musicTag= new String(bytes1,"gbk");
                    auxSongEntity.setSongTag(musicTag);
                    AuxLog.i("listMusicInfos","tagLen:"+tagLen+"   musicTag:"+musicTag+"   musicName:"+musicName);
                    index+=tagLen;
                }else
                    index+=musicNameLen;

                musicEntities.add(auxSongEntity);
            }

            if (mContentsEntities == null)
                return;
            List<AuxPlayListEntity> auxPlayListEntities = mContentsEntities.get(hostName);
            AuxPlayListEntity contentByID = AuxPlayListUtils.getContentByID(containerID, auxPlayListEntities);
            if (contentByID == null)
                return;
            ArrayList<AuxSongEntity> musicEntities1 = contentByID.getMusicEntities();
            if ( musicEntities1 != null && musicEntities1.size() > 0){
                musicEntities1.addAll(musicEntities);
                contentByID.setMusicEntities(musicEntities1);
            }else {
                contentByID.setMusicEntities(musicEntities);
            }

            AuxLog.i("listMusicInfos","count:"+contentByID.getContentsPageCount()+"   size:"+contentByID.getContentsName()+"("+contentByID.getContentsID()+")"+"   packageID:"+packageID);

            int playListCount = getPlayListCount(auxPlayListEntities);
            AuxLog.i("listMusicInfos","mIntArray:"+mIntArray.size()+", playListCount:"+playListCount+", totlePlayListCount = "+auxPlayListEntities.size()+", playListName:"+contentByID.getPlayListName()+", packageID:"+packageID+", currentCount:"+mIntArray.get(containerID)+", totleCount = "+contentByID.getContentsPageCount());
            // && contentByID.getContentsPageCount() == packageID && contentByID.getContentsID() == auxPlayListEntities.size()

            if (mIntArray.size() == playListCount && mIntArray.get(containerID) == contentByID.getContentsPageCount()){
                if (AuxUdpUnicast.getInstance().getQueryMusicListener() != null) {
                    mIntArray.clear();
                    AuxUdpUnicast.getInstance().getQueryMusicListener().onMusicList(hostName,auxPlayListEntities);
                }
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    private int  getPlayListCount(List<AuxPlayListEntity> auxPlayListEntities) {
        int index = 0;
        for (AuxPlayListEntity auxPlayListEntity : auxPlayListEntities) {
            if (auxPlayListEntity.getContentsPageCount() != 0)
                index++;
        }
        return index;
    }

    /**
     * 获取设备歌曲目录
     * @param hostName
     * @param data
     */
    private synchronized void listMusicContents(String hostName, byte[] data) {
        int index = 9;
        int contentsCount = data[index++];
        try {
            List<AuxPlayListEntity> contentsEntities = new ArrayList<>();
            for (int i = 0; i < contentsCount; i++) {
                AuxPlayListEntity entity = new AuxPlayListEntity();
                int contentsId = data[index++];//目录ID
                int contentsLen = data[index++];//目录长度

                byte[] bytes = new byte[contentsLen];
                System.arraycopy(data,index,bytes,0,contentsLen);
                String contentsName = new String(bytes,"gbk");
                entity.setContentsID(contentsId);

                entity.setContentsName(contentsName);
                index+=contentsLen;
                entity.setContentsPageCount(data[index++] & 0xFF);
                AuxLog.i("listMusicContents","目录个数："+contentsCount+"    hostName:"+hostName+"     "+entity.toString());
                contentsEntities.add(entity);
            }

            List<AuxPlayListEntity> auxPlayListEntities = AuxPlayListUtils.containsKey(hostName, mContentsEntities);
            if (auxPlayListEntities != null){
                for (AuxPlayListEntity auxPlayListEntity : auxPlayListEntities) {
                    auxPlayListEntity.setMusicEntities(null);
                }
            }

            mContentsEntities.put(hostName,contentsEntities);

            AuxLog.i("listMusicContents","size:"+mContentsEntities.size());

            if (mContentsEntities.size() > 0 && contentsEntities.size() > 0){
                synchronized (mContentsEntities){
                    for (AuxPlayListEntity contentsEntity : contentsEntities) {
                        AuxUdpUnicast.getInstance().requestMusicListByContentName(hostName, contentsEntity);
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取全部分区个数和名称
     * @param data
     * @param hostName
     */
    private void listRoomName(byte[] data, String hostName) throws InterruptedException {
        clearRoomList(data, hostName);
        try {
            int index = 9;
            int roomCount = data[index++];//分区数量
            AuxLog.i("listRoomName","roomCount:"+roomCount+", hostName:"+hostName);
            for (int i = 0; i < roomCount; i++) {
                AuxRoomEntity auxChannelEntity = new AuxRoomEntity();
                auxChannelEntity.setRoomID(data[index++]);
                int roomNameLen = data[index++];
                byte[]  bytes = new byte[roomNameLen];
                System.arraycopy(data,index,bytes,0,roomNameLen);
                String s = new String(bytes,"gbk");
                auxChannelEntity.setRoomName(s);
                auxChannelEntity.setRoomIP(hostName);
                index+=roomNameLen;
                mChannelEntities.add(auxChannelEntity);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        AuxUdpUnicast.getInstance().requestDeviceRoomState(hostName,AuxUdpUnicast.getInstance().getAuxRoomStateChangedListener());
    }

    /**
     * 获取房间列表前，必须先清空当前的房间列表，以免混乱
     * @param data
     * @param hostName
     */
    private void clearRoomList(byte[] data, String hostName) {
        int devModel = AuxConfig.getDeveiceIDResponse(data[3], data[4]);
        if (devModel == AuxConfig.DeciveModel.DEVICE_DM836 || devModel == AuxConfig.DeciveModel.DEVICE_DM838 || devModel == AuxConfig.DeciveModel.DEVICE_DM858) {
            int indexByIP = AuxRoomUtils.getChannnelIndexByIP(mChannelEntities, hostName);
            if (indexByIP > -1){
                mChannelEntities.remove(indexByIP);
            }
        }else
            mChannelEntities.clear();
    }

    /**
     * 获取全部分区状态
     * @param hostName
     * @param data
     */
    private void listRoomState(String hostName, byte[] data) {
        long timeMillis = System.currentTimeMillis();
        int index = 9;
        int maxChannelCount = data[index++];//最大分区个数
        int roomCount = (data[8]-1)/6;//分区数量
        AuxLog.i("listRoomState","roomCount:"+roomCount+"   maxChannelCount:"+maxChannelCount+", hostName:"+hostName);
        boolean isChange = false;

        for (byte i = 0; i < maxChannelCount; i++) {
            int roomID = data[index++] & 0xFF;//分区ID
            int srcID = data[index++] & 0xFF;//音源ID
            int volumeID = data[index++] & 0xFF;//音量条
            int offState = data[index++] & 0xFF;//开关机状态
            int highPitch = (data[index++] & 0xFF) - 11;//高音
            int lowPitch = (data[index++] & 0xFF) -11;//低音

            int devModel = AuxConfig.getDeveiceIDResponse(data[3], data[4]);
            AuxLog.i("listRoomState",devModel+"   roomID:"+roomID+"   srcID:"+srcID+"   volumeID:"+volumeID+"   offState:"+offState+"   highPitch"+highPitch+"   lowPitch:"+lowPitch);

            AuxDeviceEntity controlDeviceEntity = AuxUdpUnicast.getInstance().getControlDeviceEntity();
            if (controlDeviceEntity == null)
                break;

            if (devModel == controlDeviceEntity.getDevModel()){
                AuxRoomEntity auxRoomEntity;
                if (devModel == AuxConfig.DeciveModel.DEVICE_DM836 || devModel == AuxConfig.DeciveModel.DEVICE_DM838 || devModel == AuxConfig.DeciveModel.DEVICE_DM858)
                    auxRoomEntity = AuxRoomUtils.getChannnelByIP(mChannelEntities, hostName);
                else
                    auxRoomEntity = AuxRoomUtils.getChannnelByID(mChannelEntities, roomID);//根据分区ID获取房间列表

                if (auxRoomEntity == null)
                    return;

                auxRoomEntity.setTimeOut(timeMillis);

                if (auxRoomEntity.getSrcID() != srcID) {
                    auxRoomEntity.setSrcID(srcID);
                    isChange = true;
                }
                if (auxRoomEntity.getVolumeID() != volumeID){
                    auxRoomEntity.setVolumeID(volumeID);
                    isChange = true;
                }
                if (auxRoomEntity.getoNOffState() != offState) {
                    auxRoomEntity.setoNOffState(offState);
                    isChange = true;
                }
                if (auxRoomEntity.getHighPitch() != highPitch) {
                    auxRoomEntity.setHighPitch(highPitch);
                    isChange = true;
                }
                if (auxRoomEntity.getLowPitch() != lowPitch){
                    auxRoomEntity.setLowPitch(lowPitch);
                    isChange = true;
                }

                auxRoomEntity.setRoomSrcName(getSrcName(auxRoomEntity.getSrcID(),devModel));

                AuxLog.i("channnelByID","roomID:"+auxRoomEntity.toString()+"isChange:"+isChange+",hostName:"+hostName);

                if (isChange){
                    if (AuxUdpUnicast.getInstance().getAuxRoomStateChangedListener() != null){
                        AuxUdpUnicast.getInstance().getAuxRoomStateChangedListener().onRoomChange(auxRoomEntity);
                    }
                    AuxLog.i("channnelByID",auxRoomEntity.getRoomName()+"  回调...isChange == "+isChange);
                    isChange = false;
                }
            }
        }
    }

    private String getSrcName(int srcID, int devMode) {
        String sourceName = "";

        if (sourceEntities == null) {
            return sourceName;
        }

        if (devMode == AuxConfig.DeciveModel.DEVICE_DM836 || devMode == AuxConfig.DeciveModel.DEVICE_DM838 || devMode == AuxConfig.DeciveModel.DEVICE_DM858){
            sourceName = AuxSouceUtils.getSourceNameByID(sourceEntities, srcID);
        }else
        {
            if (srcID < 0xB0)
            {
                sourceName = AuxSouceUtils.getSourceNameByID(sourceEntities, srcID);
            }else
            {
                if (mNetModelEntities == null) {
                    return sourceName;
                }
                AuxNetModelEntity auxNetModelEntity = null;
                if (srcID > 0xB0 && srcID < 0xC0){
                    auxNetModelEntity = AuxNetModelUtils.getNetModelbyID(mNetModelEntities,(srcID - 176));
                    if (auxNetModelEntity == null)
                        return sourceName;
                    sourceName = auxNetModelEntity.getModelName()+"(网络音乐)";
                }else if (srcID > 0xC0 && srcID < 0xD0) {
                    auxNetModelEntity = AuxNetModelUtils.getNetModelbyID(mNetModelEntities,(srcID - 192));
                    if (auxNetModelEntity == null)
                        return sourceName;
                    sourceName = auxNetModelEntity.getModelName()+"(网络电台)";
                }else if (srcID > 0xD0 && srcID < 0xDF) {
                    sourceName = "SD/USB";
                }
                AuxLog.i("getSrcName","sourceName:"+sourceName + "  srcID:"+srcID);
            }
        }
        return sourceName;
    }
}