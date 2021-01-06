package com.jike.cameraproplus.helper;

import android.media.Image;

import java.nio.ByteBuffer;

public class ImageToByteArrayHelper {

    public static byte[] getYuvByteArrayForOld(Image image){
        int w = image.getWidth(), h = image.getHeight();
        // size是宽乘高的1.5倍 可以通过ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888)得到
        int i420Size = w * h * 3 / 2;

        Image.Plane[] planes = image.getPlanes();
        int remaining0 = planes[0].getBuffer().remaining();
        int remaining1 = planes[1].getBuffer().remaining();
        int remaining2 = planes[2].getBuffer().remaining();
        int pixelStride = planes[2].getPixelStride();
        int rowOffest = planes[2].getRowStride();
        byte[] nv21 = new byte[i420Size];
        byte[] yRawSrcBytes = new byte[remaining0];
        byte[] uRawSrcBytes = new byte[remaining1];
        byte[] vRawSrcBytes = new byte[remaining2];
        planes[0].getBuffer().get(yRawSrcBytes);
        planes[1].getBuffer().get(uRawSrcBytes);
        planes[2].getBuffer().get(vRawSrcBytes);

        if (pixelStride == w) {
            System.arraycopy(yRawSrcBytes, 0, nv21, 0, rowOffest * h);
            System.arraycopy(vRawSrcBytes, 0, nv21, rowOffest * h, rowOffest * h / 2 - 1);
        } else {
            byte[] ySrcBytes = new byte[w * h];
            byte[] uSrcBytes = new byte[w * h / 2 - 1];
            byte[] vSrcBytes = new byte[w * h / 2 - 1];
            for (int row = 0; row < h; row++) {
                System.arraycopy(yRawSrcBytes, rowOffest * row, ySrcBytes, w * row, w);
                if (row % 2 == 0) {
                    if (row == h - 2) {
                        System.arraycopy(vRawSrcBytes, rowOffest * row / 2, vSrcBytes, w * row / 2, w - 1);
                    } else {
                        System.arraycopy(vRawSrcBytes, rowOffest * row / 2, vSrcBytes, w * row / 2, w);
                    }
                }
            }
            System.arraycopy(ySrcBytes, 0, nv21, 0, w * h);
            System.arraycopy(vSrcBytes, 0, nv21, w * h, w * h / 2 - 1);
        }
        image.close();

        return nv21;
    }

    public static byte[] getYuvByteArray(Image image){
        int imgWidth = image.getWidth();
        int imgHeight = image.getHeight();
        int imageStride = image.getPlanes()[0].getRowStride();

        int ySize = image.getPlanes()[0].getBuffer().remaining();
        int uvSize = image.getPlanes()[1].getBuffer().remaining();
        int rowStride = image.getPlanes()[0].getRowStride();
        int offset = rowStride * imgHeight;

        byte[] resultByteArray = new byte[imageStride * imgHeight * 3 / 2];
        image.getPlanes()[0].getBuffer().get(resultByteArray, 0, ySize);
        image.getPlanes()[2].getBuffer().get(resultByteArray, offset, uvSize);

        image.close();
        return resultByteArray;
        //return yuv420ToNv21(image);
    }

    public static byte[] yuv420ToNv21(Image image) {
        Image.Plane[] planes = image.getPlanes();
        ByteBuffer yBuffer = planes[0].getBuffer();
        ByteBuffer uBuffer = planes[1].getBuffer();
        ByteBuffer vBuffer = planes[2].getBuffer();
        int ySize = yBuffer.remaining();
        int uSize = uBuffer.remaining();
        int vSize = vBuffer.remaining();
        int size = image.getWidth() * image.getHeight();
        byte[] nv21 = new byte[size * 3 / 2];
        yBuffer.get(nv21, 0, ySize);
        vBuffer.get(nv21, ySize, vSize);

        byte[] u = new byte[uSize];
        uBuffer.get(u);

        //每隔开一位替换V，达到VU交替
        int pos = ySize + 1;
        for (int i = 0; i < uSize; i++) {
            if (i % 2 == 0) {
                nv21[pos] = u[i];
                pos += 2;
            }
        }
        image.close();
        return nv21;
    }

    public static byte[] getJpegByteArray(Image image){
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);

        image.close();
        return bytes;
    }

    public static byte[] getRawByteArray(Image image){
        Image.Plane plane0 = image.getPlanes()[0];
        ByteBuffer buffer = plane0.getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes, 0, bytes.length);
        image.close();
        return bytes;
    }
}
