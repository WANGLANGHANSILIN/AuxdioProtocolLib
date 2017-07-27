package com.auxdio.protocol.demo.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import cn.com.auxdio.protocol.bean.AuxPlayListEntity;
import cn.com.auxdio.protocol.bean.AuxSongEntity;
import cn.com.auxdio.protocol.bean.AuxNetRadioEntity;
import cn.com.auxdio.protocol.bean.AuxNetRadioTypeEntity;

/**
 * Created by wangl on 2017/3/29 0029.
 */

public class ContentsAdapter extends AuxdioBaseAdapter<Object> {

    public ContentsAdapter(Context context, List<Object> tArrayList) {
        super(context, tArrayList);
    }

    @Override
    protected BaseViewHodle getHodle(int position) {
        return new ContentsViewHodle();
    }

    class ContentsViewHodle extends BaseViewHodle<Object>{

        private TextView mTextView;

        @Override
        protected View initView() {
            View inflate = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, null);
            mTextView = (TextView) inflate.findViewById(android.R.id.text1);
            return inflate;
        }

        @Override
        protected void refreshView(Object data) {
            if (data instanceof AuxSongEntity)
                mTextView.setText(((AuxSongEntity) data).getSongName());
            else if (data instanceof AuxPlayListEntity) {
                String contentsName = ((AuxPlayListEntity) data).getContentsName();
                Log.i("refreshView","contentsName:"+contentsName);
                if (contentsName.startsWith("/mnt/yaffs2/")){
                    mTextView.setText(contentsName.substring(12,contentsName.length()-1));
                }else if (contentsName.startsWith("/mnt/udisk/")){
                    if (contentsName.length() > 11)
                        mTextView.setText(contentsName.substring(11,contentsName.length()-1));
                    else
                        mTextView.setText("Root");
                }else
                    mTextView.setText(contentsName);
            }
            else if (data instanceof AuxNetRadioTypeEntity){
                mTextView.setText(((AuxNetRadioTypeEntity) data).getRadioType());
            }else if (data instanceof AuxNetRadioEntity){
                mTextView.setText(((AuxNetRadioEntity) data).getRadioName());
            }

            /*
            int color = 0;
            if (((SongEntity)data).isChecked())
                color = R.color.colorAccent;
            else
                color = R.color.source_normal_color;
            mTextView.setTextColor(getContext().getResources().getColor(color));
            mTextView.setBackgroundColor(getContext().getResources().getColor(R.color.write));
            */
        }
    }
}
