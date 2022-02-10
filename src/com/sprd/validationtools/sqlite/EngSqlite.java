package com.sprd.validationtools.sqlite;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;
import android.widget.Toast;
import android.os.Debug;

import com.sprd.validationtools.Const;
import com.sprd.validationtools.modules.TestItem;
import com.sprd.validationtools.modules.UnitTestItemList;

public class EngSqlite {
    private static final String TAG = "EngSqlite";
    private Context mContext;
    private SQLiteDatabase mSqLiteDatabase = null;
    private SQLiteDatabase mSqLiteDatabase2 = null;
    private SQLiteDatabase mSqLiteDatabase3 = null;

    public static final String ENG_ENGTEST_DB = Const.PRODUCTINFO_DIR
            + "/mmitest.db";
    public static final String ENG_ENGTEST_DB2 = Const.PRODUCTINFO_DIR
            + "/mmitest2.db";
    public static final String ENG_ENGTEST_DB3 = Const.PRODUCTINFO_DIR
            + "/smttest.db";
    public static final String ENG_STRING2INT_TABLE = "str2int";
    public static final String ENG_STRING2INT_NAME = "name";
    public static final String ENG_STRING2INT_DISPLAYNAME = "displayname";
    public static final String ENG_STRING2INT_VALUE = "value";
    public static final String ENG_GROUPID_VALUE = "groupid";
    public static final int ENG_ENGTEST_VERSION = 1;

    private static EngSqlite mEngSqlite;

    public static synchronized EngSqlite getInstance(Context context) {
        if (mEngSqlite == null) {
            mEngSqlite = new EngSqlite(context);
        }
        return mEngSqlite;
    }

    private EngSqlite(Context context) {
        mContext = context;
        /*sprd: add for bug 411514 @{ */
        String filePath = new String(ENG_ENGTEST_DB);
        String filePath2 = new String(ENG_ENGTEST_DB2);
        String filePath3 = new String(ENG_ENGTEST_DB3);
        File file = new File(filePath);
        File file2 = new File(filePath2);
        File file3 = new File(filePath3);
        /* @} */
        Process p = null;
        DataOutputStream os = null;
		
        try {
            p = Runtime.getRuntime().exec("chmod 777 productinfo");
            os = new DataOutputStream(p.getOutputStream());
            BufferedInputStream err = new BufferedInputStream(p.getErrorStream());
            BufferedReader br = new BufferedReader(new InputStreamReader(err));
            Log.v("Vtools", "os= " + br.readLine());
            Runtime.getRuntime().exec("chmod 777 " + file.getAbsolutePath());
            Runtime.getRuntime().exec("chmod 777 " + file2.getAbsolutePath());
            Runtime.getRuntime().exec("chmod 777 " + file3.getAbsolutePath());
            int status = p.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                    p.destroy();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            ValidationToolsDatabaseHelper databaseHelper = new ValidationToolsDatabaseHelper(mContext,ENG_ENGTEST_DB);
            ValidationToolsDatabaseHelper databaseHelper2 = new ValidationToolsDatabaseHelper(mContext,ENG_ENGTEST_DB2);
            ValidationToolsDatabaseHelper databaseHelper3 = new ValidationToolsDatabaseHelper(mContext,ENG_ENGTEST_DB3);
            mSqLiteDatabase = databaseHelper.getWritableDatabase();
            mSqLiteDatabase2 = databaseHelper2.getWritableDatabase();
            mSqLiteDatabase3 = databaseHelper3.getWritableDatabase();
			
            if(mSqLiteDatabase != null){
                mSqLiteDatabase.disableWriteAheadLogging();
            }
            Log.v(TAG, "EngSqlite mSqLiteDatabase= " + mSqLiteDatabase);
			
            if(mSqLiteDatabase2 != null){
                mSqLiteDatabase2.disableWriteAheadLogging();
            }
            Log.v(TAG, "EngSqlite mSqLiteDatabase2= " + mSqLiteDatabase2);
            if(mSqLiteDatabase3 != null){
                mSqLiteDatabase3.disableWriteAheadLogging();
            }
            Log.v(TAG, "EngSqlite mSqLiteDatabase3= " + mSqLiteDatabase3);
        }catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<TestItem> queryData(ArrayList<TestItem> queryListitem) {
        ArrayList<TestItem> resultListItem = queryListitem;
        for (int i = 0; i < resultListItem.size(); i++) {
            TestItem item = resultListItem.get(i);
            item.setTestResult(getTestListItemStatus(item.getTestClassName()));
        }
        return resultListItem;
    }

    public int getTestListItemStatus(String name) {
    	SQLiteDatabase tmpSql=null;
    	if(Const.mode==0){
    		tmpSql=mSqLiteDatabase;
    	}else if (Const.mode==1){
    		tmpSql=mSqLiteDatabase2;
    	}else if (Const.mode==2){
            tmpSql=mSqLiteDatabase3;
        }

        Cursor cursor = tmpSql.query(ENG_STRING2INT_TABLE,
                new String[] { "value" }, "name=" + "\'" + name + "\'", null,
                null, null, null);
        Log.d(TAG, "name=" + name);
        if(cursor == null) return Const.DEFAULT;
        Log.d(TAG, "cursor.count=" + cursor.getCount());
        try {
            if (cursor.getCount() != 0) {
                cursor.moveToNext();
                if (cursor.getInt(0) == Const.FAIL) {
                    return Const.FAIL;
                } else if (cursor.getInt(0) == Const.SUCCESS) {
                    return Const.SUCCESS;
                } else {
                    return Const.DEFAULT;
                }
            } else {
                Log.d(TAG, "cursor.count2=" + cursor.getCount());
                return Const.DEFAULT;
            }
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            Log.d(TAG, "fianlly");
            if (cursor != null) {
                cursor.close();
                cursor = null;
            }
        }
        return Const.DEFAULT;
    }

    public void updateData(String name, int value) {
    	SQLiteDatabase tmpSql=null;
    	if(Const.mode==0){
    		tmpSql=mSqLiteDatabase;
    	}else if(Const.mode==1){
    		tmpSql=mSqLiteDatabase2;
    	}else if(Const.mode==2){
            tmpSql=mSqLiteDatabase3;
        }
		
        ContentValues cv = new ContentValues();
        cv.put(ENG_STRING2INT_NAME, name);
        cv.put(ENG_STRING2INT_VALUE, value);
        cv.put(ENG_STRING2INT_VALUE, value);
        tmpSql.execSQL("PRAGMA synchronous = FULL;");
        tmpSql.beginTransaction();
		
        try {
            tmpSql.update(ENG_STRING2INT_TABLE, cv,
                    ENG_STRING2INT_NAME + "= \'" + name + "\'", null);
            tmpSql.setTransactionSuccessful();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }catch (IllegalStateException e) {
            e.printStackTrace();
        }finally{
            tmpSql.endTransaction();
        }
    }

    public void updateDB(String name, int value) {
        if (queryData(name)) {
            updateData(name, value);
        } else {
            Log.d(TAG, "Error,unqueryData");
            if (name != null) {
                inSertData(name, value);
            }
        }
    }

    public boolean queryData(String name) {
    	SQLiteDatabase tmpSql=null;
    	if(Const.mode==0){
    		tmpSql=mSqLiteDatabase;
    	}else if(Const.mode==1){
            tmpSql=mSqLiteDatabase2;
        }else if(Const.mode==2){
            tmpSql=mSqLiteDatabase3;
        }

        try {
            Cursor c = tmpSql.query(ENG_STRING2INT_TABLE,
                    new String[] { ENG_STRING2INT_NAME, ENG_STRING2INT_VALUE },
                    ENG_STRING2INT_NAME + "= \'" + name + "\'", null, null,
                    null, null);
            if (c != null) {
                if (c.getCount() > 0) {
                    c.close();
                    return true;
                }
                c.close();
            }
        } catch (SQLException e) {
            return false;
        }
        return false;
    }

    private void inSertData(String name, int value) {
    	SQLiteDatabase tmpSql=null;
    	if(Const.mode==0){
    		tmpSql=mSqLiteDatabase;
    	}else if(Const.mode==1){
            tmpSql=mSqLiteDatabase2;
        }else if(Const.mode==2){
            tmpSql=mSqLiteDatabase3;
        }
		
        ContentValues cv = new ContentValues();
        cv.put(ENG_STRING2INT_NAME, name);
        cv.put(ENG_STRING2INT_VALUE, value);
        Log.d(TAG, "name" + name + "value:" + value);

        if (tmpSql == null) {
            Log.e(TAG, "insertData, mSqLiteDatabase == null");
            return;
        }

        tmpSql.beginTransaction();
		
        try {
            long returnValue = tmpSql.insert(ENG_STRING2INT_TABLE, null, cv);
            Log.e(TAG, "returnValue" + returnValue);
            if (returnValue == -1) {
                Log.e(TAG, "insert DB error!");
            }
            tmpSql.setTransactionSuccessful();
        } catch (NullPointerException e) {
            e.printStackTrace();
        }catch (IllegalStateException e) {
            e.printStackTrace();
        }finally{
            tmpSql.endTransaction();
        }
    }

    public int queryNotTestCount() {
        int bln = 0;
        SQLiteDatabase tmpSql=null;
    	if(Const.mode==0){
    		tmpSql=mSqLiteDatabase;
    	}else if(Const.mode==1){
            tmpSql=mSqLiteDatabase2;
        }else if(Const.mode==2){
            tmpSql=mSqLiteDatabase3;
        }
        if (tmpSql == null)
            return bln;
        Cursor cur = tmpSql.query(ENG_STRING2INT_TABLE, new String[] {
                "name", "value" }, "value=?", new String[] { "2" }, null, null,
                null);
        if (cur != null) {
            bln = cur.getCount();
            cur.close();
        }
        return bln;
    }

    public int queryFailCount() {
        int bln = 0;
        SQLiteDatabase tmpSql=null;
    	if(Const.mode==0){
    		tmpSql=mSqLiteDatabase;
    	}else if(Const.mode==1){
            tmpSql=mSqLiteDatabase2;
        }else if(Const.mode==2){
            tmpSql=mSqLiteDatabase3;
        }
        if (tmpSql == null)
            return bln;
        Cursor cur = tmpSql.query(ENG_STRING2INT_TABLE, new String[] {
                "name", "value" }, "value!=?", new String[] { "1" }, null,
                null, null);
        if (cur != null) {
            bln = cur.getCount();
            cur.close();
        }
        return bln;
    }

    public int querySystemFailCount() {
        ArrayList<TestItem> supportList = new ArrayList<TestItem>();
        supportList.addAll(UnitTestItemList.getInstance(mContext)
                .getTestItemList());
        int count = 0;
        for (int i = 0; i < supportList.size(); i++) {
            if (Const.SUCCESS != getTestListItemStatus(supportList.get(i)
                    .getTestClassName())) {
                count++;
            }
        }
        return count;
    }

    public int queryXRFailCount() {                           
        ArrayList<TestItem> supportList = new ArrayList<TestItem>();
        supportList.addAll(UnitTestItemList.getInstance(mContext)
                .getTestItemList());
        int count = 0;
        for (int i = 0; i < supportList.size(); i++) {
            if (Const.SUCCESS != getTestListItemStatus(supportList.get(i)
                    .getTestClassName())) {
                count++;
            }
        }
        return count;
    }

	
    private static class ValidationToolsDatabaseHelper extends SQLiteOpenHelper {

        Context mContext = null;

        public ValidationToolsDatabaseHelper(Context context,String dbName) {
            super(context, dbName, null, ENG_ENGTEST_VERSION);
            mContext = context;
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + ENG_STRING2INT_TABLE + ";");
            db.execSQL("CREATE TABLE " + ENG_STRING2INT_TABLE + " ("
                    + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + ENG_GROUPID_VALUE + " INTEGER NOT NULL DEFAULT 0,"
                    + ENG_STRING2INT_NAME + " TEXT,"
                    + ENG_STRING2INT_DISPLAYNAME + " TEXT,"
                    + ENG_STRING2INT_VALUE + " INTEGER NOT NULL DEFAULT 0"
                    + ");");

            ArrayList<TestItem> supportArray = UnitTestItemList.getInstance(
                    mContext).getTestItemList();

            for (int index = 0; index < supportArray.size(); index++) {
                ContentValues cv = new ContentValues();
                cv.put(ENG_STRING2INT_NAME, supportArray.get(index)
                        .getTestClassName());
                cv.put(ENG_STRING2INT_DISPLAYNAME, supportArray.get(index)
                        .getTestName());
                cv.put(ENG_STRING2INT_VALUE, String.valueOf(Const.DEFAULT));
                long returnValue = db.insert(ENG_STRING2INT_TABLE, null, cv);
                if (returnValue == -1) {
                    Log.e(TAG, "insert DB error!");
                    continue;
                }
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            if (newVersion > oldVersion) {
                db.execSQL("DROP TABLE IF EXISTS " + ENG_STRING2INT_TABLE + ";");
                onCreate(db);
            }
        }

    }
}
