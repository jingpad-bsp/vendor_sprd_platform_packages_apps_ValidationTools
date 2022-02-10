package com.sprd.validationtools.itemstest.backlight;

import java.util.Timer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemProperties;
import android.view.View;
import android.view.View.OnClickListener;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.R;
import com.sprd.validationtools.utils.ValidationToolsUtils;
import android.app.AlertDialog;
import android.widget.LinearLayout;

public class BackLightTest extends BaseActivity implements OnClickListener {
    private static final String TAG = "BackLightTest";
    PowerManager mPowerManager = null;
    TextView mContent;
    private static final int[] COLOR_ARRAY = new int[] { Color.WHITE,
            Color.BLACK };
    private boolean isShowNavigationBar = false;
    private int mIndex = 0, mCount = 5;
    Timer mTimer;
    private static final int TIMES = 5;
    private Handler mUiHandler = new Handler();;
    private Runnable mRunnable;

    protected RelativeLayout mRelativeLayout;
    protected Button passButton;
    protected Button failButton;

    /*SPRD bug 839657:Change screen light*/
    private static final int MAX_BRIGHTNESS = 255;
    private static final boolean TEST_SCREEN_LIGHT = true;
    private void setScreenLight(Activity context, int brightness) {
        WindowManager.LayoutParams lp = context.getWindow().getAttributes();
        lp.screenBrightness = Float.valueOf(brightness) * (1f / 255f);
        context.getWindow().setAttributes(lp);
    }
    private void startScreenLight(){
        try {
            if (isShowNavigationBar) {
                mRelativeLayout.setBackgroundColor(Color.WHITE);
            } else {
                mContent.setBackgroundColor(Color.WHITE);
            }
            setScreenLight(BackLightTest.this, MAX_BRIGHTNESS >> mCount);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setScreenLight(BackLightTest.this, MAX_BRIGHTNESS >> mCount);
        isShowNavigationBar = ValidationToolsUtils.hasNavigationBar(this);
        if (isShowNavigationBar) {
            setContentView(R.layout.background_layout);
            mRelativeLayout = (RelativeLayout) findViewById(R.id.background_relativelayout);
            passButton = (Button) findViewById(R.id.pass_btn);
            failButton = (Button) findViewById(R.id.fail_btn);
            passButton.setOnClickListener(this);
            failButton.setOnClickListener(this);
        } else {
            mContent = new TextView(this);
            setContentView(mContent);
        }
        setTitle(R.string.backlight_test);
        mPowerManager = (PowerManager) getSystemService(POWER_SERVICE);
        mRunnable = new Runnable() {
            public void run() {
                if(TEST_SCREEN_LIGHT){
                    startScreenLight();
                    mCount--;
                }else{
                    if (isShowNavigationBar) {
                        mRelativeLayout.setBackgroundColor(COLOR_ARRAY[mIndex]);
                    } else {
                        mContent.setBackgroundColor(COLOR_ARRAY[mIndex]);
                    }
                    mIndex = 1 - mIndex;
                    mCount--;
                }
                if (isShowNavigationBar) {
                    mPassButton.setVisibility(View.GONE);
                    mFailButton.setVisibility(View.GONE);
                }
                setBackground();
            }
        };
        setBackground();
    }

    @Override
    public void onClick(View v) {
        if (isShowNavigationBar) {
            if (v == passButton) {
                storeRusult(true);
                finish();
            } else if (v == failButton) {
				showDialog();
            }
        } else {
            if (v == mPassButton) {
                storeRusult(true);
                finish();
            } else if (v == mFailButton) {
                showDialog();
            }
        }
    }
	private void showDialog(){
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
			finish();
		    }  
		}); 
		Button continueBtn = (Button) resultDlgLayout.findViewById(R.id.continueButton);
		continueBtn.setText("继续测试"); 
		AlertDialog dialog=builder.show();
		continueBtn.setOnClickListener(new View.OnClickListener() {  
		    public void onClick(View v) {  
				storeRusult(false);  
				finish();
		    }  
		}); 
		//builder.create().show();
		} else {
			//mHandler.postDelayed(FinishRunable, 300);
			finish();
		}
	}
    @Override
    public void onResume() {
        super.onResume();
        if (isShowNavigationBar) {
            mPassButton.setVisibility(View.GONE);
            mFailButton.setVisibility(View.GONE);
            hideNavigationBar();
        }
    }

    private void setBackground() {
        if (mCount == 0) {
            if(TEST_SCREEN_LIGHT){
                setScreenLight(BackLightTest.this, MAX_BRIGHTNESS);
            }
            if (isShowNavigationBar) {
                passButton.setVisibility(View.VISIBLE);
                failButton.setVisibility(View.VISIBLE);
            }
            return;
        }
        mUiHandler.postDelayed(mRunnable, 1000);
    }

    @Override
    public void onDestroy() {
        mUiHandler.removeCallbacks(mRunnable);
        super.onDestroy();
    }
}
