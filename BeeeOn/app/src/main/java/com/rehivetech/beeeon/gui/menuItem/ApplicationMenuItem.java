package com.rehivetech.beeeon.gui.menuItem;

import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.rehivetech.beeeon.R;

public class ApplicationMenuItem extends AbstractMenuItem {
	private String mName;
	private int mIconRes;
	private boolean mTopSeparatorVisible;
	private boolean mActualLoc;

	public ApplicationMenuItem(String name, int iconRes, boolean topSeparator, String id, boolean actualLoc) {
		super(id, MenuItemType.APPLICATION);
		mName = name;
		mIconRes = iconRes;
		mTopSeparatorVisible = topSeparator;
		mActualLoc = actualLoc;
	}

	@Override
	public void setView(View view) {
		TextView nameView = (TextView) view.findViewById(R.id.name);
		ImageView iconView = (ImageView) view.findViewById(R.id.icon);
		View separatorView = (View) view.findViewById(R.id.top_separator);

		nameView.setText(mName);
		iconView.setImageResource(mIconRes);
		if (mTopSeparatorVisible) {
			separatorView.setVisibility(View.VISIBLE);
		} else {
			separatorView.setVisibility(View.GONE);
		}
		if (mActualLoc) {
			nameView.setTextColor(view.getResources().getColor(R.color.beeeon_primary));
			nameView.setTypeface(null, Typeface.BOLD);
		}
		setMView(view);
	}

	@Override
	public int getLayout() {
		return R.layout.drawer_listview_location;
	}

	@Override
	public void setIsSelected() {
		getMView().setBackgroundColor(getMView().getResources().getColor(R.color.gray_light));
	}

	@Override
	public void setNotSelected() {
		getMView().setBackgroundColor(getMView().getResources().getColor(R.color.beeeon_background_drawer));
	}

}
