
package com.sprd.validationtools.itemstest.hall;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.R;

public class HallTestActivity extends BaseActivity {
    private static final String TAG = "HallTestActivity";
    public Handler mHandler = new Handler();
    private TextView mClose,mOff;
    private boolean flag_close,flag_off;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hall_test);
        setTitle(R.string.hall_test);
        mClose = findViewById(R.id.hall_state_close);
        mOff = findViewById(R.id.hall_state_off);
        mPassButton.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.i(TAG,"onKeyDown keyCode = " + keyCode);
        if (KeyEvent.KEYCODE_HALL_DOWN == keyCode) {
            flag_close = true;
            mClose.setText(R.string.text_pass);
        }else if(KeyEvent.KEYCODE_HALL_UP == keyCode){
            flag_off = true;
            mOff.setText(R.string.text_pass);
        }

        if(flag_close && flag_off) {
            storeRusult(true);
            Toast.makeText(HallTestActivity.this, R.string.text_pass, Toast.LENGTH_SHORT).show();
            mPassButton.setVisibility(View.VISIBLE);
        }
        return true;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return super.onKeyUp(keyCode, event);
    }
}
