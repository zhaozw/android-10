package cz.vutbr.fit.iha.arrayadapter;

import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.adapter.device.Device;
import cz.vutbr.fit.iha.adapter.device.Facility;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.household.User;
import cz.vutbr.fit.iha.util.TimeHelper;
import cz.vutbr.fit.iha.util.UnitsHelper;

public class UsersListAdapter extends BaseAdapter {

	private static final int MARGIN_LEFT_RIGHT = 2;
	private static final int MARGIN_TOP = 10;
	private static final int MARGIN_BOTTOM = 0;
	private static final int MARGIN_TOP_M_L = -2;

	// Declare Variables
	private Context mContext;
	private LayoutInflater inflater;
	private boolean mShowAdd;
	private OnClickListener mListener;
	private List<User> mUsers;

	private final Controller mController;

	public UsersListAdapter(Context context, List<User> users, OnClickListener listener) {
		mContext = context;
		mController = Controller.getInstance(context.getApplicationContext());
		mUsers = users;
		//mShowAdd = !devices.isEmpty();
		mListener = listener;
	}

	@Override
	public int getCount() {
		return mUsers.size();// + (mShowAdd ? 1 : 0);
	}

	@Override
	public Object getItem(int position) {
		return mUsers.get(position).getName();
	}

	@Override
	public long getItemId(int position) {
		return position; // TODO: what's this?
	}

	public User getUser(int position) {
		return mUsers.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		//if (position < mUsers.size()) {
		return addItem(position, convertView, parent);
		//}
		//return addAddSensor(convertView, parent);
	}


	private View addItem(int position, View convertView, ViewGroup parent) {
		inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View itemView = inflater.inflate(R.layout.user_listview_item, parent, false);

		// Locate the TextViews in drawer_list_item.xml
		TextView txtNameUser = (TextView) itemView.findViewById(R.id.adapter_user_name);
		TextView txtEmailUser = (TextView) itemView.findViewById(R.id.adapter_user_email);

		// Locate the ImageView in drawer_list_item.xml
		ImageView imgIcon = (ImageView) itemView.findViewById(R.id.iconofsensor);

		User user = mUsers.get(position);
		
		txtNameUser.setText(user.getName());
		txtEmailUser.setText(user.getEmail());


		return itemView;
	}

}
