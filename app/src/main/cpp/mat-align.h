//
// Created by 18793 on 2020/8/28.
//
#include <jni.h>
#include <arm_neon.h>
#include <string>
#include <time.h>
#include <math.h>
#include <android/log.h>
#include <android/bitmap.h>
#include <iostream>
#include <fstream>
#include <vector>
#include "log.h"
#include <opencv2/opencv.hpp>
#include <opencv2/features2d.hpp>
#include <opencv2/xfeatures2d.hpp>
#include <opencv2/superres.hpp>
#include <opencv2/core.hpp>
#include <opencv2/core/ocl.hpp>
#include "algorithm.h"

#define MAX_CORNERS 1000

using namespace cv;
using namespace std;
using namespace xfeatures2d;
using namespace superres;
using namespace cuda;

double getPSNR(Mat I1, Mat I2)
{
    Mat s1;
    absdiff(I1, I2, s1);       // |I1 - I2|
    s1.convertTo(s1, CV_32F);  // cannot make a square on 8 bits
    s1 = s1.mul(s1);           // |I1 - I2|^2

    Scalar s = sum(s1);         // sum elements per channel

    double sse = s.val[0] + s.val[1] + s.val[2]; // sum channels

    if( sse <= 1e-10) // for small values return zero
        return 0;
    else
    {
        double  mse =sse /(double)(I1.channels() * I1.total());
        double psnr = 10.0*log10((255*255)/mse);
        return psnr;
    }
}

void KeyPointsToPoints(const vector<KeyPoint>& kps, vector<Point2f>& ps)
{
    ps.clear();
    for (unsigned int i = 0; i<kps.size(); i++)
        ps.push_back(kps[i].pt);
}

static vector<KeyPoint> key_points_2;//将提取的特征点以keypoint形式存储
static Mat dstImage2;

void MatAlignSurf(Mat img1,Mat img2,Mat &res,bool isFirst){
    LOGD("开始特征点检测");
    long t1 = get_current_ms();
    long t2;
    //Ptr<AKAZE> detector = AKAZE::create();
    Ptr<FeatureDetector> detector = SurfFeatureDetector::create(5000,4,3, false, true);
    //Ptr<SiftFeatureDetector> detector = SiftFeatureDetector::create();

    vector<KeyPoint> key_points_1;//将提取的特征点以keypoint形式存储
    Mat dstImage1;

    //Mat img22;
    //resize(img2,img22,Size(img1.rows/2,img1.cols/2));
    detector->detectAndCompute(img2, noArray(), key_points_1, dstImage1);

    if(isFirst) {
        //Mat img11;
        //resize(img1,img11,Size(img1.rows/2,img1.cols/2));
        detector->detectAndCompute(img1, noArray(), key_points_2, dstImage2);
    }

    detector.release();

    t2 = get_current_ms();
    LOGD("特征点检测完成，%d", t2 - t1);
    t1 = t2;

    Ptr<DescriptorMatcher> matcher = DescriptorMatcher::create("BruteForce"); //描述匹配特征点
    vector<DMatch> mach;
    matcher->match(dstImage1, dstImage2, mach);

    matcher.release();
    dstImage1.release();
    //dstImage2.release();

    //删除错误匹配的特征点
    vector<DMatch> InlierMatches;//定义内点集合
    vector<Point2f> p1, p2;//先把keypoint转换为Point格式
    for (int i = 0; i < mach.size(); i++)
    {
        p1.push_back(key_points_1[mach[i].queryIdx].pt);// pt是position
        p2.push_back(key_points_2[mach[i].trainIdx].pt);
    }

    //RANSAC FindFundamental剔除错误点
    vector<uchar> RANSACStatus;//用以标记每一个匹配点的状态，等于0则为外点，等于1则为内点。
    findFundamentalMat(p1, p2, RANSACStatus, FM_RANSAC);//p1 p2必须为float型
    for (int i = 0; i < mach.size(); i++)
    {
        if (RANSACStatus[i] != 0)
        {
            InlierMatches.push_back(mach[i]); //不等于0的是内点
        }
    }

    vector<Point2f> Tele_point, Wide_point;
    for (int i = 0; i < InlierMatches.size(); i++)
    {
        Tele_point.push_back(key_points_1[InlierMatches[i].queryIdx].pt);
        Wide_point.push_back(key_points_2[InlierMatches[i].trainIdx].pt);
    }

    Mat Homography = findHomography(Tele_point, Wide_point, RANSAC); //计算将p2投影到p1上的单映性矩阵
    warpPerspective(img2, res, Homography, Size(img1.cols, img1.rows));

    //double v = getPSNR(img1,res);
    LOGD("图像配准完成");

    img1.release();
    img2.release();
    Homography.release();
    Tele_point.clear();
    Wide_point.clear();
    RANSACStatus.clear();
    InlierMatches.clear();
    p1.clear();
    p2.clear();
    mach.clear();
}

void MatAlignORB(Mat img1,Mat img2,Mat &res){

    long t1 = get_current_ms();
    long t2;

    //Mat img11,img22;
    //mycanny(img1,img11);
    //mycanny(img2,img22);

    Ptr<FeatureDetector> detector = ORB::create(1000);

    vector<KeyPoint> key_points_1, key_points_2;//将提取的特征点以keypoint形式存储
    Mat dstImage1, dstImage2;

    detector->detectAndCompute(img2, noArray(), key_points_1, dstImage1);
    detector->detectAndCompute(img1, noArray(), key_points_2, dstImage2);

    t2 = get_current_ms();
    LOGD("特征点检测完成，%d", t2 - t1);
    t1 = t2;

    Ptr<DescriptorMatcher> matcher = DescriptorMatcher::create("BruteForce"); //描述匹配特征点
    vector<DMatch> mach;
    matcher->match(dstImage1, dstImage2, mach);

    dstImage1.release();
    dstImage2.release();

    //删除错误匹配的特征点
    vector<DMatch> InlierMatches;//定义内点集合
    vector<Point2f> p1, p2;//先把keypoint转换为Point格式
    for (int i = 0; i < mach.size(); i++)
    {
        p1.push_back(key_points_1[mach[i].queryIdx].pt);// pt是position
        p2.push_back(key_points_2[mach[i].trainIdx].pt);
    }

    //RANSAC FindFundamental剔除错误点
    vector<uchar> RANSACStatus;//用以标记每一个匹配点的状态，等于0则为外点，等于1则为内点。
    findFundamentalMat(p1, p2, RANSACStatus, FM_RANSAC);//p1 p2必须为float型
    for (int i = 0; i < mach.size(); i++)
    {
        if (RANSACStatus[i] != 0)
        {
            InlierMatches.push_back(mach[i]); //不等于0的是内点
        }
    }

    vector<Point2f> Tele_point, Wide_point;
    for (int i = 0; i < InlierMatches.size(); i++)
    {
        Tele_point.push_back(key_points_1[InlierMatches[i].queryIdx].pt);
        Wide_point.push_back(key_points_2[InlierMatches[i].trainIdx].pt);
    }

    Mat origin_res;
    Mat Homography = findHomography(Tele_point, Wide_point, RANSAC); //计算将p2投影到p1上的单映性矩阵
    warpPerspective(img2, origin_res, Homography, Size(img1.cols, img1.rows));

    //double v = getPSNR(img1,origin_res);
    /*if(v < 30){
        res = img1;
    } else{
        res = origin_res;
    }*/

    LOGD("图像配准完成");

    img1.release();
    img2.release();
    Homography.release();
    Tele_point.clear();
    Wide_point.clear();
    RANSACStatus.clear();
    InlierMatches.clear();
    p1.clear();
    p2.clear();
    mach.clear();
}

/*
void MatAlignORB2(Mat img1,Mat img2){
    float inlier_threshold = 2.5f;
    float nn_match_ratio = 0.8f;

    vector<KeyPoint> kpts1, kpts2;
    Mat desc1, desc2;

    Ptr<cv::ORB> orb_detector = cv::ORB::create(1000);

    Ptr<xfeatures2d::LATCH> latch = xfeatures2d::LATCH::create();

    orb_detector->detect(img1, kpts1);
    latch->compute(img1, kpts1, desc1);

    orb_detector->detect(img2, kpts2);
    latch->compute(img2, kpts2, desc2);

    BFMatcher matcher(NORM_HAMMING);
    vector< vector<DMatch> > nn_matches;
    matcher.knnMatch(desc1, desc2, nn_matches, 2);

    vector<KeyPoint> matched1, matched2, inliers1, inliers2;
    vector<DMatch> good_matches;
    for (size_t i = 0; i < nn_matches.size(); i++) {
        DMatch first = nn_matches[i][0];
        float dist1 = nn_matches[i][0].distance;
        float dist2 = nn_matches[i][1].distance;

        if (dist1 < nn_match_ratio * dist2) {
            matched1.push_back(kpts1[first.queryIdx]);
            matched2.push_back(kpts2[first.trainIdx]);
        }
    }

    for (unsigned i = 0; i < matched1.size(); i++) {
        Mat col = Mat::ones(3, 1, CV_64F);
        col.at<double>(0) = matched1[i].pt.x;
        col.at<double>(1) = matched1[i].pt.y;

        //col = homography * col;
        col /= col.at<double>(2);
        double dist = sqrt(pow(col.at<double>(0) - matched2[i].pt.x, 2) +
                           pow(col.at<double>(1) - matched2[i].pt.y, 2));

        if (dist < inlier_threshold) {
            int new_i = static_cast<int>(inliers1.size());
            inliers1.push_back(matched1[i]);
            inliers2.push_back(matched2[i]);
            good_matches.push_back(DMatch(new_i, new_i, 0));
        }
    }

    vector<Point2f> Tele_point, Wide_point;
    for (int i = 0; i < InlierMatches.size(); i++)
    {
        Tele_point.push_back(key_points_1[InlierMatches[i].queryIdx].pt);
        Wide_point.push_back(key_points_2[InlierMatches[i].trainIdx].pt);
    }

    Mat Homography = findHomography(Tele_point, Wide_point, RANSAC); //计算将p2投影到p1上的单映性矩阵
    warpPerspective(img2, res, Homography, Size(img1.cols, img1.rows));

    Mat res;
    drawMatches(img1, inliers1, img2, inliers2, good_matches, res);
    imwrite("latch_result.png", res);
}
*/

/*
void MatAlignOpt(Mat img1,Mat img2,Mat &res){
    vector<KeyPoint>left_keypoints, right_keypoints;
    //寻找左右两张图中的特征点
    Ptr<FastFeatureDetector> ffd;
    ffd->detect(img1, left_keypoints);
    ffd->detect(img2, right_keypoints);
    vector<Point2f> left_points;
    KeyPointsToPoints(left_keypoints, left_points);
    vector<Point2f> right_points(left_keypoints.size());
    KeyPointsToPoints(right_keypoints, right_points);
    //保证图片为灰度图
    Mat imgGray1, imgGray2;
    cvtColor(img1, imgGray1, CV_RGB2GRAY);
    cvtColor(img2, imgGray2, CV_RGB2GRAY);
    //计算光流域
    vector<uchar>vstatus;
    vector<float>verror;
    calcOpticalFlowPyrLK(imgGray1, imgGray2, left_points, right_points, vstatus, verror);
    Mat imofkl = img1.clone();
    for (int i = 0; i < vstatus.size(); i++)
    {
        if (vstatus[i] && verror[i] < 12)
        {
            line(imofkl, left_points[i], right_points[i], CV_RGB(255, 255, 255), 1, 8, 0);
            circle(imofkl, right_points[i], 3, CV_RGB(255, 255, 255), 1, 8, 0);
        }
    }

    //去除大误差点
    vector<Point2f> right_points_to_find;
    vector<int> right_points_to_find_back_index;
    for (unsigned int i = 0; i < vstatus.size(); i++)
    {
        if (vstatus[i] && verror[i] < 12.0)
        {
            //为使用特征保留原始光流序列的点索引
            right_points_to_find_back_index.push_back(i);
            //保持特征点本身
            right_points_to_find.push_back(right_points[i]);
        }
        else
        {
            vstatus[i] = 0;
        }
    }
    //查看每个正确点属于的特征
    Mat right_points_to_find_flat = Mat(right_points_to_find).reshape(1, right_points_to_find.size());
    vector<Point2f> right_features;
    KeyPointsToPoints(right_keypoints, right_features);
    Mat right_features_flat = Mat(right_features).reshape(1, right_features.size());
    //匹配
    BFMatcher matcher(CV_L2);
    vector< vector<DMatch> > nearest_neighbors;
    matcher.radiusMatch(right_points_to_find_flat, right_features_flat, nearest_neighbors, 2.0f);
    //去除距离过近可能导致错误的点
    set<int>found_in_right_points;
    vector<DMatch>matches;
    for (int i = 0; i < nearest_neighbors.size(); i++)
    {
        DMatch _m;
        if (nearest_neighbors[i].size() == 1)
        {
            _m = nearest_neighbors[i][0];
        }
        else if (nearest_neighbors[i].size()>1)
        {
            double ratio = nearest_neighbors[i][0].distance / nearest_neighbors[i][1].distance;
            if (ratio < 0.7)
            {
                _m = nearest_neighbors[i][0];
            }
            else
                continue;
        }
        else
            continue;
        if (found_in_right_points.find(_m.trainIdx) == found_in_right_points.end())
        {
            _m.queryIdx = right_points_to_find_back_index[_m.queryIdx];
            matches.push_back(_m);
            found_in_right_points.insert(_m.trainIdx);
        }
    }
    Mat Homography = findHomography(left_points, right_features, CV_RANSAC); //计算将p2投影到p1上的单映性矩阵
    warpPerspective(img2, res, Homography, Size(img1.cols, img1.rows));
}*/

void OpticalFlow(vector<Mat> &srcs){
    Mat R2 = srcs[1].clone();
    vector<Mat> gray_images;

    for(size_t i=0;i<srcs.size();i++){
        //复制原来的图片
        Mat temp;
        cvtColor(srcs[i], temp, COLOR_BGR2GRAY);
        gray_images.push_back(temp);
    }

    vector<Point2f> point[3];
    double qualityLevel = 0.01;
    double minDistance = 10;
    goodFeaturesToTrack(gray_images[0], point[0], MAX_CORNERS, qualityLevel, minDistance);

    Mat flow;
    calcOpticalFlowFarneback(gray_images[0], gray_images[1], flow, 0.5, 3, 15, 3, 5, 1.2, 0);

    Mat dst = srcs[0].clone();

    for(size_t y=0;y<srcs[0].rows;y+=10){
        for(size_t x=0;x<srcs[0].cols;x+=10){
            Point2f fxy = flow.at<Point2f>(y, x);
            point[1].push_back(Point2f(y,x));
            point[2].push_back(fxy);
            line(dst, Point(x,y), Point(cvRound(x+fxy.x), cvRound(y+fxy.y)), CV_RGB(0, 255, 0), 1, 8);
        }
    }

    //Mat Homography = getPerspectiveTransform(point[1], point[2]);
    Mat Homography = findHomography(point[1], point[2]); //计算将p2投影到p1上的单映性矩阵
    warpPerspective(srcs[1], srcs[1], Homography, Size(srcs[0].cols, srcs[0].rows));
    imwrite("sdcard/2ORIGIN2.jpg", R2);

    imwrite("sdcard/2HOMO2.jpg", srcs[1]);
    imwrite("sdcard/2MIX2.jpg", srcs[1]/2+srcs[0]/2);

    //dst = srcs[0];

    /*//稀疏光流

    TermCriteria criteria = TermCriteria(TermCriteria::COUNT|TermCriteria::EPS, 20, 0.03);
    vector<uchar> status;
    vector<float> err;

    calcOpticalFlowPyrLK(gray_images[0], gray_images[1], point[0], point[1], status, err, Size(15, 15), 3, criteria);

    for(size_t i=0;i<point[0].size()&&i<point[1].size();i++){
        line(srcs[1],Point(cvRound(point[0][i].x),cvRound(point[0][i].y)), Point(cvRound(point[1][i].x),
                                                                                 cvRound(point[1][i].y)), cvScalar(0,50,200),1,CV_AA);
    }
    imshow("稀疏光流：", srcs[1]);*/
}

void templateMatch(Mat &temp, Mat src, Mat &dst){
    matchTemplate(src, temp, dst, 0);
    imwrite("sdcard/matchTemplate.jpg",dst);
    normalize(dst, dst, 0, 1, 32);
    imwrite("sdcard/normalize.jpg",dst);

    cv::Point minPoint;
    cv::Point maxPoint;
    double *minVal = 0;
    double *maxVal = 0;
    minMaxLoc(dst, minVal, maxVal, &minPoint,&maxPoint);
    cv::rectangle(temp, minPoint, Point(minPoint.x + src.cols, minPoint.y + src.rows), cv::Scalar(0,0,255), 3, 8);

}