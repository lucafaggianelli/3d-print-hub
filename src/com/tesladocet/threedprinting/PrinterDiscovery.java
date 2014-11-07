package com.tesladocet.threedprinting;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable.Creator;
import android.print.PrintAttributes.Margins;
import android.print.PrintAttributes.MediaSize;
import android.print.PrintAttributes.Resolution;
import android.print.PrintAttributes;
import android.print.PrinterCapabilitiesInfo;
import android.print.PrinterId;
import android.print.PrinterInfo;
import android.printservice.PrinterDiscoverySession;
import android.util.Log;

public class PrinterDiscovery extends PrinterDiscoverySession {

	private final static String TAG = "DiscoverySession";
	
	private ThreeDPrintService printService;
	
	public PrinterDiscovery(ThreeDPrintService s) {
		printService = s;
	}
	
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStartPrinterDiscovery(List<PrinterId> prio) {
		for (PrinterId p: prio) {
			Log.d(TAG,"Priority: " + p);
		}
		
		ArrayList<PrinterInfo> printers = new ArrayList<PrinterInfo>();
		PrinterInfo info = discoverPrusa();
		printers.add(info);
		addPrinters(printers);
	}

	@Override
	public void onStartPrinterStateTracking(PrinterId id) {
		PrinterCapabilitiesInfo.Builder caps = new PrinterCapabilitiesInfo.Builder(id);
		PrinterInfo.Builder builder = new PrinterInfo.Builder(id, 
				"my-nice-printer", PrinterInfo.STATUS_IDLE);
		
		caps.addMediaSize(MediaSize.ISO_A4, true);
		caps.addResolution(new Resolution("res1", "Res1", 100, 100), true);
		caps.setColorModes(PrintAttributes.COLOR_MODE_COLOR, PrintAttributes.COLOR_MODE_COLOR);
		caps.setMinMargins(Margins.NO_MARGINS);
		
		// Add printer
		builder.setCapabilities(caps.build());
		List<PrinterInfo> infos = new ArrayList<PrinterInfo>();
        infos.add(builder.build());
        addPrinters(infos);
	}

	@Override
	public void onStopPrinterDiscovery() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopPrinterStateTracking(PrinterId arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onValidatePrinters(List<PrinterId> arg0) {
		// TODO Auto-generated method stub

	}

	public PrinterInfo discoverPrusa() {
		return new PrinterInfo.Builder(printService.generatePrinterId("my-nice-printer"),
				"Prusa i3", PrinterInfo.STATUS_IDLE).build();
	}
}
