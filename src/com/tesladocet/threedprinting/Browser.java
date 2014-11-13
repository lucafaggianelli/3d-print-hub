package com.tesladocet.threedprinting;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class Browser extends Activity {

	private final static String TAG = "Browser";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, getIntent().getData().toString());
	}
}
