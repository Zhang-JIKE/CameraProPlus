package com.jike.cameraproplus.view;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.daily.flexui.util.AppContextUtils;
import com.daily.flexui.util.DisplayUtils;
import com.jike.cameraproplus.cameradata.CamSize;
import com.jike.cameraproplus.interfaces.OnImageDetectedListener;


public class BaseCameraView extends TextureView {


    protected int mRatioWidth = 0;
    protected int mRatioHeight = 0;
    public float mScaleTime = 1;

    protected OnImageDetectedListener onImageDetectedListener;

    public void setOnImageDetectedListener(OnImageDetectedListener onImageDetectedListener) {
        this.onImageDetectedListener = onImageDetectedListener;
    }

    public CameraControllerView cameraControllerView;

    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        mRatioWidth = width;
        mRatioHeight = height;
        Log.e("Cam-Ratio","W:"+width+" H:"+height);
        requestLayout();
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (0 == mRatioWidth || 0 == mRatioHeight) {
            setMeasuredDimension(width, height);
        } else {
            if (width < height * mRatioWidth / mRatioHeight) {
                setMeasuredDimension(width, width * mRatioHeight / mRatioWidth);
            } else {
                setMeasuredDimension(height * mRatioWidth / mRatioHeight, height);
            }
        }
    }

    public void configureTransform(int viewWidth, int viewHeight) {

        if (null == CamSize.getPicSize() || null == AppContextUtils.getAppActivity()) {
            return;
        }
        int rotation = AppContextUtils.getAppActivity().getWindowManager().getDefaultDisplay().getRotation();
        Log.e("Rotation",rotation+"");
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect;
        bufferRect = new RectF(0, 0,CamSize.getPicSize().getHeight(), CamSize.getPicSize().getWidth());

        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {

            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);

            float viewLongEdge = viewWidth > viewHeight ? viewWidth : viewHeight;
            float viewShortEdge = viewWidth <= viewHeight ? viewWidth : viewHeight;
            float scale = Math.max(
                    (float) viewShortEdge / CamSize.getPicSize().getHeight(),
                    (float) viewLongEdge / CamSize.getPicSize().getWidth());
            matrix.postScale(scale, scale, centerX, centerY);

            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        } else {
            //matrix.postRotate(90, centerX, centerY);
        }
        setTransform(matrix);
    }

    public void setUpTexturePadding(View parent){
        Display display = AppContextUtils.getAppActivity().getWindowManager().getDefaultDisplay();
        int screenW = display.getWidth();
        int screenH = display.getHeight();
        Log.e("Screen","W"+screenW+"H"+screenH);

        int width = CamSize.getPicSize().getHeight();
        int height = CamSize.getPicSize().getWidth();

        if(((float) width/(float)height !=(float)3/4 &&(float) width/(float)height !=(float)9/16)) {
            parent.setPadding(0,0,0,0);
        } else {
            parent.setPadding(0, DisplayUtils.dp2px(66),0,0);
        }
        setAspectRatio(CamSize.getPicSize().getHeight(), CamSize.getPicSize().getWidth());
    }

    public void setControllerView(ViewGroup parent) {
        if (cameraControllerView == null) {
            cameraControllerView = new CameraControllerView(AppContextUtils.getAppContext());
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.width = parent.getRight();
            params.height = (int) (params.width * CamSize.getPicRatio());
            cameraControllerView.setLayoutParams(params);

            int width = CamSize.getPicSize().getHeight();
            int height = CamSize.getPicSize().getWidth();

            if(((float) width/(float)height !=(float)3/4 &&(float) width/(float)height !=(float)9/16)) {
                cameraControllerView.setPad(0);
            } else {
                cameraControllerView.setPad(DisplayUtils.dp2px(66));
            }
            parent.addView(cameraControllerView);

        } else {
            parent.removeView(cameraControllerView);
            cameraControllerView = new CameraControllerView(AppContextUtils.getAppContext());
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.width = getWidth();
            params.height = (int) (getWidth() * CamSize.getPicRatio());
            cameraControllerView.setLayoutParams(params);

            int width = CamSize.getPicSize().getHeight();
            int height = CamSize.getPicSize().getWidth();

            if(((float) width/(float)height !=(float)3/4 &&(float) width/(float)height !=(float)9/16)) {
                cameraControllerView.setPad(0);
            } else {
                cameraControllerView.setPad(DisplayUtils.dp2px(66));
            }

            parent.addView(cameraControllerView);
        }
    }


    public BaseCameraView(Context context) {
        super(context);
    }

    public BaseCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
}
