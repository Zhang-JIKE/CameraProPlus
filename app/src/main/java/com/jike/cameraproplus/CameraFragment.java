package com.jike.cameraproplus;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Message;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.daily.flexui.util.AppContextUtils;
import com.daily.flexui.view.CircleImageView;
import com.jike.cameraproplus.cameradata.CamMode;
import com.jike.cameraproplus.cameradata.CamSetting;
import com.jike.cameraproplus.enhance.EnhanceFunc;
import com.jike.cameraproplus.interfaces.CameraTouchListener;
import com.jike.cameraproplus.interfaces.CaptureListener;
import com.jike.cameraproplus.interfaces.CaptureListenerHelper;
import com.jike.cameraproplus.interfaces.OnHandFocusListener;
import com.jike.cameraproplus.interfaces.OnImageDetectedListener;
import com.jike.cameraproplus.interfaces.SurfaceTextureListenerAdapter;
import com.jike.cameraproplus.view.CameraView;
import com.jike.cameraproplus.view.ShutterView;
import com.google.android.material.tabs.TabLayout;



import java.text.DecimalFormat;

public class CameraFragment extends Fragment implements View.OnClickListener {

    final int[] pos = {6};
    private boolean isFaceingFront = false;
    private CameraView cameraView;

    private ImageView ivFlash;
    private ImageView ivHdr;
    private ImageView ivFilter;
    private ImageView ivSuperRes;
    private ImageView ivSettings;
    private ImageView ivFacingSwitch;
    private CircleImageView gallery;

    private ShutterView shutterView;

    private FrameLayout textureLayout;
    private CircleImageView ivPicture;
    private TextView tvInfo;
    private TextView tvScaler,tips;

    private String[] tabs = new String[]{"","","","Night", "Picture", "Fusion","Su-Res","","",""};
    private View[] views = new View[10];

    private final SurfaceTextureListenerAdapter textureListenerAdapter = new SurfaceTextureListenerAdapter() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            openCamera(width,height,isFaceingFront);
        }
    };

    public static CameraFragment newInstance() {
        return new CameraFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        initView(view);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (cameraView.isAvailable()) {
            openCamera(cameraView.viewWidth,cameraView.viewHeight,isFaceingFront);
        } else {
            cameraView.setSurfaceTextureListener(textureListenerAdapter);
        }
        //cameraView.c2helper.setScaleTime(cameraView.mScaleTime);
        DecimalFormat mFormat = new DecimalFormat(".0");
        String formatNum = mFormat.format(cameraView.mScaleTime);
        if(formatNum.contains(".0")){
            formatNum=formatNum.substring(0,formatNum.indexOf("."));
        }
        tvScaler.setText(formatNum+"x");
    }

    @Override
    public void onPause() {
        super.onPause();
        cameraView.closeCamera();
        cameraView.stopBackgroundThread();
    }

    private void initView(View view){
        ivFacingSwitch = view.findViewById(R.id.iv_facing_switch);
        tips = view.findViewById(R.id.tips);
        tvInfo = view.findViewById(R.id.information);
        tvScaler = view.findViewById(R.id.scaler);
        gallery = view.findViewById(R.id.iv_picture);

        ivFacingSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFaceingFront = !isFaceingFront;
                cameraView.closeCamera();
                if (cameraView.isAvailable()) {
                    openCamera(cameraView.viewWidth,cameraView.viewHeight,isFaceingFront);
                } else {
                    cameraView.setSurfaceTextureListener(textureListenerAdapter);
                }
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AppContextUtils.getAppContext(), ViewActivity.class);
                startActivity(intent);
            }
        });

        cameraView = (CameraView) view.findViewById(R.id.texture);
        cameraView.setOnImageDetectedListener(new OnImageDetectedListener() {
            @Override
            public void onSceneDetected(String tag) {
                /*Message message = Message.obtain();
                message.what = 1;
                message.obj = tag;*/
                //handler.sendMessage(message);
            }

            @Override
            public void onFaceDetected() {

            }
        });

        textureLayout = view.findViewById(R.id.texture_container);
        ivPicture = view.findViewById(R.id.iv_picture);

        ivFlash = view.findViewById(R.id.iv_flash);
        ivHdr = view.findViewById(R.id.iv_hdr);
        ivFilter = view.findViewById(R.id.iv_filter);
        ivSuperRes = view.findViewById(R.id.iv_super_res);
        ivSettings = view.findViewById(R.id.iv_settings);
        shutterView = view.findViewById(R.id.shutter);

        ivSettings.setOnClickListener(this);
        ivHdr.setOnClickListener(this);
        ivSuperRes.setOnClickListener(this);
        shutterView.setOnClickListener(this);

        CaptureListenerHelper.captureListener = new CaptureListener() {
            boolean isFirst = true;

            @Override
            public void onStartToCapture() {
                if(isFirst) {
                    isFirst = false;
                    shutterView.startCapture();
                    tips.post(new Runnable() {
                        @Override
                        public void run() {
                            tips.setVisibility(View.VISIBLE);
                        }
                    });

                }
            }

            @Override
            public void onCaptureFinished() {
                tips.post(new Runnable() {
                    @Override
                    public void run() {
                        tips.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onStartToCalculate() {
                shutterView.startProcess();
            }

            @Override
            public void onCalculateFinished() {
                shutterView.backToNormal();
                isFirst = true;
            }
        };

        //cameraView.setShutterView(shutterView);
        //cameraView.setTips(tips);

        initTab(view);
    }

    private void initTab(View view){
        final TabLayout tabLayout = view.findViewById(R.id.tablayout);
        CameraTouchListener touchListener = new CameraTouchListener();
        touchListener.setFingerListener(new CameraTouchListener.FingerListener() {
            @Override
            public void onScaleChanged(float scaleV, String scaleS) {
                cameraView.c2helper.setScaleTime(scaleV);
                tvScaler.setText(scaleS);
            }

            @Override
            public void onFocusSelected(Point point) {
                cameraView.cameraControllerView.setControlledFocus(point);
            }

            @Override
            public void onSlideToRight() {
                if (pos[0] > 3) {
                    int postion = --pos[0];
                    tabLayout.selectTab(tabLayout.getTabAt(postion));
                }
            }

            @Override
            public void onSlideToLeft() {
                if (pos[0] < 6) {
                    int postion = ++pos[0];
                    tabLayout.selectTab(tabLayout.getTabAt(postion));
                }
            }
        });
        cameraView.setOnTouchListener(touchListener);

        for(int i = 0; i < tabs.length; i++) {
            //tablayout.addTab(tablayout.newTab().setText(tab));
            String title = tabs[i];
            TabLayout.Tab tab = tabLayout.newTab();
            View inflate = View.inflate(getContext(), R.layout.view_tab, null);
            TextView textView = inflate.findViewById(R.id.text);
            textView.setText(title);
            if(i == 0 ||i == 1 ||i == 2 ||i == 7 ||i == 8 ||i == 9){
                inflate.setVisibility(View.GONE);
            }
            tab.setCustomView(inflate);
            views[i] = inflate;
            tabLayout.addTab(tab);
        }


        tabLayout.getTabAt(1).view.setClickable(false);
        tabLayout.getTabAt(2).view.setClickable(false);
        tabLayout.getTabAt(7).view.setClickable(false);
        tabLayout.getTabAt(8).view.setClickable(false);
        tabLayout.getTabAt(9).view.setClickable(false);

        tabLayout.post(new Runnable() {
            @Override
            public void run() {
                tabLayout.getTabAt(4).select();
            }
        });

        tabLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                tabLayout.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
                pos[0] = tab.getPosition();

                if(pos[0] == 3){
                    setNightOn();
                }else if(pos[0] == 4){
                    setNormal();
                }else if(pos[0] == 5){
                    setFusionOn();
                }else if(pos[0] == 6){
                    setSuperRes();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {


            }
        });
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == ivFlash.getId()){

        } else if(id == ivHdr.getId()){
            if ((CamMode.mode != CamMode.Mode.HDR)) {
                setHdrOn();
            } else {
                setNormal();
            }
        } else if(id == ivFilter.getId()){

        } else if(id == ivSuperRes.getId()){
            if(CamMode.mode != CamMode.Mode.PIX_FUSION){
                setFusionOn();
            }else {
                setNormal();
            }
        } else if(id == ivSettings.getId()){
            Intent intent = new Intent(AppContextUtils.getAppContext(), SettingsActivity.class);
            startActivity(intent);
        } else if(id == shutterView.getId()){
            if (shutterView.isEnabled) {
                //v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_RELEASE);
                MediaPlayer player = MediaPlayer.create(getContext(), R.raw.beep_twice);
                player.start();
                //EnhanceFunc.imgEnhance();
                cameraView.takePicture();
            }
        }
    }

    private void openCamera(int width, int height, boolean isFaceingFront){
        cameraView.openCamera(width, height, isFaceingFront);
        cameraView.setUpTexturePadding(textureLayout);
        cameraView.setControllerView(textureLayout);
        cameraView.cameraControllerView.setOnHandFocusListener(new OnHandFocusListener() {
            @Override
            public void onHandFocus(Point point) {
                cameraView.c2helper.setFocus(point);
            }

            @Override
            public void onFocusFallBack() {
            }
        });
        cameraView.startBackgroundThread();
    }

    private void setHdrOn(){
        setAllOff();
        ivHdr.setImageResource(R.drawable.ic_hdr_on);
        CamSetting.setIsAiSceneOpend(false);
        CamMode.mode = CamMode.Mode.HDR;
    }

    private void setFusionOn(){
        setAllOff();
        ivSuperRes.setImageResource(R.drawable.ic_super_res_on);
        CamSetting.setIsAiSceneOpend(false);
        CamMode.mode = CamMode.Mode.PIX_FUSION;
    }

    private void setNightOn(){
        setAllOff();
        ivFlash.setVisibility(View.INVISIBLE);
        ivHdr.setVisibility(View.INVISIBLE);
        ivFilter.setVisibility(View.INVISIBLE);
        ivSuperRes.setVisibility(View.INVISIBLE);
        CamSetting.setIsAiSceneOpend(false);
        CamMode.mode = CamMode.Mode.NIGHT;
    }

    private void setAllOff(){
        ivHdr.setImageResource(R.drawable.ic_hdr_off);
        ivSuperRes.setImageResource(R.drawable.ic_super_res_off);
    }

    private void setNormal(){
        setAllOff();
        ivFlash.setVisibility(View.VISIBLE);
        ivHdr.setVisibility(View.VISIBLE);
        ivFilter.setVisibility(View.VISIBLE);
        ivSuperRes.setVisibility(View.VISIBLE);
        CamMode.mode = CamMode.Mode.NORMAL;
    }

    private void setSuperRes(){
        setAllOff();
        ivFlash.setVisibility(View.VISIBLE);
        ivHdr.setVisibility(View.VISIBLE);
        ivFilter.setVisibility(View.VISIBLE);
        ivSuperRes.setVisibility(View.VISIBLE);
        CamSetting.setIsAiSceneOpend(false);
        CamMode.mode = CamMode.Mode.SUPER_RES;
    }
}
