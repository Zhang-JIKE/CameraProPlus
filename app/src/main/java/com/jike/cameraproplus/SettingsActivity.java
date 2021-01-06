package com.jike.cameraproplus;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.daily.flexui.activity.SlideActivity;
import com.daily.flexui.util.AppContextUtils;
import com.daily.flexui.view.SwitchButton;
import com.jike.cameraproplus.cameradata.CamEncode;
import com.jike.cameraproplus.cameradata.CamRates;
import com.jike.cameraproplus.cameradata.CamSetting;
import com.jike.cameraproplus.cameradata.CamSize;
import com.jike.cameraproplus.dialog.OnSelectItemListener;
import com.jike.cameraproplus.dialog.SettingPicSizeDialog;
import com.jike.cameraproplus.dialog.SettingVideoEncodeDialog;
import com.jike.cameraproplus.dialog.SettingVideoRatesDialog;
import com.jike.cameraproplus.dialog.SettingVideoSizeDialog;
import com.jike.cameraproplus.view.SettingView;


public class SettingsActivity extends SlideActivity {

    LinearLayout itemLab,itemSize,itemVideoSize,itemVideoRate,itemVideoEncode;

    TextView tvPicSize,tvVideoSize,tvVideoRates,tvVideoEncode;

    private SettingPicSizeDialog sSizeDlg;
    private SettingVideoSizeDialog sVideoSizeDlg;
    private SettingVideoRatesDialog sVideoRatesDlg;
    private SettingVideoEncodeDialog sVideoEncodeDlg;

    private SettingView swtClickSounds,swtLocation,swtMirror,swtLine,swtFaceDetect,swtSceneDetect,swtYuv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND, WindowManager.LayoutParams.FLAG_BLUR_BEHIND);

        AppContextUtils.setAppActivity(this);
        AppContextUtils.setAppContext(this);
        setContentView(R.layout.activity_settings);

        sSizeDlg = new SettingPicSizeDialog(SettingsActivity.this);
        sVideoSizeDlg = new SettingVideoSizeDialog(SettingsActivity.this);
        sVideoRatesDlg = new SettingVideoRatesDialog(SettingsActivity.this);
        sVideoEncodeDlg = new SettingVideoEncodeDialog(SettingsActivity.this);

        sSizeDlg.setOnSelectItemListener(new OnSelectItemListener() {
            @Override
            public void OnSelectItem(int pos) {
                CamSize.setPicIndex(pos);
                tvPicSize.setText(""+ CamSize.getPicSize().getWidth()+"x"+ CamSize.getPicSize().getHeight());

            }
        });

        sVideoSizeDlg.setOnSelectItemListener(new OnSelectItemListener() {
            @Override
            public void OnSelectItem(int pos) {
                CamSize.setVideoIndex(pos);
                tvVideoSize.setText(""+ CamSize.getRearVideoSize().getWidth()+"x"+ CamSize.getRearVideoSize().getHeight());
            }
        });

        sVideoRatesDlg.setOnSelectItemListener(new OnSelectItemListener() {
            @Override
            public void OnSelectItem(int pos) {
                CamRates.setSelectPos(pos);
                tvVideoRates.setText(""+ CamRates.getFpsRanges().get(CamRates.selectPos).getUpper()+"FPS");
            }
        });

        sVideoEncodeDlg.setOnSelectItemListener(new OnSelectItemListener() {
            @Override
            public void OnSelectItem(int pos) {
                CamEncode.setEncodePos(pos);
                tvVideoEncode.setText("" + CamEncode.encodes[CamEncode.encodePos]);
            }
        });

        itemLab = findViewById(R.id.item_lab);
        itemSize = findViewById(R.id.item_size);
        itemVideoSize = findViewById(R.id.item_video_size);
        itemVideoRate = findViewById(R.id.item_video_rate);
        itemVideoEncode = findViewById(R.id.item_video_encode);
        tvPicSize = findViewById(R.id.tv_pic_size);
        tvVideoSize = findViewById(R.id.tv_video_size);
        tvVideoRates = findViewById(R.id.tv_video_rates);
        tvVideoEncode = findViewById(R.id.tv_video_encode);

        swtClickSounds = findViewById(R.id.switch_click_sounds);
        swtLocation = findViewById(R.id.switch_location);
        swtMirror = findViewById(R.id.switch_mirror);

        swtLine = findViewById(R.id.switch_line);
        swtFaceDetect = findViewById(R.id.switch_face_detect);
        swtSceneDetect = findViewById(R.id.switch_scene_detect);
        swtYuv = findViewById(R.id.switch_isyuv);

        swtClickSounds.setOnSwitchListener(new SwitchButton.OnSwitchChangedListner() {
            @Override
            public void onSwitchChanged(boolean isChecked) {
                CamSetting.setIsClickSoundsOpened(isChecked);
            }
        });

        swtLocation.setOnSwitchListener(new SwitchButton.OnSwitchChangedListner() {
            @Override
            public void onSwitchChanged(boolean isChecked) {
                CamSetting.setIsGeoOpened(isChecked);
            }
        });

        swtMirror.setOnSwitchListener(new SwitchButton.OnSwitchChangedListner() {
            @Override
            public void onSwitchChanged(boolean isChecked) {
                CamSetting.setIsMirrorOpend(isChecked);
            }
        });
        swtLine.setOnSwitchListener(new SwitchButton.OnSwitchChangedListner() {
            @Override
            public void onSwitchChanged(boolean isChecked) {
                CamSetting.setIsLineOpend(isChecked);
            }
        });


        swtFaceDetect.setOnSwitchListener(new SwitchButton.OnSwitchChangedListner() {
            @Override
            public void onSwitchChanged(boolean isChecked) {
                synchronized (new Object()) {
                    CamSetting.setIsFaceDetectOpend(isChecked);
                }
            }
        });

        swtSceneDetect.setOnSwitchListener(new SwitchButton.OnSwitchChangedListner() {
            @Override
            public void onSwitchChanged(boolean isChecked) {
                synchronized (new Object()) {
                    CamSetting.setIsAiSceneOpend(isChecked);
                }
            }
        });

        swtYuv.setOnSwitchListener(new SwitchButton.OnSwitchChangedListner() {
            @Override
            public void onSwitchChanged(boolean isChecked) {
                CamSetting.setIsYuv(isChecked);
            }
        });

        itemLab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AppContextUtils.getAppContext(), LabActivity.class);
                startActivity(intent);
            }
        });

        itemSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sSizeDlg.show();
            }
        });

        itemVideoSize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sVideoSizeDlg.show();
            }
        });

        itemVideoRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sVideoRatesDlg.show();
            }
        });

        itemVideoEncode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sVideoEncodeDlg.show();
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        tvPicSize.setText(""+ CamSize.getPicSize().getWidth()+"x"+ CamSize.getPicSize().getHeight());
//        tvVideoSize.setText(""+ CamSize.getRearVideoSize().getWidth()+"x"+ CamSize.getRearVideoSize().getHeight());
        tvVideoRates.setText(""+ CamRates.getFpsRanges().get(CamRates.selectPos).getUpper()+"FPS");
        tvVideoEncode.setText("" +CamEncode.encodes[CamEncode.encodePos]);

        swtClickSounds.setChecked(CamSetting.isClickSoundsOpened);
        swtLocation.setChecked(CamSetting.isGeoOpened);
        swtMirror.setChecked(CamSetting.isMirrorOpend);

        swtLine.setChecked(CamSetting.isLineOpend);
        swtFaceDetect.setChecked(CamSetting.isFaceDetectOpend);
        swtSceneDetect.setChecked(CamSetting.isAiSceneOpend);
        swtYuv.setChecked(CamSetting.isYuv);
    }


}
