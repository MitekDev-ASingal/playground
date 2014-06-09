package com.miteksystems.misnaphelloworld;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.miteksystems.misnap.MiSnap;
import com.miteksystems.misnap.MiSnapAPI;

/*
 * entry activity for MiSnapHelloWorld
 * This exercises the MiSnap API by calling MiSnap for a result with
 * mostly default parameters and awaiting the callback for onActivityResult
 */
public class MiSnapHelloWorld extends Activity {
//	static final int AUTO_INTERVAL_MILLIS = 1000;
	int RSS_DOWNLOAD_REQUEST_CODE=1;
	
	Handler mHandler;
	boolean mStarted = false;
	boolean mManualModeStart=false;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
        mStarted = false;
        if (requestCode == MiSnapAPI.RESULT_PICTURE_CODE && resultCode == Activity.RESULT_OK && data != null)
		{
        	// Image returned successfully
        	byte[] image = data.getByteArrayExtra(MiSnapAPI.RESULT_PICTURE_DATA);
			mHandler.removeCallbacks(mStart);
//            mHandler.postDelayed(mStart, AUTO_INTERVAL_MILLIS);
		}
		else if(requestCode == MiSnapAPI.RESULT_PICTURE_CODE && resultCode == Activity.RESULT_OK && data == null) {
        	// Image canceled, stop
        	Toast.makeText(this, "MiSnap canceled", Toast.LENGTH_LONG).show();
		} else if (requestCode == RSS_DOWNLOAD_REQUEST_CODE) {
	        switch (resultCode) {
	            case ExceptionResult.INVALID_URL_CODE:
	            	//handle the invalid URL error here
	                break;
	            case ExceptionResult.ERROR_CODE:
	            	//handle the error code here
	                break;
	            case ExceptionResult.RESULT_CODE:
	            	//get the result
	            	mManualModeStart = data.getBooleanExtra(ExceptionResult.EXCEPTION_RESULT_EXTRA, false);
	                //call the MiSnap at this point
	                mHandler = new Handler();
	                mHandler.post(mStart);
	                break;
	        }
	    }else if (resultCode == Activity.RESULT_CANCELED) {
			// Camera not working or not available, stop
        	Toast.makeText(this, "Camera not working or canceled", Toast.LENGTH_LONG).show();
		}
	}
	
	// Start MiSnap
	Runnable mStart = new Runnable() {
		public void run() {
			mStarted = true;
			JSONObject jjs = null;
			try {
				jjs = new JSONObject();

				/////////////////  Override defaults
				
				jjs.put(MiSnapAPI.Name, "CheckFront");	// set job file
				if(mManualModeStart == true) {
					jjs.put(MiSnapAPI.AllowVideoFrames, "0");
					jjs.put(MiSnapAPI.CameraVideoAutoCaptureProcess, "0");
				}
				/*
				jjs.put(MiSnapAPI.RequiredCompressionLevel, "30");
				//jjs.put(MiSnapAPI.CameraViewfinderMinFill, "400");	// For DLs
				jjs.put(MiSnapAPI.CameraViewfinderMinFill, "300");	// For ACH, REMITTANCE
				jjs.put(MiSnapAPI.CameraTimeoutInSeconds, "15");
				//jjs.put(MiSnapAPI.CameraAutoCaptureProcess, "2");	// Test for old MIP parameter compliance
				jjs.put(MiSnapAPI.CameraBrightness, "400");
				jjs.put(MiSnapAPI.CameraGForce, "2");
				jjs.put(MiSnapAPI.CameraSharpness, "400");
				//jjs.put(MiSnapAPI.CameraFlash, "1"); // off=0, auto=1, on=2, torch=3
				//jjs.put(MiSnapAPI.DeviceID, "whatever");
				
				jjs.put(MiSnapAPI.CameraVideoAutoCaptureProcess, "1");
				jjs.put(MiSnapAPI.CameraViewfinderBoundingBox, "1"); // 1=MiSnap, 0=Manual
				*/
				// All other non-default parameters set into jjs here
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			Intent i = new Intent(MiSnapHelloWorld.this, MiSnap.class);
			i.putExtra(MiSnapAPI.JOB_SETTINGS, jjs.toString());
			startActivityForResult(i, MiSnapAPI.RESULT_PICTURE_CODE);
		}
	};
	
	public void onSnapIt(View v) {
		if(!mStarted) {
			Log.i("onSnapIt", "device is " + android.os.Build.MODEL);
	        PendingIntent pendingResult = createPendingResult(
	        	    RSS_DOWNLOAD_REQUEST_CODE, new Intent(), 0);
	        	Intent intent = new Intent(getApplicationContext(), ExceptionResult.class);
	        	intent.putExtra(ExceptionResult.MISNAP_VERSION, "2.0.7");
	        	intent.putExtra(ExceptionResult.DEVICE_MODEL, "Nexus 4"/*android.os.Build.MODEL*/);
	        	intent.putExtra(ExceptionResult.PENDING_RESULT_EXTRA, pendingResult);
	        	startService(intent);
		}
	}

    @Override
    public void onBackPressed() {
    	if(mStart != null) {
    		mHandler.removeCallbacks(mStart);
    	}
    	finish();
    }
    
}