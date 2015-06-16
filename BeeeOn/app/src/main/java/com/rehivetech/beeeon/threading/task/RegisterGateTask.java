package com.rehivetech.beeeon.threading.task;

import android.content.Context;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.household.gate.Gate;
import com.rehivetech.beeeon.model.GatesModel;
import com.rehivetech.beeeon.threading.CallbackTask;

import java.util.List;
import java.util.Locale;
import java.util.Vector;

/**
 * Registers new gate. It automatically reloads list of gates and then we set this gate as active which also load all its sensors.
 */
public class RegisterGateTask extends CallbackTask<Gate> {

	public RegisterGateTask(Context context) {
		super(context);
	}

	private String getUniqueGateName(List<Gate> gates) {
		Vector<String> gateNames = new Vector<String>();

		for (Gate gate : gates) {
			gateNames.add(gate.getName());
		}

		String name;

		int number = 1;
		do {
			name = mContext.getString(R.string.adapter_default_name, number++);
		} while (gateNames.contains(name));

		return name;
	}

	private String getHexaGateName(String id) throws NumberFormatException {
		int number = Integer.parseInt(id);
		return Integer.toHexString(number).toUpperCase(Locale.getDefault());
	}

	@Override
	protected Boolean doInBackground(Gate gate) {
		Controller controller = Controller.getInstance(mContext);
		GatesModel gatesModel = controller.getGatesModel();

		String serialNumber = gate.getId();
		String name = gate.getName().trim();

		// Set default name for this gate, if user didn't filled any
		if (name.isEmpty()) {
			try {
				name = getHexaGateName(serialNumber);
			} catch (NumberFormatException e) {
				name = getUniqueGateName(gatesModel.getGates());
			}
		}

		// Register new gate and set it as active
		if (gatesModel.registerGate(serialNumber, name)) {
			controller.setActiveGate(serialNumber, true);
			return true;
		}

		return false;
	}

}
