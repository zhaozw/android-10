package com.rehivetech.beeeon.household.device.units;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;

public class HumidityUnit extends BaseUnit {

	public static final int DEFAULT = 0;

	public HumidityUnit() {
		super();

		mItems.add(this.new Item(DEFAULT, R.string.unit_humidity, R.string.unit_humidity));
	}

	@Override
	public int getDefaultId() {
		return DEFAULT;
	}

	@Override
	public String getPersistenceKey() {
		return Constants.PERSISTENCE_PREF_HUMIDITY;
	}

	@Override
	public double convertValue(Item to, double value) {
		return value;
	}

}
