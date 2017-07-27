package com.auxdio.protocol.demo.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.auxdio.protocol.demo.R;
import com.auxdio.protocol.demo.bean.SourceEntity;

import java.util.ArrayList;

/**
 * Created by wangl on 2017/3/15 0015.
 */

public class SourceAdapter extends AuxdioBaseAdapter<SourceEntity> {


    private int mCheckSrc;

    public SourceAdapter(Context context, ArrayList arrayList) {
        super(context, arrayList);
    }

    @Override
    protected BaseViewHodle getHodle(int position) {
        return new ProgramViewHodle();
    }

    public void setCheckSrc(int checkSrc) {
        mCheckSrc = checkSrc;
    }

    class ProgramViewHodle extends BaseViewHodle<SourceEntity>{

        private TextView mTextView;

        @Override
        protected View initView() {
            View inflate = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, null);
            mTextView = (TextView) inflate.findViewById(android.R.id.text1);
            return inflate;
        }

        @Override
        protected void refreshView(SourceEntity data) {
            mTextView.setText(data.getSourceName());
            int color = 0;
            if (data.isChecked())
                color = R.color.source_check_color;
            else
                color = R.color.source_normal_color;
            mTextView.setTextColor(getContext().getResources().getColor(color));
            mTextView.setBackgroundColor(getContext().getResources().getColor(R.color.write));
        }
    }
}
