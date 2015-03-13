package com.rehivetech.beeeon.persistence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import com.rehivetech.beeeon.IdentifierComparator;
import com.rehivetech.beeeon.adapter.Adapter;
import com.rehivetech.beeeon.exception.AppException;
import com.rehivetech.beeeon.network.INetwork;

public class AdaptersModel {

	private final INetwork mNetwork;

	private final Map<String, Adapter> mAdapters = new HashMap<String, Adapter>();

	private DateTime mLastUpdate;

	private static final int RELOAD_EVERY_SECONDS = 10 * 60;

	public AdaptersModel(INetwork network) {
		mNetwork = network;
	}

	public Map<String, Adapter> getAdaptersMap() {
		return mAdapters;
	}

	public List<Adapter> getAdapters() {
		List<Adapter> adapters = new ArrayList<Adapter>();

		for (Adapter adapter : mAdapters.values()) {
			adapters.add(adapter);
		}

		// Sort result adapters by id
		Collections.sort(adapters, new IdentifierComparator());

		return adapters;
	}

	private void setAdapters(List<Adapter> adapters) {
		mAdapters.clear();

		for (Adapter adapter : adapters) {
			mAdapters.put(adapter.getId(), adapter);
		}
	}

	public Adapter getAdapter(String id) {
		return mAdapters.get(id);
	}

	private void setLastUpdate(DateTime lastUpdate) {
		mLastUpdate = lastUpdate;
	}

	private boolean isExpired() {
		return mLastUpdate == null || mLastUpdate.plusSeconds(RELOAD_EVERY_SECONDS).isBeforeNow();
	}

	public boolean reloadAdapters(boolean forceReload) {
		if (!forceReload && !isExpired()) {
			return false;
		}

		// TODO: check if user is logged in
		if (mNetwork.isAvailable()) {
			return loadFromServer();
		} else if (forceReload) {
			return loadFromCache();
		}

		return false;
	}

	private boolean loadFromServer() {
		try {
			setAdapters(mNetwork.getAdapters());
			setLastUpdate(DateTime.now());
			saveToCache();
		} catch (AppException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	private boolean loadFromCache() {
		// TODO: implement this
		return false;

		// setAdapters(adaptersFromCache);
		// setLastUpdate(lastUpdateFromCache);
	}

	private void saveToCache() {
		// TODO: implement this
	}

}