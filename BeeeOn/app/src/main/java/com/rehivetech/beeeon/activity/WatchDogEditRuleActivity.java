package com.rehivetech.beeeon.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.melnykov.fab.FloatingActionButton;
import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.IIdentifier;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.menuItem.AdapterMenuItem;
import com.rehivetech.beeeon.activity.menuItem.ApplicationMenuItem;
import com.rehivetech.beeeon.activity.menuItem.GroupMenuItem;
import com.rehivetech.beeeon.activity.menuItem.LocationMenuItem;
import com.rehivetech.beeeon.activity.menuItem.SeparatorMenuItem;
import com.rehivetech.beeeon.activity.menuItem.SettingMenuItem;
import com.rehivetech.beeeon.adapter.Adapter;
import com.rehivetech.beeeon.adapter.WatchDog;
import com.rehivetech.beeeon.adapter.device.Device;
import com.rehivetech.beeeon.adapter.device.Facility;
import com.rehivetech.beeeon.adapter.location.Location;
import com.rehivetech.beeeon.arrayadapter.DeviceArrayAdapter;
import com.rehivetech.beeeon.asynctask.CallbackTask;
import com.rehivetech.beeeon.asynctask.ReloadFacilitiesTask;
import com.rehivetech.beeeon.asynctask.RemoveWatchDogTask;
import com.rehivetech.beeeon.asynctask.SaveWatchDogTask;
import com.rehivetech.beeeon.base.BaseApplicationActivity;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.pair.DelWatchDogPair;
import com.rehivetech.beeeon.util.Log;
import com.rehivetech.beeeon.util.UnitsHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Activity for creation and editation of watchdog rule
 * @author mlyko
 */
public class WatchDogEditRuleActivity extends BaseApplicationActivity {
    private static final String TAG = WatchDogEditRuleActivity.class.getSimpleName();

    public static final String EXTRA_ADAPTER_ID = "adapter_id";
    public static final String EXTRA_RULE_ID = "rule_id";
    public static final String EXTRA_IS_NEW ="rule_is_new";

    // helper variables for getting devices from list
    private static final int DEVICES_ALL = 0;
    private static final int DEVICES_ACTORS = 1;
    private static final int DEVICES_SENSORS = 2;

    // extras
    private String mActiveAdapterId;
    private String mActiveRuleId;

    private Controller mController;
    private Toolbar mToolbar;
    private Menu mOptionsMenu;
    private ProgressDialog mProgress;
    private Adapter mAdapter;
    private UnitsHelper mUnitsHelper;
    private SharedPreferences mPrefs;

    private List<Location> mLocations;
    private List<Facility> mFacilities;

    // TODO??
    private List<Device> _sensors;
    private List<Device> _actors;

    private boolean mIsValueLess = false;        // value for View FAB
    private boolean mIsNew = false;
    private WatchDog mWatchDog;

    // async tasks
    private ReloadFacilitiesTask mReloadFacilitiesTask;
    private SaveWatchDogTask mSaveWatchDogTask;
    private RemoveWatchDogTask mRemoveWatchDogTask;

    // GUI elements
    private RadioGroup mActionType;
    private EditText mRuleName;
    private TextView mRuleTresholdUnit;
    private SwitchCompat mRuleEnabled;
    private Spinner mSensorSpinner;
    private FloatingActionButton mGreatLessButton;
    private EditText mRuleTreshold;
    private EditText mNotificationText;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watchdog_edit_rule);
        Log.d(TAG, "onCreate()");

        // prepare toolbar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            mToolbar.setTitle(R.string.watchdog_rule);
            setSupportActionBar(mToolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
            // x instead of <- indicating that no changes will be saved upon click
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_action_cancel);
        }

        // Prepare progress dialog
        mProgress = new ProgressDialog(this);
        mProgress.setMessage(getString(R.string.progress_saving_data));
        mProgress.setCancelable(false);
        mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            mActiveAdapterId = bundle.getString(EXTRA_ADAPTER_ID);
            mActiveRuleId = bundle.getString(EXTRA_RULE_ID);
            mIsNew = false;
        }
        else{
            bundle = savedInstanceState;
            if (bundle != null) {
                mActiveAdapterId = bundle.getString(EXTRA_ADAPTER_ID);
                mActiveRuleId = bundle.getString(EXTRA_RULE_ID);
                mIsNew = bundle.getBoolean(EXTRA_IS_NEW);
            }
            else{
                mIsNew = true;
            }
        }

        // if we want detail of some rule, we need id and adapter
        if (!mIsNew && (mActiveAdapterId == null || mActiveRuleId == null)) {
            Toast.makeText(this, R.string.toast_wrong_or_no_watchdog_rule, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // get controller
        mController = Controller.getInstance(this);
        // get adapter
        mAdapter = mActiveAdapterId == null ? mController.getActiveAdapter() : mController.getAdapter(mActiveAdapterId);

        // UserSettings can be null when user is not logged in!
        mPrefs = mController.getUserSettings();
        mUnitsHelper = (mPrefs == null) ? null : new UnitsHelper(mPrefs, this);

        // get all locations for spinners
        mLocations = mController.getLocations(mAdapter.getId());

        // facilities get by cycling through all locations
        mFacilities = new ArrayList<Facility>();
        for(Location loc : mLocations){
            List<Facility> tempFac = mController.getFacilitiesByLocation(mAdapter.getId(), loc.getId());
            mFacilities.addAll(tempFac);
        }

        // get watchdog rule
        if(!mIsNew) {
            // hide keyboard when editing
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

            mWatchDog = mController.getWatchDog(mAdapter.getId(), mActiveRuleId);
            if(mWatchDog == null){
                Toast.makeText(this, R.string.toast_something_wrong, Toast.LENGTH_LONG).show();
                finish();
                return;
            }
        }
        else{
            mWatchDog = new WatchDog();
        }

        initLayout();
        setValues();
    }

    /**
     * Initializes GUI elements to work
     */
    private void initLayout() {
        // init gui elements
        mRuleName = (EditText) findViewById(R.id.watchdog_edit_name);
        mRuleEnabled = (SwitchCompat) findViewById(R.id.watchdog_edit_switch);
        mSensorSpinner = (Spinner) findViewById(R.id.watchdog_edit_sensor_spinner);
        mGreatLessButton = (FloatingActionButton) findViewById(R.id.watchdog_edit_greatless);
        mRuleTreshold = (EditText) findViewById(R.id.watchdog_edit_treshold);
        mRuleTresholdUnit = (TextView) findViewById(R.id.watchdog_edit_treshold_unit);
        mActionType = (RadioGroup) findViewById(R.id.watchdog_edit_action_radiogroup);

        // ----- prepare list of available devices

        final DeviceArrayAdapter dataAdapter = new DeviceArrayAdapter(this, R.layout.custom_spinner2_item, getDevicesArray(DEVICES_SENSORS), mLocations);
        dataAdapter.setLayoutInflater(getLayoutInflater());
        dataAdapter.setDropDownViewResource(R.layout.custom_spinner2_dropdown_item);
        mSensorSpinner.setAdapter(dataAdapter);

        mSensorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Device selectedDevice = dataAdapter.getItem(position);
                mRuleTresholdUnit.setText(mUnitsHelper.getStringUnit(selectedDevice.getValue()));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //*/

    /*
        final MenuListAdapter mMenuAdapter = new MenuListAdapter(this);

        mMenuAdapter.addItem(new AdapterMenuItem(mAdapter.getName(), mAdapter.getRole().name(), true, mAdapter.getId()));

        mMenuAdapter.addItem(new SettingMenuItem(getResources().getString(R.string.action_about), R.drawable.info, com.rehivetech.beeeon.activity.menuItem.MenuItem.ID_ABOUT));
        mMenuAdapter.addItem(new SettingMenuItem(getString(R.string.action_logout), R.drawable.logout, com.rehivetech.beeeon.activity.menuItem.MenuItem.ID_LOGOUT));
        mMenuAdapter.addItem(new SeparatorMenuItem());
        mMenuAdapter.addHeader(new GroupMenuItem(getResources().getString(R.string.menu_applications)));
        mMenuAdapter.addItem(new ApplicationMenuItem(getString(R.string.menu_watchdog), R.drawable.ic_app_watchdog, false, Constants.GUI_MENU_WATCHDOG, true));

        mSensorSpinner.setAdapter(mMenuAdapter);


        mSensorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                com.rehivetech.beeeon.activity.menuItem.MenuItem selected = (com.rehivetech.beeeon.activity.menuItem.MenuItem) mMenuAdapter.getItem(position);

                switch(selected.getType()){
                    case ADAPTER:
                        Toast.makeText(WatchDogEditRuleActivity.this, "vybral sis adapter", Toast.LENGTH_SHORT).show();
                        break;

                    case SETTING:
                        Toast.makeText(WatchDogEditRuleActivity.this, "SETTING", Toast.LENGTH_SHORT).show();
                        break;

                    case APPLICATION:
                        Toast.makeText(WatchDogEditRuleActivity.this, "APPLICATION", Toast.LENGTH_SHORT).show();
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //*/

        // TODO make value not boolean but OperatorType
        mGreatLessButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImageButton img = (ImageButton) v;
                img.setImageResource(mIsValueLess ? R.drawable.ic_action_next_item : R.drawable.ic_action_previous_item);
                mIsValueLess = !mIsValueLess;
            }
        });

        // TODO udelat nejak,ze ziskat pres parametry + asi include layout pro to
        mNotificationText = (EditText) findViewById(R.id.watchdog_edit_notification_text);

        // changing specified layout when checked
        mActionType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RelativeLayout NotifLayout = (RelativeLayout) findViewById(R.id.watchdog_detail_notification);
                RelativeLayout ActionLayout = (RelativeLayout) findViewById(R.id.watchdog_edit_actor_layout);

                switch (checkedId) {
                    case R.id.watchdog_edit_notification:
                        NotifLayout.setVisibility(View.VISIBLE);
                        ActionLayout.setVisibility(View.GONE);
                        break;

                    case R.id.watchdog_edit_actor:
                        NotifLayout.setVisibility(View.GONE);
                        ActionLayout.setVisibility(View.VISIBLE);
                        break;
                }
            }
        });

        // first checked
        RadioButton radioNotification = (RadioButton) findViewById(R.id.watchdog_edit_notification);
        radioNotification.setChecked(true);

        // ------- choose actor
        Spinner actorSpinner = (Spinner) findViewById(R.id.watchdog_edit_actor_spinner);

        final DeviceArrayAdapter actorAdapter = new DeviceArrayAdapter(this, R.layout.custom_spinner2_item, getDevicesArray(DEVICES_ACTORS), mLocations);
        actorAdapter.setLayoutInflater(getLayoutInflater());
        actorAdapter.setDropDownViewResource(R.layout.custom_spinner2_dropdown_item);
        actorSpinner.setAdapter(actorAdapter);
    }

    /**
     * Fills gui elements with values
     */
    private void setValues(){
        // if this is new watchdog, we don't set anything
        if(mWatchDog.getId() == null) return;

        mRuleName.setText(mWatchDog.getName());
        mRuleEnabled.setChecked(mWatchDog.isEnabled());
        mRuleTreshold.setText(mWatchDog.getParams().get(WatchDog.PAR_TRESHOLD));

        // TODO revise when new protocol version
        Device firstDev = mController.getDevice(mWatchDog.getAdapterId(), mWatchDog.getDevices().get(0));
        int index = getIndexFromList(firstDev.getId(), getDevicesArray(DEVICES_SENSORS));
        if(index > -1) mSensorSpinner.setSelection(index);

        switch(mWatchDog.getType()){
            case WatchDog.ACTION_NOTIFICATION:
                mActionType.check(R.id.watchdog_edit_notification);
                mNotificationText.setText(mWatchDog.getParams().get(WatchDog.PAR_ACTION_VALUE));
                break;

            case WatchDog.ACTION_ACTOR:
                mActionType.check(R.id.watchdog_edit_actor);
                // TODO - vybrat actor
                break;
        }

        setGreatLess(mWatchDog.getParams().get(WatchDog.PAR_OPERATOR), mGreatLessButton);
    }

    private void setGreatLess(String operator, FloatingActionButton glButton) {
        switch(operator){
            case WatchDog.OPERATOR_GT:
                mIsValueLess = false;
                glButton.setImageResource(R.drawable.ic_action_next_item);
                break;
            case WatchDog.OPERATOR_LT:
                mIsValueLess = true;
                glButton.setImageResource(R.drawable.ic_action_previous_item);
                break;
        }
    }

    @Override
    protected void onAppResume() {

    }

    @Override
    protected void onAppPause() {

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(EXTRA_ADAPTER_ID, mActiveAdapterId);
        savedInstanceState.putString(EXTRA_RULE_ID, mActiveRuleId);
        savedInstanceState.putBoolean(EXTRA_IS_NEW, mIsNew);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Hides delete button when we have new watchdog
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.watchdog_edit_menu, menu);
        mOptionsMenu = menu;

        onUpdateOptionsMenu();
        return true;
    }

    private void onUpdateOptionsMenu(){
        if(mOptionsMenu == null) return;

        MenuItem deleteActionButton = mOptionsMenu.findItem(R.id.wat_menu_delete);
        deleteActionButton.setVisible(mIsNew ? false : true);
    }

    /**
     * Clicked on actionbar button (delete / save)
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.wat_menu_save:
                doSaveWatchDogTask();
                break;
            case R.id.wat_menu_delete:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder
                    .setMessage(R.string.rule_delete_dialog)
                    .setPositiveButton(R.string.rule_menu_del, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            doRemoveWatchDogTask();
                        }
                    })
                    .setNegativeButton(R.string.notification_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // ---------- ASYNC TASKS ---------- //

    /**
     * Async task for saving watchdog
     * TODO revise this
     */
    private void doSaveWatchDogTask(){
        if(!validateInput(mRuleName, "This must be filled!") || !validateInput(mRuleTreshold, "This must be filled!")){
            return;
        }

        // TODO check it
        ArrayList<String> devsIds = new ArrayList<>();

        Device selectedDevice = getDevicesArray(DEVICES_SENSORS).get(mSensorSpinner.getSelectedItemPosition());
        devsIds.add(selectedDevice.getId());
        mWatchDog.setDevices(devsIds);

        mWatchDog.setName(mRuleName.getText().toString());
        mWatchDog.setEnabled(mRuleEnabled.isChecked());

        String par_dev_id = selectedDevice.getId();
        String par_operator = mIsValueLess ? "lt" : "gt";
        String par_treshold = mRuleTreshold.getText().toString();
        String par_notif_text = mNotificationText.getText().toString();

        ArrayList<String> newParams = new ArrayList<String>();
            newParams.add(par_dev_id);
            newParams.add(par_operator);
            newParams.add(par_treshold);
            newParams.add(par_notif_text);
        mWatchDog.setParams(newParams);

        mProgress.show();

        // TODO check if data were changed ???
        mSaveWatchDogTask = new SaveWatchDogTask(this);
        mSaveWatchDogTask.setListener(new CallbackTask.CallbackTaskListener() {
            @Override
            public void onExecute(boolean success) {
                if (mProgress != null) mProgress.dismiss();
                if(success){
                    mIsNew = false;
                    onUpdateOptionsMenu();
                }
                Toast.makeText(WatchDogEditRuleActivity.this, getResources().getString(success ? R.string.toast_success_save_data : R.string.toast_fail_save_data), Toast.LENGTH_LONG).show();
            }
        });

        mSaveWatchDogTask.execute(mWatchDog);
    }

    /**
     * Async task for deleting watchDog
     */
    private void doRemoveWatchDogTask() {
        mRemoveWatchDogTask = new RemoveWatchDogTask(this, false);
        DelWatchDogPair pair = new DelWatchDogPair(mWatchDog.getId(), mWatchDog.getAdapterId());

        mRemoveWatchDogTask.setListener(new CallbackTask.CallbackTaskListener() {
            @Override
            public void onExecute(boolean success) {
                Toast.makeText(WatchDogEditRuleActivity.this, getResources().getString(success ? R.string.toast_delete_success : R.string.toast_delete_fail), Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        mRemoveWatchDogTask.execute(pair);
    }

    // ---------- HELPER FUNCTIONS ---------- //
    /**
     * Helper function from getting position of object from list of objects
     * @param id
     * @param list
     * @param <T>
     * @return
     */
    private <T extends IIdentifier> int getIndexFromList(String id, List<T> list) {
        int index = 0;
        for (T tempObj : list) {
            if (tempObj.getId().equalsIgnoreCase(id)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    /**
     * Helper function for getting type of devices from list of facilities
     * @param type DEVICES_ALL | DEVICES_ACTORS | DEVICES_SENSORS
     * @return
     */
    private List<Device> getDevicesArray(int type){
        if(type == DEVICES_ACTORS && _actors != null) {
            return _actors;
        }
        else if(type == DEVICES_SENSORS && _sensors != null) {
            return _sensors;
        }

        List<Device> devices = new ArrayList<Device>();

        for(Facility facility : mFacilities)
            for (Device device : facility.getDevices()) {
                if (type == DEVICES_ACTORS && !device.getType().isActor()) {
                    continue;
                }
                else if(type == DEVICES_SENSORS && device.getType().isActor()) {
                    continue;
                }

                devices.add(device);
            }

        if(type == DEVICES_ACTORS) {
            _actors = devices;
        }
        else if(type == DEVICES_SENSORS) {
            _sensors = devices;
        }

        return devices;
    }

    /**
     * ¨Helper function for validating EditText
     * @param eText
     * @param msg
     * @return
     */
    private static boolean validateInput(EditText eText, String msg){
        if(eText.getText().toString().trim().length() == 0){
            eText.setError(msg);
            return false;
        }
        return true;
    }
}

