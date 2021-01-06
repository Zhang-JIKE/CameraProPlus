package com.jike.cameraproplus.view;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.Face;
import android.hardware.camera2.params.MeteringRectangle;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Range;
import android.view.Surface;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.daily.flexui.util.AppContextUtils;
import com.jike.cameraproplus.cameradata.CamMode;
import com.jike.cameraproplus.cameradata.CamRates;
import com.jike.cameraproplus.cameradata.CamSetting;
import com.jike.cameraproplus.cameradata.CamSize;
import com.jike.cameraproplus.cameradata.CameraParameter;
import com.jike.cameraproplus.cvprocessor.CvHdrQueueProcessor;
import com.jike.cameraproplus.imagereader.ProReader;
import com.jike.cameraproplus.interfaces.CameraParamListener;
import com.jike.cameraproplus.pixfomula.PixFormula;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CameraView extends BaseCameraView {

  public boolean isFaceingFront;

  public CameraView(Context context) {
    super(context);
  }

  public CameraView(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  private ProReader proReader;

  private CameraParamListener paramListener;

  public void setParamListener(CameraParamListener paramListener) {
    this.paramListener = paramListener;
  }

  public int viewWidth = 0;
  public int viewHeight = 0;
  
  public C2Helper c2helper = new C2Helper();


  public void openCamera(int width, int height, boolean isFaceingFront) {
    this.isFaceingFront = isFaceingFront;

    if (ContextCompat.checkSelfPermission(AppContextUtils.getAppActivity(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
      return;
    }
    viewHeight = height;
    viewWidth = width;

    c2helper.setUpCameraOutputs();
    configureTransform(width,height);
    setAspectRatio(CamSize.getPicSize().getHeight(), CamSize.getPicSize().getWidth());
    CameraManager manager = (CameraManager) AppContextUtils.getAppActivity().getSystemService(Context.CAMERA_SERVICE);
    try {
      manager.openCamera(c2helper.mCameraId, c2helper.deviceStateListener, c2helper.mBackgroundHandler);
    } catch (CameraAccessException e) {
      e.printStackTrace();
    }
  }

  public void closeCamera() {
    if (null != c2helper.cameraCaptureSession) {
      c2helper.cameraCaptureSession.close();
      c2helper.cameraCaptureSession = null;
    }
    if (null != c2helper.mCameraDevice) {
      c2helper.mCameraDevice.close();
      c2helper.mCameraDevice = null;
    }
    proReader.closeAllReader();
  }

  public void startBackgroundThread() {
    c2helper.mBackgroundThread = new HandlerThread("CameraBackground");
    c2helper.mBackgroundThread.start();
    c2helper.mBackgroundHandler = new Handler(c2helper.mBackgroundThread.getLooper());
    c2helper.mBackgroundHandler.post(c2helper.detectRunnable);
  }

  public void stopBackgroundThread() {
    if(c2helper.mBackgroundThread!=null) {
      c2helper.mBackgroundThread.quitSafely();
      try {
        c2helper.mBackgroundThread.join();
        c2helper.mBackgroundThread = null;
        c2helper.mBackgroundHandler = null;
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

  public void takePicture(){
    c2helper.takePicture();
  }

  public class C2Helper{
    private String mCameraId;

    private CameraDevice mCameraDevice;
    private CameraCharacteristics mCameraCharacteristics;

    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

    private CaptureRequest.Builder previewBuilder;
    private CaptureRequest previewRequest;
    private CaptureResult captureResult;

    private CameraCaptureSession cameraCaptureSession;
    private Surface surface;


    private CameraCaptureSession.CaptureCallback callback = new CameraCaptureSession.CaptureCallback() {

      @Override
      public void onCaptureProgressed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureResult partialResult) {
      }

      @Override
      public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
        captureResult = result;
        proReader.captureResult = result;
        if(CamSetting.isFaceDetectOpend) {
          faceDetect(result);
        }
        try {
          CameraParameter.exposureTime = result.get(CaptureResult.SENSOR_EXPOSURE_TIME);
          CameraParameter.iso = result.get(CaptureResult.SENSOR_SENSITIVITY);
          CameraParameter.isoBoost = result.get(CaptureResult.CONTROL_POST_RAW_SENSITIVITY_BOOST);
        }catch (NullPointerException e){
          e.printStackTrace();
        }
      }

    };

    private CameraCaptureSession.StateCallback stateCallback = new CameraCaptureSession.StateCallback() {
      @Override
      public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
        if (null == mCameraDevice) {
          return;
        }
        c2helper.cameraCaptureSession = cameraCaptureSession;
        try {
          previewBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                  CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);

          previewBuilder.set(CaptureRequest.NOISE_REDUCTION_MODE,CaptureRequest.NOISE_REDUCTION_MODE_HIGH_QUALITY);
          previewBuilder.set(CaptureRequest.HOT_PIXEL_MODE,CaptureRequest.HOT_PIXEL_MODE_HIGH_QUALITY);

          previewBuilder.set(CaptureRequest.COLOR_CORRECTION_MODE,CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE_HIGH_QUALITY);

          previewBuilder.set(CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE,CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE_HIGH_QUALITY);
          previewBuilder.set(CaptureRequest.EDGE_MODE,CaptureRequest.EDGE_MODE_HIGH_QUALITY);
          previewBuilder.set(CaptureRequest.SHADING_MODE,CaptureRequest.SHADING_MODE_HIGH_QUALITY);
          previewBuilder.set(CaptureRequest.TONEMAP_MODE,CaptureRequest.TONEMAP_MODE_HIGH_QUALITY);

          previewBuilder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE,CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON);
          previewBuilder.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE,CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_ON);

          cameraCaptureSession.setRepeatingRequest(previewBuilder.build(),
                  callback, mBackgroundHandler);
        } catch (CameraAccessException e) {
          e.printStackTrace();
        }
      }

      @Override
      public void onConfigureFailed(
              @NonNull CameraCaptureSession cameraCaptureSession) {
      }
    };

    private final CameraDevice.StateCallback deviceStateListener = new CameraDevice.StateCallback() {
      @Override
      public void onOpened(@NonNull CameraDevice cameraDevice) {
        mCameraDevice = cameraDevice;
        createCameraPreviewSession();
      }

      @Override
      public void onDisconnected(@NonNull CameraDevice cameraDevice) {
        cameraDevice.close();
        mCameraDevice = null;
      }

      @Override
      public void onError(@NonNull CameraDevice cameraDevice, int error) {
        cameraDevice.close();
        mCameraDevice = null;
      }
    };

    private void setUpCameraOutputs() {
      CameraManager manager = (CameraManager) AppContextUtils.getAppActivity().getSystemService(Context.CAMERA_SERVICE);
      String[] cameraIdList;
      try {
        cameraIdList = manager.getCameraIdList();
        Toast.makeText(getContext(),"检测到"+cameraIdList.length+"个摄像头",Toast.LENGTH_SHORT).show();
        for (String cameraId : cameraIdList) {
          Log.e("cameraId",""+cameraId);
          /*mCameraCharacteristics = manager.getCameraCharacteristics(cameraId);
          Integer facing = mCameraCharacteristics.get(CameraCharacteristics.LENS_FACING);
          if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
            continue;
          }
          mCameraId = cameraId;
          c2helper.getCameraArgs();*/
        }
        String cameraId;
        if(isFaceingFront){
          cameraId = "1";
        }else {
          cameraId = "0";
        }
        mCameraCharacteristics = manager.getCameraCharacteristics(cameraId);
        mCameraId = cameraId;
        c2helper.getCameraArgs();
      } catch (CameraAccessException e) {
        e.printStackTrace();
      }
    }

    private void createCameraPreviewSession() {
      try {
        SurfaceTexture texture = getSurfaceTexture();
        texture.setDefaultBufferSize(CamSize.getPicSize().getWidth(), CamSize.getPicSize().getHeight());
        surface = new Surface(texture);
        previewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        previewBuilder.addTarget(surface);

        setCameraArgs();
        proReader = new ProReader();
        proReader.characteristics = mCameraCharacteristics;
        proReader.setUpImageReader(mBackgroundHandler);

        mCameraDevice.createCaptureSession(Arrays.asList(surface,
                proReader.imageReader.getSurface()),
                stateCallback,
                mBackgroundHandler
        );
      } catch (CameraAccessException e) {
        e.printStackTrace();
      }
    }

    private void takePicture() {
      try {
        if (null == AppContextUtils.getAppActivity() || null == mCameraDevice) {
          return;
        }
        Rect rectSensor = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        int pixW = rectSensor.width();
        int pixH = rectSensor.height();
        int l = (int)((pixW-pixW/mScaleTime)*0.5);
        int t = (int)((pixH-pixH/mScaleTime)*0.5);
        int r = (int)((pixW+pixW/mScaleTime)*0.5);
        int b = (int)((pixH+pixH/mScaleTime)*0.5);

        switch (CamMode.mode){
          case NORMAL:
            List<CaptureRequest> buildersn = new ArrayList<>();
            for(int i = 0; i<1;i++){
              CaptureRequest.Builder builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
              builder.set(CaptureRequest.SCALER_CROP_REGION,new Rect(l,t,r,b));
              builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
              if(!CamSetting.isDenoiseOpened) {
                builder.set(CaptureRequest.NOISE_REDUCTION_MODE, CaptureRequest.NOISE_REDUCTION_MODE_OFF);
              }

              builder.addTarget(proReader.imageReader.getSurface());
              buildersn.add(builder.build());
            }
            cameraCaptureSession.captureBurst(buildersn, null, null);
            break;

          case HDR:
            if(!ProReader.hdrKey) {
              ProReader.hdrKey = true;
              List<CaptureRequest> buildersHdr = new ArrayList<>();
              for (int i = 0; i < ProReader.HDR_COUNT; i++) {
                CaptureRequest.Builder builderHdr = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_ZERO_SHUTTER_LAG);
                builderHdr.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, CamRates.rawFpsRanges[0]);
                builderHdr.set(CaptureRequest.CONTROL_AWB_LOCK, true);

                builderHdr.set(CaptureRequest.SCALER_CROP_REGION, new Rect(l, t, r, b));

                if(i==0){
                  //CvHdrQueueProcessor.times[i] = CameraParameter.exposureTime * 1.0f / CameraParameter.ONE_SECOND;
                }else if(i==1){
                  //CvHdrQueueProcessor.times[i] = CameraParameter.exposureTime / 2.0f / CameraParameter.ONE_SECOND;
                  // /2
                  builderHdr.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
                  builderHdr.set(CaptureRequest.CONTROL_POST_RAW_SENSITIVITY_BOOST, CameraParameter.isoBoost);
                  builderHdr.set(CaptureRequest.SENSOR_EXPOSURE_TIME, CameraParameter.exposureTime/4);
                  builderHdr.set(CaptureRequest.SENSOR_SENSITIVITY, CameraParameter.iso);
                }else if(i==2){
                  // *2
                  //CvHdrQueueProcessor.times[i] = CameraParameter.exposureTime * 2.0f / CameraParameter.ONE_SECOND;
                  builderHdr.set(CaptureRequest.CONTROL_POST_RAW_SENSITIVITY_BOOST, CameraParameter.isoBoost);
                  builderHdr.set(CaptureRequest.SENSOR_EXPOSURE_TIME, CameraParameter.exposureTime/2);
                  builderHdr.set(CaptureRequest.SENSOR_SENSITIVITY, CameraParameter.iso);
                }else if(i==3){
                  // /3
                  //CvHdrQueueProcessor.times[i] = CameraParameter.exposureTime * 2.0f / CameraParameter.ONE_SECOND;
                  builderHdr.set(CaptureRequest.CONTROL_POST_RAW_SENSITIVITY_BOOST, CameraParameter.isoBoost);
                  builderHdr.set(CaptureRequest.SENSOR_EXPOSURE_TIME, CameraParameter.exposureTime*2);
                  builderHdr.set(CaptureRequest.SENSOR_SENSITIVITY, CameraParameter.iso);
                }else if(i==4){
                  // *3
                  //CvHdrQueueProcessor.times[i] = CameraParameter.exposureTime * 2.0f / CameraParameter.ONE_SECOND;
                  builderHdr.set(CaptureRequest.CONTROL_POST_RAW_SENSITIVITY_BOOST, CameraParameter.isoBoost);
                  builderHdr.set(CaptureRequest.SENSOR_EXPOSURE_TIME, CameraParameter.exposureTime*4);
                  builderHdr.set(CaptureRequest.SENSOR_SENSITIVITY, CameraParameter.iso);
                }

                builderHdr.addTarget(proReader.imageReader.getSurface());
                //builderHdr.addTarget(new Surface(getSurfaceTexture()));
                buildersHdr.add(builderHdr.build());
              }
              cameraCaptureSession.captureBurst(buildersHdr, null, null);
            }
            break;

          case PIX_FUSION:
            if(!ProReader.fusionKey) {
              ProReader.fusionKey = true;
              List<CaptureRequest> buildersSuperRes = new ArrayList<>();
              for (int i = 0; i < ProReader.FUSION_COUNT; i++) {
                CaptureRequest.Builder builderSuperRes = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                builderSuperRes.set(CaptureRequest.SCALER_CROP_REGION, new Rect(l, t, r, b));
                builderSuperRes.addTarget(proReader.imageReader.getSurface());
                builderSuperRes.addTarget(new Surface(getSurfaceTexture()));
                
                buildersSuperRes.add(builderSuperRes.build());
              }
              cameraCaptureSession.captureBurst(buildersSuperRes, null, null);
            }
            break;

          case NIGHT:
            /*if(!ProReader.nightKey) {
              ProReader.nightKey = true;*/
              List<CaptureRequest> buildersSuperNight = new ArrayList<>();
              for (int i = 0; i < ProReader.NIGHT_COUNT; i++) {
                CaptureRequest.Builder builderSuperNight = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_ZERO_SHUTTER_LAG);
                builderSuperNight.set(CaptureRequest.SCALER_CROP_REGION, new Rect(l, t, r, b));

                builderSuperNight.addTarget(proReader.imageReader.getSurface());
                builderSuperNight.addTarget(new Surface(getSurfaceTexture()));

                builderSuperNight.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
                builderSuperNight.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, CamRates.rawFpsRanges[0]);
                builderSuperNight.set(CaptureRequest.CONTROL_AWB_LOCK, true);

                //if(i==0) {
                builderSuperNight.set(CaptureRequest.NOISE_REDUCTION_MODE, CaptureRequest.NOISE_REDUCTION_MODE_OFF);
                builderSuperNight.set(CaptureRequest.CONTROL_POST_RAW_SENSITIVITY_BOOST, 100);
                builderSuperNight.set(CaptureRequest.SENSOR_EXPOSURE_TIME, CameraParameter.ONE_SECOND / 4);
                builderSuperNight.set(CaptureRequest.SENSOR_SENSITIVITY, (int) (CameraParameter.iso / (CameraParameter.ONE_SECOND / 4.0 / CameraParameter.exposureTime))*CameraParameter.isoBoost/100);
                //}

                if(i==ProReader.NIGHT_COUNT-1) {
                  builderSuperNight.set(CaptureRequest.NOISE_REDUCTION_MODE, CaptureRequest.NOISE_REDUCTION_MODE_HIGH_QUALITY);
                  builderSuperNight.set(CaptureRequest.CONTROL_POST_RAW_SENSITIVITY_BOOST, 100);
                  builderSuperNight.set(CaptureRequest.SENSOR_EXPOSURE_TIME, CameraParameter.exposureTime * (int) (CameraParameter.iso / 50f)*CameraParameter.isoBoost/100);
                  builderSuperNight.set(CaptureRequest.SENSOR_SENSITIVITY, 50);
                }
                buildersSuperNight.add(builderSuperNight.build());
                /*}*/
              }
              cameraCaptureSession.captureBurst(buildersSuperNight, null, null);


            break;

          case SUPER_RES:
            if(!ProReader.resKey) {
              ProReader.resKey = true;
              List<CaptureRequest> captureRequests = new ArrayList<>();
              for (int i = 0; i < ProReader.RES_COUNT; i++) {
                CaptureRequest.Builder builder1 =  mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                builder1.set(CaptureRequest.SCALER_CROP_REGION, new Rect(l, t, r, b));
                builder1.set(CaptureRequest.NOISE_REDUCTION_MODE, CaptureRequest.NOISE_REDUCTION_MODE_OFF);
                builder1.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_OFF);
                builder1.set(CaptureRequest.CONTROL_POST_RAW_SENSITIVITY_BOOST, 100);
                builder1.set(CaptureRequest.SENSOR_EXPOSURE_TIME, CameraParameter.ONE_SECOND / 4);
                builder1.set(CaptureRequest.SENSOR_SENSITIVITY, (int) (CameraParameter.iso / (CameraParameter.ONE_SECOND / 4.0 / CameraParameter.exposureTime))*CameraParameter.isoBoost/100);
                builder1.addTarget(new Surface(getSurfaceTexture()));
                builder1.addTarget(proReader.imageReader.getSurface());
                captureRequests.add(builder1.build());
              }
              cameraCaptureSession.captureBurst(captureRequests, null, null);
            }
            break;
        }
      } catch (CameraAccessException e) {
        e.printStackTrace();
      }
    }

    public void getCameraArgs(){
      StreamConfigurationMap map = mCameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
      CamSize.setPicSizes(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)));
      CamSize.setVideoSizes(Arrays.asList(map.getOutputSizes(MediaRecorder.class)));
      CamRates.rawFpsRanges = mCameraCharacteristics.get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
      Boolean available = mCameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
      CamSetting.mFlashSupported = available == null ? false : available;
    }

    public void setCameraArgs(){
      Range<Integer> fpsRange = CamRates.rawFpsRanges[CamRates.rawFpsRanges.length-2];

      if(CamRates.isForcedOpen60Fps){
        fpsRange = new Range<>(60,60);
      }
      previewBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, fpsRange);

      previewBuilder.set(CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE,CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON);
      previewBuilder.set(CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE,CaptureRequest.CONTROL_VIDEO_STABILIZATION_MODE_ON);

      int[] faceDetectModes = mCameraCharacteristics.get(CameraCharacteristics.STATISTICS_INFO_AVAILABLE_FACE_DETECT_MODES);
      for(int i : faceDetectModes){
        if(i ==CameraCharacteristics.STATISTICS_FACE_DETECT_MODE_SIMPLE && CamSetting.isFaceDetectOpend) {
          previewBuilder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, CameraCharacteristics.STATISTICS_FACE_DETECT_MODE_SIMPLE);
          break;
        }
      }
    }

    private void faceDetect(TotalCaptureResult result){
      Face[] faces = result.get(CaptureResult.STATISTICS_FACES);
      Point[] points = new Point[faces.length];
      Rect rectSensor = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
      if(faces!=null) {
        final Rect[] rects = new Rect[faces.length];
        int screenW = getWidth();
        int screenH = getHeight();
        int i = 0;
        for (Face face : faces) {
          Point point = face.getRightEyePosition();
          Rect faceBounds = face.getBounds();
          int l = (int) (screenH * ((float) faceBounds.left / rectSensor.width()));
          int t = (int) (screenW * ((float) faceBounds.top / rectSensor.height()));
          int r = (int) (screenH * ((float) faceBounds.right / rectSensor.width()));
          int b = (int) (screenW * ((float) faceBounds.bottom / rectSensor.height()));

          Rect rect = new Rect( screenW - b,l,screenW-t,r);
          if(point!=null) {
            point.x = (int) (screenH * ((float) point.x / CamSize.getPicSize().getWidth()));
            point.y = (int) (screenW - screenW * ((float) point.y / CamSize.getPicSize().getHeight()));
            points[i] = point;
          }
          rects[i]=rect;
          i++;
        }
        if(cameraControllerView !=null) {
          cameraControllerView.setDetectedFaces(rects);
          cameraControllerView.setDetectedEye(points);
        }
      }
    }

    private Runnable detectRunnable = new Runnable() {
      @Override
      public void run() {
        brightNessDetect();
        if(mBackgroundThread.isAlive()&&mBackgroundHandler!=null) {
          mBackgroundHandler.postDelayed(detectRunnable,500);
        }
      }
    };

    private void brightNessDetect(){
      Bitmap bitmap = getBitmap(3, 3);
      int[] pixels = new int[bitmap.getWidth()*bitmap.getHeight()];
      bitmap.getPixels(pixels,0,bitmap.getWidth(),0,0,bitmap.getWidth(),bitmap.getHeight());
      CameraParameter.brightNess = PixFormula.getBrightness(bitmap);
      if(paramListener != null) {
        paramListener.onParamReceived(CameraParameter.exposureTime,
                CameraParameter.iso,
                CameraParameter.isoBoost,
                CameraParameter.brightNess);
      }
    }

    public void setFocus(Point point){
      int screenW = getWidth();
      int screenH = getHeight();

      Rect size = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
      float realPreviewWidth = size.height();
      float realPreviewHeight = size.width();

      //根据预览像素与拍照最大像素的比例，调整手指点击的对焦区域的位置
      float focusX = (float) realPreviewWidth / screenW * point.x;
      float focusY = (float) realPreviewHeight / screenH * point.y;

      Rect totalPicSize = previewBuilder.get(CaptureRequest.SCALER_CROP_REGION);

      Log.e("CFocus","x"+focusX+"y"+focusY);
      float cutDx = 0;//(totalPicSize.height() - size.height()) / 2;
      Rect rect2 = new Rect((int)focusY,
              (int)realPreviewWidth - (int)focusX,
              (int)(focusY + 1000),
              (int)realPreviewWidth - (int)(focusX) + 1000);

      Log.e("CFocus","l:"+rect2.left+"t:"+rect2.top);

      previewBuilder.set(CaptureRequest.CONTROL_AE_REGIONS, new MeteringRectangle[]{new MeteringRectangle(rect2,5000)});
      //previewBuilder.set(CaptureRequest.CONTROL_AF_REGIONS, new MeteringRectangle[]{new MeteringRectangle(rect2,1000)});
      previewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,CaptureRequest.CONTROL_AF_TRIGGER_START);
      previewBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER,CameraMetadata.CONTROL_AE_PRECAPTURE_TRIGGER_START);
      try {
        if(cameraCaptureSession!=null) {
          cameraCaptureSession.setRepeatingRequest(previewBuilder.build(), null, mBackgroundHandler);
        }
      } catch (CameraAccessException e) {
        e.printStackTrace();
      }
    }

    public void setScaleTime(float time){
      mScaleTime = time;
      Rect rectSensor = mCameraCharacteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
      int pixW = rectSensor.width();
      int pixH = rectSensor.height();
      previewBuilder.set(CaptureRequest.SCALER_CROP_REGION,new Rect(
              (int)((pixW-pixW/mScaleTime)*0.5),
              (int)((pixH-pixH/mScaleTime)*0.5),
              (int)((pixW+pixW/mScaleTime)*0.5),
              (int)((pixH+pixH/mScaleTime)*0.5)));
      previewRequest = previewBuilder.build();
      try {
        cameraCaptureSession.setRepeatingRequest(previewRequest, null, mBackgroundHandler);
      } catch (CameraAccessException e) {
        e.printStackTrace();
      }

    }

  }

}
