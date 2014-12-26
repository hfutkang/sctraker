package com.sctek.tracker;

import com.sctek.tracker.DeviceProvideData.DeviceTableData;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class DeviceProvider extends ContentProvider {
	
	final String TAG = "DeviceProvider";
	
	private static final UriMatcher mUriMatcher;
	private static final int THE_WHOLE_DEVICE_TABLE_URI = 1;
	private static final int SINGLE_DEVICE_URI = 2;
	private static final int THE_WHOLE_WARNING_TABLE_URI = 3;
	private static final int SINGLE_WARNING_URI = 4;
	
	private DeviceDatabaseHelper mDBHelper;
	
	static
	{
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		mUriMatcher.addURI(DeviceProvideData.AUTHORITY, "devices", 
							THE_WHOLE_DEVICE_TABLE_URI);
		mUriMatcher.addURI(DeviceProvideData.AUTHORITY, "devices/#", 
							SINGLE_DEVICE_URI);
		
		mUriMatcher.addURI(DeviceProvideData.AUTHORITY, "warnings", 
							THE_WHOLE_WARNING_TABLE_URI);
		mUriMatcher.addURI(DeviceProvideData.AUTHORITY, "warnings/#", 
							SINGLE_WARNING_URI);
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		Log.e(TAG, "onCreate");
		
		mDBHelper = new DeviceDatabaseHelper(getContext());
		
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		// TODO Auto-generated method stub
		Log.e(TAG, "query");
		
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		SQLiteDatabase database = mDBHelper.getReadableDatabase();
		
		switch (mUriMatcher.match(uri)) {
		
				case THE_WHOLE_DEVICE_TABLE_URI:
					queryBuilder.setTables(DeviceTableData.TABLE_NAME_D);
					break;
					
				case SINGLE_DEVICE_URI:
					queryBuilder.setTables(DeviceTableData.TABLE_NAME_D);
					queryBuilder.appendWhere(DeviceTableData._ID + "="
							+ uri.getPathSegments().get(1));
					break;
					
				case THE_WHOLE_WARNING_TABLE_URI:
					queryBuilder.setTables(DeviceTableData.TABLE_NAME_W);
					break;
					
				case SINGLE_WARNING_URI:
					queryBuilder.setTables(DeviceTableData.TABLE_NAME_W);
					queryBuilder.appendWhere(DeviceTableData._ID + "="
							+ uri.getPathSegments().get(1));
					
				default:
					throw new IllegalArgumentException("Unknow URI " + uri);
			
		}
		
		Cursor cursor = queryBuilder.query(database, projection, selection, 
				selectionArgs, null, null, sortOrder);

		cursor.setNotificationUri(getContext().getContentResolver(), uri);

		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		Log.e(TAG, "getType");
		
		switch (mUriMatcher.match(uri)) {
		
			case THE_WHOLE_DEVICE_TABLE_URI:
				return DeviceTableData.CONTENT_TYPE;
				
			case SINGLE_DEVICE_URI:
				return DeviceTableData.CONTENT_ITEM_TYPE;
				
			case THE_WHOLE_WARNING_TABLE_URI:
				return DeviceTableData.CONTENT_TYPE;
				
			case SINGLE_WARNING_URI:
				return DeviceTableData.CONTENT_ITEM_TYPE;
				
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
				
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		Log.e(TAG, "insert");
		
		if(mUriMatcher.match(uri) != THE_WHOLE_DEVICE_TABLE_URI
				&&mUriMatcher.match(uri) != THE_WHOLE_WARNING_TABLE_URI) {
			throw new IllegalArgumentException("Unknow URI " + uri);
		}
		
		SQLiteDatabase database = mDBHelper.getWritableDatabase();
		long rowId;
		if(mUriMatcher.match(uri) == THE_WHOLE_DEVICE_TABLE_URI)
			rowId = database.insert(DeviceTableData.TABLE_NAME_D, 
					DeviceTableData._ID, values);
		else
			rowId = database.insert(DeviceTableData.TABLE_NAME_W,
					DeviceTableData._ID, values);
		
		if(rowId > 0) {
			
			Uri insertUri = ContentUris.withAppendedId(uri, rowId);
			getContext().getContentResolver().notifyChange(insertUri, null);
			
			return insertUri;
		}
		
		throw new SQLException("Failded to insert row into " + uri);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		Log.e(TAG, "delete");
		
		SQLiteDatabase database = mDBHelper.getWritableDatabase();
		int count;
		
		switch (mUriMatcher.match(uri)){
		
				case THE_WHOLE_DEVICE_TABLE_URI:
					count = database.delete(DeviceTableData.TABLE_NAME_D, 
							selection, selectionArgs);
					break;
					
				case SINGLE_DEVICE_URI:
					String rowId = uri.getPathSegments().get(1);
					count = database.delete(DeviceTableData.TABLE_NAME_D, 
							DeviceTableData._ID + "=" + rowId +(!TextUtils.isEmpty(selection)? 
							"AND (" + selection + ')' : ""), selectionArgs);
					break;
					
				case THE_WHOLE_WARNING_TABLE_URI:
					count = database.delete(DeviceTableData.TABLE_NAME_W,
							selection, selectionArgs);
					
				case SINGLE_WARNING_URI:
					String rowId1 = uri.getPathSegments().get(1);
					count = database.delete(DeviceTableData.TABLE_NAME_W,
							DeviceTableData._ID + "=" + rowId1 + (!TextUtils.isEmpty(selection)?
									"AND (" + selection + ')' : ""), selectionArgs);
						
				default:
					throw new IllegalArgumentException("Unkown URI" + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		Log.e(TAG, "update");
		
		SQLiteDatabase database = mDBHelper.getWritableDatabase();
		int count;
		
		switch (mUriMatcher.match(uri)){
		
				case THE_WHOLE_DEVICE_TABLE_URI:
					count = database.update(DeviceTableData.TABLE_NAME_D, 
							values, selection, selectionArgs);
					break;
					
				case SINGLE_DEVICE_URI:
					String rowId = uri.getPathSegments().get(1);
					
					count = database.update(DeviceTableData.TABLE_NAME_D, 
							values, DeviceTableData._ID + "=" + rowId + (!TextUtils.isEmpty(selection) ? 
							"AND (" + selection + ')' : ""), selectionArgs);
					break;
					
				case THE_WHOLE_WARNING_TABLE_URI:
					count = database.update(DeviceTableData.TABLE_NAME_W,
							values, selection, selectionArgs);
					
				case SINGLE_WARNING_URI:
					String rowId1 = uri.getPathSegments().get(1);
					count = database.update(DeviceTableData.TABLE_NAME_W,
							values, DeviceTableData._ID + "=" + rowId1 + (!TextUtils.isEmpty(selection) ? 
									"AND (" + selection + ')' : ""), selectionArgs);
					
				default:
					throw new IllegalArgumentException("Unkonw Uri" + uri);
		}
		
		getContext().getContentResolver().notifyChange(uri, null);
		
		return count;
	}
	
}
