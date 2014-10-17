package cz.vutbr.fit.iha.adapter.device.values;

import cz.vutbr.fit.iha.R;


public final class IlluminationValue extends BaseDeviceValue {

	private float mValue = Float.MAX_VALUE;

	@Override
	public int getUnitStringResource() {
		return R.string.dev_illumination_unit;
	}

	@Override
	public void setValue(int value) {
		mValue = value;
	}

	@Override
	public int getRawIntValue() {
		return (int)mValue;
	}

	@Override
	public float getRawFloatValue() {
		return mValue;
	}

	@Override
	public void setValue(String value) {
		mValue = Float.parseFloat(value);
	}

	@Override
	public String getStringValue() {
		return Float.toString(mValue);
	}

}
