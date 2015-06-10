package com.rehivetech.beeeon.threading.task;

import android.content.Context;
import android.os.SystemClock;

import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.model.UninitializedDevicesModel;
import com.rehivetech.beeeon.threading.CallbackTask;


public class PairRequestTask extends CallbackTask<String> {
	private final String mGateId;
	private final long mTimeLimit;
	public boolean success = false;

	public PairRequestTask(Context context, String GateId, long timeLimit) {
		super(context);
		this.mGateId = GateId;
		this.mTimeLimit = timeLimit;
	}

	@Override
	protected Boolean doInBackground(String gateId) {
		Controller controller = Controller.getInstance(mContext);

		controller.getGatesModel().sendPairRequest(mGateId);

		UninitializedDevicesModel uninitializedDevicesModel = controller.getUninitializedDevicesModel();

		long startTime = System.currentTimeMillis();
		while (startTime + mTimeLimit * 1000 > System.currentTimeMillis()) {
			if (isCancelled()) {
				break;
			}

			uninitializedDevicesModel.reloadUninitializedDevicesByGate(gateId, true);
			if (uninitializedDevicesModel.getUninitializedDevicesByGate(mGateId).size() > 0) {
				return true;
			}
			SystemClock.sleep(1000);
		}
		// when the loop is over, the time is out -> it was unsuccessful
		return false;
	}


}
