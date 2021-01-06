package com.jike.cameraproplus.cameradata;

import android.content.SharedPreferences;
import android.util.Range;

import com.daily.flexui.util.AppContextUtils;
import com.jike.cameraproplus.utils.DataUtils;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class CamRates {

    public static final String SELECT_POS = "SELECT_POS";
    public static final String IS_FORCED_OPEN_60_FPS = "IS_FORCED_OPEN_60_FPS";

    public static Range<Integer>[] rawFpsRanges;

    public static int selectPos = 0;

    public static Range<Integer> normalFps = new Range<>(30,60);

    public static boolean isForcedOpen60Fps = false;

    public static void initSettings(){
        SharedPreferences mSpf = AppContextUtils.getAppContext().getSharedPreferences("Settings",MODE_PRIVATE);
        selectPos = mSpf.getInt(SELECT_POS,0);
        isForcedOpen60Fps = mSpf.getBoolean(IS_FORCED_OPEN_60_FPS, false);
    }

    public static void setIsForcedOpen60Fps(boolean isForcedOpen60Fps) {
        CamRates.isForcedOpen60Fps = isForcedOpen60Fps;
        DataUtils.saveSettings(IS_FORCED_OPEN_60_FPS,isForcedOpen60Fps);
    }

    public static void setSelectPos(int selectPos) {
        CamRates.selectPos = selectPos;
        DataUtils.saveSettings(SELECT_POS, selectPos);
    }

    public static List<Range<Integer>> getFpsRanges() {
        List<Range<Integer>> ranges = new ArrayList<>();
        for(Range range : rawFpsRanges){
            if((int)range.getLower()>24){
                ranges.add(range);
            }
        }
        return ranges;
    }
}
