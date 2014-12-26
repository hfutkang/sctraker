package com.sctek.tracker;

import java.io.File;
import java.util.ArrayList;

import com.sctek.tracker.DeviceProvideData.DeviceTableData;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class DeviceListViewAdapter extends BaseAdapter{
	
	private final String TAG = "DeviceListViewAdapter";
	
	private Context mContext;
	private ContentResolver contentResolver;
	private ArrayList<DeviceListViewData> listViewData;
	private TrackerApplication mApplication;
	private Handler handler;
	
	public DeviceListViewAdapter(Context context,Handler hd){
		
		this.mContext = context;
		mApplication = (TrackerApplication)context.getApplicationContext();
		contentResolver = context.getContentResolver();
		listViewData = mApplication.getDeviceList();
		handler = hd;
		
		//initListViewData();
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return listViewData.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		Log.e(TAG, "getView");
		
		Holder holder = new Holder();
		final DeviceListViewData data = listViewData.get(position);
		File f = new File(data.imagePath);
		
		if(convertView == null){
			
			convertView = LayoutInflater.from(mContext)
					.inflate(R.layout.device_listview_item, null);
			
			ImageView deviceLable = (ImageView)convertView.findViewById(R.id.device_lable);
			TextView deviceName = (TextView)convertView.findViewById(R.id.device_name);
			TextView deviceId = (TextView)convertView.findViewById(R.id.device_id_num);
			TextView masterNum = (TextView)convertView.findViewById(R.id.master_num);
			ImageView battery = (ImageView)convertView.findViewById(R.id.battery);
			ImageView refresh = (ImageView)convertView.findViewById(R.id.refresh_iv);
			TextView lposition = (TextView)convertView.findViewById(R.id.position_tv);
			
			deviceName.setText(data.name);
			deviceId.setText(data.deviceId);
			masterNum.setText(data.masterNum);
			battery.setImageResource(getPowerResId(data.power));
			lposition.setText(data.last_position);
			refresh.setEnabled(true);
			refresh.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Intent intent = new Intent(mContext, HttpLocateService.class);
					intent.putExtra("type", Constant.LAST_POSITION);
					intent.putExtra("id", data.deviceId);
					mContext.startService(intent);
					v.setEnabled(false);
				}
			});
			
		   if (f.exists()) {
			   Bitmap bm = BitmapFactory.decodeFile(data.imagePath);
			   Drawable drawable=new BitmapDrawable(bm);
			   deviceLable.setImageDrawable(drawable);
		    }
			
			holder.lable = deviceLable;
			holder.name = deviceName;
			holder.deviceId = deviceId;
			holder.masterNum = masterNum;
			holder.battery = battery;
			holder.refresh = refresh;
			holder.postion = lposition;
			
			convertView.setTag(holder);
			
			return convertView;
		}
		
		holder = (Holder)convertView.getTag();
		holder.name.setText(data.name);
		holder.deviceId.setText(data.deviceId);
		holder.masterNum.setText(data.masterNum);
		holder.battery.setImageResource(
				getPowerResId(data.power));
		holder.postion.setText(data.last_position);
		holder.refresh.setEnabled(true);
		holder.refresh.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				Intent intent = new Intent(mContext, HttpLocateService.class);
				intent.putExtra("type", Constant.LAST_POSITION);
				intent.putExtra("id", data.deviceId);
				mContext.startService(intent);
				v.setEnabled(false);
			}
		});
		
		if (f.exists()) {
			Bitmap bm = BitmapFactory.decodeFile(data.imagePath);
			Drawable drawable=new BitmapDrawable(bm);
			holder.lable.setImageDrawable(drawable);
		}
		else
			holder.lable.setImageResource(R.drawable.targetposition);
		
		return convertView;
	}
	
	public void initListViewData(){
		Log.e(TAG, "initListViewData");
		
		Cursor cursor = contentResolver.query(
				DeviceTableData.CONTENT_URI_D, null, null, null, null);
		
		while(cursor.moveToNext()){

			int nameIndex = cursor.getColumnIndex(DeviceTableData.DEVICE_NAME);
			int deviceNumberIndex = cursor.getColumnIndex(DeviceTableData.DEVICE_SIM_NUMBER);
			int masterNumberIndex = cursor.getColumnIndex(DeviceTableData.MASTER_PHONE_NUMBER);
			
			String name = cursor.getString(nameIndex);
			String deviceNumber = cursor.getString(deviceNumberIndex);
			String masterNumber = cursor.getString(masterNumberIndex);
			
			ListViewData data = new ListViewData(name, deviceNumber, masterNumber);

			//listViewData.add(data);

		}
	}
	
	public class ListViewData {
		
		String name;
		String deviceNum;
		String masterNum;
		
		public ListViewData(String n, String d, String m){
			name = n;
			deviceNum = d;
			masterNum = m;
		}

	}
	
	public class Holder {
		
		public TextView postion;
		public ImageView lable;
		public TextView name;
		public TextView deviceId;
		public TextView masterNum;
		public ImageView battery;
		public TextView state;
		public ImageView refresh;
		
	}
	
	public ArrayList<DeviceListViewData> getListViewData() {
		
		return listViewData;
		
	}
	
	public void removeItem(int position) {
		
		if(position < listViewData.size()) {
			
			DeviceListViewData dd = listViewData.get(position);
			
			mApplication.removeDevice(dd.deviceId, dd.deviceNum);
			mApplication.removeImage(dd.imagePath);
			mApplication.removeRunnable(dd.deviceNum);
			
			listViewData.remove(position);
		}
		
	}
	
	public void addItem(DeviceListViewData dd) {
		
		listViewData.add(dd);
	}
	
	public int getPowerResId(int power) {
		
		int id = R.drawable.battery_unknown;
		if(power == -1)
			;
		else if(power == 0)
			id = R.drawable.battery_0;
		else if(power <= 15)
			id = R.drawable.battery_15;
		else if(power<= 28)
			id = R.drawable.battery_28;
		else if(power <= 43)
			id = R.drawable.battery_43;
		else if(power <= 57)
			id = R.drawable.battery_57;
		else if(power <= 71)
			id = R.drawable.battery_71;
		else if(power <= 85)
			id = R.drawable.battery_85;
		else if(power <= 100)
			id = R.drawable.battery_100;
		return id;
	}

}
