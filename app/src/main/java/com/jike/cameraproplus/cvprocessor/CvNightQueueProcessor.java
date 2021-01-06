package com.jike.cameraproplus.cvprocessor;

import android.os.Environment;

import com.jike.cameraproplus.cameradata.CamSetting;
import com.jike.cameraproplus.interfaces.CalculateListener;
import com.jike.cameraproplus.pixfomula.PixFormula;
import com.jike.cameraproplus.utils.Camera2Utils;

import org.opencv.core.Mat;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


public class CvNightQueueProcessor extends CvImageQueueProcessor {

    public float[] times = new float[]{1/32f,1/16f,1/8f,1/4f};

    public CvNightQueueProcessor(int maxSize) {
        super(maxSize);
    }

    @Override
    protected void process(long[] addrs) {
        SimpleDateFormat sTimeFormat=new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
        String date = sTimeFormat.format(new Date());
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()+"/Camera"+"/Night-Merge"+date+".jpg";

        PixFormula.cvMatFusion(addrs,path, CamSetting.isAccel);

        File file2 = new File(path);
        Camera2Utils.galleryAddPic(file2);
    }

    @Override
    protected void release(Mat[] mats) {

    }
}
