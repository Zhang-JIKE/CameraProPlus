package com.jike.cameraproplus.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

import androidx.annotation.Nullable;

import com.daily.flexui.util.DisplayUtils;
import com.jike.cameraproplus.R;
import com.jike.cameraproplus.cameradata.CamSetting;
import com.jike.cameraproplus.interfaces.OnHandFocusListener;


public class CameraControllerView extends View {


    private int mRatioWidth = 0;
    private int mRatioHeight = 0;

    private Paint rectPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint evPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint aimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint aimPointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Point aim;

    private Rect[] rects;
    private Point[] points;

    private int pad;

    private float aimDistance = DisplayUtils.dp2px(2);
    private float aimLength = DisplayUtils.dp2px(6);
    private float aimRectDis = DisplayUtils.dp2px(32);
    private float aimRectLength = DisplayUtils.dp2px(9);

    private boolean isControlledFocus = false;

    private ValueAnimator animator,animatorFocus;

    private OnHandFocusListener onHandFocusListener;

    public void setOnHandFocusListener(OnHandFocusListener onHandFocusListener) {
        this.onHandFocusListener = onHandFocusListener;
    }

    public void setControlledFocus(Point aim) {
        this.aim = aim;
        initAnim();
        isControlledFocus = true;
        invalidate();
        animator.start();
        animatorFocus.start();
        if(onHandFocusListener!=null){
            onHandFocusListener.onHandFocus(aim);
        }
    }

    public CameraControllerView(Context context) {
        super(context);
        init();
    }

    public CameraControllerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init(){
        setWillNotDraw(false);
        rectPaint.setColor(getResources().getColor(R.color.colorAccent));
        rectPaint.setStrokeWidth(DisplayUtils.dp2px(1.5f));
        rectPaint.setStyle(Paint.Style.STROKE);

        linePaint.setColor(Color.argb(120,255,255,255));
        linePaint.setStrokeWidth(DisplayUtils.dp2px(0.8f));
        linePaint.setStyle(Paint.Style.STROKE);

        evPaint.setColor(Color.argb(80,255,255,255));
        evPaint.setStrokeWidth(DisplayUtils.dp2px(1f));
        evPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        aimPaint.setColor(getResources().getColor(R.color.colorAccent));
        aimPaint.setStrokeWidth(DisplayUtils.dp2px(1.2f));
        aimPaint.setStyle(Paint.Style.STROKE);

        aimPointPaint.setColor(getResources().getColor(R.color.colorAccent));
        aimPointPaint.setStrokeWidth(DisplayUtils.dp2px(0.6f));
        aimPointPaint.setStyle(Paint.Style.STROKE);
    }

    private void initAnim(){
        if(animator==null){
            animator=ValueAnimator.ofInt(0,10);
            animator.setInterpolator(new LinearInterpolator());
            animator.setDuration(5000);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int v = (int) animation.getAnimatedValue();
                    if(v>5){
                        aimPaint.setAlpha(100);
                        aimPointPaint.setAlpha(100);
                    }else {
                        aimPaint.setAlpha(255);
                        aimPointPaint.setAlpha(255);
                    }
                }
            });
            animator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    isControlledFocus = false;
                    if(onHandFocusListener!=null){
                        onHandFocusListener.onFocusFallBack();
                    }
                    postInvalidate();
                }
            });
        }else {
            animator.cancel();
        }

        if(animatorFocus==null){
            animatorFocus=ValueAnimator.ofFloat(46,32);
            animatorFocus.setDuration(250);
            animatorFocus.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float v = (float) animation.getAnimatedValue();
                    aimRectDis = DisplayUtils.dp2px(v);
                    invalidate();
                }
            });
        } else {
            animatorFocus.cancel();
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(rects!=null) {
            for (Rect rect : rects) {
                canvas.drawRoundRect(rect.left, rect.top, rect.right, rect.bottom, DisplayUtils.dp2px(4), DisplayUtils.dp2px(4), rectPaint);
            }
        }

        if(points!=null){
            for (Point point : points){
                canvas.drawCircle(point.x, point.y,20,rectPaint);
            }
        }

        if(CamSetting.isLineOpend) {
            canvas.drawLine(getRight() / 3, 0, getRight() / 3, getBottom(), linePaint);
            canvas.drawLine(2 * getRight() / 3, 0, 2 * getRight() / 3, getBottom(), linePaint);
            canvas.drawLine(0, (getBottom()-pad) / 3, getRight(), (getBottom()-pad) / 3, linePaint);
            canvas.drawLine(0, 2 * (getBottom()-pad) / 3, getRight(), 2 * (getBottom()-pad) / 3, linePaint);
        }

        if(isControlledFocus && aim!=null) {
            drawAimRect(canvas, aim.x, aim.y);
        }
    }

    public void setDetectedFaces(final Rect[] rects) {
        this.rects = rects;
        postInvalidate();
    }

    public void setDetectedEye(final Point[] eyes) {
        this.points = points;
        postInvalidate();
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

    public void setPad(int pad) {
        Log.e("pad",""+pad);
        this.pad = pad;
        invalidate();
    }

    public void setAspectRatio(int width, int height) {
        if (width < 0 || height < 0) {
            throw new IllegalArgumentException("Size cannot be negative.");
        }
        mRatioWidth = width;
        mRatioHeight = height;
        requestLayout();
    }

    private void drawAimRect(Canvas canvas,int x,int y){
        //左右
        canvas.drawLine(x-aimDistance-aimLength,y,x-aimDistance,y,aimPaint);
        canvas.drawLine(x+aimDistance,y,x+aimLength+aimDistance,y,aimPaint);

        //上下
        canvas.drawLine(x,y-aimDistance-aimLength,x,y-aimDistance,aimPaint);
        canvas.drawLine(x,y+aimDistance,x,y+aimDistance+aimLength,aimPaint);

        //外框左上
        canvas.drawPoint(x-aimRectDis,y-aimRectDis,aimPointPaint);
        canvas.drawLine(x-aimRectDis,y-aimRectDis,x-aimRectDis,y-aimRectDis+aimRectLength,aimPaint);
        canvas.drawLine(x-aimRectDis,y-aimRectDis,x-aimRectDis+aimRectLength,y-aimRectDis,aimPaint);

        //外框右上
        canvas.drawPoint(x+aimRectDis,y-aimRectDis,aimPointPaint);
        canvas.drawLine(x+aimRectDis,y-aimRectDis,x+aimRectDis,y-aimRectDis+aimRectLength,aimPaint);
        canvas.drawLine(x+aimRectDis,y-aimRectDis,x+aimRectDis-aimRectLength,y-aimRectDis,aimPaint);

        //外框右下
        canvas.drawPoint(x+aimRectDis,y+aimRectDis,aimPointPaint);
        canvas.drawLine(x+aimRectDis,y+aimRectDis,x+aimRectDis,y+aimRectDis-aimRectLength,aimPaint);
        canvas.drawLine(x+aimRectDis,y+aimRectDis,x+aimRectDis-aimRectLength,y+aimRectDis,aimPaint);

        //外框左下
        canvas.drawPoint(x-aimRectDis,y+aimRectDis,aimPointPaint);
        canvas.drawLine(x-aimRectDis,y+aimRectDis,x-aimRectDis,y+aimRectDis-aimRectLength,aimPaint);
        canvas.drawLine(x-aimRectDis,y+aimRectDis,x-aimRectDis+aimRectLength,y+aimRectDis,aimPaint);

        int l = getRight() - DisplayUtils.dp2px(24);
        int r = l + DisplayUtils.dp2px(6);
        int t = (getBottom() - getTop())/3;
        int b = 2*(getBottom() - getTop())/3;
        canvas.drawRoundRect(l,t,r,b,DisplayUtils.dp2px(6),DisplayUtils.dp2px(6),evPaint);
    }
}
