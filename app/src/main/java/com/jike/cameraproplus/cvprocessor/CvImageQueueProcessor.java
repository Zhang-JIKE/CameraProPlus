package com.jike.cameraproplus.cvprocessor;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.annotation.NonNull;

import com.jike.cameraproplus.cameradata.CamSetting;
import com.jike.cameraproplus.helper.BitmapHelper;
import com.jike.cameraproplus.helper.ImageToByteArrayHelper;
import com.jike.cameraproplus.imagereader.ProReader;
import com.jike.cameraproplus.interfaces.CalculateListener;
import com.jike.cameraproplus.interfaces.CaptureListenerHelper;
import com.jike.cameraproplus.pixfomula.PixFormula;
import com.jike.cameraproplus.utils.Jpeg;
import com.jike.cameraproplus.utils.Yuv420;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.nio.Buffer;
import java.nio.ByteBuffer;

public abstract class CvImageQueueProcessor {

    public static final int START_CAL = 0x000111;
    public static final int OVER_CAL = 0x000222;
    public static final int GET_MAT = 0x000333;

    protected int maxSize;

    private final Mat[] mats;
    private final long[] matAddrs;
    private final Image[] images;

    @SuppressLint("HandlerLeak")
    protected Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {

            if(msg.what == START_CAL){
                if(CaptureListenerHelper.captureListener !=null){
                    CaptureListenerHelper.captureListener.onStartToCalculate();
                }
            }else if(msg.what == OVER_CAL){
                if(CaptureListenerHelper.captureListener!=null){
                    CaptureListenerHelper.captureListener.onCalculateFinished();
                }
            }else if(msg.what == GET_MAT){
                Message message = new Message();;
                message.what = START_CAL;
                handler.sendMessage(message);

                new Thread(){
                    @Override
                    public void run() {
                        process(matAddrs);
                        Message message2 = new Message();
                        message2.what = OVER_CAL;
                        handler.sendMessage(message2);
                    }
                }.start();
            }
        }
    };

    public CvImageQueueProcessor(int maxSize) {
        this.maxSize = maxSize;
        this.mats = new Mat[maxSize];
        this.matAddrs = new long[maxSize];
        this.images = new Image[maxSize];
    }

    public void addImage(Image image,int idx){
        images[idx] = image;
        if(idx == maxSize-1){
            ImageThread imageThread = new ImageThread();
            imageThread.start();
        }
    }

    class ImageThread extends Thread{
        @Override
        public void run() {
            for(int i = 0; i < maxSize; i++) {
                int width = images[i].getWidth();
                int height = images[i].getHeight();

                Bitmap bitmap;
                Mat mat;
                if (CamSetting.isYuv) {
                    byte[] bytes = ImageToByteArrayHelper.getYuvByteArray(images[i]);
                    //byte[] rgb = PixFormula.nv21ToRGB(width,height,bytes);
                    //bitmap = BitmapHelper.getBitmapFromRGB(rgb, width, height);
                    bitmap = BitmapHelper.getBitmapFromYuv(bytes, width, height);
                    mat = new Mat(width, height, CvType.CV_8UC3);
                    Utils.bitmapToMat(bitmap, mat);
                    bitmap.recycle();
                    Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2BGR);
                    //mat = Yuv420.rgb(images[i]);
                } else {
                /*byte[] bytes = ImageToByteArrayHelper.getJpegByteArray(image);
                bitmap = BitmapHelper.getBitmapFromJpeg(bytes,width,height);*/
                    mat = Jpeg.rgb(images[i]);
                }
                mats[i] = mat;
                matAddrs[i] = mats[i].getNativeObjAddr();
            }
            Message message = new Message();;
            message.what = GET_MAT;
            handler.sendMessage(message);
        }
    }

    protected abstract void process(long[] matAddrs);
    protected abstract void release(Mat[] mats);
}