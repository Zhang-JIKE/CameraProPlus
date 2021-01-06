package com.jike.cameraproplus.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

import com.daily.flexui.util.DisplayUtils;
import com.daily.flexui.view.abstractview.BaseView;
import com.jike.cameraproplus.R;

public class ShutterView extends BaseView {

    private Paint yellowDegreePaint;
    private Paint whiteDegreePaint;
    private Paint ringPaint;
    private Paint buttonPaint;

    private int captureProgress = 0;
    private int buttonRadius = DisplayUtils.dp2px(25);
    public boolean isEnabled = true;

    public ValueAnimator captureAnim,processAnim,backAnim;

    public ShutterView(Context context) {
        super(context);
    }

    public ShutterView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void init(AttributeSet attrs) {
        yellowDegreePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        yellowDegreePaint.setColor(getResources().getColor(R.color.colorAccent));
        yellowDegreePaint.setStrokeWidth(DisplayUtils.dp2px(1f));

        whiteDegreePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        whiteDegreePaint.setColor(getResources().getColor(R.color.colorWhite));
        whiteDegreePaint.setAlpha(100);

        buttonPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        buttonPaint.setColor(getResources().getColor(R.color.colorWhite));

        ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        ringPaint.setColor(getResources().getColor(R.color.colorWhite));
        ringPaint.setStrokeWidth(DisplayUtils.dp2px(2.5f));
        ringPaint.setStyle(Paint.Style.STROKE);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for (int i = 0; i < 60; i++) {
            canvas.drawLine(width/2,  DisplayUtils.dp2px(0), width/2,  DisplayUtils.dp2px(2.5f), whiteDegreePaint);
            canvas.rotate(6,width/2,width/2);
        }

        for (int i = 0; i < captureProgress; i++) {
            canvas.drawLine(width/2,  DisplayUtils.dp2px(0), width/2,  DisplayUtils.dp2px(2.5f), yellowDegreePaint);
            canvas.rotate(6,width/2,width/2);
        }

        canvas.drawCircle(width/2,height/2,buttonRadius, buttonPaint);
        canvas.drawCircle(width/2,height/2, DisplayUtils.dp2px(30.75f), ringPaint);
    }

    public void startCapture(){
        if(captureAnim != null){
            captureAnim.cancel();
        }
        captureAnim = ValueAnimator.ofInt(0,61);
        captureAnim.setDuration(4000);
        captureAnim.setInterpolator(new LinearInterpolator());
        captureAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int v = (int)animation.getAnimatedValue();
                captureProgress = v;
                invalidate();
            }
        });
        captureAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                isEnabled = false;
                ringPaint.setAlpha(0);
                buttonPaint.setAlpha(100);
                invalidate();
            }
        });
        captureAnim.start();
    }

    public void startProcess(){
        Log.e("Shutter","startProcess");

        if(processAnim != null){
            processAnim.cancel();
        }
        processAnim = ValueAnimator.ofInt(DisplayUtils.dp2px(25), DisplayUtils.dp2px(16));
        processAnim.setRepeatMode(ValueAnimator.REVERSE);
        processAnim.setRepeatCount(-1);
        processAnim.setDuration(450);
        processAnim.setInterpolator(new OvershootInterpolator());
        processAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int v = (int)animation.getAnimatedValue();
                buttonRadius = v;
                invalidate();
            }
        });

        processAnim.start();
    }

    public void backToNormal(){
        Log.e("Shutter","backToNormal");

        if(processAnim !=null){
            processAnim.setRepeatCount(0);
            processAnim.removeAllUpdateListeners();
            processAnim.cancel();
        }

        if(backAnim != null){
            backAnim.cancel();
        }

        backAnim = ValueAnimator.ofInt(buttonRadius, DisplayUtils.dp2px(25));
        backAnim.setDuration(500);
        backAnim.setInterpolator(new OvershootInterpolator());
        backAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int v = (int)animation.getAnimatedValue();
                float percent = 1 - ((float)(DisplayUtils.dp2px(25) - v)/ DisplayUtils.dp2px(25));
                ringPaint.setAlpha(100+(int)(155*percent));
                buttonPaint.setAlpha(100+(int)(155*percent));

                buttonRadius = v;
                postInvalidate();
            }
        });

        backAnim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                ringPaint.setAlpha(255);
                buttonPaint.setAlpha(255);
                invalidate();
                isEnabled = true;
            }
        });
        backAnim.start();
    }


    @Override
    public int getWrapContentWidth() {
        return DisplayUtils.dp2px(64);
    }

    @Override
    public int getWrapContentHeight() {
        return DisplayUtils.dp2px(64);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        if(isEnabled) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                buttonPaint.setAlpha(100);
                invalidate();
            } else if (event.getAction() == MotionEvent.ACTION_UP) {
                buttonPaint.setAlpha(255);
                invalidate();
            }
        }
        return super.onTouchEvent(event);
    }
}
