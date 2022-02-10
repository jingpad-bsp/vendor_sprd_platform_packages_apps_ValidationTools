package com.android.adb;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

import com.sprd.validationtools.Const;

import android.content.Intent;
import android.os.EnvironmentEx;
import android.util.Log;

/**
 *
 * @author : zpa 
 *
 * @version : 1.0.0 
 *
 * 2017年4月21日  下午1:37:46
 *
 */
public class InitFileUtils {

	private static final boolean DEBUG=true;
	private static final String TAG="InitFileUtils";
	private static InitFileUtils mFileUtils;
	
	private File mSdcardFile=EnvironmentEx.getExternalStoragePath();
	//private File mSdcardFile=new File("/sdcard");
	public static final String INIT1_FILE="init1";
	public static final String INIT2_FILE="init2";
	
	public static final String CHANGE1_FILE="/productinfo/val_change1";
	public static final String CHANGE2_FILE="/productinfo/val_change2";
	public static final String RUNIN_FILE="/data/RUNINPhaseCheck";
	public static InitFileUtils getInstance(){
		if(mFileUtils==null){
			mFileUtils=new InitFileUtils();
		}
		return mFileUtils;
	}
	
	private InitFileUtils(){
		
	}
	
	public String readFile(int lines){
		if(mSdcardFile==null){
			if(DEBUG)Log.e(TAG,"customValidationTool : mSdcardFile ==null");
			return null;
		}
		String fileName="";
		if(Const.mode==0){
			fileName=INIT1_FILE;
		}else{
			fileName=INIT2_FILE;
		}
				
		File mInitFile= new File(mSdcardFile.getAbsolutePath()+File.separator+fileName);
		
		if(!mInitFile.exists()){
			if(DEBUG)Log.e(TAG,fileName+" file not exits");
			return null;
		}
		try {
			FileReader mReader = new FileReader(mInitFile);
			BufferedReader mBufferedReader = new BufferedReader(mReader);

			String buffer = mBufferedReader.readLine();

			String startActivity=mBufferedReader.readLine();
			if(lines==1){
				return buffer;
			}else if(lines==2){
				return buffer+";"+startActivity;
			}
			
			mReader.close();
			mBufferedReader.close();
			
		} catch (Exception e) {

			e.printStackTrace();
		}
		
		return null;
	}
	
	public void writeChange() throws IOException {
		String path = "";
		if (Const.mode == 0) {
			path = CHANGE1_FILE;
		} else {
			path = CHANGE2_FILE;
		}
		File mFile = new File(path);
		if(!mFile.exists()){
			mFile.createNewFile();
		}
		FileWriter mWriter=new FileWriter(mFile);
		mWriter.write("1");
		mWriter.close();
	}
	public void writeRUNINPhaseCheck(String result) throws IOException{
		File mFile = new File(RUNIN_FILE);
		if(!mFile.exists()){
			mFile.createNewFile();
		}
		FileWriter mWriter=new FileWriter(mFile);
		mWriter.write(result);
		mWriter.close();
	}
}
