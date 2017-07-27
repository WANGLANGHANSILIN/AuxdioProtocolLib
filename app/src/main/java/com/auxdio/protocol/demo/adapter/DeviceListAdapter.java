package com.auxdio.protocol.demo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.auxdio.protocol.demo.R;
import com.auxdio.protocol.demo.bean.DeviceEntity;

import java.util.List;

import cn.com.auxdio.protocol.bean.AuxDeviceEntity;

/**
 * Created by wangl on 2017/3/14 0014.
 */

public class DeviceListAdapter extends AuxdioBaseAdapter{

    private List<? extends AuxDeviceEntity> auxdioDeviceEntities;
    public DeviceListAdapter(Context context, List<? extends AuxDeviceEntity> auxdioDeviceEntities) {
        super(context, auxdioDeviceEntities);
        this.auxdioDeviceEntities = auxdioDeviceEntities;
    }

    @Override
    protected BaseViewHodle<? extends AuxDeviceEntity> getHodle(int position) {
        return new DeviceHodle();
    }

    private class DeviceHodle extends BaseViewHodle{

        private View mView;
        private TextView mTv;

        @Override
        protected View initView() {
            mView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, null);
            mTv = (TextView) mView.findViewById(android.R.id.text1);
            return mView;
        }

        @Override
        protected void refreshView(Object data) {
            if (data instanceof AuxDeviceEntity){
                mTv.setText(((AuxDeviceEntity)data).getDevName()+" ("+((AuxDeviceEntity)data).getDevIP()+")");
            }else if (data instanceof DeviceEntity){
                if (((DeviceEntity)data).isChecked()){
                    mTv.setTextColor(getContext().getResources().getColor(R.color.colorPrimary));
                }else
                    mTv.setTextColor(getContext().getResources().getColor(R.color.colorAccent));
                mTv.setText(((DeviceEntity)data).getDevName()+" ("+((DeviceEntity)data).getDevIP()+")");
            }
        }
    }
}
