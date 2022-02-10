package com.sprd.validationtools.utils;

import android.util.Log;

public class NativeEncryptionChip {

     static {
        try {
            Log.d("ValidationToolsNative", " #loadLibrary jni_encryptionChip begin  ");
            System.loadLibrary("jni_encryptionChip");
        } catch (UnsatisfiedLinkError e) {
            Log.d("ValidationToolsNative", " #loadLibrary jni_encryptionChip failed  ");
            e.printStackTrace();
        }
    }
   //add for a3test begin
   public static native String native_encryptionChiptest();
   //add for a3test end

}

