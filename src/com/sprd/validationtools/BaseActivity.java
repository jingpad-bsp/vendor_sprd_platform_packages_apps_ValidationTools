package com.sprd.validationtools;

import android.app.Activity;
import android.app.ProcessProtection;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import com.sprd.validationtools.sqlite.EngSqlite;

public class BaseActivity extends Activity implements OnClickListener {
    private static final String TAG = "BaseActivity";
    private AlertDialog mResultDialog;
    private String mTestname = null;
    private String mTestClassName = null;
    private EngSqlite mEngSqlite;
    public static boolean shouldCanceled = true;

    protected Button mPassButton;
    protected Button mFailButton;
    private static final int TEXT_SIZE = 30;
    protected boolean canPass = true;
    protected WindowManager mWindowManager;
    protected long time;

    private PhaseCheckParse mPhaseCheckParse = null;
    public static final String STATION_MMIT_VALUE = "MMI";
    public static final String STATION_AGING_VALUE = "AGING";
    public static final boolean SUPPORT_WRITE_STATION = true;
    public static final boolean SUPPORT_WRITE_ITEM_STATION = false;
    public static final int MISCDATA_USERSETION_OFFSET_BASE = 768 * 1024;
    public static final int MISCDATA_USERSETION_OFFSET_AGING = MISCDATA_USERSETION_OFFSET_BASE + 44;
    public static final int MISCDATA_USERSETION_OFFSET_RING_TUNE = MISCDATA_USERSETION_OFFSET_BASE + 74;
	protected boolean isTerminaled = false;
    private ProcessProtection mProcessProtection = null;
    protected boolean mSaveResult=true;
    private Handler mHandler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent=getIntent();
        if(intent!=null){
        	mSaveResult=intent.getBooleanExtra("save_result", true);
        }
        mTestname = this.getIntent().getStringExtra(Const.INTENT_PARA_TEST_NAME);
        mTestClassName = this.getIntent().getStringExtra(Const.INTENT_PARA_TEST_CLASSNAME);
        time = System.currentTimeMillis();

        mEngSqlite = EngSqlite.getInstance(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mWindowManager = getWindowManager();
        createButton(true);
        createButton(false);
        isTerminaled = false;
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        Log.d(TAG, "onCreate SUPPORT_WRITE_STATION=" + SUPPORT_WRITE_STATION);
        if (SUPPORT_WRITE_STATION) {
            mPhaseCheckParse = PhaseCheckParse.getInstance();
        }
        mProcessProtection = new ProcessProtection();
        mProcessProtection.setSelfProtectStatus(ProcessProtection.PROCESS_STATUS_PERSISTENT);
    }

    public void hideNavigationBar() {
        View decorView = getWindow().getDecorView();
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) {
            decorView.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            // for new api versions. View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    public int getHeight(Context context) {
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        display.getMetrics(dm);
        int height = dm.heightPixels;
        return height;
    }

    public int getRealHeight(Context context) {
        WindowManager windowManager = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            display.getRealMetrics(dm);
        } else {
            display.getMetrics(dm);
        }
        int realHeight = dm.heightPixels;
        return realHeight;
    }

    @Override
    protected void onDestroy() {
        if(mProcessProtection != null){
            mProcessProtection.setSelfProtectStatus(ProcessProtection.PROCESS_STATUS_IDLE);
        }
        removeButton();
        if (mTestname != null) {
            Log.d("APK_MMI",
                    "*********** " + mTestname + " Time: "
                            + (System.currentTimeMillis() - time) / 1000
                            + "s ***********");
        }
        super.onDestroy();
    }

    public void createButton(boolean isPassButton) {
        int buttonSize = getResources().getDimensionPixelSize(
                R.dimen.pass_fail_button_size);
        if (isPassButton) {
            mPassButton = new Button(this);
            mPassButton.setText(R.string.text_pass);
            mPassButton.setTextColor(Color.WHITE);
            mPassButton.setTextSize(TEXT_SIZE);
            mPassButton.setBackgroundColor(Color.GREEN);
            mPassButton.setOnClickListener(this);
        } else {
            mFailButton = new Button(this);
            mFailButton.setText(R.string.text_fail);
            mFailButton.setTextColor(Color.WHITE);
            mFailButton.setTextSize(TEXT_SIZE);
            mFailButton.setBackgroundColor(Color.RED);
            mFailButton.setOnClickListener(this);
        }

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION,
                // WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        lp.gravity = isPassButton ? Gravity.LEFT | Gravity.BOTTOM
                : Gravity.RIGHT | Gravity.BOTTOM;
        lp.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                | LayoutParams.FLAG_NOT_FOCUSABLE;
        lp.width = buttonSize;
        lp.height = buttonSize;
        mWindowManager.addView(isPassButton ? mPassButton : mFailButton, lp);
    }

    public void storeRusult(boolean isSuccess) {
	   if(!mSaveResult){
        	Log.d(TAG,"not save result");
        	return ;
        }
        Log.d(TAG, "storeResult classname:" + getClass().getName());
        Log.d(TAG, "storeResult mTestClassName:" + mTestClassName);
        if(TextUtils.isEmpty(mTestClassName)){
            mTestClassName = getClass().getName();
        }
        mEngSqlite.updateDB(mTestClassName, isSuccess ? Const.SUCCESS
                : Const.FAIL);
        Log.d(TAG, "onCreate storeRusult SUPPORT_WRITE_STATION="
                + SUPPORT_WRITE_STATION);
        if (SUPPORT_WRITE_STATION) {
            storePhaseCheck();
        }
    }

    private void storePhaseCheck() {
        String station = STATION_MMIT_VALUE;
        if (mPhaseCheckParse == null) {
            return;
        }

        Log.d(TAG,
                "storePhaseCheck: fail = " + mEngSqlite.queryFailCount()
                        + ", NotTest = " + mEngSqlite.queryNotTestCount());
        mPhaseCheckParse.writeStationTested(station);
        if (mEngSqlite.queryFailCount() == 0
                && mEngSqlite.queryNotTestCount() == 0) {
            mPhaseCheckParse.writeStationPass(station);
        } else {
            mPhaseCheckParse.writeStationFail(station);
        }
    }

    protected boolean mTestClikPass = false;
    @Override
    public void finish() {
        removeButton();
        /*SPRD bug 868665:Test can pass/fail must click button*/
			if(isTerminaled == false){
				Log.d("likenk", "isTerminaled == false");
        if (Const.isBoardISharkL210c10()) {
            Log.d(TAG, "finish isBoardISharkL210c10 is return!");
            int status = mEngSqlite.getTestListItemStatus(getClass().getName());
            Log.d(TAG, "status="+status);
            this.setResult(
                    mTestClikPass || status == Const.SUCCESS ? Const.TEST_ITEM_DONE
                            : 1, getIntent());
            super.finish();
            return;
        }
        /* @} */
        this.setResult(Const.TEST_ITEM_DONE, getIntent());
}else{
			 this.setResult(Const.FINISH_TEST_ITEM, getIntent());
			}
		Log.d("wu", "wu--->finish:isTerminaled:"+isTerminaled);
        super.finish();
    }

    protected void removeButton() {
        if(mPassButton != null){
            mWindowManager.removeView(mPassButton);
            mPassButton = null;
        }
        if(mFailButton != null){
            mWindowManager.removeView(mFailButton);
            mFailButton = null;
        }
    }

    @Override
    public void onBackPressed() {
        /* SPRD bug 760913:Test can pass/fail must click button */
        if (Const.isBoardISharkL210c10()) {
            Log.d("", "isBoardISharkL210c10 is return!");
            return;
        }
        /* @} */
        //Intent intent = BaseActivity.this.getIntent();
        //BaseActivity.this.startActivityForResult(intent, 0);
        //finish();
    }

    @Override
    public void onClick(View v) {
        if (v == mPassButton) {
            if (canPass) {
                Log.d("onclick", "pass.." + this);
                mTestClikPass = true;
                /*@}*/
                storeRusult(true);
                mHandler.postDelayed(FinishRunable, 300);
                //finish();
                } else {
                Toast.makeText(this, R.string.can_not_pass, Toast.LENGTH_SHORT).show();
            }
        } else if (v == mFailButton) {
            Log.d("onclick", "false.." + this);
           // mTestClikPass = false;
        	//storeRusult(false);
          //  mHandler.postDelayed(FinishRunable, 300);
            //finish();
		if(mSaveResult == true){
		mTestClikPass = false;
		storeRusult(false);
		LinearLayout resultDlgLayout = (LinearLayout) getLayoutInflater().inflate(
		  R.layout.result_dlg, null);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		String content = getString(R.string.phone_shutdown);
		TextView title = (TextView) resultDlgLayout.findViewById(R.id.title);
		title.setText(getResources().getString(R.string.alert_tip));
		TextView message = (TextView) resultDlgLayout.findViewById(R.id.message);
		message.setText(content);
		builder.setView(resultDlgLayout);
		Button shutdownBtn = (Button) resultDlgLayout.findViewById(R.id.positiveButton);
		shutdownBtn.setText(R.string.now_shutdown); 
		shutdownBtn.setOnClickListener(new View.OnClickListener() {  
		    public void onClick(View v) {  
		        Intent intent = new Intent(Intent.ACTION_REQUEST_SHUTDOWN);
                	startActivity(intent);
		    }  
		}); 
		Button backBtn = (Button) resultDlgLayout.findViewById(R.id.negativeButton);
		backBtn.setText(R.string.key_test_back); 
		backBtn.setOnClickListener(new View.OnClickListener() {  
		    public void onClick(View v) {  
			isTerminaled = true;
			Log.d("likenk", "qyg------->backBtn:isTerminaled:"+isTerminaled);
			//Intent intent = new Intent(BaseActivity.this, ValidationToolsMainActivity.class);
            //startActivity(intent);
			//mHandler.postDelayed(FinishRunable, 300);
			Log.d("likenk", "qyg------->finish()");
			 BaseActivity.this.finish();
		    }  
		}); 
		Button continueBtn = (Button) resultDlgLayout.findViewById(R.id.continueButton);
		continueBtn.setText("继续测试"); 
		AlertDialog dialog=builder.show();
		continueBtn.setOnClickListener(new View.OnClickListener() {  
		    public void onClick(View v) {  
				storeRusult(false);  
				BaseActivity.this.finish();
		    }  
		}); 
		//builder.create().show();
		} else {
			mHandler.postDelayed(FinishRunable, 300);
		}
}

}
    private Runnable FinishRunable = new Runnable() {
        public void run() {
            finish();
        }
    };
}
