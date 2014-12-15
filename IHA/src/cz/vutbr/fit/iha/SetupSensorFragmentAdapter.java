package cz.vutbr.fit.iha;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.viewpagerindicator.IconPagerAdapter;

import cz.vutbr.fit.iha.activity.fragment.AddAdapterFragment;
import cz.vutbr.fit.iha.activity.fragment.AddSensorFragment;
import cz.vutbr.fit.iha.activity.fragment.IntroImageFragment;
import cz.vutbr.fit.iha.activity.fragment.SetupSensorFragment;

public class SetupSensorFragmentAdapter extends FragmentPagerAdapter implements IconPagerAdapter {
    protected static final String[] CONTENT = new String[] { "Welcome", "to", "IHA", "Test", };
    protected static final int[] ICONS = new int[] {
            R.drawable.loc_bath_room,
            R.drawable.loc_garden,
            R.drawable.loc_wc,
            R.drawable.loc_dinner_room
    };

    private int mCount = 1;

    public SetupSensorFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
    	switch (position) {
    	case 0:
    		// setup senzor
    		return new SetupSensorFragment();
    	}
		return null;
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public CharSequence getPageTitle(int position) {
      return SetupSensorFragmentAdapter.CONTENT[position % CONTENT.length];
    }

    @Override
    public int getIconResId(int index) {
      return ICONS[index % ICONS.length];
    }

    public void setCount(int count) {
        if (count > 0 && count <= 10) {
            mCount = count;
            notifyDataSetChanged();
        }
    }
}