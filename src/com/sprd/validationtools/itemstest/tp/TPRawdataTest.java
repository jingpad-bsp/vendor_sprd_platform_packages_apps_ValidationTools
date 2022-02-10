
package com.sprd.validationtools.itemstest.tp;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.R;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import android.content.BroadcastReceiver;
import android.content.ComponentName;

import com.sprd.validationtools.utils.FileUtils;


public class TPRawdataTest extends BaseActivity {
    private static final String TAG = "TPRawdataTest";

    private static final String tpRawNodePath = "/sys/devices/platform/soc/soc:ap-apb/70600000.i2c/i2c-3/3-0010/elan_ktf/tp_module_test";
    private Context mContext;
    private Thread thread = null;
    private Button mStartButton;
    private Button mRetestButton;
    private static final int result_value = -1;

    private static final String TEST_RESULT = "testResult";
    private TextView mTPRawdataResult = null;
    private int second = 20;
    private boolean mTesting = false;

    private static final int TPRAW_TEST_START = 1;
    private static final int TPRAW_TEST_RUNNING = 2;
    private static final int TPRAW_TEST_FINISH = 3;
    private static final int TPRAW_TEST_PASS = 4;
    private static final int TPRAW_TEST_FAIL = 5;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case TPRAW_TEST_START:
                    startAndWaitForResponse();
                    break;

                case TPRAW_TEST_RUNNING:
                    second--;
                    setTestStatus();


                    if(second > 0 && mTesting) {
                        mHandler.sendMessageDelayed(mHandler.obtainMessage(TPRAW_TEST_RUNNING), 1000L);
                    } else if(mTesting) {
                        mHandler.sendMessage(mHandler.obtainMessage(TPRAW_TEST_FAIL));
                    }

                break;

                case TPRAW_TEST_FINISH:
                break;

                case TPRAW_TEST_PASS:
                    mTPRawdataResult.setText(R.string.text_pass);
                    mTPRawdataResult.setTextColor(Color.GREEN);
                    mPassButton.setVisibility(View.VISIBLE);
                break;

                case TPRAW_TEST_FAIL:
                    mTPRawdataResult.setText(R.string.text_fail);
                    mTPRawdataResult.setTextColor(Color.RED);
                    mRetestButton.setVisibility(View.VISIBLE);
                break;

            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.tp_rawdata_test);
        setTitle(R.string.tp_rawdata_info);
        mPassButton.setVisibility(View.GONE);

        mContext = this;

        mTPRawdataResult = (TextView) findViewById(R.id.result_tp_rawdata);
        mStartButton = (Button) findViewById(R.id.start_btn);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "mStartButton");
                mStartButton.setVisibility(View.GONE);
                startTPRawdataTest();
            }
        });

        mRetestButton = (Button) findViewById(R.id.retest_btn);
        mRetestButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "mRetestButton");
                mRetestButton.setVisibility(View.GONE);
                startTPRawdataTest();
            }
        });

        if(!FileUtils.fileIsExists(tpRawNodePath)){
            Log.d(TAG, "tpRawNodePath isn't exist!");
            mStartButton.setEnabled(false);
            mRetestButton.setEnabled(false);
            mTPRawdataResult.setText(R.string.text_fail);
            mTPRawdataResult.setTextColor(Color.RED);
        }
    }

    @Override
    protected void onResume() {
        Log.i(TAG, "onResume()");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.i(TAG, "onPause()");
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy()");
        super.onDestroy();
    }

    private void startTPRawdataTest() {
        Log.i(TAG, "startTPRawdataTest()");

        second = 20;
        setTestStatus();

        mHandler.sendMessage(mHandler.obtainMessage(TPRAW_TEST_START));
        mHandler.sendMessageDelayed(mHandler.obtainMessage(TPRAW_TEST_RUNNING), 1000L);
    }

    private void setTestStatus() {
        StringBuilder strBuild = new StringBuilder();
        strBuild.append(mContext.getString(R.string.tp_rawdata_testing));
        strBuild.append(" " + second);
        mTPRawdataResult.setText(strBuild.toString());
        mTPRawdataResult.setTextColor(Color.RED);
    }


    private void startAndWaitForResponse() {
        thread = new Thread() {
            public void run() {
                mTesting = true;
                Log.i(TAG, "startAndWaitforresponse111");                
                FileUtils.writeFile(tpRawNodePath, "1");
                Log.i(TAG, "startAndWaitforresponse222");                

                String result = FileUtils.readFile(tpRawNodePath);
                Log.i(TAG, "startAndWaitforresponse333 result: " + result);  
                Log.i(TAG, "startAndWaitforresponse333 result.indexOf: " + result.indexOf("PASS")); 

                if(result != null && result.indexOf("PASS") >= 0) {
                    mHandler.sendMessage(mHandler.obtainMessage(TPRAW_TEST_PASS));
                    mHandler.removeMessages(TPRAW_TEST_RUNNING);
                } else {
                    mHandler.sendMessage(mHandler.obtainMessage(TPRAW_TEST_FAIL));
                    mHandler.removeMessages(TPRAW_TEST_RUNNING);                    
                }
                mTesting = false;
                Log.i(TAG, "startAndWaitforresponse444");                
            }
        };

        thread.start();
    }
}
