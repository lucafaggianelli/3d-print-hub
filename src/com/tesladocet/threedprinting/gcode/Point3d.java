package com.tesladocet.threedprinting.gcode;

public class Point3d {
	public double x;
	public double y;
	public double z;
	
	public Point3d() {
		x = 0;
		y = 0;
		z = 0;
	}
	
	public Point3d(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Point3d(Point3d p) {
		x = p.x;
		y = p.y;
		z = p.z;
	}
	
	public final void scale(double s) {
		x *= s;
		y *= s;
		z *= s;
	}
}