package com.rehivetech.beeeon.gcm.notification;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.rehivetech.beeeon.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by Martin on 22. 4. 2015.
 */
public class DeviceLowSignalNotification extends VisibleNotification {
	public static final String TAG = DeviceLowSignalNotification.class.getSimpleName();

	private int mGateId;
	private String mModuleId;
	private int mSignalLevel;

	private DeviceLowSignalNotification(int msgid, long time, NotificationType type, boolean read, int gateId, String moduleId, int signalLevel) {
		super(msgid, time, type, read);
		mGateId = gateId;
		mModuleId = moduleId;
		mSignalLevel = signalLevel;
	}

	protected static DeviceLowSignalNotification getInstance(Integer msgId, Long time, NotificationType type, Bundle bundle) throws NullPointerException, IllegalArgumentException {
		DeviceLowSignalNotification instance = null;

		try {
			Integer gateId = Integer.valueOf(bundle.getString("gateid"));
			String moduleId = bundle.getString("did");
			Integer batterylevel = Integer.valueOf(bundle.getString("batt"));

			if (gateId == null || moduleId == null || batterylevel == null) {
				Log.d(TAG, "DeviceAdded: some compulsory value is missing.");
				return null;
			}

			instance = new DeviceLowSignalNotification(msgId, time, type, false, gateId, moduleId, batterylevel);
		} catch (IllegalArgumentException | NullPointerException e) {
			return instance;
		}

		return instance;
	}

	protected static VisibleNotification getInstance(Integer msgId, Long time, NotificationType type, boolean isRead, XmlPullParser parser) throws IOException, XmlPullParserException, NumberFormatException {
		Integer gateId = null;
		String moduleId = null;
		Integer signalLevel = null;

		String text = null;
		int eventType = parser.getEventType();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			if (eventType == XmlPullParser.END_TAG &&
					parser.getName().equals("notif")) {
				break;
			}
			String tagname = parser.getName();
			switch (eventType) {
				case XmlPullParser.START_TAG:
					// ignore it
					break;

				case XmlPullParser.TEXT:
					text = parser.getText();
					break;

				case XmlPullParser.END_TAG:
					if (tagname.equalsIgnoreCase("gateid")) {
						gateId = Integer.valueOf(text);
					} else if (tagname.equalsIgnoreCase("did")) {
						moduleId = text;
					} else if (tagname.equalsIgnoreCase("batt")) {
						signalLevel = Integer.valueOf(text);
					}
					break;
				default:
					break;
			}
			eventType = parser.next();
		}

		if (gateId == null || moduleId == null || signalLevel == null) {
			Log.d(TAG, "Xml: Some compulsory value is missing.");
			return null;
		}

		return new DeviceLowSignalNotification(msgId, time, type, isRead, gateId, moduleId, signalLevel);

	}

	@Override
	protected void onGcmHandle(Context context) {
		// TODO notifikovat controller aby si stahl nove data, zobrzit notiifkaci a po kliknuti odkazazt na datail senzort
//		NotificationCompat.Builder builder = getBaseNotificationBuilder(context);
//
//		showNotification(context, builder);
	}

	@Override
	protected void onClickHandle(Context context) {
		// TODO
		Toast.makeText(context, "on click", Toast.LENGTH_LONG).show();
	}

	@Override
	protected String getMessage(Context context) {
		// TODO pridat lokalizovany string
		return "ahoj";
	}

	@Override
	protected String getName(Context context) {
		return context.getString(com.rehivetech.beeeon.R.string.notification_device_low_battery_low_signal_name_new_module);
	}
}
