package com.rehivetech.beeeon.gcm.notification;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by Martin on 22. 4. 2015.
 */
public class DeviceAddedNotification extends VisibleNotification {
	public static final String TAG = DeviceAddedNotification.class.getSimpleName();

	private String mGateId;
	private String mDeviceId;

	private DeviceAddedNotification(int msgid, long time, NotificationType type, boolean read, String gateId, String deviceId) {
		super(msgid, time, type, read);
		mGateId = gateId;
		mDeviceId = deviceId;
	}

	protected static DeviceAddedNotification getInstance(Integer msgId, Long time, NotificationType type, Bundle bundle) throws NullPointerException, IllegalArgumentException {
		try {
			String gateId = bundle.getString("gateid");
			String deviceId = bundle.getString("did");

			if (gateId == null || deviceId == null) {
				Log.d(TAG, "DeviceAdded: some compulsory value is missing.");
				return null;
			}

			return new DeviceAddedNotification(msgId, time, type, false, gateId, deviceId);
		} catch (IllegalArgumentException | NullPointerException e) {
			return null;
		}
	}

	protected static VisibleNotification getInstance(Integer msgId, Long time, NotificationType type, boolean isRead, XmlPullParser parser) throws IOException, XmlPullParserException, NumberFormatException {
		String gateId = null;
		String deviceId = null;

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
						gateId = text;
					} else if (tagname.equalsIgnoreCase("did")) {
						deviceId = text;
					}
					break;
				default:
					break;
			}
			eventType = parser.next();
		}

		if (gateId == null || deviceId == null) {
			Log.d(TAG, "Xml: Some compulsory value is missing.");
			return null;
		}

		return new DeviceAddedNotification(msgId, time, type, isRead, gateId, deviceId);

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
