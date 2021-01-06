package com.jike.cameraproplus.cameradata;

import android.util.Log;

public class CamPara {

    public static final long ONE_SECOND = 1000000000;

    public static final long DANG = 25000000;

    public static float[] hdrExposureTimeFactor =
            new float[]{
                    5f/6,
                    1,
                    7f/6,
                    8f/6,
                    9f/6,
                    14f/6
    };

    public static float[] exposureTime = new float[]{1/12000f, 1/8000f, 1/6400f, 1/5000f, 1/4000f, 1/3200f,
            1/2500f, 1/2000f, 1/1600f, 1/1250f, 1/1000f, 1/800f, 1/640f, 1/500f, 1/400f, 1/320f, 1/250f,
            1/200f, 1/160f, 1/125f, 1/100f, 1/80f, 1/60f, 1/50f, 1/40f, 1/30f, 1/25f, 1/20f, 1/15f,
            1/13f, 1/10f, 1/8f, 1/6f, 1/5f, 1/4f, 0.3f, 0.4f, 0.5f, 0.6f, 0.8f, 1, 1.3f, 1.6f,
            2, 2.5f, 3.2f, 4, 5, 6, 8, 10, 13, 15, 20, 25, 30, 32,
    };

    public static int getIndex(float time){
        int index = 0;
        float dec = Math.abs(time - exposureTime[0]);
        for(int i = 0; i < exposureTime.length; i++){
            float d = Math.abs(time - exposureTime[i]);
            if(d < dec){
                dec = d;
                index = i;
            }
        }
        return index;
    }

    public static long timeIncrease(long time,int increasement){
        int i = getIndex((float)time / (float)ONE_SECOND);
        if(i + increasement >= 0 && i + increasement < exposureTime.length){
            Log.e("TimeIncrease",""+i+increasement);
            return (long) (exposureTime[i+increasement] * ONE_SECOND);
        }

        return time;
    }
}
