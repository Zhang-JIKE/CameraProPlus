package com.jike.cameraproplus.interfaces;

public interface CameraParamListener {
    void onParamReceived(long exposedTime, int iso, int isoBoost, int brightNess);
}
