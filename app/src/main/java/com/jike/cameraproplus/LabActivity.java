package com.jike.cameraproplus;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.daily.flexui.util.AppContextUtils;
import com.daily.flexui.view.SwitchButton;
import com.jike.cameraproplus.cameradata.CamRates;
import com.jike.cameraproplus.cameradata.CamSetting;


public class LabActivity extends AppCompatActivity {

    private SwitchButton swtForce60Fps,swtAccel,swtDenoise,swtNight;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppContextUtils.setAppActivity(this);
        AppContextUtils.setAppContext(this);
        setContentView(R.layout.activity_lab);

        swtForce60Fps = findViewById(R.id.switch_force_60fps);
        swtAccel = findViewById(R.id.switch_cv_accel);
        swtNight = findViewById(R.id.switch_night);
        swtDenoise = findViewById(R.id.switch_denoise);

        swtForce60Fps.setOnSwitchChangedListner(new SwitchButton.OnSwitchChangedListner() {
            @Override
            public void onSwitchChanged(boolean isChecked) {
                CamRates.setIsForcedOpen60Fps(isChecked);
            }
        });

        swtAccel.setOnSwitchChangedListner(new SwitchButton.OnSwitchChangedListner() {
            @Override
            public void onSwitchChanged(boolean isChecked) {
                CamSetting.setIsAccel(isChecked);
            }
        });

        swtDenoise.setOnSwitchChangedListner(new SwitchButton.OnSwitchChangedListner() {
            @Override
            public void onSwitchChanged(boolean isChecked) {
                CamSetting.setIsDenoiseOpened(isChecked);
            }
        });

        swtNight.setOnSwitchChangedListner(new SwitchButton.OnSwitchChangedListner() {
            @Override
            public void onSwitchChanged(boolean isChecked) {
                CamSetting.setIsNightOpened(isChecked);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        swtForce60Fps.setChecked(CamRates.isForcedOpen60Fps);
        swtAccel.setChecked(CamSetting.isAccel);
        swtDenoise.setChecked(CamSetting.isDenoiseOpened);
        swtNight.setChecked(CamSetting.isNightOpened);
    }
}
