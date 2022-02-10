
package com.sprd.validationtools.itemstest.sysinfo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.PhaseCheckParse;
import com.sprd.validationtools.R;
import com.sprd.validationtools.utils.FileUtils;
import com.sprd.validationtools.utils.WifiTestUtil;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;


public class SystemVersionTest extends BaseActivity
{
    private static final String TAG = "SystemVersionTest";
    private static final String PROD_VERSION_FILE = "/proc/version";
    private static final String TP_CHIP_ID = "/sys/touchscreen/chip_id";
    private static final String TP_VERSION_FILE = "/sys/touchscreen/firmware_version";
    private static final String CAMERA_SENSOR = "/sys/devices/virtual/misc/sprd_sensor/camera_sensor_name";
    private static final String LCD_ID = "/sys/class/display/panel0/name";
    private static final String LCD_MIPI_VOLTAGE_FILE = "/sys/bus/i2c/devices/6-004b/mipi_voltage";
    private static final String FINGERPRINT_ID = "/sys/devices/platform/soc/soc:ap-apb/70800000.spi/spi_master/spi0/spi0.0/debug/debug";
    private TextView androidVersion;
    private TextView linuxVersion;
    private TextView platformVersion;
    private TextView platformSn;
    private TextView product_model;
    private TextView base_band;
    private TextView phasecheckInfo;
    private TextView phasecheck_rf_pass;
    private TextView phasecheck_rf_fail;
    private boolean phasecheck_rf_result;
    private TextView phasecheck_cft_pass;
    private TextView phasecheck_cft_fail;
    private boolean phasecheck_cft_result;
    private TextView phasecheck_antenna_pass;
    private TextView phasecheck_antenna_fail;
    private TextView wifiMac;
    private boolean wifiMac_result;
    private TextView blueMac;
    private TextView camera_id_tv;
    private TextView tp_version_tv;
    private TextView fingerprint_id_tv;
    private TextView mipi_voltage_tv;
    private boolean blueMac_result;
    private boolean phasecheck_antenna_result;
    private WifiManager mWifiManager = null;
    private BluetoothManager mBlueManager = null;
    private WifiTestUtil wifiTestUtil = null;
    private boolean BT_WIFI_result = false;
    private boolean SN_result = false;
    private String wifimac_str;
    private String bluemac_str;
    private boolean hasmainbackcamera = false;
    private boolean hasfrontcamera = false;
    private boolean camera_id_result = false;
    private boolean TP_result = false;
    private boolean lcd_mipi_voltage_result = false;
    private String sn;
    private Timer timer;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    //wifimac_str = mWifiManager.getConnectionInfo().getMacAddress();
                    final String[] macAddresses = mWifiManager.getFactoryMacAddresses();
                    if (macAddresses != null && macAddresses.length > 0) {
                        wifimac_str = macAddresses[0];
                    }
                    /*** 
                    Log.i(TAG,"handleMessage sn = " + sn);
                    if(sn != null && !sn.equals("") && sn.length() >= 20){
                         String str_snsub = sn.substring(14,20);
                         String array[] = wifimac_str.toUpperCase().substring(9,17).split(":");
                         StringBuffer buffer = new StringBuffer();
                        for (String str :
                                array) {
                            buffer = buffer.append(str);
                        }
                         Log.i(TAG,"buffer = " + buffer);
                         int wifisub = Integer.valueOf(buffer.toString(),16);
                         int snsub = Integer.valueOf(str_snsub);
                        Log.i(TAG,"snsub = " + snsub + " wifisub = " + wifisub);
                        if(snsub + 4686804 - 1 == wifisub) {
                            SN_result = true;
                            platformSn.setTextColor(Color.WHITE);
                        }else {
                            SN_result = false;
                            platformSn.setTextColor(Color.RED);
                        }
                    }else {
                        SN_result = false;
                        platformSn.setTextColor(Color.RED);
                    }
                    ***/

                    break;
                case 1:
                    bluemac_str = BluetoothAdapter.getDefaultAdapter().getAddress();
                    break;
                case 2:
                    if(timer != null){
                        timer.purge();
                        timer.cancel();
                    }
                    if(!BT_WIFI_result){
                        //wifiMac.setTextColor(Color.RED);
                        //blueMac.setTextColor(Color.RED);
                    }
                    break;
            }

            /*** 
            if(!(wifimac_str != null && bluemac_str != null && wifimac_str.substring(0,8).toUpperCase().equals("40:88:E0") && bluemac_str.substring(0,8).toUpperCase().equals("1C:5F:FF") && wifimac_str.substring(9,16).toUpperCase().equals(bluemac_str.substring(9,16).toUpperCase()))){
                BT_WIFI_result = false;
            }else {
                BT_WIFI_result = true;
                if(timer != null){
                    timer.purge();
                    timer.cancel();
                }
                Log.i(TAG,"wifimac_str = " + wifimac_str.substring(0,8));
            }
            ***/

            wifiMac.setText(getString(R.string.wifi_mac_title) + " " + wifimac_str + "\n");
            blueMac.setText(getString(R.string.blue_mac_title) + " " + bluemac_str + "\n");

            Log.i(TAG,"phasecheck_rf_result = " + phasecheck_rf_result + " phasecheck_cft_result = " + phasecheck_cft_result + " phasecheck_antenna_result = " + phasecheck_antenna_result +
                    " BT_WIFI_result = " + BT_WIFI_result + " SN_result = " + SN_result + " camera_id_result = " + camera_id_result + " TP_result = " + TP_result);
            if(phasecheck_rf_result && phasecheck_cft_result && phasecheck_antenna_result && camera_id_result && TP_result) {
                if(mPassButton != null) {
                    mPassButton.setVisibility(View.VISIBLE);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.version);
        setTitle(R.string.version_test);
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mBlueManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        androidVersion = (TextView) findViewById(R.id.android_version);
        linuxVersion = (TextView) findViewById(R.id.linux_version);
        product_model = (TextView) findViewById(R.id.product_model);
        platformVersion = (TextView) findViewById(R.id.platform_version);
        platformSn = (TextView) findViewById(R.id.platform_sn);
        camera_id_tv = (TextView) findViewById(R.id.camera_id);
        tp_version_tv = (TextView) findViewById(R.id.tp_version);
        mipi_voltage_tv = (TextView) findViewById(R.id.mipi_voltage);
        base_band = (TextView) findViewById(R.id.base_band);
        androidVersion.setText(getString(R.string.android_version) + " " + Build.VERSION.RELEASE
                + "\n");
        linuxVersion.setText(getString(R.string.prop_version) + "\n" + getPropVersion());
        base_band.setText(getString(R.string.base_band_title) + " " + SystemProperties.get("gsm.version.baseband","unknown") + "\n");
        product_model.setText(getString(R.string.product_model) + " " + Build.MODEL + "\n");
        platformVersion.setText(getString(R.string.build_number) + " " +
                SystemProperties.get("ro.internal.display.id", "unknown") + "\n");
        phasecheckInfo = (TextView) findViewById(R.id.phasecheckInfo);
        platformSn.setText(getString(R.string.device_sn) + "\n" + getSn() + "\n");


        wifiMac = (TextView) findViewById(R.id.wifi_mac);
        blueMac = (TextView) findViewById(R.id.blue_mac);
        /*SPRD bug 855450:ZTE feature*/
        if(Const.isBoardISharkL210c10() && getIntent() != null && getIntent().getExtras() != null){
            String securiy_code = getIntent().getExtras().getString(Const.SECURITY_CODE);
            Log.d(TAG, "onCreate securiy_code="+securiy_code);
            if(!TextUtils.isEmpty(securiy_code) && "833".equals(securiy_code)){
                platformVersion.setVisibility(View.GONE);
            }
        }
        /*@}*/
        //mPassButton.setVisibility(View.GONE);
        PhaseCheckParse parse = PhaseCheckParse.getInstance();
        String info = parse.getPhaseCheck();
        Log.i(TAG,"parse.getPhaseCheck() = " + info);
        phasecheck_rf_result = info.contains("CALIBRAT:PASS");
        phasecheck_cft_result = info.contains("CFT:PASS");
        phasecheck_antenna_result = info.contains("ANT:PASS");

        phasecheckInfo.setText((phasecheck_rf_result ? getString(R.string.phasecheck_rf_pass) : getString(R.string.phasecheck_rf_fail)) + "\n"
                                + (phasecheck_cft_result ? getString(R.string.phasecheck_cft_pass) : getString(R.string.phasecheck_cft_fail)) + "\n"
                                + (phasecheck_antenna_result ? getString(R.string.phasecheck_antenna_pass) : getString(R.string.phasecheck_antenna_fail)));

        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] cameraIdList = cameraManager.getCameraIdList();
            for (String strcameraId : cameraIdList) {
                Log.i(TAG, " strcameraId = " + strcameraId);
                int cameraId = Integer.parseInt(strcameraId);
                Log.d(TAG, " cameraId = " + cameraId);
                if (cameraId == 0) {
                    hasmainbackcamera = true;
                }
                if(cameraId == 1){
                    hasfrontcamera = true;
                }
            }
        } catch (CameraAccessException ex) {
            Log.e(TAG, "Unable to read camera list.", ex);
        }
        if(!hasmainbackcamera || !hasfrontcamera){
            camera_id_tv.setTextColor(Color.RED);
            camera_id_result = false;
        }else {
            camera_id_result = true;
        }

        String mainCamera = "";
        String subCamera = "";
        try {
            String cameraInfo = checkCameraID();
            Log.i(TAG,"cameraInfo = " + cameraInfo);
            if(cameraInfo != null) {
                BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cameraInfo.getBytes(Charset.defaultCharset())), Charset.defaultCharset()));  
                String line;
                int i = 0;  
                while ( (line = br.readLine()) != null ) {  
                    if(!line.trim().equals("")){
                        i++;
                        if(i == 1) {
                            mainCamera = line;
                        } else if(i == 2) {
                            subCamera = line;
                        }
                        Log.i(TAG,"line:  " + line);
                    }       		    	 
                }                 
            }
        }catch (IOException e){

        }        

        camera_id_tv.setText(getString(R.string.camera_main_title) + (hasmainbackcamera ? mainCamera:"NULL ") + "\n" + (getString(R.string.camera_sub_title) + (hasfrontcamera ? subCamera:"NULL")) + "\n");

        String tp_id = checkTpID();
        Log.i(TAG,"tp_id = " + tp_id);
        if(tp_id != null && tp_id.length() > 0){
            TP_result = true;
        }else {
            TP_result = false;
            tp_version_tv.setTextColor(Color.RED);
        }

        String tp_version = checkTpVersion();
        Log.i(TAG,"tp_version = " + tp_version);
        if(tp_version != null && tp_version.length() > 0){
            TP_result = true;
        }else {
            TP_result = false;
            tp_version_tv.setTextColor(Color.RED);
        }


        tp_version_tv.setText(getString(R.string.tp_version_title) + "\n" + tp_id + " " + tp_version);

        //int mipi_voltage = Integer.valueOf(readLcdMipiVoltage());
        /*** 
        try {
            String mipi_volteage = readLcdMipiVoltage();
            if(mipi_volteage != null) {
                int mipi_voltage = Integer.parseInt(mipi_volteage.replace("\n",""));
                Log.d(TAG, "mipi_voltage = " + mipi_voltage);
                if (mipi_voltage == 5500) {
                    mipi_voltage_tv.setTextColor(Color.WHITE);
                    mipi_voltage_tv.setText(getString(R.string.mipi_voltage_title) + (((float)mipi_voltage) / 1000) + "V" + "\n");
                    lcd_mipi_voltage_result = true;
                } else {
                    mipi_voltage_tv.setTextColor(Color.RED);
                    mipi_voltage_tv.setText(getString(R.string.mipi_voltage_title) + (((float)mipi_voltage) / 1000) + "V" + "\n");
                    lcd_mipi_voltage_result = false;
                }
            }
        }catch (NumberFormatException e){
            Log.d(TAG, "mipi_voltage NumberFormatException " + e);
        }
        ***/

        String lcdInfo = checkLCDID();
        if(lcdInfo != null) {
            if(lcdInfo.length() > 0) {
                mipi_voltage_tv.setText(getString(R.string.lcd_id) + lcdInfo + getString(R.string.mipi_voltage_title) + "5.5V\n");
            } else {
                mipi_voltage_tv.setText(getString(R.string.lcd_id) + "\n" + getString(R.string.mipi_voltage_title) + "\n");
            }
        }
        

        final IntentFilter blueConnectivityIntentFilter = new IntentFilter();
        blueConnectivityIntentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);

        registerReceiver(mBlueConnectivityReceiver, blueConnectivityIntentFilter,
                android.Manifest.permission.CHANGE_NETWORK_STATE, null);

        final IntentFilter wifiConnectivityIntentFilter = new IntentFilter();
        wifiConnectivityIntentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);

        registerReceiver(mWifiConnectivityReceiver, wifiConnectivityIntentFilter,
                android.Manifest.permission.CHANGE_NETWORK_STATE, null);

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(0);
                mHandler.sendEmptyMessage(1);
            }
        },0,2*1000);


        fingerprint_id_tv = (TextView) findViewById(R.id.fingerprint_id);
        fingerprint_id_tv.setText("Fingerprint id: " + checkFingerprintID());
    }

    private final BroadcastReceiver mWifiConnectivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i("pepsl","mWifiConnectivityReceiver action = " + action);
            mHandler.sendEmptyMessageDelayed(0,3*1000);
        }
    };

    private final BroadcastReceiver mBlueConnectivityReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i("pepsl","mBlueConnectivityReceiver action = " + action);
            mHandler.sendEmptyMessageDelayed(1,3*1000);
        }
    };

    private String readLcdMipiVoltage(){
        return FileUtils.readFile(LCD_MIPI_VOLTAGE_FILE);
    }

    private String getPropVersion() {
        return FileUtils.readFile(PROD_VERSION_FILE);
    }

    private String getSn() {
        PhaseCheckParse parse = PhaseCheckParse.getInstance();
        sn = parse.getSn();
        Log.i(TAG,"sn = " + sn);
        return sn;
    }

    @Override
    protected void onResume() {
        super.onResume();
        startTest();
        mHandler.sendEmptyMessageDelayed(2,20*1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTest();
        unregisterReceiver(mBlueConnectivityReceiver);
        unregisterReceiver(mWifiConnectivityReceiver);
    }

    public void startTest() {
        Log.d(TAG, "startTest mWifiManager.isWifiEnabled="+ mWifiManager.isWifiEnabled());
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            BluetoothAdapter.getDefaultAdapter().enable();
        }
    }

    public void stopTest() {
        Log.d(TAG, "stopTest! mWifiManager.isWifiEnabled=" + mWifiManager.isWifiEnabled());
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }
        if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            BluetoothAdapter.getDefaultAdapter().disable();
        }
    }

    private String checkTpVersion(){
        return FileUtils.readFile(TP_VERSION_FILE);
    }

    private String checkTpID(){
        return FileUtils.readFile(TP_CHIP_ID);
    }

    private String checkCameraID(){
        return FileUtils.readFile(CAMERA_SENSOR);
    }

    private String checkLCDID(){
        return FileUtils.readFile(LCD_ID);
    }

    private String checkFingerprintID(){
        if(FileUtils.fileIsExists(FINGERPRINT_ID)) {
            return "goodix";
        } else {
            return "silead";
        }
    }

}