package cz.vutbr.fit.iha.activity.menuItem;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import cz.vutbr.fit.iha.R;

public class GroupImageMenuItem extends GroupMenuItem {
	private int mImgRes;
	private OnClickListener mListener;

	public GroupImageMenuItem(String name, int imagRes, OnClickListener imageClickListener) {
		super(name, MenuItemType.GROUP_IMAGE);
		mImgRes = imagRes;
		mListener = imageClickListener;
	}

	@Override
	public void setView(View view) {
		super.setView(view);
		ImageView imgView = (ImageView) view
				.findViewById(cz.vutbr.fit.iha.R.id.image);
		imgView.setImageResource(mImgRes);
		imgView.setOnClickListener(mListener);
	}

	@Override
	public int getLayout() {
		return R.layout.drawer_listview_group_image;
	}
}
