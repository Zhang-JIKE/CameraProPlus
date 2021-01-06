package com.jike.cameraproplus.interfaces;

import android.graphics.Point;
import android.view.MotionEvent;
import android.view.View;

import java.text.DecimalFormat;

public class CameraTouchListener implements View.OnTouchListener {

    private float oldX;
    private float oldY;
    private float oldX1,oldX2,oldY1,oldY2;

    private float oldScale = 0;
    private float newScale;

    private boolean isMultipleFingerDown = false;
    private final float resistNum = 2000;

    private FingerListener fingerListener;

    public void setFingerListener(FingerListener fingerListener) {
        this.fingerListener = fingerListener;
    }

    public interface FingerListener{
        void onScaleChanged(float scaleV, String scaleS);
        void onFocusSelected(Point point);
        void onSlideToRight();
        void onSlideToLeft();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            oldX = event.getX();
            oldY = event.getY();
        }else if(event.getAction() == MotionEvent.ACTION_MOVE){
            if(event.getPointerCount()==2){
                if(!isMultipleFingerDown){
                    oldX1 = event.getX(0);
                    oldX2 = event.getX(1);
                    oldY1 = event.getY(0);
                    oldY2 = event.getY(1);
                    isMultipleFingerDown = true;
                }
                float dx = event.getX(0) - event.getX(1);
                float dy = event.getY(0) - event.getY(1);
                float odx = oldX1 - oldX2 ;
                float ody = oldY1 - oldY2 ;
                float value = 10*(float)(Math.sqrt(dx * dx + dy * dy)-Math.sqrt(odx * odx + ody * ody))/resistNum;
                newScale = value + oldScale;
                if(newScale<1) {
                    newScale = 1;
                }
                if(newScale>10){
                    newScale = 10;
                }

                if(fingerListener!=null) {
                    fingerListener.onScaleChanged(newScale,scale2String(newScale));
                }
            }
        }else if(event.getAction() == MotionEvent.ACTION_UP) {
            if (isMultipleFingerDown) {
                isMultipleFingerDown = false;
            } else {
                float deltaX = event.getX() - oldX;
                float deltaY = event.getY() - oldY;
                if (Math.abs(deltaX) < 10 && Math.abs(deltaY) < 10) {
                    if(fingerListener!=null) {
                        fingerListener.onFocusSelected(new Point((int) event.getX(), (int) event.getY()));
                    }
                } else if (Math.abs(deltaX) > Math.abs(deltaY)) {
                    if (deltaX > 10) {
                        if(fingerListener!=null) {
                            fingerListener.onSlideToRight();
                        }
                    } else if (deltaX < -10) {
                        if(fingerListener!=null) {
                            fingerListener.onSlideToLeft();
                        }
                    }
                }
            }
            oldScale = newScale;
        }
        return true;
    }

    private String scale2String(float scaleTime){
        DecimalFormat mFormat = new DecimalFormat(".0");
        String formatNum = mFormat.format(scaleTime);
        if (formatNum.contains(".0")) {
            formatNum = formatNum.substring(0, formatNum.indexOf("."));
        }
        return formatNum + "x";
    }
}
