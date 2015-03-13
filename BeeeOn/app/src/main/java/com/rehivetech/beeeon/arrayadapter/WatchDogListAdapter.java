package com.rehivetech.beeeon.arrayadapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rehivetech.beeeon.R;
import com.rehivetech.beeeon.adapter.WatchDogRule;
import com.rehivetech.beeeon.controller.Controller;
import com.rehivetech.beeeon.util.UnitsHelper;

import java.util.List;

/**
 * Created by Tomáš on 25. 2. 2015.
 */
public class WatchDogListAdapter extends BaseAdapter {

    Context mContext;
    Controller mController;
    LayoutInflater mInflater;
    List<WatchDogRule> mRules;
    SharedPreferences mPrefs;
    UnitsHelper mUnitsHelper;


    public WatchDogListAdapter(Context context, List<WatchDogRule> rules, LayoutInflater inflater){
        mContext = context;
        mController = Controller.getInstance(mContext);
        mInflater = inflater;
        mRules = rules;

        // UserSettings can be null when user is not logged in!
        mPrefs = mController.getUserSettings();
        mUnitsHelper = (mPrefs == null) ? null : new UnitsHelper(mPrefs, mContext);
    }

    @Override
    public int getCount() {
        return mRules.size();
    }

    @Override
    public Object getItem(int position) {
        return mRules.get(position);
    }

    public WatchDogRule getRule(int position){
        return mRules.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        // when first time inflating layout (not when scrolling)
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.watchdog_listview_item, parent, false);

            holder = new ViewHolder();

            holder.ItemIcon = (ImageView) convertView.findViewById(R.id.watchdogItemIcon);
            holder.ItemRuleName = (TextView) convertView.findViewById(R.id.watchdogItemRuleName);
            holder.ItemSensorName = (TextView) convertView.findViewById(R.id.watchdogItemSensorName);
            holder.ItemOperator = (ImageView) convertView.findViewById(R.id.watchdogItemOperator);
            holder.ItemTreshold = (TextView) convertView.findViewById(R.id.watchdogItemTreshold);
            holder.ItemAction = (ImageView) convertView.findViewById(R.id.watchdogItemAction);
            holder.ItemSwitch = (SwitchCompat) convertView.findViewById(R.id.watchdogItemSwitch);

            convertView.setTag(holder);
        }
        else{
            // when we've inflated enough layouts, we just take them from memory
            holder = (ViewHolder) convertView.getTag();
        }

        // set values of item
        WatchDogRule rule = (WatchDogRule) this.getItem(position);

        holder.ItemIcon.setImageResource(rule.getDevice().getIconResource());
        holder.ItemRuleName.setText(rule.getName());
        holder.ItemSensorName.setText(rule.getDevice().getName());
        holder.setItemOperator(rule.getOperator());

        // sets value if unitsHelper otherwise raw value
        holder.ItemTreshold.setText(mUnitsHelper != null ? mUnitsHelper.getStringValueUnit(rule.getTreshold()) : String.valueOf(rule.getTreshold().getDoubleValue()));

        holder.setItemAction(rule.getAction());
        holder.ItemSwitch.setChecked(rule.getEnabled());

        return convertView;
    }

    public void updateData(List<WatchDogRule> data){
        this.mRules = data;
        notifyDataSetChanged();
    }

    private static class ViewHolder{
        public ImageView ItemIcon;
        public TextView ItemRuleName;
        public TextView ItemSensorName;
        public ImageView ItemOperator;
        public TextView ItemTreshold;
        public ImageView ItemAction;
        public SwitchCompat ItemSwitch;

        void setItemOperator(WatchDogRule.OperatorType type){
            switch(type){
                case SMALLER:
                    ItemOperator.setImageResource(R.drawable.ic_action_previous_item);
                    break;

                case GREATER:
                    ItemOperator.setImageResource(R.drawable.ic_action_next_item);
                    break;
            }
        }

        void setItemAction(WatchDogRule.ActionType type){
            switch(type){
                case NOTIFICATION:
                    ItemAction.setImageResource(R.drawable.ic_notification);
                break;

                case ACTOR_ACTION:
                    ItemAction.setImageResource(R.drawable.ic_shutdown);
                break;
            }
        }
    }
}