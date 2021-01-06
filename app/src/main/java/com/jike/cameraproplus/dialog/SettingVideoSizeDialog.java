package com.jike.cameraproplus.dialog;

import android.content.Context;
import android.util.Size;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.daily.flexui.util.DisplayUtils;
import com.jike.cameraproplus.R;
import com.jike.cameraproplus.cameradata.CamSize;
import com.jike.cameraproplus.view.CheckView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;

public class SettingVideoSizeDialog extends BottomSheetDialog {

    private LinearLayout itemRearVideoSizes;

    private ArrayList<CheckView> checkViews = new ArrayList<>();

    private OnSelectItemListener onSelectItemListener;

    public SettingVideoSizeDialog(@NonNull Context context) {
        super(context);
        createView(context);
    }

    public void createView(Context context) {
        final View bottomSheetView = getLayoutInflater().inflate(R.layout.dialog_setting_video_size, null);
        setContentView(bottomSheetView);

        // 注意：这里要给layout的parent设置peekHeight，而不是在layout里给layout本身设置，下面设置背景色同理，坑爹！！！
        final BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(((View) bottomSheetView.getParent()));

        bottomSheetView.post(new Runnable() {
            @Override
            public void run() {
                bottomSheetBehavior.setPeekHeight(((View) bottomSheetView.getParent()).getHeight());
            }
        });

        itemRearVideoSizes = findViewById(R.id.item_pic_sizes);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(DisplayUtils.dp2px(26), DisplayUtils.dp2px(14), DisplayUtils.dp2px(26), DisplayUtils.dp2px(14));

        for(int i = 0; i < CamSize.getPicSizes().size(); i++){
            Size size = CamSize.getPicSizes().get(i);
            final CheckView checkView = new CheckView(getContext());

            int gcd = gcd(size.getWidth(),size.getHeight());

            checkView.setLayoutParams(params);
            checkView.setTitle(""+size.getWidth()+"x"+size.getHeight()+" - "+size.getWidth()/gcd+" : "+size.getHeight()/gcd);
            checkView.setCheckedIconId(R.drawable.ic_check);

            final int finalI = i;
            checkView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkView.setChecked(true);
                    CamSize.videoIndex = finalI;
                    unCheckOthersView();
                    if(onSelectItemListener!=null){
                        onSelectItemListener.OnSelectItem(finalI);
                    }
                    dismiss();
                }
            });

            if(i == CamSize.videoIndex){
                checkView.setChecked(true);
            }

            checkViews.add(checkView);

            itemRearVideoSizes.addView(checkView);
        }
    }

    private void unCheckOthersView(){
        for(int i = 0; i < checkViews.size(); i++){
            if(i != CamSize.videoIndex){
                checkViews.get(i).setChecked(false);
            }
        }
    }

    public void setOnSelectItemListener(OnSelectItemListener onSelectItemListener) {
        this.onSelectItemListener = onSelectItemListener;
    }

    public static int gcd(int x, int y){
        if(y == 0)
            return x;
        else
            return gcd(y,x%y);
    }

}
