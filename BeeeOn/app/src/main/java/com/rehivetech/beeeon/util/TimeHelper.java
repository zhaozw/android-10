package com.rehivetech.beeeon.util;

import android.content.SharedPreferences;

import com.rehivetech.beeeon.household.gate.Gate;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class TimeHelper {

	private SharedPreferences mPrefs;

	// private Gate mAdapter;

	public TimeHelper(SharedPreferences prefs/* , Gate gate */) {
		mPrefs = prefs;
		// mAdapter = gate;
	}

	/*
	 * private final DateTimeZone mTimezone;
	 * 
	 * public TimeFormatter(SharedPreferences prefs, Gate gate) { mTimezone = Timezone.fromPreferences(prefs).getDateTimeZone(gate); }
	 */

	private boolean useLocalTimezone() {
		Timezone.BaseItem item = new Timezone().fromSettings(mPrefs);
		return item.getId() == Timezone.ACTUAL;
	}

	public DateTimeZone getDateTimeZone(Gate gate) {
		boolean useLocalTime = useLocalTimezone() || gate == null;

		int offsetInMillis = (gate != null ? gate.getUtcOffset() : 0) * 60 * 1000;
		return useLocalTime ? DateTimeZone.getDefault() : DateTimeZone.forOffsetMillis(offsetInMillis);
	}

	public DateTimeFormatter getFormatter(String pattern, Gate gate) {
		DateTimeZone zone = getDateTimeZone(gate);
		return DateTimeFormat.forPattern(pattern).withZone(zone);
	}

	/**
	 * Return string with formatted time (if it is 23 hours ago, it show also date)
	 *
	 * @param lastUpdate
	 * @param gate       If null, then it will use local timezone
	 * @return
	 */
	public String formatLastUpdate(DateTime lastUpdate, Gate gate) {
		boolean isTooOld = lastUpdate.plusHours(23).isBeforeNow();
		DateTimeFormatter fmt = isTooOld ? DateTimeFormat.shortDateTime() : DateTimeFormat.mediumTime();

		DateTimeZone zone = getDateTimeZone(gate);
		return fmt.withZone(zone).print(lastUpdate);
	}

	/**
	 * Return string with formatted date time
	 *
	 * @param time
	 * @param gate If null, then it will use local timezone
	 * @return
	 */
	public String formatTime(DateTime time, Gate gate) {
		return DateTimeFormat.shortDateTime().withZone(getDateTimeZone(gate)).print(time);
	}

}
