package cn.com.auxdio.protocol.net;

import android.util.Log;

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

import cn.com.auxdio.protocol.bean.AuxDeviceEntity;
import cn.com.auxdio.protocol.bean.AuxNetModelEntity;
import cn.com.auxdio.protocol.bean.AuxRoomEntity;
import cn.com.auxdio.protocol.bean.AuxSoundEffectEntity;
import cn.com.auxdio.protocol.protocol.AuxConfig;
import cn.com.auxdio.protocol.util.AuxByteToStringUtils;
import cn.com.auxdio.protocol.util.AuxDeviceUtils;
import cn.com.auxdio.protocol.util.AuxLog;
import cn.com.auxdio.protocol.util.AuxNetModelUtils;
import cn.com.auxdio.protocol.util.AuxSouceUtils;

import static cn.com.auxdio.protocol.net.AuxUdpBroadcast.getInstace;


/**
 * Created by Auxdio on 2017/3/8 0008.
 */

public class BroadCastRunnable implements Runnable {

    private DatagramSocket datagramSocket = null;
    private Map<Integer,List<AuxDeviceEntity>> mDeviceHashMap;

    protected BroadCastRunnable(){
        try {
            init();
        } catch (SocketException e) {
            e.printStackTrace();
            AuxLog.e("","...........datagramSocket == null..."+e.getMessage());
        }
    }

    private void init() throws SocketException {
        mDeviceHashMap = new Hashtable<>();

        datagramSocket = new DatagramSocket(null);
        datagramSocket.setReuseAddress(true);
        datagramSocket.bind(new InetSocketAddress(AuxNetConstant.BROADCAST_PORT));
//        datagramSocket.connect(new InetSocketAddress(AuxNetConstant.BROADCAST_PORT));
//        datagramSocket.setSoTimeout(AuxNetConstant.TIME_OUT);
    }

    protected void onDestory(){
        mDeviceHashMap.clear();
        if (datagramSocket != null) {
//            datagramSocket.disconnect();
//            datagramSocket.close();
//            datagramSocket = null;
        }
    }

    public Map<Integer, List<AuxDeviceEntity>> getDeviceHashMap() {
        return mDeviceHashMap;
    }

    /**
     * 发送数据到设备
     * @param sendData
     * @param len
     * @throws IOException
     */
    public void sendDataToDevice(byte[] sendData,int len) throws IOException {

//        AuxLog.i("BroadCastRunnable","sendDataToDevice:"+sendData[0]+" "+sendData[1]);
        DatagramPacket datagramPacket = new DatagramPacket(sendData,len,
                InetAddress.getByName(AuxNetConstant.BROADCAST_ADRESS), AuxNetConstant.UICAST_PORT);
        if (datagramPacket != null && datagramSocket != null){
            AuxLog.i("sendDataToDevice","sendDataToDevice:"+ AuxByteToStringUtils.bytesToHexString(sendData,len));
            datagramSocket.send(datagramPacket);
        }
        else
            AuxLog.e("sendDataToDevice",".............datagramPacket == null or datagramSocket == null.................");

    }

    @Override
    public void run() {

        while (!getInstace().isStop()){
            byte[] bytes = new byte[AuxNetConstant.MAX_BUFFER];
            DatagramPacket datagramPacket = new DatagramPacket(bytes,bytes.length);
            try {
                if (datagramSocket != null){
                    datagramSocket.receive(datagramPacket);

                    String hostAddress = datagramPacket.getAddress().getHostAddress();
                    int port = datagramPacket.getPort();
//                    AuxNetConstant.sDeviceIpAddress = hostAddress;
                    AuxLog.i("BroadCastRunnable","hostAddress:"+hostAddress+"   port:"+port);

                    byte[] data = datagramPacket.getData();
                    int length = datagramPacket.getLength();
                    if (length > 0 ) {
                        handleAuxdioCmd(hostAddress, data, length);
                    }else
                        AuxLog.e("","接受数据为空...");
                }
            } catch (IOException e) {
                e.printStackTrace();
//                AuxLog.e("run","广播是否停止："+(Thread.interrupted()));
            }
        }

    }

    /**
     * 处理协议命令
     * @param hostAddress
     * @param data
     * @param length
     */
    private void handleAuxdioCmd(String hostAddress, byte[] data, int length) {

        AuxLog.i("handleAuxdioCmd","BroadCastRunnable---data:"+ AuxByteToStringUtils.bytesToHexString(data, length));
        switch(AuxConfig.combineCommand(data[0], data[1])) {
            case AuxConfig.ResOrReqCommand.CMD_DEVICE_ON: //新设备上线
                AuxLog.i("handleAuxdioCmd", "新设备上线!设备应答....");

            case AuxConfig.ResOrReqCommand.CMD_SEARCH_HOST_RESPONSE://搜索到设备应答
                AuxLog.i("handleAuxdioCmd", "UDP BroadCast find device!");
                try {
                    listDeviceHandle(hostAddress,data);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;

            case AuxConfig.ResOrReqCommand.CMD_SRCCHANGE_BROADCAST://0x25设备节目源主动广播
                try {
                    deviceStatusChange(hostAddress,data);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                break;
            case AuxConfig.ResOrReqCommand.CMD_PLAYMODECHANGE_RES://播放模式改变广播
                int srcID = data[9] & 0xFF;
                AuxSouceUtils.callBackPlayMode(srcID,data[10] & 0xFF);
                break;

            case AuxConfig.ResOrReqCommand.CMD_USB_STATE_RES://usb插入拔出广播
                boolean isChanged = ((data[9] & 0xFF) == 0x01);
                if (isChanged)
                    AuxUdpUnicast.getInstance().requestDevicePlayList(hostAddress);
                Log.i("CMD_USB_STATE_RES", "handleAuxdioCmd: "+isChanged+", "+(getInstace().getAuxUSBSDChangedListener()));
                if (getInstace().getAuxUSBSDChangedListener() != null) {
                    getInstace().getAuxUSBSDChangedListener().onUSBChanged(isChanged);
                }
                break;

            case AuxConfig.ResOrReqCommand.CMD_RADIO_STATE://电台连接状态广播
                if (getInstace().getRadioConnectListener() != null) {
                    getInstace().getRadioConnectListener().onConnectState(data[9] & 0xFF);
                }
                break;

            case AuxConfig.ResOrReqCommand.CMD_EQ_STATE://EQ(音效)改变时广播
                if (getInstace().getSoundEffectChangedListener() != null) {
                    List<AuxSoundEffectEntity> soundEffectEntities = AuxUdpUnicast.getInstance().getSoundEffectEntities();
                    if (soundEffectEntities != null){
                        AuxSoundEffectEntity auxSoundEffectEntity = AuxSouceUtils.getSoundEffectByID(soundEffectEntities, data[9] & 0xFF);
                        getInstace().getSoundEffectChangedListener().onSoundEffectChanged(hostAddress,auxSoundEffectEntity);
                    }
                }
                break;

            case AuxConfig.ResOrReqCommand.CMD_SD_CHANGED://SD卡歌曲变化，设备主动广播

                if (getInstace().getAuxUSBSDChangedListener() != null)
                    getInstace().getAuxUSBSDChangedListener().onSDChanged("SD卡歌曲改变了...");

                Log.i("CMD_USB_STATE_RES", "handleAuxdioCmd: "+"CMD_SD_CHANGED"+", "+(getInstace().getAuxUSBSDChangedListener()));
                AuxUdpUnicast.getInstance().requestDevicePlayList(hostAddress);
                break;

            case AuxConfig.ResOrReqCommand.CMD_NETMODE_BROADCAST://网络模块改变主动广播
                netModelChanged(data);
                break;

            case AuxConfig.ResOrReqCommand.CMD_CUSTOM_CHANELMODEL_ATTR_QUERY_SET://分区模块关联改变主动广播
                AuxUdpUnicast.getInstance().getUnicastRunnable().handleRoomModelBind(data);
                break;

            case AuxConfig.ResOrReqCommand.CMD_NETPLAY_MODEL_BROADCAST://点播模式改变主动广播
                if (AuxUdpUnicast.getInstance().getControlDeviceEntity() == null)
                    return;
                if (hostAddress.equals(AuxUdpUnicast.getInstance().getControlDeviceEntity().getDevIP()))
                    pointPlayChanged(data);
                break;


        }
    }

    //点播模式主动改变广播
    private void pointPlayChanged(byte[] data) {
        int index = 8;
        int modelCount = (data[index++] & 0xFF)/2;
        for (int i = 0; i < modelCount; i++) {
            int modelID = data[index++] & 0xFF;
            int modeValue = data[index++] & 0xFF;
            AuxLog.i("pointPlayChanged","modelID:"+modelID+"   modeValue:"+modeValue);

            /*
            AuxRoomEntity roomEntityByModelId = AuxNetModelUtils.getRoomEntityByModelId(modelID);
//            AuxLog.e("pointPlayChanged","roomEntityByModelId != null   "+(roomEntityByModelId != null) +"   "+roomEntityByModelId.getSrcID());
            if (roomEntityByModelId != null){
                AuxLog.i("pointPlayChanged","roomEntityByModelId:"+roomEntityByModelId.toString());
                if (roomEntityByModelId.getSrcID() > 0xD0) {

                }
            }
            */
            AuxSouceUtils.callBackPlayModel(modelID, modeValue);
        }

    }



    //网络模块改变
    private void netModelChanged(byte[] data) {
        int index = 9;
        int modelCount = data[index++];
        if (AuxUdpUnicast.getInstance().getUnicastRunnable() == null)
            return;
        List<AuxNetModelEntity> netModelEntities = AuxUdpUnicast.getInstance().getUnicastRunnable().getNetModelEntities();
        if (netModelEntities == null)
            return;
        for (int i = 0; i < modelCount; i++) {
            int modelID = data[index++];
            int workMode = data[index++];
            AuxNetModelEntity netModelbyID = AuxNetModelUtils.getNetModelbyID(netModelEntities, modelID);
            if (netModelbyID != null)
                netModelbyID.setWorkMode(workMode);
        }

        if (AuxUdpUnicast.getInstance().getNetModelListener() != null)
            AuxUdpUnicast.getInstance().getNetModelListener().onNetModelList(netModelEntities);

    }

    /**
     * 设备状态改变
     * @param hostAddress
     * @param data
     */
    private void deviceStatusChange(String hostAddress, byte[] data) throws UnsupportedEncodingException {
        int  roomID = data[7] & 0xFF;//分区ID
        int index= 9;
        int srcID = data[index++] & 0xFF;//音源ID
        int programNameLen = data[index++];

        byte[] bytes = new byte[programNameLen];
        System.arraycopy(data,index,bytes,0,programNameLen);

        String programName = new String(bytes,"gbk");//音源名称
        index+=programNameLen;
        int playState = data[index];//播放状态

//        AuxProgramStateEntity auxProgramStateEntity = new AuxProgramStateEntity(roomID,srcID);
//        auxProgramStateEntity.setPlayState(playState);
//        auxProgramStateEntity.setProgramName(programName);
//        auxProgramStateEntity.setRoomIP(hostAddress);

        AuxRoomEntity[] auxControlRoomEntities = AuxUdpUnicast.getInstance().getControlRoomEntities();
        if (auxControlRoomEntities == null)
            return;

        AuxLog.i("deviceStatusChange","srcID:"+srcID+"   playState:"+playState+"   programName:"+programName+"   hostAddress:"+hostAddress);
        for (int i = 0; i < auxControlRoomEntities.length; i++) {
            if (hostAddress.equals(auxControlRoomEntities[i].getRoomIP())){
                if (auxControlRoomEntities[i].getSrcID() == srcID){
                    AuxSouceUtils.callBackPlayState(srcID,playState);
                    AuxSouceUtils.callBackProgramName(srcID,programName);
                }
            }
        }
    }

    /**
     * 获取设备信息
     * @param hostAddress
     * @param data
     */
    protected synchronized void listDeviceHandle(String hostAddress, byte[] data) throws UnsupportedEncodingException {
        if (data[8] == 0x00)
            return;
        AuxDeviceEntity auxDeviceEntity = new AuxDeviceEntity();
        auxDeviceEntity.setDevModel(AuxConfig.getDeveiceIDResponse(data[3], data[4]));//设备类型（模式）
        auxDeviceEntity.setDevID(data[4] & 0x0F);//设备识别码
        auxDeviceEntity.setDevIP(hostAddress);//ip
        auxDeviceEntity.setDevZoneOrGroup(data[6] & 0x01);//组/分区标识
        //获取设备别名
        int index = 9;
        int deviceNameLen = data[index++];
        byte[] nameByte = new byte[deviceNameLen];//data[9]负载数据  命令 十六进制
        System.arraycopy(data,index, nameByte, 0, deviceNameLen);//复制从一个数组到另一个数组
        String deviceName = new String(nameByte, 0, deviceNameLen,"gbk");//将数组转换成字符串

        auxDeviceEntity.setDevName(deviceName);//设备别名

        index+=deviceNameLen;

        int ipLength = data[index++];//获取IP地址长度
        byte[] bytes = new byte[ipLength];
        System.arraycopy(data,index, bytes, 0, ipLength);
        String deviceIP = new String(bytes, 0, ipLength,"gbk");

        index += ipLength;

        index += 6;
        StringBuilder macIBuilder = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            macIBuilder.append(":");
            macIBuilder.append(Integer.toHexString(data[index++] & 0xFF));
        }
        String s = macIBuilder.toString();
        auxDeviceEntity.setDevMAC(s.substring(1,s.length()));

        AuxLog.i("listDeviceHandle","auxdio---DeviceName:"+ auxDeviceEntity.getDevName()+"   DeviceIP:"+ auxDeviceEntity.getDevIP()+"   DeviceMac:"+ auxDeviceEntity.getDevMAC()
                +"   deviceIP:"+deviceIP);

        callBackDeviceHandle(auxDeviceEntity);


//
//        if (AuxUdpBroadcast.getInstace().getAuxSreachDeviceListener() != null) {
//            AuxUdpBroadcast.getInstace().getAuxSreachDeviceListener().onSreachDevice(auxDeviceEntity);
//        }


    }

    private void callBackDeviceHandle(AuxDeviceEntity deviceEntity){
        boolean isUpData = false;
        if (mDeviceHashMap.size() == 0) {
            isUpData = addDeviceUpdat(deviceEntity);
        }else
        {
            boolean deviceModelExist = AuxDeviceUtils.isDeviceModelExist(mDeviceHashMap.keySet(), deviceEntity);//判断设备模式是否存在
            if (!deviceModelExist){
                isUpData = addDeviceUpdat(deviceEntity);
            }
            else
            {
                if (deviceEntity.getDevModel() == AuxConfig.DeciveModel.DEVICE_DM836 || deviceEntity.getDevModel() == AuxConfig.DeciveModel.DEVICE_DM838 || deviceEntity.getDevModel() == AuxConfig.DeciveModel.DEVICE_DM858){
                    List<AuxDeviceEntity> deviceEntities = mDeviceHashMap.get(Integer.valueOf(deviceEntity.getDevModel()));
                    boolean deviceIPExist = AuxDeviceUtils.isDeviceIPExist(deviceEntities, deviceEntity);//判断设备IP是否存在
                    if (!deviceIPExist){
                        deviceEntities.add(deviceEntity);
                        //查询新上线的设备房间名称
                        if (AuxUdpUnicast.getInstance().getControlDeviceEntity() != null && AuxUdpUnicast.getInstance().getControlDeviceEntity().getDevModel() == deviceEntity.getDevModel())
                            AuxUdpUnicast.getInstance().requestDeviceRoomList(deviceEntity.getDevIP());
                        isUpData = true;
                    }
                }
            }
        }

        AuxLog.i("listDeviceHandle","callBackDeviceHandle---:"+ mDeviceHashMap.size()+"   DeviceName:"+deviceEntity.getDevName()+"   DeviceIP:"+ deviceEntity.getDevIP()+"   DeviceMac:"+ deviceEntity.getDevMAC());

        for (Integer integer : mDeviceHashMap.keySet()) {
            List<AuxDeviceEntity> deviceEntities = mDeviceHashMap.get(integer);
            for (AuxDeviceEntity entity : deviceEntities) {
                AuxLog.i("handleDeviceList",mDeviceHashMap.keySet().size()+"   isUpData: - "+isUpData+"   "+entity.getDevName()+"("+entity.getDevIP()+")   deviceModel:"+entity.getDevModel()+"   deviceModel(key):"+integer+"   deviceCount:"+deviceEntities.size());
            }
        }

        if (isUpData) {
            if (getInstace().getAuxSreachDeviceListener() != null) {
                getInstace().getAuxSreachDeviceListener().onSreachDevice(mDeviceHashMap);
            }
        }
    }

    private boolean addDeviceUpdat(AuxDeviceEntity deviceEntity) {
        ArrayList<AuxDeviceEntity> deviceEntities = new ArrayList<>();
        deviceEntities.add(deviceEntity);
        mDeviceHashMap.put(deviceEntity.getDevModel(),deviceEntities);
        return true;
    }

}
