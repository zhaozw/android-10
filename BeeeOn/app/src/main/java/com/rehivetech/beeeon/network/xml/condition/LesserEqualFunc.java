package com.rehivetech.beeeon.network.xml.condition;

import com.rehivetech.beeeon.household.device.Device;

//new drop
public class LesserEqualFunc extends ConditionFunction {
	private String mValue;
	private Device mDevice;

	public LesserEqualFunc(Device device, String value) {
		mDevice = device;
		mValue = value;
	}

	public Device getDevice() {
		return mDevice;
	}

	public String getValue() {
		return mValue;
	}
}