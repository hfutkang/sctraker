package com.sctek.tracker;

import java.lang.Thread.UncaughtExceptionHandler;

import com.sctek.tracker.R.string;

import android.content.Context;
import android.util.Log;

public class CrashHandler implements UncaughtExceptionHandler{
	
	private final static String TAG = "CrashHandler";
	
	private static CrashHandler instance;
	
	public synchronized static CrashHandler getInstance() {
		if(instance == null) {
			instance = new CrashHandler();
		}
		return instance;
	}
	
	public void init() {
		Thread.setDefaultUncaughtExceptionHandler(this);
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		// TODO Auto-generated method stub
		Log.e(TAG, thread.getName() + " " + thread.getId() + 
				" " + ex.getMessage());
		ex.printStackTrace();
	}

}
