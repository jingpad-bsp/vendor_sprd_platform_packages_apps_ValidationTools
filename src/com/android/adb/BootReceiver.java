package com.android.adb;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.android.adb.SystemPropInitService;
/**
 *
 * @author : zpa 
 *
 * @version : 1.0.0 
 *
 * 2017年3月27日  上午11:01:28
 *
 */
public class BootReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("BootReceiver","SystemPropInit :  BootReceiver");
		if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
			//Intent mIntent=new Intent();
			//mIntent.setClassName("com.android.adb", "com.android.adb.SystemPropInitService");
			Intent mIntent=new Intent(context, SystemPropInitService.class);
			context.startService(mIntent);
		}
		
	}

}
