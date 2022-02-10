package com.sprd.validationtools.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import android.content.Context;
import android.text.format.Formatter;
import android.os.Environment;
import android.os.EnvironmentEx;
import android.os.storage.StorageManager;
import com.sprd.validationtools.nonpublic.EnvironmentExProxy;
import android.util.Log;

public class StorageUtil {
    public static final String TAG = "StorageUtil";

    private static final int EXT_EMULATED_PATH = 0;
    private static final int EXT_COMMON_PATH = 1;
    private static final int OTG_UDISK_PATH = 2;

    /*
     * type:0 --- External storage(SD card) emulated app directory.
     * type:1 --- External storage(SD card) common app directory.
     * type:2 --- USB mass storage(OTG U disk) app directory.
     */
    public static String getExternalStorageAppPath(Context context, int type) {
        String extEmulatedPath = null;
        String extCommonPath = null;
        String extOtgUdiskPath = null;
        String otgUdiskPath = "null";

        List<File> allDirPaths = new ArrayList<>();
        Collections.addAll(allDirPaths, context.getExternalFilesDirs(null));

        File[] otgPaths = EnvironmentExProxy.getUsbdiskVolumePaths();
        for (File file : otgPaths) {
            if (file != null && Environment.MEDIA_MOUNTED.equals(EnvironmentExProxy.getUsbdiskVolumeState(file))) {
                Log.d(TAG, "otg udisk mounted, otg path is " + file.getPath());
                otgUdiskPath = file.getPath();
            } else {
                Log.i(TAG, "otg udisk not mounted, otg path is null");
                otgUdiskPath = "null";
            }
        }

        for (File file : allDirPaths) {
            if (file != null) {
                String path = file.getAbsolutePath();
                if (path.startsWith("/storage/emulated/0")) {
                    Log.d(TAG, "external storage emulated path is: " + path);
                    extEmulatedPath = path;
                } else if (path.startsWith(otgUdiskPath)) {
                    Log.d(TAG, "external storage otg udisk path is: " + path);
                    extOtgUdiskPath = path;
                } else {
                    Log.d(TAG, "external storage common path is: " + path);
                    extCommonPath = path;
                }
            }
        }

        if (type == EXT_EMULATED_PATH) {
            return extEmulatedPath;
        } else if (type == EXT_COMMON_PATH) {
            return extCommonPath;
        } else if (type == OTG_UDISK_PATH) {
            return extOtgUdiskPath;
        }else{
            Log.w(TAG, "type is incorrect!");
            return null;
        }
    }

    public static String getExternalStoragePathState(){
        return EnvironmentExProxy.getExternalStoragePathState();
    }

    public static String getInternalStoragePath(){
        return EnvironmentExProxy.getInternalStoragePath().getAbsolutePath();
    }


	/*

		//String ex_path = EnvironmentEx.getExternalStorageLinkPath().getAbsolutePath(); // /storage/sdcard0,
		//String in_path = Environment.getExternalStorageDirectory().getAbsolutePath();  // /storage/emulated/0,
		//String in2_path = EnvironmentEx.getEmulatedStoragePath().getAbsolutePath();    // /storage/self/emulated
		//Log.d(TAG, ", ex_path = " + ex_path + ", in_path = " + in_path + ", in2_path = " + in2_path);

	*/
	public static int getOTGcapacityFile(Context context, File f)
	{
		long totalBytes = f.getTotalSpace();
        long freeBytes = f.getFreeSpace();
        long usedBytes = totalBytes - freeBytes;                  

        String used = Formatter.formatFileSize(context, usedBytes);
        String total = Formatter.formatFileSize(context, totalBytes);   

		int idx = total.indexOf(".");
		String capacityStr;
	    if (idx == -1)
	        capacityStr = total.substring(0, total.length() - 3);
	    else
	        capacityStr = total.substring(0, idx);

        int capacity = Integer.valueOf(capacityStr);

		if (capacity <= 9)	capacity = 8;
		else if (capacity > 9 && capacity <= 17 )	capacity = 16;
		else if (capacity > 17 && capacity <= 33 )	capacity = 32;
		else if (capacity > 33 && capacity <= 65 )	capacity = 64;
		else if (capacity > 65 && capacity <= 129 )	capacity = 128;
		else if (capacity > 129 && capacity <= 257 )	capacity = 256;
		
        Log.i(TAG, "=== totalBytes = " + totalBytes + ", capacity = " + capacity + ", used = " + used + ", total = " + total + ", ===");
		
		return capacity;		
	}

	public static int getOTGcapacity(Context context, String otgPath)
	{
		File f = new File(otgPath);

        int capacity = getOTGcapacityFile(context, f);
		return capacity;				
	}
}
