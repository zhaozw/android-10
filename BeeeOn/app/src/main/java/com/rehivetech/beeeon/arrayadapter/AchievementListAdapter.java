package com.rehivetech.beeeon.arrayadapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.gamification.AchievementList;
import com.rehivetech.beeeon.gamification.AchievementListItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Jan Lamacz
 */
public class AchievementListAdapter extends BaseAdapter implements Filterable{
	private static final String TAG = AchievementListAdapter.class.getSimpleName();

	Context mContext;
	Controller mController;
	LayoutInflater mInflater;

	private String mCategoryId;
	private ItemFilter mFilter = new ItemFilter();

	List<AchievementListItem> mAchievementList;
	List<AchievementListItem> mFilteredList;

	public AchievementListAdapter(Context context, LayoutInflater inflater, String categoryId, AchievementList achievementList){
		mContext = context;
		mController = Controller.getInstance(mContext);
		mInflater = inflater;
		mCategoryId = categoryId;
		mAchievementList = achievementList.getAchievements();
		mFilteredList = mAchievementList;
		mFilter.filter(categoryId);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if(convertView == null){
			convertView = mInflater.inflate(R.layout.achievement_listview_item, parent, false);
			holder = new ViewHolder();
			holder.achievementName = (TextView) convertView.findViewById(R.id.achievement_list_name);
			holder.achievementDescription = (TextView) convertView.findViewById(R.id.achievement_list_description);
			holder.achievementPoints = (TextView) convertView.findViewById(R.id.achievement_list_points);
			holder.achievementDate = (TextView) convertView.findViewById(R.id.achievement_list_date);
			holder.achievementTick = (ImageView) convertView.findViewById(R.id.achievement_list_tick);
			convertView.setTag(holder);
		}
		else{
			// when we've inflated enough layouts, we just take them from memory
			holder = (ViewHolder) convertView.getTag();
		}

		// set values of item
		AchievementListItem achievement = this.getItem(position);

		holder.achievementName.setText(achievement.getName());
		holder.achievementDescription.setText(achievement.getDescription());
		holder.achievementPoints.setText(String.valueOf(achievement.getPoints()));
//		holder.achievementDate.setText("10/5/15");
		if(achievement.isDone()) {
			setBg(holder.achievementPoints, convertView.getResources().getDrawable(R.drawable.hexagon_cyan));
			holder.achievementName.setTextColor(convertView.getResources().getColor(R.color.beeeon_primary_cyan));
			holder.achievementDescription.setTextColor(convertView.getResources().getColor(R.color.beeeon_secundary_pink));
			holder.achievementDate.setText(achievement.getDate());
			holder.achievementDate.setVisibility(View.VISIBLE);
			holder.achievementTick.setVisibility(View.VISIBLE);
		}
		else {
			setBg(holder.achievementPoints, convertView.getResources().getDrawable(R.drawable.hexagon_gray));
			holder.achievementName.setTextColor(convertView.getResources().getColor(R.color.beeeon_text_color));
			holder.achievementDescription.setTextColor(convertView.getResources().getColor(R.color.beeeon_text_hint));
			holder.achievementDate.setVisibility(View.INVISIBLE);
			holder.achievementTick.setVisibility(View.INVISIBLE);
		}

		return convertView;
	}

	/** Sets background from Java.
	 * Made bcs setBackground works from API 16 and more
	 * and setBackgroundDrawable is market as deprecated.
	 */
	@SuppressWarnings("deprecation")
	private static void setBg(TextView view, Drawable image) {
		if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN)
			view.setBackgroundDrawable(image);
		else
			view.setBackground(image);
	}

		@Override
	public int getCount() {return mFilteredList.size();}

	@Override
	public AchievementListItem getItem(int position) {return mFilteredList.get(position);}

	@Override
	public long getItemId(int position) {return position;}

	@Override
	public Filter getFilter() {return mFilter;}

	/** Filter for specific category.
	 * Goes through all (downloaded) achievements and returns
	 * the ones belonging to @param constraint (categoryId)
	 */
	private class ItemFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			String categoryId = constraint.toString();

			FilterResults results = new FilterResults();

			final List<AchievementListItem> list = mAchievementList;

			int count = list.size();
			final ArrayList<AchievementListItem> nlist = new ArrayList<>(count);

			AchievementListItem achievement;

			for (int i = 0; i < count; i++) {
				achievement = list.get(i);
				if (achievement.getCategory().contains(categoryId)) {
						nlist.add(achievement);
				}
			}
			Collections.sort(nlist);
			results.values = nlist;
			results.count = nlist.size();

			return results;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint, FilterResults results) {
			mFilteredList = (ArrayList<AchievementListItem>) results.values;
			notifyDataSetChanged();
		}

	}

	private static class ViewHolder{
		public TextView achievementName;
		public TextView achievementDescription;
		public TextView achievementPoints;
		public TextView achievementDate;
		public ImageView achievementTick;
	}
}