package com.tesladocet.threedprinting.plugins;

import com.tesladocet.threedprinting.printers.PrintAttributes;
import com.tesladocet.threedprinting.printers.PrintAttributes.BuildVolume;
import com.tesladocet.threedprinting.printers.PrintAttributes.Resolution;
import com.tesladocet.threedprinting.printers.Printer3D;

public class Reprap extends Printer3D {

	public Reprap(String id, String name) {
		super(id, name);
	}

	private void init() {
		PrintAttributes attr = new PrintAttributes();
		
		attr.setVolume(new BuildVolume(100, 100, 100));
		attr.addResolution(new Resolution(20, 20, 50));
		attr.addResolution(new Resolution(20, 20, 100));
		attr.addResolution(new Resolution(20, 20, 200));
		
		setAttributes(attr);
	}
}
