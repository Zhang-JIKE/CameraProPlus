package com.jike.cameraproplus.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.daily.flexui.view.SwitchButton;
import com.jike.cameraproplus.R;

public class SettingView extends LinearLayout {

    private View view;

    private TextView title;
    private ImageView image;
    private SwitchButton switchButton;

    public SettingView(Context context) {
        super(context);

        init();
    }

    public SettingView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();

        TypedArray a = context.obtainStyledAttributes(attrs,R.styleable.SettingView);
        String text = a.getString(R.styleable.SettingView_Stv_title);
        int resId = a.getResourceId(R.styleable.SettingView_Stv_img_res, R.drawable.ic_settings);

        setTitle(text);
        setImage(resId);

        a.recycle();
    }

    private void init(){
        view = LayoutInflater.from(getContext()).inflate(R.layout.view_settings,this,true);
        title = view.findViewById(R.id.title);
        image = view.findViewById(R.id.image);
        switchButton = view.findViewById(R.id.switch_btn);
    }

    public void setChecked(boolean checked){
        switchButton.setChecked(checked);
    }

    public void setOnSwitchListener(SwitchButton.OnSwitchChangedListner listener){
        switchButton.setOnSwitchChangedListner(listener);
    }

    public void setTitle(String title){
        this.title.setText(title);
    }

    public void setImage(int resId){
        image.setImageResource(resId);
    }

}
