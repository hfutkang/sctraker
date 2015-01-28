package com.sctek.tracker;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MyPhoneNumberActivity extends Activity {
	
	private final static String TAG = MyPhoneNumberActivity.class.getName();
	private EditText selfNumEt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e(TAG, "onCreate");
		setContentView(R.layout.activity_my_phone_number);
		selfNumEt = (EditText)findViewById(R.id.self_num_et);
	}
	
	public void onOkButtonClicked(View v) {
		
		String num = selfNumEt.getEditableText().toString();
		
		if(illegalNum(num)) {
			Toast.makeText(this, R.string.illegal_self_num, Toast.LENGTH_SHORT).show();
			selfNumEt.setText("");
			return;
		}
		
		SharedPreferences sPref = PreferenceManager.getDefaultSharedPreferences(this);
		SharedPreferences.Editor editor = sPref.edit();
		editor.putString("mynumber", num);
		editor.commit();
		
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish();
	}
	
	private boolean illegalNum(String num) {
		
		if(num.length() == 0)
			return true;
		
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher matcher = pattern.matcher(num);
		return !matcher.matches();
		
	}
}
