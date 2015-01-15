package com.sctek.tracker;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.TimerTask;

import org.unism.wang.widget.DatePicker;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.utils.CoordinateConverter;
import com.baidu.mapapi.utils.CoordinateConverter.CoordType;

import android.telephony.SmsManager;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class LocationActivity extends Activity {
	
	private final String TAG = "LocationActivity";
	private final int SETTING_REQUEST_CODE = 1;
	private final int[] locationFrequences = {5, 10, 20, 40, 60};
	
	private final static long TIME_OUT_PERIOD = 2*60*1000;
	
	private String deviceId;
	private String time;
	private int locateFre;
	
	private MapView bdMap;
	private ServiceManager servicemanager;
	private GeoCoder geoCoder;
	private TrackerApplication mApplication;
	
	private List<LocationData> positions;
	
	private TextView locationTitletv;
	private LinearLayout playLout;
	private LinearLayout seekbarLout;
	private SeekBar seekbar;
	
	private ImageButton locationPlaybt;
	private ImageButton historyPlaybt;
	
//	private ImageButton backbt;
	private ImageButton menubt;
	
	private boolean reallocating = false;
	private boolean historylocating = false;
	
	private boolean locate_started;
	private boolean locate_waiting;
	
	private TimerTask tTask;
	private int progressC;
	
	private Dialog waitingDialog;
	
	DatePicker from;
	DatePicker to;
	String startTime = null;
	String endTime = null;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e(TAG, "onCreate");
		setContentView(R.layout.activity_location);
		
		ActionBar actionBar = this.getActionBar();
		if(actionBar != null) {
			
			actionBar.setCustomView(R.layout.location_title_bar);
			actionBar.setDisplayShowCustomEnabled(true);
			actionBar.setDisplayShowHomeEnabled(false);
			actionBar.setDisplayShowTitleEnabled(false);
			actionBar.show();
		}
		
		initElement();
		
		//servicemanager.bindLocateService();
		
		initListener();
		
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		Log.e(TAG, "onResume");
		
		bdMap.onResume();
		
		MapStatusUpdate u = MapStatusUpdateFactory.zoomTo(16);
		bdMap.getMap().setMapStatus(u);
		
		servicemanager.attachHandler(handler);
		
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.e(TAG, "onPause");
		
		servicemanager.detachHandler();
		stopLocation();
		bdMap.onPause();
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		recycleElement();
		super.onDestroy();
		Log.e(TAG, "onDestroy");
		//servicemanager.unBindLocateService();
		
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if(resultCode == RESULT_OK
				&&requestCode == SETTING_REQUEST_CODE) {
			setResult(RESULT_OK);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	public void onHistoryItmeClicked() {
		showDataPickDialog();
	}
	
	public void initElement() {
		
		deviceId = getIntent().getStringExtra("deviceid");
		
		//deviceNum = "13322222222";
		playLout = (LinearLayout)findViewById(R.id.play_lout);
		seekbarLout = (LinearLayout)findViewById(R.id.seekbar_lout);
		locationTitletv = (TextView)findViewById(R.id.title_tv_l);
		locationPlaybt = (ImageButton)findViewById(R.id.location_play_bt);
		historyPlaybt = (ImageButton)findViewById(R.id.history_play_bt);
//		backbt = (ImageButton)findViewById(R.id.back_bt_l);
		menubt = (ImageButton)findViewById(R.id.menu_bt_l);
		seekbar = (SeekBar)findViewById(R.id.seekbar);
		
		bdMap = (MapView)findViewById(R.id.baidu_mapview);
		mApplication = (TrackerApplication)getApplication();
		
		servicemanager = ServiceManager.getServiceManager();
		
		waitingDialog = new AlertDialog.Builder(this).create();
		geoCoder = GeoCoder.newInstance();
		
		tTask = new MyTimerTask();
		progressC = 0;
		
		positions = new ArrayList<LocationData>();
		
	}
	
	public void recycleElement() {
		
		//bdMap.onDestroy();
		geoCoder.destroy();
		positions.clear();
		
		mApplication = null;
		positions = null;
		//bdMap = null;
		servicemanager = null;
		waitingDialog = null;
		tTask = null;
		seekbar = null;
		menubt = null;
		historyPlaybt = null;
		locationPlaybt = null;
		locationTitletv = null;
		deviceId = null;
		playLout = null;
		seekbarLout = null;
		handler = null;
	}
	
	public void showDataPickDialog() {
		
		LayoutInflater layoutInflater = LayoutInflater.from(this);
		View dialogView = layoutInflater.inflate(R.layout.datepick_dialog, null);
		from = (DatePicker)dialogView.findViewById(R.id.datepicker_from);
		to = (DatePicker)dialogView.findViewById(R.id.datepicker_to);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				Calendar cal1 = Calendar.getInstance();
				cal1.set(Calendar.YEAR, from.getYear()); 
				cal1.set(Calendar.MONTH, from.getMonth() -1);
				cal1.set(Calendar.DATE, from.getDay());
				cal1.set(Calendar.HOUR_OF_DAY, 0);
				cal1.set(Calendar.MINUTE, 0);
				cal1.set(Calendar.SECOND, 0);
//				int zoneOffset1 = cal1.get(Calendar.ZONE_OFFSET);
//				cal1.add(Calendar.MILLISECOND, -zoneOffset1);
				
				Calendar cal2 = Calendar.getInstance();
				cal2.set(Calendar.YEAR, to.getYear()); 
				cal2.set(Calendar.MONTH, to.getMonth() - 1);
				cal2.set(Calendar.DATE, to.getDay());
				cal2.set(Calendar.HOUR_OF_DAY, 24);
				cal2.set(Calendar.MINUTE, 0);
				cal2.set(Calendar.SECOND, 0);
//				int zoneOffset2 = cal2.get(Calendar.ZONE_OFFSET);
//				cal2.add(Calendar.MILLISECOND, -zoneOffset2);
				
//				Log.e(TAG, cal2.getTimeZone().getID() + " " + zoneOffset1 + " " + " " + zoneOffset2 + " ");
				long dayCount = cal2.getTimeInMillis()-cal1.getTimeInMillis();
				if(dayCount < 24*3600) {
					Toast.makeText(LocationActivity.this, 
							R.string.query_time_error, Toast.LENGTH_SHORT).show();
					try {
						Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");  
		              field.setAccessible(true);  
						field.set(dialog, false);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else {
					
					SimpleDateFormat dataFormat = new SimpleDateFormat("yyyyMMddHHmmss");
					
					Date date1 = new Date(cal1.getTimeInMillis());
					Date date2 = new Date(cal2.getTimeInMillis());
					startTime = dataFormat.format(date1);
					endTime = dataFormat.format(date2);
					
					startLocation(Constant.HISTORY_LOCATION);
					
					waitForResponse();
					
					locationTitletv.setText(R.string.trace);
					locationPlaybt.setImageResource(R.drawable.play_button_selector);
					historyPlaybt.setImageResource(R.drawable.play_button_selector);
					playLout.setVisibility(View.GONE);
					
					reallocating = false;
					historylocating = false;
					positions.clear();
					seekbar.setMax(0);
					bdMap.getMap().clear();
					
					from = null;
					to = null;
					
					try {
						Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");  
		              field.setAccessible(true);  
						field.set(dialog, true);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
		
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				try {
					Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");  
	              field.setAccessible(true);  
					field.set(dialog, true);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				from = null;
				to = null;
				dialog.dismiss();
			}
		});
		builder.setView(dialogView);
		AlertDialog datePickDilDialog = builder.create();
		datePickDilDialog.show();
	}
	
	public void initListener() {
		
		locationPlaybt.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(reallocating) {
					
					locationPlaybt.setImageResource(R.drawable.play_button_selector);
					stopLocation();
				}
				else {
					
					locationPlaybt.setImageResource(R.drawable.pause_button_selector);
					startLocation(Constant.REAL_LOCATION);
				}
				reallocating = !reallocating;
			}
		});
		
		historyPlaybt.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(historylocating) {
					historyPlaybt.setImageResource(R.drawable.play_button_selector);
				}
				else {
					historyPlaybt.setImageResource(R.drawable.pause_button_selector);
					handler.postDelayed(tTask, 0);
				}
				historylocating = !historylocating;
			}
		});
		
//		backbt.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				onBackButtonClicked(v);
//			}
//		});
		
		menubt.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				onMenuButtonClicked(v);
			}
		});
		
		seekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				Log.e(TAG, "onStopTrackingTouch");
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub
				Log.e(TAG, "onStartTrackingTouch");
			}
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,
					boolean fromUser) {
				// TODO Auto-generated method stub
				Log.e(TAG, "onProgressChanged");
				progressC = progress;
				if(!positions.isEmpty()) {
					locateTo(positions.get(progress));
				}
			}
		});
		
		geoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {

			@Override
			public void onGetGeoCodeResult(GeoCodeResult arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
				// TODO Auto-generated method stub
				showInfoWindow(result.getLocation(), result.getAddress());
				if(historylocating) {
					handler.postDelayed(tTask, 2000);
				}
			}
			
		});
		
	}

	@SuppressLint("NewApi")
	public void onMenuButtonClicked(View v) {
		
		final PopupMenu popup = new PopupMenu(LocationActivity.this, v);
		
		SharedPreferences sPref = getSharedPreferences("devicestate"
				, Activity.MODE_PRIVATE);
				
		locate_started = sPref.getBoolean(deviceId + 
				"locating", false);
		locate_waiting = sPref.getBoolean(deviceId + 
				"locwaiting", false);
		
		popup.getMenuInflater().inflate(R.menu.location_menu, popup.getMenu());
		MenuItem item = popup.getMenu().findItem(R.id.start_real_location_item);
		
		final String deviceNum = mApplication.isMaster(deviceId);
		
		if(deviceNum.equals("false"))
			item.setVisible(false);
		if(locate_started)
			item.setTitle(R.string.stop_real_location);
		else 
			item.setTitle(R.string.start_real_location);
		if((locate_waiting))
			item.setEnabled(false);
		
		popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				switch (item.getItemId()) {
				case R.id.real_location_item:
					if(!reallocating) {
						historylocating = false;
						locationTitletv.setText(R.string.real_location);
						playLout.setVisibility(View.VISIBLE);
						seekbarLout.setVisibility(View.GONE);
						bdMap.getMap().clear();
					}
					break;
				case R.id.history_location_item:
					
					showDataPickDialog();
					break;
					
				case R.id.start_real_location_item:
					
					String pw = mApplication.getPassword(deviceId);
					if(!locate_started)
						showFrequenceDialog(deviceNum, pw);
					else {
						SharedPreferences sPref = 
								getSharedPreferences("devicestate", Activity.MODE_PRIVATE);
						locateFre = sPref.getInt(deviceId + "lfrequence", 20);
						SmsUtils.stopLocation(deviceNum, pw, locateFre);
						SmsTimeRunnable runnable = 
								new SmsTimeRunnable(handler, 
										deviceNum, Constant.STOP_REAL_LOCATE);
						handler.postDelayed(runnable, TIME_OUT_PERIOD);
						
						mApplication.addRunnble(runnable);
						SharedPreferences.Editor editor = sPref.edit();
						editor.putBoolean(deviceId + "locwaiting", true);
						editor.commit();
					}
					popup.dismiss();
					break;
				case R.id.setting:
					
					if(reallocating) 
						locationPlaybt.setImageResource(R.drawable.play_button_selector);
					else
						historyPlaybt.setImageResource(R.drawable.play_button_selector);
					historylocating = false;
					Intent intent = new Intent(LocationActivity.this
							, SettingActivity.class);
					intent.putExtra("deviceid", deviceId);
					startActivityForResult(intent, SETTING_REQUEST_CODE);
					break;
				default:
					break;
				}
				return true;
			}
		});
		popup.show();
	}
	
	public void showFrequenceDialog(final String dNum, final String pw) {
		
		final SharedPreferences sPref = 
				getSharedPreferences("devicestate", Activity.MODE_PRIVATE);
		View view = getLayoutInflater()
				.inflate(R.layout.realfreq_dialog, null);
		
		Spinner lsp = (Spinner)view.findViewById(R.id.location_frequence_sp);
		
		locateFre = sPref.getInt(deviceId + "lfrequence", 20);
		
		int lIndex = getItemIndexL(locateFre);
		
		lsp.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				locateFre = locationFrequences[position];
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		
			
		});
		
		lsp.setSelection(lIndex);
		
		AlertDialog.Builder builder = new Builder(this);
		builder.setPositiveButton(R.string.ok, 
				new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				SmsUtils.startLocation(dNum, locateFre, pw);
				SmsTimeRunnable runnable = 
						new SmsTimeRunnable(handler, 
								dNum, Constant.START_REAL_LOCATE);
				handler.postDelayed(runnable, TIME_OUT_PERIOD);
				
				mApplication.addRunnble(runnable);
				
				SharedPreferences.Editor editor = sPref.edit();
				editor.putBoolean(deviceId + "locwaiting", true);
				editor.commit();
					
			}
		});
		
		builder.setNegativeButton(R.string.cancel, 
				new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.cancel();
			}
		});
		builder.setView(view);
		builder.create().show();
	}
	
	public int getItemIndexL(int value) {
		
		int i;
		for(i = 0; i < locationFrequences.length; i++) {
			if(value == locationFrequences[i])
				break;
		}
		return i;
	}
	
	public void stopLocation() {
		Intent intent =  
				new Intent(LocationActivity.this, HttpLocateService.class);
		intent.putExtra("type", Constant.STOP_TASK);
		startService(intent);
	}
	
	public void startLocation(int type) {
		
		String pw = mApplication.getPassword(deviceId);
		
		Intent intent = new Intent(LocationActivity.this, HttpLocateService.class);
		intent.putExtra("type", type);
		intent.putExtra("id", deviceId);
		intent.putExtra("stime", startTime);
		intent.putExtra("etime", endTime);
		intent.putExtra("pw", pw);
		startService(intent);
	}
	
	Handler handler = new  Handler() {
		
		public void handleMessage(Message msg) {
			Log.e(TAG,"handleMessage" + " " + msg.what);
			SharedPreferences sPref = getSharedPreferences("devicestate"
					, Activity.MODE_PRIVATE);
			super.handleMessage(msg);
			if(msg.what == Constant.TIMEOUT_MSG) {
				
				SharedPreferences.Editor editor = sPref.edit();
				editor.putBoolean(deviceId + "locwaiting", false);
				editor.commit();
				Bundle bundle = msg.getData();
				String deviceNum = bundle.getString("num");
				int command = bundle.getInt("cmd");
				switch (command) {
				case Constant.START_REAL_LOCATE:
					Toast.makeText(LocationActivity.this, 
							R.string.start_real_location_timeout, Toast.LENGTH_SHORT).show();
					mApplication.removeRunnable(deviceNum
							+ Constant.START_REAL_LOCATE);
				case Constant.STOP_REAL_LOCATE:
					Toast.makeText(LocationActivity.this, 
							R.string.stop_real_location_timeout, Toast.LENGTH_SHORT).show();
					mApplication.removeRunnable(deviceNum
							+ Constant.STOP_REAL_LOCATE);
				}
			}
			else if(msg.what == Constant.START_REAL_LOCATE) {
				Toast.makeText(LocationActivity.this, 
						R.string.start_real_locate_success, Toast.LENGTH_SHORT).show();
				SharedPreferences.Editor editor = sPref.edit();
				editor.putInt(deviceId + "lfrequence", locateFre);
				editor.putBoolean(deviceId + "locwaiting", false);
				editor.putBoolean(deviceId + "locating", true);
				editor.commit();
			}
			else if(msg.what == Constant.START_REAL_LOCATE_FAIL) {
				Toast.makeText(LocationActivity.this, 
						R.string.start_locate_fail, Toast.LENGTH_SHORT).show();
				SharedPreferences.Editor editor = sPref.edit();
				editor.putBoolean(deviceId + "locwaiting", false);
				editor.commit();
			}
			else if(msg.what == Constant.STOP_REAL_LOCATE) {
				Toast.makeText(LocationActivity.this, 
						R.string.stop_locate_success, Toast.LENGTH_SHORT).show();
				
				SharedPreferences.Editor editor = sPref.edit();
				editor.putBoolean(deviceId + "locwaiting", false);
				editor.putBoolean(deviceId + "locating", false);
				editor.commit();
			}
			else if(msg.what == Constant.STOP_REAL_LOCATE_FAIL) {
				Toast.makeText(LocationActivity.this, 
						R.string.stop_locate_fail, Toast.LENGTH_SHORT).show();
				SharedPreferences.Editor editor = sPref.edit();
				editor.putBoolean(deviceId + "locwaiting", false);
				editor.commit();
			}
			
			else if(msg.what == Constant.HISTORY_LOCATION) {
				LocationData point = new LocationData();
				
				Bundle bundle = msg.getData();
				point.clinetid = bundle.getString("clientnum");
				point.time = bundle.getString("time");
				point.longitude = bundle.getDouble("longitude");
				point.laitude = bundle.getDouble("laitude");
				
				positions.add(point);
			}
			else if(msg.what == Constant.REAL_LOCATION) {
				LocationData point = new LocationData();
				
				Bundle bundle = msg.getData();
				point.clinetid = bundle.getString("clientnum");
				point.time = bundle.getString("time");
				point.longitude = bundle.getDouble("longitude");
				point.laitude = bundle.getDouble("laitude");
				if(point.time == null)
					Toast.makeText(LocationActivity.this, 
							R.string.no_new_position, Toast.LENGTH_SHORT).show();
				else
					locateTo(point);
				
			}
			else if(msg.what == Constant.LAST_MSG) {
				if(positions.size() == 1)
					seekbar.setMax(1);
				else
					seekbar.setMax(positions.size()-1);
				cancelWait();
				seekbarLout.setVisibility(View.VISIBLE);
			}
			else if(msg.what == Constant.REAL_LOCATION_FAIL) {
				Toast.makeText(LocationActivity.this,
						R.string.connection_error_real, Toast.LENGTH_SHORT).show();
			}
			else if(msg.what == Constant.HISTORY_LOCATION_FAIL) {
				cancelWait();
				Toast.makeText(LocationActivity.this, 
						R.string.connection_error_histroy, Toast.LENGTH_SHORT).show();
			}
			else if(msg.what == Constant.EMPTY_DATA) {
				
				if(reallocating) {
					Toast.makeText(LocationActivity.this, 
							R.string.no_new_position, Toast.LENGTH_SHORT).show();
				}
				else {
					cancelWait();
					seekbarLout.setVisibility(View.GONE);
					Toast.makeText(LocationActivity.this, 
							R.string.no_trace, Toast.LENGTH_SHORT).show();
				}
			}
		}
	};
	
	public void waitForResponse() {
		Log.e(TAG, "waitForREsponse");
		
		waitingDialog.show();
		waitingDialog.setContentView(R.layout.loading_process_dialog_anim);
		try {
			Field afield = waitingDialog.getClass().getSuperclass().getDeclaredField("mShowing");
			afield.setAccessible(true);  
			afield.set(waitingDialog, false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}
	
	public void cancelWait() {
		
		try {
			Field afield = waitingDialog.getClass().getSuperclass().getDeclaredField("mShowing");
			afield.setAccessible(true);  
			afield.set(waitingDialog, true);
			
			waitingDialog.cancel();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
	}
	
	@SuppressLint("NewApi")
	public void showInfoWindow( LatLng ll, String addr) {
		Log.e(TAG, "showInfoWindow");
		
		View infoView = (View)getLayoutInflater().
				inflate(R.layout.information_window, null);
		TextView timeTv = (TextView)infoView.findViewById(R.id.time_tv);
		TextView addressTv = (TextView)infoView.findViewById(R.id.address_tv);
		
		timeTv.setText(time);
		addressTv.setText(addr);
		
		InfoWindow iw = new InfoWindow(infoView, ll, -70);
		bdMap.getMap().showInfoWindow(iw);
		
		infoView = null;
		iw = null;
	}
	
//	double a = 22.557293;
//	double b = 113.940462;
	
	public void locateTo(LocationData ld) {
		
		time = ld.time;
		
		bdMap.getMap().clear();
		
//		LatLng ll = new LatLng(Float.valueOf(ld.laitude), 
//				Float.valueOf(ld.longitude));
//		a = a + 0.001;
//		b = b + 0.0001;
		
		LatLng ll = new LatLng(ld.laitude, ld.longitude);
		CoordinateConverter cc = new CoordinateConverter();
		cc.from(CoordType.GPS);
		cc.coord(ll);
		LatLng lll = cc.convert();
		OverlayOptions oo = new MarkerOptions().position(lll)
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_postition_marker));
		bdMap.getMap().addOverlay(oo);
		MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(lll);
		bdMap.getMap().setMapStatus(u);
		
		geoCoder.reverseGeoCode(new ReverseGeoCodeOption().
				location(lll));
		
	}
	public class LocationData {
		
		public String clinetid;
		public String time;
		public double longitude;
		public double laitude;
	}
	
	public class MyTimerTask extends TimerTask {
		@Override
		public void run() {
			// TODO Auto-generated method stub
			++progressC;
			if(progressC < positions.size()) {
				seekbar.setProgress(progressC);
			}
			else {
				historyPlaybt.setImageResource(
						R.drawable.play_button_selector);
				progressC = 0;
				historylocating = false;
			}
		}
		}
}
