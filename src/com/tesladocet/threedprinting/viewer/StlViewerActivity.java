package com.tesladocet.threedprinting.viewer;

import com.tesladocet.threedprinting.R;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

public class StlViewerActivity extends Activity {
	
	private final static String TAG = "StlViewer";
	private final static int FILE_SELECT_REQUEST = 0;
	
	private StlView stlView = null;
	
	private FrameLayout stlFrameLayout;
	private TextView hintView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stl);
		
		stlFrameLayout = (FrameLayout) findViewById(R.id.stlFrameLayout);
		hintView = (TextView) findViewById(R.id.hint);
		
		Uri stlUri = getIntent().getData();
		
		if (stlUri != null) {
			hintView.setVisibility(View.GONE);
			stlFrameLayout.setVisibility(View.VISIBLE);
			setUpViews(stlUri);
		} else {
			hintView.setVisibility(View.VISIBLE);
			stlFrameLayout.setVisibility(View.GONE);
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (stlView != null) {
			Log.i(TAG, "onResume");
			//stlRenderer.requestRedraw();
			stlView.requestRender();
			stlView.onResume();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (stlView != null) {
			Log.i(TAG, "onPause");
			stlView.onPause();
		}
	}
	
	private void setUpViews(Uri uri) {
		if (uri != null) {
			Log.d(TAG, "URI: " + uri.toString());
			setTitle(uri.getPath().substring(uri.getPath().lastIndexOf("/") + 1));
			
			FrameLayout relativeLayout = (FrameLayout) findViewById(R.id.stlFrameLayout);
			stlView = new StlView(this, uri);
			Log.d(TAG,"stlview null? " + (stlView==null));
			relativeLayout.addView(stlView);
		} else {
			Log.d(TAG, "URI is null");
		}
	}
	
	public void showFilePicker(View v) {
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT); 
	    intent.setType("application/sla");
	    intent.addCategory(Intent.CATEGORY_OPENABLE);

	    try {
	        startActivityForResult(
	                Intent.createChooser(intent, "Select a File to Upload"), 
	                FILE_SELECT_REQUEST);
	    } catch (android.content.ActivityNotFoundException ex) {
	        // Potentially direct the user to the Market with a Dialog
	        Toast.makeText(this, "Please install a File Manager", 
	                Toast.LENGTH_LONG).show();
	    }
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    switch (requestCode) {
	        case FILE_SELECT_REQUEST:
	        if (resultCode == RESULT_OK) {
	            Uri uri = data.getData();
	            Log.d(TAG, "Chosen file: " + uri.toString());
	            hintView.setVisibility(View.GONE);
				stlFrameLayout.setVisibility(View.VISIBLE);
				setUpViews(uri);
	        }
	        break;
	    }
	    super.onActivityResult(requestCode, resultCode, data);
	}
}