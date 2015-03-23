package com.rehivetech.beeeon.asynctask;

import android.content.Context;
import com.rehivetech.beeeon.controller.Controller;

/**
 * Reloads facilities by adapter
 */
public class GetAdapterUsersTask extends CallbackTask<String> {

	private final boolean mForceReload;

	public GetAdapterUsersTask(Context context, boolean forceReload) {
		super(context);

		mForceReload = forceReload;
	}

	@Override
	protected Boolean doInBackground(String adapterId) {
		Controller controller = Controller.getInstance(mContext);

		return controller.reloadAdapterUsers(adapterId, mForceReload);
	}

}
