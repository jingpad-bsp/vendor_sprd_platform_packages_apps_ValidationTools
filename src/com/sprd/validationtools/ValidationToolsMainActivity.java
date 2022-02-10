
package com.sprd.validationtools;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.SystemProperties;
import android.os.Build;
import android.os.UserHandle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.sprd.validationtools.modules.QrActivity;
import com.sprd.validationtools.background.BackgroundBtTest;
import com.sprd.validationtools.background.BackgroundGpsTest;
import com.sprd.validationtools.background.BackgroundSdTest;
import com.sprd.validationtools.background.BackgroundSimTest;
import com.sprd.validationtools.background.BackgroundTest;
import com.sprd.validationtools.background.BackgroundTestActivity;
import com.sprd.validationtools.background.BackgroundWifiTest;
import com.sprd.validationtools.itemstest.ListItemTestActivity;
import com.sprd.validationtools.itemstest.TestResultActivity;
import com.sprd.validationtools.modules.AutoTestItemList;
import com.sprd.validationtools.modules.TestItem;
import com.sprd.validationtools.sqlite.EngSqlite;
import com.sprd.validationtools.testinfo.TestInfoMainActivity;
import com.sprd.validationtools.utils.ValidationToolsUtils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.content.ComponentName;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.PowerManager;
import android.widget.TextView;
import android.widget.Button;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.os.PowerManager.WakeLock;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;

public class ValidationToolsMainActivity extends Activity implements
        AdapterView.OnItemClickListener {
    private final static String TAG = "ValidationToolsMainActivity";
    private final static int FULL_TEST = 0;
    private final static int UNIT_TEST = 1;
    private final static int TEST_INFO = 2;
    private final static int CAMERA_CALI_VERIFY = 3;
    private final static int RESET = 4;
	private final static int AUDIO_TEST=5;
	private final static int AGING_TEST = 6;
    private final static boolean SUPPORT_CAMERA_FEATURE = true;
    private String[] mListItemString;
    private ListView mListView;
    private ArrayList<TestItem> mAutoTestArray = null;
    private int mAutoTestCur = 0;
    private int mUserId;

    private ArrayList<BackgroundTest> mBgTest = null;

    private  boolean mIsTested = false;
    public final static String IS_SYSTEM_TESTED = "is_system_tested";
    private SharedPreferences mPrefs;
    private long time = 0;
    //Save full test used time
    public final static String FULL_TEST_USED_TIME = "fulltest_used_time";
    private long mFullTestUsedtime = 0;
    private PhaseCheckParse mPhaseCheckParse = null;
    private PowerManager.WakeLock mWakeLock;

    public final static String ACTION_CAMERA_CALI_VERUFY = "com.sprd.cameracalibration.START_CAMERACALIBRATION";
    public final static String EXTRA_GET_PHASECHECK = "phasecheck_result";

    private ArrayAdapter<String> mArrayAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "oncreate start!");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validation_tools_main);
        if(SUPPORT_CAMERA_FEATURE && Const.isSupportCameraCaliVeri()){
            mListItemString = new String[] {
                    this.getString(R.string.full_test),
                    this.getString(R.string.item_test),
                    this.getString(R.string.test_info),
                    this.getString(R.string.camera_cali_verify),
                    this.getString(R.string.reset),
					this.getString(R.string.audio_test),
					this.getString(R.string.aging_test),
                    this.getString(R.string.qr_code)
            };
        }else{
            mListItemString = new String[] {
                    this.getString(R.string.full_test),
                    this.getString(R.string.item_test),
                    this.getString(R.string.test_info),
                    this.getString(R.string.reset),
					this.getString(R.string.audio_test),
					this.getString(R.string.aging_test),
                    this.getString(R.string.qr_code)
					
            };
        }
		Intent intent=getIntent();
        if(intent!=null){
        	 Const.mode=intent.getIntExtra("mode", 0);
        }
		if(Const.mode==0){
        	setTitle(getResources().getString(R.string.mode_mmi_one)+ " test");
        }else if(Const.mode==1){
        	setTitle(getResources().getString(R.string.mode_mmi_second)+ " test");
        }else if(Const.mode==2){
            setTitle("SMT test");
        }else if(Const.mode==3){
            setTitle(getResources().getString(R.string.mode_mmi_second)+ " test");
        }
        Log.d(TAG,"mode : "+Const.mode);
        mListView = (ListView) findViewById(R.id.ValidationToolsList);
        mArrayAdapter = new ArrayAdapter<String>(this,
                R.layout.simple_list_item, mListItemString);

        mListView.setAdapter(mArrayAdapter);
        mListView.setOnItemClickListener(this);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mIsTested = mPrefs.getBoolean(IS_SYSTEM_TESTED, false);
        mUserId = UserHandle.myUserId();
        mPhaseCheckParse = PhaseCheckParse.getInstance();
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        ValidationToolsUtils.parsePCBAConf();
        startValidationToolsService(this, true);
        PowerManager localPowerManager = (PowerManager)getSystemService("power");
        if (this.mWakeLock == null) {
          this.mWakeLock = localPowerManager.newWakeLock(26, getPackageName());
        }
        this.mWakeLock.acquire();
        startADBD();
        registerBroadCast();
        registerBroadCastReadStation();
    }
    public void startADBD(){
    	SystemProperties.set("persist.sys.adb.debug","1");
    	Settings.Global.putInt(getContentResolver(),Settings.Global.ADB_ENABLED, 1);
    }

    @Override
    public void onPause() {
        if(mUserId==0){
            saveTestInfo();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        //stop service
        startValidationToolsService(this, false);
        unRegistBroadCast();
        unRegistBroadCastReadStation();
        if (this.mWakeLock.isHeld())
        {
          this.mWakeLock.release();
          this.mWakeLock = null;
        }
        super.onDestroy();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == Const.TEST_ITEM_DONE) {
            autoTest();
        }
    }

    private void startValidationToolsService(Context context,boolean startService){
        if(context == null) return;
        Intent intent = new Intent(context, ValidationToolsService.class);
        if(startService){
            intent.setFlags(ValidationToolsService.FLAG_START_FOREGROUND);
            context.startService(intent);
        }else{
            intent.setFlags(ValidationToolsService.FLAG_STOP_FOREGROUND);
            context.stopService(intent);
        }
    }

    private void storePhaseCheck() {
        try {
            //String station = BaseActivity.STATION_MMIT_VALUE;
            if(mPhaseCheckParse == null){
                return;
            }
            EngSqlite engSqlite = EngSqlite.getInstance(this);
            if (engSqlite == null){
                return;
            }
            Log.d(TAG, "storePhaseCheck: fail = "+engSqlite.queryFailCount() + ", NotTest = " + engSqlite.queryNotTestCount());

            if (engSqlite.queryFailCount() == 0 && engSqlite.queryNotTestCount()== 0) {
                if(Const.mode == 1){
                    mPhaseCheckParse.writeStationTested(Const.MMI2_STATION_INDEX);
                    mPhaseCheckParse.writeStationPass(Const.MMI2_STATION_INDEX);
                }else{
                    mPhaseCheckParse.writeStationTested(Const.MMI1_STATION_INDEX);
                    mPhaseCheckParse.writeStationPass(Const.MMI1_STATION_INDEX);		
                }
            }else {
                if(Const.mode == 1){
                    mPhaseCheckParse.writeStationTested(Const.MMI2_STATION_INDEX);
                    mPhaseCheckParse.writeStationFail(Const.MMI2_STATION_INDEX);
                }else{
                    mPhaseCheckParse.writeStationTested(Const.MMI1_STATION_INDEX);
                    mPhaseCheckParse.writeStationFail(Const.MMI1_STATION_INDEX);		
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void autoTest() {
        if (mAutoTestArray != null) {
            Log.d(TAG, "autoTest: mAutoTestArray.size() = " + mAutoTestArray.size() + ", mAutoTestCur = " + mAutoTestCur);
        }
        if (mAutoTestArray != null && mAutoTestCur < mAutoTestArray.size()) {
            Log.d(TAG, "autoTest1 ");
            Intent intent = new Intent();
            intent.setClassName(this, mAutoTestArray.get(mAutoTestCur).getTestClassName());
            intent.putExtra(Const.INTENT_PARA_TEST_NAME,
                    mAutoTestArray.get(mAutoTestCur).getTestName());
            intent.putExtra(Const.INTENT_PARA_TEST_INDEX, mAutoTestCur);
            startActivityForResult(intent, 0);

            mAutoTestCur++;
        } else if (mBgTest != null && mAutoTestArray != null) {
            Log.d(TAG, "autoTest2 ");
            EngSqlite engSqlite = EngSqlite.getInstance(this);
            //addFailedBgTestToTestlist();
            StringBuffer buffer = new StringBuffer("");
            buffer.append(getResources().getString(R.string.bg_test_notice) + "\n\n");
            for (BackgroundTest bgTest : mBgTest) {
                bgTest.stopTest();
                engSqlite.updateDB(bgTest.getTestItem(getApplicationContext()).getTestClassName(),
                        bgTest.getResult() == BackgroundTest.RESULT_PASS ? Const.SUCCESS
                                : Const.FAIL);
                buffer.append(bgTest.getResultStr());
                buffer.append("\n\n");
            }

            //Restore pharsecheck.
            storePhaseCheck();

            Intent intent = new Intent(ValidationToolsMainActivity.this,
                    BackgroundTestActivity.class);
            intent.putExtra(Const.INTENT_BACKGROUND_TEST_RESULT, buffer.toString());
            startActivityForResult(intent, 0);
            mBgTest = null;
        } else {
            Log.d(TAG, "autoTest3 ");
            mFullTestUsedtime = System.currentTimeMillis() - time;
            saveFullTestUsedTime();
            Intent intent = new Intent(ValidationToolsMainActivity.this,
                    TestResultActivity.class);
            intent.putExtra("start_time", mFullTestUsedtime);
            startActivity(intent);
        }
    }

    private void addFailedBgTestToTestlist() {
        for (BackgroundTest bgTest : mBgTest) {
            if (bgTest.getResult() != BackgroundTest.RESULT_PASS) {
                TestItem item = bgTest.getTestItem(getApplicationContext());
                mAutoTestArray.add(item);
            }
        }
    }

    private void startBackgroundTest() {
        mBgTest = new ArrayList<BackgroundTest>();
        mBgTest.add(new BackgroundBtTest(this));
        mBgTest.add(new BackgroundWifiTest(this));
        mBgTest.add(new BackgroundGpsTest(this));
    //    mBgTest.add(new BackgroundSimTest(this));
    //    mBgTest.add(new BackgroundSdTest(this));
        for (BackgroundTest bgTest : mBgTest) {
            bgTest.startTest();
        }
    }

    public void saveTestInfo() {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(IS_SYSTEM_TESTED, mIsTested);
        editor.apply();
    }

    public void saveFullTestUsedTime() {
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putLong(FULL_TEST_USED_TIME, mFullTestUsedtime);
        editor.apply();
    }

    public void onResume(){
        super.onResume();
    }

    @Override
    public void onItemClick(AdapterView l, View v, int position, long id) {
        /* SPRD: bug453083 ,Multi user mode, set button is not click. {@ */
        if (mUserId != 0) {
            Toast.makeText(getApplicationContext(), R.string.multi_user_hint, Toast.LENGTH_LONG).show();
            return;
        }
        /* @} */
        Log.d(TAG, "position:"+position+",id="+id);
        if(mArrayAdapter != null){
            String clickItem = mArrayAdapter.getItem(position);
            Log.d(TAG, "clickItem:"+clickItem);
            if(getString(R.string.full_test).equals(clickItem)){
                time = System.currentTimeMillis();
                mAutoTestArray = AutoTestItemList.getInstance(this).getTestItemList();
        	LinearLayout resultDlgLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.confirm_dlg, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String content = getString(R.string.alert_content);
		TextView title = (TextView) resultDlgLayout.findViewById(R.id.title);
		title.setText(getResources().getString(R.string.alert_title));
		TextView message = (TextView) resultDlgLayout.findViewById(R.id.message);
		message.setText(content);
		builder.setView(resultDlgLayout);
		final AlertDialog alertDialog = builder.create();
		Button passBtn = (Button) resultDlgLayout.findViewById(R.id.positiveButton);
		passBtn.setText(R.string.alert_ok); 
		passBtn.setOnClickListener(new View.OnClickListener() {  
		    public void onClick(View v) {  
			//mAutoTestArray = AutoTestItemList.getInstance(this).getTestItemList();
                mAutoTestCur = 0;
                mIsTested = true;
                if (Const.isBoardISharkL210c10()) {
                    //
                } else {
                    //startBackgroundTest();
                }
		PhaseCheckParse parse = PhaseCheckParse.getInstance();
		if(Const.mode == 1){
			parse.writeStationTested(Const.MMI2_STATION_INDEX);
			parse.writeStationFail(Const.MMI2_STATION_INDEX);
		}else{
			parse.writeStationTested(Const.MMI1_STATION_INDEX);
			parse.writeStationFail(Const.MMI1_STATION_INDEX);		
		}
                autoTest();
				alertDialog.dismiss();
		    }  
		}); 
		Button failBtn = (Button) resultDlgLayout.findViewById(R.id.negativeButton);
		failBtn.setText(R.string.alert_cancel); 
		failBtn.setOnClickListener(new View.OnClickListener() {  
		    public void onClick(View v) {
			//mClickCount = 0;  
			alertDialog.dismiss();
		    }  
		}); 
		alertDialog.show();
            }else if(getString(R.string.item_test).equals(clickItem)){
                Intent intent = new Intent(this, ListItemTestActivity.class);
                startActivity(intent);
            }else if(getString(R.string.test_info).equals(clickItem)){
                Intent intent = new Intent(this, TestInfoMainActivity.class);
                intent.putExtra(IS_SYSTEM_TESTED, mIsTested);
                startActivity(intent);
            }else if(getString(R.string.camera_cali_verify).equals(clickItem)){
                launcherCameraCaliVerify();
            }else if(getString(R.string.reset).equals(clickItem)){
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.reset)
                .setMessage(R.string.factory_reset_message)
                .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_FACTORY_RESET);
                        intent.setPackage("android");
                        intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                        intent.putExtra(Intent.EXTRA_REASON, "MasterClearConfirm");
                        intent.putExtra(Intent.EXTRA_WIPE_EXTERNAL_STORAGE, false);
                        ValidationToolsMainActivity.this.sendBroadcast(intent);
                    }
                })
                .setPositiveButton(android.R.string.cancel, null);
                builder.show();
        }else if (getString(R.string.audio_test).equals(clickItem)) {
                Intent intent=new Intent(Intent.ACTION_MAIN);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                ComponentName cn=new ComponentName("pepsl.com.recordwavrf64","pepsl.com.recordwavrf64.MainActivity");
                intent.setComponent(cn);
                startActivity(intent);
        }else if (getString(R.string.aging_test).equals(clickItem)) {
                if (("user".equals(Build.TYPE) && mPhaseCheckParse.isStationPass("ANT")) || "userdebug".equals(Build.TYPE)) {
                    Intent intent = new Intent(Intent.ACTION_MAIN);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ComponentName cn = new ComponentName("com.sprd.runtime", "com.sprd.runtime.RuntimeTestMain");
                    intent.setComponent(cn);
                    startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(), R.string.mt_not_test,
                            Toast.LENGTH_LONG).show();
                }
        }else if (getString(R.string.qr_code).equals(clickItem)) {
                Intent intent = new Intent(this, QrActivity.class);
                startActivity(intent);
        }
    
    }
}

    @Override
    public void onBackPressed() {
        if (mUserId != 0) {
            finish();
            return;
        }
        if (!SystemProperties.get("ro.bootmode").contains("engtest")) {
            super.onBackPressed();
        }
    }

    private void launcherCameraCaliVerify(){
        try {
            String phasecheck = PhaseCheckParse.getInstance().getPhaseCheck();
            Intent intent = new Intent(ACTION_CAMERA_CALI_VERUFY);
            intent.putExtra(EXTRA_GET_PHASECHECK, phasecheck);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    //adb shell am start -n com.sprd.validationtools/.ValidationToolsMainActivity
    //adb shell am broadcast -a write_station --es name "***" --es result "0", *** stationName, 0 pass, 1 fail
    private void registerBroadCast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("write_station");
        registerReceiver(WrinteStationBroadcast, intentFilter);
    }

    private void unRegistBroadCast() {
        if (WrinteStationBroadcast != null) {
            unregisterReceiver(WrinteStationBroadcast);
        }

    }

    private BroadcastReceiver WrinteStationBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "onReceive action = " + action);
            if (action == null)
                return;
            if (action.equals("write_station")) {
                String stationName = intent.getStringExtra("name");
                String stationResult = intent.getStringExtra("result");
                Log.d(TAG, "onReceive stationName = " + stationName + ", stationResult = " + stationResult);
                if ("0".equals(stationResult)) {
                    PhaseCheckParse.getInstance().writeStationTested(stationName);
                    PhaseCheckParse.getInstance().writeStationPass(stationName);
                } else if ("1".equals(stationResult)) {
                    PhaseCheckParse.getInstance().writeStationTested(stationName);
                    PhaseCheckParse.getInstance().writeStationFail(stationName);
                }
            }
        }
    };

    //adb shell am start -n com.sprd.validationtools/.ValidationToolsMainActivity
    //adb shell am broadcast -a read_station --es name "DualCam"
    //adb shell getprop persist.sys.station.state
    private void registerBroadCastReadStation() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("read_station");
        registerReceiver(ReadStationBroadcast, intentFilter);
    }

    private void unRegistBroadCastReadStation() {
        if (ReadStationBroadcast != null) {
            unregisterReceiver(ReadStationBroadcast);
        }

    }

    private BroadcastReceiver ReadStationBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String result = null;
            String stationResult = null;
            StringBuffer sb = new StringBuffer();
            Log.d(TAG, "ReadStationBroadcast onReceive action = " + action);
            if (action == null)
                return;
            if (action.equals("read_station")) {
                String stationName = intent.getStringExtra("name");
                result = PhaseCheckParse.getInstance().getPhaseCheckState(stationName);
                sb.append(stationName+ ":" + result);
                stationResult = sb.toString();
                Log.d(TAG, "ReadStationBroadcast onReceive stationName = " + stationName + ", result = " + result + " ; sb = " + sb);
                SystemProperties.set("persist.sys.station.state",stationResult);

                String type = SystemProperties.get("persist.sys.station.state", "unknown");
                Log.d(TAG, "ReadStationBroadcast onReceive type = " + type);
            }
        }
    };
}
