package com.sprd.validationtools.modules;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.telephony.TelephonyManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.os.SystemProperties;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import android.net.wifi.WifiManager;
import java.io.FileNotFoundException;
import java.io.File;
import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.sprd.validationtools.R;

import android.util.Log;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.os.Build;


public class QrActivity extends Activity {
	private static final String TAG = "QrActivity";
	private static final int width = 500;
	private static final String WIFI_MACID_FILE_PATH = "/mnt/vendor/wifimac.txt";
	private static final String BT_MACID_FILE_PATH = "/data/vendor/bluetooth/btmac.txt";
	private WifiManager mWifiManager;
	private BluetoothManager mBlueManager;
	private TextView tvbtmacsummary,tvwifimacsummary;
	private ImageView ivbtmac,ivwifimac;
    private Timer timer;
    private String wifimac_str,bluemac_str;
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
                    break;
                case 1:
                    bluemac_str = BluetoothAdapter.getDefaultAdapter().getAddress();
                    break;
                case 2:
                    if(timer != null){
                        timer.purge();
                        timer.cancel();
                    }
                    break;
            }

            if((wifimac_str != null && bluemac_str != null)){
                if(timer != null){
                    timer.purge();
                    timer.cancel();
                }
            }
            if(bluemac_str != null) {
                tvbtmacsummary.setText(bluemac_str);
                ivbtmac.setImageBitmap(generateBitmap(bluemac_str));
            }else {
                bluemac_str = getResources().getString(R.string.status_unavailable);
                tvbtmacsummary.setText(bluemac_str);
                ivbtmac.setImageBitmap(generateBitmap(bluemac_str));
            }

            if(wifimac_str != null) {
                tvwifimacsummary.setText(wifimac_str);
                ivwifimac.setImageBitmap(generateBitmap(wifimac_str));
            }else {
                wifimac_str = getResources().getString(R.string.status_unavailable);
                tvwifimacsummary.setText(wifimac_str);
                ivwifimac.setImageBitmap(generateBitmap(wifimac_str));
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.qr);
        mWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mBlueManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        enableBtWifi();
		//SN
		TextView tvsn = (TextView)findViewById(R.id.sn);
        TextView tvsnsummary = (TextView)findViewById(R.id.sn_summary);
        ImageView ivsn = (ImageView)findViewById(R.id.iv_sn);
		tvsn.setText("SN");
        String serial = Build.getSerial();
        tvsnsummary.setText(serial);
        ivsn.setImageBitmap(generateBitmap(serial));


		//IMEI
        TextView tvimei = (TextView)findViewById(R.id.imei);
        TextView tvimeisummary = (TextView)findViewById(R.id.imei_summary);
        ImageView ivimei = (ImageView)findViewById(R.id.iv_imei);
		tvimei.setText("IMEI");
		String imeiStr = "unknow IMEI";		
        try {
			TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
			imeiStr = tm.getImei();
   		} catch (Exception e) {
			e.printStackTrace();
			imeiStr = "unknow IMEI";
		}
		tvimeisummary.setText(imeiStr);
		ivimei.setImageBitmap(generateBitmap(imeiStr));


		//WIFI MAC
        TextView tvwifimac = (TextView)findViewById(R.id.wifi_mac);
        tvwifimacsummary = (TextView)findViewById(R.id.wifi_mac_summary);
		ivwifimac = (ImageView)findViewById(R.id.iv_wifi_mac);
		tvwifimac.setText("WIFI MAC");
		/*WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		final String[] macAddresses = wifiManager.getFactoryMacAddresses();
        String wifiMacAddress = null;
        if (macAddresses != null && macAddresses.length > 0) {
            wifiMacAddress = macAddresses[0];
        }
        if (TextUtils.isEmpty(wifiMacAddress)) {
            String mac = getWifiMacFromFile();
            if (!TextUtils.isEmpty(mac)) {
				wifiMacAddress = mac;
            } else {
                wifiMacAddress = getResources().getString(R.string.status_unavailable);
            }
		}
		tvwifimacsummary.setText(wifiMacAddress.toLowerCase());
        ivwifimac.setImageBitmap(generateBitmap(wifiMacAddress.toLowerCase()));*/


		//BT MAC
        TextView tvbtmac = (TextView)findViewById(R.id.bt_mac);
        tvbtmacsummary = (TextView)findViewById(R.id.bt_mac_summary);
		ivbtmac = (ImageView)findViewById(R.id.iv_bt_mac);
		String btMacAddress = null;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        tvbtmac.setText("BT MAC");
        /*if (bluetoothAdapter != null) {
            btMacAddress = bluetoothAdapter.isEnabled() ? bluetoothAdapter.getAddress() : null;
            if (!TextUtils.isEmpty(btMacAddress)) {
                // Convert the address to lowercase for consistency with the wifi MAC address.
                tvbtmacsummary.setText(btMacAddress.toLowerCase());
                ivbtmac.setImageBitmap(generateBitmap(btMacAddress.toLowerCase()));
            } else {
				String mac = getBtMacFromFile();
				if (!TextUtils.isEmpty(mac)) {
					btMacAddress = mac;
				} else {
					btMacAddress = getResources().getString(R.string.status_unavailable);
				}

                tvbtmacsummary.setText(btMacAddress.toLowerCase());
                ivbtmac.setImageBitmap(generateBitmap(btMacAddress.toLowerCase()));
            }
        } else {
			String mac = getBtMacFromFile();
			if (!TextUtils.isEmpty(mac)) {
				btMacAddress = mac;
			} else {
				btMacAddress = getResources().getString(R.string.status_unavailable);
			}

			tvbtmacsummary.setText(btMacAddress.toLowerCase());
			ivbtmac.setImageBitmap(generateBitmap(btMacAddress.toLowerCase()));
	    }*/

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
    }

    private Bitmap generateBitmap(String content) {
        Log.e("DevicesInfo ", content);
        if (content.equals("")) {
                return null;
            }
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        Map<EncodeHintType, String> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        try {
            BitMatrix encode = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, width, hints);
            int[] pixels = new int[width * width];
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < width; j++) {
                    if (encode.get(j, i)) {
                        pixels[i * width + j] = 0x00000000;
                    } else {
                        pixels[i * width + j] = 0xffffffff;
                    }
                }
            }
            return Bitmap.createBitmap(pixels, 0, width, width, width, Bitmap.Config.RGB_565);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
	}
	
	private String getWifiMacFromFile() {
        File file = new File(WIFI_MACID_FILE_PATH);
        BufferedReader reader = null;
        String macAddress = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                macAddress = line;
                break;
            }
        } catch (FileNotFoundException e) {
            Log.w(TAG , "WIFI Mac file not exist", e);
        } catch (Exception e) {
            Log.w(TAG , "get wifi mac from file caught exception", e);
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                Log.w(TAG, "reader close exception");
            }
        }
        return macAddress;
	}
	
	private String getBtMacFromFile() {
        File file = new File(BT_MACID_FILE_PATH);
        BufferedReader reader = null;
        String macAddress = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = reader.readLine()) != null) {
                macAddress = line;
                break;
            }
        } catch (FileNotFoundException e) {
            Log.w(TAG , "BT Mac file not exist", e);
        } catch (Exception e) {
            Log.w(TAG , "get bt mac from file caught exception", e);
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                Log.w(TAG, "reader close exception");
            }
        }
        return macAddress;
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

    @Override
    protected void onPause() {
        super.onPause();
        disableBtWifi();
        unregisterReceiver(mBlueConnectivityReceiver);
        unregisterReceiver(mWifiConnectivityReceiver);
    }

    public void enableBtWifi() {
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            BluetoothAdapter.getDefaultAdapter().enable();
        }
    }

    public void disableBtWifi() {
        if (mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(false);
        }
        if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            BluetoothAdapter.getDefaultAdapter().disable();
        }
    }
}
