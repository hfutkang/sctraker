package com.sctek.tracker;

import java.io.File;
import java.io.FileOutputStream;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class NewDeviceActivity extends Activity {
	
	private final String TAG = "NewDeviceActivity";
	
	private String dirPath;
	private String initialized;
	private String password;
	
	private String imageName;
	
	private ImageView lableIv;
	private EditText nameEt;
	private EditText deviceNumEt;
	private EditText confirmEt;
	private EditText passwordEt;
	private View confirmView;
	private View simView;
//	private ImageView backIb;
	private TextView title;
	
	private Intent resultIntent;
	
	private String[] items = new String[] { "选择本地图片", "拍照" };
	
    private static final String IMAGE_FILE_NAME = "faceImage.jpg";
      
    private static final int IMAGE_REQUEST_CODE = 4;
    private static final int CAMERA_REQUEST_CODE = 5;
    private static final int RESULT_REQUEST_CODE = 6;

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_device);
		Log.e(TAG, "onCreate");
		
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
	
	@Override
	protected void onResume() {
		Log.e(TAG, "onResume");
		// TODO Auto-generated method stub
		super.onResume();
	}
	
	@Override
	protected void onDestroy() {
		Log.e(TAG, "onDestroy");
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	public void initUiView() {
		
		dirPath = getFilesDir()
				.getAbsolutePath() + "/avatar";
		
		imageName = getIntent().getStringExtra("id");
		
		Intent intent = getIntent();
		initialized = intent.getStringExtra("initialized");
		password = intent.getStringExtra("pw");
		
		lableIv = (ImageView)findViewById(R.id.device_lable_n);
		nameEt = (EditText)findViewById(R.id.device_name_et_n);
		deviceNumEt = (EditText)findViewById(R.id.device_num_et_n);
		passwordEt = (EditText)findViewById(R.id.password_et_n);
		confirmEt = (EditText)findViewById(R.id.confirm_password_et_n);
		confirmView = findViewById(R.id.confirm_password_layout_n);
		simView= findViewById(R.id.device_num_layout_n);
		
		if(initialized.equals("1")) {
			confirmView.setVisibility(View.GONE);
			simView.setVisibility(View.GONE);
		}
//		backIb = (ImageView)findViewById(R.id.back_bt_m);
		title = (TextView)findViewById(R.id.title_tv_m);
		
		title.setText(R.string.new_device_title);
		
		lableIv.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showImagePickDialog();
			}
		});
		
		Button positiveBt = (Button)findViewById(R.id.add_device_bt);
//		Button negativeBt = (Button)findViewById(R.id.cancel_button_n);
		
		resultIntent = new Intent();
		
		positiveBt.setOnClickListener(new OnClickListener() {
			
			@SuppressLint("NewApi")
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String name = nameEt.getText().toString();
				String dnum = deviceNumEt.getText().toString();
				String pw = passwordEt.getText().toString();
				
				if(pw.length() != 6) {
					Toast.makeText(NewDeviceActivity.this
							, R.string.input_six_password, Toast.LENGTH_SHORT).show();
					passwordEt.setText("");
					return;
				} 
				if(initialized.equals("0")) {
					if(dnum.isEmpty()) {
						Toast.makeText(NewDeviceActivity.this
								, R.string.sim_is_empty, Toast.LENGTH_SHORT).show();
						return;
					}
					if(!pw.equals(confirmEt.getText().toString())) {
						Toast.makeText(NewDeviceActivity.this
								, R.string.password_confirm_fail, Toast.LENGTH_SHORT).show();
						passwordEt.setText("");
						confirmEt.setText("");
						return;
					}
					resultIntent.putExtra("name", name);
					resultIntent.putExtra("devicenum", dnum);
					resultIntent.putExtra("pw", pw);
				}
				else if(pw.equals(password)){
					resultIntent.putExtra("name", name);
				}
				else {
					Toast.makeText(NewDeviceActivity.this, 
							R.string.password_incorrect, Toast.LENGTH_SHORT).show();
					return;
				}
				setResult(RESULT_OK, resultIntent);
				finish();
			}
		});
		
//		negativeBt.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
//				setResult(RESULT_CANCELED);
//				finish();
//			}
//		});
		
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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.e(TAG, "onActivityResult");
		if(resultCode != RESULT_OK)
			return ;
		
		switch(requestCode) {
		
			case IMAGE_REQUEST_CODE:
				final Uri uri= data.getData();
				Log.e(TAG, uri.getPath());
				new Handler().postDelayed(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						startPhotoZoom(uri);
					}
				}, 0);
				
				break;
				
			case CAMERA_REQUEST_CODE:
				
				if(hasSdcard()) {
					File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
					File tempFile = new File(path,IMAGE_FILE_NAME);
					startPhotoZoom(Uri.fromFile(tempFile));
				} else {
					Toast.makeText(NewDeviceActivity.this
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
	
	public void showImagePickDialog() {
		
		new AlertDialog.Builder(this)
				.setTitle(R.string.set_lable)
				.setItems(items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case 0:
							Intent intentFromGallery = new Intent();
							intentFromGallery.setType("image/*");
							intentFromGallery.addCategory(Intent.CATEGORY_OPENABLE);
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
	
	public void showPasswordDialog() {
		
		LayoutInflater layoutInflater = LayoutInflater.from(this);
		View dialogView = layoutInflater.inflate(R.layout.devicenum_dialog, null);
		passwordEt = (EditText)dialogView.findViewById(R.id.device_num_et);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(NewDeviceActivity.this);
		builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				String pw = passwordEt.getText().toString();
				resultIntent.putExtra("password", pw);
				setResult(RESULT_OK, resultIntent);
				dialog.dismiss();
				finish();
			}
		});
		
		builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		});
		
		builder.setView(dialogView);
		builder.create().show();
	}
	
	private static final String IMAGE_FILE_LOCATION = "file:///sdcard/temp.jpg";
	
	public void startPhotoZoom(Uri uri) {   
		
		Uri imageUri = Uri.parse(IMAGE_FILE_LOCATION);
		Log.e(TAG, "startPhotoZoon");
		 if (uri == null) {  
             Log.i("tag", "The uri is not exist.");  
             return;  
         }  
		 Intent intent = new Intent("com.android.camera.action.CROP");  
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {  
			String url=ImageCropUtils.getPath(this,uri); 
			Log.e(TAG,"url:" + url);
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
//        intent.putExtra("noFaceDetection", true);
//        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, RESULT_REQUEST_CODE);
    }
	
	private void setImageToView(Intent data) {
		Log.e(TAG, "setImageToView");
        Bundle extras = data.getExtras();
        if (extras != null) {
        	Bitmap photo = extras.getParcelable("data");
        	if(photo != null) {
	        	Drawable drawable = new BitmapDrawable(photo);
	        	lableIv.setImageDrawable(drawable);
	        	saveImage(photo);
        	}
        }
        
    }
	
	public void saveImage(Bitmap bm) {
		
		File file = new File(dirPath);
		
		if(!file.exists()) {
			file.mkdir();
		}
		
		File avatarFile = new File(file, imageName + ".png");
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
	
	public static boolean hasSdcard(){
        String state = Environment.getExternalStorageState();
        if(state.equals(Environment.MEDIA_MOUNTED)){
                return true;
        }else{
                return false;
        }
	}
	
}
