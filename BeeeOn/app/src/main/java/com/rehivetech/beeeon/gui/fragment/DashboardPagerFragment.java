package com.rehivetech.beeeon.gui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gcm.analytics.GoogleAnalyticsManager;
import com.rehivetech.beeeon.gui.activity.AddDashboardItemActivity;
import com.rehivetech.beeeon.gui.activity.BaseApplicationActivity;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.BaseItem;
import com.rehivetech.beeeon.gui.adapter.dashboard.items.VentilationItem;
import com.rehivetech.beeeon.gui.dialog.ConfirmDialog;
import com.rehivetech.beeeon.gui.view.FloatingActionMenu;
import com.rehivetech.beeeon.threading.CallbackTask;
import com.rehivetech.beeeon.threading.task.ReloadDashboardDataTask;
import com.rehivetech.beeeon.threading.task.ReloadGateDataTask;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author martin
 * @since 23.4.16
 */
public class DashboardPagerFragment extends BaseApplicationFragment implements ConfirmDialog.ConfirmDialogListener {

	public static final int RESULT_CODE_ADD_ITEM = 10;

	private static final String KEY_GATE_ID = "gate_id";

	public static final String EXTRA_ADD_ITEM = "add_item";
	public static final String EXTRA_INDEX = "index";

	private String mGateId;

	@BindView(R.id.dashboard_pager_root_layout)
	CoordinatorLayout mRootLayout;
	@BindView(R.id.dashboard_tab_layout)
	TabLayout mTabLayout;
	@BindView(R.id.dashboard_viewpager)
	ViewPager mViewPager;
	@BindView(R.id.dashboard_fab_menu)
	FloatingActionMenu mFloatingActionMenu;

	DashboardPagerAdapter mViewsAdapter;

	public static DashboardPagerFragment newInstance(String gateId) {
		Bundle args = new Bundle();
		args.putString(KEY_GATE_ID, gateId);

		DashboardPagerFragment fragment = new DashboardPagerFragment();
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();
		if (args != null) {
			mGateId = args.getString(KEY_GATE_ID);
		}
		setHasOptionsMenu(true);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_pager_dashboard, container, false);
		mUnbinder = ButterKnife.bind(this, view);
		mViewsAdapter = new DashboardPagerAdapter(getChildFragmentManager());
		setupViewpager();
		return view;
	}

	/**
	 * Setups toolbar + refresh icon
	 *
	 * @param savedInstanceState fragment's state
	 */
	@Override
	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Toolbar toolbar = mActivity.setupToolbar(R.string.nav_drawer_menu_menu_household, BaseApplicationActivity.INDICATOR_MENU);
		AppBarLayout.LayoutParams layoutParams = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
		layoutParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
		toolbar.setLayoutParams(layoutParams);
		mActivity.setupRefreshIcon(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				doReloadDevicesTask(true);
			}
		});
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_dashboard, menu);
	}

	/**
	 * Handles deleting view
	 *
	 * @param item selected
	 * @return if consumed here
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.dashboard_delete_tab:
				ConfirmDialog.confirm(this, mActivity.getString(R.string.dashboard_delete_view_x, mViewPager.getCurrentItem() + 1), mActivity.getString(R.string.dashboard_delete_view_message),
						R.string.activity_fragment_menu_btn_remove, ConfirmDialog.TYPE_DELETE_DASHBOARD_VIEW, "");

				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onResume() {
		super.onResume();
		GoogleAnalyticsManager.getInstance().logScreen(GoogleAnalyticsManager.DASHBOARD_SCREEN);
	}

	@Override
	public void onStart() {
		super.onStart();
		doReloadDevicesTask(false);
	}

	/**
	 * Handles result of adding item
	 * TODO should be handled with requestCode and Activity.RESULT_OK !!!!!!!
	 *
	 * @param requestCode
	 * @param resultCode
	 * @param data
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_CODE_ADD_ITEM) {
			BaseItem item = data.getParcelableExtra(EXTRA_ADD_ITEM);
			int index = data.getIntExtra(EXTRA_INDEX, 0);

			DashboardFragment fragment = (DashboardFragment) mViewsAdapter.getItem(index);

			if (fragment != null) {
				mViewPager.setCurrentItem(index); // TODO not working
				fragment.addItem(item);
			}
		}
	}

	/**
	 * Async task for refreshing data
	 *
	 * @param forceReload forcing reload
	 */
	private void doReloadDevicesTask(boolean forceReload) {
		Log.d("AHOJ", "reload");
		Controller controller = Controller.getInstance(mActivity);

		VentilationItem ventilationItem = null;
		int numOfItems = controller.getNumberOfDashboardTabs(mGateId) + 1;
		for (int i = 0; i < numOfItems; i++) {
			List<BaseItem> items = controller.getDashboardItems(i, mGateId);
			if (items == null) continue;

			for (BaseItem item : items) {
				if (item instanceof VentilationItem) {
					ventilationItem = (VentilationItem) item;
					break;
				}
			}
		}

		CallbackTask reloadDeviceTask = createReloadDevicesTask(forceReload);
		// if outside temperature from provider, reload weather
		if (ventilationItem != null && ventilationItem.getOutsideAbsoluteModuleId() == null) {
			mActivity.callbackTaskManager.executeTask(reloadDeviceTask, mGateId, ventilationItem.getLatitude(), ventilationItem.getLongitude());
		} else {
			mActivity.callbackTaskManager.executeTask(reloadDeviceTask, mGateId);
		}
	}

	/**
	 * Task for reloading devices in
	 *
	 * @param forceReload if should reload
	 * @return task
	 */
	private CallbackTask createReloadDevicesTask(final boolean forceReload) {
		ReloadDashboardDataTask reloadDashboardDataTask = new ReloadDashboardDataTask(
				mActivity,
				forceReload,
				ReloadGateDataTask.ReloadWhat.DEVICES
		);

		reloadDashboardDataTask.setListener(new CallbackTask.ICallbackTaskListener() {
			@Override
			public void onExecute(boolean success) {
				if (!success || !forceReload)
					return;

				updateViewPager();
			}
		});
		return reloadDashboardDataTask;
	}

	/**
	 * Prepares viewpager with different dashboard views
	 */
	private void setupViewpager() {
		Controller controller = Controller.getInstance(mActivity);
		int numOfItems = controller.getNumberOfDashboardTabs(mGateId);

		numOfItems = numOfItems == 0 ? 1 : numOfItems;

		for (int i = 1; i <= numOfItems; i++) {
			mViewsAdapter.addFragment(DashboardFragment.newInstance(i - 1, mGateId), mActivity.getString(R.string.dashboard_view, i));

		}
		mViewPager.setAdapter(mViewsAdapter);
		mTabLayout.setupWithViewPager(mViewPager);
	}

	/**
	 * Update views with fresh data
	 */
	private void updateViewPager() {
		for (int i = 0; i < mViewsAdapter.getCount(); i++) {
			DashboardFragment fragment = (DashboardFragment) mViewsAdapter.getItem(i);
			fragment.updateDashboard();
		}
	}

	/**
	 * Clicking on add card button (from FAmenu)
	 */
	@OnClick(R.id.dashboard_add_item_fab)
	public void onFloatingActionButtonClicked() {
		Intent intent = AddDashboardItemActivity.getAddDashBoardActivityIntent(mActivity, mViewPager.getCurrentItem(), mGateId);
		startActivityForResult(intent, 0);
	}

	/**
	 * Clicking on add view button (from FAmenu)
	 */
	@OnClick(R.id.dashboard_add_view_fab)
	public void onAddViewFloatingActionButtonClicked() {
		DashboardFragment fragment = DashboardFragment.newInstance(mViewsAdapter.getCount(), mGateId);
		mViewsAdapter.addFragment(fragment, mActivity.getString(R.string.dashboard_view, mViewsAdapter.getCount() + 1));
		mViewsAdapter.notifyDataSetChanged();

		mFloatingActionMenu.close(true);
		mViewPager.setCurrentItem(mViewsAdapter.getCount() - 1, true);

		Controller controller = Controller.getInstance(mActivity);
		controller.saveNumberOfDashboardTabs(mGateId, mViewsAdapter.getCount());
	}

	/**
	 * Shows snackbar with undo action
	 *
	 * @param text          snackbar's text
	 * @param clickListener undo click listener
	 */
	public void showSnackbar(String text, View.OnClickListener clickListener) {
		Snackbar.make(mRootLayout, text, Snackbar.LENGTH_LONG).setAction(R.string.dashboard_undo, clickListener).show();
	}

	/**
	 * Confirming "confirm" dialog
	 *
	 * @param confirmType who sent request (submitted)
	 * @param dataId      data sent through dialog
	 */
	@Override
	public void onConfirm(int confirmType, String dataId) {

		if (confirmType == ConfirmDialog.TYPE_DELETE_DASHBOARD_VIEW) {
			Controller controller = Controller.getInstance(mActivity);

			int index = mViewPager.getCurrentItem();

			controller.removeDashboardView(index, mGateId);
			mViewsAdapter.removeFragment(index);
			controller.saveNumberOfDashboardTabs(mGateId, mViewsAdapter.getCount());
			// TODO not showing correctly
			if (mViewsAdapter.getCount() == 0) {
				setupViewpager();
			}

			Snackbar.make(mRootLayout, R.string.activity_fragment_toast_delete_success, Snackbar.LENGTH_SHORT).show();
		}
	}


	/**
	 * Viewpager for different dashboard views
	 */
	static class DashboardPagerAdapter extends FragmentPagerAdapter {
		private final List<Fragment> mFragments = new ArrayList<>();
		private final List<String> mFragmentTitles = new ArrayList<>();
		private final FragmentManager mFragmentManager;

		public DashboardPagerAdapter(FragmentManager fm) {
			super(fm);
			mFragmentManager = fm;
		}

		@Override
		public Fragment getItem(int position) {
			return mFragments.get(position);
		}

		@Override
		public int getCount() {
			return mFragments.size();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return mFragmentTitles.get(position);
		}

		public Fragment getActiveFragment(ViewPager container, int position) {
			String name = makeFragmentName(container.getId(), position);
			return mFragmentManager.findFragmentByTag(name);
		}

		private static String makeFragmentName(int viewId, int index) {
			return "android:switcher:" + viewId + ":" + index;
		}

		public void addFragment(Fragment fragment, String title) {
			mFragments.add(fragment);
			mFragmentTitles.add(title);
		}

		/**
		 * Removes fragment on position
		 *
		 * @param index position to delete fragment on
		 */
		public void removeFragment(int index) {
			mFragments.remove(index);
			mFragmentTitles.remove(index);
			notifyDataSetChanged();
		}

		public void removeAll() {
			mFragments.clear();
			mFragmentTitles.clear();
			notifyDataSetChanged();
		}
	}


}
