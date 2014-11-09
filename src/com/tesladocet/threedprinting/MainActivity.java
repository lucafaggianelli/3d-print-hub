package com.tesladocet.threedprinting;

import java.util.ArrayList;

import com.tesladocet.threedprinting.printers.Printer3D;
import com.tesladocet.threedprinting.viewer.StlView;
import com.tesladocet.threedprinting.viewer.StlViewerActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends Activity {

	ArrayList<Printer3D> printers;
	PrintersAdapter printersAdapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		printers = new ArrayList<Printer3D>();
		printers.add(new Printer3D("my-printer", "My Printer"));
		
		printersAdapter = new PrintersAdapter(this, printers);
		
		ListView listView = (ListView) findViewById(R.id.printers_list);
		listView.setOnItemClickListener(printerClickListener);
		listView.setAdapter(printersAdapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.action_stl_viewer) {
			startActivity(new Intent(this, StlViewerActivity.class));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	OnItemClickListener printerClickListener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> adap, View view, int position,
				long id) {
			Intent i = new Intent(MainActivity.this, PrinterDetails.class);
			i.putExtra(Printer3D.EXTRA_PRINTER, printers.get(position));
			startActivity(i);
		}
	};
	
	public class PrintersAdapter extends ArrayAdapter<Printer3D> {
		
	    public PrintersAdapter(Context context, ArrayList<Printer3D> printers) {
	       super(context, 0, printers);
	    }

	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	       // Get the data item for this position
	       Printer3D printer = getItem(position);
	       // Check if an existing view is being reused, otherwise inflate the view
	       if (convertView == null) {
	          convertView = LayoutInflater.from(getContext()).inflate(
	        		  R.layout.item_printer, parent, false);
	       }
	       // Lookup view for data population
	       TextView title = (TextView) convertView.findViewById(R.id.title);
	       TextView desc = (TextView) convertView.findViewById(R.id.description);
	       // Populate the data into the template view using the data object
	       title.setText(printer.getName());
	       desc.setText(printer.getDescription());
	       // Return the completed view to render on screen
	       return convertView;
	   }
	}
}