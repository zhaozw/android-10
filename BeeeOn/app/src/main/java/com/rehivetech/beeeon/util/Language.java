package com.rehivetech.beeeon.util;

import android.content.Context;

import com.rehivetech.beeeon.R;

/**
 * Created by david on 30.8.15.
 */
public class Language extends SettingsItem {

	public static final String PERSISTENCE_PREF_LANGUAGE = "pref_language";

	public static final int FROM_SYSTEM = 0;
	public static final int ENGLISH = 1;
	public static final int CZECH = 2;
	public static final int SLOVAK = 3;

	//TODO this class is also in timezone
	public class Item extends BaseItem {
		private final int mResName;

		protected Item(int id, int resName) {
			super(id);

			this.mResName = resName;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getSettingsName(Context context) {
			return context.getString(mResName);
		}
	}

	public Language() {
		super();

		mItems.add(this.new Item(FROM_SYSTEM, R.string.unit_noise_decibel_full));
		mItems.add(this.new Item(ENGLISH, R.string.unit_noise_bel_full));
		mItems.add(this.new Item(CZECH, R.string.unit_noise_neper_full));
		mItems.add(this.new Item(SLOVAK, R.string.unit_noise_neper_full));
	}

	@Override
	public int getDefaultId() {
		return FROM_SYSTEM;
	}

	@Override
	public String getPersistenceKey() {
		return PERSISTENCE_PREF_LANGUAGE;
	}
}
