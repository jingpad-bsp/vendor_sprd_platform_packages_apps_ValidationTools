
package com.sprd.validationtools.itemstest.keypad;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;

import android.view.View;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.R;
import android.graphics.Color;
public class KeyTestActivity extends BaseActivity {
    private static final String TAG = "KeyTestActivity";
    private ImageButton mHomeButton;
    private ImageButton mMenuButton;
    private ImageButton mBackButton;
    private ImageButton mVolumeUpButton;
    private ImageButton mVolumeDownButton;
    private ImageButton mCameraButton;
    private ImageButton mPowerButton;
    private byte keyPressedFlag = 0;
    private byte keySupportFlag = 0;
    private boolean isHideCamera = false;
    private boolean keyPressedPass = false;
    private AlertDialog mCameraConfirmDialog;
    public Handler mHandler = new Handler();
    public boolean shouldBack = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.key_test);
        setTitle(R.string.key_test);
        mHomeButton = (ImageButton) findViewById(R.id.home_button);
        mMenuButton = (ImageButton) findViewById(R.id.menu_button);
        mBackButton = (ImageButton) findViewById(R.id.back_button);
        mVolumeUpButton = (ImageButton) findViewById(R.id.volume_up_button);
        mVolumeDownButton = (ImageButton) findViewById(R.id.volume_down_button);
        mPowerButton = (ImageButton) findViewById(R.id.power_off);
        //mCameraButton = (ImageButton) findViewById(R.id.camera_button);
        //showHasCameraDialog();
        mPassButton.setEnabled(false);
        mPassButton.setBackgroundColor(Color.GRAY);        
        isHideCamera = true;
        keyPressedPass = false;
        showKey(isHideCamera);  //hide the camera key in test screen,by htian 20150829
		/*mFailButton.setEnabled(false);
		mFailButton.setBackgroundColor(Color.GRAY);  
		mHandler.postDelayed(new Runnable(){
		            @Override
		            public void run() {
		                  mFailButton.setEnabled(true);
		   		      mFailButton.setBackgroundColor(Color.RED);     
		            }
		        }, 3000);*/
        /*SPRD bug 760913:Test can pass/fail must click button*/
        if(Const.isBoardISharkL210c10()){
            mPassButton.setVisibility(View.GONE);
        }
        /*@}*/
    }


        //boolean hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey();

    protected void showHasCameraDialog() {
        Dialog cameraKeyDialog = new AlertDialog.Builder(this)
                .setIconAttribute(android.R.attr.alertDialogIcon)
                .setTitle(this.getString(R.string.has_camera_title))
                .setCancelable(false)
                .setNegativeButton(R.string.has_camera_key, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        isHideCamera = false;
                        showKey(isHideCamera);
                        dialog.dismiss();
                    }
                })
                .setPositiveButton(R.string.no_camera_key, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub
                        isHideCamera = true;
                        showKey(isHideCamera);
                        dialog.dismiss();
                    }
                }).create();
        cameraKeyDialog.show();
    }

    private void showKey(boolean hideCamera) {
        if (Const.isHomeSupport(this)) {
            mHomeButton.setVisibility(View.VISIBLE);
            keySupportFlag |= 1;
        }else {
/* SPRD: modify 20140529 Spreadtrum of 305634 MMI test,lack of button which is on the right 0f "Home" @{ */
            mHomeButton.setVisibility(View.GONE);
/* @} */
        }
        if (Const.isBackSupport(this)) {
            mBackButton.setVisibility(View.VISIBLE);
            keySupportFlag |= 2;
        }else {
/* SPRD: modify 20140529 Spreadtrum of 305634 MMI test,lack of button which is on the right 0f "Home" @{ */
            mBackButton.setVisibility(View.GONE);
/* @} */
        }
        if (Const.isMenuSupport(this)) {
            mMenuButton.setVisibility(View.VISIBLE);
            keySupportFlag |= 4;
        }else {
            mMenuButton.setVisibility(View.GONE);
        }
        if (Const.isPowerSupport(this)) {
            mPowerButton.setVisibility(View.VISIBLE);
            keySupportFlag |= 8;
        }
        if (Const.isVolumeUpSupport()) {
            mVolumeUpButton.setVisibility(View.VISIBLE);
            keySupportFlag |= 16;
        }
        if (Const.isVolumeDownSupport()) {
            mVolumeDownButton.setVisibility(View.VISIBLE);
            keySupportFlag |= 32;
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
	Log.i(TAG, "code="+keyCode);
        if (KeyEvent.KEYCODE_HOME == keyCode) {
            mHomeButton.setPressed(true);
            mHomeButton.clearAnimation();
            mHomeButton.setVisibility(View.GONE);
            //keyPressedFlag |= 1;
        } else if (KeyEvent.KEYCODE_BACK == keyCode) {
            if (!mBackButton.isPressed()) {
                mBackButton.setVisibility(View.GONE);
                mBackButton.setVisibility(View.GONE);
                //keyPressedFlag |= 2;
            } else {
                return super.onKeyDown(keyCode, event);
            }
        } else if (KeyEvent.KEYCODE_APP_SWITCH == keyCode || KeyEvent.KEYCODE_MENU == keyCode || 999 == keyCode) {
            mMenuButton.setPressed(true);
            mMenuButton.clearAnimation();
            mMenuButton.setVisibility(View.GONE);
            //keyPressedFlag |= 4;
        } else if (KeyEvent.KEYCODE_POWER == keyCode) {
            mPowerButton.setPressed(true);
             mPowerButton.clearAnimation();
            mPowerButton.setVisibility(View.GONE);
            keyPressedFlag |= 8;
        }/* else if (KeyEvent.KEYCODE_CAMERA == keyCode) {
        }*/ else if (KeyEvent.KEYCODE_VOLUME_UP == keyCode) {
            mVolumeUpButton.setPressed(true);
            mVolumeUpButton.clearAnimation();
            mVolumeUpButton.setVisibility(View.GONE);
            keyPressedFlag |= 16;
        } else if (KeyEvent.KEYCODE_VOLUME_DOWN == keyCode) {
            mVolumeDownButton.setPressed(true);
             mVolumeDownButton.clearAnimation();
            mVolumeDownButton.setVisibility(View.GONE);
            keyPressedFlag |= 32;
        }
        Log.i(TAG, " keySupportFlag = " + keySupportFlag + " ; keyPressedFlag = " + keyPressedFlag + " ; keyPressedPass = " + keyPressedPass);
        if ((keySupportFlag == keyPressedFlag) && !keyPressedPass) {
            BaseActivity.shouldCanceled = false;
            keyPressedPass = true;
            //showResultDialog(getString(R.string.key_test_info));
            /*SPRD bug 760913:Test can pass/fail must click button*/
            if(!Const.isBoardISharkL210c10()){
                storeRusult(true);
                mHandler.postDelayed(new Runnable(){
                    @Override
                    public void run() {
                        finish();
                    }
                /*SPRD bug 752003:Avoid press home key go to launcher*/
                }, 100);
            }
            /*@}*/
            /*SPRD bug 760913:Test can pass/fail must click button*/
            if(Const.isBoardISharkL210c10()){
                mPassButton.setVisibility(View.VISIBLE);
            }
            /*@}*/
        }
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return super.onKeyUp(keyCode, event);
    }
}
