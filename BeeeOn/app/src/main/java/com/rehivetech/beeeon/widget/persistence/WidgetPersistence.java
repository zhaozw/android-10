package com.rehivetech.beeeon.widget.persistence;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.util.Compatibility;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;
import com.rehivetech.beeeon.widget.WidgetSettings;

import java.util.List;

/**
 * @author mlyko
 */
public abstract class WidgetPersistence {
	private static final String TAG = WidgetPersistence.class.getSimpleName();

	protected static final String PREF_ID = "id";
	protected static final String PREF_NAME = "name";
	protected static final String PREF_ADAPTER_ID = "adapter_id";

	protected String id;
	protected String name;
	protected String adapterId;

	// helpers
	protected UnitsHelper mUnitsHelper;
	protected TimeHelper mTimeHelper;

	// persistence data
	protected int mWidgetId;
	protected final int mOffset;
	protected int mBoundView;
	protected boolean mIsCached = false;

	protected Context mContext;
	protected RemoteViews mParentRemoteViews;
	protected RemoteViews mValueRemoteViews;

	protected WidgetSettings mWidgetSettings;

	public WidgetPersistence(Context context, int widgetId, int offset, int view, UnitsHelper unitsHelper, TimeHelper timeHelper, WidgetSettings settings) {
		mContext = context.getApplicationContext();
		mWidgetId = widgetId;
		mOffset = offset;
		mBoundView = view;
		mUnitsHelper = unitsHelper;
		mTimeHelper = timeHelper;
		mWidgetSettings = settings;
	}

	public void setCached(boolean isCached){
		mIsCached = isCached;
	}

	public int getOffset() {
		return mOffset;
	}

	public int getBoundView() {
		return mBoundView;
	}

	public abstract String getPrefFileName();

	public abstract void load();
	public abstract void configure(Object obj, Object obj2);
	public abstract void save();
	public abstract void change(Object obj, Adapter adapter);

	public void initValueView(RemoteViews parentRV){
		Log.d(TAG, "initValueView()");
		mParentRemoteViews = parentRV;
	}

	/**
	 * Updates value layout when logged in
	 */
	public void updateValueView(boolean isCached){
		updateValueView(isCached, "");
	}

	/**
	 * Updates value layout with gotten data (either cached or from UnitsHelper)
	 * @param cachedFormat	Format for specifying what looks like when cached data -> available only %s
	 */
	public void updateValueView(boolean isCached, String cachedFormat){
		Log.d(TAG, "updateValueView()");
		this.setCached(isCached);
	}

	public RemoteViews getValueViews(){
		return mValueRemoteViews;
	}

	public String getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public String getAdapterId(){
		return adapterId;
	}

	public void delete(){
		getSettings().edit().clear().commit();
	}

	public SharedPreferences getSettings() {
		return mContext.getSharedPreferences(String.format(getPrefFileName(), mWidgetId, mOffset), Context.MODE_PRIVATE);
	}

	public void setTextSize(int view, int sizeInSp){
		Compatibility.setTextViewTextSize(mContext, getValueViews(), view, TypedValue.COMPLEX_UNIT_SP, sizeInSp);
	}

	// ------ METHODS FOR WORKING WITH MORE OBJECTS AT ONCE ------ //
	public static <T extends WidgetPersistence> void loadAll(List<T> widgetPersistences){
		if(widgetPersistences == null) return;
		for(WidgetPersistence per : widgetPersistences){
			per.load();
		}
	}

	public static <T extends WidgetPersistence> void saveAll(List<T> widgetPersistences){
		if(widgetPersistences == null) return;
		for(WidgetPersistence per : widgetPersistences){
			per.save();
		}
	}

	public static <T extends WidgetPersistence> void deleteAll(List<T> widgetPersistences){
		if(widgetPersistences == null) return;
		for(WidgetPersistence per : widgetPersistences){
			per.delete();
		}
	}
}
