package com.jike.cameraproplus.utils;

import android.content.SharedPreferences;

import com.daily.flexui.util.AppContextUtils;

import static android.content.Context.MODE_PRIVATE;

public class DataUtils {

    public static void saveSettings(String key, boolean value){
        SharedPreferences mSpf = AppContextUtils.getAppContext().getSharedPreferences("Settings",MODE_PRIVATE);
        SharedPreferences.Editor editor = mSpf.edit();
        editor.putBoolean(key,value);
        editor.commit();
    }

    public static void saveSettings(String key, int value){
        SharedPreferences mSpf = AppContextUtils.getAppContext().getSharedPreferences("Settings",MODE_PRIVATE);
        SharedPreferences.Editor editor = mSpf.edit();
        editor.putInt(key,value);
        editor.commit();
    }

    public static void saveSettings(String key, String value){
        SharedPreferences mSpf = AppContextUtils.getAppContext().getSharedPreferences("Settings",MODE_PRIVATE);
        SharedPreferences.Editor editor = mSpf.edit();
        editor.putString(key,value);
        editor.commit();
    }
}
