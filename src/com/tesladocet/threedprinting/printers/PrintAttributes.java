package com.tesladocet.threedprinting.printers;

import java.util.List;

import android.graphics.Color;
import android.util.ArrayMap;

public class PrintAttributes {
	
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
	int uom = UOM_METRIC;
	
	public BuildVolume getVolume() {
		return volume;
	}

	public void setVolume(BuildVolume volume) {
		this.volume = volume;
	}

	public List<Resolution> getResolutions() {
		return resolutions;
	}

	public void addResolution(Resolution res) {
		this.resolutions.add(res);
	}

	public ArrayMap<Material, Color> getExtruders() {
		return extruders;
	}

	public void setExtruders(ArrayMap<Material, Color> extruders) {
		this.extruders = extruders;
	}

	public static class Material {
		String id, label;
		
		public Material(String id, String label) {
			this.id = id;
			this.label = label;
		}
	}
	
	public static class Resolution {
		float x,y,z;
		
		public Resolution(float x, float y, float z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}
	
	public static class BuildVolume {
		float x,y,z;
		
		public BuildVolume(float x, float y, float z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}
	}
}
