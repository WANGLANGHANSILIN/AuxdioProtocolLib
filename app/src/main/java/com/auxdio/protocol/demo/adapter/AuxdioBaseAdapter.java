package com.auxdio.protocol.demo.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.io.Serializable;
import java.util.List;

/**
 * Created by wangl on 2017/3/13 0013.
 */

public abstract class AuxdioBaseAdapter<T> extends BaseAdapter implements Serializable {

    private List<T> mArrayList;
    private Context mContext;

    public AuxdioBaseAdapter(Context context, List<T> tArrayList) {
        this.mArrayList = tArrayList;
        this.mContext = context;
    }

    public void setDataList(List<T> arrayList){
        this.mArrayList = arrayList;
        this.notifyDataSetChanged();
    }

    public List<T> getDataList(){
        return this.mArrayList;
    }

    @Override
    public int getCount() {
        return mArrayList.size();
    }

    @Override
    public T getItem(int position) {
        return mArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BaseViewHodle<T> viewHodle = null;
        if (convertView == null) {
            viewHodle = getHodle(position);
        } else {
            viewHodle = (BaseViewHodle<T>) convertView.getTag();
        }
        viewHodle.setData(getItem(position));
        return viewHodle.getRootView();
    }

    protected abstract BaseViewHodle<T> getHodle(int position);


    public Context getContext() {
        return mContext;
    }

    public static abstract class BaseViewHodle<T>{

        private T mData;
        private View mRootView;

        public BaseViewHodle() {
            mRootView = initView();
            mRootView.setTag(this);
        }

        protected abstract View initView();

        public View getRootView() {
            return mRootView;
        }

        public void setData(T data){
            this.mData = data;
            refreshView(data);
        }

        protected abstract void refreshView(T data);

        public T getData(){
            return mData;
        }
    }
}
