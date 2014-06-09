package com.miteksystems.misnaphelloworld;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

public class ExceptionResult extends IntentService {
	private static final String TAG = ExceptionResult.class.getSimpleName();
	private static String APP_SERVER_URL = "http://aetest.mitek.dom/AppServer/api/exceptions";
    public static final String PENDING_RESULT_EXTRA = "pending_result";
    public static final String MISNAP_VERSION = "MISNAP_VERSION";
    public static final String DEVICE_MODEL = "DEVICE_MODEL";
    public static final String EXCEPTION_RESULT_EXTRA = "EXCEPTION_RESULT_EXTRA";

    public static final int RESULT_CODE = 0;
    public static final int INVALID_URL_CODE = 1;
    public static final int ERROR_CODE = 2;
    
    String mMisnapVersion=null;
    String mDeviceModel=null;

    public ExceptionResult(String name) {
		super(TAG);
		// TODO Auto-generated constructor stub
	}
    
    public ExceptionResult() {
		super(TAG);
		// TODO Auto-generated constructor stub
	}

    @Override
    protected void onHandleIntent(Intent intent) {
    	HttpClient httpclient=null;
    	boolean bReturnValue=false;
    	
        PendingIntent reply = intent.getParcelableExtra(PENDING_RESULT_EXTRA);
        mMisnapVersion = intent.getStringExtra(MISNAP_VERSION);
        mDeviceModel = intent.getStringExtra(DEVICE_MODEL);
        try {
        	try {
        		List<NameValuePair> params = new LinkedList<NameValuePair>();
        		params.add(new BasicNameValuePair("misnapversion", mMisnapVersion));
                params.add(new BasicNameValuePair("model", mDeviceModel));
                String paramString = URLEncodedUtils.format(params, "utf-8");
                String mAppServerURL = APP_SERVER_URL;
                if(!mAppServerURL.endsWith("?"))
                	mAppServerURL += "?";
                mAppServerURL += paramString;
                HttpUriRequest request = new HttpGet(mAppServerURL);
	            if(request != null) {
		        	httpclient = new DefaultHttpClient();
		        	if(httpclient != null) {
			        	HttpResponse response = httpclient.execute(request);
			        	if(response != null) {
				            HttpEntity entity = response.getEntity();
				            if (entity != null && response.getStatusLine().getStatusCode() == 200) {
				                if( entity.getContentLength() > 0) {
				                	String responseStr = EntityUtils.toString(entity);
				                	JSONObject jresponse = new JSONObject(responseStr);
				                	bReturnValue = jresponse.getBoolean("IsException");
				                }
				            }
			        	}
		        	}
	        	}           	
                Intent result = new Intent();
                result.putExtra(EXCEPTION_RESULT_EXTRA, bReturnValue);
                reply.send(this, RESULT_CODE, result);
            } catch (IOException exc) {
                reply.send(INVALID_URL_CODE);
            } catch (Exception exc) {
                // could do better by treating the different sax/xml exceptions individually
                reply.send(ERROR_CODE);
            }finally {
	            // When HttpClient instance is no longer needed,
	            // shut down the connection manager to ensure
	            // immediate deallocation of all system resources
	        	if(httpclient != null) {
	        		httpclient.getConnectionManager().shutdown();
	        	}
	        }
        } catch (PendingIntent.CanceledException exc) {
            Log.i(TAG, "reply cancelled", exc);
        }
    }
}