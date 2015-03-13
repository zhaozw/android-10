package com.rehivetech.beeeon.adapter.device.values;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.adapter.device.units.TemperatureUnit;

public final class TemperatureValue extends BaseValue {

	private double mValue = Double.NaN;

	private static TemperatureUnit mUnit = new TemperatureUnit();

	@Override
	public void setValue(String value) {
		super.setValue(value);
		mValue = Double.parseDouble(value);
	}

	@Override
	public int getIconResource() {
		return R.drawable.dev_temperature;
	}

	public double getValue() {
		return mValue;
	}

	@Override
	public double getDoubleValue() {
		return mValue;
	}

	@Override
	public TemperatureUnit getUnit() {
		return mUnit;
	}

}