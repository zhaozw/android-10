package com.rehivetech.beeeon.activity;

import java.util.ArrayList;
import java.util.List;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.LinearLayout.LayoutParams;
import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.adapter.Adapter;
import com.rehivetech.beeeon.adapter.device.Device;
import com.rehivetech.beeeon.adapter.device.Facility;
import com.rehivetech.beeeon.arrayadapter.UsersListAdapter;
import com.rehivetech.beeeon.asynctask.GetAdapterUsersTask;
import com.rehivetech.beeeon.asynctask.ReloadFacilitiesTask;
import com.rehivetech.beeeon.asynctask.CallbackTask.CallbackTaskListener;
import com.rehivetech.beeeon.base.BaseApplicationActivity;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.User;
import com.rehivetech.beeeon.util.Log;

public class AdapterUsersActivity extends BaseApplicationActivity {
	
	private Controller mController;
	
	private Activity mActivity;
	
	private Adapter mAdapter;
	
	private List<User> mAdapterUsers;
	
	private ListView mListActUsers;
	private ListView mListPenUsers;

	private GetAdapterUsersTask mGetAdapterUsersTask;
	
	private static final int NAME_ITEM_HEIGHT = 74;
    private Toolbar mToolbar;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_adapter_users);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        if (mToolbar != null) {
            mToolbar.setTitle(R.string.app_name);
            setSupportActionBar(mToolbar);
        }
		
		// Get controller
		mController = Controller.getInstance(this);
		// Get actual activity
		mActivity = this;
		
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		
		// Get selected adapter
		mAdapter = mController.getAdapter(getIntent().getStringExtra(Constants.GUI_SELECTED_ADAPTER_ID));
		
		// Get all users for adapter
		doGetAdapterUsers(mAdapter.getId(), true);
	}

	private void initLayouts() {
		// Get elements
		mListActUsers = (ListView) findViewById(R.id.adapter_users_list);
		//mListPenUsers = (ListView) findViewById(R.id.adapter_users_pending_list);
		
		mListActUsers.setAdapter(new UsersListAdapter(mActivity,mAdapterUsers,null));
		// Set listview height, for all 
		float scale = mActivity.getResources().getDisplayMetrics().density;
		mListActUsers.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, (int) (scale*NAME_ITEM_HEIGHT*mAdapterUsers.size())));
		
		Button mButton = (Button) findViewById(R.id.add_users_adapter);
		mButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// Go to add new user 
				Intent intent = new Intent(mActivity, AddAdapterUserActivity.class);
				intent.putExtra(Constants.GUI_SELECTED_ADAPTER_ID, mAdapter.getId());
				mActivity.startActivity(intent);
			}
		});
	}


	@Override
	protected void onAppResume() {
		if(mAdapter != null)
			doGetAdapterUsers(mAdapter.getId(), true);
	}

	@Override
	protected void onAppPause() {
		// TODO Auto-generated method stub

	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {
		case android.R.id.home:
			
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void doGetAdapterUsers(String adapterId ,boolean forceReload) {
		mGetAdapterUsersTask = new GetAdapterUsersTask(this, forceReload);

		mGetAdapterUsersTask.setListener(new CallbackTaskListener() {

			@Override
			public void onExecute(boolean success) {
				mAdapterUsers = mController.getUsers();
				
				initLayouts();
			}

		});

		mGetAdapterUsersTask.execute(adapterId);
	}

}