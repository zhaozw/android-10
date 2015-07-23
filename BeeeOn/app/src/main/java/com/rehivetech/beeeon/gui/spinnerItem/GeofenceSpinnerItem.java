package com.rehivetech.beeeon.gui.spinnerItem;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.geofence.SimpleGeofence;

public class GeofenceSpinnerItem extends AbstractSpinnerItem {
	private SimpleGeofence mGeofence;

	public GeofenceSpinnerItem(SimpleGeofence geofence, String id, Context context) {
		super(id, SpinnerItemType.GEOFENCE);
		mGeofence = geofence;
	}

	@Override
	public SimpleGeofence getObject() {
		return mGeofence;
	}

	@Override
	public void setDropDownView(View convertView) {
		TextView ItemLabel = (TextView) convertView.findViewById(R.id.custom_spinner_dropdown_label);
		ItemLabel.setText(mGeofence.getName());

		setMView(convertView);
	}

	@Override
	public int getDropDownLayout() {
		return R.layout.item_spinner_geofence_no_icon_dropdown;
	}


	@Override
	public void setView(View convertView) {
		ImageView ItemIcon = (ImageView) convertView.findViewById(R.id.custom_spinner_icon);
		ItemIcon.setImageResource(R.drawable.ic_val_geofence_gray);

		TextView ItemLabel = (TextView) convertView.findViewById(R.id.custom_spinner_label);
		ItemLabel.setText(mGeofence.getName());
	}

	public int getLayout() {
		return R.layout.item_spinner_geofence_icon;
	}

	@Override
	public void setIsSelected() {
		getMView().setBackgroundColor(getMView().getResources().getColor(R.color.gray_light));
	}

	@Override
	public void setNotSelected() {
		getMView().setBackgroundColor(getMView().getResources().getColor(R.color.beeeon_background));
	}

}
