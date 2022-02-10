
package com.sprd.validationtools.itemstest.sensor;

import java.util.Timer;
import java.util.TimerTask;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.R;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;

public class ASensorSlopeTestActivity extends BaseActivity {
    private static final String TAG = "ASensorSlopeTestActivity";
    private static final int DATA_X = 0;
    private static final int DATA_Y = 1;
    private static final int DATA_Z = 2;

    private static final int DELAY_TIME = 300;

    /** sensor manager object */
    private SensorManager manager = null;

    /** sensor object */
    private Sensor sensor = null;

    /** sensor listener object */
    private SensorEventListener listener = null;

    private Timer mTimer;
    private Button mStartButton;
    private static final double Xmin = -5.28;
    private static final double Xmax = -4.62;
    private static final double Ymin = 4.62;
    private static final double Ymax = 5.28;
    private static final double Zmin = 6.601;
    private static final double Zmax = 7.267;
    int mTestpassNum = 0;
    int X_pass = 0;
    int Y_pass = 0;
    int Z_pass = 0;
    private float[] mValues;
//    public Handler mHandler = new Handler();
    private static final int SHOW_MSG = 1;
    private TextView mTextView = null;
    private TextView mTextViewxyz = null;
    public Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
            case SHOW_MSG:
                String text = (String) msg.obj;
                if(mTextView != null){
                    mTextView.setText(text);
                }
                break;

            default:
                break;
            }
        };
    };
    private void showMsg(float x, float y, float z) {
        StringBuffer text = new StringBuffer("");
        if (sensor != null){
            text.append("chip id: " + sensor.getName() + "\n");
        }
        text.append(" X = " + x + "\n");
        text.append(" Y = " + y + "\n");
        text.append(" Z = " + z + "\n");
        Message message = Message.obtain();
        message.obj = text.toString();
        message.what = SHOW_MSG;
        mHandler.sendMessage(message);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.sensor_a_slope);
        setTitle(R.string.a_sensor_test);
        mTextView = (TextView) findViewById(R.id.result_sensor_slope_a);
        mTextViewxyz = (TextView) findViewById(R.id.result_sensor_slope_xyz);
        mTextViewxyz.setText("");
        /*SPRD bug 760913:Test can pass/fail must click button*/
     //   if(Const.isBoardISharkL210c10()){
            mPassButton.setVisibility(View.GONE);
     //   }
        /*@}*/
        initSensor();
        mTestpassNum = 0;
        mStartButton = (Button) findViewById(R.id.start_btn);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "mStartButton");
                mStartButton.setVisibility(View.GONE);
                mTestpassNum = 0;
                starttime();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME);
    }

    private void starttime() {
        Log.i(TAG, "starttime ");
        if (mTimer != null) {
            Log.i(TAG, "starttime mTimer cancel ");
            mTimer.cancel();
            mTimer = null;
        }
        Log.i(TAG, "starttime1 ");
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (mValues != null) {
                            float x = mValues[DATA_X];
                            float y = mValues[DATA_Y];
                            float z = mValues[DATA_Z];
                            Log.i(TAG, "starttime x = " + x + "; y = " + y + "; z = " + z);
                            showArrow(x, y, z);
                        }
                    }
                });
            }
        }, 0, DELAY_TIME);
    }

    @Override
    protected void onPause() {
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        manager.unregisterListener(listener);
        super.onPause();
    }

    private void initSensor() {
        listener = new SensorEventListener() {
            public void onAccuracyChanged(Sensor s, int accuracy) {
            }

            public void onSensorChanged(SensorEvent event) {
                mValues = event.values;
                float x = event.values[SensorManager.DATA_X];
                float y = event.values[SensorManager.DATA_Y];
                float z = event.values[SensorManager.DATA_Z];
                Log.i(TAG, "initSensor x = " + x + "; y = " + y + "; z = " + z);
                showMsg(x, y, z);
            }
        };

        manager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        sensor = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    private void showArrow(float x, float y, float z) {
        StringBuffer textxyz = new StringBuffer("");
        Log.i(TAG, "showArrow x = " + x + "; y = " + y + "; z = " + z + " ; mTestpassNum = " +mTestpassNum);
        if ((Xmin <= x) && (Xmax >= x)) {
            X_pass = 1;
        }
        if ((Ymin <= y) && (Ymax >= y)) {
            Y_pass = 1;
        }
        if ((Zmin <= z) && (Zmax >= z)) {
            Z_pass = 1;
        }

        Log.i(TAG, "showArrow X_pass = " + X_pass + "; Y_pass = " + Y_pass + "; Z_pass = " + Z_pass);

        if (1 == X_pass) textxyz.append("X: " + "PASS \n");
        if (1 == Y_pass) textxyz.append("Y: " + "PASS \n");
        if (1 == Z_pass) textxyz.append("Z: " + "PASS \n");

        mTextViewxyz.setTextColor(Color.GREEN);
        mTextViewxyz.setText(textxyz);

        if ((1 == X_pass) && (1 == Y_pass) && (1 == Z_pass)) {
            mTestpassNum ++;
            Log.i(TAG, "showArrow mTestpassNum = " +mTestpassNum);
        }

        if ( mTestpassNum == 1) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(ASensorSlopeTestActivity.this, R.string.text_pass,
                                Toast.LENGTH_SHORT).show();
                    Log.d(TAG, " test pass!");
                    /*SPRD bug 760913:Test can pass/fail must click button*/
                    if(Const.isBoardISharkL210c10()){
                        Log.d("", "isBoardISharkL210c10 is return!");
                        mPassButton.setVisibility(View.VISIBLE);
                        return;
                    }
                    /*@}*/
                    storeRusult(true);
                    finish();
                }
            }, 500);

        }
    }
}
