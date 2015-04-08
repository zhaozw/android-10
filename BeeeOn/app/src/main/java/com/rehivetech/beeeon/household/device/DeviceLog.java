package com.rehivetech.beeeon.household.device;

import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.joda.time.Interval;

/**
 * Represents history of values for device.
 */
public class DeviceLog {
	private SortedMap<Long, Float> mValues = new TreeMap<Long, Float>();
	private DataType mType;
	private DataInterval mInterval;

	private float mMinValue;
	private float mMaxValue;

	public enum DataType {
		MINIMUM("min"), //
		AVERAGE("avg"), //
		MEDIAN("med"), //
		MAXIMUM("max"), //
		BATTERY("bat"); // for future use

		private final String mValue;

		private DataType(String value) {
			mValue = value;
		}

		public String getValue() {
			return mValue;
		}

		public static DataType fromValue(String value) {
			for (DataType item : values()) {
				if (value.equalsIgnoreCase(item.getValue())) {
					return item;
				}
			}
			throw new IllegalArgumentException("Invalid DataType value");
		}
	}

	public enum DataInterval {
		RAW(0), //
		MINUTE(60), //
		HOUR(60 * 60), //
		DAY(60 * 60 * 24), //
		WEEK(60 * 60 * 24 * 7), //
		MONTH(60 * 60 * 24 * 7 * 4); // for server this is anything bigger than value of week

		private final int mValue;

		private DataInterval(int value) {
			mValue = value;
		}

		public int getValue() {
			return mValue;
		}

		public static DataInterval fromValue(int value) {
			for (DataInterval item : values()) {
				if (value <= item.getValue()) {
					return item;
				}
			}
			throw new IllegalArgumentException("Invalid DataInterval value");
		}
	}

	/**
	 * Constructor
	 */
	public DeviceLog() {
		clearValues();
	}

	/**
	 * Constructor.
	 * 
	 * @param type
	 * @param interval
	 */
	public DeviceLog(DataType type, DataInterval interval) {
		mType = type;
		mInterval = interval;
		clearValues(); // to reset min/max values
	}

	/**
	 * Return type of values in this log.
	 * 
	 * @return
	 */
	public DataType getType() {
		return mType;
	}

	/**
	 * Return interval of values in this log.
	 * 
	 * @return
	 */
	public DataInterval getInterval() {
		return mInterval;
	}

	/**
	 * Return minimum value in this log
	 * 
	 * @return
	 */
	public float getMinimum() {
		return mMinValue;
	}

	/**
	 * Return maximum value in this log
	 * 
	 * @return
	 */
	public float getMaximum() {
		return mMaxValue;
	}

	/**
	 * Return deviation between maximum and minimum value in this log
	 * 
	 * @return
	 */
	public float getDeviation() {
		return mMaxValue - mMinValue;
	}

	/**
	 * Return all values from log
	 * 
	 * @return sorted map of rows
	 */
	public SortedMap<Long, Float> getValues() {
		return mValues;
	}
	
	/**
	 * Return values between <start, end) date from log
	 *
	 * @param interval
	 * @return sorted map of rows (or empty map)
	 */

	public SortedMap<Long, Float> getValues(Interval interval) {
		return mValues.subMap(interval.getStartMillis(), interval.getEndMillis());
	}

	/**
	 * Add single value.
	 * 
	 * @param row
	 */
	public void addValue(Long dateMillis, Float value) {
		mValues.put(dateMillis, value);

		// Remember min/max values
		if (!Float.isNaN(value)) {
			mMinValue = Math.min(mMinValue, value);
			mMaxValue = Math.max(mMaxValue, value);
		}
	}

	/**
	 * Add interval of same values.
	 * 
	 * @param dateMillis
	 * @param value
	 * @param repeat
	 *            number of rows
	 * @param gap
	 *            gap in seconds
	 */
	public void addValueInterval(Long dateMillis, Float value, int repeat, int gap) {
		for (int i = 0; i <= repeat; i++) {
			addValue(dateMillis + i * (gap * 1000), value);
		}
	}

	/**
	 * Clear all values and add all rows.
	 * 
	 * @param rows
	 */
	public void setValues(SortedMap<Long, Float> rows) {
		clearValues();
		
		for (Entry<Long, Float> entry : rows.entrySet()) {
			addValue(entry.getKey(), entry.getValue());
		}
	}

	public void setDataType(DataType type) {
		mType = type;
	}

	public void setDataInterval(DataInterval interval) {
		mInterval = interval;
	}

	/**
	 * Clear all values.
	 */
	public void clearValues() {
		mValues.clear();
		mMinValue = Float.POSITIVE_INFINITY;
		mMaxValue = Float.NEGATIVE_INFINITY;
	}

}