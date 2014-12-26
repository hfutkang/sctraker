package com.sctek.tracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.sctek.tracker.XmlContentHandler.HttpResData;
import com.sctek.tracker.XmlContentHandler.SmsResData;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

public class HttpLocateService extends Service{
	
	private final String TAG = "HttpLocateService";
	
	private final String realUrl = "http://www.sctek.cn:9090/tracercmd?cmd=%s&clientid=%s";
	private final String activateUrl = "http://www.sctek.cn:9090/tracercmd?cmd=%s&id=%s";
	private final String historyUrl = "http://www.sctek.cn:9090/tracercmd?"
										+ "cmd=%s&"
										+ "clientid=%s&"
										+ "starttime=%s&"
										+ "endtime=%s";
	private final String verifyUrl = "http://www.sctek.cn:9090/tracercmd?"
										+ "cmd=verify&"
										+ "clientid=%s&"
										+ "pw=%s";
	
	private final String cmdp = "getposition";
	private final String cmdps = "getpositions";
	private final String cmda = "getinfo";
	private final String cmdv = "verify";
	
	private Timer timer;
	private final IBinder mBinder = new ServiceBinder();
	private Handler handler;
	private int type;
	
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.e(TAG, "onCreate");
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		super.onStartCommand(intent, flags, startId);
		Log.e(TAG, "onStartCommand");
		
		type = intent.getIntExtra("type", 0);
		String deviceId = intent.getStringExtra("id");
		String startTime = intent.getStringExtra("stime");
		String endTime = intent.getStringExtra("etime");
		String pw = intent.getStringExtra("pw");
		
		if(type == Constant.REAL_LOCATION
				|| type == Constant.LAST_POSITION) {
			
			String url = String.format(realUrl, cmdp, deviceId);
			Log.e(TAG, url);
			HttpClient client = CustomHttpClient.getHttpClient();
			HttpGet locationRequest = new HttpGet(url);
			HttpRequestTask hrTask = new HttpRequestTask(locationRequest, client);
			if(type == Constant.REAL_LOCATION) {
				if(timer != null)
					timer.cancel();
				timer = new Timer();
				timer.schedule(hrTask, 0, 10000);
			}
			else {
				timer = new Timer();
				timer.schedule(hrTask, 0);
			}
		}
		else if(type == Constant.HISTORY_LOCATION) {
			
			String url = String.format(historyUrl, cmdps, deviceId, startTime, endTime);
			Log.e(TAG, url);
			HttpClient client = CustomHttpClient.getHttpClient();
			HttpGet locationRequest = new HttpGet(url);
			HttpRequestTask hrTask = new HttpRequestTask(locationRequest, client);
			if(timer != null)
				timer.cancel();
			timer = new Timer();
			timer.schedule(hrTask, 0);
		}
		else if(type == Constant.ACTIVATE_REQ) {
			String url = String.format(activateUrl, cmda, deviceId);
			Log.e(TAG, url);
			HttpClient client = CustomHttpClient.getHttpClient();
			HttpGet locationRequest = new HttpGet(url);
			HttpRequestTask hrTask = new HttpRequestTask(locationRequest, client);
			if(timer != null)
				timer.cancel();
			timer = new Timer();
			timer.schedule(hrTask, 0);
		}
		else if(type == Constant.VERIFY_REQ) {
			String url = String.format(verifyUrl, deviceId, pw);
			Log.e(TAG, url);
			HttpClient client = CustomHttpClient.getHttpClient();
			HttpGet locationRequest = new HttpGet(url);
			HttpRequestTask hrTask = new HttpRequestTask(locationRequest, client);
			if(timer != null)
				timer.cancel();
			timer = new Timer();
			timer.schedule(hrTask, 0);
		}
		else if(type == Constant.STOP_TASK) {
			if(timer != null)
				timer.cancel();
			timer = null;
		}
		
		return START_STICKY;
	}
	
	@Override
	public void onDestroy() {
		Log.e(TAG, "onDestroy");
		if(timer != null)
			timer.cancel();
		
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}
	
	class HttpRequestTask extends TimerTask {
		
		private HttpGet httpGet;
		private HttpClient httpClient;
		private String result;
		
		public HttpRequestTask(HttpGet hg, HttpClient hc) {
			httpGet = hg;
			httpClient = hc;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			Log.e(TAG, "run");
			
			String result = null;
			int requestType = type;
			int retry = 0;
			boolean success = false;
			while(retry < 3 && !success) {
				
				try{
					
					result = httpRequestExecute(httpClient, httpGet);
					success = true;
					
				} catch(Exception e){
					
					result = null;
					success = false;
					retry++;
					Log.e(TAG,"http error");
					
				}
			}
			
			if(type==requestType&&handler!=null) {
				if(!success) {
					handler.sendEmptyMessage(type + 1);
				}
				else {
					Log.e(TAG, result);
					if(result.contains("null"))
						handler.sendEmptyMessage(Constant.EMPTY_DATA);
					else if(result.contains("tracer"))
						sendCommandMessage(result);
					else if(result.contains("position"))
						sendLocationMessage(result);
					else
						handler.sendEmptyMessage(type + 1);
				}
			}
		}
		
	}
	
	/**
	 * execute http get request 2014 09 18
	 * @param httpclient
	 * @param httpget
	 * @return the device location information
	 * @throws Exception
	 */
	public String httpRequestExecute(HttpClient httpclient, HttpGet httpget) throws Exception {
		
		BufferedReader in = null;
		
		try{
			
			HttpResponse response = httpclient.execute(httpget);
			in = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			
			StringBuffer result = new StringBuffer();
			String line;
			
			while((line = in.readLine()) != null){
				result.append(line);
			}
			in.close();
			
			return result.toString();
		}finally {
			if (in != null) {
				try {
					in.close();
				}catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	@SuppressLint("NewApi")
	public void sendCommandMessage(String data) {
		
		Log.e(TAG, "sendCommandMessage");
		
		if(!data.isEmpty()) {
			try {
				StringReader reader = new StringReader(data);
				SAXParserFactory factory = SAXParserFactory.newInstance();    
				SAXParser parser = factory.newSAXParser();    
				XMLReader xmlReader = parser.getXMLReader(); 
				XmlContentHandler xmlHandler = new XmlContentHandler();
				xmlReader.setContentHandler(xmlHandler);
				xmlReader.parse(new InputSource(reader));
				
				SmsResData resData = xmlHandler.getSmsRes();
				
				if(resData != null){
					String master = resData.master;
					String initialized = resData.initialized;
					String clientid = resData.clientid;
					String pw = resData.pw;
					
					Bundle bundle = new Bundle();
					bundle.putString("master", master);
					bundle.putString("initialized", initialized);
					bundle.putString("clientid", clientid);
					bundle.putString("pw", pw);
					
					Message msg = Message.obtain(handler, type);
					msg.setData(bundle);
					msg.sendToTarget();
				}
				xmlHandler = null;
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		} 
		else {
			handler.sendEmptyMessage(Constant.EMPTY_DATA);
		}
	}

	/**
	 * send location message to the map activity 2014 09 18 
	 * @param handler
	 * @param locationResult
	 */
	@SuppressLint("NewApi")
	public void sendLocationMessage(String data) {
		
		Log.e(TAG, "sendLocationMessage");
		
		if(!data.isEmpty()) {
			try {
				StringReader reader = new StringReader(data);
				SAXParserFactory factory = SAXParserFactory.newInstance();    
				SAXParser parser = factory.newSAXParser();    
				XMLReader xmlReader = parser.getXMLReader(); 
				XmlContentHandler xmlHandler = new XmlContentHandler();
				xmlReader.setContentHandler(xmlHandler);
				xmlReader.parse(new InputSource(reader));
				
				List<HttpResData> points = xmlHandler.getHttpRes();
				
				for(int i = 0; points!=null&&i<points.size(); i++) {
					Log.e(TAG,"" + i);
					HttpResData hr = points.get(i);
					String clientNum = hr.clientid;
					String time = hr.time;
					double longtitude = hr.longtitude;
					double latitude =hr.latitude;
					int power = hr.power;
					
					Bundle bundle = new Bundle();
					bundle.putString("clientnum", clientNum);
					bundle.putString("time", time);
					bundle.putDouble("longitude", longtitude);
					bundle.putDouble("laitude", latitude);
					bundle.putInt("power", power);
					
					Message msg = handler.obtainMessage();
					msg.what = type;
					msg.setData(bundle);
					
					handler.sendMessage(msg);
				}
				
				if(type == Constant.HISTORY_LOCATION)
					handler.sendEmptyMessage(Constant.LAST_MSG);
				points.clear();
				xmlHandler = null;
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		} 
		else {
			handler.sendEmptyMessage(Constant.EMPTY_DATA);
		}
	}
	
	public class ServiceBinder extends Binder{	
			HttpLocateService getService(){
				return HttpLocateService.this;
			}
		}
	
	public void attachHandler(Handler handler){
		this.handler = handler;
	}
	
	public void dettachHandler() {
		handler = null;
	}

}
