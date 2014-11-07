package com.tesladocet.threedprinting;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class AddPrintersActivity extends Activity {
	private final static String TAG = "AddPrinters";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Bundle bundle = getIntent().getExtras();
		for (String key : bundle.keySet()) {
		    Object value = bundle.get(key);
		    Log.d(TAG, String.format("%s %s (%s)", key,  
		        value.toString(), value.getClass().getName()));
		}
	}
}