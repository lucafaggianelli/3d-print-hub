package com.tesladocet.threedprinting;

import com.tesladocet.threedprinting.printers.MaterialView;
import com.tesladocet.threedprinting.printers.Printer3D;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PrinterDetails extends Activity {
	
	Printer3D printer;
	
	TextView title;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.printer_details);
		
		printer = (Printer3D) getIntent()
				.getParcelableExtra(Printer3D.EXTRA_PRINTER);
		
		setUpView();
	}
	
	private void setUpView() {
		setTitle(printer.getName());
		
		((TextView) findViewById(R.id.name))
			.setText(printer.getName());
		
		((TextView) findViewById(R.id.description))
			.setText(printer.getDescription());
		
		((LinearLayout) findViewById(R.id.extruders))
			.addView(new MaterialView(this));
	}
}
