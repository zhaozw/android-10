package com.rehivetech.beeeon.base;

import android.content.Context;
import android.content.Intent;

import com.rehivetech.beeeon.activity.LoginActivity;
import com.rehivetech.beeeon.asynctask.CallbackTask;
import com.rehivetech.beeeon.asynctask.CallbackTaskManager;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gcm.INotificationReceiver;
import com.rehivetech.beeeon.gcm.notification.GcmNotification;
import com.rehivetech.beeeon.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Abstract parent for activities that requires logged in user
 * 
 * When user is not logged in, it will switch to LoginActivity automatically.
 */
public abstract class BaseApplicationActivity extends BaseActivity implements INotificationReceiver, CallbackTaskManager.ICallbackTaskManager {

	private static String TAG = BaseApplicationActivity.class.getSimpleName();
	private boolean triedLoginAlready = false;

	private CallbackTaskManager mCallbackTaskManager = new CallbackTaskManager();

	protected boolean isPaused = false;

	@Override
	public void onResume() {
		super.onResume();

		Controller controller = Controller.getInstance(this);

		if (!controller.isLoggedIn()) {
			if (!triedLoginAlready) {
				triedLoginAlready = true;
				redirectToLogin(this);
			} else {
				finish();
			}
			return;
		} else {
			triedLoginAlready = false;
		}

		controller.getGcmModel().registerNotificationReceiver(this);

		isPaused = false;
		onAppResume();
	}

	@Override
	public void onPause() {
		super.onPause();

		Controller controller = Controller.getInstance(this);
		controller.getGcmModel().unregisterNotificationReceiver(this);

		isPaused = true;
		onAppPause();
	}

	@Override
	public void onStop() {
		super.onStop();

		// Cancel and remove all remembered tasks
		mCallbackTaskManager.cancelAndRemoveAll();
	}

	public static void redirectToLogin(Context context) {
		Log.d(TAG,"Try to relogin");
		Intent intent = new Intent(context, LoginActivity.class);
		intent.putExtra(LoginActivity.BUNDLE_REDIRECT, true);
		intent.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);

		context.startActivity(intent);
	}

	/**
	 * This is called after onResume(), but only when user is correctly logged in
	 */
	protected void onAppResume() {
		// Empty default method
	}

	/**
	 * This is called after onPause()
	 */
	protected void onAppPause() {
		// Empty default method
	}

	@Override
	public <T> void executeTask(CallbackTask<T> task, T param) {
		mCallbackTaskManager.addTask(task);
		task.execute(param);
	}

	@Override
	public void executeTask(CallbackTask task) {
		mCallbackTaskManager.addTask(task);
		task.execute();
	}

	/**
	 * Method that receives Notifications.
	 */
	public boolean receiveNotification(final GcmNotification notification) {
		// FIXME: Leo (or someone else?) should implement correct handling of notifications (showing somewhere in activity or something like that?)

		return false;
	}
}
