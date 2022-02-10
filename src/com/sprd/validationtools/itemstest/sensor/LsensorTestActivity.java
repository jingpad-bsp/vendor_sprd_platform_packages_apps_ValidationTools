
package com.sprd.validationtools.itemstest.sensor;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.R;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class LsensorTestActivity extends BaseActivity {
    /** the value of change color */
    private static final float VALUE_OF_CHANGE_COLOR = 0.5f;

    /** the default value */
    private static final int LSENSOR_DEFAULT_VALUE = 0;

    private static final String VALUE_FAR = "Distant";

    private static final String VALUE_CLOSE = "Closer";

    private static final float MAXIMUM_BACKLIGHT = 1.0f;

    /** Screen backlight when the value of the darkest */
    private static final float MINIMUM_BACKLIGHT = 0.1f;

    /** sensor manager object */
    private SensorManager lManager = null;

    /** sensor object */
    private Sensor lSensor = null;

    /** sensor listener object */
    private SensorEventListener lListener = null;

    /** the progressBar object */
    private ProgressBar lsensorProgressBar;

    /** the textview object */
    private TextView valueIllumination;

    /** the max value of progressBar */
    private static final int MAX_VALUE_PROGRESSBAR = 300;

    /** System backlight value */
    private int mCurrentValue;

    /** Integer into a floating-point type */
    private float mBrightnessValue;

    /** Brightness current value */
    private static final int BRIGHTNESS_CURRENT_VALUE = 180;

    /** Brightness max value */
    private static final float BRIGHTNESS_MAX_VALUE = 255.0f;

    private Context mContext;

    private int mSensorMin = -1;
    private int mSensorMax = -1;
    private boolean isTestPass = false;


    private static final String TAG = "LsensorTestActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;
        setContentView(R.layout.sensor_light);
        setTitle(R.string.light_sensor_test);
        isTestPass = false;
        initSensor();
        try {
            mCurrentValue = Settings.System.getInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS);
        } catch (SettingNotFoundException e) {
            mCurrentValue = BRIGHTNESS_CURRENT_VALUE;
        }
        mBrightnessValue = mCurrentValue / BRIGHTNESS_MAX_VALUE;

        valueIllumination = (TextView) findViewById(R.id.txt_value_lsensor);

        lsensorProgressBar = (ProgressBar) findViewById(R.id.progressbar_lsensor);
        lsensorProgressBar.setMax(MAX_VALUE_PROGRESSBAR);

        /*SPRD bug 857094:Check light sensor*/
        Log.d(TAG, "onCreate lSensor="+lSensor);

        TextView textTile = (TextView) findViewById(R.id.test_tile);
        if(textTile != null){
            textTile.setText(R.string.tip_for_lsensor);
        }

        /*SPRD bug 760913:Test can pass/fail must click button*/
        //if(Const.isBoardISharkL210c10()){
            mPassButton.setVisibility(View.GONE);
        //}
        /*@}*/
    }

    @Override
    protected void onResume() {
        super.onResume();

        valueIllumination.setText(getString(R.string.lsensor_value) + LSENSOR_DEFAULT_VALUE);
        lsensorProgressBar.setProgress(LSENSOR_DEFAULT_VALUE);
        lManager.registerListener(lListener, lSensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        if (lManager != null) {
            lManager.unregisterListener(lListener);
        }
        setBrightness(mBrightnessValue);
        super.onPause();
    }

    private void showStatus(float x) {
        valueIllumination.setText(getString(R.string.lsensor_value) + x);
        int valueProgress = (int) x;
        lsensorProgressBar.setProgress(valueProgress);
        float mCurrentBrightnessValue = x / BRIGHTNESS_MAX_VALUE;
        if (mCurrentBrightnessValue > MAXIMUM_BACKLIGHT) {
            setBrightness(MAXIMUM_BACKLIGHT);
        } else if (mCurrentBrightnessValue < MINIMUM_BACKLIGHT) {
            setBrightness(MINIMUM_BACKLIGHT);
        } else {
            setBrightness(mCurrentBrightnessValue);
        }
    }

    private void setBrightness(float brightness) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.screenBrightness = brightness;
        getWindow().setAttributes(lp);
    }

    private void initSensor() {
        lManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        lSensor = lManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        lListener = new SensorEventListener() {
            public void onAccuracyChanged(Sensor s, int accuracy) {
            }

            public void onSensorChanged(SensorEvent event) {
                float x = event.values[SensorManager.DATA_X];

                int processInt = (int) x;
                Log.d(TAG, "onSensorChanged x = " + x + " ;processInt = " + processInt + " ; mSensorMin = " + mSensorMin + " ; mSensorMax = " + mSensorMax);
                if (mSensorMin == -1 || mSensorMin > x) {
                    mSensorMin = processInt;
                }

                if (mSensorMax == -1 || mSensorMax < x) {
                    mSensorMax = processInt;
                }
                Log.d(TAG, "onSensorChanged mSensorMax = " + mSensorMax + " ; mSensorMin = " + mSensorMin + " ; isTestPass = " + isTestPass);
                showStatus(x);

                if (((mSensorMax - mSensorMin) > 50) && !isTestPass) {
                    Toast.makeText(mContext, R.string.text_pass, Toast.LENGTH_SHORT).show();
                    /*SPRD bug 760913:Test can pass/fail must click button*/
                    //if(Const.isBoardISharkL210c10()){
                        Log.d(TAG, "isBoardISharkL210c10 is return!");
                        //mPassButton.setVisibility(View.VISIBLE);
                        isTestPass = true;
                        //return;
                    //}
                    /*@}*/
                    storeRusult(true);
                    finish();
                }
            }
        };
    }
}
