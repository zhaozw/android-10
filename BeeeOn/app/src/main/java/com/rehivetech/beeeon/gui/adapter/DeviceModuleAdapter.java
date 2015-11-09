package com.rehivetech.beeeon.gui.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.rehivetech.beeeon.IconResourceType;
import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.values.BaseValue;
import com.rehivetech.beeeon.household.device.values.BooleanValue;
import com.rehivetech.beeeon.household.device.values.EnumValue;
import com.rehivetech.beeeon.util.UnitsHelper;

import java.util.List;

/**
 * @author martin on 13.8.2015.
 */
public class DeviceModuleAdapter extends RecyclerView.Adapter<DeviceModuleAdapter.DeviceModuleAdapterViewHolder> {

	public static final String TAG = DeviceModuleAdapter.class.getSimpleName();

	private Context mContext;
	private List<Module> mModuleList;
	private ItemClickListener mItemClickListener;

	public DeviceModuleAdapter(Context context, List<Module> moduleList, ItemClickListener itemClickListener) {
		mContext = context;
		mModuleList = moduleList;
		mItemClickListener = itemClickListener;
	}

	@Override
	public DeviceModuleAdapter.DeviceModuleAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_device_module, parent, false);
		return new DeviceModuleAdapterViewHolder(view);
	}

	@Override
	public void onBindViewHolder(final DeviceModuleAdapter.DeviceModuleAdapterViewHolder holder, int position) {
		final Module module = mModuleList.get(position);
		holder.mModuleId = module.getId();

		SharedPreferences prefs = Controller.getInstance(mContext).getUserSettings();
		UnitsHelper unitsHelper = (prefs == null) ? null : new UnitsHelper(prefs, mContext);

		holder.mTitle.setText(module.getName(mContext));
		if (unitsHelper != null) {
			holder.mValue.setText(String.format("%s %s",
					unitsHelper.getStringValue(module.getValue()),
					unitsHelper.getStringUnit(module.getValue())));
		}
		holder.mIcon.setImageResource(module.getIconResource(IconResourceType.DARK));

		if (module.isActuator()) {
			BaseValue value = module.getValue();

			if (value instanceof BooleanValue) {
				holder.mButton.setVisibility(View.GONE);

				holder.mSwitch.setVisibility(View.VISIBLE);
				holder.mSwitch.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mItemClickListener.onSwitchChange(holder.mModuleId);
					}
				});
			} else if (value instanceof EnumValue) {
				holder.mSwitch.setVisibility(View.GONE);

				holder.mButton.setVisibility(View.VISIBLE);
				holder.mButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mItemClickListener.onButtonChangeState(holder.mModuleId);
					}
				});
			} else {
				holder.mSwitch.setVisibility(View.GONE);

				holder.mButton.setVisibility(View.VISIBLE);
				holder.mButton.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mItemClickListener.onButtonSetNewValue(holder.mModuleId);
					}
				});
			}
		} else {
			holder.mButton.setVisibility(View.GONE);
			holder.mSwitch.setVisibility(View.GONE);
		}

		holder.mView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mItemClickListener.onItemClick(holder.mModuleId);
			}
		});
	}

	@Override
	public int getItemCount() {
		return mModuleList.size();
	}

	public interface ItemClickListener {
		void onItemClick(String moduleId);

		void onButtonChangeState(String moduleId);

		void onButtonSetNewValue(String moduleId);

		void onSwitchChange(String moduleId);
	}

	public void swapModules(List<Module> modules) {
		mModuleList = modules;
		notifyDataSetChanged();
	}

	public class DeviceModuleAdapterViewHolder extends RecyclerView.ViewHolder {

		public String mModuleId;

		public final ImageView mIcon;
		public final TextView mTitle;
		public final TextView mValue;
		public final ImageButton mButton;
		public final SwitchCompat mSwitch;
		public final View mView;

		public DeviceModuleAdapterViewHolder(View itemView) {
			super(itemView);
			mIcon = (ImageView) itemView.findViewById(R.id.list_module_item_icon);
			mTitle = (TextView) itemView.findViewById(R.id.list_module_item_title);
			mValue = (TextView) itemView.findViewById(R.id.list_module_item_value);
			mButton = (ImageButton) itemView.findViewById(R.id.list_device_module_item_set_button);
			mSwitch = (SwitchCompat) itemView.findViewById(R.id.list_device_module_item_switch);
			mView = itemView;
		}
	}
}
