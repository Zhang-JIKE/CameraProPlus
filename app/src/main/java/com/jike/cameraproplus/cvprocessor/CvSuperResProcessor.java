package com.jike.cameraproplus.cvprocessor;

import android.graphics.Bitmap;
import android.media.Image;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.daily.flexui.util.AppContextUtils;
import com.jike.cameraproplus.cameradata.CamSetting;
import com.jike.cameraproplus.helper.BitmapHelper;
import com.jike.cameraproplus.helper.ImageToByteArrayHelper;
import com.jike.cameraproplus.imagereader.ProReader;
import com.jike.cameraproplus.pixfomula.PixFormula;
import com.jike.cameraproplus.utils.Camera2Utils;
import com.jike.cameraproplus.utils.Jpeg;
import com.jike.cameraproplus.utils.Yuv420;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


public class CvSuperResProcessor {

    public static int index = 0;

    public static void addOne(Image image){
        int width = image.getWidth();
        int height = image.getHeight();
        index++;
        Log.e("INDEX",":"+index);

        Mat mat;
        if(CamSetting.isYuv) {
            byte[] bytes = ImageToByteArrayHelper.getYuvByteArray(image);
            //byte[] rgb = PixFormula.nv21ToRGB(width,height,bytes);
            //Bitmap bitmap = BitmapHelper.getBitmapFromRGB(rgb, width, height);
            Bitmap bitmap = BitmapHelper.getBitmapFromYuv(bytes, width, height);
            mat = new Mat(width, height, CvType.CV_8UC3);
            Utils.bitmapToMat(bitmap, mat);
            bytes = null;
            bitmap.recycle();
            Imgproc.cvtColor(mat, mat, Imgproc.COLOR_RGB2BGR);
        } else {
                /*byte[] bytes = ImageToByteArrayHelper.getJpegByteArray(image);
                bitmap = BitmapHelper.getBitmapFromJpeg(bytes,width,height);*/
            mat = Jpeg.rgb(image);
        }
        PixFormula.cvMatAverage(mat.getNativeObjAddr());
        mat.release();

        if(index == ProReader.RES_COUNT) {
            SimpleDateFormat sTimeFormat = new SimpleDateFormat("yyyy-MM-dd-hh:mm:ss");
            String date = sTimeFormat.format(new Date());
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/Camera" + "/Super" + date + ".jpg";
            PixFormula.cvMatSaveFather(path);
            index = 0;

            File file2 = new File(path);
            Camera2Utils.galleryAddPic(file2);

            Toast.makeText(AppContextUtils.getAppContext(),"Ok",Toast.LENGTH_SHORT).show();
        }
    }

}
