package com.sctek.tracker;

import java.text.SimpleDateFormat;

import com.sctek.tracker.DeviceProvideData.DeviceTableData;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class SmsTimeRunnable implements Runnable {
	
	private final String TAG = "SmsTimeRunnable";
	
	public Handler handler;
	public Context mContext;
	public String number;
	public int command;
	
	public SmsTimeRunnable(Context context, String num, int cmd) {
		mContext = context;
		number = num;
		command = cmd;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		Log.e(TAG, "run");
		
		addNotification(mContext, "追踪器", command);
		
	}
	
	@SuppressLint("NewApi")
	private void addNotification(Context context, String title, int cmd) {
		Log.e(TAG, "addNotification");
		TrackerApplication mApplication = (TrackerApplication)context;
        int notificationId = mApplication.getNextNotificationId();
        
        String mBody = null;
        switch (cmd) {
        case Constant.BIND:
        	mBody = "添加设备操作长时间没有收到回应，\n请检查设备是否开启，SIM卡号是否输入正确";
        	break;
        case Constant.REBIND:
        	mBody = "重绑定操作长时间没有收到回应，\n请检查设备是否开启，SIM卡号是否输入正确";
        	break;
        case Constant.NEW_PASSWORD:
        	mBody = "修改密码操作长时间没有收到回应，\n请检查设备是否开启";
        	break;
    	default:
    		mBody = "";
    		break;
        }
        
        Notification.Builder notification = new Notification.Builder(context)
            .setWhen(System.currentTimeMillis())
            .setContentTitle(title)
            .setContentText(mBody)
            .setSmallIcon(R.drawable.postition_marker_gps)
            .setContentIntent(createDisplayMessageIntent(context, notificationId))
            .setTicker(title);

        NotificationManager notificationManager =
            (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        notificationManager.cancel(notificationId-1);
        Notification nf = notification.getNotification();
        notificationManager.notify(notificationId, nf);
    }

    private PendingIntent createDisplayMessageIntent(Context context, int notificationId) {
        // Trigger the main activity to fire up a dialog that shows the received messages
        Intent di = new Intent();
        //di.setClass(context, DialogSmsDisplay.class);
        di.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //di.putExtra(DialogSmsDisplay.SMS_FROM_ADDRESS_EXTRA, fromAddress);
        //di.putExtra(DialogSmsDisplay.SMS_MESSAGE_EXTRA, message);
        di.putExtra("id", notificationId);

        // This line is needed to make this intent compare differently than the other intents
        // created here for other messages. Without this line, the PendingIntent always gets the
        // intent of a previous message and notification.
        di.setType(Integer.toString(notificationId));
        di.setClass(context, WarningActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, di, 0);
        return pendingIntent;
    }
    
    public void addWarnMessage(Context context, String id, String cmd) {
    	ContentResolver cr = context.getContentResolver();
		ContentValues value = new ContentValues();
		
		value.put(DeviceTableData.DEVICE_SIM_NUMBER, id);
		value.put(DeviceTableData.DEVICE_ID, id);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date = sdf.format(new java.util.Date());
		value.put(DeviceTableData.TIME, date);
		
		if(cmd.equals("powerstatus")) {
			value.put(DeviceTableData.WARNING_TYPE, Constant.LOW_POWER_WARNING);
		}
		else if(cmd.equals("emergency")) {
			value.put(DeviceTableData.WARNING_TYPE, Constant.EMERGENCY);
		}
		else if(cmd.equals("unbinded")) {
			value.put(DeviceTableData.WARNING_TYPE, Constant.UNBINDED);
		}
		
		cr.insert(DeviceTableData.CONTENT_URI_W, value);
		
    }
	
}
