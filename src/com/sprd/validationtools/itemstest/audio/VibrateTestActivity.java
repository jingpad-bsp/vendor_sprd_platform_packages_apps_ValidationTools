package com.sprd.validationtools.itemstest.audio;

import android.app.Activity;
import android.view.View;
import android.view.KeyEvent;
import android.view.Window;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.BufferedReader;
import android.view.WindowManager;
import android.view.WindowManager;
import android.os.Handler;
import android.os.Message;
import android.graphics.Color;
import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.R;

public class VibrateTestActivity extends BaseActivity
{
    private static final String TAG = "VibrateTestActivity";
    private String display;
    Button failBtn = null;
    private String imeiStr;
    private String imeiStr2;
    private TextView mTextView;
    private String snStr;
    Button successBtn = null;
    private TelephonyManager tm;
    private static final int successBtnTrue= 1;

    protected void onCreate(Bundle paramBundle)
    {
        super.onCreate(paramBundle);
        setContentView(R.layout.vibrate_test_activity); 
        Log.d(TAG, " onCreate() " );
        try
        {
            mTextView = ((TextView)findViewById(R.id.vibrate_textview));
            mTextView.setVisibility(View.GONE);
            
            ImageView icon = (ImageView)findViewById(R.id.iv_icon);
            icon.setVisibility(View.GONE);
            TextView tips = (TextView)findViewById(R.id.tv_tips);
            tips.setText(getString(R.string.vibrate_tips));
            icon.setImageDrawable(getResources().getDrawable(R.drawable.vibon));
        }
        catch (Exception localException)
        {
            localException.printStackTrace();
            //Log.v("FactoryMode", "KeyActivity getListener Exception.");
        }
    }

    protected void onPause()
    {
        Log.d(TAG, " onPause() " );
        //WakeLock.releaseCpuLock();
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Vibrator mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mVibrator.cancel();
        super.onPause();
    }
    
    protected void onResume()
    {
        Log.d(TAG, " onResume() " );
        //WakeLock.acquireCpuWakeLock(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Vibrator mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {100, 500, 100, 500, 50, 500};
        mVibrator.vibrate(pattern, 0);
        mPassButton.setEnabled(false);
        mPassButton.setBackgroundColor(Color.GRAY);
        mFailButton.setEnabled(false);
        mHandler.sendEmptyMessageDelayed(successBtnTrue,3000);
        
        super.onResume();
    }

     private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.d(TAG, "  handleMessage,msg = " + msg);
            switch(msg.what) {
            case successBtnTrue:
                mPassButton.setEnabled(true);
                mPassButton.setBackgroundColor(Color.GREEN);
                mFailButton.setEnabled(true);
                break;
            }
        }
     };
        
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) 
    {
        boolean handled = false;
        Log.d(TAG, "  onKeyDown() , keyCode = " + keyCode);
        if(keyCode == KeyEvent.KEYCODE_BACK)
        {
            return true;
        }
        else
        {
            return super.onKeyDown(keyCode, event);
        }
    }
}

