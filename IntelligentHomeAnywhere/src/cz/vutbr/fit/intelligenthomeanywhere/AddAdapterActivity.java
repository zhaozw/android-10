package cz.vutbr.fit.intelligenthomeanywhere;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

public class AddAdapterActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_adapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.add_adapter, menu);
		return true;
	}

	/**
	 * Method for perform onClick for registration of new adapter by serial number
	 * @param v
	 */
	public void addMethodSerialNumber(View v){
		EditText serialNuber = (EditText)findViewById(R.id.addadapter_ser_num);
		Log.i("seriove cislo",serialNuber.getText().toString());
		Intent intent = new Intent(this, RegistrationActivity.class);
        intent.putExtra(Constants.ADAPTER_SERIAL_NUMBER, serialNuber.getText().toString());
        startActivity(intent);
        this.finish();
	}
	
	/**
	 * Method for perform onClick for registration of new adapter by QR code
	 * @param v
	 */
	public void addMethodQRCode(View v){
		try {
			//TODO: maybe different client
		    Intent intent = new Intent("com.google.zxing.client.android.SCAN");
		    intent.putExtra("SCAN_MODE", "QR_CODE_MODE"); // PRODUCT_MODE for bar codes

		    startActivityForResult(intent, 0);
		} catch (Exception e) {
		    Uri marketUri = Uri.parse("market://details?id=com.google.zxing.client.android");
		    Intent marketIntent = new Intent(Intent.ACTION_VIEW,marketUri);
		    startActivity(marketIntent);
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {           
	    super.onActivityResult(requestCode, resultCode, data);
	    if (requestCode == 0) {

	        if (resultCode == RESULT_OK) {
	            String contents = data.getStringExtra("SCAN_RESULT");
	            Log.i("seriove cislo",contents);
	            Intent intent = new Intent(this, RegistrationActivity.class);
	            intent.putExtra(Constants.ADAPTER_SERIAL_NUMBER, contents);
	            startActivity(intent);
	            
	        }
	        if(resultCode == RESULT_CANCELED){
	        	//TODO: handle cancel ?
	        }
	        this.finish();
	    }
	}
}
