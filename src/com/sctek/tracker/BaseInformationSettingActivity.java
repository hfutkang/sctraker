package com.sctek.tracker;

import java.io.File;
import java.io.FileOutputStream;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class BaseInformationSettingActivity extends Activity {
	
	private final String TAG = "Baseinformation";
	
	private static final int IMAGE_REQUEST_CODE = 0;
	private static final int CAMERA_REQUEST_CODE = 1;
	private static final int RESULT_REQUEST_CODE = 2;
	
	private static final String IMAGE_FILE_NAME = "faceImage.jpg";
	private String[] items = new String[] { "选择本地图片", "拍照" };
	
	private String imagePath;
	private String deviceId;
	
	private ImageView lableIv;
	private EditText nameEt;
	private EditText deviceNumEt;
//	private ImageView backIb;
	private TextView title;
	
	private TrackerApplication mApplication;
	private SharedPreferences sharepreferences;
	private DeviceListViewData dd;
	
	Bitmap photo;
	
	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_base_information_setting);
		
		ActionBar actionBar = this.getActionBar();
		if(actionBar != null) {
			
			actionBar.setCustomView(R.layout.main_title_bar);
			actionBar.setDisplayShowCustomEnabled(true);
			actionBar.setDisplayShowHomeEnabled(false);
			actionBar.setDisplayShowTitleEnabled(false);
			
			actionBar.show();
		}
		
		initUiView();
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.e(TAG, "onActivityResult");
		if(resultCode != RESULT_OK)
			return ;
		
		switch(requestCode) {
		
			case IMAGE_REQUEST_CODE:
				startPhotoZoom(data.getData());
				break;
				
			case CAMERA_REQUEST_CODE:
				
				if(hasSdcard()) {
					File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
					File tempFile = new File(path,IMAGE_FILE_NAME);
					startPhotoZoom(Uri.fromFile(tempFile));
				} else {
					Toast.makeText(BaseInformationSettingActivity.this
							, R.string.no_sdcard,Toast.LENGTH_LONG).show();
				}
				break;
				
			case RESULT_REQUEST_CODE:
				if(data != null)
					setImageToView(data);
				break;
				
			default:
				break;
		}
		
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	public void initUiView() {
		Log.e(TAG, "initUiView");
		mApplication = (TrackerApplication)getApplication();
		sharepreferences = getSharedPreferences(
				"hasnumber", Activity.MODE_PRIVATE);
		
		deviceId = getIntent().getStringExtra("deviceid");
		
		int i = mApplication.searchDevice(deviceId);
		dd = mApplication	.getDeviceList().get(i);
		
		lableIv = (ImageView)findViewById(R.id.device_lable_b);
		nameEt = (EditText)findViewById(R.id.device_name_et_b);
		deviceNumEt = (EditText)findViewById(R.id.device_num_et_b);
		View dNumView = findViewById(R.id.device_num_layout_b);
//		backIb = (ImageView)findViewById(R.id.back_bt_m);
		title = (TextView)findViewById(R.id.title_tv_m);
		
		title.setText(R.string.base_information);
		
		File f = new File(dd.imagePath);
		if (f.exists()) {
			   Bitmap bm = BitmapFactory.decodeFile(dd.imagePath);
			   Drawable drawable=new BitmapDrawable(bm);
			   lableIv.setImageDrawable(drawable);
		    }
		nameEt.setText(dd.name);
		if(dd.isMaster.equals("true"))
			deviceNumEt.setText(dd.deviceNum);
		else
			dNumView.setVisibility(View.GONE);
		
		lableIv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showImagePickDialog();
			}
		});
		
		Button saveBt = (Button)findViewById(R.id.save_bt);
		
		saveBt.setOnClickListener(new OnClickListener() {
			
			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub			
				String name = nameEt.getText().toString();
				String newNum = null;
				if(dd.isMaster.equals("true")) {
					 newNum = deviceNumEt.getText().toString();
					
					if(newNum.isEmpty()) {
						Toast.makeText(BaseInformationSettingActivity.this
								, R.string.sim_is_empty, Toast.LENGTH_SHORT).show();
						return;
					}
					
					dd.deviceNum = newNum;
					SharedPreferences.Editor editor = sharepreferences.edit();
					editor.remove(dd.deviceNum);
					editor.putBoolean(newNum, true);
					editor.commit();
				}
				dd.name = name;
				
				if(photo != null)
					saveImage(photo);
				
				mApplication.updateDevice(dd.deviceId);
//				updatePreference(deviceNum, newNum);

				setResult(RESULT_OK);
				finish();
			}
		});
		
//		backIb.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				setResult(RESULT_CANCELED);
//				finish();
//			}
//		});
	}
	
	public void showImagePickDialog() {
		
		new AlertDialog.Builder(this)
				.setTitle(R.string.set_lable)
				.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
							Intent intentFromGallery = new Intent();
							intentFromGallery.addCategory(Intent.CATEGORY_OPENABLE);
							intentFromGallery.setType("image/*");
							intentFromGallery
									.setAction(Intent.ACTION_GET_CONTENT);
							startActivityForResult(intentFromGallery,
									IMAGE_REQUEST_CODE);
							break;
						case 1:
							Intent intentFromCapture = new Intent(
									MediaStore.ACTION_IMAGE_CAPTURE);
							if (hasSdcard()) {
								File path = Environment.getExternalStoragePublicDirectory(
										Environment.DIRECTORY_DCIM);
								File file = new File(path,IMAGE_FILE_NAME);
								intentFromCapture.putExtra(
										MediaStore.EXTRA_OUTPUT,
										Uri.fromFile(file));
							}
							startActivityForResult(intentFromCapture,CAMERA_REQUEST_CODE);
							break;
						}
					}
				})
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				}).show();
	}
	
	public void startPhotoZoom(Uri uri) {    
	    
		if (uri == null) {  
            Log.i("tag", "The uri is not exist.");  
            return;  
        }  
		 Intent intent = new Intent("com.android.camera.action.CROP");  
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {  
			String url=ImageCropUtils.getPath(this,uri); 
			Log.e(TAG,"url1:" + url);
			intent.setDataAndType(Uri.fromFile(new File(url)), "image/*");  
		}else{  
			Log.e(TAG,"url:" + uri.getPath());
		    intent.setDataAndType(uri, "image/*");  
		}  
        // 设置裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 250);    
        intent.putExtra("outputY", 250);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, RESULT_REQUEST_CODE);
    }
	
	private void setImageToView(Intent data) {
		 Log.e(TAG, "setImageToView");
        Bundle extras = data.getExtras();
        if (extras != null) {
        	photo = extras.getParcelable("data");
        	Drawable drawable = new BitmapDrawable(photo);
        	lableIv.setImageDrawable(drawable);
        }
        
    }
	
	public void saveImage(Bitmap bm) {
		Log.e(TAG, "saveImage");
		String dirPath = getFilesDir()
				.getAbsolutePath() + "/avatar";
		File file = new File(dirPath);
		
		if(!file.exists()) {
			file.mkdir();
		}
		
		File avatarFile = new File(dd.imagePath);
		try {
			
			avatarFile.createNewFile();
			FileOutputStream fo = new FileOutputStream(avatarFile);
			bm.compress(Bitmap.CompressFormat.PNG, 100, fo);
			fo.flush();
			fo.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	public boolean hasSdcard(){
        String state = Environment.getExternalStorageState();
        if(state.equals(Environment.MEDIA_MOUNTED)){
                return true;
        }else{
                return false;
        }
	}
	
	public void updatePreference(String oldNum, String newNum) {
		
		int lf = sharepreferences.getInt(oldNum + "lfrequence", 20);
		int rf = sharepreferences.getInt(oldNum + "rfrequence", 30);
		
		SharedPreferences.Editor editor = sharepreferences.edit();
		editor.remove(oldNum + "lfrequence");
		editor.remove(oldNum + "rfrequence");
		
		editor.putInt(newNum + "rfrequence", lf);
		editor.putInt(newNum + "rfrequence", rf);
		editor.commit();
	}

}
