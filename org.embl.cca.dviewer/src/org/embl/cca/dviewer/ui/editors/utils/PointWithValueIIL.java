/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 

package org.embl.cca.dviewer.ui.editors.utils;

import java.io.Serializable;

/**
 * Instances of this class represent places on the (x, y, z)
 * coordinate plane. The x and y coordinates are type of int,
 * and the z coordinate is type of long. In this class the
 * z coordinate is considered as the value of a function of
 * x and y coordinates. z = f( x, y ).
 * <p>
 * The order of points is defined by the order of z coordinates,
 * when using <code>compareTo</code> method. 
 * </p>
 * <p>
 * The hashCode() method in this class uses the values of the public
 * fields to compute the hash value. When storing instances of the
 * class in hashed collections, do not modify these fields after the
 * object has been inserted.  
 * </p>
 * <p>
 * Application code does <em>not</em> need to explicitly release the
 * resources managed by each instance when those instances are no longer
 * required, and thus no <code>dispose()</code> method is provided.
 * </p>
 *
 * @see Serializable, Comparable
 * 
 * @author  Gábor Náray
 * @version 1.10 18/07/2012
 * @since   20120718
 */

public class PointWithValueIIL extends Point implements Serializable, Comparable<PointWithValueIIL> {
		
	/**
	 * 
	 */
	private static final long serialVersionUID = -2441094983140347120L;

	/**
	 * the z coordinate of the point
	 */
	public long z;
	
	/**
	 * Constructs a new point with the given x, y and z coordinates.
	 *
	 * @param x the x coordinate of the new point
	 * @param y the y coordinate of the new point
	 * @param z the z coordinate of the new point
	 */
	public PointWithValueIIL( int x, int y, long z ) {
		super( x, y );
		this.z = z;
	}

	/**
	 * Compares the argument to the receiver, and returns true
	 * if they represent the <em>same</em> object using a class
	 * specific comparison.
	 *
	 * @param object the object to compare with this object
	 * @return <code>true</code> if the object is the same as this object and <code>false</code> otherwise
	 *
	 * @see #hashCode()
	 */
	public boolean equals(Object object) {
		if (object == this) return true;
		if (!(object instanceof PointWithValueIIL)) return false;
		PointWithValueIIL p = (PointWithValueIIL)object;
		return (p.x == this.x) && (p.y == this.y) && (p.z == this.z);
	}

	/**
	 * Returns an integer hash code for the receiver. Any two 
	 * objects that return <code>true</code> when passed to 
	 * <code>equals</code> must return the same value for this
	 * method.
	 *
	 * @return the receiver's hash
	 *
	 * @see #equals(Object)
	 */
	public int hashCode() {
		return super.hashCode() ^ (int)z;
	}

	/**
	 * Returns a string containing a concise, human-readable
	 * description of the receiver.
	 *
	 * @return a string representation of the point
	 */
	public String toString() {
		return "Point {" + x + ", " + y + ", " + z + "}";
	}

	@Override
	public int compareTo(PointWithValueIIL o) {
		return (int)Math.signum( this.z - o.z );
	}

}
