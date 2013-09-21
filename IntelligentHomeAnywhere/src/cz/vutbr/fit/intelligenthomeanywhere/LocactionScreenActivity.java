package cz.vutbr.fit.intelligenthomeanywhere;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * Activity class for choosing location
 * @author ThinkDeep
 *
 */
public class LocactionScreenActivity extends Activity {

	private Capabilities _capabilities;
	
	/**
	 * Call xml parser to file on sdcard
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_locaction_screen);
		
		String GETFROMSERVER = null;
		Bundle bundle = this.getIntent().getExtras();
		if(bundle != null){
			if(bundle.getString(Constants.LOGIN).equals(Constants.LOGIN_DEMO)){
				GETFROMSERVER = Constants.DEMO_COMMUNICATION;
			}else if(bundle.getString(Constants.LOGIN).equals(Constants.LOGIN_COMM)){
				//TODO: calling to server for xml file
				//TODO: setting up timed calling to server
				Log.d("TODO","here");
				this.finish();
				return;
			}
		}

		XmlParser xmlparser = new XmlParser(GETFROMSERVER);
		xmlparser.Parse();
		_capabilities = xmlparser.GetResult();
		if(_capabilities == null)
			return;
		Log.i("parsedXML",_capabilities.toString());
		Constants.SetCapabilities(_capabilities);
		Constants.setContext(this.getApplicationContext());
		
		if(_capabilities.isNewOne()){
			Intent intent = new Intent(this,Notification.class);
			startActivity(intent);
		}
		
		ArrayList<String> locations = _capabilities.getLocations(false);
		Log.d("lokace",locations.toString());
		
		int marginTop = 5;
		int ID = Constants.BUTTON_ID;
		for(String s : locations){
			if(addLocationButton(s, ID, marginTop))
				ID++;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.locaction_screen, menu);
		return true;
	}

	public void onResume(){
		super.onResume();
		int ID = Constants.BUTTON_ID;
		//DEBUG: maybe saving on SDCard even after calling sever as a cache
		XmlCreator xmlcreator = new XmlCreator(Constants.GetCapabilities());
		xmlcreator.SaveXml(Environment.getExternalStorageDirectory().toString() + "/IHA/","komunikace.xml");
		
		Log.i("onResume",this.getLocalClassName());
		if(Constants.GetCapabilities().isNewInit()){
			ArrayList<String> Old = GetLocationsFromButtons(ID);
			ArrayList<String> New = Constants.GetCapabilities().getLocations(false);
			Log.d("Old",Old.toString());
			Log.d("New", New.toString());
			if(Old.size() != New.size()){
				New.removeAll(Old);
				Log.i("Wanted",New.toString());
				addLocationButton(New.get(0), ID + Old.size() + 1, 5);
			}
		}
	}
	
	/**
	 * Draw a button to gui
	 * @param s specific location name
	 * @param ID id of button
	 * @param marginTop margin of the button
	 */
	private boolean addLocationButton(String s, int ID, int marginTop){
		Button button = new Button(this);
		button.setText(s);
		button.setTextSize(getResources().getDimension(R.dimen.textsize));
		button.setId(ID);
		if(s == null || s.length() < 1)
			return false;
		
		LinearLayout mylayout = (LinearLayout)findViewById(R.id.location_scroll);
		mylayout.setOrientation(LinearLayout.VERTICAL);
		
		LinearLayout.LayoutParams params_btn = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		params_btn.setMargins(0, marginTop, 0, 0);
		button.setLayoutParams(params_btn);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Button clicked = (Button)v;
				Log.i("kliknuto-na",clicked.getText().toString());
				
				Intent intent = new Intent(getBaseContext(), DataOfLocacionScreenActivity.class);
				intent.putExtra(Constants.LOCATION_CLICKED, clicked.getText().toString());
				startActivity(intent);
			}
		});
		//XXX: setBackground from API 16
		button.setBackgroundDrawable(getResources().getDrawable(R.drawable.shape));
		mylayout.addView(button);
		return true;
	}
	
	/**
	 * Return list of location names of buttons
	 * @param ID number of start id (end with first null)
	 * @return arraylist with location names
	 */
	private ArrayList<String> GetLocationsFromButtons(int ID){
		ArrayList<String> result = new ArrayList<String>();
		try{
			for(int i = ID; i > 0; i++)
				result.add(((Button)findViewById(i)).getText().toString());
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}
}
