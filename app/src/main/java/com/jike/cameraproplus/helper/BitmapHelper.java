package com.jike.cameraproplus.helper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.os.Environment;
import android.widget.Toast;

import com.daily.flexui.util.AppContextUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.jike.cameraproplus.utils.Camera2Utils.galleryAddPic;


public class BitmapHelper {

    public static void createBmpByPixels(int[] colors,String title,int width, int height) {
        Bitmap bmp = Bitmap.createBitmap(colors,width, height, Bitmap.Config.ARGB_8888);

        SimpleDateFormat sTimeFormat=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String date=sTimeFormat.format(new Date());
        File mFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()+"/Camera"+"/CamPro-"+title+date+".jpg");
        Toast.makeText(AppContextUtils.getAppContext(),"保存"+mFile.getAbsolutePath(),Toast.LENGTH_LONG).show();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mFile);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            galleryAddPic(mFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void createBmpByPixels(byte[] res, String title, int width, int height) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(res);
        Bitmap bitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(byteBuffer);

        SimpleDateFormat sTimeFormat=new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
        String date=sTimeFormat.format(new Date());
        File mFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()+"/Camera"+"/CamPro-"+title+date+".jpg");
        Toast.makeText(AppContextUtils.getAppContext(),"保存"+mFile.getAbsolutePath(),Toast.LENGTH_SHORT).show();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();

            galleryAddPic(mFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            bitmap.recycle();
        }
    }

    public static byte[] NV21toRGBA(byte[] data, int width, int height) {
        int size = width * height;
        byte[] bytes = new byte[size * 4];
        int y, u, v;
        int r, g, b;
        int index;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                index = j % 2 == 0 ? j : j - 1;

                y = data[width * i + j] & 0xff;
                u = data[width * height + width * (i / 2) + index + 1] & 0xff;
                v = data[width * height + width * (i / 2) + index] & 0xff;

                r = y + (int) 1.370705f * (v - 128);
                g = y - (int) (0.698001f * (v - 128) + 0.337633f * (u - 128));
                b = y + (int) 1.732446f * (u - 128);

                r = r < 0 ? 0 : (r > 255 ? 255 : r);
                g = g < 0 ? 0 : (g > 255 ? 255 : g);
                b = b < 0 ? 0 : (b > 255 ? 255 : b);

                bytes[width * i * 4 + j * 4 + 0] = (byte) r;
                bytes[width * i * 4 + j * 4 + 1] = (byte) g;
                bytes[width * i * 4 + j * 4 + 2] = (byte) b;
                bytes[width * i * 4 + j * 4 + 3] = (byte) 255;//透明度
            }
        }
        return bytes;
    }

    public static Bitmap getBitmapFromYuv(byte[] data, int width, int height) {
        YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        yuvimage.compressToJpeg(new Rect(0, 0, width, height), 100, baos);
        byte[] jdata = baos.toByteArray();
        BitmapFactory.Options bitmapFatoryOptions = new BitmapFactory.Options();
        bitmapFatoryOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bmp = BitmapFactory.decodeByteArray(jdata, 0, jdata.length, bitmapFatoryOptions);
        return bmp;
    }

    public static Bitmap getBitmapFromJpeg(byte[] data, int width, int height){

        BitmapFactory.Options bitmapFatoryOptions = new BitmapFactory.Options();
        bitmapFatoryOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length, bitmapFatoryOptions);

        return bmp;
    }

    public static Bitmap getBitmapFromRGB(byte[] colors, int width, int height){
        ByteBuffer byteBuffer = ByteBuffer.wrap(colors);
        Bitmap bitmap = Bitmap.createBitmap(width,height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(byteBuffer);

        return bitmap;
    }

    public static void saveBitmap(String title, Bitmap bitmap) {
        SimpleDateFormat sTimeFormat=new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss-SS");
        //SimpleDateFormat sTimeFormat=new SimpleDateFormat("yyyy-MM-dd-hh-mm-ss");
        String date=sTimeFormat.format(new Date());

        File mFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath() + "/Camera" + "/CPro-" + title + date + ".jpg");
        Toast.makeText(AppContextUtils.getAppContext(), "保存" + mFile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(mFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();

            galleryAddPic(mFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void saveYuvImage(String path, byte[] outdata, int width, int height){

        YuvImage yuvImage = new YuvImage(outdata, ImageFormat.NV21, width, height, null);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        yuvImage.compressToJpeg(new Rect(0, 0, width, height), 100,
                byteArrayOutputStream);
        byte[] jpegData = byteArrayOutputStream.toByteArray();

        File file = new File(path);

        try {
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(jpegData, 0, jpegData.length);
            fos.flush();
            fos.close();

            Toast.makeText(AppContextUtils.getAppContext(), "保存" + file.getAbsolutePath(), Toast.LENGTH_LONG).show();

            galleryAddPic(file);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
