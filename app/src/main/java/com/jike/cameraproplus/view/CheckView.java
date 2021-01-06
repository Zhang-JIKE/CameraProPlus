package com.jike.cameraproplus.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jike.cameraproplus.R;


public class CheckView extends FrameLayout {

    private ImageView ivIcon;
    private TextView tvTitle;

    private boolean isChecked;
    private int iconId,checkedIconId;

    public CheckView(@NonNull Context context) {
        super(context);
        initView();
    }

    public CheckView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    private void initView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_checkview, this);
        ivIcon = view.findViewById(R.id.iv_icon);
        tvTitle = view.findViewById(R.id.tv_title);
    }

    public void setTitle(String s){
        tvTitle.setText(s);
    }

    public void setIconId(int iconId) {
        this.iconId = iconId;
    }

    public void setCheckedIconId(int checkedIconId) {
        this.checkedIconId = checkedIconId;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
        if(isChecked){
            ivIcon.setImageResource(checkedIconId);
        }else {
            ivIcon.setImageResource(iconId);
        }
    }

    public boolean isChecked() {
        return isChecked;
    }
}
