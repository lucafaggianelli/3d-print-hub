package com.tesladocet.threedprinting.viewer;

import java.io.File;

import com.tesladocet.threedprinting.R;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.ToggleButton;

public class ViewerActivity extends Activity {
	
	private final static String TAG = "Act";
	
	private StlView stlView = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
//		//mGLView = new StlSurfaceView(this);
//		setContentView(stlView);
		Uri u = Uri.fromFile(new File (
				Environment.getExternalStorageDirectory(),"horse_tummy.stl"));
		setUpViews(u);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (stlView != null) {
			Log.i(TAG, "onResume");
			StlRenderer.requestRedraw();
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
		setContentView(R.layout.stl);
				
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
}