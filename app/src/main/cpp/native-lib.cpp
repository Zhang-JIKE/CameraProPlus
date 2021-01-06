#include "mat-align.h"
#include "libyuv/libyuv.h"

static Mat father;

struct AlignMat {
    Mat mat1;
    Mat mat2;
};

void *threadMatAlign(void *args) {
    pthread_t myid = pthread_self();
    AlignMat para = (*((AlignMat *) args));
    MatAlignORB(para.mat1,para.mat2,para.mat2);
    pthread_exit(NULL);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_jike_cameraproplus_pixfomula_PixFormula_cvMatFusion(JNIEnv *env, jclass clazz,
                                                             jlongArray mat_addrs, jstring path,
                                                             jboolean isAccel) {
    
    char *fpath = (char *)env->GetStringUTFChars(path, NULL);
    int size = env->GetArrayLength(mat_addrs);

    jlong *addrs = env->GetLongArrayElements(mat_addrs,NULL);

    vector<Mat> matv;
    for (int i = 0; i < size; i++) {
        Mat mat = (*((Mat *) addrs[i]));
        matv.push_back(mat);
        mat.release();
    }

    /*if(isAccel){
        pthread_t pt[size];
        AlignMat mats[size];
        long Mbyte = matv[0].cols * matv[0].rows * 3 /1024 /1024;
        int steplength = 7;//140 / Mbyte;
        int step = 0;
        for (int i = 1; i < size; i++) {
            step++;
            mats[i].mat1 = matv[0];
            mats[i].mat2 = matv[i];
            pthread_create(&pt[i], NULL, &threadMatAlign, (void *)&mats[i]);
            if(step % steplength == 0 || i == size-1){
                pthread_join(pt[i], NULL);
            }
        }
        *//*Ptr<AlignMTB> alignMTB = createAlignMTB();
        alignMTB->process(matv, matv);*//*
    } else {
        for (int i = 1; i < size; i++) {
            MatAlignSurf(matv[0], matv[i], matv[i], i == 1);
        }
        //Ptr<AlignMTB> alignMTB = createAlignMTB();
        //alignMTB->process(matv, matv);
    }*/
    /*for (int i = 1; i < size; i++) {
        MatAlignSurf(matv[0], matv[i], matv[i], i == 1);
    }*/
    matv[0].convertTo(matv[0],CV_32F);
    Mat dst = matv[0]/size;
    for(int i = 1; i < size; i++) {
        matv[i].convertTo(matv[i],CV_32F);
        dst += matv[i]/size;
    }

    /*Mat dst;
    Ptr<MergeMertens> merge = createMergeMertens();
    merge->process(matv, dst);
    dst *= 255;*/
    transpose(dst, dst);
    flip(dst, dst, 1);

    imwrite(fpath, dst);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_jike_cameraproplus_pixfomula_PixFormula_getBrightness(JNIEnv *env, jclass clazz,
                                                               jobject bitmap) {
    AndroidBitmapInfo info;

    unsigned char* pixel;

    AndroidBitmap_getInfo(env, bitmap, &info);

    AndroidBitmap_lockPixels(env, bitmap, reinterpret_cast<void **>(&pixel));

    float sum = 0;

    for(int i = 0; i < info.width*info.height*4; i+=4) {
        int r = pixel[i];
        int g = pixel[i + 1];
        int b = pixel[i + 2];
        sum += 0.299f*r + 0.587f*g + 0.114f*b;
    }

    sum /= (info.width*info.height);
    AndroidBitmap_unlockPixels(env,bitmap);

    return (int)sum;
}


extern "C"
JNIEXPORT void JNICALL
Java_com_jike_cameraproplus_pixfomula_PixFormula_cvMatSave(JNIEnv *env, jclass clazz,
                                                           jlong mat_addr, jstring path) {
    char *fpath = (char *)env->GetStringUTFChars(path, NULL);

    Mat mat = (*((Mat *) mat_addr));

    transpose(mat, mat);
    flip(mat, mat, 1);
    imwrite(fpath, mat);
}

extern "C"
JNIEXPORT void JNICALL
Java_com_jike_cameraproplus_pixfomula_PixFormula_cvMatAverage(JNIEnv *env, jclass clazz,
                                                              jlong mat_addr) {

    Mat mat = (*((Mat *) mat_addr));
    mat.convertTo(mat,CV_32F);
    mat = mat / 16;
    if(father.empty()){
        father = mat;
    }else{
        father += mat;
    }
    mat.release();
}

extern "C"
JNIEXPORT void JNICALL
Java_com_jike_cameraproplus_pixfomula_PixFormula_cvMatSaveFather(JNIEnv *env, jclass clazz,
                                                                 jstring path) {
    // TODO: implement cvMatSaveFather()
    char *fpath = (char *)env->GetStringUTFChars(path, NULL);

    transpose(father, father);
    flip(father, father, 1);
    imwrite(fpath, father);
    father.release();
}


extern "C"
JNIEXPORT jfloatArray JNICALL
Java_com_jike_cameraproplus_pixfomula_PixFormula_bytes2Float(JNIEnv *env, jclass clazz, jint w,
                                                             jint h, jbyteArray rgb) {
    // TODO: implement bytes2Float()
    jfloatArray array = env->NewFloatArray(w*h*4);
    float *fpixels = (float*)env->GetFloatArrayElements(array, 0);


    unsigned char *pixels = (unsigned char*)env->GetByteArrayElements(rgb, 0);

    int idx = 0;
    for(int i = 0; i < w * h * 4; i += 4) {
        float r = pixels[i] / 255.0;
        float g = pixels[i + 1] / 255.0;
        float b = pixels[i + 2] / 255.0;

        fpixels[idx] = r;
        idx++;
        fpixels[idx] = g;
        idx++;
        fpixels[idx] = b;
        idx++;
    }

    return array;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_jike_cameraproplus_pixfomula_PixFormula_float2Byte(JNIEnv *env, jclass clazz, jint w,
                                                            jint h, jfloatArray rgb) {
    jbyteArray array = env->NewByteArray(w*h*4);

    float *fpixels = (float*)env->GetFloatArrayElements(rgb, 0);

    unsigned char *pixels = (unsigned char*)env->GetByteArrayElements(array, 0);

    for(int i = 0; i < w * h * 4; i += 4) {
        int r = fpixels[i] * 255;
        int g = fpixels[i + 1] * 255;
        int b = fpixels[i + 2] * 255;

        pixels[i] = r;
        pixels[i+1] = g;
        pixels[i+2] = b;
        pixels[i+3] = 0xff;
    }

    return array;
}

extern "C"
JNIEXPORT void JNICALL
Java_com_jike_cameraproplus_pixfomula_PixFormula_cvMatHdr(JNIEnv *env, jclass clazz,
                                                          jlongArray mat_addrs, jfloatArray times,
                                                          jstring path) {
    // TODO: implement cvMatHdr()
    char *fpath = (char *)env->GetStringUTFChars(path, NULL);
    float *times_p = env->GetFloatArrayElements(times, NULL);

    int size = env->GetArrayLength(mat_addrs);

    jlong *addrs = env->GetLongArrayElements(mat_addrs,NULL);

    vector<Mat> matv;
    vector<float> timesv;


    for (int i = 0; i < size; i++) {
        Mat mat = (*((Mat *) addrs[i]));
        matv.push_back(mat);
        mat.release();

        timesv.push_back(times_p[i]);
    }

    Ptr<AlignMTB> alignMTB = createAlignMTB();
    alignMTB->process(matv, matv);

    Mat dst;
    Ptr<MergeMertens> merge = createMergeMertens();
    merge->process(matv, dst);
    dst *= 255;
    imwrite(fpath, dst);
    /*Mat responseDebevec;
    Ptr<CalibrateDebevec> calibrateDebevec = createCalibrateDebevec();
    calibrateDebevec->process(matv, responseDebevec, timesv);

    Mat hdrDebevec;
    Ptr<MergeDebevec> mergeDebevec = createMergeDebevec();
    mergeDebevec->process(matv, hdrDebevec, timesv, responseDebevec);

    Mat ldrReinhard;
    Ptr<TonemapReinhard> tonemapReinhard = createTonemapReinhard(1.5, 0,0,0);
    tonemapReinhard->process(hdrDebevec, ldrReinhard);
    imwrite(fpath, ldrReinhard * 255);*/
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_jike_cameraproplus_pixfomula_PixFormula_i420ToRGB(JNIEnv *env, jclass clazz, jint w,
                                                           jint h, jbyteArray src) {

    uint8_t *yuv = reinterpret_cast<uint8_t *>(env->GetByteArrayElements(src, 0));

    jbyteArray rgbArray = env->NewByteArray(w*h*4);
    uint8_t *rgba = reinterpret_cast<uint8_t *>(env->GetByteArrayElements(rgbArray, 0));

    uint8_t *pY = yuv;
    uint8_t *pU = yuv + w * h;
    uint8_t *pV = yuv + w * h * 5 / 4;
    libyuv::I420ToRGBA(pY, w, pU, w >> 1, pV, w >> 1, rgba, w * 4, w, h);

    return rgbArray;
}



extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_jike_cameraproplus_pixfomula_PixFormula_nv21ToRGB(JNIEnv *env, jclass clazz, jint w,
                                                           jint h, jbyteArray nv21Array) {
    unsigned char *nv21 = (unsigned char*)(env->GetByteArrayElements(nv21Array, 0));

    jbyteArray rgbArray = env->NewByteArray(w*h*4);
    unsigned char *rgb = (unsigned char*)env->GetByteArrayElements(rgbArray, 0);

    yuv420ToRgbA(w, h, nv21, rgb);

    return rgbArray;
}

extern "C"
JNIEXPORT jbyteArray JNICALL
Java_com_jike_cameraproplus_pixfomula_PixFormula_nv21ToRGBFromLibYuv(JNIEnv *env, jclass clazz,
                                                                     jint w, jint h,
                                                                     jbyteArray nv21) {
    uint8_t *yuv = reinterpret_cast<uint8_t *>(env->GetByteArrayElements(nv21, 0));

    jbyteArray rgbArray = env->NewByteArray(w*h*4);
    uint8_t *rgba = reinterpret_cast<uint8_t *>(env->GetByteArrayElements(rgbArray, 0));

    uint8_t *pY = yuv;
    uint8_t *pUV = yuv + w * h;
    libyuv::NV12ToARGB(pY, w, pUV, w, rgba, w * 4, w, h);

    return rgbArray;
}