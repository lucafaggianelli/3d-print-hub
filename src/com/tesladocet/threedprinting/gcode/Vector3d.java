package com.tesladocet.threedprinting.gcode;

public class Vector3d extends Point3d {
	
	public Vector3d(double x, double y, double z) {
		super(x, y, z);
	}
	
	/**
	 * Sets this vector to the vector cross product of vectors v1 and v2.
	 * @param v1 the first vector
	 * @param v2 the second vector
	 */
	public final void cross(Vector3d v1, Vector3d v2) {
		double x,y;

		x = v1.y*v2.z - v1.z*v2.y;
		y = v2.x*v1.z - v2.z*v1.x;
		this.z = v1.x*v2.y - v1.y*v2.x;
		this.x = x;
		this.y = y;
	}


	/**
	 * Sets the value of this vector to the normalization of vector v1.
	 * @param v1 the un-normalized vector
	 */
	public final void normalize(Vector3d v1) {
		double norm;

		norm = 1.0/Math.sqrt(v1.x*v1.x + v1.y*v1.y + v1.z*v1.z);
		this.x = v1.x*norm;
		this.y = v1.y*norm;
		this.z = v1.z*norm;
	}


	/**
	 * Normalizes this vector in place.
	 */
	public final void normalize() {
		double norm;

		norm = 1.0/Math.sqrt(this.x*this.x + this.y*this.y + this.z*this.z);
		this.x *= norm;
		this.y *= norm;
		this.z *= norm;
	}


	/**
	 * Returns the dot product of this vector and vector v1.
	 * @param v1 the other vector
	 * @return the dot product of this and v1
	 */
	public final double dot(Vector3d v1) {
		return (this.x*v1.x + this.y*v1.y + this.z*v1.z);
	}


	/**
	 * Returns the squared length of this vector.
	 * @return the squared length of this vector
	 */
	public final double lengthSquared()	{
		return (this.x*this.x + this.y*this.y + this.z*this.z);
	}


	/**
	 * Returns the length of this vector.
	 * @return the length of this vector
	 */
	public final double length() {
		return Math.sqrt(this.x*this.x + this.y*this.y + this.z*this.z);
	}


	/**
	 *   Returns the angle in radians between this vector and the vector
	 *   parameter; the return value is constrained to the range [0,PI].
	 *   @param v1    the other vector
	 *   @return   the angle in radians in the range [0,PI]
	 */
	public final double angle(Vector3d v1) {
		double vDot = this.dot(v1) / ( this.length()*v1.length() );
		if( vDot < -1.0) vDot = -1.0;
		if( vDot >  1.0) vDot =  1.0;
		return((double) (Math.acos( vDot )));
	}
}
