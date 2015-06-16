package com.rehivetech.beeeon.gamification.achievement;

import android.content.Context;
import android.content.SharedPreferences;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.controller.Controller;

/**
 * @author Jan Lamacz
 */
public class TwLoginAchievement extends Achievement {
	public TwLoginAchievement(Context context) {
		super(Constants.ACHIEVEMENT_TWITTER_LOGIN, context, false);
		if (!mData.isDone()) {
			SharedPreferences prefs = Controller.getInstance(mContext).getUserSettings();
			prefs.edit().putString(Constants.PERSISTENCE_PREF_LOGIN_TWITTER, "whatever").apply();
		}
	}
}
