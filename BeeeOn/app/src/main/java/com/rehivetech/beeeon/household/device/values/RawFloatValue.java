package com.rehivetech.beeeon.household.device.values;

import com.rehivetech.beeeon.IconResourceType;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.device.units.BlankUnit;
import com.rehivetech.beeeon.household.device.units.NoiseUnit;

public final class RawFloatValue extends BaseValue {

	private double mValue = Double.NaN;

	private static BlankUnit mUnit = new BlankUnit();

	@Override
	public void setValue(String value) {
		super.setValue(value);
		mValue = Double.parseDouble(value);
	}

	@Override
	public int getIconResource(IconResourceType type) {
		return type == IconResourceType.WHITE ? R.drawable.ic_val_unknown : R.drawable.ic_val_unknown_gray; // FIXME: Use correct icon
	}

	@Override
	public int getActorIconResource(IconResourceType type) {
		// FIXME: Use real resource when we will have real actor icon
		return getIconResource(type);
	}

	@Override
	public BlankUnit getUnit() {
		return mUnit;
	}

	@Override
	public double getDoubleValue() {
		return mValue;
	}

}