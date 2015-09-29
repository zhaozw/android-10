package com.rehivetech.beeeon.household.device;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.rehivetech.beeeon.IOrderIdentifier;
import com.rehivetech.beeeon.IconResourceType;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.device.values.BaseValue;
import com.rehivetech.beeeon.household.device.values.EnumValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Module implements IOrderIdentifier {
	/**
	 * Properties inherited from device's specification table.
	 */
	private final String mId;
	private final ModuleType mType; // type defines what BaseValue should be created

	private final Integer mSort;
	private final int mGroupRes;
	private final int mNameRes;
	private final boolean mIsActuator;
	private final List<Rule> mRules;

	private final Device mDevice; // parent device
	private final BaseValue mValue;
	private final ModuleId mModuleId;

	public static Module createUnknownModule(@NonNull Device device, @NonNull String id) {
		return new Module(device, id, ModuleType.TYPE_UNKNOWN.getTypeId(), null, null, null, false, null, null);
	}

	public Module(@NonNull Device device, @NonNull String id, int typeId, @Nullable Integer sort, @Nullable Integer groupRes, @Nullable Integer nameRes, boolean isActuator, @Nullable List<Rule> rules, @Nullable String defaultValue) {
		this(device, id, typeId, sort, groupRes, nameRes, isActuator, rules, (BaseValue.Constraints) null, defaultValue);
	}

	public Module(@NonNull Device device, @NonNull String id, int typeId, @Nullable Integer sort, @Nullable Integer groupRes, @Nullable Integer nameRes, boolean isActuator, @Nullable List<Rule> rules,
				  @Nullable BaseValue.Constraints constraints, @Nullable String defaultValue) throws IllegalArgumentException {
		mDevice = device;
		mId = id;
		mSort = sort;
		mGroupRes = groupRes != null ? groupRes : 0;
		mNameRes = nameRes != null ? nameRes : 0;
		mIsActuator = isActuator;
		mRules = rules != null ? Collections.unmodifiableList(rules) : null;

		if (isActuator && constraints == null) {
			throw new IllegalArgumentException("Module is actuator, but constructor was called without constraints nor enumValues.");
		}

		mType = ModuleType.fromTypeId(typeId);
		if (mType.getValueClass() == EnumValue.class) {
			throw new IllegalArgumentException("ValueClass received from ModuleType is EnumValue, but constructor was called without enumValues.");
		}
		mValue = BaseValue.createFromModuleType(mType, constraints, defaultValue);

		mModuleId = new ModuleId(device.getAddress(), id);
	}

	public Module(@NonNull  Device device, @NonNull String id, int typeId, @Nullable Integer sort, @Nullable Integer groupRes, @Nullable Integer nameRes, boolean isActuator, @Nullable List<Rule> rules,
				  @Nullable List<EnumValue.Item> enumValues, @Nullable String defaultValue) throws IllegalArgumentException {
		mDevice = device;
		mId = id;
		mSort = sort;
		mGroupRes = groupRes != null ? groupRes : R.string.device_detail_default_group;
		mNameRes = nameRes != null ? nameRes : 0;
		mIsActuator = isActuator;
		mRules = rules != null ? Collections.unmodifiableList(rules) : null;

		mType = ModuleType.fromTypeId(typeId);
		if (mType.getValueClass() != EnumValue.class) {
			throw new IllegalArgumentException("ValueClass received from ModuleType is not EnumValue, but constructor was called with enumValues.");
		}
		mValue = new EnumValue(enumValues);
		mValue.setDefaultValue(defaultValue);

		mModuleId = new ModuleId(device.getAddress(), id);
	}

	public ModuleType getType() {
		return mType;
	}

	public BaseValue getValue() {
		return mValue;
	}

	public void setValue(String value) {
		mValue.setValue(value);
	}

	/**
	 * Get resource for human readable string representing type of this module
	 *
	 * @return
	 */
	public int getTypeStringResource() {
		return mType.getStringResource();
	}

	public int getIconResource(){
		return getIconResource(IconResourceType.DARK);
	}

	public int getIconResource(IconResourceType type){
		return mIsActuator ? mValue.getActorIconResource(type) : mValue.getIconResource(type);
	}

	public Device getDevice() {
		return mDevice;
	}

	/**
	 * @return id of this module inside the parent device (regarding specification).
	 */
	@Override
	public String getId() {
		return mId;
	}

	public ModuleId getModuleId() {
		return mModuleId;
	}

	/**
	 * @param context
	 * @return name of group
	 */
	public String getGroupName(Context context) {
		return mGroupRes > 0 ? context.getString(mGroupRes) : "";
	}

	/**
	 * @param context
	 * @param withGroup
	 * @return name of module, optionally prefixed with name of group
	 */
	public String getName(Context context, boolean withGroup) {
		String group = mGroupRes > 0 ? context.getString(mGroupRes) : "";
		String name = mNameRes > 0 ? context.getString(mNameRes) : "";

		return withGroup ? String.format("%s %s", group, name).trim() : name;
	}

	/**
	 * @param context
	 * @return name of module
	 */
	public String getName(Context context) {
		return getName(context, false);
	}

	/**
	 * @return true if module should be visible to the user at this moment
	 */
	public List<String> getHideModuleIdsFromRules() {
		List<String> list = new ArrayList<>();

		// Without rules is everyone visible
		if (mRules == null)
			return list;

		// Get actual value
		int value = (mValue instanceof EnumValue) ? ((EnumValue) mValue).getActive().getId() : (int) mValue.getDoubleValue();

		// Find rule with this value
		for (Rule rule : mRules) {
			if (rule.value == value) {
				// Process rule for actual value
				for (int id : rule.hideModulesIds) {
					list.add(String.valueOf(id));
				}
			}
		}

		return list;
	}

	public boolean isActuator() {
		return mIsActuator;
	}

	@Nullable
	@Override
	public Integer getSort() {
		return mSort;
	}

	public static class Rule {
		public final int value;
		public final int[] hideModulesIds;

		public Rule(int value, int[] hideModulesIds) {
			this.value = value;
			this.hideModulesIds = hideModulesIds;
		}
	}

	public static class ModuleId {
		private static final String ID_SEPARATOR = "---";

		/** Unique identifier (address) of device that holds this module. */
		public final String deviceId;

		/** Identifier of this module inside the parent device (regarding specification). */
		public final String moduleId;

		/** Unique identifier of module (device address + module id). */
		public final String absoluteId;

		public ModuleId(String deviceId, String moduleId) {
			this.deviceId = deviceId;
			this.moduleId = moduleId;
			this.absoluteId = deviceId + ID_SEPARATOR + moduleId;
		}

		public ModuleId(String absoluteId) {
			String[] ids = absoluteId.split(ID_SEPARATOR, 2);

			if (ids.length != 2) {
				throw new IllegalArgumentException(String.format("Id of module must have 2 parts, given: '%s'", absoluteId));
			}

			this.deviceId = ids[0];
			this.moduleId = ids[1];
			this.absoluteId = deviceId + ID_SEPARATOR + moduleId;
		}
	}

}
