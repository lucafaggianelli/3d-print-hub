package com.tesladocet.threedprinting.printers;

import java.util.List;

import android.graphics.Color;
import android.util.ArrayMap;

public class PrinterOptions {
	
	public final static int UOM_METRIC = 0;
	public final static int UOM_INCHES = 1;
	
	public final static Material ABS = new Material("abs", "ABS");
	public final static Material PLA = new Material("pla", "PLA");
	public final static Material TEFLON = new Material("tef", "Teflon");
	public final static Material NYLON = new Material("nyl", "Nylon");
	public final static Material METAL = new Material("met", "Metal");
	
	// Options
	BuildVolume volume;
	List<Resolution> resolutions;
	ArrayMap<Material,Color> extruders;
	
	public static class Material {
		String id, label;
		
		public Material(String id, String label) {
			this.id = id;
			this.label = label;
		}
	}
	
	public class Resolution {
		float x,y,z;
		
		public Resolution(float x, float y, float z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}
	
	public class BuildVolume {
		float x,y,z;
		
		public BuildVolume(float x, float y, float z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}
}
