package cz.vutbr.fit.iha.activity;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.controller.Controller;

/**
 * The control preference activity handles the preferences for the control
 * extension.
 */
public class SettingsUnitActivity extends SherlockPreferenceActivity implements
		OnSharedPreferenceChangeListener {
	/**
	 * keys which are defined in res/xml/preferences.xml
	 */

	private ListPreference mListPrefTemperature;
	private Controller mController;
	private SharedPreferences mPrefs;

	// added suppressWarnings because of support of lower version
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mController = Controller.getInstance(this);
		
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		mPrefs = mController.getUserSettings();

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.unit_preferences);

		mListPrefTemperature = (ListPreference) findPreference(Constants.PREF_TEMPERATURE);

	}

	@Override
	protected void onResume() {
		super.onResume();
		mPrefs.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mPrefs.unregisterOnSharedPreferenceChangeListener(this);
	}


	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}
		return false;
	}

	// @Override
	// public boolean onPreferenceChange(Preference preference, Object newValue)
	// {
	//
	// setDefaultLocAndAdap();
	//
	// return true;
	// }

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
	}

}