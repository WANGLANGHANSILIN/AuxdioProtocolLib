package com.auxdio.protocol.demo.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import cn.com.auxdio.protocol.bean.AuxPlayListEntity;

import java.util.ArrayList;

/**
 * Created by wangl on 2017/3/15 0015.
 */

public class ContentAdapter extends BaseAdapter {
    private ArrayList<AuxPlayListEntity> mContentsEntities;
    private Context mContext;

    public ContentAdapter(Context context, ArrayList<AuxPlayListEntity> contentsEntities) {
        this.mContext = context;
        this.mContentsEntities = contentsEntities;
    }

    @Override
    public int getCount() {
        return mContentsEntities.size();
    }

    @Override
    public Object getItem(int position) {
        return mContentsEntities.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null){

        }else{

        }


        return convertView;
    }
}
