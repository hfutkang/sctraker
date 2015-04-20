package com.sctek.tracker;

import android.app.PendingIntent;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;

public class SmsUtils {
	
	public final static String TAG = "SmsUtils";
	
	public final static String SMS_SEND_ACTION = "sms_send_action";
	
	public static void stateRequest(String dNum, PendingIntent spi, PendingIntent dpi) {
		
		StringBuffer msgBody = new StringBuffer();
		
		msgBody.append("<tracer>" + '\n');
		msgBody.append("<cmd>state</cmd>" + '\n');
		msgBody.append("</tracer>");
		
		try {
			
			sendSmsMessage(dNum, msgBody.toString(), spi, dpi);
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
	}
	
	public static void newMonitor(String dNum, String password, PendingIntent spi, PendingIntent dpi) {
		
		StringBuffer msgBody = new StringBuffer();
		
		msgBody.append("<tracer>" + '\n');
		msgBody.append("<cmd>newmonitor</cmd>" + '\n');
		msgBody.append("<pw>" + password + "</pw>" + '\n');
		msgBody.append("</tracer>");
		
		try {
			
			sendSmsMessage(dNum, msgBody.toString(), spi, dpi);
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
	}

	public static void setFrequence(String dNum, String id , int reportFrequence, String pw, PendingIntent spi, PendingIntent dpi) {
		
		StringBuffer msgBody = new StringBuffer();
		
		msgBody.append("<tracer>" + '\n');
		msgBody.append("<cmd>regular</cmd>" + '\n');
		msgBody.append("<id>" + id + "</id>");
		msgBody.append("<freq>" + reportFrequence + "</freq>" + '\n');
		msgBody.append("<pw>" + pw + "</pw>");
		msgBody.append("</tracer>");
		
		try {
			
			sendSmsMessage(dNum, msgBody.toString(), spi, dpi);
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
	}
	
	public static void sendBindMessage(String dNum, String id, String pw, PendingIntent spi, PendingIntent dpi) {
		
		StringBuffer msgBody = new StringBuffer();
		
		msgBody.append("<tracer>" + '\n');
		msgBody.append("<cmd>bind</cmd>" + '\n');
		msgBody.append("<id>"+ id + "</id>" + '\n');
		msgBody.append("<pw>" + pw + "</pw>" + '\n');
		msgBody.append("</tracer>");
		
		try {
			
			sendSmsMessage(dNum, msgBody.toString(), spi, dpi);
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
	}
	
	public static void sendUnbindMessage(String dNum, PendingIntent spi, PendingIntent dpi) {
		
		StringBuffer msgBody = new StringBuffer();
		
		msgBody.append("<tracer>" + '\n');
		msgBody.append("<cmd>unbind</cmd>" + '\n');
		msgBody.append("</tracer>");
		
		try {
			
			sendSmsMessage(dNum, msgBody.toString(), spi, dpi);
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
	}
	
	public static void sendRebindMessage(String dNum, String id, String pw, PendingIntent spi, PendingIntent dpi) {
		Log.e(TAG, "sendRebindMessage");
		StringBuffer msgBody = new StringBuffer();
		
		msgBody.append("<tracer>" + '\n');
		msgBody.append("<cmd>rebind</cmd>" + '\n');
		msgBody.append("<id>" + id + "</id>");
		msgBody.append("<pw>" + pw + "</pw>");
		msgBody.append("</tracer>");
		
		try {
			
			sendSmsMessage(dNum, msgBody.toString(), spi, dpi);
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
	}
	
	private static void sendSmsMessage( String addr, String msg, PendingIntent spi, PendingIntent dpi)throws Exception {
		
		SmsManager smsMgr = SmsManager.getDefault();
		smsMgr.sendTextMessage(addr, null, msg, spi, dpi);
		
	}

	public static void startLocation(String dNum, String id, int fre, String pw, PendingIntent spi, PendingIntent dpi) {
		
		StringBuffer msgBody = new StringBuffer();
		
		msgBody.append("<tracer>" + '\n');
		msgBody.append("<cmd>real</cmd>" + '\n');
		msgBody.append("<id>" + id + "</id>");
		msgBody.append("<freq>" + (fre == 5?("0" + fre):fre) + "</freq>" + '\n');
		msgBody.append("<switch>on</switch>");
		msgBody.append("<pw>" + pw + "</pw>" + '\n');
		msgBody.append("</tracer>");
		
		try {
			
			sendSmsMessage(dNum, msgBody.toString(), spi, dpi);
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
	}
	
	public static void stopLocation(String dNum, String id, String pw, int fre, PendingIntent spi, PendingIntent dpi) {
		
		StringBuffer msgBody = new StringBuffer();
		
		msgBody.append("<tracer>" + '\n');
		msgBody.append("<cmd>real</cmd>" + '\n');
		msgBody.append("<id>" + id + "</id>");
		msgBody.append("<freq>" + (fre == 5?("0" + fre):fre) + "</freq>" + '\n');
		msgBody.append("<switch>off</switch>");
		msgBody.append("<pw>" + pw + "</pw>" + '\n');
		msgBody.append("</tracer>");
		
		try {
			
			sendSmsMessage(dNum, msgBody.toString(), spi, dpi);
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
	}

	public static void sendModifyPwMessage(String dNum, String id, String newPw,
			String pw, PendingIntent spi, PendingIntent dpi) {

		Log.e(TAG, "sendRebindMessage");
		StringBuffer msgBody = new StringBuffer();
		
		msgBody.append("<tracer>" + '\n');
		msgBody.append("<cmd>modifypw</cmd>" + '\n');
		msgBody.append("<id>" + id + "</id>");
		msgBody.append("<pw>" + newPw + "</pw>" + '\n');
		msgBody.append("<old>" + pw + "</old>" + '\n');
		msgBody.append("</tracer>");
		
		try {
			
			sendSmsMessage(dNum, msgBody.toString(), spi, dpi);
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
	}

}
