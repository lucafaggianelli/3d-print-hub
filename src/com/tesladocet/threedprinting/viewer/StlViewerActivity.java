package com.tesladocet.threedprinting.viewer;

import com.tesladocet.threedprinting.R;
import com.tesladocet.threedprinting.printers.PrintModelAdapter;
import com.tesladocet.threedprinting.utils.FilePicker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.print.PrintManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

public class StlViewerActivity extends Activity {
	
	private final static String TAG = "StlViewer";
	private final static int FILE_SELECT_REQUEST = 0;
	
	private PrintManager printManager;
	
	private StlView stlView = null;
	
	private FrameLayout stlFrameLayout;
	private TextView hintView;
	
	private MenuItem itemInfo;
	private MenuItem itemPrint;
	
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
		
		printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (stlView != null) {
			//stlRenderer.requestRedraw();
			stlView.requestRender();
			stlView.onResume();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (stlView != null) {
			stlView.onPause();
		}
	}
	
	private void setUpViews(Uri uri) {
		if (uri != null) {
			Log.d(TAG, "URI: " + uri.toString());
			setTitle(uri.getPath().substring(uri.getPath().lastIndexOf("/") + 1));
			
			FrameLayout relativeLayout = (FrameLayout) findViewById(R.id.stlFrameLayout);
			stlView = new StlView(this, uri);
			relativeLayout.addView(stlView);
			itemInfo.setVisible(true);
			itemPrint.setVisible(true);
		} else {
			Log.d(TAG, "URI is null");
		}
	}
	
	public void showFilePicker(View v) {	    
	    FilePicker filePicker = new FilePicker(this,
    		new FilePicker.SimpleFileDialogListener() {
                @Override
                public void onChosenDir(String chosenDir) {
                    hintView.setVisibility(View.GONE);
    				stlFrameLayout.setVisibility(View.VISIBLE);
    				setUpViews(Uri.parse("file://" + chosenDir));
                }
            }
        );
	    filePicker.setType(FilePicker.FILE_OPEN, true);
	    filePicker.launch();
	}
	
//	@Override
//	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//	    switch (requestCode) {
//	        case FILE_SELECT_REQUEST:
//	        if (resultCode == RESULT_OK) {
//	            Uri uri = data.getData();
//	            Log.d(TAG, "Chosen file: " + uri.toString());
//	            hintView.setVisibility(View.GONE);
//				stlFrameLayout.setVisibility(View.VISIBLE);
//				setUpViews(uri);
//	        }
//	        break;
//	    }
//	    super.onActivityResult(requestCode, resultCode, data);
//	}
	
	private AlertDialog getDialog() {		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("STL Info");
		String[] items = new String[] {
				"Volume: " + (stlView.getStlObject().volume/1000) + " cm^3"
		};
		builder.setItems(items, null);

		return builder.create();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.stl_viewer, menu);
		itemInfo = menu.findItem(R.id.action_stl_info);
		itemPrint = menu.findItem(R.id.action_stl_print);
		itemInfo.setVisible(false);
		itemPrint.setVisible(false);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		
		switch (id) {
		
		case R.id.action_stl_info:
			getDialog().show();
			return true;
		
		case R.id.action_stl_print:
			Log.d(TAG, "Need to print");
			String jobName = getString(R.string.app_name) + " Model";
			printManager.print(jobName, new PrintModelAdapter(), null);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}