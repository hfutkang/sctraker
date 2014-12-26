package com.sctek.tracker;

import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class LocationHandler extends Handler{
	
	private final String TAG = "LocationHandler";
	
	double a = 22.5460540000;
	double b = 114.0259740000;
	
	public MapView mMapView;
	
	public LocationHandler(MapView v){
		mMapView = v;
	}

	public void handleMessage(Message msg){
		super.handleMessage(msg);
		Log.e(TAG, "handleMessage");
		
		if(msg.what == Constant.REAL_LOCATION) {
			
			Bundle bundle = msg.getData();
			String clientNum = bundle.getString("clientnum");
			String time = bundle.getString("time");
			String longtitude = bundle.getString("long");
			String latitude = bundle.getString("lat");
			
			a += 0.0001;
			b += 0.00001;
//			LatLng ll = new LatLng(a, b);
//			MapStatusUpdate u = MapStatusUpdateFactory.newLatLng(ll);
//			mMapView.getMap().animateMapStatus(u);
			MyLocationData.Builder locationBuilder = new MyLocationData.Builder();
			
			locationBuilder.latitude(a);
			locationBuilder.longitude(b);
			MyLocationData locationData = locationBuilder.build();
			Log.e("BaseMapDemoActivity","" + a + ";" + b);
			//mMapView.getMap().setMyLocationData(locationData);
			Log.e(TAG, clientNum + " " + time + " " + longtitude + " " + latitude);
			
		}
		else if(msg.what == Constant.REAL_LOCATION_FAIL) {
			
			Log.e(TAG, "Http error!");
			
		}
	}


}
