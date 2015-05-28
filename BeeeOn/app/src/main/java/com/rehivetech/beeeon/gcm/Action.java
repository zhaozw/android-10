package com.rehivetech.beeeon.gcm;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.activity.SensorDetailActivity;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.device.Module;
import com.rehivetech.beeeon.household.device.Facility;

import java.util.List;

/**
 * Created by Martin on 6. 5. 2015.
 */
public final class Action {

	private Action(){};

	@Nullable
	static public void getSensorDetailIntent(Context context, int adapterId, String sensorId, int type) {
		Controller controller = Controller.getInstance(context);
		Facility facility = controller.getFacilitiesModel().getFacility(String.valueOf(adapterId), sensorId);
		if (facility == null) {
			Toast.makeText(context, R.string.toast_device_not_available, Toast.LENGTH_SHORT).show();
			return;
		}


		List<Module> modules = facility.getModules();
		if (modules.size() == 0) {
			Toast.makeText(context, R.string.toast_device_not_available, Toast.LENGTH_SHORT).show();
			return;
		}

		int pos = 0;
		for (int i = 0; i < modules.size(); i++) {
			if (modules.get(i).getRawTypeId().equals(String.valueOf(type))) {

				pos = i;
				break;
			}
		}

		Module module = modules.get(pos);

		// Sensor exists, we can open activity
		Intent intent = new Intent(context, SensorDetailActivity.class);
		intent.putExtra(SensorDetailActivity.EXTRA_DEVICE_ID, module.getId());
		intent.putExtra(SensorDetailActivity.EXTRA_ADAPTER_ID, String.valueOf(adapterId));
		intent.putExtra(SensorDetailActivity.EXTRA_ACTIVE_POS, pos);

		context.startActivity(intent);
	}
}
