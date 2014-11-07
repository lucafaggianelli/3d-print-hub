package com.tesladocet.threedprinting;

import android.app.Activity;
import android.os.Bundle;
import android.print.PrintJobInfo;
import android.print.PrinterInfo;
import android.printservice.PrintService;

public class ThreeDPrintOptionsActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.advanced_print_options);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		PrintJobInfo printJobInfo = (PrintJobInfo) getIntent().getParcelableExtra(
                PrintService.EXTRA_PRINT_JOB_INFO);
        PrinterInfo printerInfo = (PrinterInfo) getIntent().getParcelableExtra(
                "android.intent.extra.print.EXTRA_PRINTER_INFO");
	}
}