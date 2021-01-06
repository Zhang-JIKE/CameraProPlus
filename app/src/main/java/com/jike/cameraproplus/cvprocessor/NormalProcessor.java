package com.jike.cameraproplus.cvprocessor;

import android.graphics.Bitmap;
import android.media.Image;
import android.os.Environment;
import android.util.Log;


import com.daily.flexui.util.AppContextUtils;
import com.jike.cameraproplus.cameradata.CamSetting;
import com.jike.cameraproplus.helper.BitmapHelper;
import com.jike.cameraproplus.helper.ImageToByteArrayHelper;
import com.jike.cameraproplus.neuralengine.DenoiseEngine;
import com.jike.cameraproplus.pixfomula.PixFormula;
import com.jike.cameraproplus.utils.Camera2Utils;
import com.jike.cameraproplus.utils.Yuv420;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;


public class NormalProcessor implements Runnable {

    private final Queue<Image> mImageQueue = new LinkedList<>();

    public NormalProcessor() {
    }

    public void addImage(Image image){
        mImageQueue.add(image);
    }


    @Override
    public void run() {
        synchronized (mImageQueue) {
            while (!mImageQueue.isEmpty()) {
                Image image = mImageQueue.poll();
                int width = image.getWidth();
                int height = image.getHeight();

                if(CamSetting.isYuv) {
//                    /*Mat直接存储*/
//                    Mat mat = Yuv420.rgb(image);
//                    String path = getPath("Yuv");
//                    Imgcodecs.imwrite(path,mat);
//                    Camera2Utils.galleryAddPic(path);

                    /*YuvImage存储*/
                    byte[] bytes = ImageToByteArrayHelper.getYuvByteArray(image);
                    Bitmap bitmap = BitmapHelper.getBitmapFromYuv(bytes, width, height);
                    BitmapHelper.saveBitmap("Yuv",bitmap);
//
//                    /*Yuv转Rgb-native存储*/
//                    byte[] bytes = ImageToByteArrayHelper.getYuvByteArray(image);
//                    byte[] rgb = PixFormula.nv21ToRGB(width,height,bytes);
//                    Bitmap bitmap = BitmapHelper.getBitmapFromRGB(rgb, width, height);
//                    BitmapHelper.saveBitmap("Yuv",bitmap);
//
//                    /*Yuv转Rgb-libyuv存储*/
//                    byte[] bytes = ImageToByteArrayHelper.getYuvByteArray(image);
//                    byte[] rgb = PixFormula.nv21ToRGBFromLibYuv(width,height,bytes);
//                    Bitmap bitmap = BitmapHelper.getBitmapFromRGB(rgb, width, height);
//                    BitmapHelper.saveBitmap("Yuv",bitmap);



                    /*float[] pixels = PixFormula.bytes2Float(width,height,rgb);

                    DenoiseEngine denoiseEngine = new DenoiseEngine(AppContextUtils.getAppActivity().getAssets());
                    float[] nowpixels = denoiseEngine.process(pixels,width,height);
                    byte[] nowrgb = PixFormula.float2Byte(width,height,nowpixels);*/
                } else {
                    /*byte[] bytes = ImageToByteArrayHelper.getJpegByteArray(image);
                    Bitmap bitmap = BitmapHelper.getBitmapFromJpeg(bytes, width, height);
                    BitmapHelper.saveBitmap("Jpg",bitmap);*/
                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);
                    FileOutputStream output = null;
                    String path = getPath("Jpg");
                    try {
                        File file = new File(path);
                        output = new FileOutputStream(file);
                        output.write(bytes);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        image.close();
                        Camera2Utils.galleryAddPic(path);
                        if (null != output) {
                            try {
                                output.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

        }
    }


    public String getPath(String title){
        SimpleDateFormat sTimeFormat=new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss-SS");
        //SimpleDateFormat sTimeFormat=new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
        String date=sTimeFormat.format(new Date());

        String finalP = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/Camera" + "/CPro-" + title + date + ".jpg";
        return finalP;
    }
}