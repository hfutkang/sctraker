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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.BaseAdapter;
import android.widget.RemoteViews;
import android.widget.RemoteViews.RemoteView;
import android.widget.Toast;

public class SmsReceiver extends BroadcastReceiver {
	
	private static final String TAG = "SmsReceiver";
	private static final String lowPowerNotify = "低电报警，点击查看所有报警信息。";
	private static final String unbindNotify = "你已被解除设备的主控权限";
	
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
		map = mApplication.getMap();
		
		Bundle bundle = intent.getExtras();
		Object messages[] = (Object [])bundle.get("pdus");
		SmsMessage smsMessages[] = new SmsMessage[messages.length];
		
		for(int n = 0; n < messages.length; n++) {
			
			smsMessages[n] = SmsMessage.createFromPdu((byte[])messages[n]);
			deviceNum = smsMessages[n].getDisplayOriginatingAddress();
			Log.e(TAG, deviceNum);
			if(mApplication.hasDevice(deviceNum)) {
				try{
					
					String msgBody = smsMessages[n].getMessageBody();
					StringReader stringReader = new StringReader(msgBody);
					
					SAXParserFactory factory = SAXParserFactory.newInstance();    
					SAXParser parser = factory.newSAXParser();    
					XMLReader xmlReader = parser.getXMLReader(); 
					XmlContentHandler xmlHandler = new XmlContentHandler();
					xmlReader.setContentHandler(xmlHandler);
					xmlReader.parse(new InputSource(stringReader));
					
					if(msgBody.contains("stateres")
							&&(run=map.get(deviceNum + 
									Constant.STATE_REQUEST))!=null) 
						sendStateMsg(xmlHandler);
					
					else if(msgBody.contains("newmonitorres")
							&&(run=map.get(deviceNum + 
									Constant.NEW_MONITOR))!=null)
						sendNewMonitorMsg(xmlHandler);
					
					else if(msgBody.contains("positionres")
							&&(run=map.get(deviceNum + 
									Constant.REAL_LOCATION))!=null)
						sendLocationMsg(xmlHandler);
					
					else if(msgBody.contains("regularres")
							&&(run=map.get(deviceNum + 
									Constant.FREQUENCY_SET))!=null)
						sendFrequenceMsg(xmlHandler);
					
					else if(msgBody.contains("realres")) {
						if((run=map.get(deviceNum + 
									Constant.START_REAL_LOCATE))!=null)
							sendStartMsg(xmlHandler);
						else if((run=map.get(deviceNum + 
									Constant.STOP_REAL_LOCATE))!=null)
							sendStopMsg(xmlHandler);
					}
					else if(msgBody.contains("bindres")
							&&(run=map.get(deviceNum + 
									Constant.BIND))!=null)
						sendBindMsg(xmlHandler);
					
					else if(msgBody.contains("unbindres")
							&&(run=map.get(deviceNum + 
									Constant.UNBIND))!=null)
						sendUnBindMsg(xmlHandler);
					
					else if(msgBody.contains("rebindres")
							&&(run=map.get(deviceNum +
									Constant.REBIND))!=null)
						sendRebindMsg(xmlHandler);
					
					else if(msgBody.contains("powerstatus")) {	
						String id = mApplication.getDeviceId(deviceNum);
						Log.e(TAG, id);
						if(id.length() != 0) {
							addNotification(context, id, xmlHandler.getSmsRes().command);
							addWarnMessage(context, id,
									xmlHandler.getSmsRes().command);
						}
					}
					else if(msgBody.contains("modifypwres")
							&&(run=map.get(deviceNum +
									Constant.NEW_PASSWORD))!=null)
						sendNewPwMessage(xmlHandler);
					else if(msgBody.contains("unbinded")) {
						
						String id = mApplication.getDeviceId(deviceNum);
						mApplication.resetMaster(id, 
								xmlHandler.getSmsRes().master, "false");
						Log.e(TAG, id);
						addNotification(context, id, xmlHandler.getSmsRes().command);
						addWarnMessage(context, id,	
								xmlHandler.getSmsRes().command);
					}
					xmlHandler = null;
					this.abortBroadcast();
				
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void sendStateMsg(XmlContentHandler xh) {
		
		int power = xh.getSmsRes().power;
		String master = xh.getSmsRes().master;
		double longitude = xh.getSmsRes().longtitude;
		double laitude = xh.getSmsRes().latitude;
		String time = xh.getSmsRes().time;
		
		Message msg = run.handler.obtainMessage();
		msg.what = Constant.STATE_RES_MSG;
		
		Bundle bl = new Bundle();
		bl.putInt("power", power);
		bl.putString("master", master);
		bl.putString("devicenum", deviceNum);
		bl.putDouble("longitude", longitude);
		bl.putDouble("laitude", laitude);
		bl.putString("time", time);
		
		msg.setData(bl);
		run.handler.sendMessage(msg);
		
		run.handler.removeCallbacks(run);
		map.remove(deviceNum + Constant.STATE_REQUEST);
	}
	
	public void sendNewMonitorMsg(XmlContentHandler xh) {
		if(xh.getSmsRes().result.equals("failure")) {
			run.handler.sendEmptyMessage(
					Constant.NEW_MONITOR_FAILURE);
		} 
		else {
			int power = xh.getSmsRes().power;
			String master = xh.getSmsRes().master;
			
			Message msg = run.handler.obtainMessage();
			msg.what = Constant.NEW_MONITOR_SUCCESES;
			
			Bundle bl = new Bundle();
			bl.putInt("power", power);
			bl.putString("master", master);
			bl.putString("devicenum", deviceNum);
			
			msg.setData(bl);
			run.handler.sendMessage(msg);
		}
		
		run.handler.removeCallbacks(run);
		map.remove(deviceNum + Constant.NEW_MONITOR);
	}
	
	public void sendLocationMsg(XmlContentHandler xh) {
		
		int power = xh.getSmsRes().power;
		String client = xh.getSmsRes().clientid;
		double longtitude = xh.getSmsRes().longtitude;
		double latitude = xh.getSmsRes().latitude;
		String time  = xh.getSmsRes().time;
		
		Message msg = run.handler.obtainMessage();
		msg.what = Constant.LOCATION_MSG;
		
		Bundle bl = new Bundle();
		bl.putInt("power", power);
		bl.putString("master", client);
		bl.putDouble("longitude", longtitude);
		bl.putDouble("laitude", latitude);
		bl.putString("time", time);
		bl.putString("devicenum", deviceNum);
		
		msg.setData(bl);
		run.handler.sendMessage(msg);
		
		run.handler.removeCallbacks(run);
		map.remove(deviceNum + Constant.REAL_LOCATION);
	}
	
	public void sendFrequenceMsg(XmlContentHandler xh) {
		
		String result = xh.getSmsRes().result;
		
		if(result.equals("success")) 
			run.handler.sendEmptyMessage(
					Constant.FREQUENCE_SUCCESES_MSG);
		else
			run.handler.sendEmptyMessage(
					Constant.FREQUENCE_FAIL_MSG);
		
		run.handler.removeCallbacks(run);
		map.remove(deviceNum + Constant.FREQUENCY_SET);
	}
	
	public void sendBindMsg(XmlContentHandler xh) {
		
		String result = xh.getSmsRes().result;
		String masterNum = xh.getSmsRes().master;
		
		Message msg = run.handler.obtainMessage();
		if(result.equals("success"))
			msg.what = Constant.BIND_SUCCESES_MSG;
		else {
			msg.what = Constant.BIND_FAIL_MSG;
			masterNum = "";
		}
		
		Bundle bl = new Bundle();
		bl.putString("masternum", masterNum);
		bl.putString("devicenum", deviceNum);
		
		msg.setData(bl);
		run.handler.sendMessage(msg);
		
		run.handler.removeCallbacks(run);
		map.remove(deviceNum + Constant.BIND);
	}
	
	public void sendUnBindMsg(XmlContentHandler xh) {
		
		String masterNum = xh.getSmsRes().master;
		
		Message msg = run.handler.obtainMessage();
		msg.what = Constant.REBIND;
		Bundle bl = new Bundle();
		
		bl.putString("masternum", masterNum);
		
		msg.setData(bl);
		run.handler.sendMessage(msg);
		
		run.handler.removeCallbacks(run);
		map.remove(deviceNum);
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
		map.remove(deviceNum + Constant.UNBIND);
		
	}
	
	public void sendStartMsg(XmlContentHandler xh) {
		
		String result = xh.getSmsRes().result;
		
		if(result.equals("success")) 
			run.handler.sendEmptyMessage(
					Constant.START_REAL_LOCATE);
		else
			run.handler.sendEmptyMessage(
					Constant.START_REAL_LOCATE_FAIL);
		
		run.handler.removeCallbacks(run);
		map.remove(deviceNum  + Constant.START_REAL_LOCATE);
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
            .setContentIntent(createDisplayMessageIntent(context, deviceNum, notificationId))
            .setTicker(id);

        NotificationManager notificationManager =
            (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        notificationManager.cancel(notificationId-1);
        Notification nf = notification.getNotification();
        notificationManager.notify(notificationId, nf);
    }

    private PendingIntent createDisplayMessageIntent(Context context, String fromAddress,
            int notificationId) {
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
		
		value.put(DeviceTableData.DEVICE_SIM_NUMBER, deviceNum);
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
