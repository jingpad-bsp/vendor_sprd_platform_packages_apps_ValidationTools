package com.sprd.validationtools.itemstest.telephony;

import java.util.ArrayList;
import java.util.List;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.R;


import android.os.Bundle;
import android.os.Handler;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.graphics.Color;

import com.android.internal.telephony.TelephonyIntents;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;



public class SIMCardTestActivity extends BaseActivity {

    private LinearLayout container;

    private TelephonyManager telMgr;
    private int mSimReadyCount = 0;
    private int phoneCount;
    public Handler mHandler = new Handler();
    private Button  startButton;
    private Button mRetestButton;
    private TextView Sim1_test_status = null;
    private TextView Sim2_test_status = null;
    private boolean mItest = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sim_card_test);
        setTitle(R.string.sim_card_test_tittle);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        container = (LinearLayout) findViewById(R.id.sim_test_result_container);
        setTitle(R.string.sim_card_test_tittle);
        mPassButton.setVisibility(View.GONE);
         startButton = new Button(this);
         startButton.setText(R.string.SIMstartTest);
         startButton.setTextSize(25);
        Sim1_test_status = (TextView) findViewById(R.id.sim1_test);
        Sim2_test_status = (TextView) findViewById(R.id.sim2_test);
         //startButton.setBackgroundColor(Color.GREEN);
         startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startTest();
            }
        });
        container.addView(startButton);

         mRetestButton = new Button(this);
         mRetestButton.setText(R.string.manual_retest);
         mRetestButton.setTextSize(25);
         mRetestButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startTest();
            }
        });
        container.addView(mRetestButton);
        mRetestButton.setVisibility(View.GONE);

    }
    private void  startTest(){
        startButton.setVisibility(View.GONE);
        phoneCount = 1;//TelephonyManager.from(SIMCardTestActivity.this).getPhoneCount();
        mRetestButton.setVisibility(View.GONE);
        //showDevice();
        final IntentFilter filter = new IntentFilter();
        filter.addAction(TelephonyIntents.ACTION_SIM_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        /*SPRD bug 760913:Test can pass/fail must click button*/
        if(Const.isBoardISharkL210c10()){
            mPassButton.setVisibility(View.GONE);
        }
        Sim1_test_status.setText("SIM1:检测中");
        Sim2_test_status.setText("SIM2:检测中");
        /*@}*/
        int phoneCount = 1;//TelephonyManager.from(SIMCardTestActivity.this).getPhoneCount();
        mFailButton.setEnabled(false);
        /*SPRD bug 760913:Test can pass/fail must click button*/
        if(Const.isBoardISharkL210c10()){
            //ignore
        }else{
            //super.removeButton();
        }
        mHandler.postDelayed(mTimeOutRunnable, 10000);
        mItest = true;
        /*@}*/
    }
    /* UNISOC:Bug 1506527 java.lang.IndexOutOfBoundsException @{ */
    private List<String> getResultList(int simId) {
        List<String> resultList = new ArrayList<String>();
        telMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE + simId);
        if (telMgr == null) {
            telMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
            if (telMgr == null) {
                return null;
            }
        }
        int mSubId = SubscriptionManager.getSubId(simId)[0];
        /* SPRD:436223 Sim test wrong in sigle sim in the phone@{*/
        if (telMgr.getSimState(simId) == TelephonyManager.SIM_STATE_READY) {
            resultList.add("fine");
            mSimReadyCount++;
        } else if (telMgr.getSimState(simId) == TelephonyManager.SIM_STATE_ABSENT) {
            resultList.add("no SIM card");
        } else {
            resultList.add("locked/unknow");
        }

        if (telMgr.getSimCountryIsoForPhone(simId).equals("")) {
            resultList.add("can not get country");
        } else {
            resultList.add(telMgr.getSimCountryIsoForPhone(simId));
        }

        if (telMgr.getSimOperatorNumericForPhone(simId).equals("")) {
            resultList.add("can not get operator");
        } else {
            resultList.add(telMgr.getSimOperatorNumericForPhone(simId));
        }

        if (telMgr.getSimOperatorNameForPhone(simId).equals("")) {
            resultList.add("can not get operator name");
        } else {
            resultList.add(telMgr.getSimOperatorNameForPhone(simId));
        }

        if (telMgr.getSimSerialNumber(mSubId) != null && SubscriptionManager.isValidSubscriptionId(mSubId)) {
            resultList.add(telMgr.getSimSerialNumber(mSubId));
        } else {
            resultList.add("can not get serial number");
        }

        if (telMgr.getSubscriberId(mSubId) != null && SubscriptionManager.isValidSubscriptionId(mSubId)) {
            resultList.add(telMgr.getSubscriberId(mSubId));
        } else {
            resultList.add("can not get subscriber id");
        }

        if (telMgr.getDeviceId(simId) != null) {
            resultList.add(telMgr.getDeviceId(simId));
        } else {
            resultList.add("can not get device id");
        }

        if (telMgr.getLine1Number(mSubId) != null && SubscriptionManager.isValidSubscriptionId(mSubId)) {
            resultList.add(telMgr.getLine1Number(mSubId));
        } else {
            resultList.add("can not get phone number");
        }

        if (telMgr.getPhoneType(simId) == 0) {
            resultList.add("NONE");
        } else if (telMgr.getPhoneType(simId) == 1) {
            resultList.add("GSM");
        } else if (telMgr.getPhoneType(simId) == 2) {
            resultList.add("CDMA");
        } else if (telMgr.getPhoneType(simId) == 3) {
            resultList.add("SIP");
        } else {
            resultList.add("unknow");
        }
        /* @}*/

        if (telMgr.getDataState() == 0) {
            resultList.add("disconnected");
        } else if (telMgr.getDataState() == 1) {
            resultList.add("connecting");
        } else if (telMgr.getDataState() == 2) {
            resultList.add("connected");
        } else if (telMgr.getDataState() == 3) {
            resultList.add("suspended");
        } else {
            resultList.add("unknow");
        }

        if (telMgr.getDataActivity() == 0) {
            resultList.add("none");
        } else if (telMgr.getDataActivity() == 1) {
            resultList.add("in");
        } else if (telMgr.getDataActivity() == 2) {
            resultList.add("out");
        } else if (telMgr.getDataActivity() == 3) {
            resultList.add("in/out");
        } else if (telMgr.getDataActivity() == 4) {
            resultList.add("dormant");
        } else {
            resultList.add("unknow");
        }

        if (!telMgr.getNetworkCountryIsoForPhone(simId).equals("")) {
            resultList.add(telMgr.getNetworkCountryIsoForPhone(simId));
        } else {
            resultList.add("can not get network country");
        }

        if (!telMgr.getNetworkOperatorForPhone(simId).equals("")) {
            resultList.add(telMgr.getNetworkOperatorForPhone(simId));
        } else {
            resultList.add("can not get network operator");
        }

        if (telMgr.getNetworkType(mSubId) == 1) {
            resultList.add("gprs");
        } else if (telMgr.getNetworkType(mSubId) == 2) {
            resultList.add("edge");
        } else if (telMgr.getNetworkType(mSubId) == 3) {
            resultList.add("umts");
        } else if (telMgr.getNetworkType(mSubId) == 4) {
            resultList.add("hsdpa");
        } else if (telMgr.getNetworkType(mSubId) == 5) {
            resultList.add("hsupa");
        } else if (telMgr.getNetworkType(mSubId) == 6) {
            resultList.add("hspa");
        } else if (telMgr.getNetworkType(mSubId) == 7) {
            resultList.add("cdma");
        } else if (telMgr.getNetworkType(mSubId) == 8) {
            resultList.add("evdo 0");
        } else if (telMgr.getNetworkType(mSubId) == 9) {
            resultList.add("evdo a");
        } else if (telMgr.getNetworkType(mSubId) == 10) {
            resultList.add("evdo b");
        } else if (telMgr.getNetworkType(mSubId) == 11) {
            resultList.add("1xrtt");
        } else if (telMgr.getNetworkType(mSubId) == 12) {
            resultList.add("iden");
        } else if (telMgr.getNetworkType(mSubId) == 13) {
            resultList.add("lte");
        } else if (telMgr.getNetworkType(mSubId) == 14) {
            resultList.add("ehrpd");
        } else if (telMgr.getNetworkType(mSubId) == 15) {
            resultList.add("hspap");
        } else {
            resultList.add("unknow");
        }
        return resultList;
    }
    /* @}*/

    private List<String> getKeyList() {
        List<String> keyList = new ArrayList<String>();
        keyList.add("Sim State:  ");
        keyList.add("Sim Country:  ");
        keyList.add("Sim Operator:  ");
        keyList.add("Sim Operator Name:  ");
        keyList.add("Sim Serial Number:  ");
        keyList.add("Subscriber Id:  ");
        keyList.add("Device Id:  ");
        keyList.add("Line 1 Number:  ");
        keyList.add("Phone Type:  ");
        keyList.add("Data State:  ");
        keyList.add("Data Activity:  ");
        keyList.add("Network Country:  ");
        keyList.add("Network Operator:  ");
        keyList.add("Network Type:  ");
        return keyList;
    }

    private void showDevice() {

        List<String> keyList = getKeyList();
        List<String> resultList0 = null;

        for (int i = 0; i < phoneCount; i++) {
            TextView tv = new TextView(this);
            if (i != 0) {
                tv.append("\n\n");
            }
            tv.append("Sim" + (i + 1) + " "
                    + this.getResources().getString(R.string.sim_test_result)
                    + ":\n");
            resultList0 = getResultList(i);
            for (int j = 0; j < 1; j++) {
                if (resultList0 != null)
                    tv.append(keyList.get(j) + resultList0.get(j) + "\n");
                else
                    tv.append(keyList.get(j) + "\n");
            }
            container.addView(tv);
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String action = intent.getAction();
            int phoneCount = 1;//TelephonyManager.from(SIMCardTestActivity.this).getPhoneCount();
            if(phoneCount == 1){
                Sim2_test_status.setVisibility(View.GONE);
            }
            Log.d("whq", "onReceive action =" + action);
            if (TelephonyIntents.ACTION_SIM_STATE_CHANGED.equals(action)) {
                telMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                Log.d("whq", "onReceive telMgr.getSimState(0) =" + telMgr.getSimState(0) + "telMgr.getSimState(1) = " + telMgr.getSimState(1));
                if(telMgr.getSimState(0) == TelephonyManager.SIM_STATE_READY && phoneCount == 1) {
                    mSimReadyCount = 2;
                   // showDevice();
                    Sim1_test_status.setText("SIM1:检测到SIM卡");
                    Sim2_test_status.setText("SIM2:检测到SIM卡");
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(SIMCardTestActivity.this,
                                    R.string.text_pass, Toast.LENGTH_SHORT).show();
                            storeRusult(true);
                            finish();
                        }
                    }, 1000);
                }else{
                    if (telMgr.getSimState(0) == TelephonyManager.SIM_STATE_ABSENT && telMgr.getSimState(1) == TelephonyManager.SIM_STATE_READY){
                        Sim1_test_status.setText("SIM1:未检测到SIM卡");
                        Sim2_test_status.setText("SIM2:检测到SIM卡");
                    }else if (telMgr.getSimState(1) == TelephonyManager.SIM_STATE_ABSENT && telMgr.getSimState(0) == TelephonyManager.SIM_STATE_READY){
                        Sim1_test_status.setText("SIM1:检测到SIM卡");
                        Sim2_test_status.setText("SIM2:未检测到SIM卡");
                    }else{
                        Sim1_test_status.setText("SIM1:未检测到SIM卡");
                        Sim2_test_status.setText("SIM2:未检测到SIM卡");
                    }


                   // showDevice();
                }
            }
        }
    };

    @Override
    protected void onPause() {
        if(mItest){
            unregisterReceiver(mReceiver);
            mHandler.removeCallbacks(mTimeOutRunnable);
        }
        super.onPause();
    }

    public Runnable mTimeOutRunnable = new Runnable() {
        public void run() {
            Toast.makeText(SIMCardTestActivity.this, R.string.text_fail, Toast.LENGTH_SHORT).show();
            //storeRusult(false);
            //finish();
            mFailButton.setEnabled(true);
            mRetestButton.setVisibility(View.VISIBLE);
        }
    };

//    @Override
//    public void onBackPressed() {
//        if (TelephonyManager.getPhoneCount() == mSimReadyCount) {
//            storeRusult(true);
//        } else {
//            storeRusult(false);
//        }
//        finish();
//    }
}
