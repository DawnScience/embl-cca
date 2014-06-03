package org.embl.cca.utils.imageviewer;

import java.io.Serializable;
import java.util.Vector;

import org.embl.cca.utils.general.Util;

/**
 * Instances of this class represent values in a range on the (x)
 * coordinate plane. The x and y coordinates are type of int,
 * and the z coordinate is type of double. In this class the
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
 * @see Rectangle
 * 
 * @author  Gabor Naray
 * @version 1.00 07/12/2011
 * @since   20111207
 */

public class RangeWithValuesFFV<E> implements Serializable, Comparable<E> {
		
	/**
	 * the x coordinate of the point
	 */
	public float rangeStart;
	
	/**
	 * the y coordinate of the point
	 */
	public float rangeEnd;
	
	/**
	 * the values vector of range
	 */
	public Vector<E> values;
	
	static final long serialVersionUID = 3257002163938146354L;
		
	/**
	 * Constructs a new range with the given x1, x2 borders.
	 *
	 * @param x1 the starting x coordinate of new range
	 * @param x2 the ending x coordinate of the new range
	 */
	public RangeWithValuesFFV( float x1, float x2 ) {
		this( x1, x2, 10 );
	}

	/**
	 * Constructs a new range with the given x1, x2 borders and empty values vector with initial capacity.
	 *
	 * @param x1 the starting x coordinate of the new range
	 * @param x2 the ending x coordinate of the new range
	 * @param initialCapacity the initialCapacity of values vector of range
	 */
	public RangeWithValuesFFV( float x1, float x2, int initialCapacity ) {
		this.rangeStart = x1;
		this.rangeEnd = x2;
		this.values = new Vector<E>( initialCapacity );
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
		if (!(object instanceof RangeWithValuesFFV)) return false;
		@SuppressWarnings("unchecked")
		RangeWithValuesFFV<E> p = (RangeWithValuesFFV<E>)object;
		return (p.rangeStart == this.rangeStart) && (p.rangeEnd == this.rangeEnd) && (p.values.containsAll(this.values)) && (this.values.containsAll(p.values));
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
		return Util.hashCode(new Object[] {rangeStart, rangeEnd, values.size()});
	}

	/**
	 * Returns a string containing a concise, human-readable
	 * description of the receiver.
	 *
	 * @return a string representation of the point
	 */
	public String toString() {
		return "Point {" + rangeStart + ", " + rangeEnd + ", " + values.toString() + "}";
	}

	@Override
	public int compareTo(E object) {
		if( object == this ) return 0;
		if( !(object instanceof RangeWithValuesFFV))
			throw new ClassCastException();
		@SuppressWarnings("unchecked")
		RangeWithValuesFFV<E> dest = (RangeWithValuesFFV<E>)object;
		float result = this.rangeStart - dest.rangeStart;
		if( result == 0 )
			result = this.rangeEnd - dest.rangeEnd;
		return (int)Math.signum( result );
	}

}
