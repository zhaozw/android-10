package com.rehivetech.beeeon.household.device.values;

import com.rehivetech.beeeon.IconResourceType;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.device.units.IlluminationUnit;

public final class IlluminationValue extends BaseValue {

	private double mValue = Double.NaN;

	private static IlluminationUnit mUnit = new IlluminationUnit();

	@Override
	public void setValue(String value) {
		super.setValue(value);
		mValue = Double.parseDouble(value);
	}

	@Override
	public int getIconResource(IconResourceType type) {
		return type == IconResourceType.WHITE ? R.drawable.ic_val_lux : R.drawable.ic_val_lux_gray;

	}

	@Override
	public int getActorIconResource(IconResourceType type) {
		// FIXME: Use real resource when we will have real actor icon
		return getIconResource(type);
	}

	@Override
	public IlluminationUnit getUnit() {
		return mUnit;
	}

	public double getValue() {
		return mValue;
	}

	@Override
	public double getDoubleValue() {
		return mValue;
	}

}
