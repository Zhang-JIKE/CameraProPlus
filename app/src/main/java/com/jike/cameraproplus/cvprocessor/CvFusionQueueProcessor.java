package com.jike.cameraproplus.cvprocessor;

import android.os.Environment;
import android.widget.Toast;


import com.daily.flexui.util.AppContextUtils;
import com.jike.cameraproplus.cameradata.CamSetting;
import com.jike.cameraproplus.interfaces.CalculateListener;
import com.jike.cameraproplus.pixfomula.PixFormula;
import com.jike.cameraproplus.utils.Camera2Utils;

import org.opencv.core.Mat;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


public class CvFusionQueueProcessor extends CvImageQueueProcessor {

    public CvFusionQueueProcessor(int maxSize) {
        super(maxSize);
    }

    @Override
    protected void process(long[] addrs) {
        SimpleDateFormat sTimeFormat=new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
        String date = sTimeFormat.format(new Date());
        String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()+"/Camera"+"/Fusion"+date+".jpg";

        long t1 = System.currentTimeMillis();
        PixFormula.cvMatFusion(addrs,path, CamSetting.isAccel);
        final long t2 = System.currentTimeMillis() - t1;

        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(AppContextUtils.getAppContext(),"用时" + t2+"ms",Toast.LENGTH_LONG).show();
            }
        });

        File file = new File(path);
        Camera2Utils.galleryAddPic(file);
    }

    @Override
    protected void release(Mat[] mats) {

    }

}
