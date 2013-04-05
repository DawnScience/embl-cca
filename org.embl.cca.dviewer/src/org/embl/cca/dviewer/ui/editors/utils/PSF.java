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

import java.util.Arrays;
import java.util.Vector;

import org.eclipse.swt.graphics.Rectangle;
import org.embl.cca.utils.imageviewer.RangeWithValuesFFV;
import org.embl.cca.utils.sorting.ArrayAndIndexBI;
import org.embl.cca.utils.sorting.ArrayAndIndexDI;
import org.embl.cca.utils.sorting.ArrayAndIndexFI;
import org.embl.cca.utils.sorting.ArrayAndIndexII;
import org.embl.cca.utils.sorting.ArrayAndIndexLI;
import org.embl.cca.utils.sorting.ArrayAndIndexSI;
import org.embl.cca.utils.sorting.QuickSort;
import org.slf4j.Logger;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ByteDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.FloatDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
import uk.ac.diamond.scisoft.analysis.dataset.LongDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ShortDataset;

/**
 * The <code>PSF</code> class is for applying a PSF effect to an image.
 * <p>
 *
 * @author  Gábor Náray
 * @version 1.10 18/07/2012
 * @since   20120718
 */
public class PSF {
	public static final String featureName = "PHA";
	public static final String featureFullName = "Point Highlighting Alghorithm";
	public static final String featureIdentifierName = "pha";

	protected int radius;
	protected double minValue;
	protected float kernel[][];
	protected int kernelCenter;

	public PSF() {
		this( 0, 0 );
	}

	public PSF( int radius, double minValue ) {
		kernel = new float[0][0]; //Creating an empty kernel so we can synchronize with it
		setRadius( radius );
		setMinValue( minValue );
	}

	public void setRadius( int radius ) {
		synchronized (kernel) {
			if( this.radius != radius ) {
				this.radius = radius;
				calculateGaussKernel();
			}
		}
	}

	public int getRadius() {
		return radius;
	}

	public void setMinValue( double minValue ) {
		synchronized (kernel) {
			if( this.minValue != minValue ) {
				this.minValue = minValue;
			}
		}
	}

	public double getMinValue() {
		return minValue;
	}

	//	protected void calculateOrbKernel() {
	//		final double r2 = Math.pow(radius, 2);
	//		final int kernelLength = radius * 2 + 1 - 2; // Two edges are full 0
	//		kernelCenter = (kernelLength - 1) / 2;
	//		kernel = new float[kernelLength][kernelLength];
	//		for (int j = 0; j < kernelLength; j++) {
	//			double oneMinusj2 = 1 - Math.pow(j - kernelCenter, 2) / r2;
	//			for (int i = 0; i < kernelLength; i++) {
	//				kernel[j][i] = (int) (Math.sqrt(oneMinusj2
	//						- Math.pow(i - kernelCenter, 2) / r2));
	//			}
	//		}
	//	}
	//
	//	protected void calculateGaussLikeKernel() {
	//		final int kernelLength = 9;
	//		kernelCenter = (kernelLength - 1) / 2;
	//		kernel = new float[][] { { 7/99, 10/99, 15/99, 18/99, 20/99, 18/99, 15/99, 10/99, 7 },
	//				{ 10/99, 16/99, 25/99, 35/99, 40/99, 35/99, 25/99, 16/99, 10 },
	//				{ 15/99, 25/99, 45/99, 70/99, 80/99, 70/99, 45/99, 25/99, 15 },
	//				{ 18/99, 35/99, 70/99, 85/99, 95/99, 85/99, 70/99, 35/99, 18 },
	//				{ 20/99, 40/99, 80/99, 95/99, 99/99, 95/99, 80/99, 40/99, 20 },
	//				{ 18/99, 35/99, 70/99, 85/99, 95/99, 85/99, 70/99, 35/99, 18 },
	//				{ 15/99, 25/99, 45/99, 70/99, 80/99, 70/99, 45/99, 25/99, 15 },
	//				{ 10/99, 16/99, 25/99, 35/99, 40/99, 35/99, 25/99, 16/99, 10 },
	//				{ 7/99, 10/99, 15/99, 18/99, 20/99, 18/99, 15/99, 10/99, 7 } };
	//	}

	protected void calculateGaussKernel() {
		final int kernelLength = radius * 2 + 1;
		kernelCenter = (kernelLength - 1) / 2;
		kernel = new float[kernelLength][kernelLength];
		final double amax = 100; // In equation: 1 / ( c * Math.sqrt( 2*Math.PI )
		final double amin = 1;
		final double b = 0; // mean (mu)
//		final double c = 2.2; // standard deviation (sigma), should depend on radius
		final double c = Math.abs( radius - b ) / Math.sqrt( 2 * Math.log( amax/amin )) ; // standard deviation (sigma), should depend on radius
		final double c2m2 = 2 * Math.pow(c, 2);
		for (int j = 0; j < kernelLength; j++) {
			final double j2 = Math.pow(j - kernelCenter, 2);
			for (int i = 0; i < kernelLength; i++) {
				kernel[j][i] = (float) (amax * Math.pow(
						Math.E,
						-Math.pow(Math.sqrt(Math.pow(i - kernelCenter, 2) + j2)
								- b, 2)
								/ c2m2));
			}
		}
		final float kernelMaxValue = kernel[kernelCenter][kernelCenter]; // center is max
		for (int j = 0; j < kernelLength; j++) {
			for (int i = 0; i < kernelLength; i++) {
				kernel[j][i] /= kernelMaxValue;
			}
		}
	}

	/**
	 * Searches the specified bins vector for the bin (range of keys)
	 * containing the specified key using the binary search algorithm. The
	 * vector must be sorted into ascending order prior to making this call.
	 * If it is not sorted, the results are undefined. If the vector contains
	 * multiple elements containing the specified key, there is no guarantee
	 * which one will be found.
	 *
	 * <p>This method runs in log(n) time for a "random access" vector (which
	 * provides near-constant-time positional access).
	 *
	 * @param  bins the bins vector to be searched.
	 * @param  key the value of which bin to be searched for.
	 * @return the index of the search key, if it is contained in the vector;
	 *	       otherwise, <tt>(-(<i>insertion point</i>) - 1)</tt>.  The
	 *	       <i>insertion point</i> is defined as the point at which the
	 *	       key would be inserted into the vector: the index of the first
	 *	       element greater than the key, or <tt>vector.size()</tt> if all
	 *	       elements in the vector are less than the specified key.  Note
	 *	       that this guarantees that the return value will be &gt;= 0 if
	 *	       and only if the key is found.
	 */
	public static int searchBin( Vector<RangeWithValuesFFV<Float>> bins, float key ) {
		int low = 0;
		int high = bins.size() - 1;
		while (low <= high) {
			int mid = (low + high) >>> 1;
			if( key < bins.get(mid).rangeStart )
				high = mid - 1;
			else {
				if( key >= bins.get(mid).rangeEnd )
					low = mid + 1;
				else
					return mid; // key found
			}
		}
		return -(low + 1);  // key not found
	}

	protected int getTopAmountGoal( int pointAmount ) {
//		final int topAmountIdeal = (int) (14000000 / Math.pow( (float)radius * ( 5f / ( radius + 1 ) + 1 ), 2 )); //Value and formula by experience
		final int topAmountIdeal = (int) (pointAmount * 0.1f ); //Value and formula by experience
		final int topAmountGoal = Math.min( topAmountIdeal, pointAmount );
		return topAmountGoal;
	}

	/**
	 * Get the points to apply PSF for, for a sub <code>Rectangle</code> of byte dataset.
	 * 
	 * @param dataSet
	 * @param rect
	 * @return The points to apply PSF for
	 */
	protected Point[] getPSFPoints(ByteDataset dataSet, Rectangle rect) {
		final Rectangle imageRect = new Rectangle(0, 0, dataSet.getShape()[1], dataSet.getShape()[0]);
		final Rectangle constrained = imageRect.intersection(rect);
		final byte[] imageValues = dataSet.getData();
		final byte minValueHere = minValue < Byte.MIN_VALUE ? Byte.MIN_VALUE : (minValue > Byte.MAX_VALUE ? Byte.MAX_VALUE : (byte)minValue);
		final byte[] rectData = new byte[ constrained.width * constrained.height ];
		final byte[] sortedData;
		final byte topFromValue;
		final byte highlightValueMin;
		byte val;

		final Logger logger = org.slf4j.LoggerFactory.getLogger(PSF.class);
		int iMax = constrained.x + constrained.width;
		int jMax = constrained.y + constrained.height;
		int d = 0;
		int s = constrained.y * imageRect.width + constrained.x;
		long t0 = System.nanoTime();
		//Copying data of selected rectangle
		for( int j = constrained.y; j < jMax; j++ ) {
			int sj = s;
			for( int i = constrained.x; i < iMax; i++ ) {
				rectData[ d++ ] = imageValues[ s++ ];
			}
			s = sj + imageRect.width;
		}

		long t1 = System.nanoTime();
		//		logger.debug( "cut rect.dt [msec]= " + ( t1 - t0 ) / 1000000 ); //around 37 msec
		int topAmount = getTopAmountGoal( rectData.length ); //The top topAmountGoal of points will be PSF-ed
//		int topFrom = rectData.length - topAmount;
//		sortedData = QuickSort.sortTop( rectData, topFrom );
		ArrayAndIndexBI result = QuickSort.sortFromValue( rectData, minValueHere );
		sortedData = result.getArray();
		int topFrom = result.getIndex();
		long t2 = System.nanoTime();
		logger.debug( "QuickSort.dt [msec]= " + ( t2 - t1 ) / 1000000 ); //around 760 msec
		topFromValue = sortedData[ topFrom ]; 
		//Find first other than value@topFrom, because there can be more value@topFrom below topFrom we do not count
		while( topFrom < rectData.length && sortedData[ topFrom ] == topFromValue )
			topFrom++;
		topAmount = rectData.length - topFrom;

		for( int i = topFrom + 1; i < sortedData.length; i++ )
			if( sortedData[ i - 1 ] > sortedData[ i ] )
				System.out.println( "QuickSort failure!" );

		PointWithValueIIB[] psfPoints = new PointWithValueIIB[ topAmount ];
		if( topAmount > 0 ) {
			highlightValueMin = sortedData[ topFrom ];
			// Searching for the values >= highlightValueMin to be highlighted by PSF
			int iH = 0;
			iMax = constrained.width;
			jMax = constrained.height;
			for( int j = 0; j < jMax; j++ ) {
				int xyOffset = (constrained.y + j) * imageRect.width + constrained.x; 
				for( int i = 0; i < iMax; i++ ) {
					val = imageValues[ xyOffset++ ];
					if( val >= highlightValueMin ) {
						//						try {
						psfPoints[ iH++ ] = new PointWithValueIIB( i, j, val );
						//						} catch( Exception e ) {
						//							int aaa = 0;
						//						}
					}
				}
			}
		}
		long t3 = System.nanoTime();
		//		logger.debug( "histograming.dt [msec]= " + ( t3 - t2 ) / 1000000 ); //around 153 msec
		Arrays.sort( psfPoints );
		return psfPoints;
	}

	/**
	 * Get the points to apply PSF for, for a sub <code>Rectangle</code> of short dataset.
	 * 
	 * @param dataSet
	 * @param rect
	 * @return The points to apply PSF for
	 */
	protected Point[] getPSFPoints(ShortDataset dataSet, Rectangle rect) {
		final Rectangle imageRect = new Rectangle(0, 0, dataSet.getShape()[1], dataSet.getShape()[0]);
		final Rectangle constrained = imageRect.intersection(rect);
		final short[] imageValues = dataSet.getData();
		final short minValueHere = minValue < Short.MIN_VALUE ? Short.MIN_VALUE : (minValue > Short.MAX_VALUE ? Short.MAX_VALUE : (short)minValue);
		final short[] rectData = new short[ constrained.width * constrained.height ];
		final short[] sortedData;
		final short topFromValue;
		final short highlightValueMin;
		short val;

		final Logger logger = org.slf4j.LoggerFactory.getLogger(PSF.class);
		int iMax = constrained.x + constrained.width;
		int jMax = constrained.y + constrained.height;
		int d = 0;
		int s = constrained.y * imageRect.width + constrained.x;
		long t0 = System.nanoTime();
		//Copying data of selected rectangle
		for( int j = constrained.y; j < jMax; j++ ) {
			int sj = s;
			for( int i = constrained.x; i < iMax; i++ ) {
				rectData[ d++ ] = imageValues[ s++ ];
			}
			s = sj + imageRect.width;
		}

		long t1 = System.nanoTime();
		//		logger.debug( "cut rect.dt [msec]= " + ( t1 - t0 ) / 1000000 ); //around 37 msec
		int topAmount = getTopAmountGoal( rectData.length ); //The top topAmountGoal of points will be PSF-ed
//		int topFrom = rectData.length - topAmount;
//		sortedData = QuickSort.sortTop( rectData, topFrom );
		ArrayAndIndexSI result = QuickSort.sortFromValue( rectData, minValueHere );
		sortedData = result.getArray();
		int topFrom = result.getIndex();
		long t2 = System.nanoTime();
		logger.debug( "QuickSort.dt [msec]= " + ( t2 - t1 ) / 1000000 ); //around 760 msec
		topFromValue = sortedData[ topFrom ]; 
		//Find first other than value@topFrom, because there can be more value@topFrom below topFrom we do not count
		while( topFrom < rectData.length && sortedData[ topFrom ] == topFromValue )
			topFrom++;
		topAmount = rectData.length - topFrom;

		for( int i = topFrom + 1; i < sortedData.length; i++ )
			if( sortedData[ i - 1 ] > sortedData[ i ] )
				System.out.println( "QuickSort failure!" );

		PointWithValueIIS[] psfPoints = new PointWithValueIIS[ topAmount ];
		if( topAmount > 0 ) {
			highlightValueMin = sortedData[ topFrom ];
			// Searching for the values >= highlightValueMin to be highlighted by PSF
			int iH = 0;
			iMax = constrained.width;
			jMax = constrained.height;
			for( int j = 0; j < jMax; j++ ) {
				int xyOffset = (constrained.y + j) * imageRect.width + constrained.x; 
				for( int i = 0; i < iMax; i++ ) {
					val = imageValues[ xyOffset++ ];
					if( val >= highlightValueMin ) {
						//						try {
						psfPoints[ iH++ ] = new PointWithValueIIS( i, j, val );
						//						} catch( Exception e ) {
						//							int aaa = 0;
						//						}
					}
				}
			}
		}
		long t3 = System.nanoTime();
		//		logger.debug( "histograming.dt [msec]= " + ( t3 - t2 ) / 1000000 ); //around 153 msec
		Arrays.sort( psfPoints );
		return psfPoints;
	}

	/**
	 * Get the points to apply PSF for, for a sub <code>Rectangle</code> of int dataset.
	 * 
	 * @param dataSet
	 * @param rect
	 * @return The points to apply PSF for
	 */
	protected Point[] getPSFPoints(IntegerDataset dataSet, Rectangle rect) {
		final Rectangle imageRect = new Rectangle(0, 0, dataSet.getShape()[1], dataSet.getShape()[0]);
		final Rectangle constrained = imageRect.intersection(rect);
		final int[] imageValues = dataSet.getData();
		final int minValueHere = minValue < Integer.MIN_VALUE ? Integer.MIN_VALUE : (minValue > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)minValue);
		final int[] rectData = new int[ constrained.width * constrained.height ];
		final int[] sortedData;
		final int topFromValue;
		final int highlightValueMin;
		int val;

		final Logger logger = org.slf4j.LoggerFactory.getLogger(PSF.class);
		int iMax = constrained.x + constrained.width;
		int jMax = constrained.y + constrained.height;
		int d = 0;
		int s = constrained.y * imageRect.width + constrained.x;
		long t0 = System.nanoTime();
		//Copying data of selected rectangle
		for( int j = constrained.y; j < jMax; j++ ) {
			int sj = s;
			for( int i = constrained.x; i < iMax; i++ ) {
				rectData[ d++ ] = imageValues[ s++ ];
			}
			s = sj + imageRect.width;
		}

		long t1 = System.nanoTime();
		//		logger.debug( "cut rect.dt [msec]= " + ( t1 - t0 ) / 1000000 ); //around 37 msec
		int topAmount = getTopAmountGoal( rectData.length ); //The top topAmountGoal of points will be PSF-ed
//		int topFrom = rectData.length - topAmount;
//		sortedData = QuickSort.sortTop( rectData, topFrom );
		ArrayAndIndexII result = QuickSort.sortFromValue( rectData, minValueHere );
		sortedData = result.getArray();
		long t2 = System.nanoTime();
		logger.debug( "QuickSort.dt [msec]= " + ( t2 - t1 ) / 1000000 ); //around 760 msec
		int topFrom = result.getIndex();
		topFromValue = sortedData[ topFrom ]; 
		//Find first other than value@topFrom, because there can be more value@topFrom below topFrom we do not count
		while( topFrom < rectData.length && sortedData[ topFrom ] == topFromValue )
			topFrom++;
		topAmount = rectData.length - topFrom;

		for( int i = topFrom + 1; i < sortedData.length; i++ )
			if( sortedData[ i - 1 ] > sortedData[ i ] )
				System.out.println( "QuickSort failure!" );

		PointWithValueIII[] psfPoints = new PointWithValueIII[ topAmount ];
		if( topAmount > 0 ) {
			highlightValueMin = sortedData[ topFrom ];
			// Searching for the values >= highlightValueMin to be highlighted by PSF
			int iH = 0;
			iMax = constrained.width;
			jMax = constrained.height;
			for( int j = 0; j < jMax; j++ ) {
				int xyOffset = (constrained.y + j) * imageRect.width + constrained.x; 
				for( int i = 0; i < iMax; i++ ) {
					val = imageValues[ xyOffset++ ];
					if( val >= highlightValueMin ) {
						//						try {
						psfPoints[ iH++ ] = new PointWithValueIII( i, j, val );
						//						} catch( Exception e ) {
						//							int aaa = 0;
						//						}
					}
				}
			}
		}
		long t3 = System.nanoTime();
		//		logger.debug( "histograming.dt [msec]= " + ( t3 - t2 ) / 1000000 ); //around 153 msec
		Arrays.sort( psfPoints );
		return psfPoints;
	}

	/**
	 * Get the points to apply PSF for, for a sub <code>Rectangle</code> of long dataset.
	 * 
	 * @param dataSet
	 * @param rect
	 * @return The points to apply PSF for
	 */
	protected Point[] getPSFPoints(LongDataset dataSet, Rectangle rect) {
		final Rectangle imageRect = new Rectangle(0, 0, dataSet.getShape()[1], dataSet.getShape()[0]);
		final Rectangle constrained = imageRect.intersection(rect);
		final long[] imageValues = dataSet.getData();
		final long minValueHere = minValue < Long.MIN_VALUE ? Long.MIN_VALUE : (minValue > Long.MAX_VALUE ? Long.MAX_VALUE : (long)minValue);
		final long[] rectData = new long[ constrained.width * constrained.height ];
		final long[] sortedData;
		final long topFromValue;
		final long highlightValueMin;
		long val;

		final Logger logger = org.slf4j.LoggerFactory.getLogger(PSF.class);
		int iMax = constrained.x + constrained.width;
		int jMax = constrained.y + constrained.height;
		int d = 0;
		int s = constrained.y * imageRect.width + constrained.x;
		long t0 = System.nanoTime();
		//Copying data of selected rectangle
		for( int j = constrained.y; j < jMax; j++ ) {
			int sj = s;
			for( int i = constrained.x; i < iMax; i++ ) {
				rectData[ d++ ] = imageValues[ s++ ];
			}
			s = sj + imageRect.width;
		}

		long t1 = System.nanoTime();
		//		logger.debug( "cut rect.dt [msec]= " + ( t1 - t0 ) / 1000000 ); //around 37 msec
		int topAmount = getTopAmountGoal( rectData.length ); //The top topAmountGoal of points will be PSF-ed
//		int topFrom = rectData.length - topAmount;
//		sortedData = QuickSort.sortTop( rectData, topFrom );
		ArrayAndIndexLI result = QuickSort.sortFromValue( rectData, minValueHere );
		sortedData = result.getArray();
		int topFrom = result.getIndex();
		long t2 = System.nanoTime();
		logger.debug( "QuickSort.dt [msec]= " + ( t2 - t1 ) / 1000000 ); //around 760 msec
		topFromValue = sortedData[ topFrom ]; 
		//Find first other than value@topFrom, because there can be more value@topFrom below topFrom we do not count
		while( topFrom < rectData.length && sortedData[ topFrom ] == topFromValue )
			topFrom++;
		topAmount = rectData.length - topFrom;

		for( int i = topFrom + 1; i < sortedData.length; i++ )
			if( sortedData[ i - 1 ] > sortedData[ i ] )
				System.out.println( "QuickSort failure!" );

		PointWithValueIIL[] psfPoints = new PointWithValueIIL[ topAmount ];
		if( topAmount > 0 ) {
			highlightValueMin = sortedData[ topFrom ];
			// Searching for the values >= highlightValueMin to be highlighted by PSF
			int iH = 0;
			iMax = constrained.width;
			jMax = constrained.height;
			for( int j = 0; j < jMax; j++ ) {
				int xyOffset = (constrained.y + j) * imageRect.width + constrained.x; 
				for( int i = 0; i < iMax; i++ ) {
					val = imageValues[ xyOffset++ ];
					if( val >= highlightValueMin ) {
						//						try {
						psfPoints[ iH++ ] = new PointWithValueIIL( i, j, val );
						//						} catch( Exception e ) {
						//							int aaa = 0;
						//						}
					}
				}
			}
		}
		long t3 = System.nanoTime();
		//		logger.debug( "histograming.dt [msec]= " + ( t3 - t2 ) / 1000000 ); //around 153 msec
		Arrays.sort( psfPoints );
		return psfPoints;
	}

	/**
	 * Get the points to apply PSF for, for a sub <code>Rectangle</code> of float set.
	 * 
	 * @param dataSet
	 * @param rect
	 * @return The points to apply PSF for
	 */
	protected Point[] getPSFPoints(FloatDataset dataSet, Rectangle rect) {
		final Rectangle imageRect = new Rectangle(0, 0, dataSet.getShape()[1], dataSet.getShape()[0]);
		final Rectangle constrained = imageRect.intersection(rect);
		final float[] imageValues = dataSet.getData();
		final float minValueHere = minValue < Float.MIN_VALUE ? Float.MIN_VALUE : (minValue > Float.MAX_VALUE ? Float.MAX_VALUE : (float)minValue);
		final float[] rectData = new float[ constrained.width * constrained.height ];
		final float[] sortedData;
		final float topFromValue;
		final float highlightValueMin;
		float val;

		final Logger logger = org.slf4j.LoggerFactory.getLogger(PSF.class);
		int iMax = constrained.x + constrained.width;
		int jMax = constrained.y + constrained.height;
		int d = 0;
		int s = constrained.y * imageRect.width + constrained.x;
		long t0 = System.nanoTime();
		//Copying data of selected rectangle
		for( int j = constrained.y; j < jMax; j++ ) {
			int sj = s;
			for( int i = constrained.x; i < iMax; i++ ) {
				rectData[ d++ ] = imageValues[ s++ ];
			}
			s = sj + imageRect.width;
		}

		long t1 = System.nanoTime();
		//		logger.debug( "cut rect.dt [msec]= " + ( t1 - t0 ) / 1000000 ); //around 37 msec
		int topAmount = getTopAmountGoal( rectData.length ); //The top topAmountGoal of points will be PSF-ed
//		int topFrom = rectData.length - topAmount;
//		sortedData = QuickSort.sortTop( rectData, topFrom );
		logger.debug( "getPSFPoints@float minValueHere=" + minValueHere);
		ArrayAndIndexFI result = QuickSort.sortFromValue( rectData, minValueHere );
		sortedData = result.getArray();
		int topFrom = result.getIndex();
		long t2 = System.nanoTime();
		logger.debug( "QuickSort.dt [msec]= " + ( t2 - t1 ) / 1000000 ); //around 760 msec
		topFromValue = sortedData[ topFrom ]; 
		//Find first other than value@topFrom, because there can be more value@topFrom below topFrom we do not count
		while( topFrom < rectData.length && sortedData[ topFrom ] == topFromValue )
			topFrom++;
		topAmount = rectData.length - topFrom;

		for( int i = topFrom + 1; i < sortedData.length; i++ )
			if( sortedData[ i - 1 ] > sortedData[ i ] )
				System.out.println( "QuickSort failure!" );

		PointWithValueIIF[] psfPoints = new PointWithValueIIF[ topAmount ];
		if( topAmount > 0 ) {
			highlightValueMin = sortedData[ topFrom ];
			// Searching for the values >= highlightValueMin to be highlighted by PSF
			int iH = 0;
			iMax = constrained.width;
			jMax = constrained.height;
			for( int j = 0; j < jMax; j++ ) {
				int xyOffset = (constrained.y + j) * imageRect.width + constrained.x; 
				for( int i = 0; i < iMax; i++ ) {
					val = imageValues[ xyOffset++ ];
					if( val >= highlightValueMin ) {
						//						try {
						psfPoints[ iH++ ] = new PointWithValueIIF( i, j, val );
						//						} catch( Exception e ) {
						//							int aaa = 0;
						//						}
					}
				}
			}
		}
		long t3 = System.nanoTime();
		//		logger.debug( "histograming.dt [msec]= " + ( t3 - t2 ) / 1000000 ); //around 153 msec
		Arrays.sort( psfPoints );
		return psfPoints;
	}

	/**
	 * Get the points to apply PSF for, for a sub <code>Rectangle</code> of double dataset.
	 * 
	 * @param dataSet
	 * @param rect
	 * @return The points to apply PSF for
	 */
	protected Point[] getPSFPoints(DoubleDataset dataSet, Rectangle rect) {
		final Rectangle imageRect = new Rectangle(0, 0, dataSet.getShape()[1], dataSet.getShape()[0]);
		final Rectangle constrained = imageRect.intersection(rect);
		final double[] imageValues = dataSet.getData();
		final double minValueHere = minValue;
		final double[] rectData = new double[ constrained.width * constrained.height ];
		final double[] sortedData;
		final double topFromValue;
		final double highlightValueMin;
		double val;

		final Logger logger = org.slf4j.LoggerFactory.getLogger(PSF.class);
		int iMax = constrained.x + constrained.width;
		int jMax = constrained.y + constrained.height;
		int d = 0;
		int s = constrained.y * imageRect.width + constrained.x;
		long t0 = System.nanoTime();
		//Copying data of selected rectangle
		for( int j = constrained.y; j < jMax; j++ ) {
			int sj = s;
			for( int i = constrained.x; i < iMax; i++ ) {
				rectData[ d++ ] = imageValues[ s++ ];
			}
			s = sj + imageRect.width;
		}

		long t1 = System.nanoTime();
		//		logger.debug( "cut rect.dt [msec]= " + ( t1 - t0 ) / 1000000 ); //around 37 msec
		int topAmount = getTopAmountGoal( rectData.length ); //The top topAmountGoal of points will be PSF-ed
//		int topFrom = rectData.length - topAmount;
//		sortedData = QuickSort.sortTop( rectData, topFrom );
		ArrayAndIndexDI result = QuickSort.sortFromValue( rectData, minValueHere );
		sortedData = result.getArray();
		int topFrom = result.getIndex();
		long t2 = System.nanoTime();
		logger.debug( "QuickSort.dt [msec]= " + ( t2 - t1 ) / 1000000 ); //around 760 msec
		topFromValue = sortedData[ topFrom ]; 
		//Find first other than value@topFrom, because there can be more value@topFrom below topFrom we do not count
		while( topFrom < rectData.length && sortedData[ topFrom ] == topFromValue )
			topFrom++;
		topAmount = rectData.length - topFrom;

		for( int i = topFrom + 1; i < sortedData.length; i++ )
			if( sortedData[ i - 1 ] > sortedData[ i ] )
				System.out.println( "QuickSort failure!" );

		PointWithValueIID[] psfPoints = new PointWithValueIID[ topAmount ];
		if( topAmount > 0 ) {
			highlightValueMin = sortedData[ topFrom ];
			// Searching for the values >= highlightValueMin to be highlighted by PSF
			int iH = 0;
			iMax = constrained.width;
			jMax = constrained.height;
			for( int j = 0; j < jMax; j++ ) {
				int xyOffset = (constrained.y + j) * imageRect.width + constrained.x; 
				for( int i = 0; i < iMax; i++ ) {
					val = imageValues[ xyOffset++ ];
					if( val >= highlightValueMin ) {
						//						try {
						psfPoints[ iH++ ] = new PointWithValueIID( i, j, val );
						//						} catch( Exception e ) {
						//							int aaa = 0;
						//						}
					}
				}
			}
		}
		long t3 = System.nanoTime();
		//		logger.debug( "histograming.dt [msec]= " + ( t3 - t2 ) / 1000000 ); //around 153 msec
		Arrays.sort( psfPoints );
		return psfPoints;
	}

	public void applyPSF(AbstractDataset imageComponent, Rectangle imageRect) {
		long t0 = System.nanoTime();
		switch( imageComponent.getDtype() ) {
		case AbstractDataset.INT8:
			applyPSF((ByteDataset)imageComponent, imageRect);
			break;
		case AbstractDataset.INT16:
			applyPSF((ShortDataset)imageComponent, imageRect);
			break;
		case AbstractDataset.INT32:
			applyPSF((IntegerDataset)imageComponent, imageRect);
			break;
		case AbstractDataset.INT64:
			applyPSF((LongDataset)imageComponent, imageRect);
			break;
		case AbstractDataset.FLOAT32:
			applyPSF((FloatDataset)imageComponent, imageRect);
			break;
		case AbstractDataset.FLOAT64:
			applyPSF((DoubleDataset)imageComponent, imageRect);
			break;
		default:
			throw new RuntimeException( PSF.featureName + " is not implemented for this image containing values of class " + imageComponent.elementClass().toString() );
		}
		long t1 = System.nanoTime();
		System.out.println( "DEBUG: applyPSF took [msec]= " + ( t1 - t0 ) / 1000000 );
	}

	public void applyPSF(ByteDataset dataSet, Rectangle imageRect) {
		synchronized (kernel) {
			byte[] imageValues = dataSet.getData();
			byte valueIJ;
			byte valueMiddle;
			do {
				Point[] PSFPoints = getPSFPoints(dataSet, imageRect);
				if( PSFPoints.length == 0 )
					break;
				for( int k = 0; k < PSFPoints.length; k++ ) {
					int x = PSFPoints[ k ].x; 
					int y = PSFPoints[ k ].y;
					valueMiddle = imageValues[ y * imageRect.width + x ]; //middle
					int yMin = Math.max( y - kernelCenter, 0);
					int yMax = Math.min( y + kernelCenter, imageRect.height - 1 );
					int xMin = Math.max( x - kernelCenter, 0);
					int xMax = Math.min( x + kernelCenter, imageRect.width - 1 );
					int topLeftOffset = yMin * imageRect.width + xMin;
					int jMin = yMin - y + kernelCenter;
					int jMax = yMax - y + kernelCenter;
					int iMin = xMin - x + kernelCenter;
					int iMax = xMax - x + kernelCenter;
					int iXY = topLeftOffset;
					int dXY = imageRect.width - ( iMax + 1 - iMin );
					for( int j = jMin; j <= jMax; iXY += dXY, j++ ) {
						for( int i = iMin; i <= iMax; iXY++, i++ ) {
							//Ignoring 0 kernelvalue and protecting the gaps (besides this algorithm does no good for negative values)
							if( kernel[j][i] == 0 || imageValues[ iXY ] < 0 )
								continue;
							valueIJ = (byte)(kernel[j][i] * valueMiddle);
							if( imageValues[ iXY ] < valueIJ )
								imageValues[ iXY ] = valueIJ;
						}
					}
				}
			} while( false );
		}
	}

	public void applyPSF(ShortDataset dataSet, Rectangle imageRect) {
		synchronized (kernel) {
			short[] imageValues = dataSet.getData();
			short valueIJ;
			short valueMiddle;
			do {
				Point[] PSFPoints = getPSFPoints(dataSet, imageRect);
				if( PSFPoints.length == 0 )
					break;
				for( int k = 0; k < PSFPoints.length; k++ ) {
					int x = PSFPoints[ k ].x; 
					int y = PSFPoints[ k ].y;
					valueMiddle = imageValues[ y * imageRect.width + x ]; //middle
					int yMin = Math.max( y - kernelCenter, 0);
					int yMax = Math.min( y + kernelCenter, imageRect.height - 1 );
					int xMin = Math.max( x - kernelCenter, 0);
					int xMax = Math.min( x + kernelCenter, imageRect.width - 1 );
					int topLeftOffset = yMin * imageRect.width + xMin;
					int jMin = yMin - y + kernelCenter;
					int jMax = yMax - y + kernelCenter;
					int iMin = xMin - x + kernelCenter;
					int iMax = xMax - x + kernelCenter;
					int iXY = topLeftOffset;
					int dXY = imageRect.width - ( iMax + 1 - iMin );
					for( int j = jMin; j <= jMax; iXY += dXY, j++ ) {
						for( int i = iMin; i <= iMax; iXY++, i++ ) {
							//Ignoring 0 kernelvalue and protecting the gaps (besides this algorithm does no good for negative values)
							if( kernel[j][i] == 0 || imageValues[ iXY ] < 0 )
								continue;
							valueIJ = (short)(kernel[j][i] * valueMiddle);
							if( imageValues[ iXY ] < valueIJ )
								imageValues[ iXY ] = valueIJ;
						}
					}
				}
			} while( false );
		}
	}

	public void applyPSF(IntegerDataset dataSet, Rectangle imageRect) {
		synchronized (kernel) {
			int[] imageValues = dataSet.getData();
			int valueIJ;
			int valueMiddle;
			do {
				Point[] PSFPoints = getPSFPoints(dataSet, imageRect);
				if( PSFPoints.length == 0 )
					break;
				for( int k = 0; k < PSFPoints.length; k++ ) {
					int x = PSFPoints[ k ].x; 
					int y = PSFPoints[ k ].y;
					valueMiddle = imageValues[ y * imageRect.width + x ]; //middle
					int yMin = Math.max( y - kernelCenter, 0);
					int yMax = Math.min( y + kernelCenter, imageRect.height - 1 );
					int xMin = Math.max( x - kernelCenter, 0);
					int xMax = Math.min( x + kernelCenter, imageRect.width - 1 );
					int topLeftOffset = yMin * imageRect.width + xMin;
					int jMin = yMin - y + kernelCenter;
					int jMax = yMax - y + kernelCenter;
					int iMin = xMin - x + kernelCenter;
					int iMax = xMax - x + kernelCenter;
					int iXY = topLeftOffset;
					int dXY = imageRect.width - ( iMax + 1 - iMin );
					for( int j = jMin; j <= jMax; iXY += dXY, j++ ) {
						for( int i = iMin; i <= iMax; iXY++, i++ ) {
							//Ignoring 0 kernelvalue and protecting the gaps (besides this algorithm does no good for negative values)
							if( kernel[j][i] == 0 || imageValues[ iXY ] < 0 )
								continue;
							valueIJ = (int)(kernel[j][i] * valueMiddle);
							if( imageValues[ iXY ] < valueIJ )
								imageValues[ iXY ] = valueIJ;
						}
					}
				}
			} while( false );
		}
	}

	public void applyPSF(LongDataset dataSet, Rectangle imageRect) {
		synchronized (kernel) {
			long[] imageValues = dataSet.getData();
			long valueIJ;
			long valueMiddle;
			do {
				Point[] PSFPoints = getPSFPoints(dataSet, imageRect);
				if( PSFPoints.length == 0 )
					break;
				for( int k = 0; k < PSFPoints.length; k++ ) {
					int x = PSFPoints[ k ].x; 
					int y = PSFPoints[ k ].y;
					valueMiddle = imageValues[ y * imageRect.width + x ]; //middle
					int yMin = Math.max( y - kernelCenter, 0);
					int yMax = Math.min( y + kernelCenter, imageRect.height - 1 );
					int xMin = Math.max( x - kernelCenter, 0);
					int xMax = Math.min( x + kernelCenter, imageRect.width - 1 );
					int topLeftOffset = yMin * imageRect.width + xMin;
					int jMin = yMin - y + kernelCenter;
					int jMax = yMax - y + kernelCenter;
					int iMin = xMin - x + kernelCenter;
					int iMax = xMax - x + kernelCenter;
					int iXY = topLeftOffset;
					int dXY = imageRect.width - ( iMax + 1 - iMin );
					for( int j = jMin; j <= jMax; iXY += dXY, j++ ) {
						for( int i = iMin; i <= iMax; iXY++, i++ ) {
							//Ignoring 0 kernelvalue and protecting the gaps (besides this algorithm does no good for negative values)
							if( kernel[j][i] == 0 || imageValues[ iXY ] < 0 )
								continue;
							valueIJ = (long)(kernel[j][i] * valueMiddle);
							if( imageValues[ iXY ] < valueIJ )
								imageValues[ iXY ] = valueIJ;
						}
					}
				}
			} while( false );
		}
	}

	public void applyPSF(FloatDataset dataSet, Rectangle imageRect) {
		/* Since PointWithValueIIF are ordered by value ascending, applying the PSF
		 * in this order can be done in the gotten imageValues, because the smaller
		 * valued point does not disturb the higher valued point. Thus we do not
		 * have to clone the imageValues array, which saves time.
		 */
		synchronized (kernel) {
			float[] imageValues = dataSet.getData();
			float valueIJ;
			float valueMiddle;
			do {
				Point[] PSFPoints = getPSFPoints(dataSet, imageRect);
				if( PSFPoints.length == 0 )
					break;
				for( int k = 0; k < PSFPoints.length; k++ ) {
					int x = PSFPoints[ k ].x; 
					int y = PSFPoints[ k ].y;
					valueMiddle = imageValues[ y * imageRect.width + x ]; //middle
					int yMin = Math.max( y - kernelCenter, 0);
					int yMax = Math.min( y + kernelCenter, imageRect.height - 1 );
					int xMin = Math.max( x - kernelCenter, 0);
					int xMax = Math.min( x + kernelCenter, imageRect.width - 1 );
					int topLeftOffset = yMin * imageRect.width + xMin;
					int jMin = yMin - y + kernelCenter;
					int jMax = yMax - y + kernelCenter;
					int iMin = xMin - x + kernelCenter;
					int iMax = xMax - x + kernelCenter;
					int iXY = topLeftOffset;
					int dXY = imageRect.width - ( iMax + 1 - iMin );
					for( int j = jMin; j <= jMax; iXY += dXY, j++ ) {
						for( int i = iMin; i <= iMax; iXY++, i++ ) {
							//Ignoring 0 kernelvalue and protecting the gaps (besides this algorithm does no good for negative values)
							if( kernel[j][i] == 0 || imageValues[ iXY ] < 0 )
								continue;
							valueIJ = (float)(kernel[j][i] * valueMiddle);
							if( imageValues[ iXY ] < valueIJ )
								imageValues[ iXY ] = valueIJ;
						}
					}
				}
			} while( false );
		}
	}

	public void applyPSF(DoubleDataset dataSet, Rectangle imageRect) {
		synchronized (kernel) {
			double[] imageValues = dataSet.getData();
			double valueIJ;
			double valueMiddle;
			do {
				Point[] PSFPoints = getPSFPoints(dataSet, imageRect);
				if( PSFPoints.length == 0 )
					break;
				for( int k = 0; k < PSFPoints.length; k++ ) {
					int x = PSFPoints[ k ].x; 
					int y = PSFPoints[ k ].y;
					valueMiddle = imageValues[ y * imageRect.width + x ]; //middle
					int yMin = Math.max( y - kernelCenter, 0);
					int yMax = Math.min( y + kernelCenter, imageRect.height - 1 );
					int xMin = Math.max( x - kernelCenter, 0);
					int xMax = Math.min( x + kernelCenter, imageRect.width - 1 );
					int topLeftOffset = yMin * imageRect.width + xMin;
					int jMin = yMin - y + kernelCenter;
					int jMax = yMax - y + kernelCenter;
					int iMin = xMin - x + kernelCenter;
					int iMax = xMax - x + kernelCenter;
					int iXY = topLeftOffset;
					int dXY = imageRect.width - ( iMax + 1 - iMin );
					for( int j = jMin; j <= jMax; iXY += dXY, j++ ) {
						for( int i = iMin; i <= iMax; iXY++, i++ ) {
							//Ignoring 0 kernelvalue and protecting the gaps (besides this algorithm does no good for negative values)
							if( kernel[j][i] == 0 || imageValues[ iXY ] < 0 )
								continue;
							valueIJ = (double)(kernel[j][i] * valueMiddle);
							if( imageValues[ iXY ] < valueIJ )
								imageValues[ iXY ] = valueIJ;
						}
					}
				}
			} while( false );
		}
	}

}
