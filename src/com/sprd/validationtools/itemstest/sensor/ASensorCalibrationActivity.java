
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

import java.util.Timer;
import java.util.TimerTask;

public class ASensorCalibrationActivity extends BaseActivity {

    private static final String TAG = "ASensorCalibrationActivity";
    private static final int SHOW_MSG = 0;
    private static final int SET_CMD_COMPLETE = 1;
    private static final int CALIBRATION_SUCCESS = 2;
    private static final int CALIBRATION_FAIL = 3;

    private static final String SET_CMD = "0 1 1"; // start calibrating
    private static final String GET_RESULT = "1 1 1";// get result of Calibration
    private static final String SAVE_RESULT = "3 1 1";// save the result

    private static final String PASS_NUMBER = "0";
    private static final String TEST_OK = "2";
    private TextView mDisplayText;
    private boolean isOk = false;
    private boolean saveResult = false;
    private Context mContext;

    private SensorUtils mSensorUtils = null;
    private Button mRetestButton;
    private float[] mValues;
    private static final int DATA_X = 0;

    private static final int DATA_Y = 1;

    private static final int DATA_Z = 2;

    private static final int DELAY_TIME = 300;
    private boolean flag_flat;
    private TextView mTextView = null;
    private Timer mTimer;
    private Handler mHandler1 = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(!flag_flat){
                Toast.makeText(mContext,mContext.getResources().getString(R.string.a_sensor_calibration_fail_reason),Toast.LENGTH_LONG).show();
                storeRusult(false);
                //finish();
                mFailButton.setEnabled(true);
                mRetestButton.setVisibility(View.VISIBLE);
            }else {
                if(mTimer != null) {
                    mTimer.purge();
                    mTimer.cancel();
                }
                startSensorCalibration();
            }
        }
    };

    private Runnable mR = new Runnable() {
        public void run() {
            mDisplayText.setText(mContext.getResources().getString(
                    R.string.a_sensor_calibration_fail));
            mFailButton.setEnabled(true);
            mRetestButton.setVisibility(View.VISIBLE);

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sensor_a_calibration);
        mContext = this;
        mDisplayText = (TextView) findViewById(R.id.result_sensor_a);
        mTextView = (TextView) findViewById(R.id.txt_msg_gsensor);
        /*SPRD bug 760913:Test can pass/fail must click button*/
        //if(Const.isBoardISharkL210c10()){
            mPassButton.setVisibility(View.GONE);
            mFailButton.setEnabled(false);
        //}
        /*@}*/
        mRetestButton = (Button) findViewById(R.id.retest_btn);
        mRetestButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "mRetestButton");
                mRetestButton.setVisibility(View.GONE);
                mFailButton.setEnabled(false);
                mDisplayText.setText(mContext.getResources().getString(R.string.a_sensor_calibration_tips));
                //startSensorCalibration();
                updateData();
                mHandler1.sendEmptyMessageDelayed(0,3000);
            }
        });
        mSensorUtils = new SensorUtils(this, Sensor.TYPE_ACCELEROMETER);
        mSensorUtils.enableSensor();
        updateData();
    }
    private void updateData(){
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mSensorUtils != null) {
                    mValues = mSensorUtils.getmValues();
                    if (mValues != null) {
                        float x = mValues[DATA_X];
                        float y = mValues[DATA_Y];
                        float z = mValues[DATA_Z];
                        showMsg(x, y, z);
                        if (Math.abs(x) < 0.8 && Math.abs(y) < 0.8) {
                            flag_flat = true;
                        } else {
                            flag_flat = false;
                        }
                    }
                }
            }
        }, 0, 50);
    }
    private void showMsg(float x, float y, float z) {
        StringBuffer text = new StringBuffer("");
        text.append(" X = " + x + "\n");
        text.append(" Y = " + y + "\n");
        text.append(" Z = " + z + "\n");
        Message message = Message.obtain();
        message.obj = text.toString();
        message.what = SHOW_MSG;
        mHandler.sendMessage(message);
    }
    @Override
    protected void onResume() {
        super.onResume();
        //startSensorCalibration();
        mHandler1.sendEmptyMessageDelayed(0,3000);
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed" );
        if (true) {
            return;
        } else {
            super.onBackPressed();
        }
    }

    private void startSensorCalibration() {
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
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        /*@}*/
        if(flag_flat) {
            FileUtils.writeFile(Const.CALIBRATOR_CMD, SET_CMD);
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mHandler.sendMessage(mHandler.obtainMessage(SET_CMD_COMPLETE));
        }else {
            mHandler.sendMessage(mHandler.obtainMessage(CALIBRATION_FAIL));
        }

    }

    /**
     * start calibrating echo "2 [SENSOR_ID] 1" > calibrator_cmd cat calibrator_data, if the get
     * value is 2 ,the test is ok ,or test is fial
     **/
    private void getResult() {
        boolean isOK = false;
        FileUtils.writeFile(Const.CALIBRATOR_CMD, GET_RESULT);
        String getResult = FileUtils.readFile(Const.CALIBRATOR_DATA);
        saveResult = saveResult();
        Log.d(TAG, "getResult the result of boolen saveResult: " + saveResult);
        Log.d(TAG, "getResult the result of acceleration calibration: " + getResult);
        if (saveResult && getResult != null && TEST_OK.equals(getResult.trim())) {
            isOK = true;
        }
        Log.d(TAG, "getResult the result of isOK: " + isOK);
        if (isOK) {
            mHandler.sendMessage(mHandler.obtainMessage(CALIBRATION_SUCCESS));
        } else {
            mHandler.sendMessage(mHandler.obtainMessage(CALIBRATION_FAIL));
        }
    }

    /**
     * save the result echo "3 [SENSOR_ID] 1" > calibrator_cmd cat calibrator_data to save test
     * result
     **/
    private boolean saveResult() {
        Log.d(TAG, "saveResult...");
        FileUtils.writeFile(Const.CALIBRATOR_CMD, SAVE_RESULT);
        String saveResult = FileUtils.readFile(Const.CALIBRATOR_DATA);
        Log.d(TAG, "save result: " + saveResult);
        if (saveResult != null && PASS_NUMBER.equals(saveResult.trim())) {
            isOk = true;
            Log.d(TAG, "save result isOk: " + isOk);
        }
        return isOk;
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_MSG:
                    String text = (String) msg.obj;
                    if(mTextView != null){
                        mTextView.setText(text);
                    }
                    break;
                case SET_CMD_COMPLETE:
                    new Thread(new Runnable() {
                        public void run() {
                            getResult();
                        }
                    }).start();
                    break;
                case CALIBRATION_SUCCESS:
                    Toast.makeText(mContext, R.string.text_pass,
                            Toast.LENGTH_SHORT).show();
                    /*SPRD bug 760913:Test can pass/fail must click button*/
                    //if(Const.isBoardISharkL210c10()){
                        Log.d("", "isBoardISharkL210c10 is return!");
                        //mPassButton.setVisibility(View.VISIBLE);
                        //return;
                    //}
                    /*@}*/
                    storeRusult(true);
                    finish();
                    break;
                case CALIBRATION_FAIL:
                    mHandler.post(mR);
                    break;
                default:
            }
        }

    };

    @Override
    public void onDestroy() {
        mHandler.removeCallbacks(mR);
        if(mSensorUtils != null){
            mSensorUtils.disableSensor();
        }
        super.onDestroy();
    }
}
