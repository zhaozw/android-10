package com.rehivetech.beeeon.household.device;

import com.rehivetech.beeeon.IIdentifier;
import com.rehivetech.beeeon.SortIdentifierComparator;
import com.rehivetech.beeeon.household.location.Location;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

public final class Device implements IIdentifier {

	/**
	 * Properties inherited from device's specification table.
	 */
	private final DeviceType mType;
	private final List<Module> mModules;

	/**
	 * Properties belonging to real device.
	 */
	private final String mAddress;
	private final String mGateId;

	private String mLocationId;
	private boolean mInitialized;
	private DateTime mInvolveTime;
	private int mNetworkQuality;
	private DateTime mLastUpdate;

	/**
	 * Private constructor, Device objects are created by static factory method {@link Device#createDeviceByType(int, String)}.
	 *
	 * @param type
	 */
	public Device(DeviceType type, String gateId, String address) {
		mType = type;
		mGateId = gateId;
		mAddress = address;

		// Create modules list
		List<Module> modules = type.createModules(this);
		// And sort them by order and id
		Collections.sort(modules, new SortIdentifierComparator());
		// Them make it immutable
		mModules = Collections.unmodifiableList(modules);
	}

	/**
	 * Factory method for creating new Device objects.
	 *
	 * @param type
	 * @param address
	 * @return
	 */
	public static final Device createDeviceByType(int type, String address) {
		// FIXME: implement this somehow
		/*Device device = new Device();
		device.setAddress(address);
		return device;*/
		throw new IllegalArgumentException(String.format("Unknown device type: %d", type));
	}

	/**
	 * @return time of last update
	 */
	public DateTime getLastUpdate() {
		return mLastUpdate;
	}

	/**
	 * @param lastUpdate time of last update
	 */
	public void setLastUpdate(DateTime lastUpdate) {
		mLastUpdate = lastUpdate;
	}

	/**
	 * Check if actual value of this sensor is expired
	 *
	 * @return true when refresh interval since last update expired
	 */
	public boolean isExpired() {
		DeviceFeatures features = mType.getFeatures();
		if (!features.hasRefresh()) {
			return true;
		}
		return mLastUpdate.plusSeconds(features.getActualRefresh().getInterval()).isBeforeNow();
	}

	public String getId() {
		return mAddress;
	}

	public DeviceType getType() {
		return mType;
	}

	/**
	 * @return location id of device
	 */
	public String getLocationId() {
		return mLocationId;
	}

	/**
	 * Setting location of device
	 *
	 * @param locationId
	 */
	public void setLocationId(String locationId) {
		// From server we've got "", but internally we need to use Location.NO_LOCATION_ID
		if (locationId.isEmpty())
			locationId = Location.NO_LOCATION_ID;

		mLocationId = locationId;
	}

	/**
	 * Get gate id of device
	 *
	 * @return gate id
	 */
	public String getGateId() {
		return mGateId;
	}

	/**
	 * Returning flag if mDevice has been initialized yet
	 *
	 * @return
	 */
	public boolean isInitialized() {
		return mInitialized;
	}

	/**
	 * Setting flag for mDevice initialization state
	 *
	 * @param initialized
	 */
	public void setInitialized(boolean initialized) {
		mInitialized = initialized;
	}

	/**
	 * Get time of setting of device to system
	 *
	 * @return involve time
	 */
	public DateTime getInvolveTime() {
		return mInvolveTime;
	}

	/**
	 * Setting involve time
	 *
	 * @param involved
	 */
	public void setInvolveTime(DateTime involved) {
		mInvolveTime = involved;
	}

	/**
	 * Get MAC address of device
	 *
	 * @return address
	 */
	public String getAddress() {
		return mAddress;
	}

	/**
	 * Get value of signal quality
	 *
	 * @return quality
	 */
	public int getNetworkQuality() {
		return mNetworkQuality;
	}

	/**
	 * Setting quality
	 *
	 * @param networkQuality
	 */
	public void setNetworkQuality(int networkQuality) {
		mNetworkQuality = networkQuality;
	}

	public List<Module> getModules() {
		return mModules;
	}

	public Module getModuleByType(ModuleType type, int offset) {
		for (Module module : getModules()) {
			if (module.getType().equals(type) && module.getOffset() == offset) {
				return module;
			}
		}

		return null;
	}

	public static class DataPair {
		public final Device mDevice;
		public final EnumSet<Module.SaveModule> what;
		public final Location location;

		public DataPair(final Device device, final EnumSet<Module.SaveModule> what) {
			this(device, null, what);
		}

		public DataPair(final Device device, final Location newLoc, final EnumSet<Module.SaveModule> what) {
			this.mDevice = device;
			this.what = what;
			this.location = newLoc;
		}
	}
}
