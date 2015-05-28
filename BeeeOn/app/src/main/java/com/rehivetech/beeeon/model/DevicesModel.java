package com.rehivetech.beeeon.model;

import com.rehivetech.beeeon.IdentifierComparator;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.Module.SaveModule;
import com.rehivetech.beeeon.household.device.ModuleType;
import com.rehivetech.beeeon.network.INetwork;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.MultipleDataHolder;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;


public class DevicesModel extends BaseModel {

	private static final String TAG = DevicesModel.class.getSimpleName();

	private static final int RELOAD_EVERY_SECONDS = 10 * 60;

	private final MultipleDataHolder<Device> mDevices = new MultipleDataHolder<>(); // adapterId => mDevice dataHolder

	public DevicesModel(INetwork network) {
		super(network);
	}

	/**
	 * Return mDevice by ID.
	 *
	 * @param id
	 * @return mDevice or null if no mDevice is found
	 */
	public Device getFacility(String adapterId, String id) {
		return mDevices.getObject(adapterId, id);
	}

	/**
	 * Return module by ID
	 *
	 * @param adapterId
	 * @param id
	 * @return
	 */
	public Module getModule(String adapterId, String id) {
		String[] ids = id.split(Module.ID_SEPARATOR, 2);

		Device device = getFacility(adapterId, ids[0]);
		if (device == null)
			return null;

		// FIXME: cleanup this after demo

		int iType = -1; // unknown type
		int offset = 0; // default offset

		if (!ids[1].isEmpty()) {
			// Get integer representation of the given string value
			int value = Integer.parseInt(ids[1]);

			// Separate combined value to type and offset
			iType = value % 256;
			offset = value / 256;
		}

		ModuleType type = ModuleType.fromTypeId(iType);

		return device.getModuleByType(type, offset);
	}

	/**
	 * Return list of all devices from adapter
	 *
	 * @param adapterId
	 * @return List of devices (or empty list)
	 */
	public List<Device> getDevicesByAdapter(String adapterId) {
		List<Device> devices = mDevices.getObjects(adapterId);

		// Sort result devices by id
		Collections.sort(devices, new IdentifierComparator());

		return devices;
	}

	/**
	 * Return list of all devices by location from adapter
	 *
	 * @param locationId
	 * @return List of devices (or empty list)
	 */
	public List<Device> getDevicesByLocation(String adapterId, String locationId) {
		List<Device> devices = new ArrayList<>();

		for (Device device : getDevicesByAdapter(adapterId)) {
			if (device.getLocationId().equals(locationId)) {
				devices.add(device);
			}
		}

		return devices;
	}

	/**
	 * This CAN'T be called on UI thread!
	 *
	 * @param adapterId
	 * @param forceReload
	 * @return
	 */
	public synchronized boolean reloadDevicesByAdapter(String adapterId, boolean forceReload) throws AppException {
		if (!forceReload && !mDevices.isExpired(adapterId, RELOAD_EVERY_SECONDS)) {
			return false;
		}

		mDevices.setObjects(adapterId, mNetwork.initAdapter(adapterId));
		mDevices.setLastUpdate(adapterId, DateTime.now());

		return true;
	}

	/**
	 * This CAN'T be called on UI thread!
	 *
	 * @param devices
	 * @return
	 */
	public boolean refreshDevices(List<Device> devices, boolean forceReload) throws AppException {
		// Remove not expired devices

		// NOTE: changed from foreach version to this -> http://stackoverflow.com/questions/1196586/calling-remove-in-foreach-loop-in-java
		Iterator<Device> facIterator = devices.iterator();
		while (facIterator.hasNext()) {
			Device device = facIterator.next();
			if (!forceReload && !device.isExpired()) {
				facIterator.remove();
			}
		}

		List<Device> newDevices = mNetwork.getDevices(devices);
		if (newDevices == null)
			return false;

		for (Device newDevice : newDevices) {
			mDevices.addObject(newDevice.getAdapterId(), newDevice);
		}

		return true;
	}

	/**
	 * This reloads data of device from server...
	 * This CAN'T be called on UI thread!
	 *
	 * @param device
	 * @return
	 */
	public boolean refreshFacility(Device device, boolean forceReload) throws AppException {
		if (!forceReload && !device.isExpired()) {
			return false;
		}
		
		Device newDevice = mNetwork.getFacility(device);
		if (newDevice == null)
			return false;

		mDevices.addObject(device.getAdapterId(), device);

		return true;
	}

	/**
	 * Save specified settings of device to server.
	 *
	 * This CAN'T be called on UI thread!
	 *
	 * @param device
	 * @param what
	 *            type of settings to save
	 * @return true on success, false otherwise
	 */
	public boolean saveFacility(Device device, EnumSet<SaveModule> what) throws AppException {
		mNetwork.updateFacility(device.getAdapterId(), device, what);
		refreshFacility(device, true);

		return true;
	}

	/**
	 * Delete device from server.
	 *
	 * This CAN'T be called on UI thread!
	 */
	public boolean deleteFacility(Device device) throws AppException {
		if (mNetwork.deleteFacility(device)) {
			// Device was deleted on server, remove it from map too
			mDevices.removeObject(device.getAdapterId(), device.getId());
			return true;
		}

		return false;
    }

	/**
	 * Save specified settings of module to server.
	 *
	 * This CAN'T be called on UI thread!
	 *
	 * @param module
	 * @param what
	 *            type of settings to save
	 * @return true on success, false otherwise
	 */
	public boolean saveModule(Module module, EnumSet<SaveModule> what) throws AppException {
		Device device = module.getDevice();

		mNetwork.updateModule(device.getAdapterId(), module, what);
		refreshFacility(device, true);

		return true;
	}

	/**
	 * Send request to server to switch Actor value.
	 *
	 * This CAN'T be called on UI thread!
	 *
	 * @param module
	 *            ModuleType of this module must be actor, i.e., module.getType().isActor() must return true.
	 * @return true on success, false otherwise
	 */
	public boolean switchActor(Module module) throws AppException {
		if (!module.getType().isActor()) {
			Log.e(TAG, String.format("Tried to switch NOT-actor module '%s'", module.getName()));
			return false;
		}
		
		Device device = module.getDevice();

		mNetwork.switchState(module.getDevice().getAdapterId(), module);
		refreshFacility(device, true);

		return true;
	}

}
