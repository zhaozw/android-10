package com.rehivetech.beeeon.household.device;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.device.values.BaseValue;
import com.rehivetech.beeeon.household.device.values.BoilerOperationModeValue;
import com.rehivetech.beeeon.household.device.values.BoilerOperationTypeValue;
import com.rehivetech.beeeon.household.device.values.BoilerStatusValue;
import com.rehivetech.beeeon.household.device.values.EmissionValue;
import com.rehivetech.beeeon.household.device.values.HumidityValue;
import com.rehivetech.beeeon.household.device.values.IlluminationValue;
import com.rehivetech.beeeon.household.device.values.NoiseValue;
import com.rehivetech.beeeon.household.device.values.OnOffValue;
import com.rehivetech.beeeon.household.device.values.OpenClosedValue;
import com.rehivetech.beeeon.household.device.values.PressureValue;
import com.rehivetech.beeeon.household.device.values.TemperatureValue;
import com.rehivetech.beeeon.household.device.values.UnknownValue;

/**
 * Module's types
 */
public enum ModuleType {

	TYPE_UNKNOWN(-1, R.string.dev_unknown_type, false, UnknownValue.class), // unknown module

	// =============== Sensors ===============
	TYPE_HUMIDITY(0x01, R.string.dev_humidity_type, false, HumidityValue.class), // humidity meter
	TYPE_PRESSURE(0x02, R.string.dev_pressure_type, false, PressureValue.class), // pressure meter
	TYPE_OPEN_CLOSED(0x03, R.string.dev_open_closed_type, false, OpenClosedValue.class), // open/closed sensor
	TYPE_ON_OFF(0x04, R.string.dev_on_off_type, false, OnOffValue.class), // on/off sensor
	TYPE_ILLUMINATION(0x05, R.string.dev_illumination_type, false, IlluminationValue.class), // illumination meter
	TYPE_NOISE(0x06, R.string.dev_noise_type, false, NoiseValue.class), // noise meter
	TYPE_EMISSION(0x07, R.string.dev_emission_type, false, EmissionValue.class), // emission meter
	// TYPE_POSITION(0x08, R.string.dev_position_type, false, BitArrayValue.class), // position meter
	// 9 is missing, it's racism! :(
	TYPE_TEMPERATURE(0x0A, R.string.dev_temperature_type, false, TemperatureValue.class), // temperature meter
	TYPE_BOILER_STATUS(0x0B, R.string.dev_boiler_status_type, false, BoilerStatusValue.class), // boiler status

	// =============== Actors ================
	TYPE_ACTOR_ON_OFF(0xA0, R.string.dev_actor_on_off_type, true, OnOffValue.class), // on/off actor
	TYPE_ACTOR_PUSH(0xA1, R.string.dev_actor_push_type, true, UnknownValue.class /*PushValue.class*/), // push (on-only) actor
	TYPE_ACTOR_TOGGLE(0xA2, R.string.dev_actor_toggle_type, true, UnknownValue.class /*ToggleValue.class*/), // toggle actor
	TYPE_ACTOR_RANGE(0xA3, R.string.dev_actor_range_type, true, UnknownValue.class /*RangeValue.class*/), // range actor
	TYPE_ACTOR_RGB(0xA4, R.string.dev_actor_rgb_type, true, UnknownValue.class /*RGBValue.class*/), // RGB actor
	TYPE_ACTOR_TEMPERATURE(0xA5, R.string.dev_actor_temperature, true, TemperatureValue.class), // temperature actor
	TYPE_ACTOR_BOILER_OPERATION_TYPE(0xA6, R.string.dev_actor_boiler_operation_type, true, BoilerOperationTypeValue.class), // boiler operation type
	TYPE_ACTOR_BOILER_OPERATION_MODE(0xA7, R.string.dev_actor_boiler_operation_mode, true, BoilerOperationModeValue.class); // boiler operation mode

	private final int mTypeId;
	private final int mNameRes;
	private final Class<? extends BaseValue> mValueClass;
	private final boolean mIsActor;

	private ModuleType(int id, int nameRes, boolean isActor, Class<? extends BaseValue> valueClass) {
		mTypeId = id;
		mNameRes = nameRes;
		mIsActor = isActor;
		mValueClass = valueClass;
	}

	public int getTypeId() {
		return mTypeId;
	}

	public int getStringResource() {
		return mNameRes;
	}

	public boolean isActor() {
		return mIsActor;
	}

	public Class<? extends BaseValue> getValueClass() {
		return mValueClass;
	}

	public static ModuleType fromTypeId(int typeId) {
		// Get the ModuleType object based on type number
		for (ModuleType item : values()) {
			if (typeId == item.getTypeId()) {
				return item;
			}
		}

		return TYPE_UNKNOWN;
	}

	public static ModuleType fromValue(String value) {
		if (value.isEmpty())
			return TYPE_UNKNOWN;

		return fromTypeId(Integer.parseInt(value));
	}

}