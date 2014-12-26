package com.sctek.tracker;

import java.sql.DatabaseMetaData;

import com.sctek.tracker.DeviceProvideData.DeviceTableData;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DeviceDatabaseHelper extends SQLiteOpenHelper{
	
	final String TAG = "DeviceDatabaseHelper";
	
	public DeviceDatabaseHelper(Context context) {
		// TODO Auto-generated constructor stub
		super(context,
				DeviceProvideData.DATABASE_NAME,
				null,
				DeviceProvideData.DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		Log.e(TAG, "onCreate");
		
		db.execSQL("CREATE TABLE " + DeviceTableData.TABLE_NAME_D + " ("
				+ DeviceTableData._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ DeviceTableData.DEVICE_NAME + ","
				+ DeviceTableData.DEVICE_ID + ","
				+ DeviceTableData.DEVICE_SIM_NUMBER + ","
				+ DeviceTableData.MASTER_PHONE_NUMBER + ","
				+ DeviceTableData.IMAGE_PATH + ","
				+ DeviceTableData.PASSWORD + ","
				+ DeviceTableData.IS_MASTER
				+ ");");
		
		db.execSQL("CREATE TABLE " + DeviceTableData.TABLE_NAME_W + " ("
				+ DeviceTableData._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ DeviceTableData.DEVICE_ID + ","
				+ DeviceTableData.DEVICE_SIM_NUMBER + ","
				+ DeviceTableData.WARNING_TYPE + " INTEGER,"
				+ DeviceTableData.TIME
				+ ");");
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub
		Log.e(TAG, "onUpgrade");
		
		db.execSQL("DROP TABLE IF EXISTS " +
				DeviceTableData.TABLE_NAME_D);
		db.execSQL("DROP TABLE IF EXISTS " +
				DeviceTableData.TABLE_NAME_W);
		onCreate(db);
	}
	
	

}
