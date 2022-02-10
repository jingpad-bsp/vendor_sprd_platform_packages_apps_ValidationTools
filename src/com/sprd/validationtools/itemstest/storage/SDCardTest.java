package com.sprd.validationtools.itemstest.storage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.utils.StorageUtil;
import com.sprd.validationtools.utils.ValidationToolsUtils;
import com.sprd.validationtools.R;

import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.EnvironmentEx;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemProperties;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.graphics.Color;
public class SDCardTest extends BaseActivity {
    private String TAG = "SDCardTest";
    private static final String SPRD_SD_TESTFILE = "sprdtest.txt";
    private static final String PHONE_STORAGE_PATH = "/data/data/com.sprd.validationtools/";
    private Button  startButton;
    private Button  mRetestButton;
    TextView mContent, mContent2;
    byte[] mounted = new byte[2];
    byte[] result = new byte[2];

    private boolean mDisableExternalStorage = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout ll = new LinearLayout(this);
        LayoutParams parms = new LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        ll.setLayoutParams(parms);
        ll.setOrientation(1);
        ll.setGravity(Gravity.CENTER);
        mPassButton.setVisibility(View.GONE);
        startButton = new Button(this);
        startButton.setText(R.string.startTest);
        startButton.setTextSize(25);
         startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               // storeRusult(false);
             //   finish();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startTest(ll);
                    }
                }, 3000);

            }
        });
        ll.addView(startButton);

        mRetestButton = new Button(this);
        mRetestButton.setText(R.string.manual_retest);
        mRetestButton.setTextSize(25);
         mRetestButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               // storeRusult(false);
             //   finish();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startTest(ll);
                    }
                }, 500);

            }
        });
        ll.addView(mRetestButton);
        mRetestButton.setVisibility(View.GONE);

        mContent = new TextView(this);
        mContent.setTextSize(35);
        mContent.setText("");
        ll.addView(mContent);

        mContent2 = new TextView(this);
        mContent2.setTextSize(35);
        mContent2.setText("");
        ll.addView(mContent2);

	setContentView(ll);
    }
    private void  startTest(LinearLayout ll ){
        startButton.setVisibility(View.GONE);
        mRetestButton.setVisibility(View.GONE);

        setTitle(R.string.sdcard_test);
        mContent.setText(getResources().getText(R.string.sdcard2_test));
        mContent2.setText(getResources().getText(R.string.sdcard1_test));
        mFailButton.setEnabled(false);
        //super.removeButton();
        /*SPRD bug 760913:Test can pass/fail must click button*/
        /*if(Const.isBoardISharkL210c10()){
            mPassButton.setVisibility(View.GONE);
        }else{
            super.removeButton();
        }*/
        /*@}*/
        startBackgroundThread();
        mWorkHandler.post(vtThread);
        mDisableExternalStorage = ValidationToolsUtils.isDisableExternalStorage();
        if(mDisableExternalStorage){
            mContent.setVisibility(View.GONE);
        }
	        checkSDCard();
        if (mounted[0] == 1) {
                mContent.setText(getResources().getText(R.string.no_sdcard2));
        }
        if (mounted[1] == 1) {
                mContent2.setText(getResources().getText(R.string.no_sdcard));
        }
        // create thread to execute SDCard test command
        Log.i("SDCardTest",
                "=== create thread to execute SDCard test command! ===");
    }   
    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if(mBackgroundThread != null) {
            stopBackgroundThread();
        }
    }
	
    public byte mSDCardTestFlag[] = new byte[1];
    public Handler mHandler = new Handler();
    public Runnable mRunnable = new Runnable() {
        public void run() {
            Log.i("SDCardTest", "=== display SDCard test info! === mounted[0] = " + mounted[0]
                    + " result[0] = " + result[0] + " mounted[1] = " + mounted[1]
                            + " result[1] = " + result[1]);
			
			StringBuilder sb = new StringBuilder();

            if ((mounted[0] == 0) && (result[0] == 0)) {
				  File ex = EnvironmentEx.getExternalStorageLinkPath();
				  int capacity_ex = StorageUtil.getOTGcapacityFile(getApplicationContext(), ex);
        		  sb.append(getResources().getText(R.string.sdcard2_test_result_success)).append(" [ ").append(capacity_ex).append(" GB ]");
                  mContent.setText(sb);
            } else {
                  mContent.setText(getResources().getText(
                                     R.string.sdcard2_test_result_fail));
            }
            if ((mounted[1] == 0) && (result[1] == 0)) {
				  File in = Environment.getExternalStorageDirectory();
				  int capacity_in = StorageUtil.getOTGcapacityFile(getApplicationContext(), in);
  	              sb.setLength(0);	
        		  sb.append(getResources().getText(R.string.sdcard_test_result_success)).append(" [ ").append(capacity_in).append(" GB ]");
                  mContent2.setText(sb);
            } else {
                  mContent2.setText(getResources().getText(
                                   R.string.sdcard_test_result_fail));
            }

            //If UDT not support SDcard,set result/mount == 0.
            Log.d(TAG, "mRunnable mDisableExternalStorage="+mDisableExternalStorage);
            if(mDisableExternalStorage){
                result[0] = 0;
                mounted[0] = 0;
            }
            if (result[0] == 0 && result[1] == 0 && mounted[0] == 0 && mounted[1] == 0) {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        /*SPRD bug 760913:Test can pass/fail must click button*/
                        if(Const.isBoardISharkL210c10()){
                            mPassButton.setVisibility(View.VISIBLE);
                        }
                        /*@}*/
                        else{
                            Toast.makeText(SDCardTest.this, R.string.text_pass, Toast.LENGTH_SHORT).show();
                            storeRusult(true);
                            finish();
                        }
                    }
                }, 2000);
            }else {
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        /*SPRD bug 760913:Test can pass/fail must click button*/
                        if(Const.isBoardISharkL210c10()){
                        }
                        /*@}*/
                        else{
                            Toast.makeText(SDCardTest.this, R.string.text_fail, Toast.LENGTH_SHORT).show();
                            //storeRusult(false);
                            //finish();
                            mFailButton.setEnabled(true);
                            mRetestButton.setVisibility(View.VISIBLE);
                        }
                    }
                }, 2000);
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void checkSDCard() {
        if (!EnvironmentEx.getExternalStoragePathState().equals(
                Environment.MEDIA_MOUNTED)) {
            mounted[0] = 1;
        } else {
            mounted[0] = 0;
        }
        mounted[1] = 0;
    }

    private Handler mWorkHandler;
    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread;
    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("SdcardBackground");
        mBackgroundThread.start();
        mWorkHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    Thread vtThread = new Thread() {
        public void run() {
            FileInputStream in = null;
            FileOutputStream out = null;
            try {
                if (mounted[1] == 0) {
                    File fp = new File(PHONE_STORAGE_PATH, SPRD_SD_TESTFILE);
                    if (fp.exists())
                        fp.delete();
                    fp.createNewFile();
                    out = new FileOutputStream(fp);
                    mSDCardTestFlag[0] = 'd';
                    out.write(mSDCardTestFlag, 0, 1);
                    out.close();
                    in = new FileInputStream(fp);
                    in.read(mSDCardTestFlag, 0, 1);
                    in.close();
                    if (mSDCardTestFlag[0] == 'd'){
                        result[1] = 0;
                    }else{
                        result[1] = 1;
                    }
                }
                if (mounted[0] == 0) {
                    /* SPRD Bug 940774:OTG test fail, because permission denied. @{ */
                    File secondStorage = null;
                    String secondPath = StorageUtil.getExternalStorageAppPath(getApplicationContext(), Const.EXT_COMMON_PATH);
                    if (secondPath != null) {
                        Log.d(TAG, "secondPath =: " + secondPath);
                        secondStorage = new File(secondPath);
                    }
                    /* @} */
                    if(secondStorage != null){
                        Log.d(TAG, "secondStorage="+secondStorage.getAbsolutePath());
                        File fp = new File(/*EnvironmentEx.getExternalStoragePath()*/secondStorage, SPRD_SD_TESTFILE);
                        if (fp.exists())
                            fp.delete();
                        fp.createNewFile();
                        out = new FileOutputStream(fp);
                        mSDCardTestFlag[0] = '6';
                        out.write(mSDCardTestFlag, 0, 1);
                        out.close();
                        in = new FileInputStream(fp);
                        in.read(mSDCardTestFlag, 0, 1);
                        in.close();
                        if (mSDCardTestFlag[0] == '6') {
                            result[0] = 0;
                        } else {
                            result[0] = 1;
                        }
                    }
                }
                mHandler.post(mRunnable);
            } catch (Exception e) {
                Log.i("SDCardTest", "=== error: Exception happens when sdcard I/O! ===");
                e.printStackTrace();
                /*SPRD bug 752013:Set test fail while throw exception.*/
                mHandler.post(mRunnable);
                /*@}*/
                try {
                    if (out != null) {
                        out.close();
                        out = null;
                    }
                    if (in != null) {
                        in.close();
                        in = null;
                    }
                } catch (IOException io) {
                    Log.e("CTPTest", "close in/out err");
                    io.printStackTrace();
                }
            } finally {
                try {
                    if (out != null) {
                        out.close();
                        out = null;
                    }
                    if (in != null) {
                        in.close();
                        in = null;
                    }
                } catch (IOException io) {
                    Log.e("CTPTest", "close in/out err");
                    io.printStackTrace();
                }
            }
        }
    };

}
