
package com.sprd.validationtools.itemstest.bt;

import java.util.ArrayList;
import java.util.List;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.R;
import com.sprd.validationtools.utils.BtTestUtil;
import com.sprd.validationtools.utils.WcndUtils;

import android.widget.Button;

public class BluetoothTestActivity extends BaseActivity {
    private static final String TAG = "BluetoothTestActivity";

    private List<BluetoothDevice> mBluetoothDeviceList = new ArrayList<BluetoothDevice>();

    private TextView tvBtAddr = null;
    private TextView tvBtState = null;
    private TextView tvBtDeviceList = null;
	private boolean  isFirst=true;
    private BtTestUtil btTestUtil = null;
    private Button mRetestButton;

    @Override
    protected void onDestroy() {
        WcndUtils.dumpCPLog();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.w(TAG, "+++++++enter bt+++++++++");
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		isFirst=true;
        setContentView(R.layout.bluetooth_result);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		mPassButton.setVisibility(View.GONE);
        btTestUtil = new BtTestUtil() {

            public void btStateChange(int newState) {
                switch (newState) {
                    case BluetoothAdapter.STATE_ON:
                        tvBtState.setText("Bluetooth ON,Discovering...");
                        // SPRD: update bluetooth address when bt power on
                        tvBtAddr.setText(btTestUtil.getBluetoothAdapter().getAddress() + "\n");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        tvBtState.setText("Bluetooth Closing");
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        tvBtState.setText("Bluetooth OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        tvBtState.setText("Bluetooth Opening...");
                        break;
                    default:
                        tvBtState.setText("Bluetooth state Unknown");
                        break;
                }
            }

            public void btDeviceListAdd(BluetoothDevice device) {

                if (mBluetoothDeviceList.contains(device)) {
                    return;
                }

                if (device != null) {
                    //mBluetoothDeviceList.add(device);
                    if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                        String name = device.getName().trim();
                        if (name == null || name.isEmpty()) {
                            return;
                        }

                        mBluetoothDeviceList.add(device);
                        StringBuffer deviceInfo = new StringBuffer();
                        deviceInfo.append("device name: ");
                        deviceInfo.append(name);
                        deviceInfo.append("\n");
                        Log.d(TAG, "======find bluetooth device => name : " + name
                                + "\n address :" + device.getAddress());
                        tvBtDeviceList.append(deviceInfo.toString());
						if(isFirst==true){
							isFirst=false;
							mPassButton.setVisibility(View.VISIBLE);
						}
                    }
                }
            }

            public void btDiscoveryFinished() {
                if (mBluetoothDeviceList != null
                        && mBluetoothDeviceList.size() > 0) {
                    Toast.makeText(BluetoothTestActivity.this, R.string.text_pass,
                            Toast.LENGTH_SHORT).show();
                    storeRusult(true);

                } else {
                    Toast.makeText(BluetoothTestActivity.this, R.string.text_fail,
                            Toast.LENGTH_SHORT).show();
                    storeRusult(false);

                    mRetestButton.setVisibility(View.VISIBLE);

                }
                btTestUtil.stopTest();
                //finish();
            }
        };

        tvBtAddr = (TextView) findViewById(R.id.bt_addr_content);
        tvBtState = (TextView) findViewById(R.id.bt_state_content);
        tvBtDeviceList = (TextView) findViewById(R.id.tv_bt_device_list);

        /*SPRD bug 817253:Maybe cause NullPointerException.*/
        if(btTestUtil.getBluetoothAdapter() != null){
            tvBtAddr.setText(btTestUtil.getBluetoothAdapter().getAddress() + "\n");
        }else{
            tvBtAddr.setText("NA");
            Log.w(TAG, "onCreate mBluetoothAdapter == null");
        }
        //tvBtAddr.setText(btTestUtil.getBluetoothAdapter().getAddress() + "\n");


        mRetestButton = (Button) findViewById(R.id.retest_btn);
        mRetestButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Log.d(TAG, "mRetestButton");
                tvBtDeviceList.setText("");
                mBluetoothDeviceList.clear();
                mRetestButton.setVisibility(View.GONE);
                btTestUtil.stopTest();
                btTestUtil.startTest(BluetoothTestActivity.this);
            }
        });
    }

    public void onClick(View v){
        btTestUtil.stopTest();
        super.onClick(v);
    }

    @Override
    protected void onResume() {
        super.onResume();
        btTestUtil.startTest(this);
    }

    /*SPRD: fix bug408662 stop bluetooth rest on pause @{ */
    @Override
    protected void onPause() {
        super.onPause();
        btTestUtil.stopTest();
    }
    /* @}*/

    public void onBackPressed(){
        btTestUtil.stopTest();
        super.onBackPressed();
    }
}


