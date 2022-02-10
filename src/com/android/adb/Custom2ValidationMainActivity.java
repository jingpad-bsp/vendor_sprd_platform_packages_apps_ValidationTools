package com.android.adb;

import com.sprd.validationtools.ValidationToolsMainActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import com.sprd.validationtools.R;
import com.sprd.validationtools.PhaseCheckParse;
/**
 *
 * @author : zpa 
 *
 * @version : 1.0.0 
 *
 * 2017年4月26日  上午10:27:23
 *
 */
public class Custom2ValidationMainActivity extends Activity{

        private PhaseCheckParse mPhaseCheckParse = null;
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Intent mIntent=new Intent();
		mPhaseCheckParse = PhaseCheckParse.getInstance();
                if (mPhaseCheckParse.isStationPass("RUNIN")){
		mIntent.setClass(this, ValidationToolsMainActivity.class);
		mIntent.putExtra("mode", 1);
		startActivity(mIntent);
                finish();
                }else{
                    Toast.makeText(getApplicationContext(), R.string.aging_not_test,
                            Toast.LENGTH_LONG).show();
                finish();
                }

	}
	

}
