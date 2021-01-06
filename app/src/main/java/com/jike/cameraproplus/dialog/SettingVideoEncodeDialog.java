package com.jike.cameraproplus.dialog;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.daily.flexui.util.DisplayUtils;
import com.jike.cameraproplus.R;
import com.jike.cameraproplus.cameradata.CamEncode;
import com.jike.cameraproplus.view.CheckView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;


import java.util.ArrayList;

public class SettingVideoEncodeDialog extends BottomSheetDialog {

    private LinearLayout itemVideoEncodes;

    private ArrayList<CheckView> checkViews = new ArrayList<>();

    private OnSelectItemListener onSelectItemListener;

    public SettingVideoEncodeDialog(@NonNull Context context) {
        super(context);
        createView(context);
    }

    public void createView(Context context) {
        final View bottomSheetView = getLayoutInflater().inflate(R.layout.dialog_setting_video_encode, null);
        setContentView(bottomSheetView);

        // 注意：这里要给layout的parent设置peekHeight，而不是在layout里给layout本身设置，下面设置背景色同理，坑爹！！！
        final BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(((View) bottomSheetView.getParent()));

        bottomSheetView.post(new Runnable() {
            @Override
            public void run() {
                bottomSheetBehavior.setPeekHeight(((View) bottomSheetView.getParent()).getHeight());
            }
        });


        itemVideoEncodes = findViewById(R.id.item_pic_sizes);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(DisplayUtils.dp2px(26), DisplayUtils.dp2px(14), DisplayUtils.dp2px(26), DisplayUtils.dp2px(14));

        for(int i = 0; i < CamEncode.encodes.length; i++){
            String s = CamEncode.encodes[i];
            final CheckView checkView = new CheckView(getContext());
            checkView.setLayoutParams(params);
            checkView.setTitle(s);
            checkView.setCheckedIconId(R.drawable.ic_check);

            final int finalI = i;
            checkView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkView.setChecked(true);
                    CamEncode.encodePos = finalI;
                    unCheckOthersView();
                    if(onSelectItemListener!=null){
                        onSelectItemListener.OnSelectItem(finalI);
                    }
                    dismiss();
                }
            });

            if(i == CamEncode.encodePos){
                checkView.setChecked(true);
            }

            checkViews.add(checkView);

            itemVideoEncodes.addView(checkView);
        }
    }

    private void unCheckOthersView(){
        for(int i = 0; i < checkViews.size(); i++){
            if(i != CamEncode.encodePos){
                checkViews.get(i).setChecked(false);
            }
        }
    }

    public void setOnSelectItemListener(OnSelectItemListener onSelectItemListener) {
        this.onSelectItemListener = onSelectItemListener;
    }

}
