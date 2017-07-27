package cn.com.auxdio.protocol.net;

/**
 * Created by Auxdio on 2017/3/8 0008.
 */


import android.os.Handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cn.com.auxdio.protocol.bean.AuxDeviceEntity;
import cn.com.auxdio.protocol.bean.AuxNetModelEntity;
import cn.com.auxdio.protocol.bean.AuxNetRadioEntity;
import cn.com.auxdio.protocol.bean.AuxPlayListEntity;
import cn.com.auxdio.protocol.bean.AuxRoomEntity;
import cn.com.auxdio.protocol.bean.AuxSongEntity;
import cn.com.auxdio.protocol.bean.AuxSoundEffectEntity;
import cn.com.auxdio.protocol.bean.AuxSourceEntity;
import cn.com.auxdio.protocol.interfaces.AuxControlActionListener;
import cn.com.auxdio.protocol.interfaces.AuxRadioActionListener;
import cn.com.auxdio.protocol.interfaces.AuxRequestDeviceVersionListener;
import cn.com.auxdio.protocol.interfaces.AuxRequestHighLowPitchListener;
import cn.com.auxdio.protocol.interfaces.AuxRequestNetModelListener;
import cn.com.auxdio.protocol.interfaces.AuxRequestPlayListListener;
import cn.com.auxdio.protocol.interfaces.AuxRequestRadioListener;
import cn.com.auxdio.protocol.interfaces.AuxRequestSongTimeLengthListener;
import cn.com.auxdio.protocol.interfaces.AuxRequestSoundEffectListener;
import cn.com.auxdio.protocol.interfaces.AuxRequestSourceListener;
import cn.com.auxdio.protocol.interfaces.AuxRoomStateChangedListener;
import cn.com.auxdio.protocol.protocol.AuxConfig;
import cn.com.auxdio.protocol.protocol.AuxRequestPackage;
import cn.com.auxdio.protocol.util.AuxDeviceUtils;
import cn.com.auxdio.protocol.util.AuxLog;
import cn.com.auxdio.protocol.util.AuxNetModelUtils;
import cn.com.auxdio.protocol.util.AuxPlayListUtils;
import cn.com.auxdio.protocol.util.AuxRoomUtils;
import cn.com.auxdio.protocol.util.TimeUtils;

/**
 * Udp单播（点对点通信）
 */
public class AuxUdpUnicast {

    private UnicastRunnable mUnicastRunnable;//单播Runnable
    private Timer mTimer;//定时器
    private AuxDeviceEntity mControlDeviceEntity;//控制设备
    private AuxRoomEntity[] mControlRoomEntities;//控制的房间
    private volatile int mPeriod = 2000;//查询房间状态的周期时间
    private int mRoomDropTime = 60 * 1000;//查询房间掉线周期
    private Thread mUnicastThread;

    private AuxRoomStateChangedListener mAuxRoomStateChangedListener;//分区改变监听
    private AuxRequestPlayListListener mQueryMusicListener;//歌曲获取监听
    private AuxRoomStateChangedListener.RoomOnOffListener mRoomOnOffListener;//分区开关机监听
    private AuxRequestSourceListener mAuxRequestSourceListener;//音源列表获取监听
    private AuxControlActionListener.ControlMuteStateListener mMuteStateListener;//静音状态获取监听
    private AuxControlActionListener.ControlPlayModeListener mPlayModeListener;//播放模式状态获取监听
    private AuxControlActionListener.ControlPlayStateListener mPlayStateListener;//播放状态获取监听
    private AuxControlActionListener.ControlProgramNameListener mProgramNameListener;//节目名称获取监听
    private AuxControlActionListener.ControlVolumeListener mVolumeListener;//音量获取监听
    private AuxControlActionListener.SoundEffectListener mSoundEffectListener;//音效查询监听
    private AuxControlActionListener.ControlSourceEntityListener mSrcIDListener;//当前音源查询监听
    private AuxRequestRadioListener mRadioListener;//电台获取监听
    private AuxRadioActionListener mAuxRadioActionListener;//电台操作监听（添加删除）
    private AuxRequestNetModelListener mNetModelListener;//查询网络模块监听
    private AuxRequestDeviceVersionListener mDeviceVersionListener;//设备版本监听
    private AuxRequestHighLowPitchListener mHighLowPitchListener;//高低音监听
    private AuxRequestNetModelListener.NetModelBindTypeListener mNetModelBindTypeListener;//网络模块关联类型监听
    private AuxRequestNetModelListener.NetModelBindListListener mNetModelBindListListener;//网络模块与房间绑定监听
    private AuxRequestNetModelListener.NetModelPlayModeListListener mNetModelPlayModeListListener;//网络模块播放模式监听
    private AuxSourceEntity mAuxCurrentAudioSourceEntity;//当前音源
    private List<AuxSoundEffectEntity> mSoundEffectEntities;
    private AuxRequestSongTimeLengthListener mSongTimeLengthListener;//歌曲长度监听

    private volatile boolean isStop = false;
    private Handler mHandler;

    private static class UDPInit{
        private static final AuxUdpUnicast INSTANCE  = new AuxUdpUnicast();
    }

    public static AuxUdpUnicast getInstance(){
        return UDPInit.INSTANCE;
    }

    public boolean isStop() {
        return isStop;
    }

    private AuxUdpUnicast() {
    }


    public AuxUdpUnicast startWorking()  {
        isStop = false;
        mPeriod = 2000;
        mUnicastRunnable = new UnicastRunnable();
        mUnicastThread = new Thread(mUnicastRunnable);
        mUnicastThread.start();
        mTimer = new Timer();
        mHandler = new Handler();
        return this;
    }

    public AuxUdpUnicast stopWorking()  {
        isStop = true;
        mPeriod = -1;

        if (mUnicastRunnable != null) {
            mUnicastRunnable.onDestory();
            mUnicastRunnable = null;
        }

        if (mUnicastThread != null){
            try {
                Thread.sleep(100);
//                if (mUnicastThread != null)
//                    mUnicastThread.interrupt();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (mTimer != null){
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
        mHandler = null;
        return this;
    }


    /**
     * 查询分区全部状态
     * @param devIP
     */
    private synchronized AuxUdpUnicast requestDeviceRoomState(final String devIP){
        TimerTask mRoomStateTask = new TimerTask() {
            @Override
            public void run() {
                    if (mPeriod > 0){
                        try {
                            AuxRequestPackage.getInstance().queryChannnelState(devIP);
                            checkRoomOnLine();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
            }
        };
        if (mTimer == null){
            mTimer = new Timer();
        }

        if (mPeriod < 0)
            return this;

        mTimer.schedule(mRoomStateTask,500,mPeriod);

        return this;
    }

    private void stopRoomStateRequest(){
        if (mTimer != null){
            mTimer.purge();
        }
    }


    //校验房间在线
    private void checkRoomOnLine() {
        if (mUnicastRunnable == null|| mUnicastRunnable.getChannelEntities() == null || mUnicastRunnable.getChannelEntities().size() == 0) {
            return;
        }

        AuxLog.i("checkRoomOnLine","Roomsize:"+mUnicastRunnable.getChannelEntities().size());

        for (AuxRoomEntity channelEntity : mUnicastRunnable.getChannelEntities()) {
            long millis = System.currentTimeMillis();

            if (channelEntity.getTimeOut() == -1)
                break;

            AuxLog.i("checkRoomOnLine",channelEntity.getRoomName()+"    超时时间:   "+((millis - channelEntity.getTimeOut())/1000)+"s");
            if(millis - channelEntity.getTimeOut() >= mRoomDropTime){
                if (getControlDeviceEntity() == null)
                    return;

                if (AuxUdpBroadcast.getInstace().getBroadCastRunnable() == null)
                    return;

                Map<Integer, List<AuxDeviceEntity>> deviceHashMap = AuxUdpBroadcast.getInstace().getBroadCastRunnable().getDeviceHashMap();

                if (deviceHashMap == null)
                    return;

                mUnicastRunnable.getChannelEntities().remove(channelEntity);
                getAuxRoomStateChangedListener().OnRoomOffLine(channelEntity);//回调房间下线
                if(AuxConfig.DeciveModel.DEVICE_AM8328 == getControlDeviceEntity().getDevModel() || AuxConfig.DeciveModel.DEVICE_AM8318 == getControlDeviceEntity().getDevModel()){
                    deviceHashMap.remove(Integer.valueOf(getControlDeviceEntity().getDevModel()));
                }else {
                    List<AuxDeviceEntity> auxDeviceEntities = deviceHashMap.get(getControlDeviceEntity().getDevModel());

                    if (auxDeviceEntities == null)
                        return;

                    if (auxDeviceEntities.size() == 1)
                        deviceHashMap.remove(Integer.valueOf(getControlDeviceEntity().getDevModel()));
                    else
                    {
                        AuxDeviceEntity devicebyIP = AuxDeviceUtils.getDevicebyIP(auxDeviceEntities, channelEntity.getRoomIP());
                        if (devicebyIP == null)
                            return;
                        auxDeviceEntities.remove(devicebyIP);
                    }

                    for (AuxDeviceEntity entity : auxDeviceEntities) {
                        AuxLog.i("checkRoomOnLine","deviceCount: "+auxDeviceEntities.size()+"   "+entity.toString());
                    }
                }

                if (AuxUdpBroadcast.getInstace().getAuxSreachDeviceListener() != null) {
                    AuxUdpBroadcast.getInstace().getAuxSreachDeviceListener().onSreachDevice(deviceHashMap);
                }

                AuxLog.i("checkRoomOnLine",channelEntity.getRoomName()+"超时了...");
            }
        }
    }

    /**
     * 查询分区全部状态
     */
    public void requestDeviceRoomState(String devIP, AuxRoomStateChangedListener listener){
        setAuxRoomStateChangedListener(listener);
        requestDeviceRoomState(devIP);
    }

    /**
     * 查询分区名称（获取设备全部分区）
     */
    protected AuxUdpUnicast requestDeviceRoomList(String devIP){
        try {
            AuxRequestPackage.getInstance().queryRoomName(devIP);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }


    /**
     * 查询分区名称（获取设备全部分区）,并设置回调监听
     */
    public AuxUdpUnicast requestDeviceRoomList(String devIP, AuxRoomStateChangedListener listener){
        setAuxRoomStateChangedListener(listener);
        requestDeviceRoomList(devIP);
        return this;
    }

    //设置分区名称
    public void setRoomName(AuxRoomEntity auxChannelEntity, String newRoomName){
        try {
            setControlRoomEntities(new AuxRoomEntity[]{auxChannelEntity});
            AuxRequestPackage.getInstance().setRoomName(newRoomName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        requestDeviceRoomList(auxChannelEntity.getRoomIP());
    }


    /**
     * 查询设备目录
     */
    protected AuxUdpUnicast requestDevicePlayList(String devIP){
        try {

            AuxRequestPackage.getInstance().queryContainer(devIP);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }


    /**
     * 查询设备目录,并设置回调目录和音乐信息
     */
    public AuxUdpUnicast requestDevicePlayList(String devIP, AuxRequestPlayListListener listener){
        setQueryMusicListener(listener);
        requestDevicePlayList(devIP);
        return this;
    }


    /**
     * 根据目录查询歌曲信息
     */
    public synchronized void requestMusicListByContentName(String devIP, AuxPlayListEntity auxPlayListEntity){
        try {
            AuxRequestPackage.getInstance().queryMusic(devIP, auxPlayListEntity);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据目录查询歌曲信息,并设置回调监听
     */
    private void requestMusicListByContentName(String devIP, AuxPlayListEntity auxPlayListEntity, AuxRequestPlayListListener listener){
        setQueryMusicListener(listener);
        requestMusicListByContentName(devIP, auxPlayListEntity);
    }

    /**
     * 查询分区开关机状态
     * @return
     * @throws IOException
     */
    private AuxUdpUnicast requestRoomOnOffState(){
        try {
            AuxRequestPackage.getInstance().queryChannelOnOrOff();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * 查询分区开关机状态,并设置回调监听
     */
    public void requestRoomOnOffState(AuxRoomStateChangedListener.RoomOnOffListener onOffListener){
        setRoomOnOffListener(onOffListener);
        requestRoomOnOffState();
    }

    /**
     * 设置分区开关机
     *
     * @param roomEntity
     * @param isOn 开关机状态
     * @return
     * @throws IOException
     */
    public AuxUdpUnicast setRoomOnOffState(AuxRoomEntity roomEntity, boolean isOn){
        try {
            int onOffState;
            if (!isOn)
                onOffState = 0x00;
            else
                onOffState = 0x01;
//            setControlRoomEntities(new AuxRoomEntity[]{roomEntity});
            AuxRequestPackage.getInstance().setChannelOnOrOff(roomEntity,onOffState);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * 查询设备音源列表
     */
    private AuxUdpUnicast requestDeviceSourceList(String devIP){
        if (getControlDeviceEntity().getDevModel() == AuxConfig.DeciveModel.DEVICE_DM838){
            listDM838SorceHandle(devIP);
            return this;
        }

        try {
            AuxRequestPackage.getInstance().queryDeviceSourceName(devIP);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return this;
    }

    private void listDM838SorceHandle(final String devIP) {

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                List<AuxSourceEntity> sourceEntities = new ArrayList<>();
                sourceEntities.add(new AuxSourceEntity(0x81,"本地音乐"));
                sourceEntities.add(new AuxSourceEntity(0xC1,"网络电台"));
                sourceEntities.add(new AuxSourceEntity(0xD1,"网络音乐"));
                sourceEntities.add(new AuxSourceEntity(0xA1,"蓝牙"));
                sourceEntities.add(new AuxSourceEntity(0x51,"辅助输入"));

                getUnicastRunnable().setSourceEntities(sourceEntities);
                if (getAuxRequestSourceListener() != null) {
                    getAuxRequestSourceListener().onSourceList(devIP, sourceEntities);
                }
            }
        },1500);

    }

    /**
     * 查询设备音源列表，并设置回调
     */
    public AuxUdpUnicast requestDeviceSourceList(String devIP, AuxRequestSourceListener listener){
        setAuxRequestSourceListener(listener);
        requestDeviceSourceList(devIP);
        return this;
    }

    //根据音源ID，设置音源别名
    private AuxUdpUnicast setSourceName(AuxSourceEntity sourceEntity, String newName){
        try {
            AuxRequestPackage.getInstance().setDeviceSourceName(sourceEntity.getSourceID(),newName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * 查询音量
     */
    private AuxUdpUnicast requestVolume(AuxRoomEntity[] roomEntities){
        setControlRoomEntities(roomEntities);
        try {
            AuxRequestPackage.getInstance().queryVolume();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    /**
     * 查询音量
     * */
    private AuxUdpUnicast requestVolume(AuxRoomEntity[] roomEntities, AuxControlActionListener.ControlVolumeListener listener){
        setVolumeListener(listener);
        requestVolume(roomEntities);
        return this;
    }

    /**
     * 设置音量
     * @param volume 音量值
     * @throws IOException
     */
    public AuxUdpUnicast setVolume(AuxRoomEntity[] roomEntities, int volume){
        setControlRoomEntities(roomEntities);
        try {
            AuxRequestPackage.getInstance().setVolume(volume);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    //查询分区高低音
    public AuxUdpUnicast requestHighLowPitch(AuxRequestHighLowPitchListener listener){
        setHighLowPitchListener(listener);
        try {
            AuxRequestPackage.getInstance().queryHighLowPitch();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    //设置分区高低音
    public AuxUdpUnicast setHighLowPitch(int highPitchValue,int lowPitchValue) {
        try {
            if (highPitchValue > 10)
                highPitchValue = 10;
            else if (highPitchValue < -10)
                highPitchValue = -10;

            if (lowPitchValue > 10)
                lowPitchValue = 10;
            else if (lowPitchValue < -10)
                lowPitchValue = -10;

            AuxRequestPackage.getInstance().setHighLowPitch(highPitchValue, lowPitchValue);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    //查询音源
    private AuxUdpUnicast requestAudioSource(AuxRoomEntity[] roomEntities){
        setControlRoomEntities(roomEntities);
        try {
            AuxRequestPackage.getInstance().querySrcID();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    //查询音源
    public AuxUdpUnicast requestAudioSource(AuxRoomEntity[] roomEntities, AuxControlActionListener.ControlSourceEntityListener listener){
        setSrcIDListener(listener);
        requestAudioSource(roomEntities);
        return this;
    }

    //设置音源
    public AuxUdpUnicast setAudioSource(AuxRoomEntity[] roomEntities, AuxSourceEntity sourceEntity){
        setControlRoomEntities(roomEntities);
        notifyCurrentAudioSource(sourceEntity);
        try {
            AuxRequestPackage.getInstance().setSrcID(sourceEntity.getSourceID());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    //查询音效列表
    public AuxUdpUnicast requestSoundEffectList(AuxRequestSoundEffectListener listener){
        mSoundEffectEntities = new ArrayList<>();
        if (getControlDeviceEntity().getDevModel() != AuxConfig.DeciveModel.DEVICE_DM836)
            return this;
        mSoundEffectEntities.add(new AuxSoundEffectEntity(0x01,"标准"));
        mSoundEffectEntities.add(new AuxSoundEffectEntity(0x02,"流行"));
        mSoundEffectEntities.add(new AuxSoundEffectEntity(0x03,"古典"));
        mSoundEffectEntities.add(new AuxSoundEffectEntity(0x04,"爵士"));
        mSoundEffectEntities.add(new AuxSoundEffectEntity(0x05,"摇滚"));
        mSoundEffectEntities.add(new AuxSoundEffectEntity(0x06,"人声"));
        mSoundEffectEntities.add(new AuxSoundEffectEntity(0x07,"金属"));
        mSoundEffectEntities.add(new AuxSoundEffectEntity(0x08,"伤感"));
        mSoundEffectEntities.add(new AuxSoundEffectEntity(0x09,"舞曲"));
        mSoundEffectEntities.add(new AuxSoundEffectEntity(0x0A,"自定义"));
        listener.OnSoundEffetList(mSoundEffectEntities);
        return this;
    }

    //查询音效
    private AuxUdpUnicast requestCurrentSoundEffect(AuxRoomEntity[] roomEntities){
        if (getControlDeviceEntity() == null)
            return this;
        if (getControlDeviceEntity().getDevModel() != AuxConfig.DeciveModel.DEVICE_DM836)
            return this;
        setControlRoomEntities(roomEntities);
        try {
            AuxRequestPackage.getInstance().queryRoomInfo(AuxConfig.ResOrReqCommand.CMD_SOUNDTRACK_SET_QUERY);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    //查询音效,并设置回调监听
    public AuxUdpUnicast requestCurrentSoundEffect(AuxRoomEntity[] roomEntities, AuxControlActionListener.SoundEffectListener listener){
        setSoundEffectListener(listener);
        requestCurrentSoundEffect(roomEntities);
        return this;
    }

    //设置音效
    public AuxUdpUnicast setCurrentSoundEffect(AuxRoomEntity[] roomEntities, AuxSoundEffectEntity soundEffect){
        if (getControlDeviceEntity().getDevModel() != AuxConfig.DeciveModel.DEVICE_DM836)
            return this;
        setControlRoomEntities(roomEntities);
        try {
            AuxRequestPackage.getInstance().setRoomInfo(AuxConfig.ResOrReqCommand.CMD_SOUNDTRACK_SET_QUERY,new byte[]{(byte) soundEffect.getSoundID()});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    //查询播放模式
    private AuxUdpUnicast requestPlayMode(AuxRoomEntity[] roomEntities){
        setControlRoomEntities(roomEntities);
        try {
            AuxRequestPackage.getInstance().queryRoomInfo(AuxConfig.ResOrReqCommand.CMD_PLAYMODE_SET_QUERY);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    //查询播放模式
    public AuxUdpUnicast requestPlayMode(AuxRoomEntity[] roomEntities, AuxControlActionListener.ControlPlayModeListener listener){
        setPlayModeListener(listener);
        if (getCurrentSrcID(roomEntities) == -1)
            return this;

        if (getCurrentSrcID(roomEntities) < 0xD0)
            requestPlayMode(roomEntities);
        else{
            List<AuxNetModelEntity> checkAuxNetModelListExist = AuxNetModelUtils.isCheckAuxNetModelListExist(roomEntities);
            if(checkAuxNetModelListExist != null && checkAuxNetModelListExist.size() > 0)
                for (AuxNetModelEntity auxNetModelEntity : checkAuxNetModelListExist) {
                    requestPointPlayMode(auxNetModelEntity);
                }
        }

        return this;
    }

    private int getCurrentSrcID(AuxRoomEntity[] roomEntities) {
        if (mAuxCurrentAudioSourceEntity == null){
            if(!AuxRoomUtils.isControlRoomSame(roomEntities)) {
                AuxLog.e("控制的多个房间，音源不相同，不能操作...");
                return -1;
            }
            return roomEntities[0].getSrcID();
        }
        else
            return mAuxCurrentAudioSourceEntity.getSourceID();

    }

    //设置播放模式
    public AuxUdpUnicast setPlayMode(AuxRoomEntity[] roomEntities, int playMode,AuxControlActionListener.ControlPlayModeListener listener){

        try {
            if (getCurrentSrcID(roomEntities) == -1)
                return this;

            setControlRoomEntities(roomEntities);
            setPlayModeListener(listener);
            if (getCurrentSrcID(roomEntities) > 0xD0){
                List<AuxNetModelEntity> checkAuxNetModelListExist = AuxNetModelUtils.isCheckAuxNetModelListExist(roomEntities);
                if(checkAuxNetModelListExist != null && checkAuxNetModelListExist.size() > 0)
                    for (AuxNetModelEntity auxNetModelEntity : checkAuxNetModelListExist) {
                        setPointPlayMode(auxNetModelEntity,playMode);
                    }
            }else
                AuxRequestPackage.getInstance().setRoomInfo(AuxConfig.ResOrReqCommand.CMD_PLAYMODE_SET_QUERY,new byte[]{(byte) playMode});


        } catch (IOException e) {
            e.printStackTrace();
        }

        return this;
    }

    //查询播放状态
    private AuxUdpUnicast requestPlayState(AuxRoomEntity[] roomEntities){
        setControlRoomEntities(roomEntities);
        try {
            AuxRequestPackage.getInstance().queryRoomInfo(AuxConfig.ResOrReqCommand.CMD_PLAYSTATE_SET_QUERY);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    //查询播放状态
    public AuxUdpUnicast requestPlayState(AuxRoomEntity[] roomEntities, AuxControlActionListener.ControlPlayStateListener listener){
        setPlayStateListener(listener);
        requestPlayState(roomEntities);
        return this;
    }

    //设置播放状态
    public AuxUdpUnicast setPlayState(AuxRoomEntity[] roomEntities, int playState,AuxControlActionListener.ControlPlayStateListener listener){
        setControlRoomEntities(roomEntities);
        setPlayStateListener(listener);
        try {
            AuxRequestPackage.getInstance().setRoomInfo(AuxConfig.ResOrReqCommand.CMD_PLAYSTATE_SET_QUERY,new byte[]{(byte) playState});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    //设置静音状态
    public AuxUdpUnicast setMuteState(AuxRoomEntity[] roomEntities, boolean isMuted){
        setControlRoomEntities(roomEntities);
        byte muteValue;
        if (isMuted)
            muteValue = 0x10;
        else
            muteValue = 0x01;
        try {
            AuxRequestPackage.getInstance().setRoomInfo(AuxConfig.ResOrReqCommand.CMD_MUTESTATE_SET_QUERY,new byte[]{muteValue});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    //上一首
    public AuxUdpUnicast prevProgram(AuxRoomEntity[] roomEntities,AuxControlActionListener.ControlProgramNameListener listener){
        setControlRoomEntities(roomEntities);
        setProgramNameListener(listener);
        try {
            AuxRequestPackage.getInstance().sendDataToRoom(AuxConfig.ResOrReqCommand.CMD_PREORNEXT_REQUEST,new byte[]{0x01});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    //下一首
    public AuxUdpUnicast nextProgram(AuxRoomEntity[] roomEntities,AuxControlActionListener.ControlProgramNameListener listener){
        setControlRoomEntities(roomEntities);
        setProgramNameListener(listener);
        try {
            AuxRequestPackage.getInstance().sendDataToRoom(AuxConfig.ResOrReqCommand.CMD_PREORNEXT_REQUEST,new byte[]{0x10});
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    //查询节目名称
    private AuxUdpUnicast requestProgramName(AuxRoomEntity[] roomEntities){
        setControlRoomEntities(roomEntities);
        try {
            AuxRequestPackage.getInstance().queryRoomInfo(AuxConfig.ResOrReqCommand.CMD_QUERY_PROGRAM);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    //查询节目名称
    public AuxUdpUnicast requestProgramName(AuxRoomEntity[] roomEntities, AuxControlActionListener.ControlProgramNameListener listener){
        setProgramNameListener(listener);
        requestProgramName(roomEntities);
        return this;
    }

    //添加一个网络电台
    public AuxUdpUnicast addNetRadio(AuxNetRadioEntity radioEntity){
        try {
            AuxRequestPackage.getInstance().netRadioOperation((byte) AuxConfig.AddOrDel.REQUEST_ADD,radioEntity.getRadioName(),radioEntity.getRadioAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    //删除一个网络电台
    public AuxUdpUnicast delNetRadio(AuxNetRadioEntity radioEntity){
        try {
            AuxRequestPackage.getInstance().netRadioOperation((byte) AuxConfig.AddOrDel.REQUEST_DEL,radioEntity.getRadioName(),radioEntity.getRadioAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    //请求电台数据
    private AuxUdpUnicast requestRadioData(){
        if (getControlDeviceEntity().getDevModel() == AuxConfig.DeciveModel.DEVICE_DM838 || getControlDeviceEntity().getDevModel() == AuxConfig.DeciveModel.DEVICE_DM858)
            return this;
        new Thread(new Runnable() {
            @Override
            public void run() {
                RadioRequest.getRadioList(AuxNetConstant.RADIO_URL);
            }
        }).start();
        return this;
    }

    //请求电台数据，并设置回调
    public AuxUdpUnicast requestRadioData(AuxRequestRadioListener listener){
        setRadioListener(listener);
        requestRadioData();
        return this;
    }

    //播放网络电台
    public AuxUdpUnicast playRadio(int modelID, AuxNetRadioEntity radioEntity){
        try {
            AuxRequestPackage.getInstance().playRadio(modelID,radioEntity.getRadioName(),radioEntity.getRadioAddress());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    public AuxUdpUnicast playDM858Radio(boolean b, long ablumID, long trackID){
        try {
            AuxRequestPackage.getInstance().playRadio_xima(b?1:0,String.valueOf(ablumID),String.valueOf(trackID));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }
    //播放本地音乐
    public AuxUdpUnicast playSong(AuxSongEntity auxSongEntity){
        try {
            int contentID = auxSongEntity.getContentID();
            if (getUnicastRunnable() == null)
                return this;

            Map<String, List<AuxPlayListEntity>> contentsEntities1 = getUnicastRunnable().getContentsEntities();
            if (contentsEntities1 == null) {
                AuxLog.e("playSong","当前歌曲所在目录不存在...");
                return this;
            }
            List<AuxPlayListEntity> contentsEntities = contentsEntities1.get(getControlRoomEntities()[0].getRoomIP());
            AuxLog.i("playSong","contentsEntities != null    "+(contentsEntities != null)+"   size:"+contentsEntities.size()+"   "+auxSongEntity.toString());
            if (contentsEntities != null && contentsEntities.size() > 0){
                AuxLog.i("playSong",contentsEntities.size()+"   "+contentsEntities.get(0).toString()+"   "+auxSongEntity.toString());
                AuxPlayListEntity contentByID = AuxPlayListUtils.getContentByID(contentID, contentsEntities);
                if (contentByID == null) {
                    AuxLog.e("playSong","contentByID == null");
                    return this;
                }
                AuxLog.i("playSong",""+contentByID.getContentsName()+ auxSongEntity.getSongName());
                String songName = "";
                int devModel = AuxUdpUnicast.getInstance().getControlDeviceEntity().getDevModel();
                if (devModel == AuxConfig.DeciveModel.DEVICE_DM838 || devModel == AuxConfig.DeciveModel.DEVICE_DM858)
                    songName = auxSongEntity.getSongTag();
                else
                    songName = auxSongEntity.getSongName();

                if (getCurrentSrcID(mControlRoomEntities) == -1)
                    return this;
                if(getCurrentSrcID(mControlRoomEntities) > 0xD0){
                    List<AuxNetModelEntity> checkAuxNetModelListExist = AuxNetModelUtils.isCheckAuxNetModelListExist(getControlRoomEntities());
                    if(checkAuxNetModelListExist != null && checkAuxNetModelListExist.size() > 0)
                        for (AuxNetModelEntity auxNetModelEntity : checkAuxNetModelListExist) {
                            pointPlaySong(auxNetModelEntity,auxSongEntity);
                        }
                }else
                    AuxRequestPackage.getInstance().playMusic(contentByID.getContentsName(), songName);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    //查询网络模块工作模式
    private AuxUdpUnicast requestNetModelWorkModel(){
        try {
            if (!isDevice_AM8318_AM8328())
                return this;
            AuxRequestPackage.getInstance().queryNetModelWorkModel();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    //查询网络模块工作模式，并设置回调
    protected AuxUdpUnicast requestNetModelWorkModel(AuxRequestNetModelListener listener){
        setNetModelListener(listener);
        requestNetModelWorkModel();
        return this;
    }

    //设置网络模块工作模式
    private AuxUdpUnicast setNetModelWorkModel(AuxNetModelEntity netModelEntity,int workMode){
        try {
            if (!isDevice_AM8318_AM8328())
                return this;
            AuxRequestPackage.getInstance().setNetModelWorkModel(netModelEntity.getModelID(),workMode);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    //查询网络模块名称
    protected AuxUdpUnicast requestNetModelList(String devIP){
        try {
            if (!isDevice_AM8318_AM8328())
                return this;
            AuxRequestPackage.getInstance().queryNetModelName(devIP);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    //查询网络模块名称,并设置回调
    public AuxUdpUnicast requestNetModelList(String devIP,AuxRequestNetModelListener listener){
        setNetModelListener(listener);
        requestNetModelList(devIP);
        return this;
    }

    //设置网络模块名称
    public AuxUdpUnicast setNetModelName(AuxNetModelEntity auxNetModelEntity,String newModelName){
        try {
            AuxLog.i("setNetModelName","Netmodel length : "+newModelName.getBytes("gb2312").length);
            if (!isDevice_AM8318_AM8328())
                return this;
            if (newModelName.getBytes("gb2312").length < 8)
                AuxRequestPackage.getInstance().setNetModelName(auxNetModelEntity.getModelID(),newModelName.getBytes("gb2312"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    //查询设备版本信息
    private AuxUdpUnicast requestDeviceVersion(String devIP){
        try {
            AuxRequestPackage.getInstance().queryDeviceVersion(devIP);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    //查询设备版本信息,并设置回调
    public AuxUdpUnicast requestDeviceVersion(String devIP,AuxRequestDeviceVersionListener listener){
        setDeviceVersionListener(listener);
        requestDeviceVersion(devIP);
        return this;
    }

    //查询网络模块关联类型
    private AuxUdpUnicast requestNetModelRelevanceType(){
        try {
            if (!isDevice_AM8318_AM8328())
                return this;
            AuxRequestPackage.getInstance().queryModelRelevanceType();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    //查询网络模块关联类型，并回调
    public AuxUdpUnicast requestNetModelRelevanceType(AuxRequestNetModelListener.NetModelBindTypeListener listener){
        setNetModelBindTypeListener(listener);
        requestNetModelRelevanceType();
        return this;
    }

    //设置网络模块关联类型
    public AuxUdpUnicast setNetModelRelevanceType(int type){
        if (type > 3 || type < 1)
            return this;
        if (!isDevice_AM8318_AM8328())
            return this;
        try {
            AuxRequestPackage.getInstance().setNetModelRelevanceType(type);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    //查询全部房间绑定的模块
    public AuxUdpUnicast requestBindAllRoomForNetModel(AuxRequestNetModelListener.NetModelBindListListener listener) {
        setNetModelBindListListener(listener);
        requestBindAllRoomForNetModel();
        return this;
    }

    //查询全部房间绑定的模块,并设置回调
    protected AuxUdpUnicast requestBindAllRoomForNetModel(){
        try {
            if (!isDevice_AM8318_AM8328())
                return this;
            AuxRequestPackage.getInstance().queryBindAllRoomForNetModel();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    //设置模块绑定房间
    public AuxUdpUnicast setBindRoomforNetModel(AuxRoomEntity auxRoomEntity,AuxNetModelEntity auxNetModelEntity){
        try {
            if (!isDevice_AM8318_AM8328())
                return this;
            AuxRequestPackage.getInstance().setbindRoomforNetModel(auxRoomEntity,auxNetModelEntity.getModelID());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    //查询点播模式
    private AuxUdpUnicast requestPointPlayMode(AuxNetModelEntity auxNetModelEntity){
        try {
            if (!isDevice_AM8318_AM8328())
                return this;
            AuxRequestPackage.getInstance().queryPointPlayMode(auxNetModelEntity.getModelID());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    //设置点播模式
    private AuxUdpUnicast setPointPlayMode(AuxNetModelEntity auxNetModelEntity,int playMode){
        try {
            if (!isDevice_AM8318_AM8328())
                return this;
            AuxRequestPackage.getInstance().setPointPlayMode(auxNetModelEntity.getModelID(),playMode);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    //点播歌曲
    private AuxUdpUnicast pointPlaySong(AuxNetModelEntity auxNetModelEntity,AuxSongEntity auxSongEntity){
        try {
            if (!isDevice_AM8318_AM8328())
                return this;
            if (auxSongEntity == null)
                return this;
            int contentID = auxSongEntity.getContentID();
            if (getUnicastRunnable() == null || getUnicastRunnable().getContentsEntities() == null)
                return this;
            Map<String, List<AuxPlayListEntity>> contentsEntities = getUnicastRunnable().getContentsEntities();
            List<AuxPlayListEntity> auxPlayListEntities = contentsEntities.get(getControlDeviceEntity().getDevIP());
            AuxPlayListEntity contentByID = AuxPlayListUtils.getContentByID(contentID, auxPlayListEntities);
            if (contentByID != null && contentByID.getContentsName() != null)
                AuxRequestPackage.getInstance().pointPlaySong(auxNetModelEntity.getModelID(),contentByID.getContentsName(),auxSongEntity.getSongName());
            else
                AuxLog.e("pointPlaySong","目录为空...");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    private boolean isDevice_AM8318_AM8328(){
        if (getControlDeviceEntity() != null) {
            if(getControlDeviceEntity().getDevModel() == AuxConfig.DeciveModel.DEVICE_AM8318 || getControlDeviceEntity().getDevModel() == AuxConfig.DeciveModel.DEVICE_AM8328){
                return true;
            }
        }
        return false;
    }
    private boolean isDevice_DM838_DM836(){
        if (getControlDeviceEntity() != null) {
            int devModel = getControlDeviceEntity().getDevModel();
            if(devModel == AuxConfig.DeciveModel.DEVICE_DM838 || devModel == AuxConfig.DeciveModel.DEVICE_DM836 || devModel == AuxConfig.DeciveModel.DEVICE_DM858){
                return true;
            }
        }
        return false;
    }

    public int getRequestRoomStatePeriod() {
        return mPeriod;
    }

    public AuxUdpUnicast setRequestRoomStatePeriod(int period) {
        if (period < 1000)
            period = 1000;

        if (period > 5000)
            mPeriod = 5000;

        mPeriod = period;
        return this;
    }

    public AuxDeviceEntity getControlDeviceEntity() {
        return mControlDeviceEntity;
    }

    public AuxUdpUnicast setControlDeviceEntity(AuxDeviceEntity controlDeviceEntity) {
        mControlDeviceEntity = controlDeviceEntity;
        if (controlDeviceEntity != null) {
            requestDeviceVersion(mControlDeviceEntity.getDevIP());
        }
        return this;
    }

    public AuxUdpUnicast setCurrentPlayPosition(AuxRoomEntity roomEntity, int currentPosition){
        if (!isDevice_DM838_DM836())
            return this;
        setControlRoomEntities(new AuxRoomEntity[]{roomEntity});

        AuxRequestPackage.getInstance().setPlayTime(TimeUtils.getSongTimeByte(0),TimeUtils.getSongTimeByte(currentPosition));
        return this;
    }

    public AuxUdpUnicast requestCurrentPlayPosition(AuxRoomEntity roomEntity, AuxRequestSongTimeLengthListener listener){
        if (!isDevice_DM838_DM836())
            return this;
        setSongTimeLengthListener(listener);
        setControlRoomEntities(new AuxRoomEntity[]{roomEntity});
        try {
            AuxRequestPackage.getInstance().queryRoomInfo(AuxConfig.ResOrReqCommand.CMD_QUERY_PLAYTIME);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return this;
    }

    ///////////////////////////////设置监听器与获取监听器//////////////////////////////////////////////////////////////

    public AuxRoomStateChangedListener getAuxRoomStateChangedListener() {
        return mAuxRoomStateChangedListener;
    }

    public AuxUdpUnicast setAuxRoomStateChangedListener(AuxRoomStateChangedListener auxRoomStateChangedListener) {
        mAuxRoomStateChangedListener = auxRoomStateChangedListener;
        return this;
    }

    public AuxRequestPlayListListener getQueryMusicListener() {
        return mQueryMusicListener;
    }

    public AuxUdpUnicast setQueryMusicListener(AuxRequestPlayListListener queryMusicListener) {
        mQueryMusicListener = queryMusicListener;
        return this;
    }

    public AuxRoomStateChangedListener.RoomOnOffListener getRoomOnOffListener() {
        return mRoomOnOffListener;
    }

    public AuxUdpUnicast setRoomOnOffListener(AuxRoomStateChangedListener.RoomOnOffListener roomOnOffListener) {
        mRoomOnOffListener = roomOnOffListener;
        return this;
    }

    public AuxRequestSourceListener getAuxRequestSourceListener() {
        return mAuxRequestSourceListener;
    }

    public AuxUdpUnicast setAuxRequestSourceListener(AuxRequestSourceListener auxRequestSourceListener) {
        mAuxRequestSourceListener = auxRequestSourceListener;
        return this;
    }

    public AuxControlActionListener.ControlMuteStateListener getMuteStateListener() {
        return mMuteStateListener;
    }

    public AuxUdpUnicast setMuteStateListener(AuxControlActionListener.ControlMuteStateListener muteStateListener) {
        mMuteStateListener = muteStateListener;
        return this;
    }

    public AuxControlActionListener.ControlPlayModeListener getPlayModeListener() {
        return mPlayModeListener;
    }

    public AuxUdpUnicast setPlayModeListener(AuxControlActionListener.ControlPlayModeListener playModelListener) {
        mPlayModeListener = playModelListener;
        return this;
    }

    public AuxControlActionListener.ControlPlayStateListener getPlayStateListener() {
        return mPlayStateListener;
    }

    private AuxUdpUnicast setPlayStateListener(AuxControlActionListener.ControlPlayStateListener playStateListener) {
        mPlayStateListener = playStateListener;
        return this;
    }

    public AuxControlActionListener.ControlProgramNameListener getProgramNameListener() {
        return mProgramNameListener;
    }

    public AuxUdpUnicast setProgramNameListener(AuxControlActionListener.ControlProgramNameListener programNameListener) {
        mProgramNameListener = programNameListener;
        return this;
    }

    public AuxControlActionListener.ControlVolumeListener getVolumeListener() {
        return mVolumeListener;
    }

    public AuxUdpUnicast setVolumeListener(AuxControlActionListener.ControlVolumeListener volumeListener) {
        mVolumeListener = volumeListener;
        return this;
    }

    public AuxControlActionListener.SoundEffectListener getSoundEffectListener() {
        return mSoundEffectListener;
    }

    public AuxUdpUnicast setSoundEffectListener(AuxControlActionListener.SoundEffectListener soundEffectListener) {
        mSoundEffectListener = soundEffectListener;
        return this;
    }

    public AuxControlActionListener.ControlSourceEntityListener getSrcIDListener() {
        return mSrcIDListener;
    }

    public AuxUdpUnicast setSrcIDListener(AuxControlActionListener.ControlSourceEntityListener srcIDListener) {
        mSrcIDListener = srcIDListener;
        return this;
    }

    public AuxRequestRadioListener getRadioListener() {
        return mRadioListener;
    }

    public AuxUdpUnicast setRadioListener(AuxRequestRadioListener radioListener) {
        mRadioListener = radioListener;
        return this;
    }

    public AuxRadioActionListener getAuxRadioActionListener() {
        return mAuxRadioActionListener;
    }

    public AuxUdpUnicast setAuxRadioActionListener(AuxRadioActionListener auxRadioActionListener) {
        mAuxRadioActionListener = auxRadioActionListener;
        return this;
    }

    public AuxRequestNetModelListener getNetModelListener() {
        return mNetModelListener;
    }

    public AuxUdpUnicast setNetModelListener(AuxRequestNetModelListener netModelListener) {
        mNetModelListener = netModelListener;
        return this;
    }

    public AuxRequestDeviceVersionListener getDeviceVersionListener() {
        return mDeviceVersionListener;
    }

    public void setDeviceVersionListener(AuxRequestDeviceVersionListener deviceVersionListener) {
        mDeviceVersionListener = deviceVersionListener;
    }

    public AuxRequestHighLowPitchListener getHighLowPitchListener() {
        return mHighLowPitchListener;
    }

    public void setHighLowPitchListener(AuxRequestHighLowPitchListener highLowPitchListener) {
        mHighLowPitchListener = highLowPitchListener;
    }

    public AuxRequestNetModelListener.NetModelBindTypeListener getNetModelBindTypeListener() {
        return mNetModelBindTypeListener;
    }

    public void setNetModelBindTypeListener(AuxRequestNetModelListener.NetModelBindTypeListener netModelBindTypeListener) {
        mNetModelBindTypeListener = netModelBindTypeListener;
    }

    public AuxRequestNetModelListener.NetModelBindListListener getNetModelBindListListener() {
        return mNetModelBindListListener;
    }

    public void setNetModelBindListListener(AuxRequestNetModelListener.NetModelBindListListener netModelBindListListener) {
        mNetModelBindListListener = netModelBindListListener;
    }

    public AuxRequestNetModelListener.NetModelPlayModeListListener getNetModelPlayModeListListener() {
        return mNetModelPlayModeListListener;
    }

    public void setNetModelPlayModeListListener(AuxRequestNetModelListener.NetModelPlayModeListListener netModelPlayModeListListener) {
        mNetModelPlayModeListListener = netModelPlayModeListListener;
    }

    public AuxRequestSongTimeLengthListener getSongTimeLengthListener() {
        return mSongTimeLengthListener;
    }

    public void setSongTimeLengthListener(AuxRequestSongTimeLengthListener songTimeLengthListener) {
        mSongTimeLengthListener = songTimeLengthListener;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public UnicastRunnable getUnicastRunnable() {
        return mUnicastRunnable;
    }

    private AuxUdpUnicast setUnicastRunnable(UnicastRunnable unicastRunnable) {
        mUnicastRunnable = unicastRunnable;
        return this;
    }

    public Timer getTimer() {
        return mTimer;
    }

    private void setTimer(Timer timer) {
        mTimer = timer;
    }

    public AuxRoomEntity[] getControlRoomEntities() {
        return mControlRoomEntities;
    }

    public AuxUdpUnicast setControlRoomEntities(AuxRoomEntity[] controlRoomEntities) {
        mControlRoomEntities = controlRoomEntities;
        return this;
    }

    private AuxUdpUnicast notifyCurrentAudioSource(AuxSourceEntity sourceEntity) {
        mAuxCurrentAudioSourceEntity = sourceEntity;
        return this;
    }

    public List<AuxSoundEffectEntity> getSoundEffectEntities() {
        return mSoundEffectEntities;
    }

    public int getRoomDropTime() {
        return mRoomDropTime;
    }

    public AuxUdpUnicast setRoomDropTime(int roomDropTime) {
        if (roomDropTime < 10 * 1000)
            mRoomDropTime = 10 * 1000;
        mRoomDropTime = roomDropTime;
        return this;
    }


}
