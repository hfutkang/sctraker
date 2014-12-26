package com.sctek.tracker;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.DefaultClientConnection;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.net.SSLCertificateSocketFactory;

public class CustomHttpClient {
	
	private static HttpClient customHttpClient;
	
	private CustomHttpClient(){
		
	}
	
	public static synchronized HttpClient getHttpClient(){
		
		if(customHttpClient == null){
			
			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, 
					HTTP.DEFAULT_CONTENT_CHARSET);
			HttpProtocolParams.setUseExpectContinue(params, true);
			HttpProtocolParams.setUserAgent(params, "Mozilla/5.0 (Linux; U; Android 2.2.1; en-us;"
					+ "Nexus One Build/FRG83) AppleWebKit/533.1 (KHML,like Gecko) Version/4.0 Mobile "
					+ "Safari/533.1");
			
			ConnManagerParams.setTimeout(params, 1000);
			
			HttpConnectionParams.setConnectionTimeout(params, 3000);
			HttpConnectionParams.setSoTimeout(params, 5000);
			
			SchemeRegistry schReg = new SchemeRegistry();
			schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
			
			ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params, schReg);
			
			customHttpClient = new DefaultHttpClient(conMgr, params);
		}
		
		return customHttpClient;
		
	}

	public Object clone() throws CloneNotSupportedException {
		
		throw new CloneNotSupportedException();
		
	}
}
