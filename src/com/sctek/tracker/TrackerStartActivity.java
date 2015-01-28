package com.sctek.tracker;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.text.DecimalFormat;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.sax.StartElementListener;
import android.text.AlteredCharSequence;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class TrackerStartActivity extends Activity {
	
	private final static String TAG = "TrackerstartActivity";
	
	private final static String DOWNLOAD_FOLDER = "sctracker";
	
	private final static int INSTALL_PACKAGE_REQUEST = 2;
	
	private final static String url = "http://www.sctek.cn:9090/tracercmd?cmd=getlastedition";
	private String result;
	private EditionInfo ei;
	
	private int currentV;
	private HttpClient client;
	private HttpGet getRequest;
	
	private DownloadManager        downloadManager;
	private DownloadManagerPro     downloadManagerPro;
	private long                   downloadId           = 0;
	
	private ProgressBar            downloadProgress;
	private TextView               downloadSize;
	private TextView               downloadPrecent;

    private MyHandler              handler;

    private DownloadChangeObserver downloadObserver;
    private CompleteReceiver       completeReceiver;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tracker_start);
		
		Thread.setDefaultUncaughtExceptionHandler(CrashHandler.getInstance());
		
		client = CustomHttpClient.getHttpClient();
		getRequest = new HttpGet(url);
		
		PackageManager pm = getPackageManager();
		try {
			PackageInfo pi = pm.getPackageInfo(getPackageName(), 
					PackageManager.GET_CONFIGURATIONS);
			currentV = pi.versionCode;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		handler = new MyHandler();
		downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);
		downloadManagerPro = new DownloadManagerPro(downloadManager);
		
		downloadObserver = new DownloadChangeObserver();
		completeReceiver = new CompleteReceiver();
		/** register download success broadcast **/
		registerReceiver(completeReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
		
		new Handler().postDelayed(new Runnable() {
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				new Thread(runnable).start();
			}
		}, 0);
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		getContentResolver().registerContentObserver(DownloadManagerPro.CONTENT_URI, true, downloadObserver);
	}
	
	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		getContentResolver().unregisterContentObserver(downloadObserver);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		unregisterReceiver(completeReceiver);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == INSTALL_PACKAGE_REQUEST) {
			handler.sendEmptyMessage(R.id.install_canceled);
		}
	}
	
	public String httpRequestExecute(HttpClient httpclient, HttpGet httpget) throws Exception {
		
		BufferedReader in = null;
		
		try{
			
			HttpResponse response = httpclient.execute(httpget);
			in = new BufferedReader(new InputStreamReader(
					response.getEntity().getContent()));
			
			StringBuffer result = new StringBuffer();
			String line;
			
			while((line = in.readLine()) != null){
				result.append(line);
			}
			in.close();
			
			return result.toString();
		}finally {
			if (in != null) {
				try {
					in.close();
				}catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	Runnable runnable = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			int retry = 0;
			boolean success = false;
			// TODO Auto-generated method stub
			while(retry < 3 && !success) {
				
				try{
					result = httpRequestExecute(client,getRequest);
					success = true;
				} catch(Exception e){
					e.printStackTrace();
					result = null;
					success = false;
					retry++;
					Log.e(TAG,"http error");
				}
			}
			if(success) {
				try {
					StringReader reader = new StringReader(result);
					SAXParserFactory factory = SAXParserFactory.newInstance();    
					SAXParser parser = factory.newSAXParser();    
					XMLReader xmlReader = parser.getXMLReader(); 
					xmlReader.setContentHandler(xmlHandler);
					xmlReader.parse(new InputSource(reader));
					Log.e(TAG, result);
					if(ei.version > currentV) {
						handler.sendEmptyMessage(R.id.has_new_version);
						return;
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			SharedPreferences sPref = PreferenceManager.
            		getDefaultSharedPreferences(TrackerStartActivity.this);
            	
    		if(sPref.contains("mynumber")) {
            	Intent intent = new Intent(
						TrackerStartActivity.this, MainActivity.class);
				startActivity(intent);
    		}
    		else {
    			Intent intent = new Intent(
    					TrackerStartActivity.this, MyPhoneNumberActivity.class);
    			startActivity(intent);
    		}
    		
			finish();
		}
	};
	
	public void showUpdateDialog() {
		
		AlertDialog.Builder builder = new Builder(this);
		builder.setTitle(R.string.new_edition);
		builder.setMessage("有新的软件版本\n" + ei.changes);
		builder.setPositiveButton(R.string.updatenow, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
				File folder = Environment.getExternalStoragePublicDirectory(DOWNLOAD_FOLDER);
                if (!folder.exists() || !folder.isDirectory()) {
                    folder.mkdirs();
                }
              File apkFile = new File(folder, ei.name + ".apk");
              if(apkFile.exists())
            	  install(apkFile.getPath());
              else {
            	  startDownload();
            	  showDownLoadProgressView();
                }
				
			}
		});
		
		builder.setNegativeButton(R.string.updatelater, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.cancel();
				
				SharedPreferences sPref = PreferenceManager.
                		getDefaultSharedPreferences(TrackerStartActivity.this);
                	
        		if(sPref.contains("mynumber")) {
                	Intent intent = new Intent(
    						TrackerStartActivity.this, MainActivity.class);
    				startActivity(intent);
        		}
        		else {
        			Intent intent = new Intent(
        					TrackerStartActivity.this, MyPhoneNumberActivity.class);
        			startActivity(intent);
        		}
        		
				finish();
			}
		});
		
		builder.create().show();
	}
	
	@SuppressLint("NewApi")
	public void showDownLoadProgressView(){
		
		LayoutInflater inflater = getLayoutInflater();
		View view = inflater.inflate(R.layout.download_progress_view, null);
		
		downloadProgress = (ProgressBar)view.findViewById(R.id.download_progress);
		downloadSize = (TextView)view.findViewById(R.id.download_size);
		downloadPrecent = (TextView)view.findViewById(R.id.download_precent);
		
		AlertDialog.Builder builder = new Builder(this);
		builder.setNegativeButton(R.string.cancel, new OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				File folder = Environment.getExternalStoragePublicDirectory(DOWNLOAD_FOLDER);
                if (!folder.exists() || !folder.isDirectory()) {
                    folder.mkdirs();
                }
              File apkFile = new File(folder, ei.name + ".apk");
              if(apkFile.exists())
            	  apkFile.delete();
             
              downloadManager.remove(downloadId);
              
              SharedPreferences sPref = PreferenceManager.
              		getDefaultSharedPreferences(TrackerStartActivity.this);
              	
          		if(sPref.contains("mynumber")) {
	                	Intent intent = new Intent(
	    						TrackerStartActivity.this, MainActivity.class);
	    				startActivity(intent);
          		}
          		else {
          			Intent intent = new Intent(
          					TrackerStartActivity.this, MyPhoneNumberActivity.class);
          			startActivity(intent);
          		}
          		
				finish();
			}
		});
		builder.setView(view);
		builder.create().show();
		updateView();
	}
	
	public void updateView() {
        int[] bytesAndStatus = downloadManagerPro.getBytesAndStatus(downloadId);
        handler.sendMessage(handler.obtainMessage(R.id.download_state, bytesAndStatus[0], bytesAndStatus[1], bytesAndStatus[2]));
    }
	
	DefaultHandler xmlHandler = new DefaultHandler(){
		String nodeName;
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
			nodeName = localName;
			if("update".equals(nodeName))
				ei = new EditionInfo();
		}
		
		@Override
		public void endElement(String uri, String localName, String qName)
				throws SAXException {
			// TODO Auto-generated method stub
			super.endElement(uri, localName, qName);
		}
		
		@Override
		public void characters(char[] ch, int start, int length)
				throws SAXException {
			// TODO Auto-generated method stub
			super.characters(ch, start, length);
			if("version".equals(nodeName)) {
				ei.version = Integer.valueOf(new String(ch, start, length));
			}
			else if("updatetime".equals(nodeName)) {
				ei.upTime = new String(ch, start, length);
			}
			else if("changes".equals(nodeName)) {
				ei.changes = new String(ch, start, length);
				ei.changes = ei.changes.replaceAll(",", "\n");
			}
			else if("url".equals(nodeName)) {
				ei.url = new String(ch, start, length);
			}
			else if("name".equals(nodeName)) {
				ei.name = new String(ch ,start, length);
			}
		}
		
		@Override
		public void endDocument() throws SAXException {
			// TODO Auto-generated method stub
			super.endDocument();
		}
	};
	
	private class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case R.id.download_state:
                    int status = (Integer)msg.obj;
                    if (isDownloading(status)) {

                        if (msg.arg2 < 0) {
                        		downloadProgress.setIndeterminate(true);
                            downloadPrecent.setText("0%");
                            downloadSize.setText("0M/0M");
                        } else {
                        		downloadProgress.setIndeterminate(false);
                            downloadProgress.setMax(msg.arg2);
                            downloadProgress.setProgress(msg.arg1);
                            downloadPrecent.setText(getNotiPercent(msg.arg1, msg.arg2));
                            downloadSize.setText(getAppSize(msg.arg1) + "/" + getAppSize(msg.arg2));
                           }
                       } 
                    break;
                case R.id.has_new_version:
                		showUpdateDialog();
                		break;
                case R.id.install_canceled:
	                	SharedPreferences sPref = PreferenceManager.
	                		getDefaultSharedPreferences(TrackerStartActivity.this);
	                	
	            		if(sPref.contains("mynumber")) {
		                	Intent intent = new Intent(
		    						TrackerStartActivity.this, MainActivity.class);
		    				startActivity(intent);
	            		}
	            		else {
	            			Intent intent = new Intent(
	            					TrackerStartActivity.this, MyPhoneNumberActivity.class);
	            			startActivity(intent);
	            		}
	            		
	    				finish();
	    				break;
                default:
                		break;
            }
        }
    }
	
    class DownloadChangeObserver extends ContentObserver {

        public DownloadChangeObserver() {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            updateView();
        }

    }

    class CompleteReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            /**
             * get the id of download which have download success, if the id is my id and it's status is successful,
             * then install it
             **/
            long completeDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (completeDownloadId == downloadId) {
                // if download successful, install apk
                if (downloadManagerPro.getStatusById(downloadId) == DownloadManager.STATUS_SUCCESSFUL) {
                    String apkFilePath = new StringBuilder(Environment.getExternalStorageDirectory().getAbsolutePath())
                            .append(File.separator).append(DOWNLOAD_FOLDER).append(File.separator)
                            .append(ei.name + ".apk").toString();
                    install(apkFilePath);
                }
            }
        }
    };
    
    public boolean install(String filePath) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        File file = new File(filePath);
        if (file != null && file.length() > 0 && file.exists() && file.isFile()) {
            i.setDataAndType(Uri.parse("file://" + filePath), "application/vnd.android.package-archive");
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivityForResult(i, INSTALL_PACKAGE_REQUEST);
            return true;
        }
        return false;
    }
    
    @SuppressLint("NewApi")
	public void startDownload() {
    	
    	DownloadManager.Request request = new DownloadManager.Request(Uri.parse(ei.url));
		request.setDestinationInExternalPublicDir(DOWNLOAD_FOLDER, ei.name + ".apk");
		request.setTitle(getString(R.string.download_notification_title));
		request.setDescription("meilishuo desc");
		request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
		request.setVisibleInDownloadsUi(true);
		// request.allowScanningByMediaScanner();
		// request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
		// request.setShowRunningNotification(false);
		// request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
		request.setMimeType("application/vnd.android.package-archive");
		downloadId = downloadManager.enqueue(request);
    }
	
	public static boolean isDownloading(int downloadManagerStatus) {
        return downloadManagerStatus == DownloadManager.STATUS_RUNNING
                || downloadManagerStatus == DownloadManager.STATUS_PAUSED
                || downloadManagerStatus == DownloadManager.STATUS_PENDING;
    }
	
	static final DecimalFormat DOUBLE_DECIMAL_FORMAT = new DecimalFormat("0.##");

    public static final int    MB_2_BYTE             = 1024 * 1024;
    public static final int    KB_2_BYTE             = 1024;
    
	public static CharSequence getAppSize(long size) {
        if (size <= 0) {
            return "0M";
        }

        if (size >= MB_2_BYTE) {
            return new StringBuilder(16).append(DOUBLE_DECIMAL_FORMAT.format((double)size / MB_2_BYTE)).append("M");
        } else if (size >= KB_2_BYTE) {
            return new StringBuilder(16).append(DOUBLE_DECIMAL_FORMAT.format((double)size / KB_2_BYTE)).append("K");
        } else {
            return size + "B";
        }
    }

    public static String getNotiPercent(long progress, long max) {
        int rate = 0;
        if (progress <= 0 || max <= 0) {
            rate = 0;
        } else if (progress > max) {
            rate = 100;
        } else {
            rate = (int)((double)progress / max * 100);
        }
        return new StringBuilder(16).append(rate).append("%").toString();
    }
	
	public class EditionInfo {
		String name;
		int version;
		String changes;
		String url;
		String upTime;
	}
}
