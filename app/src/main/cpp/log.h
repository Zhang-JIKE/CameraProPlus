//
// Created by 18793 on 2020/8/28.
//

#ifndef CAMERAPROPLUS_LOG_H
#define CAMERAPROPLUS_LOG_H

#endif //CAMERAPROPLUS_LOG_H

#define TAG    "CamProPlus"
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG,TAG ,__VA_ARGS__)

long get_current_ms() {
    struct timespec res;
    clock_gettime(CLOCK_REALTIME, &res);
    return 1000 * res.tv_sec + res.tv_nsec / 1e6;
}
