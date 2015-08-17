package com.rehivetech.beeeon.gui.listItem;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.device.Device;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.util.TimeHelper;
import com.rehivetech.beeeon.util.UnitsHelper;

public class ModuleListItem extends AbstractListItem {
	private final Context mContext;
	private Module mModule;
	private boolean mSeparatorVisible;

	public ModuleListItem(Module module, String id, Context context, boolean separator) {
		super(id, ListItemType.MODULE);
		mModule = module;
		mSeparatorVisible = separator;
		mContext = context;
	}

	@Override
	public void setView(View itemView) {
		Controller controller = Controller.getInstance(mContext);
		// Locate the TextViews in drawer_list_item.xml
		TextView txtTitle = (TextView) itemView.findViewById(R.id.list_module_item_title);
		TextView txtValue = (TextView) itemView.findViewById(R.id.list_module_item_value);
		TextView txtUnit = (TextView) itemView.findViewById(R.id.list_module_item_unit);
		TextView txtTime = (TextView) itemView.findViewById(R.id.list_module_item_time);

		// Separators
		View sepMidle = itemView.findViewById(R.id.list_module_item_sep_middle);

		// Locate the ImageView in drawer_list_item.xml
		ImageView imgIcon = (ImageView) itemView.findViewById(R.id.list_module_item_icon);

		// UserSettings can be null when user is not logged in!
		SharedPreferences prefs = controller.getUserSettings();

		TimeHelper timeHelper = (prefs == null) ? null : new TimeHelper(prefs);
		UnitsHelper unitsHelper = (prefs == null) ? null : new UnitsHelper(prefs, mContext);

		// Set the results into TextViews
		txtTitle.setText(mModule.getName(mContext));

		if (unitsHelper != null) {
			txtValue.setText(unitsHelper.getStringValue(mModule.getValue()));
			txtUnit.setText(unitsHelper.getStringUnit(mModule.getValue()));
		}

		Device device = mModule.getDevice();
		Gate gate = controller.getGatesModel().getGate(device.getGateId());

		if (timeHelper != null) {
			txtTime.setText(timeHelper.formatLastUpdate(device.getLastUpdate(), gate));
		}

		// Set title selected for animation if is text long
		txtTitle.setSelected(true);

		// Set the results into ImageView
		imgIcon.setImageResource(mModule.getIconResource());

		if (mSeparatorVisible) {
			sepMidle.setVisibility(View.VISIBLE);
		} else {
			sepMidle.setVisibility(View.GONE);
		}
		setMView(itemView);
	}

	@Override
	public int getLayout() {
		return R.layout.item_list_module_item;
	}

	@Override
	public void setIsSelected() {
		getMView().setBackgroundColor(getMView().getResources().getColor(R.color.gray_light));
	}

	@Override
	public void setNotSelected() {
		getMView().setBackgroundColor(getMView().getResources().getColor(R.color.beeeon_background));
	}

	public Module getModule() {
		return mModule;
	}

}