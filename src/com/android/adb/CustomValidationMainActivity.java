package com.android.adb;

import com.sprd.validationtools.ValidationToolsMainActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 *
 * @author : zpa 
 *
 * @version : 1.0.0 
 *
 * 2017年4月26日  上午10:27:23
 *
 */
public class CustomValidationMainActivity extends Activity{
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Intent mIntent=new Intent();
		mIntent.setClass(this, ValidationToolsMainActivity.class);
		mIntent.putExtra("mode", 1);
		startActivity(mIntent);
		finish();
	}
	

}
