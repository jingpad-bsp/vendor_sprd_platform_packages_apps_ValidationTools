package com.sprd.validationtools.itemstest.sensor;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class SensorUtils {

	private static final String TAG = "SensorUtils";
	private SensorManager mSensorManager = null;
	private Sensor mSensor = null;
	private Context mContext = null;
	private int mSensorType = -1;
	private SensorEventListener mSensorEventListener = null;
	private float[] mValues;
	private boolean mDxOk = false;
	private boolean mDyOk = false;
	private boolean mDzOk = false;

	public SensorUtils(Context context, int sensorType) {
		mSensorManager = (SensorManager) context
				.getSystemService(Context.SENSOR_SERVICE);
		mContext = context;
		mSensorType = sensorType;
		mSensor = mSensorManager.getDefaultSensor(mSensorType);
		mSensorEventListener = new SensorEventListener() {
			public void onAccuracyChanged(Sensor s, int accuracy) {
			}

			public void onSensorChanged(SensorEvent event) {
				if(mSensorType == Sensor.TYPE_ACCELEROMETER) {
					mValues = event.values;
					float x = event.values[SensorManager.DATA_X];
					float y = event.values[SensorManager.DATA_Y];
					float z = event.values[SensorManager.DATA_Z];
					//showMsg(x, y, z);
					double dx = Math.abs(9.8 - Math.abs(x));
					double dy = Math.abs(9.8 - Math.abs(y));
					double dz = Math.abs(9.8 - Math.abs(z));
					double ref = 9.8 * 0.08;
					if (!mDxOk)
						mDxOk = dx < ref;
					if (!mDyOk)
						mDyOk = dy < ref;
					if (!mDzOk)
						mDzOk = dz < ref;
				}
			}
		};
	}

	public boolean enableSensor() {
		if (mSensorManager == null) {
			mSensorManager = (SensorManager) mContext
					.getSystemService(Context.SENSOR_SERVICE);
		}
		Log.d(TAG, "enableSensor mSensorType=" + mSensorType);
		if (mSensorManager != null) {
			boolean ret = mSensorManager.registerListener(mSensorEventListener,
					mSensor, SensorManager.SENSOR_DELAY_UI);
			return ret;
		}
		return false;
	}

	public void disableSensor() {
		if (mSensorManager == null) {
			mSensorManager = (SensorManager) mContext
					.getSystemService(Context.SENSOR_SERVICE);
		}
		Log.d(TAG, "enableSensor mSensorType=" + mSensorType);
		if (mSensorManager != null) {
			mSensorManager.unregisterListener(mSensorEventListener);
		}
	}

	public float[] getmValues(){
		return mValues;
	}
}
