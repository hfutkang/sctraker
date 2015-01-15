package com.sctek.tracker;

import java.util.ArrayList;

import com.sctek.tracker.DeviceProvideData.DeviceTableData;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class WarningListViewAdapter extends BaseAdapter{
	private static final String TAG = "WarningListViewAdapter";
	private Context context;
	private ArrayList<WarningInfo> wList;
	
	public WarningListViewAdapter(Context c) {
		context = c;
		wList = new ArrayList<WarningInfo>();
		loadWarningList();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return wList.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		Log.e(TAG, "getView");
		Holder holder;
		WarningInfo wInfo = wList.get(position);
		if(convertView == null) {
			convertView = LayoutInflater.from(context)
					.inflate(R.layout.warning_item, null);
			TextView id = (TextView)convertView.findViewById(R.id.device_sim_w);
			TextView time = (TextView)convertView.findViewById(R.id.time_w);
			TextView warn = (TextView)convertView.findViewById(R.id.warning);
			
			id.setText(wInfo.id);
			time.setText(wInfo.time);
			switch (wInfo.type) {
			case Constant.EMERGENCY:
				warn.setText(R.string.emergency);
				break;
			case Constant.LOW_POWER_WARNING:
				warn.setText(R.string.low_power);
				break;
			case Constant.UNBINDED:
				warn.setText(R.string.master_unbind);
				break;
			default:
				break;
			}
			
			holder = new Holder();
			holder.id = id;
			holder.time = time;
			holder.warning = warn;
			convertView.setTag(holder);
			return convertView;
		}
		
		holder = (Holder)convertView.getTag();
		holder.id.setText(wInfo.id);
		holder.time.setText(wInfo.time);
		switch (wInfo.type) {
		case Constant.EMERGENCY:
			holder.warning.setText(R.string.emergency);
			break;
		case Constant.LOW_POWER_WARNING:
			holder.warning.setText(R.string.low_power);
			break;
		case Constant.UNBINDED:
			holder.warning.setText(R.string.master_unbind);
			break;
		default:
			break;
		}
		return convertView;
	}
	
	public class Holder {
		
		public TextView id;
		public TextView time;
		public TextView warning;
		
	}
	
	public class WarningInfo {
		public String id;
		public String sim;
		public int type;
		public String time;
	}
	
	public void loadWarningList() {
		Log.e(TAG, "loadWarningList");
		ContentResolver contentResolver = 
				context.getContentResolver();
		
		Cursor cursor = contentResolver.query(
				DeviceTableData.CONTENT_URI_W, null, null, null, null);
		
		while(cursor.moveToNext()){
			int deviceIdIndex = cursor
					.getColumnIndex(DeviceTableData.DEVICE_ID);
			int deviceNumberIndex = cursor
					.getColumnIndex(DeviceTableData.DEVICE_SIM_NUMBER);
			int typeIndex = cursor
					.getColumnIndex(DeviceTableData.WARNING_TYPE);
			int timeIndex = cursor
					.getColumnIndex(DeviceTableData.TIME);
			
			int type = cursor.getInt(typeIndex);
			String deviceNumber = cursor.getString(deviceNumberIndex);
			String time = cursor.getString(timeIndex);
			String deviceId = cursor.getString(deviceIdIndex);
			Log.e(TAG, deviceId);
			WarningInfo wi = new WarningInfo();
			wi.sim = deviceNumber;
			wi.time = time;
			wi.type = type;
			wi.id = deviceId;
			
			wList.add(wi);

		}
		cursor.close();
	}
	
	public void removeItem(int pos) {
		WarningInfo wi = wList.get(pos);
		ContentResolver contentResolver = 
				context.getContentResolver();
		contentResolver.delete(DeviceTableData.CONTENT_URI_W,
				DeviceTableData.TIME + "=?", new String[]{wi.time});
		wList.remove(pos);
	}

}
