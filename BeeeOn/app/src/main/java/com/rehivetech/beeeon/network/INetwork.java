package com.rehivetech.beeeon.network;

import com.rehivetech.beeeon.gamification.AchievementListItem;
import com.rehivetech.beeeon.IIdentifier;
import com.rehivetech.beeeon.gcm.notification.VisibleNotification;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.Module.SaveDevice;
import com.rehivetech.beeeon.household.device.DeviceLog;
import com.rehivetech.beeeon.household.device.Facility;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.household.watchdog.WatchDog;
import com.rehivetech.beeeon.network.authentication.IAuthProvider;
import com.rehivetech.beeeon.pair.LogDataPair;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public interface INetwork {

	/**
	 * Action of View messages
	 *
	 * @author ThinkDeep
	 */
	enum NetworkAction implements IIdentifier {
		REMOVE("0"), //
		ADD("1");

		private final String mAction;

		NetworkAction(String action) {
			mAction = action;
		}

		public String getId() {
			return mAction;
		}
	}

	/**
	 * Checks if Internet connection is available.
	 *
	 * @return true if available, false otherwise
	 */
	boolean isAvailable();

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////SIGNIN,SIGNUP,ADAPTERS//////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	/**
	 * Return beeeon-token used for communication
	 *
	 * @return BT of actual user
	 */
	String getBT();

	/**
	 * Set beeeon-token for communication
	 *
	 * @return
	 */
	void setBT(String token);

	/**
	 * Check if beeeon-token is present (but does NOT check if it is still valid on server)
	 *
	 * @return
	 */
	boolean hasBT();

	/**
	 * Download information about actual user from server
	 *
	 * @return User object with data from server
	 */
	User loadUserInfo();

	/**
	 * Method log in user by specified provider
	 *
	 * @param authProvider provider object with data for authentication
	 * @return true if user has been logged in with this provider, false otherwise
	 */
	boolean loginMe(IAuthProvider authProvider);

	/**
	 * Method register user to server by specified provider
	 *
	 * @param authProvider provider object with data for authentication
	 * @return true if user has beed added to database with this provider, false otherwise
	 */
	boolean registerMe(IAuthProvider authProvider);

	/**
	 * Method add new provider information (join your accounts) to your account
	 *
	 * @param authProvider
	 * @return true if everything is ok, false otherwise
	 */
	boolean addProvider(IAuthProvider authProvider);

	/**
	 * Method remove one of your provider from your account
	 *
	 * @param providerName
	 * @return
	 */
	boolean removeProvider(String providerName);

	/**
	 * Method remove all providers, so remove whole account from system
	 *
	 * @return
	 */
	boolean deleteMyAccount();

	/**
	 * Method register adapter to server
	 *
	 * @param adapterID   adapter id
	 * @param adapterName adapter name
	 * @return true if adapter has been registered, false otherwise
	 */
	boolean addAdapter(String adapterID, String adapterName);

	/**
	 * Method ask for list of adapters. User has to be sign in before
	 *
	 * @return list of adapters or empty list
	 */
	List<Adapter> getAdapters();

	/**
	 * Method ask for whole adapter data
	 *
	 * @param adapterID of wanted adapter
	 * @return Adapter
	 */
	List<Facility> initAdapter(String adapterID);

	/**
	 * Method change adapter id
	 *
	 * @param oldId id to be changed
	 * @param newId new id
	 * @return true if change has been successfully
	 */
	boolean reInitAdapter(String oldId, String newId);

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////DEVICES,LOGS////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method send updated fields of devices
	 *
	 * @return true if everything goes well, false otherwise
	 */
	boolean updateFacilities(String adapterID, List<Facility> facilities, EnumSet<SaveDevice> toSave);

	/**
	 * Method send wanted fields of module to server
	 *
	 * @param adapterID id of adapter
	 * @param module    to save
	 * @param toSave    ENUMSET specified fields to save
	 * @return true if fields has been updated, false otherwise
	 */
	boolean updateDevice(String adapterID, Module module, EnumSet<SaveDevice> toSave);

	/**
	 * Method toggle or set actor to new value
	 *
	 * @param adapterID
	 * @param module
	 * @return
	 */
	boolean switchState(String adapterID, Module module);

	/**
	 * Method make adapter to special state, when listen for new sensors (e.g. 15s) and wait if some sensors has been
	 * shaken to connect
	 *
	 * @param adapterID
	 * @return
	 */
	boolean prepareAdapterToListenNewSensors(String adapterID);

	/**
	 * Method delete facility from server
	 *
	 * @param facility to be deleted
	 * @return true if is deleted, false otherwise
	 */
	boolean deleteFacility(Facility facility);

	/**
	 * Method ask for actual data of facilities
	 *
	 * @param facilities list of facilities to which needed actual data
	 * @return list of updated facilities fields
	 */
	List<Facility> getFacilities(List<Facility> facilities);

	/**
	 * Method ask server for actual data of one facility
	 *
	 * @param facility
	 * @return
	 */
	Facility getFacility(Facility facility);

	boolean updateFacility(String adapterID, Facility facility, EnumSet<SaveDevice> toSave);

	/**
	 * TODO: need to test
	 *
	 * @param adapterID
	 * @return
	 */
	List<Facility> getNewFacilities(String adapterID);

	/**
	 * Method ask for data of logs
	 *
	 * @param pair data of log (from, to, type, interval)
	 * @return list of rows with logged data
	 */
	DeviceLog getLog(String adapterID, Module module, LogDataPair pair);

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////ROOMS///////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method call to server for actual list of locations
	 *
	 * @return List with locations
	 */
	List<Location> getLocations(String adapterID);

	/**
	 * Method call to server to update location
	 *
	 * @param locations to update
	 * @return true if everything is OK, false otherwise
	 */
	boolean updateLocations(String adapterID, List<Location> locations);

	/**
	 * Method call to server to update location
	 *
	 * @param location
	 * @return
	 */
	boolean updateLocation(Location location);

	/**
	 * Method call to server and delete location
	 *
	 * @param location
	 * @return true room is deleted, false otherwise
	 */
	boolean deleteLocation(Location location);

	Location createLocation(Location location);

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////ACCOUNTS////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	boolean addAccounts(String adapterID, ArrayList<User> users);

	/**
	 * Method add new user to adapter
	 *
	 * @param adapterID
	 * @return
	 */
	boolean addAccount(String adapterID, User user);

	/**
	 * Method delete users from actual adapter
	 *
	 * @param users email of user
	 * @return true if all users has been deleted, false otherwise
	 */
	boolean deleteAccounts(String adapterID, List<User> users);

	/**
	 * Method delete on user from adapter
	 *
	 * @param adapterID
	 * @param user
	 * @return
	 */
	boolean deleteAccount(String adapterID, User user);

	/**
	 * Method ask for list of users of current adapter
	 *
	 * @return Map of users where key is email and value is User object
	 */
	List<User> getAccounts(String adapterID);

	/**
	 * Method update users roles on server on current adapter
	 * <p/>
	 * map with email as key and role as value
	 *
	 * @return true if all accounts has been changed false otherwise
	 */
	boolean updateAccounts(String adapterID, ArrayList<User> users);

	/**
	 * Method update users role on adapter
	 *
	 * @param adapterID
	 * @param user
	 * @return
	 */
	boolean updateAccount(String adapterID, User user);

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////TIME////////////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method set wanted time zone to server
	 *
	 * @param offsetInMinutes - difference from GMT (UTC+0)
	 * @return
	 */
	boolean setTimeZone(String adapterID, int offsetInMinutes);

	/**
	 * Method call to server to get actual time zone
	 *
	 * @return integer in range <-12,12>
	 */
	int getTimeZone(String adapterID);

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////NOTIFICATIONS///////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	/**
	 * Method set read flag to notification on server
	 *
	 * @param msgID id of notification
	 * @return true if server took flag, false otherwise
	 */
	boolean NotificationsRead(ArrayList<String> msgID);

	List<VisibleNotification> getNotifications();

	// /////////////////////////////////////////////////////////////////////////////////
	// /////////////////////////////////////ALGORITHMS//////////////////////////////////
	// /////////////////////////////////////////////////////////////////////////////////

	boolean addWatchDog(WatchDog watchDog, String AdapterID);

	List<WatchDog> getWatchDogs(ArrayList<String> watchDogIds, String adapterID);

	List<WatchDog> getAllWatchDogs(String adapterID);

	boolean updateWatchDog(WatchDog watchDog, String AdapterId);

	boolean deleteWatchDog(WatchDog watchDog);

	boolean passBorder(String regionId, String type);

	List<AchievementListItem> getAllAchievements(String adapterID);

	List<String> setProgressLvl(String adapterId, String achievementId);
}
