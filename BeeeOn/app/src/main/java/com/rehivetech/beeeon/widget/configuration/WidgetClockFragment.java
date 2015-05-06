package com.rehivetech.beeeon.widget.configuration;


import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.dialog.ILocationPickerDialogListener;
import com.rehivetech.beeeon.activity.dialog.LocationPickerDialogFragment;
import com.rehivetech.beeeon.arrayadapter.DeviceArrayAdapter;
import com.rehivetech.beeeon.household.adapter.Adapter;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.location.Location;
import com.rehivetech.beeeon.util.Utils;
import com.rehivetech.beeeon.widget.data.WidgetClockData;
import com.rehivetech.beeeon.widget.persistence.WidgetDevicePersistence;
import com.rehivetech.beeeon.widget.service.WeatherProvider;
import com.rehivetech.beeeon.widget.service.WidgetService;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mlyko
 */
public class WidgetClockFragment extends WidgetConfigurationFragment implements ILocationPickerDialogListener{
	private static final String TAG = WidgetClockFragment.class.getSimpleName();
	private static final int REQUEST_LOCATION_DIALOG = 1;

	protected SeekBar mWidgetUpdateSeekBar;

	protected WidgetClockData mWidgetData;

	protected List<WidgetDevicePersistence> mWidgetDevices;
	protected List<Spinner> mDeviceSpinners;
	private LinearLayout mDeviceSpinnersWrapper;

	private RadioGroup mColorSchemeGroup;
	private TextView mCityLabel;
	private WeatherProvider mWeatherProvider;
	private Handler mHandler;

	public WidgetClockFragment(){
		mHandler = new Handler();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mGeneralWidgetdata = new WidgetClockData(mActivity.getWidgetId(), mActivity, null, null);
		mWidgetData = (WidgetClockData) mGeneralWidgetdata;

		mWidgetDevices = mWidgetData.widgetDevices;
		mDeviceSpinners = new ArrayList<>();

		setRefreshBounds(WidgetService.UPDATE_INTERVAL_WEATHER_MIN);

		mWeatherProvider = new WeatherProvider(mActivity);
	}

	protected int getFragmentLayoutResource(){
		return R.layout.fragment_widget_clock;
	}

	protected int getFragmentTitle(){
		return R.string.widget_configuration_widget_clock;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mWidgetUpdateSeekBar = (SeekBar) mActivity.findViewById(R.id.widget_config_interval);
		initWidgetUpdateIntervalLayout(mWidgetUpdateSeekBar);

		mDeviceSpinnersWrapper = (LinearLayout) mActivity.findViewById(R.id.widget_config_devices);
		LinearLayout.LayoutParams spinnerLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		spinnerLayoutParams.setMargins(0, 0, 0, (int) mActivity.getResources().getDimension(R.dimen.widget_margin));

		for(WidgetDevicePersistence wDev : mWidgetDevices){
			Spinner deviceSpinner = new Spinner(mActivity);
			mDeviceSpinnersWrapper.addView(deviceSpinner, spinnerLayoutParams);
			mDeviceSpinners.add(deviceSpinner);
		}

		mColorSchemeGroup = (RadioGroup) mActivity.findViewById(R.id.widget_configuration_scheme);
		mColorSchemeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				switch (checkedId) {
					case R.id.scheme_white:
						mWidgetData.settings.setColorScheme(R.color.white, R.color.white);
						break;

					case R.id.scheme_black:
						mWidgetData.settings.setColorScheme(R.color.black, R.color.black);
						break;

					case R.id.scheme_pink_cyan:
					default:
						mWidgetData.settings.setColorScheme(R.color.beeeon_primary_cyan, R.color.beeeon_secundary_pink);
						break;
				}
			}
		});

		mCityLabel = (TextView) mActivity.findViewById(R.id.widget_config_location_label);

		RelativeLayout locationChooseLine = (RelativeLayout) mActivity.findViewById(R.id.widget_config_location);
		locationChooseLine.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showLocationPickerDialog();
			}
		});
	}

	private void showLocationPickerDialog() {
		LocationPickerDialogFragment
				.createBuilder(mActivity, mActivity.getSupportFragmentManager())
				.setTitle(mActivity.getString(R.string.dialog_location_select))
				.setCityName(mWidgetData.weather.cityName)
				.setPositiveButtonText(mActivity.getString(R.string.ok))
				.setNegativeButtonText(mActivity.getString(R.string.action_close))
				.setTargetFragment(this, REQUEST_LOCATION_DIALOG)
				.show();
	}

	@Override
	public void onPositiveButtonClicked(int var1, EditText city, final LocationPickerDialogFragment dialog) {
		if(city.getText().length() == 0){
			city.setError(mActivity.getString(R.string.place_must_be_filled));
			return;
		}

		// show dialog of loading
		if(mActivity.getDialog() != null) mActivity.getDialog(mActivity.getString(R.string.progress_checking_location)).show();

		final String cityInput = city.getText().toString();
		// load city data in background
		new Thread(){
			public void run(){
				final JSONObject data = mWeatherProvider.getLocations(cityInput);
				if(data == null){
					mHandler.post(new Runnable(){
						public void run(){
							loadingCityFail();
						}
					});
				}
				else{
					final List<WeatherProvider.City> foundCities = mWeatherProvider.parseCities(data);
					mHandler.post(new Runnable(){
						public void run(){
							WeatherProvider.City city = foundCities.get(0);
							if(city == null){
								loadingCityFail();
								return;
							}
							loadingCitySuccess(city, dialog);
						}
					});
				}
			}
		}.start();
	}

	private void loadingCityFail(){
		Toast.makeText(mActivity, mActivity.getString(R.string.weather_place_not_found), Toast.LENGTH_LONG).show();
		if(mActivity.getDialog() != null) mActivity.getDialog().dismiss();
	}

	private void loadingCitySuccess(WeatherProvider.City city, LocationPickerDialogFragment dialog){
		// setup weather persistence
		mWidgetData.weather.id = city.id;
		mWidgetData.weather.cityName = city.name;
		mWidgetData.weather.country = city.countryId;

		//mWidgetData.weather.configure(city.json, null);
		// setup city label
		mCityLabel.setText(city.name);

		// hide location picker dialog
		if(dialog != null) dialog.dismiss();
		// hide progress dialog
		if(mActivity.getDialog() != null) mActivity.getDialog().dismiss();
	}

	@Override
	public void onNegativeButtonClicked(int var1, EditText city, LocationPickerDialogFragment dialog) {
		dialog.dismiss();
	}

	@Override
	protected void onFragmentResume() {
		super.onFragmentResume();
		updateIntervalLayout(mWidgetUpdateSeekBar);

		// setup weather location if provided
		if(!mWidgetData.weather.cityName.isEmpty()) mCityLabel.setText(mWidgetData.weather.cityName);

		if(mWidgetData.settings.isColorSchemeEqual(R.color.white, R.color.white)){
			mColorSchemeGroup.check(R.id.scheme_white);
		}
		else if(mWidgetData.settings.isColorSchemeEqual(R.color.white, R.color.white)){
			mColorSchemeGroup.check(R.id.scheme_pink_cyan);
		}
		else if(mWidgetData.settings.isColorSchemeEqual(R.color.black, R.color.black)){
			mColorSchemeGroup.check(R.id.scheme_black);
		}
	}

	/**
	 * Updates layout and expects to have all data fresh
	 */
	protected void updateLayout() {
		// fill sensor spinner
		DeviceArrayAdapter dataAdapter = new DeviceArrayAdapter(mActivity, R.layout.custom_spinner2_item, mDevices, mLocations);
		dataAdapter.setLayoutInflater(mActivity.getLayoutInflater());
		dataAdapter.setDropDownViewResource(R.layout.custom_spinner2_dropdown_item);

		int index = 0;
		for(WidgetDevicePersistence wDev : mWidgetDevices){
			Spinner spinner = mDeviceSpinners.get(index);
			spinner.setAdapter(dataAdapter);

			int foundIndex = Utils.getObjectIndexFromList(wDev.getId(), mDevices);
			if(foundIndex != -1) spinner.setSelection(foundIndex);

			index++;
		}
	}

	@Override
	protected boolean saveSettings() {
		Adapter adapter = (Adapter) mAdapterSpinner.getSelectedItem();
		if (adapter == null) {
			Toast.makeText(mActivity, R.string.widget_configuration_select_adapter, Toast.LENGTH_LONG).show();
			return false;
		}

		int index = 0;
		for(WidgetDevicePersistence wDev : mWidgetDevices) {
			Spinner spinner = mDeviceSpinners.get(index);

			Device device = (Device) spinner.getSelectedItem();
			if (device == null) {
				Toast.makeText(mActivity, R.string.widget_configuration_select_device, Toast.LENGTH_LONG).show();
				return false;
			}

			Location location = Utils.getFromList(device.getFacility().getLocationId(), mLocations);
			if(location != null) {
				wDev.configure(device, adapter, location);
			}
			else{
				wDev.configure(device, adapter);
			}

			index++;
		}

		// setup widget
		mWidgetData.configure(mActivity.isAppWidgetEditing(), getRefreshSeconds(mWidgetUpdateSeekBar.getProgress()), adapter);
		return true;
	}
}

