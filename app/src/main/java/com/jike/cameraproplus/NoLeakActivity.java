package com.jike.cameraproplus;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import androidx.appcompat.app.AppCompatActivity;

import java.lang.ref.WeakReference;

public class NoLeakActivity extends AppCompatActivity {

    private NoLeakHandler mHandler;

    public NoLeakHandler getmHandler() {
        return mHandler;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mHandler = new NoLeakHandler(this);

        Message message = Message.obtain();

        mHandler.sendMessageDelayed(message,10*60*1000);
    }

    private static class NoLeakHandler extends Handler {
        private WeakReference<NoLeakActivity> mActivity;

        public NoLeakHandler(NoLeakActivity activity){
            mActivity = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    }
}
