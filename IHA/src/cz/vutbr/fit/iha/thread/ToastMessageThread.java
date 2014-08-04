/**
 * 
 */
package cz.vutbr.fit.iha.thread;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

/**
 * @author ThinkDeep
 * 
 */
public class ToastMessageThread implements Runnable{

	private static final String TAG = ToastMessageThread.class.getSimpleName();

	private String mMessage;
	private Activity mActivity;
	
	/**
	 * Constructor
	 * @param activity where the toast will be shown (must by alive)
	 * @param message to be shown
	 */
	public ToastMessageThread(Activity activity, String message) {
		mActivity = activity;
		mMessage = message;
	}
	
	/**
	 * Constructor
	 * @param activity where the toast will be shown (must by alive)
	 * @param messageResourceId of string to be shown 
	 */
	public ToastMessageThread(Activity activity, int messageResourceId){
		mActivity = activity;
		mMessage = mActivity.getString(messageResourceId);
	}

	@Override
	public void run() {
		Log.d(TAG, mMessage);
		Toast.makeText(mActivity, mMessage, Toast.LENGTH_LONG).show();
	}
	
	/**
	 * Show the message
	 */
	public void start(){
		mActivity.runOnUiThread(this);
	}

}
