package com.sctek.tracker;

import android.telephony.SmsManager;
import android.util.Log;

public class SmsUtils {
	
	public final static String TAG = "SmsUtils";
	
	public static void stateRequest(String dNum) {
		
		StringBuffer msgBody = new StringBuffer();
		
		msgBody.append("<tracer>" + '\n');
		msgBody.append("<cmd>state</cmd>" + '\n');
		msgBody.append("</tracer>");
		
		try {
			
			sendSmsMessage(dNum, msgBody.toString());
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
	}
	
	public static void newMonitor(String dNum, String password) {
		
		StringBuffer msgBody = new StringBuffer();
		
		msgBody.append("<tracer>" + '\n');
		msgBody.append("<cmd>newmonitor</cmd>" + '\n');
		msgBody.append("<pw>" + password + "</pw>" + '\n');
		msgBody.append("</tracer>");
		
		try {
			
			sendSmsMessage(dNum, msgBody.toString());
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
	}

	public static void setFrequence(String dNum, int reportFrequence, String pw) {
		
		StringBuffer msgBody = new StringBuffer();
		
		msgBody.append("<tracer>" + '\n');
		msgBody.append("<cmd>regular</cmd>" + '\n');
		msgBody.append("<freq>" + reportFrequence + "</freq>" + '\n');
		msgBody.append("<pw>" + pw + "</pw>");
		msgBody.append("</tracer>");
		
		try {
			
			sendSmsMessage(dNum, msgBody.toString());
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
	}
	
	public static void sendBindMessage(String dNum, String id, String pw) {
		
		StringBuffer msgBody = new StringBuffer();
		
		msgBody.append("<tracer>" + '\n');
		msgBody.append("<cmd>bind</cmd>" + '\n');
		msgBody.append("<id>"+ id + "</id>" + '\n');
		msgBody.append("<pw>" + pw + "</pw>" + '\n');
		msgBody.append("</tracer>");
		
		try {
			
			sendSmsMessage(dNum, msgBody.toString());
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
	}
	
	public static void sendUnbindMessage(String dNum) {
		
		StringBuffer msgBody = new StringBuffer();
		
		msgBody.append("<tracer>" + '\n');
		msgBody.append("<cmd>unbind</cmd>" + '\n');
		msgBody.append("</tracer>");
		
		try {
			
			sendSmsMessage(dNum, msgBody.toString());
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
	}
	
	public static void sendRebindMessage(String dNum, String pw) {
		
		StringBuffer msgBody = new StringBuffer();
		
		msgBody.append("<tracer>" + '\n');
		msgBody.append("<cmd>rebind</cmd>" + '\n');
		msgBody.append("<pw>" + pw + "</pw>");
		msgBody.append("</tracer>");
		
		try {
			
			sendSmsMessage(dNum, msgBody.toString());
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
	}
	
	private static void sendSmsMessage( String addr, String msg)throws Exception {
		
		SmsManager smsMgr = SmsManager.getDefault();
		smsMgr.sendTextMessage(addr, null, msg, null, null);
		
	}

	public static void startLocation(String dNum, int fre, String pw) {
		
		StringBuffer msgBody = new StringBuffer();
		
		msgBody.append("<tracer>" + '\n');
		msgBody.append("<cmd>real</cmd>" + '\n');
		msgBody.append("<freq>" + (fre == 5?("0" + fre):fre) + "</freq>" + '\n');
		msgBody.append("<switch>on</switch>");
		msgBody.append("<pw>" + pw + "</pw>" + '\n');
		msgBody.append("</tracer>");
		
		try {
			
			sendSmsMessage(dNum, msgBody.toString());
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
	}
	
	public static void stopLocation(String dNum, String pw, int fre) {
		
		StringBuffer msgBody = new StringBuffer();
		
		msgBody.append("<tracer>" + '\n');
		msgBody.append("<cmd>real</cmd>" + '\n');
		msgBody.append("<freq>" + (fre == 5?("0" + fre):fre) + "</freq>" + '\n');
		msgBody.append("<switch>off</switch>");
		msgBody.append("<pw>" + pw + "</pw>" + '\n');
		msgBody.append("</tracer>");
		
		try {
			
			sendSmsMessage(dNum, msgBody.toString());
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
	}

	public static void sendModifyPwMessage(String dNum, String newPw,
			String pw) {

		StringBuffer msgBody = new StringBuffer();
		
		msgBody.append("<tracer>" + '\n');
		msgBody.append("<cmd>start</cmd>" + '\n');
		msgBody.append("<pw>" + newPw + "</pw>" + '\n');
		msgBody.append("<old>" + pw + "</old>" + '\n');
		msgBody.append("</tracer>");
		
		try {
			
			sendSmsMessage(dNum, msgBody.toString());
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
	}

}
