package com.sprd.validationtools.itemstest.camera;

import android.app.Activity;
import android.app.Notification;
import android.graphics.Color;
import android.app.NotificationManager;
import android.view.View;
import android.view.KeyEvent;
import android.view.Window;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.Vibrator;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.BufferedReader;

import android.view.WindowManager;
import android.hardware.Camera;
import android.widget.Toast;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.R;
import com.sprd.validationtools.utils.FileUtils;
import com.sprd.validationtools.utils.StorageUtil;

public class CameraFlashTestActivity extends BaseActivity {
    private static final String TAG = "CameraFlashTestActivity";
    private String display;
    private String imeiStr;
    private String imeiStr2;
    private TextView mTextView;
    private String snStr;
    private Button mRestButton = null;
    private TelephonyManager tm;
    private static final int camerFlashLightOffOn = 3;
    private static final int camerFlashLightOff = 4;
    private Camera camera;
    private Camera.Parameters params;

    private static final int CAMERA_FLASH_LIGHT = 0;
    private static final int CAMERA_SECOND_FLASH_LIGHT = 1;
    private static final int CAMERA_FLASH_LIGHT_CLOSE = 2;
    private static final int CAMERA_SECOND_FLASH_LIGHT_CLOSE = 3;

    protected void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        setContentView(R.layout.camera_flash);
        Log.i(TAG, "onCreate");
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //startDoubleFlashLightTest();
        openFlashLightOften();
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause");
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //stopDoubleFlashLightTest();
        closeFlashLightOften();
        super.onPause();
    }

    @Override
    public void onDestroy(){
        Log.i(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i(TAG, "onKeyDown keyCode = " + keyCode);
        boolean handled = false;
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }

    }

    private void startDoubleFlashLightTest() {
        mHandler.sendEmptyMessageDelayed(CAMERA_FLASH_LIGHT, 300);
    }

    private void openFlashLight(int type) {
        Log.i(TAG, "openFlashLight type = " + type);
        if(type == CAMERA_FLASH_LIGHT) {
            writeFlashDev("0x10");
            mHandler.sendEmptyMessageDelayed(CAMERA_FLASH_LIGHT_CLOSE, 1000);
        } else {
            writeFlashDev("0x20");
            mHandler.sendEmptyMessageDelayed(CAMERA_SECOND_FLASH_LIGHT_CLOSE, 1000);
        }
    }

    private void closeFlashLight(int type) {
        Log.i(TAG, "closeFlashLight type = " + type);
        if(type == CAMERA_FLASH_LIGHT) {
            writeFlashDev("0x11");
            mHandler.sendEmptyMessageDelayed(CAMERA_SECOND_FLASH_LIGHT, 1000);
        } else {
            writeFlashDev("0x21");
            mHandler.sendEmptyMessageDelayed(CAMERA_FLASH_LIGHT, 1000);
        }
    }

    private void openFlashLightOften() {
        Log.i(TAG, " openFlashLightOften ");
        writeFlashDev("0x10");
    }

    private void closeFlashLightOften() {
        try {
            writeFlashDev("0x11");
            Log.i(TAG, " closeFlashLightOften ");
        } catch (Throwable e) {
            e.printStackTrace();
            Log.i(TAG, " closeFlashLightOften Throwable e ");
        }
    }

    private void writeFlashDev(String cmd) {
        Log.i(TAG, "writeFlashDev cmd = " + cmd);
        FileUtils.writeFile(Const.CAMERA_FLASH, cmd);
    }

    private void stopDoubleFlashLightTest() {
        Log.i(TAG, "stopDoubleFlashLightTest ");
        mHandler.removeMessages(CAMERA_FLASH_LIGHT);
        mHandler.removeMessages(CAMERA_SECOND_FLASH_LIGHT);
        mHandler.removeMessages(CAMERA_FLASH_LIGHT_CLOSE);
        mHandler.removeMessages(CAMERA_SECOND_FLASH_LIGHT_CLOSE);
        /* SPRD bug 753892 : Flashlight maybe not close */
        try {
            writeFlashDev("0x11");
            writeFlashDev("0x21");
        } catch (Throwable e) {
            e.printStackTrace();
        }
        /* @} */
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case CAMERA_FLASH_LIGHT:
                openFlashLight(CAMERA_FLASH_LIGHT);
                break;
            case CAMERA_SECOND_FLASH_LIGHT:
                openFlashLight(CAMERA_SECOND_FLASH_LIGHT);
                break;
            case CAMERA_FLASH_LIGHT_CLOSE:
                closeFlashLight(CAMERA_FLASH_LIGHT);
                break;
            case CAMERA_SECOND_FLASH_LIGHT_CLOSE:
                closeFlashLight(CAMERA_SECOND_FLASH_LIGHT);
                break;
            }
            super.handleMessage(msg);
        }
    };

}



