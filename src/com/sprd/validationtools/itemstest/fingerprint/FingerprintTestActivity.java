package com.sprd.validationtools.itemstest.fingerprint;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.R;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sprd.validationtools.itemstest.fingerprint.FingerprintTestImpl;

public class FingerprintTestActivity extends BaseActivity {

    private static final String TAG = "FingerprintTestActivity";
    private Context mContext;
    private TextView mFingerDetectTips;
    private TextView mFingerDetectResult;
    private Button mFingerDetect;
    private static final int RESULT_OK = 0;
    private static final int INIT_SUCCESS = 1;
    private static final int CALIBRATION_FAIL = 2;
    private static final int TEST_SUCCESS= 3;
    private static final int TEST_FAIL = 4;
    private static final int BAD_POINT_TEST_START = 5;
    private static final int BAD_POINT_TEST_END = 6;
    IFactoryTestImpl theTestImpl = null;
    private Button mRetestButton;
    private boolean isfail = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.finger_print);
        Log.d(TAG, "onCreate");

        mContext = this;

        mFingerDetectTips = (TextView) findViewById(R.id.finger_detect_tips);
        mFingerDetectResult = (TextView) findViewById(R.id.result);
        mFingerDetect = (Button) findViewById(R.id.finger_detect_button);
        mFingerDetect.setVisibility(View.GONE);
        /*SPRD bug 760913:Test can pass/fail must click button*/
        /*if(!Const.isBoardISharkL210c10()){
            removeButton();
        }*/
        /*@}*/
        /*SPRD bug 760913:Test can pass/fail must click button*/
        //if(Const.isBoardISharkL210c10()){
            mPassButton.setVisibility(View.GONE);
        //}
        /*@}*/
        isfail = false;
        mRetestButton = (Button) findViewById(R.id.retest_btn);
        mRetestButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "mRetestButton");
                int ret =0;
                try {
                    ret = theTestImpl.factory_exit();
                } catch (UnsatisfiedLinkError e) {
                    e.printStackTrace();
                }
                Log.d(TAG,"mRetestButton deinit fd ret = " + ret);
                mRetestButton.setVisibility(View.GONE);
                mFingerDetectResult.setText("");
                initSensor();
            }
        });
        initSensor();
    }

    private void initSensor() {
        new Thread(new Runnable(){
            @Override
            public void run() {
                /* SPRD bug 753816 : Add all test item to 10c10 */
                try {
                    theTestImpl = new FingerprintTestImpl();
                    int ret = theTestImpl.factory_init();
                    Log.d(TAG, "factory_init = " + ret);
                    if (0 == ret) {
                        /*SPRD bug 773174:Change init dislpay*/
                        mHandler.sendMessage(mHandler
                                .obtainMessage(INIT_SUCCESS));
                        ret = theTestImpl.spi_test();
                        Log.d(TAG, "spi_test = " + ret);
                        //Test fail while one step fail.
                        if(ret == -1){
                            Log.e(TAG, "spi_test fail!!");
                            mHandler.sendMessage(mHandler
                                    .obtainMessage(CALIBRATION_FAIL));
                            //mHandler.sendEmptyMessageDelayed(TEST_FAIL, 1000);
                            return;
                        }

                        ret = theTestImpl.interrupt_test();
                        Log.d(TAG, "interrupt_test = " + ret);
                        if(ret == -1){
                            Log.e(TAG, "interrupt_test fail!!");
                            mHandler.sendMessage(mHandler
                                    .obtainMessage(CALIBRATION_FAIL));
                            //mHandler.sendEmptyMessageDelayed(TEST_FAIL, 1000);
                            return;
                        }
                        mHandler.sendMessage(mHandler.obtainMessage(BAD_POINT_TEST_START));
                        ret = theTestImpl.deadpixel_test();
                        Log.d(TAG, "deadpixel_test = " + ret);
                        if(ret == -1){
                            Log.e(TAG, "deadpixel_test fail!!");
                            mHandler.sendMessage(mHandler
                                    .obtainMessage(CALIBRATION_FAIL));
                            //mHandler.sendEmptyMessageDelayed(TEST_FAIL, 1000);
                            return;
                        }
                        mHandler.sendMessage(mHandler.obtainMessage(BAD_POINT_TEST_END));
                        mHandler.sendEmptyMessageDelayed(TEST_FAIL, 15000);
                        //Change finger_detect step
                        theTestImpl.finger_detect(listener_fd);
                    } else {
                        mHandler.sendMessage(mHandler
                                .obtainMessage(CALIBRATION_FAIL));
                        //mHandler.sendEmptyMessageDelayed(TEST_FAIL, 1000);
                    }
                } catch (UnsatisfiedLinkError e) {
                    e.printStackTrace();
                    mHandler.sendEmptyMessageDelayed(TEST_FAIL, 1000);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                    mHandler.sendEmptyMessageDelayed(TEST_FAIL, 1000);
                }
                /* @} */
            }
        }).start();
    }


    ISprdFingerDetectListener listener_fd = new ISprdFingerDetectListener(){

        @Override
        public void on_finger_detected(int status) {
            Log.d(TAG,"ISprdFingerDetectListener fd ret = " + status);
            if (status == RESULT_OK) {
                mHandler.removeMessages(TEST_FAIL);
                mHandler.sendMessage(mHandler.obtainMessage(TEST_SUCCESS));
            }else{
                mHandler.removeMessages(TEST_FAIL);
                //mHandler.sendMessage(mHandler
                //        .obtainMessage(CALIBRATION_FAIL));
                if(!isfail) {
                    mHandler.sendEmptyMessageDelayed(TEST_FAIL, 1000);
                }
                isfail = false;
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeMessages(TEST_FAIL);
        Log.d(TAG, "onPause, deInit sensor");
    }

    @Override
    protected void onDestroy() {
        int ret =0;
        try {
            ret = theTestImpl.factory_exit();
        } catch (UnsatisfiedLinkError e) {
            e.printStackTrace();
        }
        Log.d(TAG,"onDestroy deinit fd ret = " + ret);
        super.onDestroy();
    }

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case INIT_SUCCESS:
                    Log.d(TAG, "INIT_SUCCESS");
                    mFingerDetectTips.setText(mContext.getResources().getString(
                            R.string.finger_print_tips));
                    break;
                case CALIBRATION_FAIL:
                    Log.d(TAG, "CALIBRATION_FAIL");
                    mFingerDetectTips.setText(mContext.getResources().getString(
                            R.string.finger_print_init_fail));
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "CALIBRATION_FAIL run");
                            mRetestButton.setVisibility(View.VISIBLE);
                        }
                    }, 2000);
                    isfail = true;
                    break;
                case TEST_SUCCESS:
                    mFingerDetectResult.setText(mContext.getResources().getString(
                            R.string.have_a_finger));
                    Toast.makeText(FingerprintTestActivity.this, R.string.text_pass,
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
                case TEST_FAIL:
                    Log.d(TAG, "TEST_FAIL");
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "CALIBRATION_FAIL run");
                            mFingerDetectResult.setText(mContext.getResources().getString(
                                     R.string.no_finger));
                            Toast.makeText(FingerprintTestActivity.this, R.string.text_fail,
                                     Toast.LENGTH_SHORT).show();
                            mRetestButton.setVisibility(View.VISIBLE);
                        }
                    }, 5000);
                    isfail = true;
                    /*SPRD bug 760913:Test can pass/fail must click button*/
                    /*if(Const.isBoardISharkL210c10()){
                        Log.d("", "isBoardISharkL210c10 is return!");
                        return;
                    }*/
                    /*@}*/
                    //storeRusult(false);
                    //finish();
                    break;
                case BAD_POINT_TEST_START:
                    Log.d(TAG, "BAD_POINT_TEST_START");
                    mFingerDetectTips.setText(mContext.getResources().getString(
                            R.string.bad_point_test_tips));
                    break;
                case BAD_POINT_TEST_END:
                    Log.d(TAG, "BAD_POINT_TEST_END");
                    mFingerDetectTips.setText(mContext.getResources().getString(
                            R.string.bad_point_test_end_tips));
                    break;
                default:
            }
        }
    };
}
