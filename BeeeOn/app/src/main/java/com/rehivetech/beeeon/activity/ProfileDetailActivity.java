package com.rehivetech.beeeon.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.melnykov.fab.FloatingActionButton;
import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.dialog.PairNetworkFragmentDialog;
import com.rehivetech.beeeon.arrayadapter.GamCategoryListAdapter;
import com.rehivetech.beeeon.base.BaseApplicationActivity;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gamification.AchievementList;
import com.rehivetech.beeeon.gamification.GamificationCategory;
import com.rehivetech.beeeon.household.user.User;
import com.rehivetech.beeeon.socialNetworks.BeeeOnFacebook;
import com.rehivetech.beeeon.socialNetworks.BeeeOnSocialNetwork;
import com.rehivetech.beeeon.socialNetworks.BeeeOnTwitter;
import com.rehivetech.beeeon.socialNetworks.BeeeOnVKontakte;
import com.rehivetech.beeeon.util.Log;
import com.twitter.sdk.android.core.identity.TwitterAuthClient;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKSdk;
import com.vk.sdk.VKUIHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

/**
 * @author Jan Lamacz
 */
public class ProfileDetailActivity extends BaseApplicationActivity implements Observer {
	private static final String TAG = ProfileDetailActivity.class.getSimpleName();

	private User actUser;
	private GamCategoryListAdapter mCategoryListAdapter;
	private Context mContext;
	private Activity mActivity;
	private int mDisplayPixel;

	// GUI
	private TextView userName;
	private TextView userLevel;
	private ImageView userImage;
	private ListView mCategoryList;
	private TextView mPoints;
	private FloatingActionButton mMoreArrow;
	private FloatingActionButton mMoreAdd;
	private RelativeLayout mMoreVisible;
	private RelativeLayout mMoreLayout;

	// SocialNetworks
	public CallbackManager mFacebookCallbackManager;
	private TwitterAuthClient mTwitterCallbackManager;
	private boolean showMoreAccounts = false;
	private final int totalNetworks = 3;
	private int unconnectedNetworks = 0;
	private BeeeOnFacebook mFb;
	private BeeeOnTwitter mTw;
	private BeeeOnVKontakte mVk;
	private TextView mFbName;
	private TextView mTwName;
	private TextView mVkName;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile_detail);

		mContext = this;
		mActivity = this;
		Controller controller = Controller.getInstance(mContext);
		actUser = controller.getActualUser();

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		if (toolbar != null) {
			toolbar.setTitle(R.string.title_activity_profile_detail);
			setSupportActionBar(toolbar);
			getSupportActionBar().setHomeButtonEnabled(true);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}

		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		mDisplayPixel = (int) metrics.density;

		// Inflate the layout for this fragment
		userName = (TextView) findViewById(R.id.profile_name);
		userLevel = (TextView) findViewById(R.id.profile_detail);
		userImage = (ImageView) findViewById(R.id.profile_image);
		mCategoryList = (ListView) findViewById(R.id.gam_category_list);
		mPoints = (TextView) findViewById(R.id.profile_points);
		mMoreArrow = (FloatingActionButton) findViewById(R.id.profile_more_arrow);
		mMoreAdd = (FloatingActionButton) findViewById(R.id.profile_more_add);
		mMoreVisible = (RelativeLayout) findViewById(R.id.profile_more_accounts);
		mMoreLayout = (RelativeLayout) findViewById(R.id.profile_more);

		mFb = BeeeOnFacebook.getInstance(mContext);
		mTw = BeeeOnTwitter.getInstance(mContext);
		mVk = BeeeOnVKontakte.getInstance(mContext);

		// Facebook sdk needs to be initialised in Activity, but its used in Profile Fragment
		// Registering callback for facebook log in
		FacebookSdk.sdkInitialize(this);
		mFacebookCallbackManager = CallbackManager.Factory.create();
		LoginManager.getInstance().registerCallback(
				mFacebookCallbackManager,
				BeeeOnFacebook.getInstance(getApplicationContext()).getListener());
		VKSdk.initialize(
				BeeeOnVKontakte.getInstance(getApplicationContext()).getListener(),
				getString(R.string.vkontakte_app_id),
				VKAccessToken.tokenFromSharedPreferences(getApplicationContext(), Constants.PERSISTENCE_PREF_LOGIN_VKONTAKTE)
		);
		mTwitterCallbackManager = new TwitterAuthClient();

		setNetworksView();
		setMoreButtonVisibility();
		redrawCategories();
		setOnClickLogout(mFb, mFbName);

		Bitmap picture = actUser.getPicture();
		if (picture == null)
			picture = actUser.getDefaultPicture(this);
		userName.setText(actUser.getFullName());
		userImage.setImageBitmap(picture);

		//GUI components for social networks accounts
		if(unconnectedNetworks > 0) {// more known networks to by added
			mMoreAdd.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					new PairNetworkFragmentDialog().show(getSupportFragmentManager(), TAG);
				}
			});
			mMoreAdd.setVisibility(View.VISIBLE);
		}
		else
			mMoreAdd.setVisibility(View.INVISIBLE);
		if(unconnectedNetworks != totalNetworks) { //at least one network is added
			mMoreArrow.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					showMoreAccounts = !showMoreAccounts;
					setMoreButtonVisibility();
				}
			});
			mMoreArrow.setVisibility(View.VISIBLE);
		}
		else
			mMoreArrow.setVisibility(View.INVISIBLE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		VKUIHelper.onActivityResult(requestCode, resultCode, data);
		mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
		mTwitterCallbackManager.onActivityResult(requestCode, resultCode, data);
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			default: return super.onOptionsItemSelected(item);
		}
	}

	private void setMoreButtonVisibility() {
		if(unconnectedNetworks == totalNetworks) { //none social network is paired
			mMoreArrow.setVisibility(View.INVISIBLE);
			mMoreVisible.setVisibility(View.INVISIBLE);
		}
		else {	//at least one network is connected, allow to show the profile
			if (showMoreAccounts) { // SHOW info
				// DOWNLOAD data
				if(mFb.isPaired()) mFb.downloadUserData();
				if(mTw.isPaired()) mTw.downloadUserData();
				if(mVk.isPaired()) mVk.downloadUserData();
				mMoreLayout.getLayoutParams().height = (mDisplayPixel*60)+((totalNetworks-unconnectedNetworks)*(mDisplayPixel*65));
				mMoreLayout.requestLayout();
				mMoreVisible.setVisibility(View.VISIBLE);
				rotate(90);
			} else { // HIDE info
				mMoreLayout.getLayoutParams().height = 60*mDisplayPixel;
				mMoreLayout.requestLayout();
				mMoreVisible.setVisibility(View.INVISIBLE);
				rotate(0);
			}
		}
	}

	private void setNetworksView() {
		RelativeLayout fbLayout = (RelativeLayout) findViewById(R.id.profile_facebook);
		RelativeLayout twLayout = (RelativeLayout) findViewById(R.id.profile_twitter);
		RelativeLayout vkLayout = (RelativeLayout) findViewById(R.id.profile_vkontakte);
		mFbName = (TextView) findViewById(R.id.profile_facebook_name);
		mTwName = (TextView) findViewById(R.id.profile_twitter_name);
		mVkName = (TextView) findViewById(R.id.profile_vkontakte_name);
		ViewGroup.LayoutParams fbPar = fbLayout.getLayoutParams();
		ViewGroup.LayoutParams twPar = twLayout.getLayoutParams();
		ViewGroup.LayoutParams vkPar = vkLayout.getLayoutParams();

		mFb.addObserver(this);
		mTw.addObserver(this);
		mVk.addObserver(this);
		if(!mFb.isPaired()) {
			fbLayout.setVisibility(View.GONE);
			fbPar.height = 0;
			unconnectedNetworks++;
		}
		else {
			fbLayout.setVisibility(View.VISIBLE);
			fbPar.height = 60*mDisplayPixel;
			setOnClickLogout(mFb, mFbName);
		}
		if(!mTw.isPaired()) {
			twLayout.setVisibility(View.GONE);
			twPar.height = 0;
			unconnectedNetworks++;
		}
		else {
			twLayout.setVisibility(View.VISIBLE);
			twPar.height = 60*mDisplayPixel;
			if(mTw.getUserName() != null ) setOnClickLogout(mTw, mTwName);
			else setOnClickLogin(mTw, mTwName);
		}
		if(!mVk.isPaired()) {
			vkLayout.setVisibility(View.GONE);
			vkPar.height = 0;
			unconnectedNetworks++;
		}
		else {
			vkLayout.setVisibility(View.VISIBLE);
			vkPar.height = 60*mDisplayPixel;
			setOnClickLogout(mVk, mVkName);
		}
	}

	/**
	 * Rotates arrow that shows and hides additional info
	 * about connected social networks.
	 */
	private void rotate(float end) {
		final RotateAnimation rotateAnim = new RotateAnimation(0.0f, end,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f,
				RotateAnimation.RELATIVE_TO_SELF, 0.5f);

		rotateAnim.setDuration(100);
		rotateAnim.setFillAfter(true);
		mMoreArrow.startAnimation(rotateAnim);
	}

	private void setOnClickLogout(final BeeeOnSocialNetwork network, final TextView textView) {
		if(!network.isPaired() || network.getUserName() == null) return;
		textView.setText(network.getUserName());
		textView.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				network.logOut();
				setOnClickLogin(network, textView);
				return true;
			}
		});
	}

	private void setOnClickLogin(final BeeeOnSocialNetwork network, TextView textView) {
		textView.setText(getResources().getString(R.string.login_login));
		textView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				network.logIn(mActivity);
			}
		});
	}

	private void redrawCategories() {
//		final AchievementList achievementList = AchievementList.getInstance(mContext);
//
//		userLevel.setText(getString(R.string.profile_level) + " " + achievementList.getLevel());
//		mPoints.setText(String.valueOf(achievementList.getTotalPoints()));
//
//		List<GamificationCategory> rulesList = new ArrayList<>();
//		rulesList.add(new GamificationCategory("0", getString(R.string.profile_category_app)));
//		rulesList.add(new GamificationCategory("1", getString(R.string.profile_category_friends)));
//		rulesList.add(new GamificationCategory("2", getString(R.string.profile_category_senzors)));
//
//		mCategoryListAdapter = new GamCategoryListAdapter(mContext, rulesList, getLayoutInflater(), achievementList);
//
//		mCategoryList.setAdapter(mCategoryListAdapter);
//		mCategoryList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//			@Override
//			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//				GamificationCategory category = mCategoryListAdapter.getItem(position);
//
//				Bundle bundle = new Bundle();
//				bundle.putString(AchievementOverviewActivity.EXTRA_CATEGORY_ID, category.getId());
//				bundle.putString(AchievementOverviewActivity.EXTRA_CATEGORY_NAME, category.getName());
//
//				Intent intent = new Intent(mContext, AchievementOverviewActivity.class);
//				intent.putExtras(bundle);
//				startActivity(intent);
//			}
//		});
	}

	@Override
	protected void onAppResume() {
		Log.d(TAG, "onAppResume");
		VKUIHelper.onResume(this);
	}

	@Override
	protected void onAppPause() {
		Log.d(TAG, "onAppPause");
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");
		VKUIHelper.onDestroy(this);
	}

	@Override
	public void update(Observable observable, Object o) {
		Log.d(TAG, "Newly downloaded data: "+o.toString());
		if(o.toString().equals("facebook"))
			setOnClickLogout(mFb, mFbName);
		else if(o.toString().equals("vkontakte"))
			setOnClickLogout(mVk, mVkName);
		else if(o.toString().equals("facebook login")) {
			setNetworksView();
			setMoreButtonVisibility();
		}
		else if(o.toString().equals("not_logged"))
			setOnClickLogin(mFb, mFbName);
		else if(o.toString().equals("connect_error")) {
			if(mFb.isPaired()) mFbName.setText(getResources().getString(R.string.social_no_connection));
			if(mTw.isPaired()) mTwName.setText(getResources().getString(R.string.social_no_connection));
			if(mVk.isPaired()) mVkName.setText(getResources().getString(R.string.social_no_connection));
		}
	}
}