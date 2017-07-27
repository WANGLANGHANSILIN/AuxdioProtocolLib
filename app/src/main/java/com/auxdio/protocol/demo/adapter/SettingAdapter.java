package com.auxdio.protocol.demo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.auxdio.protocol.demo.bean.SettingEntity;

import java.util.ArrayList;

/**
 * Created by wangl on 2017/3/15 0015.
 */

public class SettingAdapter extends AuxdioBaseAdapter<SettingEntity> {
    public SettingAdapter(Context context, ArrayList<SettingEntity> settingEntities) {
        super(context, settingEntities);
    }

    @Override
    protected BaseViewHodle<SettingEntity> getHodle(int position) {
        return new SettingViewHodle();
    }

    class SettingViewHodle extends BaseViewHodle<SettingEntity>{

        private TextView mTextView;

        @Override
        protected View initView() {
            View inflate = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, null);
            mTextView = (TextView) inflate.findViewById(android.R.id.text1);
            return inflate;
        }

        @Override
        protected void refreshView(SettingEntity data) {
            mTextView.setText(data.getItemName());
        }
    }
}
