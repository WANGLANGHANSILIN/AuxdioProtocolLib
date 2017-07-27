package com.auxdio.protocol.demo.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.auxdio.protocol.demo.R;
import com.auxdio.protocol.demo.adapter.AuxdioBaseAdapter;
import com.auxdio.protocol.demo.adapter.ChannelAdapter;
import com.auxdio.protocol.demo.adapter.DeviceListAdapter;
import com.auxdio.protocol.demo.bean.DeviceEntity;
import com.auxdio.protocol.demo.bean.SettingEntity;
import com.auxdio.protocol.demo.bean.SourceEntity;
import com.auxdio.protocol.demo.dialog.ListDialog;
import com.auxdio.protocol.demo.dialog.RoomControlDialog;
import com.auxdio.protocol.demo.interfaces.ListDialogListener;
import com.auxdio.protocol.demo.utils.DeviceUtils;
import com.auxdio.protocol.demo.utils.ToastUtils;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.com.auxdio.protocol.bean.AuxDeviceEntity;
import cn.com.auxdio.protocol.bean.AuxNetModelEntity;
import cn.com.auxdio.protocol.bean.AuxPlayListEntity;
import cn.com.auxdio.protocol.bean.AuxRoomEntity;
import cn.com.auxdio.protocol.bean.AuxSongEntity;
import cn.com.auxdio.protocol.bean.AuxSourceEntity;
import cn.com.auxdio.protocol.interfaces.AuxRequestDeviceVersionListener;
import cn.com.auxdio.protocol.interfaces.AuxRequestNetModelListener;
import cn.com.auxdio.protocol.interfaces.AuxRequestPlayListListener;
import cn.com.auxdio.protocol.interfaces.AuxRequestSourceListener;
import cn.com.auxdio.protocol.interfaces.AuxRoomStateChangedListener;
import cn.com.auxdio.protocol.interfaces.AuxSreachDeviceListener;
import cn.com.auxdio.protocol.interfaces.AuxUSB_SDChangedListener;
import cn.com.auxdio.protocol.net.AuxUdpBroadcast;
import cn.com.auxdio.protocol.net.AuxUdpUnicast;
import cn.com.auxdio.protocol.protocol.AuxConfig;
import cn.com.auxdio.protocol.util.AuxLog;
import cn.com.auxdio.protocol.util.AuxRoomUtils;

public class RoomListActivity extends AppCompatActivity implements ListDialogListener, AuxSreachDeviceListener,
        AuxRoomStateChangedListener, AuxRequestPlayListListener, AuxRequestSourceListener,
        AuxUSB_SDChangedListener, AuxRequestNetModelListener,AuxRequestDeviceVersionListener,
        AuxRequestNetModelListener.NetModelBindListListener,AuxRequestNetModelListener.NetModelBindTypeListener
,AuxRequestNetModelListener.NetModelPlayModeListListener{

    ImageView mIvSetting;
    ImageView mTvDeviceList;

    @BindView(R.id.tv_title_layout_title)
    TextView mtvTitle;
    @BindView(R.id.lv_channle_list)
    ListView mLvChannleList;

    private ListDialog mDeviceListDialog;
    private ListDialog mSettingDialog;
    private ListDialog mRoomActionDialog;
    private List<AuxDeviceEntity> mAuxdioDeviceEntities;

    private Map<Integer, List<AuxDeviceEntity>> mDeviceHashMap;

    private ArrayList<SettingEntity> mSettingEntities;
    private List<AuxRoomEntity> roomEntities;
    private DeviceListAdapter mDeviceListAdapter;
    private ChannelAdapter mChannelAdapter;
    private RoomControlDialog mRoomControlDialog;
    private ArrayList<SourceEntity> mSourceEntities;
    private AuxRoomEntity mRoomEntity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_list);

        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tb);
        setSupportActionBar(toolbar);

        initView(toolbar);
        initData();
        initSDK();
    }

    private void initView(Toolbar toolbar) {
        mIvSetting = (ImageView) toolbar.findViewById(R.id.iv_title_layout_left);
        mTvDeviceList = (ImageView) toolbar.findViewById(R.id.iv_title_layout_right);
    }

    private void initSDK() {
        Log.e("searchDevice","initSDK");
        AuxUdpBroadcast.getInstace().startWorking().setSearchDevicePeriod(200).searchDevice(this);
        AuxUdpUnicast.getInstance()//初始化,获取实例
                .startWorking();//开启单播
    }

    private void initData() {
        mtvTitle.setText("未选择控制设备...");
        mAuxdioDeviceEntities = new CopyOnWriteArrayList<>();
        mSettingEntities = new ArrayList<>();
        roomEntities = new CopyOnWriteArrayList<>();
        mSourceEntities = new ArrayList<>();

        mDeviceHashMap = new Hashtable<>();
        for (int i = 0; i < 5; i++) {
            mSettingEntities.add(new SettingEntity("Setting---" + i));
        }

        initDialog();
        mChannelAdapter = new ChannelAdapter(RoomListActivity.this, new ArrayList<AuxRoomEntity>());
        mLvChannleList.setAdapter(mChannelAdapter);
        mLvChannleList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mRoomControlDialog != null && mChannelAdapter.getDataList().get(position) != null) {
                    mRoomControlDialog.setControlData(mChannelAdapter.getDataList().get(position));
                } else {
                    AuxLog.e("", "data is null");
                }
                mRoomControlDialog.show(getFragmentManager(), "ControlDialog");
            }
        });

        mLvChannleList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                mRoomEntity = mChannelAdapter.getItem(position);
                mRoomActionDialog.show(getFragmentManager(),"RoomDialog");

                return true;
            }
        });
    }

    private void initDialog() {
        mDeviceListAdapter = new DeviceListAdapter(this, mAuxdioDeviceEntities);
        mDeviceListDialog = ListDialog.newInstance(mDeviceListAdapter,ListDialog.DIALOG_SHOW_MODEL_DEVICE);
        mDeviceListDialog.setListTitle("DeviceList");
        mDeviceListDialog.setListDialogListener(this);


//        mSettingDialog = new ListDialog(mSettingEntities,ListDialog.DIALOG_SHOW_MODEL_SETTING);
//        mSettingDialog.setListTitle("SettingList");
//        mSettingDialog.setListDialogListener(this);

        List<String> stringList = new ArrayList<>();
        stringList.add("房间开关机");
        stringList.add("房间重命名为：newRoomName");

        initRoomDialog(stringList);

        mRoomControlDialog = new RoomControlDialog();
        mRoomActionDialog.setListDialogListener(this);
    }

    private void initRoomDialog(final List<String> stringList) {
        AuxdioBaseAdapter<String> auxdioBaseAdapter = new AuxdioBaseAdapter<String>(this, stringList) {
            @Override
            protected BaseViewHodle getHodle(int position) {
                return new BaseViewHodle<String>() {
                    TextView textView;

                    @Override
                    protected View initView() {
                        View inflate = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, null);
                        textView = (TextView) inflate.findViewById(android.R.id.text1);
                        return inflate;
                    }

                    @Override
                    protected void refreshView(String data) {
                        textView.setText(data);
                    }
                };
            }
        };
        mRoomActionDialog = ListDialog.newInstance(auxdioBaseAdapter,ListDialog.DIALOG_SHOW_MODEL_SETTING);
    }

    public void OnCickSetting(View v) {
        if (v.getTag() != null && v.getTag().equals("Start")){
            v.setTag("Stop");
            AuxUdpBroadcast.getInstace().startWorking();
            AuxUdpUnicast.getInstance().startWorking();
            AuxUdpBroadcast.getInstace().searchDevice();
            ToastUtils.showToast(this,"Start Work....");
        }else{
            v.setTag("Start");
            AuxUdpBroadcast.getInstace().stopWorking();
            AuxUdpUnicast.getInstance().stopWorking();
            AuxUdpUnicast.getInstance().setControlDeviceEntity(null);
            ToastUtils.showToast(this,"Stop Work....");
            mDeviceHashMap.clear();
            roomEntities.clear();
            mAuxdioDeviceEntities.clear();
            mtvTitle.setText("未选择控制设备...");
            mDeviceListAdapter.notifyDataSetChanged();
            mChannelAdapter.notifyDataSetChanged();
        }
//        mSettingDialog.show(getFragmentManager(), "SettingDialog");
    }

    public void OnCickDevice(View v) {
        Toast.makeText(this, "Device...", Toast.LENGTH_SHORT).show();
        mDeviceListDialog.show(getFragmentManager(), "DeviceDialog");
    }

    @Override
    public void callBackData(Object entity) {
        Log.i("ChannelListActivity", entity.toString());
        if (entity instanceof DeviceEntity || entity instanceof AuxDeviceEntity) {
            roomEntities.clear();
            List<? extends AuxDeviceEntity> deviceEntityList = mDeviceHashMap.get(((AuxDeviceEntity) entity).getDevModel());
            for (AuxDeviceEntity deviceEntity : deviceEntityList) {
                Log.i("ChannelListActivity", deviceEntity.getDevName()+"("+deviceEntity.getDevIP()+")    deviceCount:"+deviceEntityList.size());
                mtvTitle.setText(((AuxDeviceEntity) entity).getDevName());
                AuxUdpUnicast.getInstance()
                        .setRequestRoomStatePeriod(1500)//设置数据获取循环周期
                        .setControlDeviceEntity(deviceEntity)//设置控制设备
                        .requestDeviceRoomList(deviceEntity.getDevIP(),this)//查询设备全部分区并获取分区名称
//                        .requestDevicePlayList(deviceEntity.getDevIP(),this)//查询设备歌曲目录
                        .requestDeviceSourceList(deviceEntity.getDevIP(),this)//查询设备音源
//                        .requestNetModelList(deviceEntity.getDevIP(),this)//查询设备网络模块
//                        .requestBindAllRoomForNetModel(this)
                        ;
//                        .requestPointPlayMode(this);
            }
        }else if(entity instanceof String){
            String s = (String) entity;
            Log.i("","entity:"+s);
            if (s.equals("房间开关机")){
                if(mRoomEntity.getoNOffState() == 0x00)
                    AuxUdpUnicast.getInstance().setRoomOnOffState(mRoomEntity,true);
                else
                    AuxUdpUnicast.getInstance().setRoomOnOffState(mRoomEntity, false);

            }else{
                AuxUdpUnicast.getInstance().setRoomName(mRoomEntity,"newRoomName");
                mRoomEntity.setRoomName("newRoomName");
                mChannelAdapter.notifyDataSetChanged();
            }
        }
    }
/*
    @Override
    public void onRoomChange(AuxRoomEntity auxChannelEntity) {
        listChannelHandle(auxChannelEntity);
    }
*/
    private void listChannelHandle(AuxRoomEntity convetChannelEntity) {

        Log.i("ChannelListActivity","listChannelHandle   roomCount:"+roomEntities.size()+"   "+ convetChannelEntity.toString());

        if(roomEntities.size() == 0)
            roomEntities.add(0,convetChannelEntity);
        else{
            int indexByID = -1;
            int devModel = AuxUdpUnicast.getInstance().getControlDeviceEntity().getDevModel();

            if (devModel == AuxConfig.DeciveModel.DEVICE_DM836 || devModel == AuxConfig.DeciveModel.DEVICE_DM838 || devModel == AuxConfig.DeciveModel.DEVICE_DM858)
                indexByID = AuxRoomUtils.getChannnelIndexByIP(roomEntities, convetChannelEntity.getRoomIP());
            else
                indexByID = AuxRoomUtils.getChannnelIndexByID(roomEntities, convetChannelEntity.getRoomID());
            Log.i("ChannelListActivity","listChannelHandle   devModel:"+devModel+"   indexByID:"+indexByID+"   "+convetChannelEntity.getRoomIP());

            if (indexByID >= 0)
                roomEntities.set(indexByID, convetChannelEntity);
            else
                roomEntities.add(convetChannelEntity);
        }
        Log.i("ChannelListActivity","roomCount:"+roomEntities.size()+"   ChannelName:"+ convetChannelEntity.getRoomName()+"   ChannelIP:"+convetChannelEntity.getRoomIP());
//        if (mSourceEntities.size() > 0){
//            String sourceNameByID = SourceUtils.getSourceNameByID(mSourceEntities, convetChannelEntity.getSrcID());
//            convetChannelEntity.setRoomSrcName(sourceNameByID);
//        }

        RoomListActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mChannelAdapter.setDataList(roomEntities);
            }
        });

        if (mRoomControlDialog != null && mRoomControlDialog.getControlData() != null && mRoomControlDialog.getControlData().getRoomID() == convetChannelEntity.getRoomID()){
            mRoomControlDialog.setControlData(convetChannelEntity);
        }

    }

    @Override
    public void onMusicList(String devIP, List<AuxPlayListEntity> contentsEntities) {
        for (AuxPlayListEntity entity : contentsEntities) {
            if (entity.getMusicEntities() != null){
                for (AuxSongEntity musicEntity : entity.getMusicEntities()) {
                    Log.i("ChannelListActivity","onMusicList---hostName:"+ devIP +"   "+entity.getContentsName()+"   "+musicEntity.getSongName());
                }
            }
        }
    }

    @Override
    public void onUSBChanged(boolean isInsert) {
        Log.i("ChannelListActivity","onUSBChanged:"+isInsert);
    }

    @Override
    public void onSDChanged(String sdChanged) {
        Log.i("ChannelListActivity","sdChanged:"+sdChanged);
    }

    @Override
    public void onSourceList(String hostName, List<AuxSourceEntity> sourceEntities) {
        mSourceEntities.clear();
        for (AuxSourceEntity entity : sourceEntities) {
            Log.i("ChannelListActivity","onSourceList:"+entity.toString());
            mSourceEntities.add(SourceEntity.conVerter(entity));
        }
    }

    @Override
    public void onSreachDevice(final Map<Integer, List<AuxDeviceEntity>> auxDeviceEntity) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDeviceHashMap = auxDeviceEntity;
                mAuxdioDeviceEntities.clear();

                for (Integer integer : mDeviceHashMap.keySet()) {
                    List<AuxDeviceEntity> auxDeviceEntities = mDeviceHashMap.get(integer);
                    for (AuxDeviceEntity deviceEntity : auxDeviceEntities) {
                        Log.i("onSreachDevice","onSreachDeviceMAC"+deviceEntity.toString()+deviceEntity.getDevMAC());
                    }
                    Log.i("onSreachDevice","onSreachDevice---回调   设备类型："+integer+"   设备个数："+auxDeviceEntities.size());
                    if (auxDeviceEntities != null)
                        mAuxdioDeviceEntities.add(auxDeviceEntities.get(0));
                }
                mDeviceListAdapter.setDataList(mAuxdioDeviceEntities);
            }
        });

    }

    /**
     * 设备列表处理
     * @param auxDeviceEntity
     */
    private void handleDeviceList(final AuxDeviceEntity auxDeviceEntity) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                DeviceEntity deviceEntity = DeviceEntity.CastToDeviceEntity(auxDeviceEntity);
                boolean isUpData = false;
                if (mDeviceHashMap.size() == 0) {
                    mAuxdioDeviceEntities.add(deviceEntity);
                    isUpData = addDeviceUpdat(deviceEntity);
                }else
                {
                    boolean deviceModelExist = DeviceUtils.isDeviceModelExist(mDeviceHashMap.keySet(), deviceEntity);//判断设备模式是否存在
                    if (!deviceModelExist){
                        mAuxdioDeviceEntities.add(deviceEntity);
                        isUpData = addDeviceUpdat(deviceEntity);
                    }
                    else
                    {
                        if (deviceEntity.getDevModel() == AuxConfig.DeciveModel.DEVICE_DM836 || deviceEntity.getDevModel() == AuxConfig.DeciveModel.DEVICE_DM838){
                            List<AuxDeviceEntity> deviceEntities = mDeviceHashMap.get(deviceEntity.getDevModel());
                            boolean deviceIPExist = DeviceUtils.isDeviceIPExist(deviceEntities, deviceEntity);//判断设备IP是否存在
                            if (!deviceIPExist){
                                deviceEntities.add(deviceEntity);
                                //查询新上线的设备房间名称
                                if (AuxUdpUnicast.getInstance().getControlDeviceEntity() != null && AuxUdpUnicast.getInstance().getControlDeviceEntity().getDevModel() == deviceEntity.getDevModel())
                                AuxUdpUnicast.getInstance().requestDeviceRoomList(deviceEntity.getDevIP(), RoomListActivity.this);
                                isUpData = true;
                            }
                        }
                    }
                }

                for (Integer integer : mDeviceHashMap.keySet()) {
                    List<AuxDeviceEntity> deviceEntities = mDeviceHashMap.get(integer);
                    for (AuxDeviceEntity entity : deviceEntities) {
                        Log.i("handleDeviceList",mDeviceHashMap.keySet().size()+"   isUpData: - "+isUpData+"   "+entity.getDevName()+"("+entity.getDevIP()+")   deviceModel:"+entity.getDevModel()+"   deviceModel(key):"+integer+"   deviceCount:"+deviceEntities.size());
                    }
                }

                if (isUpData) {
                    mDeviceListAdapter.setDataList(mAuxdioDeviceEntities);
                }
            }

            private boolean addDeviceUpdat(DeviceEntity deviceEntity) {
                ArrayList<AuxDeviceEntity> deviceEntities = new ArrayList<>();
                deviceEntities.add(deviceEntity);
                mDeviceHashMap.put(deviceEntity.getDevModel(),deviceEntities);
                return true;
            }
        });
    }

    @Override
    public void onRoomChange(final AuxRoomEntity auxChannelEntity) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.i("RoomListActivity",auxChannelEntity.toString());
                listChannelHandle(auxChannelEntity);
            }
        });
    }

    @Override
    public void OnRoomOffLine(final AuxRoomEntity auxChannelEntity) {
        if (auxChannelEntity == null)
            return;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                roomEntities.remove(auxChannelEntity);
                mChannelAdapter.setDataList(roomEntities);
                if (roomEntities.size() == 0)
                    mtvTitle.setText("未选择控制设备...");
                Log.i("RoomListActivity","OnRoomOffLine: size:"+roomEntities.size()+"  "+auxChannelEntity.toString());
            }
        });
    }

    @Override
    public void onNetModelList(List<AuxNetModelEntity> modelEntities) {
        for (AuxNetModelEntity entity : modelEntities) {
            Log.i("RoomListActivity","onNetModelList: size:"+modelEntities.size()+"  "+entity.toString());
        }
        AuxUdpUnicast.getInstance().requestNetModelRelevanceType(this);
    }

    @Override
    public void onDeviceVersion(String softwareVersion, String protocolVersion) {
        Log.i("RoomListActivity","onDeviceVersion: softwareVersion:"+softwareVersion+"  protocolVersion"+protocolVersion);
    }

    @Override
    public void onBindList(Map<Integer, AuxNetModelEntity> mapList) {
        for (Integer integer : mapList.keySet()) {
            Log.i("RoomListActivity","onBindList ---  roomID: "+integer+"  netModel  "+mapList.get(integer));
        }
    }

    @Override
    public void onRelevanceType(int type) {
        Log.i("RoomListActivity","onRelevanceType: type:"+type);
    }

    @Override
    public void onPlayModeList(Map<Integer, Integer> integerMap) {
        for (Integer integer : integerMap.keySet()) {
            Log.i("RoomListActivity","onPlayModeList: modelID:"+integer+"   playMode: "+integerMap.get(integer));
        }

    }
}
