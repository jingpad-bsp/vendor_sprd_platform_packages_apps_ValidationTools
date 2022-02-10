package com.sprd.validationtools.itemstest.current;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.R;

import java.io.FileReader;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class CurrentTest extends BaseActivity {
    private final String TAG = "CurrentTest";
    private static final String ENG_CHARGER_FGU_CURRENT_K414 = "/sys/class/power_supply/sc27xx-fgu/current_now";
    private Boolean battery_capacity_flag = false;
    private Boolean battery_state = false;
    /* access modifiers changed from: private */
    public int charge_count_01 = 0;
    /* access modifiers changed from: private */
    public int charge_count_02 = 0;
    /* access modifiers changed from: private */
    public Boolean charge_flag = false;
    /* access modifiers changed from: private */
    public int[] current_array = new int[12];
    private TextView current_average_tv;
    /* access modifiers changed from: private */
    public TextView current_now_tv;
    /* access modifiers changed from: private */
    public TextView current_show_tv;
    private Boolean level_flag = false;
    private BroadcastReceiver mBatteryInfoReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.BATTERY_CHANGED".equals(intent.getAction())) {
                int status = intent.getIntExtra("status", 0);
                Log.d(TAG, "QQQQQQQQQQQQQstatus::::" + status);
                if (status == 5 || status == 2) {
                    charge_count_01 = 2;
                    charge_flag = true;
                    Log.d(TAG, "QQQQQQQQQQQQQbateria::::Charging");
                } else if (status == 3) {
                    charge_count_02 = 2;
                    Log.d(TAG, "QQQQQQQQQQQQQbateria::::discharging");
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public Boolean present_flag = false;
    private Boolean qen_flag = false;
    /* access modifiers changed from: private */
    public Boolean success_flag = false;
    /* access modifiers changed from: private */
    public int time_count = 0;
    /* access modifiers changed from: private */
    public int time_exit_count = 0;
    private Timer timer = new Timer();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.current_test);
        current_now_tv = (TextView) findViewById(R.id.current_now_id);
        current_average_tv = (TextView) findViewById(R.id.current_average_id);
        current_show_tv = (TextView) findViewById(R.id.bateria_current_now);
        mPassButton.setEnabled(false);
        mPassButton.setBackgroundColor(Color.GRAY);
        current_now_tv.setTextColor(-256);
        current_now_tv.setText(R.string.current_now_str_detect);
        registerReceiver(mBatteryInfoReceiver, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        time_count = time_count + 1;
                        int current_int = getCurrent() / 1000;
                        Log.d(TAG, " current_int is " + current_int);
                        if (current_int >= 4800) {
                            current_int = 65535 - current_int;
                            Log.d(TAG, "current_int >4800 is " + current_int);
                        }
                        if (time_count % 12 == 1) {
                            current_array[0] = current_int;
                            Log.d(TAG, "current_array[0] is " +current_array[0]);
                        } else if (time_count % 12 == 5) {
                            current_array[1] = current_int;
                            Log.d(TAG, "current_array[1] is " + current_array[1]);
                        } else if (time_count % 12 == 9) {
                            current_array[2] = current_int;
                            Log.d(TAG, "current_array[2] is " + current_array[2]);
                            Boolean unused2 = present_flag = true;
                        }
                        if (present_flag) {
                            int curren_mean = ((current_array[0] + current_array[1]) +current_array[2]) / 3;
                            Log.d(TAG, "curren_mean is " + curren_mean);
                            if (curren_mean < -1000) {
                                current_show_tv.setTextColor(Color.RED);
                            } else {
                                Boolean unused3 = success_flag = true;
                                current_show_tv.setTextColor(Color.GREEN);
                                if(mPassButton != null) {
                                    mPassButton.setEnabled(true);
                                    mPassButton.setBackgroundColor(Color.GREEN);
                                }
                            }
                            current_show_tv.setText("-" + Integer.toString(current_int) + "mA/" + curren_mean + "mA");
                        }
                        if (charge_flag.booleanValue() || success_flag.booleanValue()) {
                            time_exit_count = time_exit_count + 1;
                            if (success_flag) {
                                current_now_tv.setTextColor(Color.GREEN);
                                current_now_tv.setText(R.string.current_now_str_detect_1);
                                if(mPassButton != null) {
                                    mPassButton.setEnabled(true);
                                    mPassButton.setBackgroundColor(Color.GREEN);
                                }
                            } else {
                                current_now_tv.setTextColor(Color.RED);
                                current_now_tv.setText(R.string.current_now_str_detect_0);
                            }
                            if (time_exit_count == 2) {
                                //finish();
                            }
                        } else if (time_count == 30) {
                            current_now_tv.setTextColor(Color.RED);
                            current_now_tv.setText(R.string.current_now_str_detect_0);
                        } else if (time_count == 32) {
                            Log.i(TAG,"time_count = " + time_count);
                        }
                    }
                });
            }
        }, 0, 500);
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

    private int getCurrent(){
        int current = Integer.parseInt(readFile(ENG_CHARGER_FGU_CURRENT_K414).trim());
        Log.i(TAG,"getCurrent current = " + current);
        return current;
    }
    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if(timer != null){
            timer.purge();
            timer.cancel();
        }
        super.onDestroy();
    }
}
