
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
import android.widget.Button;

public class PsensorTestActivity extends BaseActivity {
    /** the value of change color */
    private static final float VALUE_OF_CHANGE_COLOR = 0.5f;

    /** the default value */
    private static final float PSENSOR_DEFAULT_VALUE = 1.0f;

    private static final String VALUE_FAR = "Distant";

    private static final String VALUE_CLOSE = "Closer";

    /** sensor manager object */
    private SensorManager pManager = null;

    /** sensor object */
    private Sensor pSensor = null;

    /** sensor listener object */
    private SensorEventListener pListener = null;

    /** the status of p-sensor */
    private TextView psensorTextView;

    /** the max value of progressBar */
    private static final int MAX_VALUE_PROGRESSBAR = 300;

    private Context mContext;

    private boolean mIsCloseDone = false;
    private boolean mIsDistantDone = false;

    private static final String TAG = "PsensorTestActivity";
    private Button mStartButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate ");
        mContext = this;
        setContentView(R.layout.sensor_proximity);
        setTitle(R.string.proximity_sensor_test);
        psensorTextView = (TextView) findViewById(R.id.txt_psensor);
        //initSensor();
        //setPsensorDisplay(VALUE_FAR, PSENSOR_DEFAULT_VALUE, Color.WHITE);

        TextView textTile = (TextView) findViewById(R.id.test_tile);
        if(textTile != null){
            textTile.setText(R.string.proximity_sensor_tips);
        }
        /*@}*/
        /*SPRD bug 760913:Test can pass/fail must click button*/
  //      if(Const.isBoardISharkL210c10()){
            mPassButton.setVisibility(View.GONE);
     //   }
        /*@}*/
        mStartButton = (Button) findViewById(R.id.start_btn);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "mStartButton");
                mStartButton.setVisibility(View.GONE);
                textTile.setText(R.string.tip_for_psensor_zte);
                psensorTextView.setVisibility(View.VISIBLE);
                setPsensorDisplay(VALUE_FAR, PSENSOR_DEFAULT_VALUE, Color.WHITE);
                psensorTextView.setBackgroundColor(Color.WHITE);
                initSensor();
                pManager.registerListener(pListener, pSensor, SensorManager.SENSOR_DELAY_UI);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume ");
        //setPsensorDisplay(VALUE_FAR, PSENSOR_DEFAULT_VALUE, Color.WHITE);
        //psensorTextView.setBackgroundColor(Color.WHITE);
        //pManager.registerListener(pListener, pSensor, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause ");
        if (pManager != null) {
            pManager.unregisterListener(pListener);
        }

        super.onPause();
    }

    private void setPsensorDisplay(String dis, float data, int color) {
        psensorTextView.setText("");
        if (pSensor != null) {
            psensorTextView.append("Chip id: " + pSensor.getName() + "\n");
        }

        psensorTextView.append(getString(R.string.psensor_msg_data) + " " + data + "\n");
        psensorTextView.append(getString(R.string.psensor_msg_value) + " " + dis);
        psensorTextView.setBackgroundColor(color);
    }

    private void initSensor() {
        pManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        pSensor = pManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        pListener = new SensorEventListener() {
            public void onAccuracyChanged(Sensor s, int accuracy) {
            }

            public void onSensorChanged(SensorEvent event) {
                float x = event.values[SensorManager.DATA_X];

                Log.d(TAG, "onSensorChanged x = "+x);
                if (x <= VALUE_OF_CHANGE_COLOR) {
                    setPsensorDisplay(VALUE_CLOSE, x, Color.GREEN);
                    mIsCloseDone = true;
                } else {
                    /*SPRD bug 744593:We check P sensor success only near to far.*/
                    Log.d(TAG, "onSensorChanged mIsCloseDone = "+mIsCloseDone);
                    if(mIsCloseDone){
                        setPsensorDisplay(VALUE_FAR, x, Color.WHITE);
                        mIsDistantDone = true;
                    }else{
                        Log.d(TAG, "Must be test near first!");
                    }
                    /*@}*/
                }

                /*SPRD bug 744593:We check P sensor success only near to far.*/
                Log.d(TAG, "onSensorChanged22 mIsCloseDone = "+mIsCloseDone+", mIsDistantDone = "+mIsDistantDone);

                if (mIsCloseDone && mIsDistantDone) {
                    Toast.makeText(mContext, R.string.text_pass, Toast.LENGTH_SHORT).show();
                    /*SPRD bug 760913:Test can pass/fail must click button*/
                    //if(Const.isBoardISharkL210c10()){
                        Log.d("", "isBoardISharkL210c10 is return!");
                        //mPassButton.setVisibility(View.VISIBLE);
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
