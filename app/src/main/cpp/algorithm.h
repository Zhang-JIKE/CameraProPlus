//
// Created by 18793 on 2020/8/28.
//

#ifndef CAMERAPROPLUS_ALGORITHM_H
#define CAMERAPROPLUS_ALGORITHM_H

#endif //CAMERAPROPLUS_ALGORITHM_H
#include <opencv2/opencv.hpp>
#include "yuv2rgb.h"
using namespace cv;
#define pi 3.1415926

void HighLightDetect(Mat src,Mat &mask, float scale){
    Mat mat_grey;
    cvtColor(src,mat_grey,COLOR_BGR2GRAY);
    threshold(mat_grey,mask, 200, 255, THRESH_BINARY);
    //cvtColor(mask,mask,CV_BGR2GRAY);
    resize(mask,mask,Size(mask.cols*scale,mask.rows*scale),0,0,INTER_CUBIC);
    imwrite("sdcard/mask.jpg",mask);
}

std::vector<Mat> cropMat(Mat src,int rows,int cols) {
    std::vector<Mat> array;
    int origin_rows = src.rows, origin_cols = src.cols;//原图像的行数、列数
    int now_rows = origin_rows / rows, now_cols = origin_cols / cols;//分割后的小块图像的行数、列数

    int idx = 0;
    for(int i = 0; i < rows; i++) {
        for (int j = 0; j < cols; j++) {
            int x = j * now_cols, y = i * now_rows;
            Mat cell = src(Rect(x, y, now_cols, now_rows));
            array.push_back(cell);
            idx++;
        }
    }
    return array;
}

void yuv420ToRgbA(int w,int h,unsigned char *yuv,unsigned char *rgba){
    int size = w * h;

    int y, u, v;
    int r, g, b;
    int index;
    for (int i = 0; i < h; i++) {
        for (int j = 0; j < w; j++) {
            index = j % 2 == 0 ? j : j - 1;

            y = yuv[w * i + j] & 0xff;
            u = yuv[w * h + w * (i / 2) + index + 1] & 0xff;
            v = yuv[w * h + w * (i / 2) + index] & 0xff;

            /*r = y + ((360 * (v - 128))>>8) ;
            g = y - (( ( 88 * (u - 128)  + 184 * (v - 128)) )>>8) ;
            b = y +((455 * (u - 128))>>8) ;*/

            /*r = (298*y + 411 * u - 57344)>>8;
            g = (298*y - 101* u - 211* v+ 34739)>>8;
            b = (298*y + 519* u- 71117)>>8;*/

            /*r = y + 1.417 * (v - 128);
            g = y - 0.34414 * (u - 128) - 0.71414 * (v - 128);
            b = y + 1.772 * (u - 128);*/


            r = y + 1.4075*(v - 128);

            g = y - 0.3455*(u - 128) - 0.7169*(v - 128);

            b = y + 1.779*(u - 128);

            r = r < 0 ? 0 : (r > 255 ? 255 : r);
            g = g < 0 ? 0 : (g > 255 ? 255 : g);
            b = b < 0 ? 0 : (b > 255 ? 255 : b);

            rgba[w * i * 4 + j * 4 + 0] = (int)r;
            rgba[w * i * 4 + j * 4 + 1] = (int)g;
            rgba[w * i * 4 + j * 4 + 2] = (int)b;
            rgba[w * i * 4 + j * 4 + 3] = 255;//透明度
        }
    }

    //nv21_to_rgba(rgba, 255, yuv, w, h);
}

void mycanny(Mat src, Mat &dst, int T_value = 50) {
    Mat gauss, gray;
    GaussianBlur(src, gauss, Size(3, 3), 0, 0, 4);
    cvtColor(gauss, gray, COLOR_BGR2GRAY);
    Canny(gray, dst, T_value / 2, T_value, 3, false);
}

void calcPSF(Mat& outputImg, Size filterSize, int len, double theta)
{
    Mat h(filterSize, CV_32F, Scalar(0));
    Point point(filterSize.width / 2, filterSize.height / 2);
    ellipse(h, point, Size(0, cvRound(float(len) / 2.0)), 90.0 - theta, 0, 360, Scalar(255), FILLED);
    Scalar summa = sum(h);
    outputImg = h / summa[0];
}
void fftshift(const Mat& inputImg, Mat& outputImg)
{
    outputImg = inputImg.clone();
    int cx = outputImg.cols / 2;
    int cy = outputImg.rows / 2;
    Mat q0(outputImg, Rect(0, 0, cx, cy));
    Mat q1(outputImg, Rect(cx, 0, cx, cy));
    Mat q2(outputImg, Rect(0, cy, cx, cy));
    Mat q3(outputImg, Rect(cx, cy, cx, cy));
    Mat tmp;
    q0.copyTo(tmp);
    q3.copyTo(q0);
    tmp.copyTo(q3);
    q1.copyTo(tmp);
    q2.copyTo(q1);
    tmp.copyTo(q2);
}
void filter2DFreq(const Mat& inputImg, Mat& outputImg, const Mat& H)
{
    Mat planes[2] = { Mat_<float>(inputImg.clone()), Mat::zeros(inputImg.size(), CV_32F) };
    Mat complexI;
    merge(planes, 2, complexI);
    dft(complexI, complexI, DFT_SCALE);
    Mat planesH[2] = { Mat_<float>(H.clone()), Mat::zeros(H.size(), CV_32F) };
    Mat complexH;
    merge(planesH, 2, complexH);
    Mat complexIH;
    mulSpectrums(complexI, complexH, complexIH, 0);
    idft(complexIH, complexIH);
    split(complexIH, planes);
    outputImg = planes[0];
}
void calcWnrFilter(const Mat& input_h_PSF, Mat& output_G, double nsr)
{
    Mat h_PSF_shifted;
    fftshift(input_h_PSF, h_PSF_shifted);
    Mat planes[2] = { Mat_<float>(h_PSF_shifted.clone()), Mat::zeros(h_PSF_shifted.size(), CV_32F) };
    Mat complexI;
    merge(planes, 2, complexI);
    dft(complexI, complexI);
    split(complexI, planes);
    Mat denom;
    pow(abs(planes[0]), 2, denom);
    denom += nsr;
    divide(planes[0], denom, output_G);
}
void edgetaper(const Mat& inputImg, Mat& outputImg, double gamma = 5.0, double beta = 0.2)
{
    int Nx = inputImg.cols;
    int Ny = inputImg.rows;
    Mat w1(1, Nx, CV_32F, Scalar(0));
    Mat w2(Ny, 1, CV_32F, Scalar(0));
    float* p1 = w1.ptr<float>(0);
    float* p2 = w2.ptr<float>(0);
    float dx = float(2.0 * CV_PI / Nx);
    float x = float(-CV_PI);
    for (int i = 0; i < Nx; i++)
    {
        p1[i] = float(0.5 * (tanh((x + gamma / 2) / beta) - tanh((x - gamma / 2) / beta)));
        x += dx;
    }
    float dy = float(2.0 * CV_PI / Ny);
    float y = float(-CV_PI);
    for (int i = 0; i < Ny; i++)
    {
        p2[i] = float(0.5 * (tanh((y + gamma / 2) / beta) - tanh((y - gamma / 2) / beta)));
        y += dy;
    }
    Mat w = w2 * w1;

    multiply(inputImg, w, outputImg);
}

Mat blurFix(Mat imgIn,int len=125, double theta=0, int snr=700){
    Mat imgOut;
    // it needs to process even image only
    LOGD("1111111111");
    Rect roi = Rect(0, 0, imgIn.cols & -2, imgIn.rows & -2);
    //Hw calculation (start)
    LOGD("1111111111");
    Mat Hw, h;
    calcPSF(h, roi.size(), len, theta);
    LOGD("1111111111");

    calcWnrFilter(h, Hw, 1.0 / double(snr));
    LOGD("1111111111");

    //Hw calculation (stop)
    imgIn.convertTo(imgIn, CV_32F);
    LOGD("1111111111");

    edgetaper(imgIn, imgIn);
    LOGD("1111111111");

    // filtering (start)
    filter2DFreq(imgIn(roi), imgOut, Hw);
    LOGD("1111111111");

    // filtering (stop)
    imgIn.convertTo(imgOut, CV_8U);
    LOGD("1111111111");

    normalize(imgOut, imgOut, 0, 255, NORM_MINMAX);
    LOGD("1111111111");

    return imgOut;
}
