package cz.vutbr.fit.iha.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.view.ActionMode;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;

import cz.vutbr.fit.iha.Constants;
import cz.vutbr.fit.iha.NavDrawerMenu;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.activity.dialog.AddAdapterFragmentDialog;
import cz.vutbr.fit.iha.activity.dialog.AddSensorFragmentDialog;
import cz.vutbr.fit.iha.activity.dialog.CustomAlertDialog;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.adapter.location.Location;
import cz.vutbr.fit.iha.controller.Controller;

/**
 * Activity class for choosing location
 * 
 * 
 */
public class LocationScreenActivity extends BaseApplicationActivity {
	private static final String TAG = LocationScreenActivity.class.getSimpleName();

	private Controller mController;

	private static final String ADD_ADAPTER_TAG = "addAdapterDialog";
	private static final String ADD_SENSOR_TAG = "addSensorDialog";
	private NavDrawerMenu mNavDrawerMenu;
	private ListOfDevices mListDevices;

	/**
	 * Instance save state tags
	 */
	private static final String LCTN = "lastlocation";
	private static final String ADAPTER_ID = "lastAdapterId";
	private static final String IS_DRAWER_OPEN = "draweropen";

	/**
	 * saved instance states
	 */
	private String mActiveLocationId;
	private String mActiveAdapterId;
	private boolean mIsDrawerOpen = false;

	private Handler mTimeHandler = new Handler();
	private Runnable mTimeRun;

	/**
	 * Tasks which can be running in this activity and after finishing can try to change GUI -> must be cancelled when activity stop
	 */
	private CustomAlertDialog mDialog;

	//
	private ActionMode mMode;

	private boolean backPressed = false;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.activity_location_screen);

		// Get controller
		mController = Controller.getInstance(this);

		setSupportProgressBarIndeterminate(true);
		setSupportProgressBarIndeterminateVisibility(true);
		getSupportActionBar().setIcon(R.drawable.ic_launcher_white);

		

		// Create NavDrawerMenu
		mNavDrawerMenu = new NavDrawerMenu(this);
		mNavDrawerMenu.openMenu();
		mNavDrawerMenu.setIsDrawerOpen(mIsDrawerOpen);
		
		mListDevices = new ListOfDevices(this);
		mListDevices.setMenu(mNavDrawerMenu);
		
		if (savedInstanceState != null) {
			mIsDrawerOpen = savedInstanceState.getBoolean(IS_DRAWER_OPEN);
			mNavDrawerMenu.setIsDrawerOpen(mIsDrawerOpen);
			
			mActiveLocationId = savedInstanceState.getString(LCTN);
			mActiveAdapterId = savedInstanceState.getString(ADAPTER_ID);
			mListDevices.setLocationID(mActiveLocationId);
			mListDevices.setAdapterID(mActiveAdapterId);
			
		}
		
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.content_frame, mListDevices);
		ft.commit();
	}

	public void onAppResume() {
		Log.d(TAG, "onAppResume()");

		backPressed = false;
		
		mNavDrawerMenu.redrawMenu();
		
		checkNoAdapters();
	}

	public void onAppPause() {
		mTimeHandler.removeCallbacks(mTimeRun);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");

		this.setSupportProgressBarIndeterminateVisibility(false);

		if (mDialog != null) {
			mDialog.dismiss();
		}
		// Cancel all task if same is running from Menu
		if(mNavDrawerMenu != null)
			mNavDrawerMenu.cancelAllTasks();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (backPressed) {
			backPressed = false;
		}
		return super.dispatchTouchEvent(ev);
	}

	

	@Override
	public void onBackPressed() {
		Log.d(TAG, "BackPressed - onBackPressed " + String.valueOf(backPressed));
		if(mNavDrawerMenu!= null) {
			if (backPressed) {
				// second click
				mNavDrawerMenu.secondTapBack();
			} else {
				// first click
				mNavDrawerMenu.firstTapBack();
				backPressed = true;
			}
		}
		return;
	}
	
	public boolean getBackPressed() {
		return backPressed;
	}
	
	public void setBackPressed(boolean val) {
		backPressed = val;
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString(ADAPTER_ID, mActiveAdapterId);
		savedInstanceState.putString(LCTN, mActiveLocationId);
		savedInstanceState.putBoolean(IS_DRAWER_OPEN, mNavDrawerMenu.getIsDrawerOpen());
		super.onSaveInstanceState(savedInstanceState);
	}

	public boolean redrawDevices() {
		mListDevices.setIsPaused(isPaused);
		mListDevices.setLocationID(mActiveLocationId);
		mListDevices.setAdapterID(mActiveAdapterId);
		return mListDevices.redrawDevices();
	}
	
	public void redrawMenu() {
		mNavDrawerMenu.redrawMenu();
	}
	
	public void checkNoAdapters() {
		if (mController.getActiveAdapter() == null) {
			if (!mController.getUserSettings().getBoolean(Constants.PERSISTENCE_PREF_IGNORE_NO_ADAPTER, false)) {
				DialogFragment newFragment = new AddAdapterFragmentDialog();
			    newFragment.show(getSupportFragmentManager(), ADD_ADAPTER_TAG);
			}
		}
	}

	public void checkNoDevices() {
		Adapter adapter = mController.getActiveAdapter(); 
		if (adapter != null && mController.getFacilitiesByAdapter(adapter.getId()).isEmpty()) {
			// Show activity for adding new sensor, when this adapter doesn't have any yet
			Log.i(TAG, String.format("%s is empty", adapter.getName()));
			DialogFragment newFragment = new AddSensorFragmentDialog();
		    newFragment.show(getSupportFragmentManager(), ADD_SENSOR_TAG);
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggles
		mNavDrawerMenu.onConfigurationChanged(newConfig);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.location_screen, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			mNavDrawerMenu.clickOnHome();
			break;

		case R.id.action_addadapter: {
			DialogFragment newFragment = new AddAdapterFragmentDialog();
		    newFragment.show(getSupportFragmentManager(), "missiles");

		    
			break;
		}
		case R.id.action_settings: {
			Intent intent = new Intent(LocationScreenActivity.this, SettingsMainActivity.class);
			startActivity(intent);
			break;
		}
		case R.id.action_logout: {
			mController.logout();
			Intent intent = new Intent(LocationScreenActivity.this, LoginActivity.class);
			startActivity(intent);
			this.finish();
			break;
		}
		}

		return super.onOptionsItemSelected(item);
	}

	protected void renameLocation(final String location, final TextView view) {
		AlertDialog.Builder builder = new AlertDialog.Builder(LocationScreenActivity.this);

		// TODO: use better layout than just single EditText
		final EditText edit = new EditText(LocationScreenActivity.this);
		edit.setText(location);
		edit.selectAll();
		// TODO: show keyboard automatically

		builder.setCancelable(false).setView(edit).setTitle("Rename location").setNegativeButton("Cancel", null).setPositiveButton("Rename", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				String newName = edit.getText().toString();

				// TODO: show loading while saving new name to
				// server (+ use asynctask)
				Location location = new Location(); // FIXME: get that original location from somewhere
				location.setName(newName);

				boolean saved = mController.saveLocation(location);

				String message = saved ? String.format("Location was renamed to '%s'", newName) : "Location wasn't renamed due to error";

				Toast.makeText(LocationScreenActivity.this, message, Toast.LENGTH_LONG).show();

				// Redraw item in list
				view.setText(newName);
			}
		});

		AlertDialog dialog = builder.create();
		dialog.show();
	}

	class ActionModeEditSensors implements ActionMode.Callback {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			// TODO Auto-generated method stub
			menu.add("Save").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
			menu.add("Cancel").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			// TODO Auto-generated method stub
			if (item.getTitle().equals("Save")) {
				// sName.setText(sNameEdit.getText());
			}
			// sNameEdit.setVisibility(View.GONE);
			// sName.setVisibility(View.VISIBLE);

			// sNameEdit.clearFocus();
			// getSherlockActivity().getCurrentFocus().clearFocus();
			// InputMethodManager imm = (InputMethodManager) getSystemService(
			// getBaseContext().INPUT_METHOD_SERVICE);
			// imm.hideSoftInputFromWindow(mDrawerItemEdit.getWindowToken(), 0);
			mode.finish();
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
			// TODO Auto-generated method stub
			// sNameEdit.clearFocus();
			// sNameEdit.setVisibility(View.GONE);
			// sName.setVisibility(View.VISIBLE);
			mMode = null;

		}
	}
	
	public void setActiveAdapterID(String adapterId) {
		mActiveAdapterId = adapterId;
	}

	public void setActiveLocationID(String locationId) {
		mActiveLocationId = locationId;
	}

}
