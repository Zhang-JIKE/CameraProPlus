package com.jike.cameraproplus.cameradata;

import android.content.SharedPreferences;
import android.util.Size;

import com.daily.flexui.util.AppContextUtils;
import com.jike.cameraproplus.utils.DataUtils;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class CamSize {

    public static final String PIC_INDEX = "REAR_PIC_SIZE_POS";
    public static final String VID_INDEX = "REAR_VIDEO_SIZE_POS";

    public static int picIndex = 0;
    //后置拍照所有尺寸
    private static List<Size> picSizes = new ArrayList<>();

    public static List<Size> getPicSizes() {
        List<Size> sizes = new ArrayList<>();
        for(Size size:picSizes){
            if(size.getWidth() >= getMaxSize(picSizes).getWidth()/2) {
                if (size.getWidth() / size.getHeight() == 4 / 3 ||
                        size.getWidth() / size.getHeight() == 16 / 9 ||
                        size.getWidth() / size.getHeight() == 18 / 9 ||
                        size.getWidth() / size.getHeight() == 4 / 3) {
                    sizes.add(size);
                }
            }
        }
        return picSizes;
    }

    //后置录像设置角标
    public static int videoIndex = 0;
    //后置录像所有尺寸
    private static List<Size> videoSizes = new ArrayList<>();

    public static List<Size> getVideoSizes() {
        List<Size> sizes = new ArrayList<>();
        for(Size size : videoSizes){
            if(size.getHeight() >= 1080 && size.getWidth() >= 1920) {
                if ((float)size.getWidth() / size.getHeight() == (float)16 / 9) {
                    sizes.add(size);
                }
            }
        }
        return sizes;
    }

    //直取后置拍照尺寸
    public static Size getPicSize(){
        return getPicSizes().get(picIndex);
    }

    public static float getPicRatio(){
        return (float) getPicSize().getWidth()/(float) getPicSize().getHeight();
    }
    //直取后置录像尺寸
    public static Size getRearVideoSize(){
        return getVideoSizes().get(videoIndex);
    }

    private static Size getMaxSize(List<Size> sizes){
        Size maxSize = sizes.get(0);
        for(Size size:sizes){
            if(size.getWidth()+size.getHeight()>maxSize.getWidth()+maxSize.getHeight()){
                maxSize = size;
            }
        }
        return maxSize;
    }

    public static void setPicSizes(List<Size> rearPicSizes) {
        CamSize.picSizes = rearPicSizes;
    }

    public static void setVideoSizes(List<Size> rearVideoSizes) {
        CamSize.videoSizes = rearVideoSizes;
    }

    public static void setPicIndex(int index) {
        CamSize.picIndex = index;
        DataUtils.saveSettings(PIC_INDEX, index);
    }
    public static void setVideoIndex(int index) {
        CamSize.videoIndex = index;
        DataUtils.saveSettings(VID_INDEX, index);
    }

    public static void initSettings(){
        SharedPreferences mSpf = AppContextUtils.getAppContext().getSharedPreferences("Settings",MODE_PRIVATE);
        picIndex = mSpf.getInt(PIC_INDEX, 0);
        videoIndex = mSpf.getInt(VID_INDEX, 0);
    }
}
