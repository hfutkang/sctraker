package com.sctek.tracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class TrackerStartActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tracker_start);
		
		TrackerApplication mApplication = 
				(TrackerApplication)getApplication();
		mApplication.loadDeviceList();
		Thread.setDefaultUncaughtExceptionHandler(CrashHandler.getInstance());
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Intent intent = new Intent(
						TrackerStartActivity.this, MainActivity.class);
				startActivity(intent);
				finish();
			}
		}, 0);
	}
}
