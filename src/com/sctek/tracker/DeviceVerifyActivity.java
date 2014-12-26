package com.sctek.tracker;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.zxing.common.StringUtils;
import com.sctek.tracker.zxing.CaptureActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

public class DeviceVerifyActivity extends Activity {
	
	private static final String TAG = "DeviceVerifyActivity";

	private static final int SCAN_QR_REQUEST = 1;
	private static final int NEW_DEVICE_INFORMATION = 2;
	
	private static final String QR_LABLE = "sctracker:";
	
	private ImageButton scanBt;
	private Button verifyBt;
	private EditText idEt;
	private View waitView;
	
	private ServiceManager serviceManager;
	private OnButtonClickListener buttonListener;
	private TrackerApplication mApplication;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e(TAG, "onCreate");
		setContentView(R.layout.activity_device_verify);
		
		scanBt = (ImageButton)findViewById(R.id.scan_qrcode_ib);
		verifyBt = (Button)findViewById(R.id.device_verify_bt);
		idEt = (EditText)findViewById(R.id.device_id_d);
		waitView = (View)findViewById(R.id.wait_lout_d);
		
		serviceManager = ServiceManager.getServiceManager();
		buttonListener = new OnButtonClickListener();
		mApplication = (TrackerApplication)getApplication();
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Log.e(TAG, "onResume");
		scanBt.setOnClickListener(buttonListener);
		verifyBt.setOnClickListener(buttonListener);
		
		serviceManager.attachHandler(handler);
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		Log.e(TAG, "onPause");
		serviceManager.detachHandler();
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		Log.e(TAG, "onDestroy");
		super.onDestroy();
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		Log.e(TAG, "onBackPressed");
		super.onBackPressed();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		if(requestCode == SCAN_QR_REQUEST&&
				resultCode == RESULT_OK){
			String result = data.getStringExtra("id");
			if(!result.contains(QR_LABLE)) 
				Toast.makeText(DeviceVerifyActivity.this, R.string.illegal_qrcode, 
						Toast.LENGTH_SHORT).show();
			else {
				String[] split = result.split(":");
				idEt.setText(split[1]);
				idEt.setEnabled(false);
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	public class OnButtonClickListener implements View.OnClickListener {

		@SuppressLint("NewApi")
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
			switch (v.getId()){
					
				case R.id.device_verify_bt:
					String id = idEt.getText().toString();
//					Intent intent1 = new Intent();
//					intent1.putExtra("id", id);
//					intent1.putExtra("master", "13322222221");
//					intent1.putExtra("initialized", "false");
//					setResult(RESULT_OK, intent1);
//					finish();
//					Log.e(TAG, "" + getVerifyNum(id).length());
//					if(id.length()!=11||!isNumeric(id)
//							||!id.endsWith(getVerifyNum(id))) {
//						Toast.makeText(DeviceVerifyActivity.this, 
//								R.string.illegalid, Toast.LENGTH_SHORT).show();
//						return;
//					}
					if(mApplication.searchDevice(id) >= 0) {
						Toast.makeText(DeviceVerifyActivity.this, 
								R.string.device_exist, Toast.LENGTH_SHORT).show();
						return;
					}
					sendActivateRequest(id);
					idEt.setEnabled(false);
					scanBt.setEnabled(false);
					verifyBt.setEnabled(false);
					waitView.setVisibility(View.VISIBLE);
					break;
				case R.id.scan_qrcode_ib:
					Intent intent = new Intent(DeviceVerifyActivity.this,
							CaptureActivity.class);
					startActivityForResult(intent, SCAN_QR_REQUEST);
					break;
					
				default:
					return;
			}
		}
	}
	
	Handler handler = new Handler() {
		
		public void handleMessage(Message msg) {
			if(msg.what == Constant.ACTIVATE_REQ){
				Bundle bundle = msg.getData();
				String pw = bundle.getString("pw");
				String id = bundle.getString("clientid");
				String master = bundle.getString("master");
				String initialized = bundle.getString("initialized");
				if(initialized == null) {
					Toast.makeText(DeviceVerifyActivity.this, R.string.device_non_exist, 
							Toast.LENGTH_SHORT).show();
					waitView.setVisibility(View.GONE);
					idEt.setEnabled(true);
					scanBt.setEnabled(true);
					verifyBt.setEnabled(true);
					return;
				}
				Intent intent = new Intent();
				intent.putExtra("id", id);
				intent.putExtra("master", master);
				intent.putExtra("initialized", initialized);
				intent.putExtra("pw", pw);
				setResult(RESULT_OK, intent);
				finish();
			}
			else if(msg.what == Constant.ACTIVATE_REQ_ERROR) {
				waitView.setVisibility(View.GONE);
				idEt.setEnabled(true);
				scanBt.setEnabled(true);
				verifyBt.setEnabled(true);
				Toast.makeText(DeviceVerifyActivity.this, 
						R.string.connection_error_verify, Toast.LENGTH_SHORT).show();
			}
			else if(msg.what == Constant.EMPTY_DATA) {
				waitView.setVisibility(View.GONE);
				idEt.setEnabled(true);
				scanBt.setEnabled(true);
				verifyBt.setEnabled(true);
			}
		}
	};
	
	public void sendActivateRequest(String id) {
		// TODO Auto-generated method stub
		Intent intent = new Intent(DeviceVerifyActivity.this,
				HttpLocateService.class);
		intent.putExtra("type", Constant.ACTIVATE_REQ);
		intent.putExtra("id", id);
		startService(intent);
	}
	
	public boolean isNumeric(String s) {
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher macther = pattern.matcher(s);
		if(macther.matches())
			return true;
		return false;
	}
	
	@SuppressLint("NewApi")
	public String getVerifyNum(String id) {
		if(id.isEmpty())
			return "";
		char[] byteArray = id.toCharArray();
		
		String s1 = new String(byteArray, 1, 2);
		String s2 = new String(byteArray, 3, 2);
		String s3 = new String(byteArray, 5, 2);
		String s4 = new String(byteArray, 7, 2);
		
		int result = Integer.parseInt(s1) + 
				Integer.parseInt(s2) +
				Integer.parseInt(s3) +
				Integer.parseInt(s4);
		result %= 100; 
		Log.e(TAG, "" + result);
		return result < 10?"0" + result:"" + result;
	}

}
