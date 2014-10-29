package cz.vutbr.fit.iha.activity.fragment;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Interval;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragment;

import cz.vutbr.fit.iha.R;
import cz.vutbr.fit.iha.activity.MainActivity;
import cz.vutbr.fit.iha.adapter.Adapter;
import cz.vutbr.fit.iha.adapter.device.Device;
import cz.vutbr.fit.iha.adapter.device.DeviceLog;
import cz.vutbr.fit.iha.adapter.device.DeviceLog.DataInterval;
import cz.vutbr.fit.iha.adapter.device.DeviceLog.DataType;
import cz.vutbr.fit.iha.adapter.device.Facility;
import cz.vutbr.fit.iha.arrayadapter.GraphArrayAdapter;
import cz.vutbr.fit.iha.controller.Controller;
import cz.vutbr.fit.iha.pair.LogDataPair;

public class CustomViewFragment extends SherlockFragment {
	
	private MainActivity mActivity;
	
	private SparseArray<List<Device>> mDevices = new SparseArray<List<Device>>();
	private SparseArray<List<DeviceLog>> mLogs = new SparseArray<List<DeviceLog>>();
	
	private View mLayout;
	
	private static final String TAG = CustomViewFragment.class.getSimpleName();
	
	private Context mContext;
	private Controller mController;
	
	
	public CustomViewFragment(MainActivity context) {
		mActivity = context;
		mController = Controller.getInstance(mActivity.getApplicationContext());
	}
	public CustomViewFragment() {}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.graphofsensors, container, false);
		
		mLayout = view;
		
		prepareDevices();
		
		GetDeviceLogTask getDeviceLogTask = new GetDeviceLogTask();
		getDeviceLogTask.execute(mDevices.valueAt(0));
		
		//redrawCustomView(view);
		
		return view;
	}
	
	public boolean redrawCustomView(List<List<DeviceLog>> logs) {

		ListView graphsList = (ListView) mLayout.findViewById(R.id.listviewofgraphs);
		
		GraphArrayAdapter adapter = new GraphArrayAdapter(getActivity(), R.layout.custom_graph_item, logs);
		adapter.setLayoutInflater(getLayoutInflater(null));
		graphsList.setAdapter(adapter);

		//mActivity.setSupportProgressBarIndeterminateVisibility(false);
		return true;
	}
	
	
	private void prepareDevices() {
		for (Adapter adapter : mController.getAdapters()) {
			for (Facility facility: mController.getFacilitiesByAdapter(adapter.getId())) {
				for (Device device : facility.getDevices()) {
					List<Device> devices = mDevices.get(device.getType().getTypeId());
					if (devices == null) {
						devices = new ArrayList<Device>();
						mDevices.put(device.getType().getTypeId(), devices);
					}
					
					devices.add(device);
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private void prepareLogs() {
		for (int i=0; i<mDevices.size(); i++) {
			GetDeviceLogTask getDeviceLogTask = new GetDeviceLogTask();
			getDeviceLogTask.execute(mDevices.valueAt(i));
		}
	}
	
	
	
	private class GetDeviceLogTask extends AsyncTask<List<Device>, Void, List<DeviceLog>> {
		@Override
		protected List<DeviceLog> doInBackground(List<Device>... devices) {
			List<Device> list = devices[0]; // expects only one device list at a time is sent there
			
			DateTime end = DateTime.now(DateTimeZone.UTC);
			DateTime start = end.minusWeeks(1);
			
			List<DeviceLog> logs = new ArrayList<DeviceLog>();
			
			for (Device device : list) {
				LogDataPair pair = new LogDataPair( //
						device, // device
						new Interval(start, end), // interval from-to
						DataType.AVERAGE, // type
						DataInterval.HOUR); // interval

				logs.add(mController.getDeviceLog(pair.device, pair));	
			}
			
			return logs;
		}

		@Override
		protected void onPostExecute(List<DeviceLog> logs) {
			List<List<DeviceLog>> list = new ArrayList<List<DeviceLog>>();
			list.add(logs);
			
			redrawCustomView(list);
		}

	}

}
