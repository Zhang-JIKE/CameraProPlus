package com.jike.cameraproplus.cameradata;

import android.content.SharedPreferences;

import com.daily.flexui.util.AppContextUtils;
import com.jike.cameraproplus.utils.DataUtils;

import static android.content.Context.MODE_PRIVATE;

public class CamEncode {

    public static final String ENCODE = "ENCODE";

    public static final String[] encodes = new String[]{"H.264", "H.265"};

    public static int encodePos = 0;

    public static void initSettings(){
        SharedPreferences mSpf = AppContextUtils.getAppContext().getSharedPreferences("Settings",MODE_PRIVATE);
        encodePos = mSpf.getInt(ENCODE, 0);
    }

    public static void setEncodePos(int encodePos) {
        CamEncode.encodePos = encodePos;
        DataUtils.saveSettings(ENCODE,encodePos);
    }
}
