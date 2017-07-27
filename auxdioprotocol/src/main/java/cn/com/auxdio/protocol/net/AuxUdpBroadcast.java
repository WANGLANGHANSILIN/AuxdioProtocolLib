package cn.com.auxdio.protocol.net;

/**
 * Created by Auxdio on 2017/3/8 0008.
 */

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import cn.com.auxdio.protocol.interfaces.AuxUSB_SDChangedListener;
import cn.com.auxdio.protocol.interfaces.AuxRadioActionListener;
import cn.com.auxdio.protocol.interfaces.AuxSreachDeviceListener;
import cn.com.auxdio.protocol.protocol.AuxRequestPackage;

/**
 * Udp广播
 */
public class AuxUdpBroadcast {

    private BroadCastRunnable mBroadCastRunnable;
    private Thread mBroadCastThread;

    private AuxSreachDeviceListener mAuxSreachDeviceListener;//搜索设备监听
    private AuxUSB_SDChangedListener mAuxUSBSDChangedListener;//设备状态改变监听---USB插入拨出、播放模式改变
    private AuxUSB_SDChangedListener.SoundEffectChangedListener mSoundEffectChangedListener;//音效改变监听
    private AuxRadioActionListener.RadioConnectListener mRadioConnectListener;//电台连接状态监听

    private Timer mTimer;
    private boolean isStop = false;
    /*几秒查询搜索一次*/
    private volatile int mPeriod = 1000-1;

    private static class UDPInit{
        private static final AuxUdpBroadcast INSTANCE = new AuxUdpBroadcast();
    }

    //获取实例
    public static AuxUdpBroadcast getInstace(){
        return UDPInit.INSTANCE;
    }


    private AuxUdpBroadcast() {
    }

    public boolean isStop() {
        return isStop;
    }

    /**
     * 开始广播
     */
    public AuxUdpBroadcast startWorking(){
        isStop = false;
        mPeriod = 2;
        mBroadCastRunnable = new BroadCastRunnable();
        mBroadCastThread = new Thread(mBroadCastRunnable);
        mBroadCastThread.start();
        mTimer = new Timer();
        return this;
    }

    public AuxUdpBroadcast stopWorking(){
        isStop = true;
        mPeriod = -1;

        if (mBroadCastRunnable != null){
            mBroadCastRunnable.onDestory();
            mBroadCastRunnable = null;
        }

        if (mBroadCastThread != null){
            try {
                Thread.sleep(100);
//                if (mBroadCastThread != null)
//                    mBroadCastThread.interrupt();

            } catch (InterruptedException e) {
                e.printStackTrace();
//                mBroadCastThread = null;
            }
        }

        if (mTimer != null){
            mTimer.cancel();
            mTimer.purge();
            mTimer = null;
        }
        return this;
    }

    /**
     * 搜索设备
     */
    public synchronized AuxUdpBroadcast searchDevice(){
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                if(mPeriod > 0) {
                    try {
                        AuxRequestPackage.getInstance().searchDevice();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        if (mTimer == null)
            mTimer = new Timer();

        if (mPeriod >= 1000)
            mTimer.schedule(task,mPeriod,mPeriod);
        else if(mPeriod < 0) {
            return this;
        }else
            mTimer.schedule(task,1000);

        return this;
    }

    /**
     * 搜索设备,并设置回调监听
     */
    public AuxUdpBroadcast searchDevice(AuxSreachDeviceListener listener){
        setSreachDeviceListener(listener);
        searchDevice();
        return this;
    }

    public AuxSreachDeviceListener getAuxSreachDeviceListener() {
        return mAuxSreachDeviceListener;
    }

    public AuxUdpBroadcast setSreachDeviceListener(AuxSreachDeviceListener listener) {
        mAuxSreachDeviceListener = listener;
        return this;
    }

    public AuxUSB_SDChangedListener getAuxUSBSDChangedListener() {
        return mAuxUSBSDChangedListener;
    }

    public AuxUdpBroadcast setUSBSDChangedListener(AuxUSB_SDChangedListener listener) {
        mAuxUSBSDChangedListener = listener;
        return this;
    }

    public AuxUSB_SDChangedListener.SoundEffectChangedListener getSoundEffectChangedListener() {
        return mSoundEffectChangedListener;
    }

    public AuxUdpBroadcast setSoundEffectChangedListener(AuxUSB_SDChangedListener.SoundEffectChangedListener listener) {
        mSoundEffectChangedListener = listener;
        return this;
    }

    public AuxRadioActionListener.RadioConnectListener getRadioConnectListener() {
        return mRadioConnectListener;
    }

    public AuxUdpBroadcast setRadioConnectListener(AuxRadioActionListener.RadioConnectListener listener) {
        mRadioConnectListener = listener;
        return this;
    }

    public int getPeriod() {
        return mPeriod;
    }

    public AuxUdpBroadcast setSearchDevicePeriod(int period) {
        mPeriod = period;
        return this;
    }

    public BroadCastRunnable getBroadCastRunnable() {
        return mBroadCastRunnable;
    }

    private void setBroadCastRunnable(BroadCastRunnable broadCastRunnable) {
        mBroadCastRunnable = broadCastRunnable;
    }

}
