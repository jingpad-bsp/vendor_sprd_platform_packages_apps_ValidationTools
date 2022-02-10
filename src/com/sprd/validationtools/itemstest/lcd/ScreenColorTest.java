
package com.sprd.validationtools.itemstest.lcd;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.Const;
import com.sprd.validationtools.R;
import com.sprd.validationtools.utils.ValidationToolsUtils;
import android.widget.LinearLayout;
import android.app.AlertDialog;

public class ScreenColorTest extends BaseActivity implements OnClickListener {
    private String TAG = "ScreenColorTest";
    TextView mContent;

    protected RelativeLayout mRelativeLayout;
    protected Button passButton;
    protected Button failButton;

    int mIndex = 0, mCount = 0;
    private boolean isShowNavigationBar = false;
    private Handler mUiHandler = new Handler();
    private Runnable mRunnable;
    private TextView mtips;
    Drawable[] bgs = null;
    int black = 0;
    int blue = 3;
    int green = 4;
    int i = 0;
    int red = 2;
    int white = 1;

    private static final int[] COLOR_ARRAY = new int[] {
            Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW, Color.WHITE
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        isShowNavigationBar = ValidationToolsUtils.hasNavigationBar(this);
        bgs = new Drawable[]{new ColorDrawable(Color.BLACK), new ColorDrawable(Color.WHITE), new ColorDrawable(Color.RED), new ColorDrawable(Color.BLUE), new ColorDrawable(Color.GREEN), getResources().getDrawable(R.drawable.lcdfirst), getResources().getDrawable(R.drawable.lcdsecond), getResources().getDrawable(R.drawable.lcdthird), getResources().getDrawable(R.drawable.lcdforth)};
        if (isShowNavigationBar) {
            setContentView(R.layout.background_layout2);
            mRelativeLayout = (RelativeLayout) findViewById(R.id.background_relativelayout);
            mtips = (TextView)findViewById(R.id.tips);
            mRelativeLayout.setOnClickListener(this);
            passButton = (Button) findViewById(R.id.pass_btn);
            failButton = (Button) findViewById(R.id.fail_btn);
            passButton.setOnClickListener(this);
            failButton.setOnClickListener(this);
			passButton.setVisibility(View.GONE);
            failButton.setVisibility(View.GONE);
        } else {
            mContent = new TextView(this);
            mContent.setGravity(Gravity.CENTER);
            mContent.setTextSize(35);
            setContentView(mContent);
        }
        changeBackground();
    }

    private void changeBackground(){
        Drawable[] drawableArr = this.bgs;
        int i2 = i;
        i = i2 + 1;
        mRelativeLayout.setBackground(bgs[i2]);
        if(i == 1){
            mtips.setVisibility(View.VISIBLE);
        }else {
            mtips.setVisibility(View.GONE);
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

    @Override
    public void onClick(View v) {
        if (isShowNavigationBar) {
            if (v == passButton) {
                storeRusult(true);
                finish();
            } else if (v == passButton) {
                 showDialog();
            } else {
                if(i == bgs.length){
                    passButton.setVisibility(View.VISIBLE);
                    failButton.setVisibility(View.VISIBLE);
                }else {
                    changeBackground();
                }
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
    private void setBackground() {
        if (mIndex >= COLOR_ARRAY.length) {
            if (isShowNavigationBar) {
                passButton.setVisibility(View.VISIBLE);
                failButton.setVisibility(View.VISIBLE);
                hideNavigationBar();
            } else {
                mPassButton.setVisibility(View.VISIBLE);
                mFailButton.setVisibility(View.VISIBLE);
            }
            return;
        }

        if (isShowNavigationBar) {
            mRelativeLayout.setBackgroundColor(COLOR_ARRAY[mIndex]);
        } else {
            mContent.setBackgroundColor(COLOR_ARRAY[mIndex]);
        }

        /* SPRD Bug 771296:LCD screen test, white screen continue 3 seconds. @{ */
        if (Const.isBoardISharkL210c10()) {
            if (mIndex == 0) {
                mUiHandler.postDelayed(mRunnable, 3000);
            } else {
                mUiHandler.postDelayed(mRunnable, 1000);
            }
        } else {
            mUiHandler.postDelayed(mRunnable, 1200);
        }
        /* @} */
    }

    @Override
    public void onDestroy() {
        mUiHandler.removeCallbacks(mRunnable);
        super.onDestroy();
    }
}
