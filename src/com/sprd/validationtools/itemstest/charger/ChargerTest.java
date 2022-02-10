
package com.sprd.validationtools.itemstest.charger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TableRow;
import android.widget.TextView;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.PhaseCheckParse;
import com.sprd.validationtools.R;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ChargerTest extends BaseActivity {

    private static final String TAG = "ChargerTest";
    private static final String INPUT_ELECTRONIC = "/sys/class/power_supply/battery/real_time_current";
    private static final String CHARGER_ELECTRONIC = "/sys/class/power_supply/sprdfgu/fgu_current";
    private static final String ENG_CHARGER_VOL = "/sys/class/power_supply/battery/charger_voltage";
    private static final String ENG_BATTERY_TVOL = "/sys/class/power_supply/battery/real_time_voltage";
    /* SPRD Bug 773805:Show NTC temperature in charger test. @{ */
    private static final String ENG_CHARGER_TEMP = "sys/class/power_supply/battery/temp";
    /* @} */
    private static final String ENG_FAST_CHARGE_TYPE = "/sys/class/power_supply/sc2730_fast_charger/type";
    /*SPRD bug 850012:Read battery id*/
    private static final String BATTERY_FULL_CAPACITY = "/sys/class/power_supply/battery/charge_full";
    private String mBatteryTypeId = "";
    private TableRow mBatteryTypeIdTableRow = null;
    private TextView mBatteryTypeIdTextView= null;
    private static final String ID_BYD = "04";
    private static final String ID_BAK = "17";
    private static final String FD_BATTERY_ID = "sys/class/power_supply/battery/bat_id";
    private static final String USB_TYPEC_CC_JUDGE = "/sys/class/typec/port0/typec_cc_polarity_role";
    /*@}*/

    private static final String STATUS = "status";
    private static final String PLUGGED = "plugged";
    private static final String VOLTAGE = "voltage";
    private static final String EXT_CHARGE_IC = "ext charge ic";
    private static final String TEST_RESULT_SUCCESS = "success";
    private static final String TEST_RESULT_FAIL = "fail";

    private TextView statusTextView, pluggedTextView, voltageTextView, mElectronicTextView,mQuickChargeTextView,mBatteryLevelTextView,mBatteryChargingStatusTextView,
            mBatteryTextView, mTestResultTextView, mTemperatureTextView, mTypecForwardTextView, mTypecReverseTextView, fullcapacityTextView;
    private PhaseCheckParse mParse = null;
    private String mPluggeString = null;
    private boolean mIsPlugUSB = false;
    private float mChargerElectronic;
    private float mChargerVoltage;
    private float mFullCapacity;
    private boolean mIsUsbForward = false;
    private boolean mIsUsbReverse = false;
    private boolean mIsFirstTime = true;
    private boolean mIsQuickCharge = false;
    /* SPRD Bug 773805:Show NTC temperature in charger test. @{ */
    private float mNtcTemperature;
    private TableRow mTemperatureTableRow, mElectronicTableRow, mBatteryTableRow, mQuickChargeTableRow,mBatteryChargingStatusTableRow,mBatteryLevelTableRow;

    /** Replace real_time_current*/
    private static final String ENG_CHARGER_CURRENT_K414 = "/sys/class/power_supply/battery/current_now";
    /** Replace fgu_current*/
    private static final String ENG_CHARGER_FGU_CURRENT_K414 = "/sys/class/power_supply/sc27xx-fgu/current_now";
    /** Replace charger_voltage*/
    private static final String ENG_CHARGER_VOLTAGE_K414 = "/sys/class/power_supply/sc27xx-fgu/constant_charge_voltage";
    
    private boolean mIsSupportK414 = false;

    private void initSupportK414(){
        File file = new File(ENG_CHARGER_VOLTAGE_K414);
        Log.d(TAG, "initSupportK414 file="+file + ",exists="+file.exists());
        if(file != null && file.exists()){
            mIsSupportK414 = true;
        }else{
            mIsSupportK414 = false; 
        }
    }
    
    private boolean isSupportK414(){
        Log.d(TAG, "isSupportK414 mIsSupportK414="+mIsSupportK414);
        return mIsSupportK414;
    }

    private Runnable mRealtimeShow = new Runnable() {
        public void run() {
            mHandler.removeCallbacks(this);
            initView();
            mHandler.postDelayed(this, 1000);
        }
    };
    /* @} */

    private String mInputCurrent = null;
    private int mRetryNum = 0;
    private int mWaitTime = 1000;

    private Handler mHandler;

    private Runnable mElectronicUpdate = new Runnable() {
        public void run() {

            try {
                if(Const.isBoardISharkL210c10()){
                    mBatteryTypeId = readFile(FD_BATTERY_ID).trim();
                    Log.d(TAG, "mBatteryTypeId="+mBatteryTypeId);
                    if(mBatteryTypeIdTextView != null){
                        if(!TextUtils.isEmpty(mBatteryTypeId)){
                            String text = getString(R.string.battery_type_id);
                            Log.d(TAG, "text ="+ text);
                            mBatteryTypeIdTextView.setText(text + (mBatteryTypeId.equals("0") ? ID_BYD : ID_BAK));
                        }
                    }
                }
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
            String testResult = getInputElectronic();
          //  String testResult = getInputElectronicNewStep();
            Log.d(TAG, "mElectronicUpdate data,mChargerVoltage=" + mChargerVoltage
                    + ", mChargerElectronic=" + mChargerElectronic
                    + ", mNtcTemperature=" + mNtcTemperature
                    + ",testResult:" + testResult);

            if (TEST_RESULT_SUCCESS.equals(testResult)) {

                if (mIsPlugUSB) {
                    mTestResultTextView.setText(getString(R.string.charger_test_success));
                    mTestResultTextView.setTextColor(Color.GREEN);
                    /*SPRD bug 760913:Test can pass/fail must click button*/
                   // if(Const.isBoardISharkL210c10()){
                      //  mPassButton.setVisibility(View.VISIBLE);
                    //    return;
                  //  }
                    /*@}*/
                    storeRusult(true);
                    //mHandler.postDelayed(mCompleteTest, 500);
                    if(mPassButton != null) {
                        mPassButton.setVisibility(View.VISIBLE);
                        mPassButton.setBackgroundColor(Color.GREEN);
                    }
                } else {
                    mHandler.postDelayed(mElectronicUpdate, 1000);
                }

            } else {

                if (mIsPlugUSB) {
                    mRetryNum++;
                    if (mRetryNum <= 5) {
                        mWaitTime = 500 * mRetryNum;

                        mHandler.post(mElectronicUpdate);
                        Log.d(TAG, "retry test num:" + mRetryNum + ",wait time is " + mWaitTime);
                    } else {
                        mTestResultTextView.setText(getString(R.string.charger_test_fail));
                        mTestResultTextView.setTextColor(Color.RED);
                        storeRusult(false);
                        //mHandler.postDelayed(mCompleteTest, 500);
                    }
                } else {
                    mHandler.postDelayed(mElectronicUpdate, 1000);
                }
            }
        }
    };

    private Runnable mCompleteTest = new Runnable() {
        public void run() {
            /*SPRD bug 760913:Test can pass/fail must click button*/
            if(Const.isBoardISharkL210c10()){
                Log.d("", "isBoardISharkL210c10 is return!");
                return;
            }
            /*@}*/
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setTitle(R.string.battery_title_text);
        setContentView(R.layout.battery_charged_result);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        mParse = PhaseCheckParse.getInstance();
        mHandler = new Handler();
        //Update kernel 4.14
        initSupportK414();
        statusTextView = (TextView) findViewById(R.id.statusTextView);
        pluggedTextView = (TextView) findViewById(R.id.pluggedTextView);
        voltageTextView = (TextView) findViewById(R.id.voltageTextView);
        fullcapacityTextView = (TextView) findViewById(R.id.fullcapacityTextView);
        mElectronicTextView = (TextView) findViewById(R.id.electronicTextView);
        mElectronicTableRow = (TableRow) findViewById(R.id.TableRow04);

        mQuickChargeTextView = (TextView) findViewById(R.id.isQuickChargeTextView);
        mQuickChargeTableRow = (TableRow) findViewById(R.id.TableRow11);
        mQuickChargeTextView.setVisibility(View.GONE);
        mQuickChargeTableRow.setVisibility(View.GONE);

        mBatteryLevelTextView = (TextView) findViewById(R.id.battery_level);
        mBatteryLevelTableRow = (TableRow) findViewById(R.id.TableRow12);

        mBatteryChargingStatusTextView = (TextView) findViewById(R.id.battery_charging_status);
        mBatteryChargingStatusTableRow = (TableRow) findViewById(R.id.TableRow13);

        mBatteryTextView = (TextView) findViewById(R.id.batteryelectronicTextView);
        mBatteryTableRow = (TableRow) findViewById(R.id.TableRow05);

        mTestResultTextView = (TextView) findViewById(R.id.test_resultTextView);
        /* SPRD Bug 773805:Show NTC temperature in charger test. @{ */
        mTemperatureTextView = (TextView) findViewById(R.id.batterytemperatureTextView);
        mTemperatureTableRow = (TableRow) findViewById(R.id.TableRow06);

        /* @} */
        /*SPRD bug 850012:Read battery id*/
        mBatteryTypeIdTextView = (TextView) findViewById(R.id.battery_type_id);
        mBatteryTypeIdTableRow = (TableRow) findViewById(R.id.TableRow08);
        /*@}*/

        mTypecForwardTextView= (TextView) findViewById(R.id.typec_forward_charging_text);
        mTypecReverseTextView= (TextView) findViewById(R.id.typec_backward_charging_text);

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mBroadcastReceiver, filter);
        /*SPRD bug 760913:Test can pass/fail must click button*/
        //if(Const.isBoardISharkL210c10()){
            mPassButton.setVisibility(View.GONE);
       // }
        /*@}*/
        //IntentFilter itf = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        //registerReceiver(mBatteryReceiver, itf);
    }

    private void showView() {
        mElectronicTableRow.setVisibility(View.VISIBLE);
        mBatteryTableRow.setVisibility(View.VISIBLE);

        //if(Const.isBoardISharkL210c10()){
            mTemperatureTableRow.setVisibility(View.VISIBLE);
        //}

        if(Const.isBoardISharkL210c10()){
            mBatteryTypeIdTableRow.setVisibility(View.VISIBLE);
        }
        //mQuickChargeTableRow.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        //mHandler.postDelayed(mElectronicUpdate, 500);
        IntentFilter itf = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mBatteryReceiver, itf);
    }

    private BroadcastReceiver mBatteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra("level", 0);
            Log.i(TAG,"level = " + level);
            if(level >= 55){
                mBatteryChargingStatusTextView.setText("good");
            }else {
                mBatteryChargingStatusTextView.setText("normal");
            }
            mBatteryLevelTextView.setText(level + "");
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastReceiver);

        mHandler.removeCallbacks(mElectronicUpdate);
        mHandler.removeCallbacks(mRealtimeShow);
    }

    private String getInputElectronic() {
        float batteryElectronic = (float) -1.0;
        String result = "";
        if(isSupportK414()){
            mInputCurrent = readFile(ENG_CHARGER_CURRENT_K414).trim();
        }else{
            mInputCurrent = readFile(INPUT_ELECTRONIC).trim();  
        }
        if (mIsPlugUSB && "discharging".equals(mInputCurrent)) {
            startCharge();
            if(isSupportK414()){
                mInputCurrent = readFile(ENG_CHARGER_CURRENT_K414).trim();
            }else{
                mInputCurrent = readFile(INPUT_ELECTRONIC).trim();  
            }
        }
        Log.d(TAG, "inputCurrent[" + mInputCurrent + "]");
        try {
            if (Const.isBoardISharkL210c10()) {
                mHandler.postDelayed(mRealtimeShow, 100);
            } else {
                initView();
            }

            if (EXT_CHARGE_IC.equals(mInputCurrent)) {
                int c1 = -1000;
                if(isSupportK414()){
                    c1 = Integer.parseInt(readFile(ENG_CHARGER_FGU_CURRENT_K414).trim());
                }else{
                    c1 = Integer.parseInt(readFile(CHARGER_ELECTRONIC).trim()); 
                }
                Log.d(TAG, "inputCurrent c1=[" + c1 + "]");
                //int c1 = Integer.parseInt(readFile(CHARGER_ELECTRONIC).trim());
                if (c1 > -40) {
                    return TEST_RESULT_SUCCESS;
                }
                stopCharge();
                Thread.sleep(1000);
                //int c2 = Integer.parseInt(readFile(CHARGER_ELECTRONIC).trim());
                int c2 = -1000;
                if(isSupportK414()){
                    c2 = Integer.parseInt(readFile(ENG_CHARGER_FGU_CURRENT_K414).trim());
                }else{
                    c2 = Integer.parseInt(readFile(CHARGER_ELECTRONIC).trim()); 
                }
                startCharge();
                Thread.sleep(mWaitTime);
                //int c3 = Integer.parseInt(readFile(CHARGER_ELECTRONIC).trim());
                int c3 = -1000;
                if(isSupportK414()){
                    c3 = Integer.parseInt(readFile(ENG_CHARGER_FGU_CURRENT_K414).trim());
                }else{
                    c3 = Integer.parseInt(readFile(CHARGER_ELECTRONIC).trim()); 
                }
                int i = c1 - c2;
                int i1 = c3 - c2;
                if (c1 > -40 || c3 > -40 || (i > 300 && i1 > 300)) {
                    result = TEST_RESULT_SUCCESS;
                } else {
                    result = TEST_RESULT_FAIL;
                }
				if(c3>0){
					mBatteryTextView.setText(c3 + " ma");
				}
                Log.d(TAG, "getInputElectronic() c1:" + c1 + " c2:" + c2 + " c3:" + c3 + " i:" + i
                        + " i1:" + i1);
            } else {
                if (!isNum(mInputCurrent)) {
                    Log.d(TAG, "get values isn`t number.");
                    return mInputCurrent;
                }

                if (mChargerElectronic > 200) {
                    result = TEST_RESULT_SUCCESS;
                } else {
                    if (mChargerVoltage > 5500) {
                        mQuickChargeTextView.setTextColor(Color.WHITE);
                        mQuickChargeTextView.setText(getString(R.string.yes));
                        result = TEST_RESULT_SUCCESS;
                    } else {
                        mQuickChargeTextView.setTextColor(Color.RED);
                        mQuickChargeTextView.setText(getString(R.string.no));
                        result = TEST_RESULT_FAIL;
                    }
                }

            }
        } catch (Exception e) {
            Log.w(TAG, "getInputElectronic fail", e);
        }
        return result;
    }

    private String getInputElectronicNewStep() {
        float batteryElectronic = (float) -1.0;
        String result = "";
        String inputCurrent = "";
        Log.d(TAG, "getInputElectronicNewStep inputCurrent[" + inputCurrent + "]");
        try {
            if (Const.isBoardISharkL210c10()) {
                mHandler.postDelayed(mRealtimeShow, 100);
            } else {
                initView();
            }
            //step1.Stop charge, read  current
            stopCharge();
            Thread.sleep(2000);
            int c1 = 0;
            if(isSupportK414()){
                c1 = Integer.parseInt(readFile(ENG_CHARGER_FGU_CURRENT_K414).trim()) / 1000;
            }else{
                c1 = Integer.parseInt(readFile(CHARGER_ELECTRONIC).trim());
            }
            Log.d(TAG, "getInputElectronicNewStep inputCurrent c1=[" + c1 + "]");
			if(c1>0){
				mBatteryTextView.setText(c1 + " ma");
			}
            //step2.Start charge, read charging current
            startCharge();
            Thread.sleep(2000);
            int c2 = 0;
            if(isSupportK414()){
                c2 = Integer.parseInt(readFile(ENG_CHARGER_FGU_CURRENT_K414).trim())  / 1000;
            }else{
                c2 = Integer.parseInt(readFile(CHARGER_ELECTRONIC).trim());
            }
            Log.d(TAG, "getInputElectronicNewStep inputCurrent c2=[" + c2 + "]");
            int i1 = c2 - c1;
            Log.d(TAG, "getInputElectronicNewStep inputCurrent i1=[" + i1 + "]");
            Log.d(TAG, "getInputElectronicNewStep inputCurrent mChargerVoltage=[" + mChargerVoltage + "]");
            //i1 >= 200mA PASS
            if (i1 >= 500) {
                result = TEST_RESULT_SUCCESS;
            }
            //i1 > 100mA && i1 < 200mA && mChargerVoltage >= 4100 PASS
            //else if(i1 > 100 && i1 < 200 && mChargerVoltage >= 4100){
            //    mQuickChargeTextView.setTextColor(Color.WHITE);
            //    mQuickChargeTextView.setText(getString(R.string.yes));
            //    result = TEST_RESULT_SUCCESS;
            //}
            else {
                result = TEST_RESULT_FAIL;
            }
			if(i1>0){
				mBatteryTextView.setText(i1 + " ma");
			}
        } catch (Exception e) {
            Log.w(TAG, "getInputElectronicNewStep fail", e);
            e.printStackTrace();
        }
        return result;
    }

    private void initView() {

        try {
            Thread.sleep(mWaitTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //Support kernel 4.14
        if(isSupportK414()){
            mChargerElectronic = getDateFromNode(ENG_CHARGER_CURRENT_K414) / 1000;
            mChargerVoltage = getDateFromNode(ENG_CHARGER_VOLTAGE_K414) / 1000;
        }else{
            mChargerElectronic = getDateFromNode(CHARGER_ELECTRONIC);
            mChargerVoltage = getDateFromNode(ENG_CHARGER_VOL);
        }
        Log.d(TAG, "initView mChargerElectronic="+mChargerElectronic+",mChargerVoltage="+mChargerVoltage);
        /* SPRD Bug 773805:Show NTC temperature in charger test. @{ */
        mNtcTemperature = getDateFromNode(ENG_CHARGER_TEMP);
        /* @} */

        if (mChargerElectronic > 0&& mIsPlugUSB) {
            mBatteryTextView.setText(mChargerElectronic + " ma");
        } else {
            mBatteryTextView.setText("n/a");
        }

        // General power of the test will have an initial value of 40mv.
        // Unfriendly so set a value greater than 100 and must plug usb or
        // ac
        if (mChargerVoltage >= 100.0 && mIsPlugUSB) {
            mElectronicTextView.setText(mChargerVoltage + " mv");
        } else {
            mElectronicTextView.setText("n/a");
        }

        /* SPRD Bug 773805:Show NTC temperature in charger test. @{ */
        //if(Const.isBoardISharkL210c10()){
            mTemperatureTextView.setText(mNtcTemperature / 10 + " \u2103");
        //}
        /* @} */
    }

    public boolean isNum(String str) {
        return str.matches("^[-+]?(([0-9]+)([.]([0-9]+))?|([.]([0-9]+))?)$");
    }

    private float getDateFromNode(String nodeString) {
        char[] buffer = new char[1024];
        // Set a special value -100, to distinguish mChargerElectronic greater
        // than -40.
        float batteryElectronic = -100;
        FileReader file = null;
        try {
            file = new FileReader(nodeString);
            int len = file.read(buffer, 0, 1024);
            batteryElectronic = Float.valueOf((new String(buffer, 0, len)));
        } catch (Exception e) {
            e.printStackTrace();
        }finally{
            try {
                if (file != null) {
                    file.close();
                    file = null;
                }
            } catch (IOException io) {
                io.printStackTrace();
            }   
        }
        return batteryElectronic;
    }
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
                int status = intent.getIntExtra(STATUS, 0);
                int plugged = intent.getIntExtra(PLUGGED, 0);
                int voltage = intent.getIntExtra(VOLTAGE, 0);
                String statusString = "";
                switch (status) {
                    case BatteryManager.BATTERY_STATUS_UNKNOWN:
                        statusString = getResources().getString(R.string.charger_unknown);
                        break;
                    case BatteryManager.BATTERY_STATUS_CHARGING:
                        statusString = getResources().getString(R.string.charger_charging);
                        break;
                    case BatteryManager.BATTERY_STATUS_DISCHARGING:
                        statusString = getResources().getString(R.string.charger_discharging);
                        break;
                    case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                        statusString = getResources().getString(R.string.charger_not_charging);
                        break;
                    case BatteryManager.BATTERY_STATUS_FULL:
                        statusString = getResources().getString(R.string.charger_full);
                        break;
                    default:
                        break;
                }
                switch (plugged) {
                    case BatteryManager.BATTERY_PLUGGED_AC:
                        mIsPlugUSB = true;
                        mPluggeString = getResources().getString(R.string.charger_ac_plugged);
                        if(mTestResultTextView != null) {
                            mTestResultTextView.setVisibility(View.VISIBLE);
                        }
                        break;
                    case BatteryManager.BATTERY_PLUGGED_USB:
                        mIsPlugUSB = true;
                        mPluggeString = getResources().getString(R.string.charger_usb_plugged);
                        if(mTestResultTextView != null) {
                            mTestResultTextView.setVisibility(View.VISIBLE);
                        }
                        break;
                    default:
                        mIsPlugUSB = false;
                        Log.d(TAG, "mIsPlugUSB = false.");
                        mPluggeString = getResources().getString(R.string.charger_no_plugged);
                        if(mTestResultTextView != null) {
                            mTestResultTextView.setText(getString(R.string.charging_test));
                            mTestResultTextView.setTextColor(Color.RED);
                            mTestResultTextView.setVisibility(View.GONE);
                        }
                        // Prevent unplug the usb cable is still charging
                        // status.
                        if (statusString.equals(getString(R.string.charger_charging))) {
                            statusString = getResources().getString(R.string.charger_discharging);
                            Log.d(TAG, "Correct the error displays charge status.");
                        }
                        break;
                }

                if (mIsPlugUSB) {
                    String typec_judge = readFile(USB_TYPEC_CC_JUDGE);
                    Log.d(TAG, "USB TypeC charging: " + typec_judge);
                    if (!TextUtils.isEmpty(typec_judge)) {
                        if (typec_judge.contains("cc_1")) {
                            mIsUsbForward = true;
                            if(mTypecForwardTextView != null) {
                                mTypecForwardTextView.setText(R.string.text_pass);
                            }
                        } else if (typec_judge.contains("cc_2")) {
                            mIsUsbReverse = true;
                            if(mTypecForwardTextView != null) {
                                mTypecReverseTextView.setText(R.string.text_pass);
                            }
                        }
                    }
                    if (mIsUsbReverse && mIsUsbForward) {
                        if (mIsFirstTime) {
                            //mIsFirstTime =false;
                            showView();
                            mHandler.postDelayed(mElectronicUpdate, 500);
                        }
                    } else if (mIsUsbForward || mIsUsbReverse) {
                        if(mTestResultTextView != null) {
                            mTestResultTextView.setText(getString(R.string.reverse_usb));
                            mTestResultTextView.setTextColor(Color.RED);
                        }
                    }
                }

                if(statusTextView != null) {
                    statusTextView.setText(statusString);
                }

                if(pluggedTextView != null) {
                    pluggedTextView.setText(mPluggeString);
                }

                if(voltageTextView != null) {
                    voltageTextView.setText(Integer.toString(voltage) + " mv");
                }

                mFullCapacity = Integer.parseInt(readFile(BATTERY_FULL_CAPACITY).trim()) / 1000;
                fullcapacityTextView.setText(mFullCapacity + " mA·H");
                Log.v(STATUS, statusString);
                Log.v(PLUGGED, mPluggeString);
                if(!mIsPlugUSB){
                    if(mPassButton != null) {
                        mPassButton.setVisibility(View.GONE);
                    }
                }
            }
        }
    };

    @Override
    public void onDestroy() {
        //unregisterReceiver(mBroadcastReceiver);
        mHandler.removeCallbacks(mCompleteTest);
        // stopCharge();
        super.onDestroy();
    }

    private String readFile(String path) {
        char[] buffer = new char[1024];
        String batteryElectronic = "";
        FileReader file = null;
        try {
            file = new FileReader(path);
            int len = file.read(buffer, 0, 1024);
            batteryElectronic = new String(buffer, 0, len);
        } catch (Exception e) {
            Log.w(TAG, "read fail:" + e);
        } finally {
            try {
                if (file != null) {
                    file.close();
                    file = null;
                }
            } catch (IOException io) {
                Log.w(TAG, "read file close fail");
            }
        }
        return batteryElectronic;
    }

    private void stopCharge() {
        mParse.writeChargeSwitch(1);
    }

    private void startCharge() {
        mParse.writeChargeSwitch(0);
    }
}
