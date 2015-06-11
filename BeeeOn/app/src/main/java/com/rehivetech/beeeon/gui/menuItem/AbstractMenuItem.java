package com.rehivetech.beeeon.gui.menuItem;

import android.view.View;

public abstract class AbstractMenuItem implements IMenuItem {
	private String mId = ID_UNDEFINED;
	private MenuItemType mType;
	private View mMView;

	public AbstractMenuItem(String id, MenuItemType type) {
		mId = id;
		mType = type;
	}

	@Override
	public String getId() {
		return mId;
	}

	@Override
	public MenuItemType getType() {
		return mType;
	}

	public void setMView(View view) {
		mMView = view;
	}

	public View getMView() {
		return mMView;
	}
}