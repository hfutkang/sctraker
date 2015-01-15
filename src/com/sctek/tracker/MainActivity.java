package com.sctek.tracker;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.CoordinateConverter.CoordType;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private final String TAG = "MainActivity";
	
	private final int NEW_DEVICE_REQUEST_CODE = 1;
	private final int LOCATE_REQUEST_CODE = 2;
	private static final int DEVICE_INFORMATION_REQUEST = 3;
	
	private final static long TIME_OUT_PERIOD = 2*60*1000;
	
	private TrackerApplication mApplication;
	private DeviceListViewAdapter lvAdapter;
	private ArrayList<DeviceListViewData> lvD;
	private DeviceListViewData newDevice;
	private SharedPreferences hasNumberPref;
	
	private ServiceManager servicemanager;
	
	private ListView deviceListView;
	private View waitView;
	private ImageButton newDeviceBt;
	
	private final OnButtonClickListener buttonsListener =
			new OnButtonClickListener();

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e(TAG, "onCreate");
		
		setContentView(R.layout.activity_main);
		
		ActionBar actionBar = this.getActionBar();
		if(actionBar != null) {
			
			actionBar.setCustomView(R.layout.main_title_bar);
			actionBar.setDisplayShowCustomEnabled(true);
			actionBar.setDisplayShowHomeEnabled(false);
			actionBar.setDisplayShowTitleEnabled(false);
			
			actionBar.show();
		}
		
		initUiView();
		initLvClickListener();
		servicemanager.bindLocateService();
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				getLastPosition();
			}
		}, 0);
		//sendStateReqMessage();

	}
	
	@Override
	protected void onResume() {
		super.onResume();
		servicemanager.attachHandler(handler);
		Log.e(TAG, "onResume");
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		servicemanager.detachHandler();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		servicemanager.unBindLocateService();
		super.onDestroy();
	}
	
	private long backPressedTime;
	@Override
	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK 
				&& event.getAction() == KeyEvent.ACTION_DOWN){   
	        if((System.currentTimeMillis()-backPressedTime) > 2000){  
	            Toast.makeText(this, 
	            		"再按一次退出程序", Toast.LENGTH_SHORT).show();                                
	            backPressedTime = System.currentTimeMillis();   
	        } else {
	            finish();
	        }
	        return true;   
	    }
	    return super.onKeyDown(keyCode, event);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		Log.e(TAG, "onActivityResult");
		Bundle bundle = null;
		if(data != null)
			bundle = data.getExtras();
		if(requestCode == DEVICE_INFORMATION_REQUEST
				&&resultCode == RESULT_OK) {
			
			newDevice.name = bundle.getString("name");
			newDevice.deviceNum = bundle.getString("devicenum");
			newDevice.imagePath = bundle.getString("imagepath");
			String pw = bundle.getString("pw");
			
			if(newDevice.isMaster.equals("true")) {
				SmsUtils.sendBindMessage(newDevice.deviceNum, 
						newDevice.deviceId, pw);
				SmsTimeRunnable runnable = 
						new SmsTimeRunnable(handler
								, newDevice.deviceNum, Constant.BIND);
				handler.postDelayed(runnable, TIME_OUT_PERIOD);
				
				mApplication.addRunnble(runnable);
				SharedPreferences.Editor editor = hasNumberPref.edit();
				editor.putBoolean(newDevice.deviceNum, true);
				editor.commit();
				
				newDevice.pw = pw; 
				
				waitView.setVisibility(View.VISIBLE);
				newDeviceBt.setEnabled(false);
//				mApplication.addNewDevice(newDevice);
//				lvAdapter.notifyDataSetChanged();
				
			}
			else {
//				Intent intent = new Intent(this, HttpLocateService.class);
//				intent.putExtra("type", Constant.VERIFY_REQ);
//				intent.putExtra("id", newDevice.deviceId);
//				startService(intent);
				mApplication.addNewDevice(newDevice);
				lvAdapter.notifyDataSetChanged();
			}
			
		}
		else if(requestCode == LOCATE_REQUEST_CODE
				&&resultCode == RESULT_OK)
			lvAdapter.notifyDataSetChanged();
		else if(requestCode == NEW_DEVICE_REQUEST_CODE
				&&resultCode == RESULT_OK) {
			newDevice.clean();
			newDevice.deviceId = bundle.getString("id");
			newDevice.masterNum = bundle.getString("master");
			newDevice.isMaster = bundle.getString("initialized").
					equals("1")?"false":"true";
			newDevice.pw = bundle.getString("pw");
			Intent intent = new Intent(MainActivity.this, NewDeviceActivity.class);
			intent.putExtra("initialized", bundle.getString("initialized"));
			intent.putExtra("count", lvAdapter.getCount());
			intent.putExtra("pw", newDevice.pw);
			startActivityForResult(intent, DEVICE_INFORMATION_REQUEST);
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	public void initUiView() {
		
		newDeviceBt = (ImageButton)findViewById(R.id.new_device_ibt);
//		ImageButton backBt = (ImageButton)findViewById(R.id.back_bt_m);
		
		newDeviceBt.setOnClickListener(buttonsListener);
//		backBt.setOnClickListener(buttonsListener);
		
		newDeviceBt.setOnLongClickListener(new OnLongClickListener() {
			
			@Override
			public boolean onLongClick(View v) {
				// TODO Auto-generated method stub
				Toast.makeText(MainActivity.this, R.string.new_device
						, Toast.LENGTH_SHORT).show();
				return false;
			}
		});
		
		mApplication = (TrackerApplication)getApplication();
		lvD = mApplication.getDeviceList();

		deviceListView = (ListView)findViewById(R.id.device_listview);
		waitView = findViewById(R.id.wait_lout);
		
		lvAdapter = new DeviceListViewAdapter(MainActivity.this, handler);
		deviceListView.setAdapter(lvAdapter);
		
		hasNumberPref = getSharedPreferences("hasnumber", 
				Activity.MODE_PRIVATE);
		
		servicemanager = new ServiceManager(this);
		newDevice = new DeviceListViewData();
		
	}
	
	public void initLvClickListener() {
		
		deviceListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(MainActivity.this, LocationActivity.class);
				
				String deviceId = lvD.get(position).deviceId;
				intent.putExtra("deviceid", deviceId);
				
				startActivityForResult(intent, LOCATE_REQUEST_CODE);
			}
		});
		
		deviceListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				showDeleteDialog(position);
				return true;
			}
		});
	}
	
	public class OnButtonClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent();
			
			switch (v.getId()){
					
				case R.id.new_device_ibt:
					intent.setClass(MainActivity.this, DeviceVerifyActivity.class);
					startActivityForResult(intent, NEW_DEVICE_REQUEST_CODE);
					break;
					
				default:
					return;
			}
		}
	}
	
	private void showDeleteDialog(final int position) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.delete_device);
		
		builder.setPositiveButton(R.string.ok, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				lvAdapter.removeItem(position);
				lvAdapter.notifyDataSetChanged();
			}
		});
		
		builder.setNegativeButton(R.string.cancel, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.cancel();
			}
		});
		AlertDialog deleteDilDialog = builder.create();
		deleteDilDialog.show();
	}
	
	private void sendStateReqMessage() {
		
		ArrayList<DeviceListViewData> lvData = mApplication.getDeviceList();
		Iterator<DeviceListViewData> it = lvData.iterator();
		
		while(it.hasNext()) {
			
			DeviceListViewData dd = it.next();
			SmsUtils.stateRequest(dd.deviceNum);
			
			SmsTimeRunnable runnable = 
					new SmsTimeRunnable(handler, dd.deviceNum, Constant.STATE_REQUEST);
			handler.postDelayed(runnable, 1000);
			
			mApplication.addRunnble(runnable);
		
		}
	}
	
	private void getLastPosition() {
		
		ArrayList<DeviceListViewData> lvData = mApplication.getDeviceList();
		Iterator<DeviceListViewData> it = lvData.iterator();
		while(it.hasNext()) {
			DeviceListViewData dd = it.next();
			Intent intent = new Intent(MainActivity.this, HttpLocateService.class);
			intent.putExtra("type", Constant.LAST_POSITION);
			intent.putExtra("id", dd.deviceId);
			startService(intent);
			
		}
	}
	
	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			Log.e(TAG, "handleMessage" + " " + msg.what);
			ArrayList<DeviceListViewData> lvD = mApplication.getDeviceList();
			
			if(msg.what == Constant.TIMEOUT_MSG) {
				
				Bundle bundle = msg.getData();
				String deviceNum = bundle.getString("num");
				int command = bundle.getInt("cmd");
				switch (command) {
				
					case Constant.STATE_REQUEST:
						
						int i = searchDevice(deviceNum);
						if(i >= 0) {
							
							DeviceListViewData device = lvD.get(i);
							device.power = -1;
							lvAdapter.notifyDataSetChanged();
							
							mApplication.removeRunnable(device.deviceNum
									+ Constant.STATE_REQUEST);
						}
						break;
						
					case Constant.BIND:
						waitView.setVisibility(View.GONE);
						newDeviceBt.setEnabled(true);
						Toast.makeText(MainActivity.this
								, R.string.adddevice_timeout, Toast.LENGTH_SHORT).show();
						mApplication.removeRunnable(
								deviceNum + Constant.BIND);
						SharedPreferences.Editor editor = hasNumberPref.edit();
						editor.remove(deviceNum);
						editor.commit();
				}
			}
			else if(msg.what == Constant.VERIFY_REQ) {
				Bundle bundle = msg.getData();
				String result = bundle.getString("result");
				
				if(result.equals("success")) {
					newDevice.isMaster = "false";
					mApplication.addNewDevice(newDevice);
					lvAdapter.notifyDataSetChanged();
					waitView.setVisibility(View.GONE);
					newDeviceBt.setEnabled(true);
				}
				else {
					waitView.setVisibility(View.GONE);
					newDeviceBt.setEnabled(true);
					Toast.makeText(MainActivity.this
							, R.string.adddevice_fail, Toast.LENGTH_SHORT).show();
				}
			}
			else if(msg.what == Constant.VERIFY_REQ_ERROR) {
				
				waitView.setVisibility(View.GONE);
				newDeviceBt.setEnabled(true);
				Toast.makeText(MainActivity.this
						, R.string.connection_error_add, Toast.LENGTH_SHORT).show();
			}
			else if(msg.what == Constant.BIND_SUCCESES_MSG) {
				
				Bundle bundle = msg.getData();
				String masterNum = bundle.getString("masternum");
				
				newDevice.masterNum = masterNum;
				mApplication.addNewDevice(newDevice);
				lvAdapter.notifyDataSetChanged();
				waitView.setVisibility(View.GONE);
				newDeviceBt.setEnabled(true);
				
			}
			else if(msg.what == Constant.BIND_FAIL_MSG) {
				Bundle bundle = msg.getData();
				String deviceNum = bundle.getString("devicenum");
				waitView.setVisibility(View.GONE);
				newDeviceBt.setEnabled(true);
				Toast.makeText(MainActivity.this
						, R.string.verify_fail, Toast.LENGTH_SHORT).show();
				SharedPreferences.Editor editor = hasNumberPref.edit();
				editor.remove(deviceNum);
				editor.commit();
			}
			else if(msg.what == Constant.LAST_POSITION) {
				Bundle bundle = msg.getData();
				String deviceId = bundle.getString("clientnum");
				String master = bundle.getString("master");
				String time = bundle.getString("time");
				double lat = bundle.getDouble("laitude");
				double longt = bundle.getDouble("longitude");
				int power = bundle.getInt("power");
				if(time == null)
					return;
				int i = searchDevice(deviceId);
				if(i < 0)
					return;
				DeviceListViewData device = lvD.get(i);
				device.power = power/5;
				if(!device.masterNum.equals(master)) {
					device.masterNum = master;
					mApplication.updateDevice(deviceId);
				}
				LatLng ll = new LatLng(lat, longt);
				CoordinateConverter cc = new CoordinateConverter();
				cc.from(CoordType.GPS);
				cc.coord(ll);
				ll = cc.convert();
				GeoCoder geoCoder = GeoCoder.newInstance();
				geoCoder.setOnGetGeoCodeResultListener(
						new GeoCoderListener(device, geoCoder, lvAdapter));
				if(geoCoder.reverseGeoCode(new ReverseGeoCodeOption().
						location(ll)))
				Log.e(TAG, "" + ll.latitude + " " + ll.longitude);
			}
			else if(msg.what == Constant.LAST_POSITION_FAIL
					||msg.what == Constant.EMPTY_DATA) {
				lvAdapter.notifyDataSetChanged();
			}
		}
	};
	
	public int searchDevice(String id) {
		
		int i = 0;
		for(; i < lvD.size(); i++) {
			DeviceListViewData dd = lvD.get(i);
			if(dd.deviceId.equals(id))
				break;
		}
		if(i < lvD.size())
			return i;
		else
			return -1;
	}
}
