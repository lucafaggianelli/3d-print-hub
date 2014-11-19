package com.tesladocet.threedprinting.gcode;

public class Point3f {
	public float x;
	public float y;
	public float z;
	
	public Point3f() {
		x = 0;
		y = 0;
		z = 0;
	}
	
	public Point3f(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Point3f(Point3f p) {
		x = p.x;
		y = p.y;
		z = p.z;
	}
	
	public final void set(Point3f p) {
		this.x = p.x;
		this.y = p.y;
		this.z = p.z;
    }
	
	public final void scale(float s) {
		x *= s;
		y *= s;
		z *= s;
	}
}