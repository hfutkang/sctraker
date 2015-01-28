package com.sctek.tracker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

public class XmlContentHandler extends DefaultHandler {
	
	private final static String TAG = "XmlContentHandler";
	
	private boolean isSmsResponse = false;
	
	private String nodeName;
	
	private HttpResData httpRes = null;
	private List<HttpResData> points;
	
	private SmsResData smsRes = null;
	
	private int timeOffset;
	
	public XmlContentHandler() {
		points = new ArrayList<HttpResData>();
		Calendar calendar = Calendar.getInstance();
		timeOffset = calendar.get(Calendar.ZONE_OFFSET) +
				calendar.get(Calendar.DST_OFFSET);
	}
	@Override
	public void startDocument() throws SAXException {
		// TODO Auto-generated method stub
		super.startDocument();
		
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		// TODO Auto-generated method stub
		super.startElement(uri, localName, qName, attributes);
		if(localName.equals("tracer")) {
			smsRes = new SmsResData();
			isSmsResponse = true;
		} else if(localName.equals("position")){
			isSmsResponse = false;
			httpRes = new HttpResData();
		}
		nodeName = localName;
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		// TODO Auto-generated method stub
		super.endElement(uri, localName, qName);
		if(!isSmsResponse&&localName.equals("position")) {
			points.add(httpRes);
			httpRes = null;
		}
	}
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		// TODO Auto-generated method stub
		super.characters(ch, start, length);
		if(isSmsResponse) {
			
			if(nodeName.equals("cmd")) {
				smsRes.command = new String(ch, start, length);
				nodeName = "";
			}
			else if(nodeName.equals("result")) {
				smsRes.result = new String(ch, start, length);
				nodeName = "";
			}
			else if(nodeName.equals("bat")) {
				String power = new String(ch, start, length);
				smsRes.power = Integer.parseInt(power)*100;
				nodeName = "";
			}
			else if(nodeName.equals("longitude")) {
				smsRes.longtitude = parseLatLng(ch, start);
//				smsRes.longtitude = 114.0259740000;
				nodeName = "";
			}
			else if(nodeName.equals("laitude")) {
				smsRes.latitude = parseLatLng(ch, start);
//				smsRes.latitude = 22.5460540000;
				nodeName = "";
			}
			else if(nodeName.equals("time")) {
//				smsRes.time =new String(ch, start, length);
				httpRes.time = parseTime(new String(ch, start, length));
				nodeName = "";
			}
			else if(nodeName.equals("master")) {
				smsRes.master = new String(ch, start, length);
				nodeName = "";
			}
			else if(nodeName.equals("id")) {
				smsRes.clientid = new String(ch, start, length);
				nodeName = "";
			}
			else if(nodeName.equals("activated")) {
				smsRes.initialized = new String(ch, start, length);
				nodeName = "";
			}
			else if(nodeName.equals("pw")) {
				smsRes.pw = new String(ch, start, length);
				nodeName = "";
			}
		}
		else if(!isSmsResponse) {
			
			if(nodeName.equals("longitude")) {	
				httpRes.longtitude = parseLatLng(ch, start);
//				httpRes.longtitude = 114.0259740000;
				nodeName = "";
			}
			else if(nodeName.equals("laitude")) {
				httpRes.latitude = parseLatLng(ch, start);
//				httpRes.latitude = 22.5460540000;
				nodeName = "";
			}
			else if(nodeName.equals("time")) {
//				httpRes.time =new String(ch, start, length);
				httpRes.time = parseTime(new String(ch, start, length));
				nodeName = "";
			}
			else if(nodeName.equals("id")) {
				httpRes.clientid = new String(ch, start, length);
				nodeName = "";
			}
			else if(nodeName.equals("bat")) {
				String power = new String(ch, start, length);
				httpRes.power = Integer.parseInt(power);
				nodeName = "";
			}
			else if(nodeName.equals("master")) {
				httpRes.master = new String(ch, start, length);
				nodeName = "";
			}
		}
	}
	
	@Override
	public void endDocument() throws SAXException {
		// TODO Auto-generated method stub
		super.endDocument();
	}
	
	public List<HttpResData> getHttpRes() {
		return points;
	}
	
	public SmsResData getSmsRes() {
		return smsRes;
	}
	
	public class HttpResData {
		
		public double longtitude;
		public double latitude;
		public String time;
		public String clientid;
		public int power;
		public String master;
		
	}
	
	public class SmsResData {
		
		public String command;
		public String result;
		public int power;
		public double longtitude;
		public double latitude;
		public String time;
		public String master;
		public String clientid;
		public String initialized;
		public String pw;
	}
	
	public String parseTime(String sTime) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			Date date = dateFormat.parse(sTime);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			calendar.add(Calendar.MILLISECOND, timeOffset);
			return dateFormat.format(calendar.getTime());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	public double parseLatLng(char[] data, int start) {
	
		String seg1, seg2, seg3;
		double latlng;
		if(data[start] != '-') {
			seg1 = new String(data, start, 3);
			seg2 = new String(data, start + 3, 2);
			seg3 = new String(data, start + 5, 4);
			latlng = Double.parseDouble(seg1)
					+ Double.parseDouble(seg2)/60
					+ Double.parseDouble(seg3)/600000;
		}
		else {
			seg1 = new String(data, start +1, 3);
			seg2 = new String(data, start + 4, 2);
			seg3 = new String(data, start + 6, 4);
			latlng = -1*(Double.parseDouble(seg1)
					+ Double.parseDouble(seg2)/60
					+ Double.parseDouble(seg3)/600000);
		}
		return latlng;
	}

}
