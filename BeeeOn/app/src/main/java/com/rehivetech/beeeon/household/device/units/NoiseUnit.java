package com.rehivetech.beeeon.household.device.units;

import com.rehivetech.beeeon.Constants;
import com.rehivetech.beeeon.R;

public class NoiseUnit extends BaseUnit {

	public static final int DECIBEL = 0;
	public static final int BEL = 1;
	public static final int NEPER = 2;

	public NoiseUnit() {
		super();

		mItems.add(this.new Item(DECIBEL, R.string.unit_noise_decibel_full, R.string.unit_noise_decibel));
		mItems.add(this.new Item(BEL, R.string.unit_noise_bel_full, R.string.unit_noise_bel));
		mItems.add(this.new Item(NEPER, R.string.unit_noise_neper_full, R.string.unit_noise_neper));
	}



	@Override
	public int getDefaultId() {
		return DECIBEL;
	}

	@Override
	public String getPersistenceKey() {
		return Constants.PERSISTENCE_PREF_NOISE;
	}

	@Override
	public double convertValue(Item to, double value) {
		switch (to.getId()) {
			case BEL:
				return value * 0.1;
			case NEPER:
				return value / (20 * Math.log10(Math.E));
			default:
				return value;
		}
	}

}
