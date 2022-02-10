package com.sprd.validationtools.itemstest.storage;

import java.util.ArrayList;
import java.util.List;

import com.sprd.validationtools.BaseActivity;
import com.sprd.validationtools.R;

import android.os.Bundle;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import android.util.Log;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import android.content.Context;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.os.Environment;
import android.os.EnvironmentEx;
import android.content.IntentFilter;
import android.os.StatFs;
import java.io.File;
import java.util.Timer;
import android.view.View;
import java.util.TimerTask;
public class RomActivity extends BaseActivity {

	private LinearLayout container;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sim_card_test);
		setTitle(R.string.rom_test);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		container = (LinearLayout) findViewById(R.id.sim_test_result_container);
		showDevice();
		showDevice1();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void showDevice() {
	    String storage=null;
		long kb = getRomMemroy()[0];
        	long j=1024*1024*1024;
		int i=0;
        	while(kb/j>=1){
		   i++;
            	   j=j<<1;
        	}
		storage = (j>>30)+"G";		
		TextView tv = new TextView(this);
		tv.setText("ROM:"+storage);
		tv.setTextSize(24);
		if(((1<<i)+"G").equals(storage)){
			storeRusult(true);
		}else{
			storeRusult(false);
		}		
		container.addView(tv);
	}


    public long[] getRomMemroy() {  
        long[] romInfo = new long[2];  
        //Total rom memory  
        romInfo[0] = getTotalInternalMemorySize();  
  
        //Available rom memory  
        File path = Environment.getDataDirectory();  
        StatFs stat = new StatFs(path.getPath());  
        long blockSize = stat.getBlockSize();  
        long availableBlocks = stat.getAvailableBlocks();  
        romInfo[1] = blockSize * availableBlocks;  
        return romInfo;  
    }  
  
    public long getTotalInternalMemorySize() {  
        File path = Environment.getDataDirectory();  
        StatFs stat = new StatFs(path.getPath());  
        long blockSize = stat.getBlockSize();  
        long totalBlocks = stat.getBlockCount();  
        return totalBlocks * blockSize;  
    } 

		private void showDevice1() {
		TextView tv = new TextView(this);
		long ram_m = getTotalMemory(this);
		 if(ram_m > 7168){
		    tv.append("RAM:8G");
			tv.setTextSize(24);
			storeRusult(true);
		}
		else if(ram_m > 6144){
		    tv.append("RAM:7G");
			tv.setTextSize(24);
			storeRusult(true);
		}
		else if(ram_m > 5120){
		    tv.append("RAM:6G");
			tv.setTextSize(24);
			storeRusult(true);
		}
		else if(ram_m > 4096){
		    tv.append("RAM:5G");
			tv.setTextSize(24);
			storeRusult(true);
		}
		else if(ram_m > 3072){
		    tv.append("RAM:4G");
			tv.setTextSize(24);
			storeRusult(true);
		}
		else if(ram_m > 2048){
		    tv.append("RAM:3G");
			tv.setTextSize(24);
			storeRusult(true);
		}
		else if(ram_m > 1024){
			tv.append("RAM:2G");
			tv.setTextSize(24);
			storeRusult(true);
		}else if(ram_m > 768){
			tv.append("RAM:1G");
			tv.setTextSize(24);
			storeRusult(true);
		}else if(ram_m > 512){
			tv.append("RAM:768M");
			tv.setTextSize(24);
			storeRusult(true);
		}else{
			tv.append("RAM:ERROR:"+ram_m+"MB");
			storeRusult(false);
		}
		/*Timer t = new Timer();
		t.schedule(new TimerTask() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				finish();
			}
		}, 3000);*/
		container.addView(tv);
	}


    private long getTotalMemory(Context context){
        String str1 = "/proc/meminfo";
        String str2;
        String[] arrayOfString;
        long initial_memory = 0; 

        try 
        {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(
            localFileReader, 8192);
            str2 = localBufferedReader.readLine();

            arrayOfString = str2.split("\\s+");
            for (String num : arrayOfString) {
                Log.i(str2, num + "\t");
            }

            initial_memory = Integer.valueOf(arrayOfString[1]).intValue();
            localBufferedReader.close();
			Log.i("qyg---->initial_memory:", initial_memory + "\t");

        } catch (IOException e) {
			Log.i("qyg---->error:", initial_memory + "\t");
        }
        return initial_memory/(1024);
    }
}
