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


public class CvHdrQueueProcessor extends CvImageQueueProcessor {

    public static float times[] = new float[3];

    public CvHdrQueueProcessor(int maxSize) {
        super(maxSize);
    }

    @Override
    protected void process(long[] addrs) {
        SimpleDateFormat sTimeFormat=new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
        String date = sTimeFormat.format(new Date());
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()+"/Camera"+"/Hdr"+date+".jpg";
        PixFormula.cvMatHdr(addrs,times,path);

        File file = new File(path);
        Camera2Utils.galleryAddPic(file);
    }


    @Override
    protected void release(Mat[] mats) {
    }
}
