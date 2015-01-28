package com.sctek.tracker;

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.sctek.tracker.DeviceProvideData.DeviceTableData;
import com.sctek.tracker.XmlContentHandler.SmsResData;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Telephony.Sms;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.RemoteViews;
import android.widget.RemoteViews.RemoteView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class SmsReceiver extends BroadcastReceiver {
	
	private static final String TAG = "SmsReceiver";
	
	private static final String NOTIFICATION_TITLE = "追踪器";
	
	private static final String lowPowerNotify = "低电报警，点击查看所有报警信息。";
	private static final String unbindNotify = "你已被解除设备的主控权限";
	
	private static final String BIND_SUCCESS = "绑定成功";
	private static final String BIND_FAIL = "绑定失败，点击查看详情";
	private static final String BIND_SUCCESS_BUT_EXIT = "你的手机已成功绑定设备，请重新添加";
	
	private static final String REBIND_SUCCESS = "重绑定成功";
	private static final String REBIND_FAIL ="重绑定失败，点击查看详情";
	
	private static final String NEW_PW_SUCCESS = "修改密码成功";
	private static final String NEW_PW_FAIL = "修改密码失败，点击查看详情";
	
	private static final String SET_REGULAR_SUCCESS = "设置定时报告位置频率成功";
	private static final String SET_REGULAR_FAIL = "设置定时报告位置频率失败，点击查看详情";
	
	private static final String START_REAL_LOCATE_SUCCESS = "开启实时定位成功";
	private static final String START_REAL_LOCATE_FAIL = "开启实时定位失败，点击查看详情";
	
	private TrackerApplication mApplication;
	//private ArrayList<DeviceListViewData> lvData;
	private HashMap<String, SmsTimeRunnable> map;
	private SmsTimeRunnable run;
	private String deviceNum;
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		Log.e(TAG, "onReceive");
		
		mApplication = (TrackerApplication)context.getApplicationContext();
		//lvData = mApplication.getDeviceList();
		
		Bundle bundle = intent.getExtras();
		Object messages[] = (Object [])bundle.get("pdus");
		SmsMessage smsMessages[] = new SmsMessage[messages.length];
		
		for(int n = 0; n < messages.length; n++) {
			
			smsMessages[n] = SmsMessage.createFromPdu((byte[])messages[n]);
			deviceNum = smsMessages[n].getDisplayOriginatingAddress();
			if(deviceNum.startsWith("+86")) {
				deviceNum = deviceNum.substring(3);
			}
			Log.e(TAG, deviceNum);
			if(mApplication.hasNumber(deviceNum)) {
				try{
					
					String msgBody = smsMessages[n].getMessageBody();
					StringReader stringReader = new StringReader(msgBody);
					
					SAXParserFactory factory = SAXParserFactory.newInstance();    
					SAXParser parser = factory.newSAXParser();    
					XMLReader xmlReader = parser.getXMLReader(); 
					XmlContentHandler xmlHandler = new XmlContentHandler();
					xmlReader.setContentHandler(xmlHandler);
					xmlReader.parse(new InputSource(stringReader));
					
					if(msgBody.contains("bindres")) {
						addBindresNotification(xmlHandler, context);
						
					} 
					else if(msgBody.contains("rebindres"))
						addRebindNotification(xmlHandler, context);
					
					else if(msgBody.contains("powerstatus")) {	
						SmsResData d = xmlHandler.getSmsRes();
						addNotification(context, d.clientid, d.command);
						addWarnMessage(context, d.clientid, Constant.LOW_POWER_WARNING);
					}
					else if(msgBody.contains("modifypwres"))
						addModifyPwNotification(xmlHandler, context);
					
					else if(msgBody.contains("regularres"))
						addRegularresNotification(xmlHandler, context);
					
					else if(msgBody.contains("realres"))
						addRealresNotification(xmlHandler, context);
					
					else if(msgBody.contains("unbinded")) {
						
						String id = xmlHandler.getSmsRes().clientid;
						String master = xmlHandler.getSmsRes().master;

						if(!master.equals(mApplication.getMaster(id))) {
							mApplication.resetMaster(id, master, "false");
							addNotification(context, id, xmlHandler.getSmsRes().command);
							addWarnMessage(context, id,	Constant.UNBINDED);
						}
					}
					xmlHandler = null;
					this.abortBroadcast();
				
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private void addBindresNotification(XmlContentHandler xh, Context context) {
		Log.e(TAG, "addBindresNotification");
		SmsResData data = xh.getSmsRes();
		
		String result = data.result;
		String masterNum = data.master;
		String id = data.clientid;
		DeviceListViewData dd = mApplication.getWaitDevice(id, deviceNum);
		
		Intent intent = new Intent();
		
		int wType = -1;
		
		String mBody = "";
		
		if("success".equals(result)) {
			
			wType = Constant.BIND_SUCCESES_MSG;
			
			if(dd != null) { 
				
				dd.masterNum = masterNum;
				mApplication.addNewDevice(dd);
				
				updateMyNumber(context, masterNum);
				
				mBody = new String(BIND_SUCCESS);
				
				intent.setAction(Constant.BIND_SUCCESS_ACTION);
				
			}
			else {
				mBody = new String(BIND_SUCCESS_BUT_EXIT);
			}
		}
		
		if("already_bind".equals(result)) {
			mBody = new String(BIND_FAIL);
			
			intent.setAction(Constant.BIND_FAIL_ACTION);
			
			wType = Constant.ALREADY_BIND;
			
		}
		
		context.sendBroadcast(intent);
		
		mApplication.removeWaitDevice(id);
        
        notify(context, mBody, id, wType);
		
	}
	
	public void addRebindNotification(XmlContentHandler xh, Context context) {
		
		SmsResData data = xh.getSmsRes();
		
		String result = data.result;
		String masterNum = data.master;
		String id = data.clientid;
		
		int wType = -1;
		
		String mBody = "";
		
		if("success".equals(result)) {
			
			mApplication.resetMaster(id, masterNum, "true");
			mApplication.updateDeviceNum(id, deviceNum);
			
			updateMyNumber(context, masterNum);
			
			wType = Constant.REBIND_SUCCESS_MSG;
			mBody = new String(REBIND_SUCCESS);
			
		}
		else  {
			
			if("no_bind".equals(result))
				wType = Constant.REBIND_NO_BIND;
			
			if("pwd_error".equals(result)) 
				wType = Constant.REBIND_PWD_ERROR;
			
			if("deviceid_error".equals(result))
				wType = Constant.REBIND_ID_ERROR;
			
			mBody = new String(REBIND_FAIL);
			
		}
		
		notify(context, mBody, id, wType);
		
	}
	
	public void addModifyPwNotification(XmlContentHandler xh, Context context) {
		
		SmsResData data = xh.getSmsRes();
		
		String result = data.result;
		String id = data.clientid;
		String pw = data.pw;
		
		int wType = -1;
		String mBody = "";
		
		if("success".equals(result)) {
			wType = Constant.NEW_PASSWORD_SUCCESS;
			mBody = new String(NEW_PW_SUCCESS);
			mApplication.updatePassword(id, pw);
		}
		else {
			
			mBody = new String(NEW_PW_FAIL);
			
			if("no_bind".equals(result))
				wType = Constant.NEW_PASSWORD_NO_BIND;
			if("pwd_error".equals(result))
				wType = Constant.NEW_PASSWORD_PW_ERROR;
			if("deviceid_error".equals(result))
				wType = Constant.NEW_PASSWORD_ID_ERROR;
			
		}
		
		notify(context, mBody, id, wType);
	}
	
	public void addRegularresNotification(XmlContentHandler xh, Context context) {
		
		SmsResData data = xh.getSmsRes();
		
		String result = data.result;
		String id = data.clientid;
		
		String mBody = "";
		int wType = -1;
		
		if("success".equals(result)) {
			wType = Constant.FREQUENCE_SUCCESES_MSG;
			mBody = new String(SET_REGULAR_SUCCESS);
		}
		else {
			
			mBody = new String(SET_REGULAR_FAIL);
			
			if("no_bind".equals(result))
				wType = Constant.FREQUENCE_NO_BIND;
			if("pwd_error".equals(result))
				wType = Constant.FREQUENCE_PW_ERROR;
			if("pwd_error".equals(result))
				wType = Constant.FREQUENCE_ID_ERROR;
			
		}
		
		notify(context, mBody, id, wType);
	}
	
	public void addRealresNotification(XmlContentHandler xh, Context context) {
		
		SmsResData data = xh.getSmsRes();
		
		String result = data.result;
		String id = data.clientid;
		
		String mBody = "";
		int wType = -1;
		
		SharedPreferences sPref = 
				context.getSharedPreferences("devicestate", Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = sPref.edit();
		boolean locating = sPref.getBoolean(id + "locating", true);
		
		if("success".equals(result)) {
			wType = Constant.START_REAL_LOCATE_SUCCESS;
			mBody = new String(START_REAL_LOCATE_SUCCESS);
		}
		else {
			
			editor.putBoolean(id + "locating", !locating);
			editor.commit();
			
			mBody = new String(START_REAL_LOCATE_FAIL);
			
			if("no_bind".equals(result))
				wType = locating?Constant.START_LOCATE_NO_BIND:Constant.STOP_LOCATE_NO_BIND;
			if("pwd_error".equals(result))
				wType = locating?Constant.START_LOCATE_PW_ERROR:Constant.STOP_LOCATE_PW_ERROR;
			if("deviceid_error".equals(result))
				wType = locating?Constant.START_LOCATE_ID_ERROR:Constant.STOP_LOCATE_ID_ERROR;
			
		}
		
		notify(context, mBody, id, wType);
	}
	
	public void notify(Context context, String mBody, String id, int wType) {
		
		int notificationId = mApplication.getNextNotificationId();
		
		Notification.Builder notification = new Notification.Builder(context)
        .setWhen(System.currentTimeMillis())
        .setContentTitle(NOTIFICATION_TITLE)
        .setContentText(mBody)
        .setSmallIcon(R.drawable.ic_postition_marker)
        .setContentIntent(createDisplayMessageIntent(context, notificationId));
//        .setTicker(id);

	    NotificationManager notificationManager =
	        (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
	    
	    notificationManager.cancel(notificationId-1);
	    Notification nf = notification.getNotification();
	    notificationManager.cancel(notificationId - 1);
	    notificationManager.notify(notificationId, nf);
	    
	    addWarnMessage(context, id, wType);
	}
	
	public void updateMyNumber(Context context, String num) {
		
		SharedPreferences sPref = context.getSharedPreferences("mynumber", Activity.MODE_PRIVATE);
		SharedPreferences.Editor editor = sPref.edit();
		editor.putString("mynumber", num);
		editor.commit();
		
	}
	
	public void sendRebindMsg(XmlContentHandler xh) {
		String result = xh.getSmsRes().result;
		String masterNum = xh.getSmsRes().master;
		
		Message msg = run.handler.obtainMessage(Constant.REBIND);
		Bundle bl = new Bundle();
		
		
		bl.putString("masternum", masterNum);
		bl.putString("result", result);
		msg.setData(bl);
		run.handler.sendMessage(msg);
		
		run.handler.removeCallbacks(run);
		map.remove(deviceNum + Constant.REBIND);
		
	}
	
	public void sendStartMsg(XmlContentHandler xh) {
		
		String result = xh.getSmsRes().result;
		
		if(result.equals("success")) 
			run.handler.sendEmptyMessage(
					Constant.START_REAL_LOCATE_SUCCESS);
		else
			run.handler.sendEmptyMessage(
					Constant.START_REAL_LOCATE_FAIL);
		
		run.handler.removeCallbacks(run);
		map.remove(deviceNum  + Constant.START_REAL_LOCATE_SUCCESS);
	}
	
	public void sendStopMsg(XmlContentHandler xh) {
		
		String result = xh.getSmsRes().result;
		
		if(result.equals("success")) 
			run.handler.sendEmptyMessage(
					Constant.STOP_REAL_LOCATE);
		else
			run.handler.sendEmptyMessage(
					Constant.STOP_REAL_LOCATE_FAIL);
		
		run.handler.removeCallbacks(run);
		map.remove(deviceNum + Constant.STOP_REAL_LOCATE);
	}
	
	public void sendNewPwMessage(XmlContentHandler xh) {
		String result = xh.getSmsRes().result;
		
		Message msg =  run.handler.
				obtainMessage(Constant.NEW_PASSWORD);
		Bundle bundle = new Bundle();
		bundle.putString("result", result);
		
		msg.setData(bundle);
		msg.sendToTarget();
		
		run.handler.removeCallbacks(run);
		map.remove(deviceNum  + Constant.NEW_PASSWORD);
	}
	
	@SuppressLint("NewApi")
	private void addNotification(Context context, String id, String cmd) {
		Log.e(TAG, "addNotification");
        int notificationId = mApplication.getNextNotificationId();
        String mBody = null;
        if(cmd.equals("powerstatus"))
        	mBody = String.format(lowPowerNotify, deviceNum);
        else if(cmd.equals("unbinded"))
        	mBody = String.format(unbindNotify, deviceNum);
        
        Notification.Builder notification = new Notification.Builder(context)
            .setWhen(System.currentTimeMillis())
            .setContentTitle(id)
            .setContentText(mBody)
            .setSmallIcon(R.drawable.ic_launcher)
            .setContentIntent(createDisplayMessageIntent(context, notificationId));
//            .setTicker(id);

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
    
    public void addWarnMessage(Context context, String id, int type) {
    	
    	ContentResolver cr = context.getContentResolver();
		ContentValues value = new ContentValues();
		
		value.put(DeviceTableData.DEVICE_SIM_NUMBER, deviceNum);
		value.put(DeviceTableData.DEVICE_ID, id);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date = sdf.format(new java.util.Date());
		value.put(DeviceTableData.TIME, date);
		
		value.put(DeviceTableData.WARNING_TYPE, type);
		
		cr.insert(DeviceTableData.CONTENT_URI_W, value);
		
    }

}
