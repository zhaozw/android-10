package com.rehivetech.beeeon.arrayadapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.household.device.Facility;

public class SetupSensorListAdapter extends BaseAdapter {

	private Context mContext;
	private Facility mFacility;
	private EditText mName;

	private LayoutInflater mInflater;

	public SetupSensorListAdapter(Context context, Facility facility) {
		mContext = context;
		mFacility = facility;

	}

	@Override
	public int getCount() {
		return mFacility.getModules().size();
	}

	@Override
	public String getItem(int position) {
		return mName.getText().toString();
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// Create basic View
		mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View itemView = mInflater.inflate(R.layout.setup_sensor_listview_item, parent, false);
		// Get GUI elements
		ImageView img = (ImageView) itemView.findViewById(R.id.setup_sensor_item_icon);
		mName = (EditText) itemView.findViewById(R.id.setup_sensor_item_name);
		// Set image resource by sensor type
		img.setImageResource(mFacility.getModules().get(position).getIconResource());
		// Set name of sensor if isnt empty
		if(!mFacility.getModules().get(position).getServerName().isEmpty())
			mName.setText(mFacility.getModules().get(position).getServerName());


		// TODO Auto-generated method stub
		return itemView;
	}

}
