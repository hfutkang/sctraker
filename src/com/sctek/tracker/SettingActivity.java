package com.sctek.tracker;

import java.lang.reflect.Field;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class SettingActivity extends Activity {
	
	private final String TAG = "SettingActivity";
	private final String[] items = {"基本信息", "主控绑定", "频率设置","密码设置"};
	private final int[] reportFrequences = {15, 30, 60};
	private final int BASE_SETREQUEST_CODE = 2;
	
	private final String SMS_SEND_ACTION = "SMS_SEND_ACTION";
	private final String SMS_DELIVERED_ACTION= "SMS_DEVLIVERED_ACTION";
	
	private final static long TIME_OUT_PERIOD = 2*60*1000;
	
	private int reportFrequence;
	
	private String deviceId;
	private String masterNum;
	protected String deviceNum;
	
	private TrackerApplication mApplication;
	private SharedPreferences sharepreferences;
	
	private TextView hintTv = null;
	private EditText masterEt = null;
	private TextView bindTv = null;
	AlertDialog bindDialog = null;
	
	private boolean isBinded = false;
	private boolean isBindDialogshow = false;
	
	private Intent resultIntent;
	
	private PendingIntent spi;
	private PendingIntent dpi;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e(TAG,"onCreate");
		setContentView(R.layout.activity_setting);
		
		ActionBar actionBar = this.getActionBar();
		if(actionBar != null) {
			
			actionBar.setCustomView(R.layout.main_title_bar);
			actionBar.setDisplayShowCustomEnabled(true);
			actionBar.setDisplayShowHomeEnabled(false);
			actionBar.setDisplayShowTitleEnabled(false);
			
			actionBar.show();
		}
		
		init();
		IntentFilter filter = new IntentFilter();
		filter.addAction(SMS_DELIVERED_ACTION);
		filter.addAction(SMS_SEND_ACTION);
		registerReceiver(mBroadcastReceiver, filter);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		Log.e(TAG,"onPause");
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		bindDialog = null;
		unregisterReceiver(mBroadcastReceiver);
		super.onDestroy();
		Log.e(TAG, "onDestory");
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if(requestCode != BASE_SETREQUEST_CODE
				||resultCode != RESULT_OK)
			return;
		
		setResult(RESULT_OK);
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	public void init() {
		
		mApplication = (TrackerApplication) getApplication();
		sharepreferences = getSharedPreferences("devicestate"
				, Activity.MODE_PRIVATE);
		deviceId = getIntent().getStringExtra("deviceid");
		
		resultIntent = new Intent();
		
		TextView tv = (TextView)findViewById(R.id.title_tv_m);
//		ImageButton backIb = (ImageButton)findViewById(R.id.back_bt_m);
		tv.setText(R.string.set_title);
		
		ListView lv = (ListView)findViewById(R.id.setting_list);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this
				, R.layout.setting_item, R.id.item_name_tv, items);
		lv.setAdapter(adapter);
		
		lv.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				switch (position) {
				
					case 0:
						Intent intent = new Intent(SettingActivity.this
								, BaseInformationSettingActivity.class);
						intent.putExtra("deviceid", deviceId);
						startActivityForResult(intent, BASE_SETREQUEST_CODE);
						break;
					case 1:
						showBindDialog();
						break;
					case 2:
						showFrequenceDialog();
						break;
					case 3:
						showPasswordDialog();
						break;
					default:
						break;
				}
			}
		});
		
		spi = PendingIntent.getBroadcast(SettingActivity.this, 
				0, new Intent(SMS_SEND_ACTION), PendingIntent.FLAG_ONE_SHOT);
		dpi = PendingIntent.getBroadcast(SettingActivity.this, 
				0, new Intent(SMS_DELIVERED_ACTION), PendingIntent.FLAG_ONE_SHOT);
	}
	
	public void showFrequenceDialog() {
		
		final String dNum = mApplication.isMaster(deviceId);
		if(dNum.equals("false")) {
			Toast.makeText(SettingActivity.this
					, R.string.denied, Toast.LENGTH_SHORT).show();
			return;
		}
		
//		if(sharepreferences.getBoolean(deviceId + "freqwaiting", false)) {
//			Toast.makeText(this
//					, R.string.waiting, Toast.LENGTH_SHORT).show();
//			return;
//		}
		View view = getLayoutInflater()
				.inflate(R.layout.frequence_dialog, null);
		
		Spinner rsp = (Spinner)view.findViewById(R.id.report_frequence_sp);
		
		int rf = sharepreferences.getInt(deviceId + "rfrequence", 30);
		
		int rIndex = getItemIndexR(rf);
		
		rsp.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				reportFrequence = reportFrequences[position];
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
			
		});
		
		rsp.setSelection(rIndex);
		
		AlertDialog.Builder builder = new Builder(this);
		builder.setPositiveButton(R.string.ok, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				if(isFrequenceChanged()&&isSimAvailable()) {
					SmsUtils.setFrequence(dNum, reportFrequence, 
							mApplication.getPassword(deviceId), spi,dpi);
				}
			}
		});
		
		builder.setNegativeButton(R.string.cancel, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.cancel();
			}
		});
		builder.setView(view);
		builder.create().show();
	}
	
	private void showBindDialog(){
		
//		String rebinding = sharepreferences.getString(deviceId + "rebinding", "false");
//		if(!rebinding.equals("false")) {
//			Toast.makeText(this, R.string.waiting, Toast.LENGTH_SHORT).show();
//			return;
//		}
		masterNum = getMasterNumber(deviceId);
		isBindDialogshow = true;
		LayoutInflater layoutInflater = LayoutInflater.from(this);
		View bindDialogView = layoutInflater.inflate(R.layout.bind_dialog, null);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(bindDialogView);
		
		bindDialog = builder.create();
		bindDialog.show();
		
		TextView hintTv1 = (TextView)bindDialogView.findViewById(R.id.bind_hint1);
		TextView hintTv2 = (TextView)bindDialogView.findViewById(R.id.bind_hint2);
		TextView bindMaster = (TextView)bindDialogView.findViewById(R.id.bind_master);
		bindTv = (TextView)bindDialogView.findViewById(R.id.bind_tv);
		bindTv.setText(R.string.bind_to_master);
		
		hintTv1.setText(R.string.bind_hint_1);
		hintTv2.setText(R.string.bind_hint_2);
		bindMaster.setText(masterNum);
		
		bindTv.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				String dNum = mApplication.isMaster(deviceId);
				if(dNum.equals("false")) {
					showDeviceNumDialog();
				}
				else if(isSimAvailable()){
					SmsUtils.sendRebindMessage(dNum, 
							mApplication.getPassword(deviceId), spi, dpi);
//					SharedPreferences.Editor editor = sharepreferences.edit();
//					editor.putString(deviceId + "rebinding", dNum);
//					editor.commit();
//					addRunnable(Constant.REBIND, dNum, 20000);
				} 
				bindDialog.cancel();
			}
		});
		
	}
	
	public void showDeviceNumDialog() {
		
		LayoutInflater layoutInflater = LayoutInflater.from(this);
		View dialogView = layoutInflater.inflate(R.layout.devicenum_dialog, null);
		final EditText dNumEt = (EditText)dialogView.findViewById(R.id.device_num_et);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				String dNum = dNumEt.getText().toString();
				if(dNum.isEmpty()){
					Toast.makeText(SettingActivity.this, 
							R.string.device_num_empty, Toast.LENGTH_SHORT).show();
					return;
				}
				if(!isSimAvailable())
					return;
				SmsUtils.sendRebindMessage(dNum, 
						mApplication.getPassword(deviceId), spi, dpi);
				SharedPreferences sPref = getSharedPreferences("hasnumber", 
						Activity.MODE_PRIVATE);
				SharedPreferences.Editor editor = sPref.edit();
				editor.putBoolean(dNum, true);
				editor.commit();
//				editor = sharepreferences.edit();
//				editor.putString(deviceId + "rebinding", dNum);
//				editor.commit();
				addRunnable(Constant.REBIND, dNum, TIME_OUT_PERIOD);
				dialog.dismiss();
			}
		});
		
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		
		builder.setView(dialogView);
		builder.create().show();
	}
	
	public void showPasswordDialog() {
		
//		if(!sharepreferences.getString(deviceId + 
//				"pwwaiting", "false").equals("false")) {
//			Toast.makeText(this, R.string.waiting, Toast.LENGTH_SHORT).show();
//			return;
//		}
		
		LayoutInflater layoutInflater = LayoutInflater.from(this);
		View dialogView = layoutInflater.inflate(R.layout.password_dialog, null);
		
		final String deviceNum = mApplication.isMaster(deviceId);
		final boolean isMaster = deviceNum.equals("false")?
			false:true;
		
		TextView confirmPwTv = (TextView)dialogView.
				findViewById(R.id.confirm_password_tv_s);
		TextView cancelTv = (TextView)dialogView.
				findViewById(R.id.password_cancel_tv);
		TextView okTv = (TextView)dialogView.
				findViewById(R.id.password_ok_tv);
		final EditText newPwEt = (EditText)dialogView.
				findViewById(R.id.password_et_s);
		final EditText confirmPwEt = (EditText)dialogView.
				findViewById(R.id.confirm_password_et_s);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setView(dialogView);
		final AlertDialog passwordDialog = builder.create();
		
		if(!isMaster) {
			confirmPwEt.setVisibility(View.GONE);
			confirmPwTv.setVisibility(View.GONE);
		}
		
		cancelTv.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				passwordDialog.dismiss();
			}
		});
		
		okTv.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String newPw = newPwEt.getText().toString();
				String confirmPw = confirmPwEt.getText().toString();
				if(isMaster) {
					if(!isLegal(newPw, confirmPw))
						return;
					SmsUtils.sendModifyPwMessage(deviceNum, 
							newPw, mApplication.getPassword(deviceId), spi, dpi);
//					addRunnable(Constant.NEW_PASSWORD, deviceNum, TIME_OUT_PERIOD);
					
//					SharedPreferences.Editor editor = sharepreferences.edit();
//					editor.putString(deviceId + "pwwaiting", newPw);
//					editor.commit();
					
					passwordDialog.dismiss();
				}
				else {
					if(newPw.length() != 6) {
						Toast.makeText(SettingActivity.this, 
								R.string.input_six_password, Toast.LENGTH_SHORT).show();
						return;
					}
					if(!newPw.equals(mApplication.
							getPassword(deviceId)))
							mApplication.updatePassword(deviceId, newPw);
					passwordDialog.dismiss();
				}
			}
		});
		
		passwordDialog.show();
	}
	
	protected boolean isSimAvailable() {
		TelephonyManager tm = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		if(tm.getSimState() != TelephonyManager.SIM_STATE_READY) {
			Toast.makeText(this, R.string.sim_exception, Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}
	private boolean isLegal(String pw, String confirmPw) {
		if(pw.length()!=6) {
			Toast.makeText(this, R.string.input_six_password, 
					Toast.LENGTH_SHORT).show();
			return false;
		}
		else if(!pw.equals(confirmPw)) {
			Toast.makeText(this, R.string.password_confirm_fail, 
					Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}
	
	private boolean isFrequenceChanged() {
		
		int rf = sharepreferences.getInt(deviceId + "rfrequence", 30);
		
		if(reportFrequence != rf)
			return true;
		else 
			return false;
	}
	
	public int getItemIndexR(int value) {
		
		int i;
		for(i = 0; i < reportFrequences.length; i++) {
			if(value == reportFrequences[i])
				break;
		}
		return i;
	}
	
	public String getMasterNumber(String dNum) {
		
		int i = mApplication.searchDevice(dNum);
		
		DeviceListViewData dd = mApplication
				.getDeviceList().get(i);
		return dd.masterNum == null ? "":dd.masterNum;
	}
	
	public void setBindState(String id, String state) {
		
		int i = mApplication.searchDevice(id);
		
		DeviceListViewData dd = mApplication
				.getDeviceList().get(i);
		
		dd.masterNum = state;
	}
	
	public void addRunnable(int cmd, String dNum, long delay) {
		
		SmsTimeRunnable runnable = 
				new SmsTimeRunnable(getApplicationContext(), dNum, cmd);
		handler.postDelayed(runnable, delay);
		
//		mApplication.addRunnble(runnable);
	}
	
	Handler handler = new Handler() {
		
		public void handleMessage(Message msg) {
			
			if(msg.what == Constant.TIMEOUT_MSG) {
				
				SharedPreferences.Editor editor = sharepreferences.edit();
				Bundle bundle = msg.getData();
				int command = bundle.getInt("cmd");
				String deviceNum = bundle.getString("num");
				switch (command) {
				case Constant.FREQUENCY_SET:
					Toast.makeText(SettingActivity.this
							, R.string.set_frequence_timeout, Toast.LENGTH_SHORT).show();
					mApplication.removeRunnable(deviceNum);
					
					editor.putBoolean(deviceId + "freqwaiting", false);
					editor.commit();
					break;
					
				case Constant.BIND:
					Toast.makeText(SettingActivity.this
							, R.string.bind_timeout, Toast.LENGTH_SHORT).show();
					setBindState(deviceId, "");
					mApplication.removeRunnable(deviceId);
					break;
				case Constant.REBIND:
					Toast.makeText(SettingActivity.this
							, R.string.rebind_timeout, Toast.LENGTH_SHORT).show();
					
					editor.putString(deviceId + "rebinding", "false");
					editor.commit();
					mApplication.removeRunnable(deviceNum);
					break;
				case Constant.NEW_PASSWORD:
					Toast.makeText(SettingActivity.this, 
							R.string.modify_pw_timeout, Toast.LENGTH_SHORT).show();
					editor.putString(deviceId + "pwwaiting", "false");
					editor.commit();
					mApplication.removeRunnable(deviceNum);
				}
			}
			else if(msg.what == Constant.FREQUENCE_FAIL_MSG) {
				Toast.makeText(SettingActivity.this
						, R.string.set_frequence_fail, Toast.LENGTH_SHORT).show();
				
				SharedPreferences.Editor editor = sharepreferences.edit();
				editor.putBoolean(deviceId + "freqwaiting", false);
				editor.commit();
			}
			
			else if(msg.what == Constant.FREQUENCE_SUCCESES_MSG) {
				
				SharedPreferences.Editor editor = sharepreferences.edit();
				editor.putInt(deviceId + "rfrequence", reportFrequence);
				editor.putBoolean(deviceId + "freqwaiting", false);
				editor.commit();
				Toast.makeText(SettingActivity.this
						, R.string.set_frequence_ok, Toast.LENGTH_SHORT).show();
			}
			
			else if(msg.what == Constant.BIND_SUCCESES_MSG) {
				Bundle bundle = msg.getData();
				String mNum = bundle.getString("masternum");
				Log.e(TAG, mNum);
				setBindState(deviceId, mNum);
				
				Toast.makeText(SettingActivity.this
						, R.string.bind_ok, Toast.LENGTH_SHORT).show();
			}
			
			else if(msg.what == Constant.BIND_FAIL_MSG) {
				Bundle bundle = msg.getData();
				String mNum = bundle.getString("masternum");
				setBindState(deviceId, mNum);
				
				Toast.makeText(SettingActivity.this
						, R.string.bind_fail, Toast.LENGTH_SHORT).show();
			}
			
			else if(msg.what == Constant.UNBIND_SUCCESES_MSG) {
				Bundle bundle = msg.getData();
				String mNum = bundle.getString("masternum");
				setBindState(deviceId, mNum);
				
				if(isBindDialogshow) {
					hintTv.setText(R.string.unbinded);
					masterEt.setEnabled(true);
					masterEt.setText("");
					bindTv.setClickable(true);
					bindTv.setText(R.string.bind);
					isBinded = false;
				}
				else
					Toast.makeText(SettingActivity.this
							, R.string.unbind_ok, Toast.LENGTH_SHORT).show();
				
			}
			
			else if(msg.what == Constant.UNBIND_FAIL_MSG) {
				Bundle bundle = msg.getData();
				String mNum = bundle.getString("masternum");
				setBindState(deviceId, mNum);
				
				if(isBindDialogshow) {
					masterEt.setText(mNum);
					bindTv.setClickable(true);
					bindTv.setText(R.string.unbind);
				}
				else
					Toast.makeText(SettingActivity.this
							, R.string.unbind_fail, Toast.LENGTH_SHORT).show();
			}
			else if(msg.what == Constant.REBIND) {
				Bundle bundle = msg.getData();
				String mNum = bundle.getString("masternum");
				String result = bundle.getString("result");
				String dNum = sharepreferences.getString(deviceId + "rebinding", "false");
				SharedPreferences.Editor editor = sharepreferences.edit();
				editor.putString(deviceId + "rebinding", "false");
				editor.commit();
				if(result.equals("success")) {
					mApplication.resetMaster(deviceId, mNum, "true");
					mApplication.updateDeviceNum(deviceId, dNum);
					Toast.makeText(SettingActivity.this
							, R.string.rebind_success, Toast.LENGTH_SHORT).show();
				}
				else {
					SharedPreferences sPref = getSharedPreferences("hasnumber", 
							Activity.MODE_PRIVATE);
					editor = sPref.edit();
					editor.remove(dNum);
					editor.commit();
					Toast.makeText(SettingActivity.this
						, R.string.rebind_fail, Toast.LENGTH_SHORT).show();
				}
			}
			else if(msg.what == Constant.NEW_PASSWORD) {
				Bundle bundle = msg.getData();
				String result = bundle.getString("result");
				
				String newPw = sharepreferences.
						getString(deviceId + "pwwaiting", "false");
				SharedPreferences.Editor editor = sharepreferences.edit();
				editor.putString(deviceId + "pwwaiting", "false");
				editor.commit();
				
				if(result!=null&&result.equals("success")) {
					mApplication.updatePassword(deviceId, newPw);
					Toast.makeText(SettingActivity.this, 
							R.string.modify_pw_success, Toast.LENGTH_SHORT).show();
				}
				else
					Toast.makeText(SettingActivity.this, 
							R.string.modify_pw_fail, Toast.LENGTH_SHORT).show();
			}
		}
	};
	
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if(SMS_SEND_ACTION.equals(intent.getAction())) {
				switch(getResultCode()) {
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					Log.e(TAG, "SEND:" + "RESULT_ERROR_GENERIC_FAILURE");
					break;
				case Activity.RESULT_OK:
					Log.e(TAG, "SEND:" + "RESULT_OK");
					break;
				default:
					Log.e(TAG, "SEND:" + "fail");
					break;
				}
			}
			if(SMS_DELIVERED_ACTION.equals(intent.getAction())) {
				switch(getResultCode()) {
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					Log.e(TAG, "DELIVERE:" + "RESULT_ERROR_GENERIC_FAILURE");
					break;
				case Activity.RESULT_OK:
					Log.e(TAG, "DELIVERE:" + "RESULT_OK");
					break;
				default:
					Log.e(TAG, "DELIVERE:" + "fail");
					break;
				}
			}
		}
	};
}
