
package com.sprd.validationtools.itemstest.otg;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.ActionBar.LayoutParams;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.os.EnvironmentEx;
import android.os.Handler;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sprd.validationtools.Const;
import com.sprd.validationtools.utils.StorageUtil;
import com.sprd.validationtools.R;
import android.widget.Toast;
import android.os.Message;
import android.view.InputDevice;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import android.widget.Button;
import com.sprd.validationtools.BaseActivity;

public class OTGTest extends BaseActivity {
    private String TAG = "OTGTest";
    private TextView mTextView, mCountDownView, mCapacityView;
    private StorageManager mStorageManager = null;
    private boolean isUsb = false;
    private String usbMassStoragePath = "/storage/usbdisk";
    private static final String SPRD_OTG_TESTFILE = "otgtest.txt";
    private String otgPath = null;
    public byte mOTGTestFlag[] = new byte[1];
    byte[] result = new byte[1];
    byte[] mounted = new byte[1];
    private static final int UPDATE_TIME = 0;
    private long time = 20;
    private Context mContext;
    private Button mRetestButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.otg_test);
        mContext = this;
        mTextView = (TextView) findViewById(R.id.result_otg);
        mCountDownView = (TextView) findViewById(R.id.count_view);
        mCapacityView = (TextView) findViewById(R.id.capacity_otg);
        setTitle(R.string.otg_test);
        mTextView.setText(getResources().getText(R.string.otg_begin_test));
        mCountDownView.setText(time + "");
        //mHandler.sendEmptyMessageDelayed(UPDATE_TIME, 1000);
        /*SPRD bug 760913:Test can pass/fail must click button*/
        if(true){
            mPassButton.setVisibility(View.GONE);
        }
        /*@}*/
        startOtgTest();
        mRetestButton = (Button) findViewById(R.id.retest_btn);
        mRetestButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "mRetestButton");
                mHandler.removeCallbacks(mCheckRunnable);
                mHandler.removeMessages(UPDATE_TIME);
                mRetestButton.setVisibility(View.GONE);
                startOtgTest();
            }
        });
    }

    private void checkOTGdevices() {

      final int[] devices = InputDevice.getDeviceIds();
      for (int i = 0; i < devices.length; i++) {
          InputDevice device = InputDevice.getDevice(devices[i]);
          if (device.getSources() == InputDevice.SOURCE_MOUSE) {
              mounted[0] = 0;
              Log.i(TAG, "=== SOURCE_MOUSE OTG mount succeed ===");
          } else {
              mounted[0] = 1;
              Log.i(TAG, "=== else SOURCE_MOUSE OTG mount Fail ===");
          }
      }

      if(mounted[0] == 1){
          String otgPath = StorageUtil.getExternalStorageAppPath(getApplicationContext(), Const.OTG_UDISK_PATH);
          if (otgPath != null) {
              mounted[0] = 0;
              Log.i(TAG, "=== otgPath != null OTG mount succeed ===");
              usbMassStoragePath = otgPath;
			
			int capacity = StorageUtil.getOTGcapacity(getApplicationContext(), otgPath);			
			StringBuilder sb = new StringBuilder();
    		//sb.append("Udisk Capacity: ").append(total);//.append("     Used: ").append(used);

			sb.append("Udisk Capacity: ").append(capacity).append(" GB");
			mCapacityView.setText(sb);
			mCapacityView.setVisibility(View.VISIBLE);
          } else {
              mounted[0] = 1;
              Log.i(TAG, "=== otgPath == null OTG mount Fail ===");
          }
      } 

    }

    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
            case UPDATE_TIME:
                time--;
                mCountDownView.setText(time + "");
                if(time == 0) {
                    Log.d(TAG, "time out");
                    if (mounted[0] == 0) {
                        if (result[0] == 0) {
                            mTextView.setText(getResources().getText(R.string.otg_test_success));
                            /*SPRD bug 760913:Test can pass/fail must click button*/
                            if(true){
                                Log.d(TAG, "UPDATE_TIME mRunnable mounted[0] == 0 result[0] == 0 isBoardISharkL210c10 is return!");
                                mPassButton.setVisibility(View.VISIBLE);
                                mRetestButton.setVisibility(View.GONE);
                                return;
                            }
                            /*@}*/
                            //storeRusult(true);
                            //finish();
                        } else {
                            Toast.makeText(OTGTest.this, R.string.text_fail,
                            Toast.LENGTH_SHORT).show();
                            mTextView.setText(getResources().getText(R.string.otg_test_fail));
                            mRetestButton.setVisibility(View.VISIBLE);
                            /*SPRD bug 760913:Test can pass/fail must click button*/
                            if(true){
                                Log.d(TAG, "UPDATE_TIME mRunnable mounted[0] == 0 else result[0] == 0isBoardISharkL210c10 is return!");
                                return;
                            }
                            /*@}*/
                            //storeRusult(false);
                            //finish();
                        }
                    } else {
                        Toast.makeText(OTGTest.this, R.string.text_fail,
                            Toast.LENGTH_SHORT).show();
                        mTextView.setText(getResources().getText(R.string.otg_test_fail));
                        mRetestButton.setVisibility(View.VISIBLE);
                        /*SPRD bug 760913:Test can pass/fail must click button*/
                        if(true){
                            Log.d(TAG, "UPDATE_TIME else mRunnable mounted[0] == 0 result[0] == 0 isBoardISharkL210c10 is return!");
                            return;
                        }
                        /*@}*/
                        //storeRusult(false);
                        //finish();
                    }

                } else {
                    mHandler.sendEmptyMessageDelayed(UPDATE_TIME, 1000);
                }
                break;
            }
            super.handleMessage(msg);
        }
    };

    public Runnable mRunnable = new Runnable() {
        public void run() {
            Log.i(TAG, "=== display OTG test succeed info! ===");
            if (mounted[0] == 0) {
                if (result[0] == 0) {
                    mTextView.setText(getResources().getText(R.string.otg_test_success));
                    /*SPRD bug 760913:Test can pass/fail must click button*/
                    if(true){
                        Log.d(TAG, "mRunnable mounted[0] == 0 result[0] == 0 isBoardISharkL210c10 is return!");
                        mPassButton.setVisibility(View.VISIBLE);
                        mRetestButton.setVisibility(View.GONE);
                        return;
                    }
                    /*@}*/
                    //storeRusult(true);
                    //finish();
                } else {
                    mTextView.setText(getResources().getText(R.string.otg_test_fail));
                    mRetestButton.setVisibility(View.VISIBLE);
                    /*SPRD bug 760913:Test can pass/fail must click button*/
                    if(true){
                        Log.d(TAG, "mRunnable mounted[0] == 0 else result[0] == 0isBoardISharkL210c10 is return!");
                        return;
                    }
                    /*@}*/
                    //storeRusult(false);
                    //finish();
                }
            } else {
                mTextView.setText(getResources().getText(R.string.otg_test_fail));
                mRetestButton.setVisibility(View.VISIBLE);
                /*SPRD bug 760913:Test can pass/fail must click button*/
                if(true){
                    Log.d(TAG, "else mRunnable mounted[0] == 0 result[0] == 0 isBoardISharkL210c10 is return!");
                    return;
                }
                /*@}*/
                //storeRusult(false);
                //finish();
            }
        }
    };
    public Runnable mCheckRunnable = new Runnable() {
        public void run() {
            Log.i(TAG, "=== checkOTGdevices! ===");
            checkOTGdevices();
            if (mounted[0] != 0) {
                mTextView.setText(getResources().getText(R.string.otg_no_devices));
                mHandler.postDelayed(mCheckRunnable, 1000);
            } else {
                startVtThread();
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");
        //startOtgTest();
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause()");
        //mHandler.removeCallbacks(mCheckRunnable);
        //mHandler.removeMessages(UPDATE_TIME);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        mHandler.removeCallbacks(mCheckRunnable);
        mHandler.removeMessages(UPDATE_TIME);
        super.onDestroy();
    }

    private void startOtgTest() {
        Log.i(TAG, "startOtgTest()");
        mounted[0] = 1;
        result[0] = 1;
        time = 20;
        mTextView.setText(getResources().getText(R.string.otg_begin_test));
        mHandler.sendEmptyMessageDelayed(UPDATE_TIME, 1000);
        checkOTGdevices();
        if (mounted[0] != 0) {
            mTextView.setText(getResources().getText(R.string.otg_no_devices));
            mHandler.postDelayed(mCheckRunnable, 1000);
        } else {
            startVtThread();
        }
    }

    private void startVtThread() {
        Log.i(TAG,
                "=== create thread to execute OTG test command! ===");
        /* SPRD Bug 766333: OTG test should use mouse not USB. @{ */
        if (true) {
            result[0] = 0;
            mHandler.post(mRunnable);
        } else {
            Thread vtThread = new Thread() {
                public void run() {
                    FileInputStream in = null;
                    FileOutputStream out = null;
                    try {
                        if (mounted[0] == 0) {
                            File fp = new File(usbMassStoragePath, SPRD_OTG_TESTFILE);
                            if (fp.exists())
                                fp.delete();
                            fp.createNewFile();
                            out = new FileOutputStream(fp);
                            mOTGTestFlag[0] = '7';
                            out.write(mOTGTestFlag, 0, 1);
                            out.close();
                            in = new FileInputStream(fp);
                            in.read(mOTGTestFlag, 0, 1);
                            in.close();
                            if (mOTGTestFlag[0] == '7') {
                                result[0] = 0;
                            } else {
                                result[0] = 1;
                            }
                        }
                        //mHandler.post(mRunnable);
                        mHandler.postDelayed(mRunnable, 2000);
                    } catch (Exception e) {
                        Log.i(TAG, "=== error: Exception happens when OTG I/O! ===");
                        e.printStackTrace();
                        try {
                            if (out != null) {
                                out.close();
                                out = null;
                            }
                            if (in != null) {
                                in.close();
                                in = null;
                            }
                        } catch (IOException io) {
                            Log.e(TAG, "close in/out err");
                        }
                    } finally {
                        try {
                            if (out != null) {
                                out.close();
                                out = null;
                            }
                            if (in != null) {
                                in.close();
                                in = null;
                            }
                        } catch (IOException io) {
                            Log.e(TAG, "close in/out err");
                        }
                    }
                }
            };
            vtThread.start();
        }
        /* @} */
    }
}
