package com.auxdio.protocol.demo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import cn.com.auxdio.protocol.bean.AuxSongEntity;

import java.util.ArrayList;

/**
 * Created by wangl on 2017/3/15 0015.
 */

public class MusicAdapter extends AuxdioBaseAdapter<AuxSongEntity> {
    public MusicAdapter(Context context, ArrayList<AuxSongEntity> musicEntities) {
        super(context, musicEntities);
    }

    @Override
    protected BaseViewHodle<AuxSongEntity> getHodle(int position) {
        return new MusicViewHodle();
    }

    class MusicViewHodle extends BaseViewHodle<AuxSongEntity>{

        private TextView mTextView;

        @Override
        protected View initView() {
            View inflate = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, null);
            mTextView = (TextView) inflate.findViewById(android.R.id.text1);
            return inflate;
        }

        @Override
        protected void refreshView(AuxSongEntity data) {
            mTextView.setText(data.getSongName());
        }
    }
}
