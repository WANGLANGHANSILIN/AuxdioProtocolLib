package com.auxdio.protocol.demo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import cn.com.auxdio.protocol.bean.AuxRoomEntity;

/**
 * Created by wangl on 2017/3/15 0015.
 */

public class ChannelAdapter extends AuxdioBaseAdapter<AuxRoomEntity> {
    private List<AuxRoomEntity> roomEntities;
    public ChannelAdapter(Context context, List<AuxRoomEntity> roomEntities) {
        super(context, roomEntities);
        this.roomEntities = roomEntities;
    }

    @Override
    protected BaseViewHodle<AuxRoomEntity> getHodle(int position) {
        return new ChannelViewHodle();
    }

    /*
    public void setData(List<RoomEntity> roomEntities) {
//        this.roomEntities.clear();
//        for (RoomEntity roomEntity : roomEntities) {
//            this.roomEntities.add(roomEntity);
//        }
        this.roomEntities = roomEntities;
        notifyDataSetChanged();
    }

    public List<RoomEntity> callBackData(){
        return roomEntities;
    }

    */

    class ChannelViewHodle extends BaseViewHodle<AuxRoomEntity>{

        private TextView mTextView;

        @Override
        protected View initView() {
            View inflate = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, null);
            mTextView = (TextView) inflate.findViewById(android.R.id.text1);
            return inflate;
        }

        @Override
        protected void refreshView(AuxRoomEntity data) {
            String s = "";
            if (data.getoNOffState() == 0x00){
                s = "关";
            }else
                 s= "开";
            mTextView.setText((data.getRoomName().equals("")?data.getRoomIP():data.getRoomName())+" "+data.getRoomSrcName()+" ("+data.getVolumeID()+"%)" +"   "+ s);
        }
    }
}
