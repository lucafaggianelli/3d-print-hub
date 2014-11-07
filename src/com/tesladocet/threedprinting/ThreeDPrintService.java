package com.tesladocet.threedprinting;

import android.printservice.PrintJob;
import android.printservice.PrintService;
import android.printservice.PrinterDiscoverySession;
import android.util.Log;

public class ThreeDPrintService extends PrintService {

	@Override
	protected PrinterDiscoverySession onCreatePrinterDiscoverySession() {
		return new PrinterDiscovery(this);
	}

	@Override
	protected void onPrintJobQueued(PrintJob job) {
		Log.d("Service", "Queued: " + job.getInfo().getLabel());
	}

	@Override
	protected void onRequestCancelPrintJob(PrintJob job) {
		Log.d("Service", "Need to cancel: " + job.getInfo().getLabel());
	}
}
