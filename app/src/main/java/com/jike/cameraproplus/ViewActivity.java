package com.jike.cameraproplus;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.daily.flexui.activity.SlideActivity;
import com.daily.flexui.util.AppContextUtils;
import com.daily.flexui.view.SwitchButton;
import com.daily.flexui.viewgroup.NeonLayout;
import com.jike.cameraproplus.cameradata.CamRates;
import com.jike.cameraproplus.cameradata.CamSetting;

import java.io.File;


public class ViewActivity extends SlideActivity{

    private ImageView imageViewHigh,imageViewLow;

    private FrameLayout container;

    private NeonLayout delete;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppContextUtils.setAppActivity(this);
        AppContextUtils.setAppContext(this);
        setContentView(R.layout.activity_view);

        delete = findViewById(R.id.delete);
        container = findViewById(R.id.container);
        imageViewHigh = findViewById(R.id.image_high);
        imageViewLow = findViewById(R.id.image_low);

        container.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    imageViewHigh.setVisibility(View.INVISIBLE);
                } else {
                    imageViewHigh.setVisibility(View.VISIBLE);
                }
                return true;
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File fileH = new File("sdcard/dataset/highres/");
                File[] filesH = fileH.listFiles();

                File fileL = new File("sdcard/dataset/lowres/");
                File[] filesL = fileL.listFiles();

                filesH[filesH.length - 1].delete();
                filesL[filesL.length - 1].delete();
                initData();

                Toast.makeText(getApplicationContext(),"删除成功",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
    }

    private void initData(){
        File fileH = new File("sdcard/dataset/highres/");
        File[] filesH = fileH.listFiles();

        File fileL = new File("sdcard/dataset/lowres/");
        File[] filesL = fileL.listFiles();

        Bitmap imageHigh = BitmapFactory.decodeFile(filesH[filesH.length - 1].getAbsolutePath());
        Bitmap imageLow = BitmapFactory.decodeFile(filesL[filesL.length - 1].getAbsolutePath());

        imageHigh = zoomImage(imageHigh,1440);
        imageLow = zoomImage(imageLow,1440);

        imageViewHigh.setImageBitmap(imageHigh);
        imageViewLow.setImageBitmap(imageLow);
    }

    public static Bitmap zoomImage(Bitmap bgimage, double newWidth) {

        float width = bgimage.getWidth();
        float height = bgimage.getHeight();

        Matrix matrix = new Matrix();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = scaleWidth;

        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int) width,
                (int) height, matrix, true);
        return bitmap;
    }

}
