package com.rehivetech.beeeon.gcm;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.model.GcmModel;
import com.rehivetech.beeeon.util.Utils;

public class GcmRegisterRunnable implements Runnable {
	public static final String TAG = GcmRegisterRunnable.class.getSimpleName();

	/**
	 * Minimum delay in milliseconds after register GCM fail and then exponentially more.
	 */
	private static final int MIN_SLEEP_TIME_GCM = 5;
	private final Context mContext;
	private final Integer mMaxAttempts;
	private final String mOldGcmId;
	private String mNewGcmId = null;

	/**
	 * @param context
	 * @param maxAttempts Maximum attempts to get GCM ID, null for infinity
	 */
	public GcmRegisterRunnable(Context context, Integer maxAttempts) {
		mContext = context.getApplicationContext();
		mMaxAttempts = maxAttempts;
		mOldGcmId = Controller.getInstance(mContext).getGcmModel().getGCMRegistrationId();
	}

	@Override
	public void run() {
		Controller controller = Controller.getInstance(mContext);
		GcmModel gcmModel = controller.getGcmModel();

		// if there is no limit, set lower priority of this thread
		if (mMaxAttempts == null) {
			// Moves the current Thread into the background
			android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
		}

		// if there is not Internet connection, locally invalidate and next event will try again to get new GCM ID 
		if (!Utils.isInternetAvailable(mContext)) {
			Log.w(TAG, Constants.GCM_TAG + "No Internet, locally invalidate GCM ID");
			gcmModel.setGCMIdLocal("");
			return;
		}

		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(mContext);
		int timeToSleep = MIN_SLEEP_TIME_GCM;
		int attempt = 0;
		while (mNewGcmId == null || mNewGcmId.isEmpty()) {
			if (mMaxAttempts != null && attempt > mMaxAttempts) {
				break;
			}
			attempt++;

			try {
				mNewGcmId = gcm.register(Constants.PROJECT_NUMBER);
			} catch (Exception e) {
				Log.e(TAG, Constants.GCM_TAG + "Error: attempt n." + String.valueOf(attempt) + " :" + e.getMessage());
				/*
				 * No matter how many times you call register, it will always fail and throw an exception on some
				 * devices. On these devices we need to get GCM ID this way.
				 */
				// final String registrationId =
				// context.getIntent().getStringExtra(
				// "registration_id");
				// if (registrationId != null && registrationId != "") {
				// mRegId = registrationId;
				// break;
				// }
				try {
					Thread.sleep(timeToSleep);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				timeToSleep = timeToSleep * 2;
				continue;
			}
		}

		Log.i(TAG, Constants.GCM_TAG + "Module registered, attempt number " + String.valueOf(attempt) + ", registration ID=" + mNewGcmId);

		// if new GCM ID is different then the old one, delete old on server side and apply new one
		if (!mOldGcmId.equals(mNewGcmId)) {
			gcmModel.deleteGCM(controller.getActualUser().getId(), mOldGcmId);

			// Persist the regID - no need to register again.
			gcmModel.setGCMIdLocal(mNewGcmId);
			gcmModel.setGCMIdServer(mNewGcmId);

		} else {
			// save it just locally to update app version
			gcmModel.setGCMIdLocal(mNewGcmId);
			Log.i(TAG, Constants.GCM_TAG + "New GCM ID is the same, no need to change");
		}
	}

}
