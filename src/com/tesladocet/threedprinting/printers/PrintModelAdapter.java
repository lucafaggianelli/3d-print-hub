package com.tesladocet.threedprinting.printers;

import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;

public class PrintModelAdapter extends PrintDocumentAdapter {

	@Override
	public void onStart() {
		super.onStart();
	}
	
	@Override
	public void onLayout(PrintAttributes oldAttributes,
			PrintAttributes newAttributes,
			CancellationSignal cancellationSignal,
			LayoutResultCallback callback, Bundle extras) {

		// Respond to cancellation request
	    if (cancellationSignal.isCanceled()) {
	        callback.onLayoutCancelled();
	        return;
	    }
	    
	    PrintDocumentInfo info = new PrintDocumentInfo
                .Builder("print_output.pdf")
                .setContentType(PrintDocumentInfo.CONTENT_TYPE_UNKNOWN)
                .setPageCount(1)
                .build();
	    
        // Content layout reflow is complete
        callback.onLayoutFinished(info, true);
	}

	@Override
	public void onWrite(PageRange[] pages, ParcelFileDescriptor destination,
			CancellationSignal cancellationSignal, WriteResultCallback callback) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFinish() {
		super.onFinish();
	}
}