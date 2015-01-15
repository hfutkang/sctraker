package com.sctek.tracker;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;

public class WarningActivity extends Activity {
	private final static String TAG = "WarningActivity";
	
	private WarningListViewAdapter wlAdapter;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_warning);
		Log.e(TAG, "onCreate");
		
		cancelNotification();
		
		ListView lv = (ListView)findViewById(R.id.warning_lv);
		wlAdapter = new WarningListViewAdapter(this);
		lv.setAdapter(wlAdapter);
		
		ActionBar actionBar = this.getActionBar();
		if(actionBar != null) {
			
			actionBar.setCustomView(R.layout.main_title_bar);
			actionBar.setDisplayShowCustomEnabled(true);
			actionBar.setDisplayShowHomeEnabled(false);
			actionBar.setDisplayShowTitleEnabled(false);
			
			actionBar.show();
		}
		TextView tv = (TextView)findViewById(R.id.title_tv_m);
		tv.setText(R.string.warning_list);
		
		lv.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				showDeleteDialog(position);
				return false;
			}
		});
	}
	
	private void showDeleteDialog(final int position) {
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.delete_warning);
		
		builder.setPositiveButton(R.string.ok, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				wlAdapter.removeItem(position);
				wlAdapter.notifyDataSetChanged();
			}
		});
		
		builder.setNegativeButton(R.string.cancel, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.cancel();
			}
		});
		AlertDialog deleteDilDialog = builder.create();
		deleteDilDialog.show();
	}
	
	public void cancelNotification() {
		Intent intent = getIntent();
		Bundle extras = intent.getExtras();
		Log.e(TAG, "id1:" + intent.getIntExtra("id", 0));
        if (extras == null) {
            return;
        }
		int id = extras.getInt("id");
		Log.e(TAG, "id:" + id);
		NotificationManager notificationManager =
	            (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);     
	   notificationManager.cancel(id);
	}

}
