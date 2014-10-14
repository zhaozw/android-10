package cz.vutbr.fit.iha.adapter.device;

import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.R;

/**
 * Class that extends BaseDevice for all unknown device type
 * 
 * @author ThinkDeep
 * 
 */
public class UnknownDevice extends BaseDevice {

	private String mValue;

	@Override
	public void setValue(String value) {
		mValue = value;
	}

	@Override
	public int getType() {
		return Constants.TYPE_UNKNOWN;
	}

	@Override
	public int getTypeStringResource() {
		return R.string.dev_unknown_type;
	}

	@Override
	public int getTypeIconResource() {
		return R.drawable.dev_unknown;
	}

	@Override
	public int getUnitStringResource() {
		return R.string.dev_unknown_unit;
	}

	@Override
	public int getRawIntValue() {
		return Integer.MAX_VALUE;
	}

	@Override
	public float getRawFloatValue() {
		return Float.NaN;
	}

	@Override
	public void setValue(int value) {
		mValue = Integer.toString(value);
	}

	@Override
	public String getStringValue() {
		return mValue;
	}

}
