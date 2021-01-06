package com.jike.cameraproplus.utils;

import android.media.Image;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.nio.ByteBuffer;

public class Jpeg {

    private static Mat jpeg(Image image) {
        Image.Plane plan = image.getPlanes()[0];
        ByteBuffer data = plan.getBuffer();
        data.position(0);
        // 从通道读取的图片为JPEG，并不能直接使用，
        // 将其保存在一维数组里
        return new Mat(1, data.remaining(), CvType.CV_8U, data);
    }

    public static Mat rgb(Image image) {
        Mat jpeg = jpeg(image);
        // 通过cv::imdecode将其解析为彩色图
        Mat mat = Imgcodecs.imdecode(jpeg, Imgcodecs.IMREAD_COLOR);
        jpeg.release();
        image.close();
        return mat;
    }

    public static Mat gray(Image image) {
        Mat jpeg = jpeg(image);
        // 通过cv::imdecode将其解析为灰色图
        Mat mat = Imgcodecs.imdecode(jpeg, Imgcodecs.IMREAD_GRAYSCALE);
        jpeg.release();
        return mat;
    }
}