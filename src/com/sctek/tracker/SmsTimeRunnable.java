package com.sctek.tracker;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class SmsTimeRunnable implements Runnable {
	
	private final String TAG = "SmsTimeRunnable";
	
	public Handler handler;
	public String number;
	public int command;
	
	public SmsTimeRunnable(Handler hl, String num, int cmd) {
		handler = hl;
		number = num;
		command = cmd;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		Log.e(TAG, "run");
		
		Message msg = handler.obtainMessage();
		msg.what = Constant.TIMEOUT_MSG;
		
		Bundle bundle = new Bundle();
		bundle.putString("num", number);
		bundle.putInt("cmd", command);
		
		msg.setData(bundle);
		handler.sendMessage(msg);
		
	}
	
}
