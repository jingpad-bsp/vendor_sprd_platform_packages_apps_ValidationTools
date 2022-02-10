package com.android.adb;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import com.sprd.validationtools.Const;
import com.sprd.validationtools.PhaseCheckParse;
import com.sprd.validationtools.modules.TestItem;
import com.sprd.validationtools.itemstest.TestResultActivity;
import com.sprd.validationtools.sqlite.EngSqlite;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.EnvironmentEx;
import android.os.IBinder;
import android.os.SystemProperties;
import android.os.DropBoxManager.Entry;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

/**
 *
 * @author : zpa 
 *
 * @version : 1.0.0 
 *
 * 2017年3月27日  上午11:02:44
 *
 */
public class SystemPropInitService extends Service{

	static final String  TAG="SystemPropInitService";
	static final boolean DEBUG=true;
	static final String PROP_INIT_FILENAME="system_prop.conf";
	
	static final String PROP_ADB_DEBUG="persist.sys.adb.debug";
	private File mSdcardFile=EnvironmentEx.getExternalStoragePath();
	
	public static final String SYSTEM_TEST_RESULT_ACTION="com.android.systemtest.result";
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void onCreate(){
		
		new LoadSystemPropThread().start();
		
	}
	
	public void onDestory(){
		super.onDestroy();
	}
	private class LoadSystemPropThread extends Thread{
		
		public void run(){
			try {
				adbSetProp();
				/*int mode=Const.mode;
				Const.mode=0;
				customValidationTool();
				Const.mode=1;
				customValidationTool();
				Const.mode=mode;*/
			} catch (Exception e) {
				stopSelf();
			}
		}
	}
	public void adbSetProp(){
		
		if(mSdcardFile==null){
			if(DEBUG)Log.d(TAG,"external storage path not exits");
			return  ;
		}
		
		File mPropFile= new File(mSdcardFile.getAbsolutePath()+File.separator+PROP_INIT_FILENAME);
		
		if(!mPropFile.exists()){
			if(DEBUG)Log.d(TAG,"system_prop.conf file not exits");
			return ;
		}
		
		HashMap<String, String> mProp=new HashMap<String,String>();
		
		try {
			FileReader mFileReader = new FileReader(mPropFile);
			BufferedReader mBufferedReader=new BufferedReader(mFileReader);
			String line="";
			while((line=mBufferedReader.readLine())!=null){
				String tmpLine=line.trim();
				if(tmpLine.startsWith("#")){
					if(DEBUG)Log.d(TAG,"ignore : "+tmpLine);
					continue;
				}else{
					if(tmpLine.indexOf('=') ==-1){
						Log.e(TAG, "line must have = , ignore :"+tmpLine);
						continue;
					}
					String props[]=tmpLine.split("=");
					
					
					if(props.length > 2  ){
						if(DEBUG)Log.d(TAG,"line not equals xxx=xxx , ignore  : "+tmpLine);
						continue;
					}else{
						if(props[0].startsWith("ro.")){
							Log.e(TAG,"can't set ro.* props ,ignore : "+tmpLine);
							continue;
						}
						if(props.length==1){
							mProp.put(props[0], "");
						}else if(props.length==2){
							mProp.put(props[0], props[1]);
						}else{
							Log.e(TAG,"error prop.length : "+props.length);
						}
					}
				}
			}
			mFileReader.close();
			mBufferedReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		
		if(mProp.size()==0){
			if(DEBUG)Log.d(TAG,"read "+PROP_INIT_FILENAME +" properties size =0");
			return ;
		}
		
		Set<String> mPropSet=mProp.keySet();
		
		for(String prop : mPropSet){
			SystemProperties.set(prop,mProp.get(prop));
		}
		
		if(mProp.containsKey(PROP_ADB_DEBUG)){
			if(mProp.get(PROP_ADB_DEBUG).equals("1")){
				Settings.Global.putInt(getContentResolver(),Settings.Global.ADB_ENABLED, 1);
			}else{
				Settings.Global.putInt(getContentResolver(),Settings.Global.ADB_ENABLED, 0);
			}
		}
		if(DEBUG)Log.d(TAG,"SetProp Success : "+mProp.size());
	}
	
	public void customValidationTool(){
		InitFileUtils mInitFile=InitFileUtils.getInstance();
		String readConfig=mInitFile.readFile(2);
		if(readConfig==null){
			if(DEBUG)Log.e(TAG,"customValidationTool readConfig=null");
			stopSelf();
			return ;
		}
		String config[]=readConfig.split(";");
		if(config.length==2){
			if(config[1]!=null){
				String pkg[]=config[1].split(",");
				if(pkg.length==2){
					Intent mIntent=new Intent();
					mIntent.setClassName(pkg[0].trim(), pkg[1].trim());
					mIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					startActivity(mIntent);
				}
			}
			
		}
		if(config[0]==null){
			Log.e(TAG,"customValidationTool config[0]=null");
			stopSelf();
			return ;
		}
		
		try {
			mInitFile.writeChange();
		} catch (Exception e) {
			e.printStackTrace();
		}
		String tmpStr[]=config[0].split(",");
		setOldTest(tmpStr[tmpStr.length-1].trim());
	}
	
	
	public void setOldTest(String value){
		
		if(DEBUG)Log.e(TAG,"setOldTest value:"+value);
		if(value.equals("1")){
			PhaseCheckParse parse = PhaseCheckParse.getInstance();
			parse.writeStationTested(Const.RUN_STATION_INDEX);
			parse.writeStationPass(Const.RUN_STATION_INDEX);
			
		}else if (value.equals("-1")){
			PhaseCheckParse parse = PhaseCheckParse.getInstance();
			parse.writeStationTested(Const.RUN_STATION_INDEX);
			parse.writeStationFail(Const.RUN_STATION_INDEX);
		}
		
	}

}
