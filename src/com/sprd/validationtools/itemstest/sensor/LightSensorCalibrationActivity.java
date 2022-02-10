
package com.sprd.validationtools.itemstest.sensor;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.R;
import com.sprd.validationtools.utils.FileUtils;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import android.util.Log;
import android.os.Message;
import android.widget.Button;

public class LightSensorCalibrationActivity extends BaseActivity {

    private static final String TAG = "LightSensorCalibrationActivity";
    private static final int SET_CMD_COMPLETE = 1;
    private static final int CALIBRATION_SUCCESS = 2;
    private static final int CALIBRATION_FAIL = 3;
    private static final int CALIBRATION_UNREGISTER = 4;
    private static final int MAX_LUX = 1100;
    private static final int MIN_LUX = 900;

    private static final String SET_CMD = "2 5"; // start calibrating

    private static final String LIGHT_CALIBRATOR_CMD = "/sys/class/sprd_sensorhub/sensor_hub/light_sensor_calibrator";

    private static final String PASS_NUMBER = "0";
    private TextView mDisplayText;
    private boolean isOk = false;
    private boolean saveResult = false;
    private Context mContext;
    private Button startCalibration, checkoutButton;
    private Button mRetestButton;

    private SensorUtils mSensorUtils = null;

    private SensorManager sensorManager = null;
    private Sensor lightSensor = null;
    private SensorEventListener lightListener = null;
    private float luxSum = 0;
    private int count = 0;

    private Runnable mR = new Runnable() {
        public void run() {
            mDisplayText.setText(mContext.getResources().getString(
                    R.string.light_sensor_calibration_fail));
            mFailButton.setEnabled(true);
            mRetestButton.setVisibility(View.VISIBLE);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_light_calibration);
        mContext = this;
        mDisplayText = (TextView) findViewById(R.id.result_sensor_light);
        startCalibration = (Button) findViewById(R.id.start_test_button);

        checkoutButton = (Button) findViewById(R.id.checkout_button);
        checkoutButton.setVisibility(View.GONE);

        /*SPRD bug 760913:Test can pass/fail must click button*/
        //if(Const.isBoardISharkL210c10()){
            mPassButton.setVisibility(View.GONE);
        //}
        /*@}*/
        initSensor();
        startCalibration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "startCalibration");
                mDisplayText.setText(mContext.getResources().getString(
                        R.string.light_sensor_calibration_start));
                mFailButton.setEnabled(false);
                startSensorCalibration();
                startCalibration.setVisibility(View.GONE);
            }
        });
        mRetestButton = (Button) findViewById(R.id.retest_btn);
        mRetestButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "mRetestButton");
                mRetestButton.setVisibility(View.GONE);
                mFailButton.setEnabled(false);
                mDisplayText.setText(mContext.getResources().getString(
                        R.string.light_sensor_calibration_start));
                restartSensorCalibration();
            }
        });
    }

    private void initSensor() {
        Log.d(TAG, "initSensor");
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mSensorUtils = new SensorUtils(this, Sensor.TYPE_LIGHT);
        mSensorUtils.enableSensor();
        lightListener = new SensorEventListener() {
            public void onAccuracyChanged(Sensor s, int accuracy) {
            }

            public void onSensorChanged(SensorEvent event) {
                float x = event.values[SensorManager.DATA_X];
                luxSum += x;
                count += 1;
                Log.d(TAG, "Current light level is "+ x + "lux" + " ; luxSum = " + luxSum + " ; count = " + count);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void startSensorCalibration() {
        new SensorCalibrationThread().start();
    }

    private void restartSensorCalibration() {
        mHandler.removeCallbacks(mR);
        if(mSensorUtils != null){
            mSensorUtils.disableSensor();
        }
        if(sensorManager != null){
            sensorManager.unregisterListener(lightListener);
        }
        luxSum = 0;
        count = 0;
        initSensor();
        new SensorCalibrationThread().start();
    }


    class SensorCalibrationThread extends Thread {
        public void run() {
            sensorCalibration();
        };
    };

    /**
     ** start calibrating echo "0 [SENSOR_ID] 1" > calibrator_cmd
     **/
    private void sensorCalibration() {
        /*SPRD bug 760913:Wait for test begin*/
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        /*@}*/
        FileUtils.writeFile(LIGHT_CALIBRATOR_CMD, SET_CMD);
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mHandler.sendMessage(mHandler.obtainMessage(SET_CMD_COMPLETE));
    }

    /**
     * start calibrating echo "2 [SENSOR_ID] 1" > calibrator_cmd cat calibrator_data, if the get
     * value is 2 ,the test is ok ,or test is fialmHandler.sendMessage(mHandler.obtainMessage(SET_CMD_COMPLETE));
     **/
    private void getResult() {
        checkoutButton.setVisibility(View.VISIBLE);
        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "checkoutButton onClick");
                checkoutButton.setVisibility(View.GONE);
                mFailButton.setEnabled(false);
                try {
                    sensorManager.registerListener(lightListener, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                mHandler.sendMessage(mHandler.obtainMessage(CALIBRATION_UNREGISTER));
            }
        });

        mDisplayText.setText(mContext.getResources().getString(
                R.string.light_sensor_checkout_start));
        String getResult = FileUtils.readFile(LIGHT_CALIBRATOR_CMD);
        Log.d(TAG, "getResult the result of light sensor calibration: " + getResult);

    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SET_CMD_COMPLETE:
                    getResult();
                    break;
                case CALIBRATION_SUCCESS:
                    Toast.makeText(mContext, R.string.text_pass,
                            Toast.LENGTH_SHORT).show();
                    /*SPRD bug 760913:Test can pass/fail must click button*/
                    if(Const.isBoardISharkL210c10()){
                        Log.d("", "isBoardISharkL210c10 is return!");
                        mPassButton.setVisibility(View.VISIBLE);
                        return;
                    }
                    /*@}*/
                    storeRusult(true);
                    finish();
                    break;
                case CALIBRATION_FAIL:
                    mHandler.post(mR);
                    break;
                case CALIBRATION_UNREGISTER:
                    sensorManager.unregisterListener(lightListener);
                    float result = luxSum / count;
                    Log.d(TAG, "-------result =  " + result + " ; luxSum = " + luxSum + " ; count = " + count);
                    if(result >= MIN_LUX && result <= MAX_LUX){
                        mHandler.sendMessage(mHandler.obtainMessage(CALIBRATION_SUCCESS));
                    } else {
                        mHandler.sendMessage(mHandler.obtainMessage(CALIBRATION_FAIL));
                    }
                    break;
                default:
            }
        }
    };

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed" );
        if (true) {
            return;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onDestroy() {
        mHandler.removeCallbacks(mR);
        if(mSensorUtils != null){
            mSensorUtils.disableSensor();
        }
        if(sensorManager != null){
            sensorManager.unregisterListener(lightListener);
        }
        super.onDestroy();
    }
}
