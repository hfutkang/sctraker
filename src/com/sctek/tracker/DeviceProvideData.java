package com.sctek.tracker;

import android.net.Uri;
import android.provider.BaseColumns;

public class DeviceProvideData {
	
	public static final String AUTHORITY = "com.sctek.provider.DeviceProvider";
	
	public static final String DATABASE_NAME = "devices.db";
	public static final int DATABASE_VERSION = 1;
	public static final String DEVICE_TABLE_NAME = "devices";
	public static final String WARNING_TABLE_NAME = "warnings";
	
	private DeviceProvideData() {}
	
	public static final class DeviceTableData implements BaseColumns {
		
		private DeviceTableData() {}
		
		public static final String TABLE_NAME_D = "devices";
		public static final String TABLE_NAME_W = "warnings";
		
		public static final Uri CONTENT_URI_D = 
				Uri.parse("content://" + AUTHORITY + "/devices");
		public static final Uri CONTENT_URI_W =
				Uri.parse("content://" + AUTHORITY + "/warnings");
		
		public static final String CONTENT_TYPE = 
				"vnd.android.cursor.dir/vnd.sctek.tracker";
		
		public static final String CONTENT_ITEM_TYPE =
				"vnd.android.cursor.item/vnd.sctek.tracker";
		
		public static final String DEVICE_NAME = "name";
		
		public static final String DEVICE_ID = "id";
		
		public static final String DEVICE_SIM_NUMBER = "devicenumber";
		
		public static final String MASTER_PHONE_NUMBER = "masternumber";
		
		public static final String IMAGE_PATH = "imagepath";
		
		public static final String WARNING_TYPE = "type";
		
		public static final String TIME = "time";
		
		public static final String PASSWORD = "pw";
		
		public static final String IS_MASTER = "ismaster";
		
	}

}
