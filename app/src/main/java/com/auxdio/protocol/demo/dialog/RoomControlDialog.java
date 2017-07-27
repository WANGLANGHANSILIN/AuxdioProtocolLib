package com.auxdio.protocol.demo.dialog;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.auxdio.protocol.demo.R;
import com.auxdio.protocol.demo.adapter.ContentsAdapter;
import com.auxdio.protocol.demo.adapter.SourceAdapter;
import com.auxdio.protocol.demo.bean.SourceEntity;
import com.auxdio.protocol.demo.interfaces.ListDialogListener;
import com.auxdio.protocol.demo.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.com.auxdio.protocol.bean.AuxNetRadioEntity;
import cn.com.auxdio.protocol.bean.AuxNetRadioTypeEntity;
import cn.com.auxdio.protocol.bean.AuxPlayListEntity;
import cn.com.auxdio.protocol.bean.AuxRoomEntity;
import cn.com.auxdio.protocol.bean.AuxSongEntity;
import cn.com.auxdio.protocol.bean.AuxSourceEntity;
import cn.com.auxdio.protocol.interfaces.AuxControlActionListener;
import cn.com.auxdio.protocol.interfaces.AuxRadioActionListener;
import cn.com.auxdio.protocol.interfaces.AuxRequestHighLowPitchListener;
import cn.com.auxdio.protocol.interfaces.AuxRequestPlayListListener;
import cn.com.auxdio.protocol.interfaces.AuxRequestRadioListener;
import cn.com.auxdio.protocol.interfaces.AuxRequestSongTimeLengthListener;
import cn.com.auxdio.protocol.interfaces.AuxRequestSourceListener;
import cn.com.auxdio.protocol.interfaces.AuxUSB_SDChangedListener;
import cn.com.auxdio.protocol.net.AuxUdpBroadcast;
import cn.com.auxdio.protocol.net.AuxUdpUnicast;
import cn.com.auxdio.protocol.protocol.AuxConfig;
import cn.com.auxdio.protocol.util.AuxLog;

/**
 * Created by wangl on 2017/3/15 0015.
 */

public class RoomControlDialog extends DialogFragment implements AuxRequestSourceListener,ListDialogListener,AuxControlActionListener.ControlProgramNameListener,
        AuxUSB_SDChangedListener,AuxControlActionListener.ControlPlayStateListener,AuxControlActionListener.ControlPlayModeListener,AuxRequestPlayListListener
,AuxRequestRadioListener,AuxRadioActionListener.RadioConnectListener,AuxControlActionListener.ControlVolumeListener,AuxRequestHighLowPitchListener, AuxRequestSongTimeLengthListener {
    @BindView(R.id.iv_control_back)
    ImageView mIvControlBack;
    @BindView(R.id.tv_control_room_title)
    TextView mTvControlChannelTitle;
    @BindView(R.id.iv_control_src_seletor)
    ImageView mIvControlSrcSeletor;
    @BindView(R.id.tv_control_music_name)
    TextView mTvControlMusicName;
    @BindView(R.id.tv_control_src_name)
    TextView mTvControlSrcName;
    @BindView(R.id.iv_control_cent_logo)
    ImageView mIvControlCentLogo;
    @BindView(R.id.iv_control_music_file)
    ImageView mIvControlMusicFile;
    @BindView(R.id.iv_control_music_previous)
    ImageView mIvControlMusicPrevious;
    @BindView(R.id.iv_control_music_play)
    ImageView mIvControlMusicPlay;
    @BindView(R.id.iv_control_music_next)
    ImageView mIvControlMusicNext;
    @BindView(R.id.iv_control_music_playmodle)
    ImageView mIvControlMusicPlaymodle;
    @BindView(R.id.sb_control_volume)
    SeekBar mSbControlVolume;
    @BindView(R.id.iv_control_music_mute)
    ImageView mIvControlMusicMute;

    @BindView(R.id.sb_control_pro_time)
    SeekBar mSbControlTitme;

    @BindView(R.id.tv_seek_time_current_show)
    TextView mTvShowCurrentTime;

    @BindView(R.id.tv_seek_time_total_show)
    TextView mTvShowTotalTime;


    private AuxRoomEntity mControlData;// 控制的分区

    private ArrayList<SourceEntity> mSourceEntities;// 音源列表
    private ListDialog mSourceDialog;// 音源对话框
    private SourceAdapter mSourceAdapter; // 音效Adapter

    private List<Object> mAuxContentsEntities;// 目录列表
    private ListDialog mContentsDialog;// 目录对话框
    private ContentsAdapter mContentsAdapter;// 目录Adapter

    private List<Object> mAuxRadioTypeEntities;// diantai列表

    private AuxUdpUnicast mAuxUdpUnicast;

    private int[] modelIconList = new int[]{R.mipmap.ic_playmodel_single_play,R.mipmap.ic_playmodel_single_recycler,
            R.mipmap.ic_playmodel_order_play,
            R.mipmap.ic_playmodel_recycler_play,R.mipmap.ic_playmodel_random_play};
    private int[] playIconList = new int[]{R.mipmap.ic_playstate_pause,R.mipmap.ic_playstate_play};


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.dialog_control_layout, null);
        ButterKnife.bind(this, inflate);
        return inflate;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initData();
        initDialog();
    }

    private void initData() {
        mSourceEntities = new ArrayList<>();
        mAuxContentsEntities = new ArrayList<>();
        mAuxRadioTypeEntities = new ArrayList<>();
        mTvControlSrcName.setText(mControlData.getRoomSrcName());

    }

    /**
     * 音量处理
     */
    private void volumeHandle() {
        mSbControlVolume.setProgress(mControlData.getVolumeID());
        showMute();
        mSbControlVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mSbControlVolume.setProgress(progress);
                showMute();
                mAuxUdpUnicast.setVolume(new AuxRoomEntity[]{mControlData},progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        mSbControlTitme.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                String[] split = mTvShowTotalTime.getText().toString().split(":");
                for (String s : split) {
                    AuxLog.i("mSbControlTitme","...."+s);
                }
                //sendData:0x00  0x0f
                if (split.length < 2)
                    return;
                Integer integer = Integer.valueOf(split[0])*60 + Integer.valueOf(split[1]);

                int i = (integer * seekBar.getProgress()) / 100;
                mTvShowCurrentTime.setText(i/60+":"+i%60);
                AuxLog.i("mSbControlTitme","total:"+integer+",current: "+i+",  "+mTvShowCurrentTime.getText().toString());
                AuxUdpUnicast.getInstance().setCurrentPlayPosition(mControlData,i);
            }
        });
    }

    //初始化Adapter和Dialog
    private void initDialog() {
        mSourceAdapter = new SourceAdapter(getActivity(), mSourceEntities);
        mSourceDialog = ListDialog.newInstance(mSourceAdapter,ListDialog.DIALOG_SHOW_MODEL_MUSIC);
        mSourceAdapter.setCheckSrc(mControlData.getSrcID());

        mContentsAdapter = new ContentsAdapter(getActivity(),mAuxContentsEntities);
        mContentsDialog = ListDialog.newInstance(mContentsAdapter,ListDialog.DIALOG_SHOW_MODEL_MUSIC);

        if (mSourceEntities != null && mSourceEntities.size() > 0) {
            for (SourceEntity entity : mSourceEntities) {
                if (entity.getSourceID() == mControlData.getSrcID()) {

                    entity.setChecked(true);
                    mSourceAdapter.notifyDataSetChanged();
                }
            }
        }

        showMethods(mControlData.getSrcID());
//        ListDialog listDialog = new ListDialog(new ProgramAdapter(mContext,mSourceEntities),ListDialog.DIALOG_SHOW_MODEL_PROGRAM);
    }

    @Override
    public void onResume() {
        super.onResume();
        Display defaultDisplay = getDialog().getWindow().getWindowManager().getDefaultDisplay();
        getDialog().getWindow().setLayout(defaultDisplay.getWidth(), defaultDisplay.getHeight());

        AuxUdpBroadcast.getInstace().setRadioConnectListener(this).setUSBSDChangedListener(this);
        mAuxUdpUnicast = AuxUdpUnicast.getInstance().
                requestDeviceSourceList(mControlData.getRoomIP(),this)//查询设备音源
                .requestDevicePlayList(mControlData.getRoomIP(),this)//查询设备目录和歌曲
                .requestRadioData(this)//请求电台数据
                //初次进入房间查询节目名称、查询播放模式、查询模仿状态
                .requestProgramName(new AuxRoomEntity[]{mControlData},this)
                .requestPlayState(new AuxRoomEntity[]{mControlData},this)
                .requestPlayMode(new AuxRoomEntity[]{mControlData},this)
                .requestHighLowPitch(this)
                .requestCurrentPlayPosition(mControlData,this);

        if (mTvControlChannelTitle != null)
            mTvControlChannelTitle.setText(mControlData.getRoomName());
        else
            AuxLog.i("","mTvControlChannelTitle is null");

        volumeHandle();
    }

    public void setControlData(AuxRoomEntity controlData) {
        this.mControlData = controlData;
        if (isVisible()){
            mSbControlVolume.setProgress(mControlData.getVolumeID());
            showMute();
            mTvControlSrcName.setText(mControlData.getRoomSrcName());
            showMethods(controlData.getSrcID());
        }
    }

    public AuxRoomEntity getControlData() {
        return this.mControlData;
    }

    //返回房间界面
    @OnClick(R.id.iv_control_back)
    public void onClickBack(){
        dismiss();
    }

    //音源选择
    @OnClick(R.id.iv_control_src_seletor)
    public void onClickSrcSeletor(){
        mSourceDialog.setListTitle("音源列表");
        mSourceDialog.setListDialogListener(this);
        mSourceDialog.show(getFragmentManager(),"SourseDialog");
        ToastUtils.showToast(getActivity(), "音源选择");
    }
    @OnClick(R.id.iv_control_music_mute)
    public void OnClickMute(){
        if(mSbControlVolume.getProgress() == 0){
            mAuxUdpUnicast.setMuteState(new AuxRoomEntity[]{mControlData},false);
        }else{
            mAuxUdpUnicast.setMuteState(new AuxRoomEntity[]{mControlData},true);
            ToastUtils.showToast(getActivity(),"静音");
        }
        showMute();
    }

    private void showMute() {
        if(mSbControlVolume.getProgress() == 0){
            mIvControlMusicMute.setImageResource(R.mipmap.ic_playcontrol_mute);
        }
        else{
            mIvControlMusicMute.setImageResource(R.mipmap.ic_control_notification_fill);
        }
    }

    //音乐文件
    @OnClick(R.id.iv_control_music_file)
    public void onClickMusicFile(){
        if(mControlData.getSrcID() >= AuxConfig.ProgramSource.PROGRAM_NETRADIO && mControlData.getSrcID() < AuxConfig.ProgramSource.PROGRAM_NETMUSIC){
            if (AuxUdpUnicast.getInstance().getControlDeviceEntity().getDevModel() == AuxConfig.DeciveModel.DEVICE_DM858){
                AuxUdpUnicast.getInstance().playDM858Radio(false,4743804,44638118);
            }else{
                mContentsDialog.setListTitle("电台类型列表");
                mContentsDialog.setListDialogListener(this);
                mContentsAdapter.setDataList(mAuxRadioTypeEntities);
                mContentsDialog.show(getFragmentManager(), "ContentDialog");
                ToastUtils.showToast(getActivity(), "网络电台");
            }
        }else {
            mContentsDialog.setListTitle("目录列表");
            mContentsDialog.setListDialogListener(this);
            mContentsAdapter.setDataList(mAuxContentsEntities);
            mContentsDialog.show(getFragmentManager(), "ContentDialog");
            ToastUtils.showToast(getActivity(), "音乐文件");
        }
    }


    //上一曲
    @OnClick(R.id.iv_control_music_previous)
    public void onClickMusicPrevious(){
        mAuxUdpUnicast.prevProgram(new AuxRoomEntity[]{mControlData},this);
        ToastUtils.showToast(getActivity(), "上一曲");
    }

    //播放暂停
    @OnClick(R.id.iv_control_music_play)
    public void onClickMusicPlay(){
//        showPlayState(playState);
        int value = 0;
        if (playState == 0)
            value = 2;
        else
            value = 1;

        mAuxUdpUnicast.setPlayState(new AuxRoomEntity[]{mControlData},value,this);
        ToastUtils.showToast(getActivity(), "播放暂停");
//        playState++;
    }

    //下一曲
    @OnClick(R.id.iv_control_music_next)
    public void onClickMusicNext(){
        mAuxUdpUnicast.nextProgram(new AuxRoomEntity[]{mControlData},this);
        ToastUtils.showToast(getActivity(), "下一曲");
    }

    private int modelIndex = 0;
    private int playState = 0;
    //播放模式
    @OnClick(R.id.iv_control_music_playmodle)
    public void onClickPlayMode(){
        modelIndex++;
        if (modelIndex > 5)
            modelIndex = 1;
        AuxLog.i("onClickPlayMode","palyModel:"+modelIndex);
        mAuxUdpUnicast.setPlayMode(new AuxRoomEntity[]{mControlData},modelIndex,this);
    }

    @Override
    public void onSourceList(String hostName, List<AuxSourceEntity> sourceEntities) {
        for (AuxSourceEntity entity : sourceEntities) {
            mSourceEntities.add(SourceEntity.conVerter(entity));
        }
//        mSourceEntities = sourceEntities;
        AuxLog.i("RoomControlDialog","onSourceList:"+mSourceEntities.size());
        if (mSourceAdapter != null)
            mSourceAdapter.setDataList(mSourceEntities);
    }

    @Override
    public void callBackData(Object o) {
        if (o instanceof SourceEntity){
            SourceEntity auxSourceEntity = (SourceEntity) o;
            mTvControlSrcName.setText(auxSourceEntity.getSourceName());
            mAuxUdpUnicast.setAudioSource(new AuxRoomEntity[]{mControlData}, auxSourceEntity);
            mAuxUdpUnicast.requestProgramName(new AuxRoomEntity[]{mControlData},this);
            showMethods(auxSourceEntity.getSourceID());

        }else if(o instanceof AuxPlayListEntity){
            ArrayList<AuxSongEntity> musicEntities = ((AuxPlayListEntity) o).getMusicEntities();
            if (musicEntities == null) {
                musicEntities = new ArrayList<>();
            }
            List<Object> list = new ArrayList();
            list.addAll(musicEntities);
            Log.i("callBackData","callBackData:"+list.size()+"   "+((AuxPlayListEntity)o).toString()+"   ");
            mContentsDialog.getTvDialogTitle().setText(((AuxPlayListEntity) o).getContentsName());
            mContentsAdapter.setDataList(list);

        }else if(o instanceof AuxSongEntity){
            mAuxUdpUnicast.playSong((AuxSongEntity) o);
        } else if (o instanceof AuxNetRadioTypeEntity){
            List<AuxNetRadioEntity> netRadioList = ((AuxNetRadioTypeEntity) o).getNetRadioList();
            List<Object> list = new ArrayList();
            list.addAll(netRadioList);
            mContentsDialog.getTvDialogTitle().setText(((AuxNetRadioTypeEntity) o).getRadioType());
            mContentsAdapter.setDataList(list);
        }else if (o instanceof AuxNetRadioEntity){
            mAuxUdpUnicast.playRadio(1, (AuxNetRadioEntity) o);
        }

    }

    @Override
    public void onUSBChanged(boolean isInsert) {
        Log.i("onSDChanged","onUSBChanged:isInsert:"+isInsert);
    }

    @Override
    public void onSDChanged(String sdChanged) {
        Log.i("onSDChanged","onSDChanged:"+sdChanged);
    }

    private void showProgramName(String programName) {
        String finalName = "";
        if (programName != null) {
            finalName = programName;
        }
        final String finalName1 = finalName;
        if (getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTvControlMusicName.setText(finalName1);
                }
            });
        }
    }

    private void showPlayState(int playStateValue) {
        playState = playStateValue;
        if (playState == 4 || playState == 2){//停止
            playState = 1;
        }else{
            playState = 0;
        }
        Log.i("showPlayState","playState:"+playState);

        if (getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mIvControlMusicPlay.setImageResource(playIconList[playState]);
                }
            });
        }
    }

    private void showPlayModel(final int playModelValue) {
        if (getActivity()!= null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showPlayModelToast(playModelValue);
                    mIvControlMusicPlaymodle.setImageResource(modelIconList[playModelValue]);
                    modelIndex = playModelValue;
                    modelIndex++;
                }

                private void showPlayModelToast(int playModelValue) {
                    ToastUtils.showToast(getActivity(),getStringPlayModel(playModelValue));
                }

                private String getStringPlayModel(int playModelValue) {
                    String pla = "";
                    if (playModelValue == 0)
                        pla = "单曲播放";
                    else if (playModelValue == 1)
                        pla = "单曲循环";
                    else if (playModelValue == 2)
                        pla = "顺序播放";
                    else if (playModelValue == 3)
                        pla = "顺序循环";
                    else  if (playModelValue == 4)
                        pla = "随机播放";
                    return pla;
                }
            });
        }
    }

    //根据音源ID显示控件
    private void showMethods(int srcID){
        int devModel = AuxUdpUnicast.getInstance().getControlDeviceEntity().getDevModel();
        if (devModel == AuxConfig.DeciveModel.DEVICE_DM838 || devModel == AuxConfig.DeciveModel.DEVICE_DM858){
            if (srcID == 0x81){
                actionInvisible(View.VISIBLE);
                mSbControlVolume.setVisibility(View.VISIBLE);
            }else if (srcID == 0xC1 && devModel == AuxConfig.DeciveModel.DEVICE_DM858){
                actionInvisible(View.VISIBLE);
                mSbControlVolume.setVisibility(View.VISIBLE);
            }else{
                AUX_DVD_Show();
            }
            return;
        }

        switch (srcID){
            case AuxConfig.ProgramSource.PROGRAM_INTERNAL:
                actionInvisible(View.VISIBLE);
                mSbControlVolume.setVisibility(View.VISIBLE);
                break;
            case AuxConfig.ProgramSource.PROGRAM_AUX:
                AUX_DVD_Show();
                break;

            case AuxConfig.ProgramSource.PROGRAM_AUX2:
                AUX_DVD_Show();
                break;

            case AuxConfig.ProgramSource.PROGRAM_MP3_USB:
                actionInvisible(View.VISIBLE);
                mSbControlVolume.setVisibility(View.VISIBLE);
                break;

            case AuxConfig.ProgramSource.PROGRAM_DVD:
                AUX_DVD_Show();
                break;

            case AuxConfig.ProgramSource.PROGRAM_BLUET:
                AUX_DVD_Show();
                mTvControlMusicName.setVisibility(View.VISIBLE);
                mIvControlMusicPrevious.setVisibility(View.VISIBLE);
                mIvControlMusicNext.setVisibility(View.VISIBLE);
                mIvControlMusicPlay.setVisibility(View.VISIBLE);
                break;
        }
        if (srcID > 0xD0){
            actionInvisible(View.VISIBLE);
            mSbControlVolume.setVisibility(View.VISIBLE);
        }else if(srcID < 0xD0 && srcID > 0xC0){
            AUX_DVD_Show();
            mTvControlMusicName.setVisibility(View.VISIBLE);
            mIvControlMusicPrevious.setVisibility(View.VISIBLE);
            mIvControlMusicNext.setVisibility(View.VISIBLE);
            mIvControlMusicFile.setVisibility(View.VISIBLE);
        }else if(srcID < 0xC0 && srcID > 0xB0){
            actionInvisible(View.VISIBLE);
            mSbControlVolume.setVisibility(View.VISIBLE);
            mIvControlMusicPlaymodle.setVisibility(View.INVISIBLE);
        }

    }

    private void AUX_DVD_Show() {
        actionInvisible(View.INVISIBLE);
        mSbControlVolume.setVisibility(View.VISIBLE);
    }

    private void actionInvisible(int visible) {
        mIvControlMusicFile.setVisibility(visible);
        mIvControlMusicPrevious.setVisibility(visible);
        mIvControlMusicPlay.setVisibility(visible);
        mIvControlMusicNext.setVisibility(visible);
        mIvControlMusicPlaymodle.setVisibility(visible);
        mTvControlMusicName.setVisibility(visible);
    }

    /**
     * 目录和歌曲获取
     * @param devIP 主机IP
     * @param contentsEntities  目录和歌曲集合列表
     */
    @Override
    public void onMusicList(String devIP, final List<AuxPlayListEntity> contentsEntities) {

        for (AuxPlayListEntity contentsEntity : contentsEntities) {
            Log.i("RoomControlDialog","onMusicList---:"+contentsEntity.toString()+"   "+ devIP +"    "+mControlData.getRoomIP());
            if (contentsEntity.getMusicEntities() != null) {
                for (AuxSongEntity auxSongEntity : contentsEntity.getMusicEntities()) {
//                    mAuxContentsEntities.add(auxSongEntity);
                    Log.i("RoomControlDialog","onMusicList---:"+ auxSongEntity.toString());
                }
            }
        }
        if (!devIP.equals(mControlData.getRoomIP()))
            return;
        if (getActivity() != null){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAuxContentsEntities.clear();
                    mAuxContentsEntities.addAll(contentsEntities);
//                mAuxContentsEntities = contentsEntities;
                    Log.i("RoomControlDialog","onMusicList---:"+mAuxContentsEntities.size()+"   updata..."+contentsEntities.size());
                    mContentsAdapter.setDataList(mAuxContentsEntities);
                }
            });
        }
    }

    @Override
    public void onRadioList(final List<AuxNetRadioTypeEntity> netRadioEntities) {
        for (AuxNetRadioTypeEntity contentsEntity : netRadioEntities) {
//            Log.i("RoomControlDialog","onRadioList---:"+contentsEntity.toString());
            if (contentsEntity.getNetRadioList() != null) {
                for (AuxNetRadioEntity auxMusicEntity : contentsEntity.getNetRadioList()) {
//                    mAuxContentsEntities.add(auxMusicEntity);
//                    Log.i("RoomControlDialog","onRadioList---:"+auxMusicEntity.toString());
                }
            }
        }

        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAuxRadioTypeEntities.addAll(netRadioEntities);
//                    mContentsAdapter.setDataList(mAuxRadioTypeEntities);
                }
            });
        }
    }

    @Override
    public void onConnectState(int connect) {
        String connnectState = "";
        if (connect == 1)
            connnectState = "正在连接";
        else if (connect == 2)
            connnectState = "连接成功";
        else if (connect == 3)
            connnectState = "连接失败";
        Log.i("RoomControlDialog","connnectState:"+connnectState + " connect:"+connect);
        final String finalConnnectState = connnectState;
        if (getActivity() == null)
            return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTvControlSrcName.setText(finalConnnectState);
            }
        });

//        showProgramName(connnectState);
    }

    @Override
    public void onVolume(AuxRoomEntity auxRoomEntity, int volumuteValue) {
        Log.i("RoomControlDialog","volumuteValue:  "+volumuteValue+"     "+auxRoomEntity.toString());
    }

    @Override
    public void onPlayState(AuxSourceEntity sourceEntity, int playStateValue) {
        Log.i("RoomControlDialog","playStateValue:  "+playStateValue);
        showPlayState(playStateValue);
    }

    @Override
    public void onPlayModel(AuxSourceEntity sourceEntity, int playModelValue) {
        AuxLog.i("RoomControlDialog","palyModel:"+playModelValue);
        showPlayModel(--playModelValue);
    }

    @Override
    public void onProgramName(AuxSourceEntity sourceEntity, String programName) {
        Log.i("RoomControlDialog","programName:"+programName);
        AuxUdpUnicast.getInstance().requestCurrentPlayPosition(mControlData,this);
        showProgramName(programName);
    }

    @Override
    public void onHighLowPitch(int highPitch, int lowPitch) {
        Log.i("RoomControlDialog","onHighLowPitch:   highPitch"+highPitch+"   lowPitch:"+lowPitch);
    }

    @Override
    public void onSongTimeLength(final int totalLength, final int currentLength, final int percent) {
        Log.i("RoomControlDialog","onHighLowPitch:   currentLength"+currentLength+"   totalLength:"+totalLength+",percent:"+percent);
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTvShowTotalTime.setText(totalLength/60+":"+totalLength%60);
                mTvShowCurrentTime.setText(currentLength/60+":"+currentLength%60);
                mSbControlTitme.setProgress(percent);
            }
        });

    }
}
