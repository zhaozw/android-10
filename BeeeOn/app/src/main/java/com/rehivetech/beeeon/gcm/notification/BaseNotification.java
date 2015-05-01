/**
 *
 */
package com.rehivetech.beeeon.gcm.notification;

import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.network.xml.Xconstants;
import com.rehivetech.beeeon.util.Log;

import java.util.Calendar;
import java.util.TimeZone;

public abstract class BaseNotification implements GcmNotification {

	public static final String TAG = BaseNotification.class.getSimpleName();
//	public static final String DATEFORMAT = "yyyy-MM-dd HH:mm:ss";
	private static final String SEPARATOR = "\\s+";
	private final Calendar mDate;
	private final int mId;
	private final NotificationType mType;
	private boolean mRead = false;
	private String mUserId = null;

	private Bundle mBundle;

	/**
	 * Constructor
	 */
	public BaseNotification(String userId, int msgid, long timestamp, NotificationType type, boolean read) {
		mId = msgid;

		/**
		 * Convert UTC timezone to local timezone
		 */
		TimeZone tz = TimeZone.getDefault();
		mDate = Calendar.getInstance();
		mDate.setTimeInMillis(timestamp);
		mDate.add(Calendar.MILLISECOND, tz.getOffset(mDate.getTimeInMillis()));

		mUserId = userId;
		mType = type;
		mRead = read;
	}

	public static GcmNotification parseBundle(Context context, Bundle bundle) {
		if (bundle == null) {
			return null;
		}

		Controller controller = Controller.getInstance(context);

		Log.d(TAG, bundle.toString());
		BaseNotification notification = null;
		try {
			NotificationName name = NotificationName.fromValue(bundle.getString(Xconstants.NOTIFICATION_NAME));
			Integer msgId = Integer.valueOf(bundle.getString(Xconstants.MSGID));
			String userId = bundle.getString(Xconstants.UID);
			Long time = Long.valueOf(bundle.getString(Xconstants.TIME));
			NotificationType type = NotificationType.fromValue(bundle.getString(Xconstants.TYPE));

			// control validity of message
			if (name == null || msgId == null || userId == null || time == null || type == null) {
				Log.w(TAG, "Some of compulsory values is missing");
				return null;
			}
			// control if actual user ID is the same
			if (!userId.equals(controller.getActualUser().getId())) {
				Log.w(TAG, "GCM: Sent user ID is different from actaul user ID. Deleting GCM on server.");
				controller.getGcmModel().deleteGCM(notification.getUserId(), null);
				return null;
			}

			notification = getInstance(name, msgId, userId, time, type, bundle);
		}
		// catch nullpointer if some of bundle values doesn't exist
		// catch IllegalArgumentException if cannot cast
		catch (NullPointerException | IllegalArgumentException e) {
			Log.w(TAG, "Nullpointer or cannot parse to enum/number: " + e.getLocalizedMessage());

			return null;
		}
		if (notification != null) {
			notification.setBundle(bundle);
		}
		return notification;
	}

	@Nullable
	private static BaseNotification getInstance(NotificationName name, Integer msgId, String userId, Long time,
												NotificationType type, Bundle bundle) throws NullPointerException, IllegalArgumentException {
		BaseNotification notification = null;

		switch (name) {
			case WATCHDOG:
				notification = WatchdogNotification.getInstance(name, msgId, userId, time, type, bundle);
				break;
			case ADAPTER_ADDED:
				notification = AdapterAddedNotification.getInstance(name, msgId, userId, time, type, bundle);
				break;
			case SENSOR_ADDED:
				notification = SensorAddedNotification.getInstance(name, msgId, userId, time, type, bundle);
				break;
			case DELETE_NOTIF:
				notification = DeleteNotification.getInstance(name, msgId, userId, time, type, bundle);
				break;
			case URI:
				notification = UriNotification.getInstance(name, msgId, userId, time, type, bundle);
				break;
		}

		return notification;
	}

	protected void setBundle(Bundle bundle) {
		mBundle = bundle;
	}

	protected Bundle getBundle() {
		return mBundle;
	}

	abstract protected void onGcmHandle(Context context, Controller controller);
	abstract protected void onClickHandle(Context context, Controller controller);

	public void onClick(final Context context) {
		final Controller controller = Controller.getInstance(context);

		Thread t = new Thread() {
			public void run() {
				controller.getGcmModel().setNotificationRead(String.valueOf(getId()));
			}
		};
		t.start();

		onClickHandle(context, controller);
	}

	public void onGcmRecieve(final Context context) {
		final Controller controller = Controller.getInstance(context);

		if (context == null || controller == null) {
			Log.e(TAG, "onGcmRecieve(): context or controller is NULL");
			return;
		}

		// if somebody already handle notification using controller observer, then do nothing
		if (passToController(controller)) {
			return;
		}

		onGcmHandle(context, controller);
	}

	/**
	 * Send notification to controller.
	 *
	 * @param controller Actual controller
	 * @return True if notification was handled by controller. False otherwise.
	 */
	protected boolean passToController(Controller controller) {
		return controller.getGcmModel().receiveNotification(this);
	}

	/**
	 * @return Email if notification was received by GCM. Null otherwise.
	 */
	public String getUserId() {
		return mUserId;
	}

	/**
	 * @return the mDate
	 */
	public Calendar getDate() {
		return mDate;
	}

	/**
	 * @return the notification ID
	 */
	public int getId() {
		return mId;
	}

	/**
	 * @return If notification was already read or not.
	 */
	public boolean isRead() {
		return mRead;
	}

	/**
	 * @param read the mRead to set
	 */
	public void setRead(boolean read) {
		this.mRead = read;
	}

	/**
	 * @return Notification type (info, advert, alert, control)
	 */
	public NotificationType getType() {
		return mType;
	}

	/**
	 * @return True if notification is visible for user. False otherwise.
	 */
	public boolean isVisible() {
		return getType() != NotificationType.CONTROL;
	}


	protected void showNotification(Context context, NotificationCompat.Builder builder) {
		// Gets an instance of the NotificationManager service
		NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

		// Builds the notification and issues it.
		mNotifyMgr.notify(getId(), builder.build());
	}
}
