package com.jike.cameraproplus.pixfomula;

import android.graphics.Bitmap;

import org.opencv.core.Mat;

public class PixFormula {

    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("opencv_java4");
        System.loadLibrary("yuv");
    }

    public static void cvMatFusion(long[] matAddrs, String path){
        cvMatFusion(matAddrs,path,false);
    }

    public static native void cvMatFusion(long[] matAddrs, String path, boolean isAccel);

    public static native void cvMatHdr(long[] matAddrs, float[] times, String path);

    public static native void cvMatSave(long matAddr, String path);

    public static native void cvMatSaveFather(String path);

    public static native void cvMatAverage(long matAddr);

    public static native int getBrightness(Bitmap bitmap);

    public static native byte[] nv21ToRGB(int w, int h, byte[] nv21);

    public static native byte[] nv21ToRGBFromLibYuv(int w, int h, byte[] nv21);

    public static native byte[] i420ToRGB(int w, int h, byte[] src);

    public static native float[] bytes2Float(int w, int h, byte[] rgb);

    public static native byte[] float2Byte(int w, int h, float[] rgb);

}
