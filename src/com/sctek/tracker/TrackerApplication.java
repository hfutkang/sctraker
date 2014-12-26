package com.sctek.tracker;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import com.sctek.tracker.DeviceProvideData.DeviceTableData;

import android.app.Activity;
import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class TrackerApplication extends Application {
	
	private final String TAG = "TrackerApplication";
	
	private ArrayList<DeviceListViewData> deviceList;
	private HashMap<String, SmsTimeRunnable> runnableMap;
	private ContentResolver contentResolver;
	
	private int notificationId;
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		Log.e(TAG, "onCreate");
		super.onCreate();
		
		deviceList = new ArrayList<DeviceListViewData>();
		runnableMap = new HashMap<String, SmsTimeRunnable>();
		contentResolver = getContentResolver();
		notificationId = 0;
	}
	
	public void loadDeviceList() {
		Log.e(TAG, "loadDeviceList");
		
		deviceList.clear();
		Cursor cursor = contentResolver.query(
				DeviceTableData.CONTENT_URI_D, null, null, null, null);
		
		while(cursor.moveToNext()) {

			int nameIndex = cursor.getColumnIndex(DeviceTableData.DEVICE_NAME);
			int deviceNumberIndex = cursor.getColumnIndex(DeviceTableData.DEVICE_SIM_NUMBER);
			int masterNumberIndex = cursor.getColumnIndex(DeviceTableData.MASTER_PHONE_NUMBER);
			int imagePathIndex = cursor.getColumnIndex(DeviceTableData.IMAGE_PATH);
			int passwordIndex = cursor.getColumnIndex(DeviceTableData.PASSWORD);
			int deviceIdIndex = cursor.getColumnIndex(DeviceTableData.DEVICE_ID);
			int isMasterIndex = cursor.getColumnIndex(DeviceTableData.IS_MASTER);
			int idIndex = cursor.getColumnIndex(DeviceTableData._ID);
			
			int id = cursor.getInt(idIndex);
			String name = cursor.getString(nameIndex);
			String deviceNumber = cursor.getString(deviceNumberIndex);
			String masterNumber = cursor.getString(masterNumberIndex);
			String imagePath = cursor.getString(imagePathIndex);
			String pw = cursor.getString(passwordIndex);
			String deviceId = cursor.getString(deviceIdIndex);
			String isMaster = cursor.getString(isMasterIndex);
			
			DeviceListViewData dd = new DeviceListViewData();
			dd.id = id;
			dd.name = name;
			dd.deviceNum = deviceNumber;
			dd.masterNum = masterNumber;
			dd.imagePath = imagePath;
			dd.power = -1;
			dd.pw = pw;
			dd.deviceId = deviceId;
			dd.isMaster = isMaster;
			
			deviceList.add(dd);

		}
		cursor.close();
	}
	
	public void removeDevice(String id, String dNum) {
		
		ContentResolver contentResolver = getContentResolver();
		contentResolver.delete(DeviceTableData.CONTENT_URI_D
				, DeviceTableData.DEVICE_ID + "=?"
				, new String[]{id});
		
		SharedPreferences sPref = getSharedPreferences("hasnumber",
				Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = sPref.edit();
		editor.remove(dNum);
		editor.commit();
		
		sPref = getSharedPreferences("devicestate", Activity.MODE_PRIVATE);
		editor = sPref.edit();
		editor.remove(id + "lfrequence");
		editor.remove(id + "rfrequence");
		editor.remove(id + "rebinding");
		editor.remove(id + "freqwaiting");
		editor.commit();
		
	}
	
	public void removeImage(String path) {
		
		File image = new File(path);
		if(image.exists())
			image.delete();
		
	}
	
	public ArrayList<DeviceListViewData> getDeviceList() {
		return deviceList;
	}
	
	public void addRunnble(SmsTimeRunnable r) {
		runnableMap.put(r.number + r.command, r);
	}
	
	public HashMap<String, SmsTimeRunnable> getMap() {
		return runnableMap;
	}
	
	public void removeRunnable(String deviceNum) {
			runnableMap.remove(deviceNum + Constant.BIND);
			runnableMap.remove(deviceNum + Constant.FREQUENCY_SET);
			runnableMap.remove(deviceNum + Constant.REBIND);
	}
	
	public boolean addNewDevice( DeviceListViewData b) {
		Log.e(TAG, "addNewDevice");
		
		DeviceListViewData dd = new DeviceListViewData();
		dd.name = b.name;
		dd.deviceNum = b.deviceNum;
		dd.imagePath = b.imagePath;
		dd.masterNum = b.masterNum;
		dd.pw = b.pw;
		dd.deviceId = b.deviceId;
		dd.isMaster = b.isMaster;
		dd.power = -1;
		
		Cursor cursor = contentResolver.query(
				DeviceTableData.CONTENT_URI_D, null, null, null, null);
		
		boolean isExists = false;
		while (cursor.moveToNext()) {
			if (dd.deviceId.equals(cursor.getString(2))) {
				isExists = true;
				break;
			}
		}
		cursor.close();
		
		if(!isExists) {
			
			ContentValues value = new ContentValues();
			value.put(DeviceTableData.DEVICE_NAME, dd.name);
			value.put(DeviceTableData.DEVICE_SIM_NUMBER, dd.deviceNum);
			value.put(DeviceTableData.MASTER_PHONE_NUMBER, dd.masterNum);
			value.put(DeviceTableData.IMAGE_PATH, dd.imagePath);
			value.put(DeviceTableData.DEVICE_ID, dd.deviceId);
			value.put(DeviceTableData.PASSWORD, dd.pw);
			value.put(DeviceTableData.IS_MASTER, dd.isMaster);
			
			Uri uri = contentResolver.insert(DeviceTableData.CONTENT_URI_D, value);
			String rowId = uri.getPathSegments().get(1);
			dd.id = Integer.valueOf(rowId);
			deviceList.add(dd);
			
			return true;
		} 
		else
			return false;
	}
	
	public void updateDevice(String id) {
		
		int i = searchDevice(id);
		if(i == -1)
			return;
		DeviceListViewData dd = deviceList.get(i);
		
		ContentValues value = new ContentValues();
		value.put(DeviceTableData.DEVICE_NAME, dd.name);
		value.put(DeviceTableData.DEVICE_SIM_NUMBER, dd.deviceNum);
		value.put(DeviceTableData.MASTER_PHONE_NUMBER, dd.masterNum);
		value.put(DeviceTableData.IS_MASTER, dd.isMaster);
		value.put(DeviceTableData.IMAGE_PATH, dd.imagePath);
		
		Uri updateIdUri = ContentUris
				.withAppendedId(DeviceTableData.CONTENT_URI_D, dd.id);
		contentResolver.update(updateIdUri, value, null, null);
	}
	
	public void updateDeviceNum(String id, String dNum) {
		int i = searchDevice(id);
		if(i == -1)
			return;
		DeviceListViewData dd = deviceList.get(i);
		dd.deviceNum = dNum;
		ContentValues value = new ContentValues();
		value.put(DeviceTableData.MASTER_PHONE_NUMBER, dNum);
		Uri updateIdUri = ContentUris
				.withAppendedId(DeviceTableData.CONTENT_URI_D, dd.id);
		contentResolver.update(updateIdUri, value, null, null);
	}
	
	public void resetMaster(String id, String mNum, String isMaster) {
		int i = searchDevice(id);
		if(i == -1)
			return ;
		DeviceListViewData dd = deviceList.get(i);
		dd.masterNum = mNum;
		dd.isMaster = isMaster;
		if(isMaster.equals("false")) {
			SharedPreferences sPref = getSharedPreferences("hasnumber",
					Activity.MODE_PRIVATE);
			SharedPreferences.Editor editor = sPref.edit();
			editor.remove(dd.deviceNum);
			editor.commit();
			dd.deviceNum = "";
		}
		ContentValues value = new ContentValues();
		value.put(DeviceTableData.MASTER_PHONE_NUMBER, mNum);
		value.put(DeviceTableData.IS_MASTER, isMaster);
		
		Uri updateIdUri = ContentUris
				.withAppendedId(DeviceTableData.CONTENT_URI_D, dd.id);
		contentResolver.update(updateIdUri, value, null, null);
	}
	
	public int searchDevice(String id) {
		
		int i = 0;
		for(; i < deviceList.size(); i++) {
			DeviceListViewData dd = deviceList.get(i);
			if(dd.deviceId.equals(id))
				break;
		}
		if(i < deviceList.size())
			return i;
		else
			return -1;
	}
	
	public int getNextNotificationId() {
		return ++notificationId;
	}
	
	public boolean hasDevice(String num) {
		
		SharedPreferences sPref = getSharedPreferences("hasnumber",
				Activity.MODE_PRIVATE);
		return sPref.contains(num);
	}
	
	public String getPassword(String id) {
		
		DeviceListViewData dd = null;
		for(int i = 0; i < deviceList.size(); i++) {
			 dd= deviceList.get(i);
			if(dd.deviceId.equals(id)) 
				break;
			
		}
		return dd.pw;
	}
	
	public void updatePassword(String id, String newPw) {
		int i = searchDevice(id);
		if(i == -1)
			return;
		DeviceListViewData dd = deviceList.get(i);
		
		dd.pw = newPw;
		ContentValues value = new ContentValues();
		value.put(DeviceTableData.PASSWORD, newPw);
		
		Uri updateIdUri = ContentUris
				.withAppendedId(DeviceTableData.CONTENT_URI_D, dd.id);
		contentResolver.update(updateIdUri, value, null, null);
	}
	
	public String getDeviceId(String num) {
		DeviceListViewData dd = null;
		for(int i=0; i<deviceList.size(); i++) {
			dd = deviceList.get(i);
			Log.e(TAG, dd.deviceId + " " + dd.isMaster);
			if("true".equals(dd.isMaster))
					if(dd.deviceNum.equals(num))
						return dd.deviceId;
		}
		return "";
	}
	
	public String isMaster(String id) {
		DeviceListViewData dd = null;
		for(int i = 0; i < deviceList.size(); i++) {
			 dd= deviceList.get(i);
			if(dd.deviceId.equals(id)) 
				break;
			
		}
		Log.e(TAG, dd.isMaster + " " + dd.deviceId + " " + dd.deviceNum);
		return dd.isMaster.equals("true")?dd.deviceNum:"false";
	}

}
