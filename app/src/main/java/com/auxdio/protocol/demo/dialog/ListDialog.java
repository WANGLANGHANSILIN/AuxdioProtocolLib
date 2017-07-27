package com.auxdio.protocol.demo.dialog;

import android.app.DialogFragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.DisplayMetrics;
import android.util.SparseBooleanArray;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.auxdio.protocol.demo.R;
import com.auxdio.protocol.demo.adapter.AuxdioBaseAdapter;
import com.auxdio.protocol.demo.bean.DeviceEntity;
import com.auxdio.protocol.demo.bean.SettingEntity;
import com.auxdio.protocol.demo.bean.SourceEntity;
import com.auxdio.protocol.demo.interfaces.ListDialogListener;

import java.io.Serializable;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.com.auxdio.protocol.bean.AuxDeviceEntity;
import cn.com.auxdio.protocol.bean.AuxNetRadioEntity;
import cn.com.auxdio.protocol.bean.AuxNetRadioTypeEntity;
import cn.com.auxdio.protocol.bean.AuxPlayListEntity;
import cn.com.auxdio.protocol.bean.AuxSongEntity;

/**
 * Created by wangl on 2017/3/14 0014.
 */

public class ListDialog extends DialogFragment {

    public static final int DIALOG_SHOW_MODEL_DEVICE = 0;//设备显示
    public static final int DIALOG_SHOW_MODEL_MUSIC = 1;//目录音乐
    public static final int DIALOG_SHOW_MODEL_PROGRAM = 2;//节目源
    public static final int DIALOG_SHOW_MODEL_SETTING = 3;//节目源

    @BindView(R.id.tv_dialog_list_title)
    TextView mTvDialogTitle;
    @BindView(R.id.lv_dialog_list)
    ListView mLvDialogList;
    private View contentView;
    private String mListTitle;
    private AuxdioBaseAdapter mAdapter;
    private int mHeight = 1;
    private ListDialogListener mListDialogListener;
    private SparseBooleanArray mSparseArray;
    private int dialogModle;
    private int index = -1;


    public ListDialog() {
    }

    public static final ListDialog newInstance(AuxdioBaseAdapter adapter,int dialogModle)
    {
        ListDialog dialog = new ListDialog();
        Bundle bundle = new Bundle();
        bundle.putInt("mode",dialogModle);
        bundle.putSerializable("adapter", (Serializable) adapter);
        dialog.setArguments(bundle);
        return dialog ;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();
        this.mAdapter = (AuxdioBaseAdapter) arguments.getSerializable("adapter");
        this.dialogModle =  arguments.getInt("mode");

        mSparseArray = new SparseBooleanArray(mAdapter.getCount());
        initSparse();
    }

    private void initSparse() {
        for (int i = 0; i < mSparseArray.size(); i++) {
            mSparseArray.put(i,false);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        contentView = inflater.inflate(R.layout.dialog_list_layout, container);
        ButterKnife.bind(this, contentView);
        return contentView;
    }



    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mTvDialogTitle.setText(mListTitle);
        mLvDialogList.setAdapter(mAdapter);

        mLvDialogList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                handleOnItem(position);
            }
        });
    }

    private void handleOnItem(int position) {
        for (int i = 0; i < mAdapter.getCount(); i++) {
            Object item = mAdapter.getItem(i);
            if(item instanceof SettingEntity || item instanceof AuxDeviceEntity || item instanceof AuxSongEntity || item instanceof AuxNetRadioEntity
                    || item instanceof String )
            {
                if (i == position) {
                    mListDialogListener.callBackData(item);
                }
                dismiss();
            }else if(item instanceof SourceEntity){
                SourceEntity mItem = (SourceEntity) item;
                if (i == position) {
                    if (!mItem.isChecked()){
                        mItem.setChecked(true);
                        mListDialogListener.callBackData(mItem);
                    }
                }
                else {
                    mItem.setChecked(false);
                }
                dismiss();
            }else if(item instanceof AuxPlayListEntity){
                if (i == position) {
                    mListDialogListener.callBackData(item);
                    i = mAdapter.getCount();
                }
            }else if (item instanceof AuxNetRadioTypeEntity){
                if (i == position) {
                    mListDialogListener.callBackData(item);
                }
            }else if(item instanceof DeviceEntity){
                DeviceEntity mItem = (DeviceEntity) item;
                if (i == position) {
                    if (!mItem.isChecked()){
                        mItem.setChecked(true);
                        mListDialogListener.callBackData(mItem);
                    }
                }
                else {
                    mItem.setChecked(false);
                }
                dismiss();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        WindowManager windowManager = getDialog().getWindow().getWindowManager();
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);

        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        Display defaultDisplay = getDialog().getWindow().getWindowManager().getDefaultDisplay();
//        int height = defaultDisplay.getHeight();
//        int width = defaultDisplay.getWidth();
        WindowManager.LayoutParams attributes = getDialog().getWindow().getAttributes();

        if (dialogModle == DIALOG_SHOW_MODEL_DEVICE){
            attributes.x = defaultDisplay.getWidth() /5;
            attributes.y = (int) -(defaultDisplay.getHeight()/3.4);
            getDialog().getWindow().setAttributes(attributes);//设置Dialog在屏幕位置

            getDialog().getWindow().setLayout(width*4/5, height*6/10);// 设置Dialog显示大小
        }else if (dialogModle == DIALOG_SHOW_MODEL_MUSIC){
//            attributes.x = defaultDisplay.getWidth() *3/5;
//            attributes.y = (defaultDisplay.getHeight()/5);
//            getDialog().getWindow().setAttributes(attributes);
//
//            getDialog().getWindow().setLayout(width*4/5, height*8/10);

            attributes.gravity = Gravity.CENTER_VERTICAL;
            getDialog().getWindow().setLayout(width*5/5, height*3/5);

        }else if(dialogModle == DIALOG_SHOW_MODEL_SETTING){
            attributes.gravity = Gravity.CENTER_VERTICAL;
//            attributes.x = defaultDisplay.getWidth();
//            attributes.y = (defaultDisplay.getHeight());
//            getDialog().getWindow().setAttributes(attributes);

            getDialog().getWindow().setLayout(width*4/5, height*3/5);
        }
    }

    public String getListTitle() {
        return mListTitle;
    }

    public void setListTitle(String listTitle) {
        mListTitle = listTitle;
    }

    public ListDialogListener getListDialogListener() {
        return mListDialogListener;
    }

    public void setListDialogListener(ListDialogListener listDialogListener) {
        mListDialogListener = listDialogListener;
    }

    public TextView getTvDialogTitle() {
        return mTvDialogTitle;
    }
}
