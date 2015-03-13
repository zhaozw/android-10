package com.rehivetech.beeeon.activity.fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.SensorDetailActivity;
import com.rehivetech.beeeon.adapter.Adapter;
import com.rehivetech.beeeon.adapter.device.Device;
import com.rehivetech.beeeon.adapter.device.Device.SaveDevice;
import com.rehivetech.beeeon.adapter.device.DeviceLog;
import com.rehivetech.beeeon.adapter.device.DeviceLog.DataInterval;
import com.rehivetech.beeeon.adapter.device.DeviceLog.DataType;
import com.rehivetech.beeeon.adapter.device.Facility;
import com.rehivetech.beeeon.adapter.device.RefreshInterval;
import com.rehivetech.beeeon.adapter.device.values.BaseEnumValue;
import com.rehivetech.beeeon.adapter.device.values.BaseValue;
import com.rehivetech.beeeon.adapter.device.values.OnOffValue;
import com.rehivetech.beeeon.adapter.device.values.OpenClosedValue;
import com.rehivetech.beeeon.adapter.location.Location;
import com.rehivetech.beeeon.arrayadapter.LocationArrayAdapter;
import com.rehivetech.beeeon.asynctask.ActorActionTask;
import com.rehivetech.beeeon.asynctask.CallbackTask.CallbackTaskListener;
import com.rehivetech.beeeon.asynctask.SaveDeviceTask;
import com.rehivetech.beeeon.asynctask.SaveFacilityTask;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.pair.LogDataPair;
import com.rehivetech.beeeon.pair.SaveDevicePair;
import com.rehivetech.beeeon.pair.SaveFacilityPair;
import com.rehivetech.beeeon.thread.ToastMessageThread;
import com.rehivetech.beeeon.util.GraphViewHelper;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;

//import android.widget.LinearLayout;

public class SensorDetailFragment extends Fragment {

	private Controller mController;
	private static final String TAG = SensorDetailFragment.class.getSimpleName();
	private static final int EDIT_NONE = 0;
	private static final int EDIT_NAME = 1;
	private static final int EDIT_LOC = 2;
	private static final int EDIT_REFRESH_T = 3;

	// GUI elements
	private TextView mName;
	private EditText mNameEdit;
	private TextView mLocation;
	private TextView mValue;
	private Button mValueSwitch;
	private TextView mTime;
	private ImageView mIcon;
	private TextView mRefreshTimeText;
	private SeekBar mRefreshTimeValue;
	private TextView mGraphLabel;
	private LinearLayout mGraphLayout;
	private ScrollView mLayoutScroll;
	private RelativeLayout mLayoutRelative;
	private RelativeLayout mRectangleName;
	private RelativeLayout mRectangleLoc;
	private Spinner mSpinnerLoc;
	private GraphView mGraphView;
	private TextView mGraphInfo;

	private SensorDetailActivity mActivity;

	private Device mDevice;
	private Adapter mAdapter;
	
	private SaveDeviceTask mSaveDeviceTask;
	private GetDeviceLogTask mGetDeviceLogTask;
	private SaveFacilityTask mSaveFacilityTask;
	private ActorActionTask mActorActionTask;

	public static final String ARG_PAGE = "page";
	public static final String ARG_CUR_PAGE = "currentpage";
	public static final String ARG_SEL_PAGE = "selectedpage";
	public static final String ARG_LOC_ID = "locationid";
	public static final String ARG_ADAPTER_ID = "adapterid";
	
	private UnitsHelper mUnitsHelper;
	private TimeHelper mTimeHelper;

	private String mPageNumber;
	private String mLocationID;
	private int mCurPageNumber;
	private int mSelPageNumber;
	private String mAdapterId;

	private boolean mWasTapLayout = false;
	private boolean mWasTapGraph = false;
	private int mEditMode = EDIT_NONE;

	//
	private ActionMode mMode;

	private int mLastProgressRefreshTime;

	private GraphViewSeries mGraphSeries;
	

	private static final String GRAPH_DATE_TIME_FORMAT = "dd.MM. kk:mm";
	private static final String LOG_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

	/**
	 * Factory method for this fragment class. Constructs a new fragment for the given page number.
	 */
	public static SensorDetailFragment create(String IDSensor, String IDLocation, int position, int selPosition, String adapterId) {
		SensorDetailFragment fragment = new SensorDetailFragment();
		Bundle args = new Bundle();
		args.putString(ARG_PAGE, IDSensor);
		args.putString(ARG_LOC_ID, IDLocation);
		args.putInt(ARG_CUR_PAGE, position);
		args.putInt(ARG_SEL_PAGE, selPosition);
		args.putString(ARG_ADAPTER_ID, adapterId);
		fragment.setArguments(args);
		return fragment;
	}

	public SensorDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mPageNumber = getArguments().getString(ARG_PAGE);
		mLocationID = getArguments().getString(ARG_LOC_ID);
		mSelPageNumber = getArguments().getInt(ARG_SEL_PAGE);
		mCurPageNumber = getArguments().getInt(ARG_CUR_PAGE);
		mAdapterId = getArguments().getString(ARG_ADAPTER_ID);
		Log.d(TAG, "Here 1 " + mCurPageNumber);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mActivity = (SensorDetailActivity) getActivity();

		// Get controller
		mController = Controller.getInstance(mActivity.getApplicationContext());
		
		mAdapter = mController.getAdapter(mAdapterId);

		View view = inflater.inflate(R.layout.activity_sensor_detail_screen, container, false);
		Log.d(TAG, String.format("this position: %s , selected item: %s ", mCurPageNumber, mSelPageNumber));

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mDevice = mController.getDevice(mAdapterId, mPageNumber);
		if (mDevice != null) {
			Log.d(TAG, String.format("ID: %s, Name: %s", mDevice.getId(), mDevice.getName()));
			initLayout(mDevice);
		}

		Log.d(TAG, "Here 3 " + mCurPageNumber);
	}

	@Override
	public void onStop() {
		if (mSaveDeviceTask != null) {
			mSaveDeviceTask.cancel(true);
		}
		if (mGetDeviceLogTask != null) {
			mGetDeviceLogTask.cancel(true);
		}
		super.onStop();
	}

	private void initLayout(Device device) {
		final Context context = getActivity();// SensorDetailFragment.this.getView().getContext();
		// Get View for sensor name
		mName = (TextView) getView().findViewById(R.id.sen_detail_name);
		mNameEdit = (EditText) getView().findViewById(R.id.sen_detail_name_edit);
		mRectangleName = (RelativeLayout) getView().findViewById(R.id.sen_rectangle_name);
		// Get View for sensor location
		mLocation = (TextView) getView().findViewById(R.id.sen_detail_loc_name);
		mRectangleLoc = (RelativeLayout) getView().findViewById(R.id.sen_rectangle_loc);
		mSpinnerLoc = (Spinner) getView().findViewById(R.id.sen_detail_spinner_choose_location);
		// Get View for sensor value
		mValue = (TextView) getView().findViewById(R.id.sen_detail_value);
		mValueSwitch = (Button) getView().findViewById(R.id.sen_detail_value_switch);
		// Get View for sensor time
		mTime = (TextView) getView().findViewById(R.id.sen_detail_time);
		// Get Image for sensor
		mIcon = (ImageView) getView().findViewById(R.id.sen_detail_icon);
		// Get TextView for refresh time
		mRefreshTimeText = (TextView) getView().findViewById(R.id.sen_refresh_time_value);
		// Get SeekBar for refresh time
		mRefreshTimeValue = (SeekBar) getView().findViewById(R.id.sen_refresh_time_seekBar);
		// Set title selected for animation if is text long
		mName.setSelected(true);
		mLocation.setSelected(true);
		// Set Max value by length of array with values
		mRefreshTimeValue.setMax(RefreshInterval.values().length - 1);

		mRefreshTimeValue.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				String interval = RefreshInterval.values()[progress].getStringInterval(context);
				mRefreshTimeText.setText(" " + interval);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// Set variable if this first touch
				if (mEditMode != EDIT_NONE)
					return;

				if (mEditMode != EDIT_REFRESH_T) {
					mEditMode = EDIT_REFRESH_T;
					// Disable Swipe
					mActivity.setEnableSwipe(false);
					mMode =  ((ActionBarActivity) getActivity()).startSupportActionMode(new AnActionModeOfSensorEdit());
					mLastProgressRefreshTime = seekBar.getProgress();
				}

			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				String interval = RefreshInterval.values()[seekBar.getProgress()].getStringInterval(context);
				Log.d(TAG, String.format("Stop select value %s", interval));
			}
		});
		// Get LinearLayout for graph
		mGraphLayout = (LinearLayout) getView().findViewById(R.id.sen_graph_layout);
		mGraphLabel = (TextView) getView().findViewById(R.id.sen_graph_name);
		mGraphInfo = (TextView) getView().findViewById(R.id.sen_graph_info);
		// Get RelativeLayout of detail
		mLayoutRelative = (RelativeLayout) getView().findViewById(R.id.sensordetail_scroll);
		mLayoutScroll = (ScrollView) getView().findViewById(R.id.sensordetail_layout);
		mLayoutRelative.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// Disable graph if in edit Mode
				if (mEditMode != EDIT_NONE)
					return false;

				if (mWasTapLayout)
					return true;

				mWasTapLayout = true;
				mWasTapGraph = false;
				if (mGraphView != null) {
					mGraphView.setScalable(false);
					mGraphView.setScrollable(false);
					mActivity.setEnableSwipe(true);
					mGraphInfo.setVisibility(View.VISIBLE);
					onTouch(v, event);
					return true;
				}
				return false;
			}
		});


		// Set name of sensor
		mName.setText(device.getName());
		mName.setBackgroundColor(Color.TRANSPARENT);
		if(mController.isUserAllowed(mAdapter.getRole())) {
			mName.setOnClickListener(new OnClickListener() {
	
				@Override
				public void onClick(View v) {
					if (mEditMode != EDIT_NONE)
						return;
					// Disable SeekBar
					mRefreshTimeValue.setEnabled(false);
					// Disable SwipeGesture
					mActivity.setEnableSwipe(false);
	
					mEditMode = EDIT_NAME;
					mMode =  ((ActionBarActivity) getActivity()).startSupportActionMode(new AnActionModeOfSensorEdit());
					mName.setVisibility(View.GONE);
					mRectangleName.setVisibility(View.GONE);
					mNameEdit.setVisibility(View.VISIBLE);
					mNameEdit.setText(mName.getText());
					InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
					// return true;
				}
			});
		}
		
		if(mController.isUserAllowed(mAdapter.getRole())) {
			// Set value for Actor
			mValueSwitch.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// Disable button
					mValueSwitch.setEnabled(false);
					doActorAction(mDevice);
				}
			});
		}
		// Set name of location
		if (mController != null) {
			Location location = null;

			Adapter adapter = mController.getAdapter(mAdapterId);
			if (adapter != null) {
				location = mController.getLocation(adapter.getId(), device.getFacility().getLocationId());
			}

			if (location != null) {
				mLocation.setText(location.getName());
			}
			if(mController.isUserAllowed(mAdapter.getRole())) {
				mLocation.setOnClickListener(new OnClickListener() {
	
					@Override
					public void onClick(View v) {
						if (mEditMode != EDIT_NONE)
							return;
						// Disable SeekBar
						mRefreshTimeValue.setEnabled(false);
	
						// Disable Swipe
						mActivity.setEnableSwipe(false);
	
						mEditMode = EDIT_LOC;
						mMode =  ((ActionBarActivity) getActivity()).startSupportActionMode(new AnActionModeOfSensorEdit());
						mSpinnerLoc.setVisibility(View.VISIBLE);
						mLocation.setVisibility(View.GONE);
						mRectangleLoc.setVisibility(View.GONE);
	
						// open Spinner
						mSpinnerLoc.performClick();
					}
				});
			}
		} else {
			Log.e(TAG, "mController is null (this shouldn't happen)");
			mLocation.setText(device.getFacility().getLocationId());
		}

		// Set locations to spinner
		LocationArrayAdapter dataAdapter = new LocationArrayAdapter(this.getActivity(), R.layout.custom_spinner_item, getLocationsArray());
		dataAdapter.setLayoutInflater(getLayoutInflater(null));
		dataAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);

		mSpinnerLoc.setAdapter(dataAdapter);
		mSpinnerLoc.setSelection(getLocationsIndexFromArray(getLocationsArray()));

		Facility facility = device.getFacility();
		Adapter adapter = mController.getAdapter(facility.getAdapterId());

		// UserSettings can be null when user is not logged in!
		SharedPreferences prefs = mController.getUserSettings();

		mUnitsHelper = (prefs == null) ? null : new UnitsHelper(prefs, getActivity().getApplicationContext());
		mTimeHelper = (prefs == null) ? null : new TimeHelper(prefs);

		// Set value of sensor
		if (mUnitsHelper != null) {
			mValue.setText(mUnitsHelper.getStringValueUnit(device.getValue()));
			mValueSwitch.setText(mUnitsHelper.getStringValueUnit(device.getValue()));
		}

		// Set icon of sensor
		mIcon.setImageResource(device.getIconResource());

		// Set time of sensor
		if (mTimeHelper != null) {
			mTime.setText(mTimeHelper.formatLastUpdate(facility.getLastUpdate(), adapter));
		}

		// Set refresh time Text
		mRefreshTimeText.setText(" " + facility.getRefresh().getStringInterval(context));

		// Set refresh time SeekBar
		mRefreshTimeValue.setProgress(facility.getRefresh().getIntervalIndex());

		// Add Graph with history data
		if (mUnitsHelper != null && mTimeHelper != null) {
			DateTimeFormatter fmt = mTimeHelper.getFormatter(GRAPH_DATE_TIME_FORMAT, adapter);
			addGraphView(fmt, mUnitsHelper);
		}

		// Visible all elements
		visibleAllElements();

		DisplayMetrics displaymetrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);


		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mGraphInfo.getLayoutParams();


		// substitute parameters for left, top, right, bottom
		params.setMargins((int) ((displaymetrics.widthPixels / 2) - (70 * displaymetrics.density)), (int) ((-120) * displaymetrics.density), 0, 0);
		mGraphInfo.setLayoutParams(params);

		// Disable progress bar
		getActivity().setProgressBarIndeterminateVisibility(false);
	}

	

	private void visibleAllElements() {
		mName.setVisibility(View.VISIBLE);
		// mNameEdit;
		mLocation.setVisibility(View.VISIBLE);
		mValue.setVisibility(View.VISIBLE);

		// Show some controls if this device is an actor
		if (mDevice.getType().isActor() && mController.isUserAllowed(mAdapter.getRole())) {
			BaseValue value = mDevice.getValue();
			
			// For actor values of type on/off, open/closed we show switch button
			if (value instanceof OnOffValue || value instanceof OpenClosedValue) {
				mValueSwitch.setVisibility(View.VISIBLE);
				mValue.setVisibility(View.GONE);
			}
		}
			
		mTime.setVisibility(View.VISIBLE);
		mIcon.setVisibility(View.VISIBLE);
		mRefreshTimeText.setVisibility(View.VISIBLE);
		((TextView) getView().findViewById(R.id.sen_refresh_time)).setVisibility(View.VISIBLE);
		
		if(mController.isUserAllowed(mAdapter.getRole()))
			mRefreshTimeValue.setVisibility(View.VISIBLE);
		
		mGraphLayout.setVisibility(View.VISIBLE);
		mGraphLabel.setVisibility(View.VISIBLE);
		// mLayout;
		if(mController.isUserAllowed(mAdapter.getRole())) {
			mRectangleName.setVisibility(View.VISIBLE);
			mRectangleLoc.setVisibility(View.VISIBLE);
		}
		// mSpinnerLoc;
		if (mGraphView != null) {
			mGraphView.setVisibility(View.VISIBLE);
			mGraphInfo.setVisibility(View.VISIBLE);
		}

	}

	private void addGraphView(final DateTimeFormatter fmt, final UnitsHelper unitsHelper) {
		// Create and set graphView
		mGraphView = GraphViewHelper.prepareGraphView(getView().getContext(), "", mDevice, fmt, unitsHelper); // empty heading
		
		mGraphView.setVisibility(View.GONE);
		mGraphView.setScrollable(false);
		mGraphView.setScalable(false);
		
		if (mGraphView instanceof LineGraphView) {
			mGraphView.setBackgroundColor(getResources().getColor(R.color.alpha_blue));// getResources().getColor(R.color.log_blue2));
			((LineGraphView) mGraphView).setDrawBackground(true);
		}
		// graphView.setAlpha(128);

		// Add data series
		GraphViewSeriesStyle seriesStyleBlue = new GraphViewSeriesStyle(getResources().getColor(R.color.beeeon_primary_cyan), 2);
		// GraphViewSeriesStyle seriesStyleGray = new GraphViewSeriesStyle(getResources().getColor(R.color.light_gray),2);

		mGraphSeries = new GraphViewSeries("Graph", seriesStyleBlue, new GraphViewData[] { new GraphView.GraphViewData(0, 0), });
		mGraphView.addSeries(mGraphSeries);
		
		if (!(mDevice.getValue() instanceof BaseEnumValue)) {
			mGraphView.setManualYAxisBounds(1.0, 0.0);
		}

		mGraphLayout.addView(mGraphView);

		mGraphLayout.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// Disable graph if in edit Mode
				if (mEditMode != EDIT_NONE)
					return false;

				if (mWasTapGraph)
					return true;

				mWasTapLayout = false;
				mWasTapGraph = true;

				Log.d(TAG, "onTouch layout");
				mGraphView.setScrollable(true);
				mGraphView.setScalable(true);
				mActivity.setEnableSwipe(false);
				mGraphInfo.setVisibility(View.GONE);
				onTouch(v, event);
				return true;
			}
		});

		loadGraphData();
	}

	private void loadGraphData() {
		DateTime end = DateTime.now(DateTimeZone.UTC);
		DateTime start = end.minusWeeks(1);

		DateTimeFormatter fmt = DateTimeFormat.forPattern(LOG_DATE_TIME_FORMAT).withZoneUTC();
		Log.d(TAG, String.format("Loading graph data from %s to %s.", fmt.print(start), fmt.print(end)));

		mGetDeviceLogTask = new GetDeviceLogTask();
		LogDataPair pair = new LogDataPair( //
				mDevice, // device
				new Interval(start, end), // interval from-to
				DataType.AVERAGE, // type
				(mDevice.getValue() instanceof BaseEnumValue )?DataInterval.RAW:DataInterval.HOUR); // interval
		mGetDeviceLogTask.execute(new LogDataPair[] { pair });
	}

	private List<Location> getLocationsArray() {
		// Get locations from adapter
		List<Location> locations = new ArrayList<Location>();

		Adapter adapter = mController.getActiveAdapter();
		if (adapter != null) {
			locations = mController.getLocations(adapter.getId());
		}

		// Sort them
		Collections.sort(locations);

		return locations;
	}

	private int getLocationsIndexFromArray(List<Location> locations) {
		int index = 0;
		for (Location room : locations) {
			if (room.getId().equalsIgnoreCase(mLocationID)) {
				return index;
			}
			index++;
		}
		return index;
	}

	public void fillGraph(DeviceLog log) {
		if (mGraphView == null) {
			return;
		}

		SortedMap<Long, Float> values = log.getValues();
		int size = values.size();
		GraphView.GraphViewData[] data = new GraphView.GraphViewData[size];
		
		Log.d(TAG, String.format("Filling graph with %d values. Min: %.1f, Max: %.1f", size, log.getMinimum(), log.getMaximum()));

		int i = 0;
		for (Entry<Long, Float> entry : values.entrySet()) {
			Long dateMillis = entry.getKey();
			float value = Float.isNaN(entry.getValue()) ? log.getMinimum() : entry.getValue();

			data[i++] = new GraphView.GraphViewData(dateMillis, value);
			
			// This shouldn't happen, only when some other thread changes this values object - can it happen?
			if (i >= size)
				break;
		}
		
		Log.d(TAG, "Filling graph finished");

		// Set maximum as +10% more than deviation
		if (!(mDevice.getValue() instanceof BaseEnumValue)) {
			mGraphView.setManualYAxisBounds(log.getMaximum() + log.getDeviation() * 0.1, log.getMinimum());
		}
		// mGraphView.setViewPort(0, 7);
		mGraphSeries.resetData(data);
		mGraphInfo.setText(getView().getResources().getString(R.string.sen_detail_graph_info));
	}

	/*
	 * ================================= ASYNC TASK ===========================
	 */
	
	protected void doActorAction(final Device device) {
		if (!device.getType().isActor()) {
			return;
		}

		// SET NEW VALUE
		BaseValue value = device.getValue();
		if (value instanceof BaseEnumValue) {
			((BaseEnumValue)value).setNextValue();
		} else {
			Log.e(TAG, "We can't switch actor, which value isn't inherited from BaseEnumValue, yet");
			return;
		}

		mActorActionTask = new ActorActionTask(getActivity().getApplicationContext());
		mActorActionTask.setListener(new CallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				// Get new device
				mDevice = mController.getDevice(device.getFacility().getAdapterId(), device.getId());
				
				// Set new value of sensor
				if (mUnitsHelper != null) {
					mValueSwitch.setText(mUnitsHelper.getStringValueUnit(mDevice.getValue()));
				}
				// Set icon of sensor
				mIcon.setImageResource(mDevice.getIconResource());
				// Enable button
				mValueSwitch.setEnabled(true);
			}
			
		});
		mActorActionTask.execute(device);
	}

	private void doSaveDeviceTask(SaveDevicePair pair) {
		mSaveDeviceTask = new SaveDeviceTask(getActivity().getApplicationContext());
		mSaveDeviceTask.setListener(new CallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				if (mActivity.getProgressDialog() != null)
					mActivity.getProgressDialog().dismiss();
				if (success) {
					Log.d(TAG, "Success save to server");
					// Change GUI
					mActivity.redraw();
				} else {
					Log.d(TAG, "Fail save to server");
				}
				int messageId = success ? R.string.toast_success_save_data : R.string.toast_fail_save_data;
				Log.d(TAG, mActivity.getString(messageId));
				new ToastMessageThread(mActivity, messageId).start();
			}
		});

		mSaveDeviceTask.execute(pair);
	}

	public void doSaveFacilityTask(SaveFacilityPair pair) {
		mSaveFacilityTask = new SaveFacilityTask(mActivity);
		mSaveFacilityTask.setListener(new CallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (mActivity.getProgressDialog() != null)
					mActivity.getProgressDialog().dismiss();
				if (success) {
					Log.d(TAG, "Success save to server");
					// Change GUI
					mActivity.redraw();
				} else {
					Log.d(TAG, "Fail save to server");
				}
				int messageId = success ? R.string.toast_success_save_data : R.string.toast_fail_save_data;
				Log.d(TAG, mActivity.getString(messageId));
				new ToastMessageThread(mActivity, messageId).start();
			}
		});
		mSaveFacilityTask.execute(pair);
	}

	/**
	 * Changes selected location and redraws list of adapters there
	 */
	private class GetDeviceLogTask extends AsyncTask<LogDataPair, Void, DeviceLog> {
		@Override
		protected DeviceLog doInBackground(LogDataPair... pairs) {
			LogDataPair pair = pairs[0]; // expects only one device at a time is sent there

			// Load log data if needed
			mController.reloadDeviceLog(pair);
			
			// Get loaded log data (TODO: this could be done in gui)
			return mController.getDeviceLog(pair);
		}

		@Override
		protected void onPostExecute(DeviceLog log) {
			fillGraph(log);
		}

	}

	/*
	 * ============================= ACTION MODE ==============================
	 */

	// Menu for Action bar mode - edit
	class AnActionModeOfSensorEdit implements ActionMode.Callback {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.sensor_detail_menu, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

			switch (mEditMode) {
			case EDIT_LOC:
				mSpinnerLoc.setVisibility(View.GONE);
				mLocation.setVisibility(View.VISIBLE);
				mRectangleLoc.setVisibility(View.VISIBLE);
				mRefreshTimeValue.setEnabled(true);
				if (item.getTitle().equals("Save")) {
					// Progress dialog
					if (mActivity.getProgressDialog() != null)
						mActivity.getProgressDialog().show();
					// Set new location in facility
					mDevice.getFacility().setLocationId(((Location) mSpinnerLoc.getSelectedItem()).getId());
					// Update device to server
					doSaveFacilityTask(new SaveFacilityPair(mDevice.getFacility(), EnumSet.of(SaveDevice.SAVE_LOCATION)));

				}
				break;
			case EDIT_NAME:
				if (item.getTitle().equals("Save")) {
					mName.setText(mNameEdit.getText());

					// Set new name to device
					mDevice.setName(mNameEdit.getText().toString());

					// Update device to server
					doSaveDeviceTask(new SaveDevicePair(mDevice, EnumSet.of(SaveDevice.SAVE_NAME)));
				}
				mNameEdit.setVisibility(View.GONE);
				mName.setVisibility(View.VISIBLE);
				mRectangleName.setVisibility(View.VISIBLE);

				mNameEdit.clearFocus();
				imm.hideSoftInputFromWindow(mNameEdit.getWindowToken(), 0);
				mRefreshTimeValue.setEnabled(true);
				break;
			case EDIT_REFRESH_T:
				// Was clicked on cancel
				if (item.getTitle().equals("Cancel")) {
					mRefreshTimeValue.setProgress(mLastProgressRefreshTime);
				} else {
					// set actual progress
					mDevice.getFacility().setRefresh(RefreshInterval.values()[mRefreshTimeValue.getProgress()]);
					// Progress dialog
					if (mActivity.getProgressDialog() != null)
						mActivity.getProgressDialog().show();
					Log.d(TAG, "Refresh time " + mDevice.getFacility().getRefresh().getStringInterval(mActivity));
					// Update device to server
					doSaveDeviceTask(new SaveDevicePair(mDevice, EnumSet.of(SaveDevice.SAVE_REFRESH)));
				}
				break;

			default:
				break;

			}

			mEditMode = EDIT_NONE;
			// enable SeekBar

			mode.finish();
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			// Control mode and set default values
			switch (mEditMode) {
			case EDIT_REFRESH_T:
				mRefreshTimeValue.setProgress(mLastProgressRefreshTime);
				break;
			}
			mActivity.setEnableSwipe(true);

			mSpinnerLoc.setVisibility(View.GONE);
			mLocation.setVisibility(View.VISIBLE);
			mRectangleLoc.setVisibility(View.VISIBLE);
			mNameEdit.setVisibility(View.GONE);
			mName.setVisibility(View.VISIBLE);
			mRectangleName.setVisibility(View.VISIBLE);
			mNameEdit.clearFocus();
			InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(mNameEdit.getWindowToken(), 0);
			// mRefreshTimeValue.setProgress(mLastProgressRefreshTime);
			mEditMode = EDIT_NONE;
			// enable SeekBar
			mRefreshTimeValue.setEnabled(true);
		}
	}

}