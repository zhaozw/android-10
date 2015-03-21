package com.rehivetech.beeeon.activity.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v4.widget.SwipeRefreshLayout.OnRefreshListener;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.AddAdapterActivity;
import com.rehivetech.beeeon.activity.AddSensorActivity;
import com.rehivetech.beeeon.activity.MainActivity;
import com.rehivetech.beeeon.activity.SensorDetailActivity;
import com.rehivetech.beeeon.adapter.Adapter;
import com.rehivetech.beeeon.adapter.device.Device;
import com.rehivetech.beeeon.adapter.device.Facility;
import com.rehivetech.beeeon.adapter.location.Location;
import com.rehivetech.beeeon.arrayadapter.SensorListAdapter;
import com.rehivetech.beeeon.asynctask.CallbackTask.CallbackTaskListener;
import com.rehivetech.beeeon.asynctask.ReloadFacilitiesTask;
import com.rehivetech.beeeon.asynctask.RemoveFacilityTask;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.pair.DelFacilityPair;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.TutorialHelper;

import net.i2p.android.ext.floatingactionbutton.FloatingActionsMenu;

import java.util.ArrayList;
import java.util.List;

public class SensorListFragment extends Fragment {

	private static final String TAG = SensorListFragment.class.getSimpleName();

	private static final String LCTN = "lastlocation";
	private static final String ADAPTER_ID = "lastAdapterId";

	public static boolean ready = false;
	private SwipeRefreshLayout mSwipeLayout;
	private MainActivity mActivity;
	private Controller mController;
	private ReloadFacilitiesTask mReloadFacilitiesTask;

	private SensorListAdapter mSensorAdapter;
	private ListView mSensorList;
    private FloatingActionsMenu mFAM;

	private View mView;

	private String mActiveLocationId;
	private String mActiveAdapterId;
	private boolean isPaused;

	//
	private ActionMode mMode;
	
	// For tutorial
	private boolean mFirstUseAddAdapter = true;
	private boolean mFirstUseAddSensor = true;

    private Device mSelectedItem;
    private int mSelectedItemPos;
    private RemoveFacilityTask mRemoveFacilityTask;

    public SensorListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate()");
		ready = false;

		if (!(getActivity() instanceof MainActivity)) {
			throw new IllegalStateException("Activity holding SensorListFragment must be MainActivity");
		}

		mActivity = (MainActivity) getActivity();
		mController = Controller.getInstance(mActivity);

		if (savedInstanceState != null) {
			mActiveLocationId = savedInstanceState.getString(LCTN);
			mActiveAdapterId = savedInstanceState.getString(ADAPTER_ID);
		}
		// Check if tutoril was showed
		SharedPreferences prefs = mController.getUserSettings();
		if (prefs != null) {
			mFirstUseAddAdapter = prefs.getBoolean(Constants.TUTORIAL_ADD_ADAPTER_SHOWED, true);
			mFirstUseAddSensor = prefs.getBoolean(Constants.TUTORIAL_ADD_SENSOR_SHOWED, true);
		}
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.listofsensors, container, false);
		redrawDevices();
		return mView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d(TAG, "onActivityCreated()");
		ready = true;

		// mActivity = (MainActivity) getActivity();

		// Init swipe-refreshig layout
		mSwipeLayout = (SwipeRefreshLayout) mActivity.findViewById(R.id.swipe_container);
		if (mSwipeLayout == null) {
			return;
		}
		mSwipeLayout.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh() {

				Adapter adapter = mController.getActiveAdapter();
				if (adapter == null) {
					mSwipeLayout.setRefreshing(false);
					return;
				}
				mActivity.redrawMenu();
				doReloadFacilitiesTask(adapter.getId());
			}
		});
		mSwipeLayout.setColorSchemeColors(  R.color.beeeon_primary_cyan, R.color.beeeon_text_color,R.color.beeeon_secundary_pink);
	}

	public void onPause() {
		super.onPause();
		Log.d(TAG, "onPause()");
		ready = false;
		if(mMode != null)
			mMode.finish();
	}

	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume()");
		ready = true;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy()");

		ready = false;

		if (mReloadFacilitiesTask != null) {
			mReloadFacilitiesTask.cancel(true);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putString(ADAPTER_ID, mActiveAdapterId);
		savedInstanceState.putString(LCTN, mActiveLocationId);
		super.onSaveInstanceState(savedInstanceState);
	}

	public boolean redrawDevices() {
		if (isPaused) {
			mActivity.setSupportProgressBarIndeterminateVisibility(false);
			return false;
		}
		List<Facility> facilities;
        List<Location> locations;

		Log.d(TAG, "LifeCycle: redraw devices list start");

		mSensorList = (ListView) mView.findViewById(R.id.listviewofsensors);
		TextView noItem = (TextView) mView.findViewById(R.id.nosensorlistview);
		ImageView addBtn = (ImageView) mView.findViewById(R.id.nosensorlistview_addsensor_image);

        mFAM = (FloatingActionsMenu) mView.findViewById(R.id.multiple_actions);

        // All locations on adapter
        locations = mController.getLocations(mActiveAdapterId);

        List<Device> devices = new ArrayList<Device>();
		for (Location loc : locations) {
            // all facilities from actual location
            facilities = mController.getFacilitiesByLocation(mActiveAdapterId,loc.getId());
            for(Facility fac : facilities)
			devices.addAll(fac.getDevices());
		}

		if (mSensorList == null) {
			mActivity.setSupportProgressBarIndeterminateVisibility(false);
			Log.e(TAG, "LifeCycle: bad timing or what?");
			return false; // TODO: this happens when we're in different activity
							// (detail), fix that by changing that activity
							// (fragment?) first?
		}

		boolean haveDevices = devices.size() > 0;
		boolean haveAdapters = mController.getAdapters().size() > 0;
		
		
		
		if(!haveAdapters) { // NO Adapter
			noItem.setVisibility(View.VISIBLE);
			noItem.setText(R.string.no_adapter_cap);
			addBtn.setVisibility(View.VISIBLE);
			mSensorList.setVisibility(View.GONE);
			addBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					showAddAdapterDialog();
				}
			});
			
			SharedPreferences prefs = mController.getUserSettings();
			if (!(prefs != null && !prefs.getBoolean(Constants.PERSISTENCE_PREF_IGNORE_NO_ADAPTER, false))) {
				// TUTORIAL
				if(mFirstUseAddAdapter && !mController.isDemoMode()) {
					mActivity.getMenu().closeMenu();
					TutorialHelper.showAddAdapterTutorial(mActivity, mView);
					if (prefs != null) {
						prefs.edit().putBoolean(Constants.TUTORIAL_ADD_ADAPTER_SHOWED, false).commit();
					}
				}
			}
			
		}
		else if (!haveDevices) { // Have Adapter but any Devices
			noItem.setVisibility(View.VISIBLE);
			addBtn.setVisibility(View.VISIBLE);
			mSensorList.setVisibility(View.GONE);
			addBtn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					showAddSensorDialog();
				}
			});
			if(mFirstUseAddSensor && !mController.isDemoMode()){
				mActivity.getMenu().closeMenu();
				TutorialHelper.showAddSensorTutorial(mActivity, mView);
				SharedPreferences prefs = mController.getUserSettings();
				if (prefs != null) {
					prefs.edit().putBoolean(Constants.TUTORIAL_ADD_SENSOR_SHOWED, false).commit();
				}
			}
		}
		else { // Have adapter and devices
			noItem.setVisibility(View.GONE);
			addBtn.setVisibility(View.GONE);
			mSensorList.setVisibility(View.VISIBLE);
		}

        // Listener for add dialogs
		OnClickListener addSensorListener = new OnClickListener() {

			@Override
			public void onClick(View v) {
				showAddSensorDialog();
			}
		};
        OnClickListener addAdapterListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddAdapterDialog();
            }
        };
        // Buttons in floating menu
        mFAM.findViewById(R.id.fab_add_sensor).setOnClickListener(addSensorListener);
        mFAM.findViewById(R.id.fab_add_adapter).setOnClickListener(addAdapterListener);

        if(mController.getAdapter(mActiveAdapterId) != null) {
            // IF can user add senzor
            if (!mController.isUserAllowed(mController.getAdapter(mActiveAdapterId).getRole())) {
                // Hide button
                mFAM.findViewById(R.id.fab_add_sensor).setVisibility(View.GONE);
            }
        }
        else {
            // Hide button
            mFAM.findViewById(R.id.fab_add_sensor).setVisibility(View.GONE);
        }

		// Update list adapter
		mSensorAdapter = new SensorListAdapter(mActivity, devices,true);
		mSensorList.setAdapter(mSensorAdapter);

		if (haveDevices) {
			// Capture listview menu item click
			mSensorList.setOnItemClickListener(new ListView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Device device = mSensorAdapter.getDevice(position);

					Bundle bundle = new Bundle();
					bundle.putString(SensorDetailActivity.EXTRA_ADAPTER_ID, device.getFacility().getAdapterId());
					bundle.putString(SensorDetailActivity.EXTRA_DEVICE_ID, device.getId());
					Intent intent = new Intent(mActivity, SensorDetailActivity.class);
					intent.putExtras(bundle);
					startActivity(intent);
				}
			});
			Adapter tmpAda = mController.getAdapter(mActiveAdapterId);
			if(tmpAda != null) {
				if(mController.isUserAllowed(tmpAda.getRole())) {
					mSensorList.setOnItemLongClickListener(new OnItemLongClickListener() {
						@Override
						public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
							mMode =  ((ActionBarActivity) getActivity()).startSupportActionMode(new ActionModeEditSensors());
                            mSelectedItem = mSensorAdapter.getDevice(position);
                            mSelectedItemPos = position;
                            setSensorSelected();
							return true;
						}
					});
				}
			}
		}

		mActivity.setSupportProgressBarIndeterminateVisibility(false);
		Log.d(TAG, "LifeCycle: getsensors end");
		return true;
	}

    private void setSensorSelected() {
        getViewByPosition(mSelectedItemPos,mSensorList).findViewById(R.id.layoutofsensor).setBackgroundColor(mActivity.getResources().getColor(R.color.light_gray));
    }

    private void setSensorUnselected() {
        getViewByPosition(mSelectedItemPos,mSensorList).findViewById(R.id.layoutofsensor).setBackgroundColor(mActivity.getResources().getColor(R.color.white));
    }

    public View getViewByPosition(int pos, ListView listView) {
        final int firstListItemPosition = listView.getFirstVisiblePosition();
        final int lastListItemPosition = firstListItemPosition + listView.getChildCount() - 1;

        if (pos < firstListItemPosition || pos > lastListItemPosition ) {
            return listView.getAdapter().getView(pos, null, listView);
        } else {
            final int childIndex = pos - firstListItemPosition;
            return listView.getChildAt(childIndex);
        }
    }

	protected void showAddAdapterDialog() {
		Log.d(TAG, "HERE ADD ADAPTER +");
		Intent intent = new Intent(mActivity, AddAdapterActivity.class);
		mActivity.startActivityForResult(intent, Constants.ADD_ADAPTER_REQUEST_CODE);
	}
	
	protected void showAddSensorDialog() {
		Log.d(TAG, "HERE ADD SENSOR +");
		Intent intent = new Intent(mActivity, AddSensorActivity.class);
		mActivity.startActivityForResult(intent, Constants.ADD_SENSOR_REQUEST_CODE);
	}

	public void setMenuID(String locID) {
		mActiveLocationId = locID;
	}

	public void setAdapterID(String adaID) {
		mActiveAdapterId = adaID;
	}

	public void setIsPaused(boolean value) {
		isPaused = value;
	}

    private void doReloadFacilitiesTask(String adapterId) {
        mReloadFacilitiesTask = new ReloadFacilitiesTask(getActivity().getApplicationContext(), true);

        mReloadFacilitiesTask.setListener(new CallbackTaskListener() {

            @Override
            public void onExecute(boolean success) {
                mActivity.redrawMainFragment();
                mActivity.redrawMenu();
                mSwipeLayout.setRefreshing(false);
            }
        });

        mReloadFacilitiesTask.execute(adapterId);
    }

    private void doRemoveFacilityTask(Facility facility) {
        mRemoveFacilityTask = new RemoveFacilityTask(getActivity().getApplicationContext(),true);
        DelFacilityPair pair = new DelFacilityPair(facility.getId(), facility.getAdapterId());

        mRemoveFacilityTask.setListener(new CallbackTaskListener() {
            @Override
            public void onExecute(boolean success) {
                mActivity.redrawMainFragment();
                mActivity.redrawMenu();
                if(success) {
                    // Hlaska o uspechu
                }
                else {
                    // Hlaska o neuspechu
                }
            }
        });
        mRemoveFacilityTask.execute(pair);
    }

	class ActionModeEditSensors implements ActionMode.Callback {

		@Override
		public boolean onCreateActionMode(ActionMode mode, Menu menu) {
			MenuInflater inflater = mode.getMenuInflater();
			inflater.inflate(R.menu.sensorlist_actionmode, menu);
			return true;
		}

		@Override
		public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
			if (item.getItemId() == R.id.sensor_menu_del) {
				doRemoveFacilityTask(mSelectedItem.getFacility());
			}

			mode.finish();
			return true;
		}

		@Override
		public void onDestroyActionMode(ActionMode mode) {
            setSensorUnselected();
            mSelectedItem = null;
            mSelectedItemPos = 0;
			mMode = null;

		}
	}

}