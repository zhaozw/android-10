package com.rehivetech.beeeon.household.device.units;

import com.rehivetech.beeeon.R;

public class BlankUnit extends BaseUnit {

	public static final int DEFAULT = 0;

	public BlankUnit() {
		super();

		mItems.add(this.new Item(DEFAULT, R.string.empty, R.string.empty));
	}

	@Override
	public int getDefaultId() {
		return DEFAULT;
	}

	@Override
	public String getPersistenceKey() {
		return "";
	}

	@Override
	public double convertValue(Item to, double value) {
		return value;
	}

}
