/*
 * Copyright (c) 2011-2013, Qualcomm Technologies, Inc. All Rights Reserved.
 * Qualcomm Technologies Proprietary and Confidential.
 */

package com.sprd.validationtools.itemstest.encryptionChip;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
//import android.os.SystemProperties;
import android.view.WindowManager;
import android.widget.LinearLayout;
import com.sprd.validationtools.R;
import com.sprd.validationtools.utils.NativeEncryptionChip;
import com.sprd.validationtools.BaseActivity;

public class EncryptionChip extends BaseActivity {
   
    TextView mTextView;
    String TAG = "EncryptionChip";
	private String testresult = "";
	private int fd = 0;
    private LinearLayout container;
    Handler TimerHandler=new Handler(); 
    @Override
    public void finish() {
        super.finish();
    }
    
 
    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Log.e("handleMessage~testresult = ",testresult);
            TimerHandler.removeCallbacks(myTimerRun);
			mTextView.setText(TAG + " : " + testresult);
        };
    };
    
    Runnable myTimerRun=new Runnable() 
    {              
        @Override
        public void run()
        {
             mTextView.setText(TAG + " : " + "test fail");;
        }
 
    };

    void bindView() {
        loge("bindView");
        mTextView = (TextView) findViewById(R.id.test_result);
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.encrytionchip);
        loge("onCreate");
        bindView();
        mTextView.setText(TAG + " : " + "Serial configure start");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		container = (LinearLayout) findViewById(R.id.sim_test_result_container);

    }
    
    @Override
    protected void onResume() {
        super.onResume();
        begintotest();
    }
   void begintotest(){
        
        new Thread() {
            
            public void run() {
                loge("EncryptionChip begin");
                TimerHandler.postDelayed(myTimerRun, 3000); 
				testresult = NativeEncryptionChip.native_encryptionChiptest();
                Log.e("EncryptionChip testresult  = ",testresult);
				mHandler.sendEmptyMessageDelayed(0,1*1000);
                
            }
        }.start();
        
    }

    
    void fail(Object msg) {
        loge(msg);
        finish();
    }
    
    void pass() {
        finish();
        
    }
    
    @Override
    protected void onDestroy() {    
        super.onDestroy();
    }    
    private void loge(Object e) {
        
        if (e == null)
            return;
        Thread mThread = Thread.currentThread();
        StackTraceElement[] mStackTrace = mThread.getStackTrace();
        String mMethodName = mStackTrace[3].getMethodName();
        e = "[" + mMethodName + "] " + e;
        Log.e(TAG, e + "");
    }
    
    private void logd(Object s) {
        
        Thread mThread = Thread.currentThread();
        StackTraceElement[] mStackTrace = mThread.getStackTrace();
        String mMethodName = mStackTrace[3].getMethodName();
        
        s = "[" + mMethodName + "] " + s;
        Log.d(TAG, s + "");
    }
    
}
