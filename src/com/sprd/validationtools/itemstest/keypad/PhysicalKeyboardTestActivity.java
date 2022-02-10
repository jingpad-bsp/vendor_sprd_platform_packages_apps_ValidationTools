
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

import android.view.View;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.R;
import android.graphics.Color;

import android.hardware.input.InputManager;
import android.widget.TextView;
import android.view.InputDevice;

public class PhysicalKeyboardTestActivity extends BaseActivity implements InputManager.InputDeviceListener {
    private static final String TAG = "PhysicalKeyboardTestActivity";
    private TextView mTips;
    private Button mKeyNumber1;
    private Button mKeyNumber2;
    private Button mKeyNumber3;
    private Button mKeyCharator1;
    private Button mKeyCharator2;
    private Button mKeySpace;
    private byte keyPressedFlag = 0;
    private byte keySupportFlag = 63;
    private boolean keyPressedPass = false;
    public Handler mHandler = new Handler();
    public boolean shouldBack = false;

    private InputManager mIm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.physical_keyboard_test);
        setTitle(R.string.physical_keyboard_test);
        mTips = (TextView) findViewById(R.id.tv_tips);
        mKeyNumber1 = (Button) findViewById(R.id.button_number1);
        mKeyNumber2 = (Button) findViewById(R.id.button_number2);
        mKeyNumber3 = (Button) findViewById(R.id.button_number3);
        mKeyCharator1 = (Button) findViewById(R.id.button_charater1);
        mKeyCharator2 = (Button) findViewById(R.id.button_charater2);
        mKeySpace = (Button) findViewById(R.id.button_space);

        keyPressedPass = false;

        mIm = (InputManager) getSystemService(INPUT_SERVICE);

        mPassButton.setEnabled(false);
        mPassButton.setBackgroundColor(Color.GRAY);        
        mPassButton.setVisibility(View.GONE);

        updateSummary();
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
	Log.i(TAG, "code="+keyCode);
        if (KeyEvent.KEYCODE_1 == keyCode) {
            mKeyNumber1.setTextColor(Color.GREEN);
            mKeyNumber1.setPressed(true);
            keyPressedFlag |= 1;
        } else if (KeyEvent.KEYCODE_6 == keyCode) {
            mKeyNumber2.setTextColor(Color.GREEN);
            mKeyNumber2.setPressed(true);
            keyPressedFlag |= 2;
        } else if (KeyEvent.KEYCODE_0 == keyCode) {
            mKeyNumber3.setTextColor(Color.GREEN);
            mKeyNumber3.setPressed(true);
            keyPressedFlag |= 4;
        } else if (KeyEvent.KEYCODE_A == keyCode) {
            mKeyCharator1.setTextColor(Color.GREEN);
            mKeyCharator1.setPressed(true);
            keyPressedFlag |= 8;
        } else if (KeyEvent.KEYCODE_H == keyCode) {
            mKeyCharator2.setTextColor(Color.GREEN);
            mKeyCharator2.setPressed(true);
            keyPressedFlag |= 16;
        } else if (KeyEvent.KEYCODE_SPACE == keyCode) {
            mKeySpace.setTextColor(Color.GREEN);
            mKeySpace.setPressed(true);
            keyPressedFlag |= 32;
        }

        Log.i(TAG, " keySupportFlag = " + keySupportFlag + " ; keyPressedFlag = " + keyPressedFlag + " ; keyPressedPass = " + keyPressedPass);
        if ((keySupportFlag == keyPressedFlag) && !keyPressedPass) {
            BaseActivity.shouldCanceled = false;
            keyPressedPass = true;
            mPassButton.setEnabled(true);
            mPassButton.setBackgroundColor(Color.GREEN); 
            mPassButton.setVisibility(View.VISIBLE);
        }
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return super.onKeyUp(keyCode, event);
    }


    @Override
    public void onPause() {
        super.onPause();

        mIm.unregisterInputDeviceListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        mIm.registerInputDeviceListener(this, null);
    }

    @Override
    public void onInputDeviceAdded(int deviceId) {
        updateSummary();
    }

    @Override
    public void onInputDeviceRemoved(int deviceId) {
        updateSummary();
    }

    @Override
    public void onInputDeviceChanged(int deviceId) {
        updateSummary();
    }

    private void updateSummary() {
        if (mTips == null) {
            return;
        }

        if (mIm == null) {
            mTips.setText(R.string.physical_keyboard_test_tip1);
            return;
        }

        for (int deviceId : InputDevice.getDeviceIds()) {
            final InputDevice device = InputDevice.getDevice(deviceId);
            if (device != null && !device.isVirtual() && device.isFullKeyboard()) {
                mTips.setText(R.string.physical_keyboard_test_tip2);
                return;
            }

        }

        mTips.setText(R.string.physical_keyboard_test_tip1);
        return;

    }
}
