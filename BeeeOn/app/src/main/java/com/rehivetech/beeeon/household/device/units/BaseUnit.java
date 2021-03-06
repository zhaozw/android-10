package com.rehivetech.beeeon.household.device.units;

import android.content.Context;

import com.rehivetech.beeeon.util.SettingsItem;

public abstract class BaseUnit extends SettingsItem {

	public class Item extends BaseItem {
		private final int mResUnitName;
		private final int mResUnitShortName;

		protected Item(int id, int resUnitName, int resUnitShortName) {
			super(id, 0); // we set resName to 0, because we use own 2 res field and rewrite default getSettingsName(Context) method anyway

			this.mResUnitName = resUnitName;
			this.mResUnitShortName = resUnitShortName;
		}

		/**
		 * Get short form for unit. For example for celsius you will get "°C".
		 *
		 * @param context It can be app context
		 * @return Short form for unit
		 */
		public String getStringUnit(Context context) {
			return context.getString(mResUnitShortName);
		}

		/**
		 * Get full name for unit. For example for celsius you will get "Celsius".
		 *
		 * @param context It can be app context
		 * @return String which
		 */
		public String getStringName(Context context) {
			return context.getString(mResUnitName);
		}

		/**
		 * Get full name with short form for unit. For example for celsius you will get "Celsius (°C)".
		 *
		 * @param context It can be app context
		 * @return String which
		 */
		public String getStringNameUnit(Context context) {
			return String.format("%s (%s)", getStringName(context), getStringUnit(context));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String getSettingsName(Context context) {
			return getStringNameUnit(context);
		}
	}

	abstract public double convertValue(Item to, double value);

}
