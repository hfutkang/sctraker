package com.sctek.tracker;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
public class GeoCoderListener implements OnGetGeoCoderResultListener{
	
	private final static String TAG = "GeoCoderListener";
	
	private DeviceListViewData device = null;
	private BaseAdapter adapter = null;
	private GeoCoder gCoder = null;
	
	public GeoCoderListener(DeviceListViewData d, GeoCoder g, BaseAdapter a) {
		device = d;
		gCoder = g;
		adapter = a;
	}
	
	@Override
	public void onGetGeoCodeResult(GeoCodeResult arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
		// TODO Auto-generated method stub
		Log.e(TAG, "onGetReverseGeoCodeResult");
		if(device != null) {
			device.last_position = result.getAddress();
			adapter.notifyDataSetChanged();
			gCoder.destroy();
		}
		
	}

}
