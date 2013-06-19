package org.embl.cca.utils.datahandling;

import org.embl.cca.utils.datahandling.text.StringUtils;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.BooleanDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ByteDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.FloatDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
import uk.ac.diamond.scisoft.analysis.dataset.LongDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ShortDataset;

public class DatasetTypeSeparatedUtils {
	/**
	 * This method does exactly the same as executing splitAddSet and joinSplittedSets each
	 * after for one dataset, but faster. It is useful when working with only
	 * a single dataset, and the conversion of split and join is required.
	 * @param set the dataset to split and join
	 * @param maxValidNumber the maximum valid value. Above this value the values are considered bad. 
	 * @param badNumber the value which means the value is bad. Typically -2 in CBF files.
	 * @param notMeasuredValue the value which means the value is not measured. Typically -1 in CBF files.
	 */
	public static void splitJoinIntoSelf(final AbstractDataset set,
			final Number maxValidNumber, final Number badNumber, final Number notMeasuredNumber) {
		final int type = set.getDtype();
		//In case of BOOL, maxValidNumber, badNumber and notMeasuredNumber have no sense
		final boolean maxValidValueValid = type != AbstractDataset.BOOL && maxValidNumber != null;
		final boolean badValueValid = type != AbstractDataset.BOOL && badNumber != null;
		if( !badValueValid || !maxValidValueValid )
			return;
		switch (type) {
			case AbstractDataset.BOOL: {
				break;
			}
			case AbstractDataset.INT32: {
				final int[] currentData = ((IntegerDataset)set).getData();
				final int maxValidValue = maxValidNumber.intValue();
				final int badValue = badNumber.intValue();
				final int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					if( currentData[ i ] > maxValidValue )
						currentData[i] = badValue;
				}
				break;
			}
			case AbstractDataset.INT8: {
				final byte[] currentData = ((ByteDataset)set).getData();
				final byte maxValidValue = maxValidNumber.byteValue();
				final byte badValue = badNumber.byteValue();
				final int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					if( currentData[ i ] > maxValidValue )
						currentData[i] = badValue;
				}
				break;
			}
			case AbstractDataset.INT16: {
				final short[] currentData = ((ShortDataset)set).getData();
				final short maxValidValue = maxValidNumber.shortValue();
				final short badValue = badNumber.shortValue();
				final int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					if( currentData[ i ] > maxValidValue )
						currentData[i] = badValue;
				}
				break;
			}
			case AbstractDataset.INT64: {
				final long[] currentData = ((LongDataset)set).getData();
				final long maxValidValue = maxValidNumber.longValue();
				final long badValue = badNumber.longValue();
				final int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					if( currentData[ i ] > maxValidValue )
						currentData[i] = badValue;
				}
				break;
			}
			case AbstractDataset.FLOAT32: {
				final float[] currentData = ((FloatDataset)set).getData();
				final float maxValidValue = maxValidNumber.floatValue();
				final float badValue = badNumber.floatValue();
				final int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					if( currentData[ i ] > maxValidValue )
						currentData[i] = badValue;
				}
				break;
			}
			case AbstractDataset.FLOAT64: {
				final double[] currentData = ((DoubleDataset)set).getData();
				final double maxValidValue = maxValidNumber.doubleValue();
				final double badValue = badNumber.doubleValue();
				final int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					if( currentData[ i ] > maxValidValue )
						currentData[i] = badValue;
				}
				break;
			}
			default:
				throw new RuntimeException("Not supported dataset type: " + set.getDtype() );
		}
	}

	/**
	 * This method demultiplexes the set and adds the resulted sets to the summedSet, summedBadMask and summedNotMeasuredMask.
	 * @param set the dataset to add
	 * @param maxValidNumber the maximum valid value. Above this value the values are considered bad. 
	 * @param notMeasuredValue the value which means the value is not measured. Typically -1 in CBF files.
	 */
	public static void splitAddSet(final AbstractDataset set, final AbstractDataset summedSet, final AbstractDataset summedBadMask, final AbstractDataset summedNotMeasuredMask,
			final Number maxValidNumber, final Number badNumber, final Number notMeasuredNumber) {
		final int type = set.getDtype();
		//In case of BOOL, maxValidNumber, badNumber and notMeasuredNumber have no sense
		final boolean maxValidValueValid = type != AbstractDataset.BOOL && maxValidNumber != null;
		final boolean notMeasuredValueValid = type != AbstractDataset.BOOL && notMeasuredNumber != null;
		final boolean badValueValid = type != AbstractDataset.BOOL && badNumber != null;
		switch (type) {
			case AbstractDataset.BOOL: {
				final boolean[] currentData = ((BooleanDataset)set).getData();
				final boolean[] setData = ((BooleanDataset)summedSet).getData();
				final int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					setData[i] |= currentData[i];
				}
				break;
			}
			case AbstractDataset.INT32: {
				final int[] currentData = ((IntegerDataset)set).getData();
				final int[] setData = ((IntegerDataset)summedSet).getData();
				final int[] notMeasuredMaskData = ((IntegerDataset)summedNotMeasuredMask).getData();
				final int[] badMaskData = ((IntegerDataset)summedBadMask).getData();
				final int maxValidValue = maxValidNumber.intValue();
				final int notMeasuredValue = notMeasuredNumber.intValue();
				final int badValue = badNumber.intValue();
				final int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					if( (maxValidValueValid && currentData[ i ] > maxValidValue) || (badValueValid && currentData[ i ] == badValue) )
						badMaskData[ i ]++;
					else if( notMeasuredValueValid && currentData[ i ] == notMeasuredValue )
						notMeasuredMaskData[ i ]++;
					else
						setData[i] += currentData[i];
				}
				break;
			}
			case AbstractDataset.INT8: {
				final byte[] currentData = ((ByteDataset)set).getData();
				final byte[] setData = ((ByteDataset)summedSet).getData();
				final byte[] notMeasuredMaskData = ((ByteDataset)summedNotMeasuredMask).getData();
				final byte[] badMaskData = ((ByteDataset)summedBadMask).getData();
				final byte maxValidValue = maxValidNumber.byteValue();
				final byte notMeasuredValue = notMeasuredNumber.byteValue();
				final byte badValue = badNumber.byteValue();
				final int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					if( (maxValidValueValid && currentData[ i ] > maxValidValue) || (badValueValid && currentData[ i ] == badValue) )
						badMaskData[ i ]++;
					else if( notMeasuredValueValid && currentData[ i ] == notMeasuredValue )
						notMeasuredMaskData[ i ]++;
					else
						setData[i] += currentData[i];
				}
				break;
			}
			case AbstractDataset.INT16: {
				final short[] currentData = ((ShortDataset)set).getData();
				final short[] setData = ((ShortDataset)summedSet).getData();
				final short[] notMeasuredMaskData = ((ShortDataset)summedNotMeasuredMask).getData();
				final short[] badMaskData = ((ShortDataset)summedBadMask).getData();
				final short maxValidValue = maxValidNumber.shortValue();
				final short notMeasuredValue = notMeasuredNumber.shortValue();
				final short badValue = badNumber.shortValue();
				final int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					if( (maxValidValueValid && currentData[ i ] > maxValidValue) || (badValueValid && currentData[ i ] == badValue) )
						badMaskData[ i ]++;
					else if( notMeasuredValueValid && currentData[ i ] == notMeasuredValue )
						notMeasuredMaskData[ i ]++;
					else
						setData[i] += currentData[i];
				}
				break;
			}
			case AbstractDataset.INT64: {
				final long[] currentData = ((LongDataset)set).getData();
				final long[] setData = ((LongDataset)summedSet).getData();
				final long[] notMeasuredMaskData = ((LongDataset)summedNotMeasuredMask).getData();
				final long[] badMaskData = ((LongDataset)summedBadMask).getData();
				final long maxValidValue = maxValidNumber.longValue();
				final long notMeasuredValue = notMeasuredNumber.longValue();
				final long badValue = badNumber.longValue();
				final int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					if( (maxValidValueValid && currentData[ i ] > maxValidValue) || (badValueValid && currentData[ i ] == badValue) )
						badMaskData[ i ]++;
					else if( notMeasuredValueValid && currentData[ i ] == notMeasuredValue )
						notMeasuredMaskData[ i ]++;
					else
						setData[i] += currentData[i];
				}
				break;
			}
			case AbstractDataset.FLOAT32: {
				final float[] currentData = ((FloatDataset)set).getData();
				final float[] setData = ((FloatDataset)summedSet).getData();
				final float[] notMeasuredMaskData = ((FloatDataset)summedNotMeasuredMask).getData();
				final float[] badMaskData = ((FloatDataset)summedBadMask).getData();
				final float maxValidValue = maxValidNumber.floatValue();
				final float notMeasuredValue = notMeasuredNumber.floatValue();
				final float badValue = badNumber.floatValue();
				final int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					if( (maxValidValueValid && currentData[ i ] > maxValidValue) || (badValueValid && currentData[ i ] == badValue) )
						badMaskData[ i ]++;
					else if( notMeasuredValueValid && currentData[ i ] == notMeasuredValue )
						notMeasuredMaskData[ i ]++;
					else
						setData[i] += currentData[i];
				}
				break;
			}
			case AbstractDataset.FLOAT64: {
				final double[] currentData = ((DoubleDataset)set).getData();
				final double[] setData = ((DoubleDataset)summedSet).getData();
				final double[] notMeasuredMaskData = ((DoubleDataset)summedNotMeasuredMask).getData();
				final double[] badMaskData = ((DoubleDataset)summedBadMask).getData();
				final double maxValidValue = maxValidNumber.doubleValue();
				final double notMeasuredValue = notMeasuredNumber.doubleValue();
				final double badValue = badNumber.doubleValue();
				final int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					if( (maxValidValueValid && currentData[ i ] > maxValidValue) || (badValueValid && currentData[ i ] == badValue) )
						badMaskData[ i ]++;
					else if( notMeasuredValueValid && currentData[ i ] == notMeasuredValue )
						notMeasuredMaskData[ i ]++;
					else
						setData[i] += currentData[i];
				}
				break;
			}
			default:
				throw new RuntimeException("Not supported dataset type: " + set.getDtype() );
		}
	}

	/**
	 * This method demultiplexes the set and removes the resulted sets from the summedSet, summedBadMask and summedNotMeasuredMask.
	 * @param set the dataset to substract
	 * @param maxValidNumber the maximum valid value. Above this value the values are considered bad. 
	 * @param notMeasuredValue the value which means the value is not measured. Typically -1 in CBF files.
	 */
	public static void splitRemoveSet(final AbstractDataset set, final AbstractDataset summedSet, final AbstractDataset summedBadMask, final AbstractDataset summedNotMeasuredMask,
			final Number maxValidNumber, final Number badNumber, final Number notMeasuredNumber) {
		final int type = set.getDtype();
		//In case of BOOL, maxValidNumber and notMeasuredNumber have no sense
		final boolean maxValidValueValid = type != AbstractDataset.BOOL && maxValidNumber != null;
		final boolean notMeasuredValueValid = type != AbstractDataset.BOOL && notMeasuredNumber != null;
		final boolean badValueValid = type != AbstractDataset.BOOL && badNumber != null;
		switch (type) {
			case AbstractDataset.BOOL: {
				final boolean[] currentData = ((BooleanDataset)set).getData();
				final boolean[] setData = ((BooleanDataset)summedSet).getData();
				final int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					setData[i] &= !currentData[i];
				}
				break;
			}
			case AbstractDataset.INT32: {
				final int[] currentData = ((IntegerDataset)set).getData();
				final int[] setData = ((IntegerDataset)summedSet).getData();
				final int[] notMeasuredMaskData = ((IntegerDataset)summedNotMeasuredMask).getData();
				final int[] badMaskData = ((IntegerDataset)summedBadMask).getData();
				final int maxValidValue = maxValidNumber.intValue();
				final int notMeasuredValue = notMeasuredNumber.intValue();
				final int badValue = badNumber.intValue();
				final int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					if( (maxValidValueValid && currentData[ i ] > maxValidValue) || (badValueValid && currentData[ i ] == badValue) )
						badMaskData[ i ]--;
					else if( notMeasuredValueValid && currentData[ i ] == notMeasuredValue )
						notMeasuredMaskData[ i ]--;
					else
						setData[i] -= currentData[i];
				}
				break;
			}
			case AbstractDataset.INT8: {
				final byte[] currentData = ((ByteDataset)set).getData();
				final byte[] setData = ((ByteDataset)summedSet).getData();
				final byte[] notMeasuredMaskData = ((ByteDataset)summedNotMeasuredMask).getData();
				final byte[] badMaskData = ((ByteDataset)summedBadMask).getData();
				final byte maxValidValue = maxValidNumber.byteValue();
				final byte notMeasuredValue = notMeasuredNumber.byteValue();
				final byte badValue = badNumber.byteValue();
				final int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					if( (maxValidValueValid && currentData[ i ] > maxValidValue) || (badValueValid && currentData[ i ] == badValue) )
						badMaskData[ i ]--;
					else if( notMeasuredValueValid && currentData[ i ] == notMeasuredValue )
						notMeasuredMaskData[ i ]--;
					else
						setData[i] -= currentData[i];
				}
				break;
			}
			case AbstractDataset.INT16: {
				final short[] currentData = ((ShortDataset)set).getData();
				final short[] setData = ((ShortDataset)summedSet).getData();
				final short[] notMeasuredMaskData = ((ShortDataset)summedNotMeasuredMask).getData();
				final short[] badMaskData = ((ShortDataset)summedBadMask).getData();
				final short maxValidValue = maxValidNumber.shortValue();
				final short notMeasuredValue = notMeasuredNumber.shortValue();
				final short badValue = badNumber.shortValue();
				final int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					if( (maxValidValueValid && currentData[ i ] > maxValidValue) || (badValueValid && currentData[ i ] == badValue) )
						badMaskData[ i ]--;
					else if( notMeasuredValueValid && currentData[ i ] == notMeasuredValue )
						notMeasuredMaskData[ i ]--;
					else
						setData[i] -= currentData[i];
				}
				break;
			}
			case AbstractDataset.INT64: {
				final long[] currentData = ((LongDataset)set).getData();
				final long[] setData = ((LongDataset)summedSet).getData();
				final long[] notMeasuredMaskData = ((LongDataset)summedNotMeasuredMask).getData();
				final long[] badMaskData = ((LongDataset)summedBadMask).getData();
				final long maxValidValue = maxValidNumber.longValue();
				final long notMeasuredValue = notMeasuredNumber.longValue();
				final long badValue = badNumber.longValue();
				final int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					if( (maxValidValueValid && currentData[ i ] > maxValidValue) || (badValueValid && currentData[ i ] == badValue) )
						badMaskData[ i ]--;
					else if( notMeasuredValueValid && currentData[ i ] == notMeasuredValue )
						notMeasuredMaskData[ i ]--;
					else
						setData[i] -= currentData[i];
				}
				break;
			}
			case AbstractDataset.FLOAT32: {
				final float[] currentData = ((FloatDataset)set).getData();
				final float[] setData = ((FloatDataset)summedSet).getData();
				final float[] notMeasuredMaskData = ((FloatDataset)summedNotMeasuredMask).getData();
				final float[] badMaskData = ((FloatDataset)summedBadMask).getData();
				final float maxValidValue = maxValidNumber.floatValue();
				final float notMeasuredValue = notMeasuredNumber.floatValue();
				final float badValue = badNumber.floatValue();
				final int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					if( (maxValidValueValid && currentData[ i ] > maxValidValue) || (badValueValid && currentData[ i ] == badValue) )
						badMaskData[ i ]--;
					else if( notMeasuredValueValid && currentData[ i ] == notMeasuredValue )
						notMeasuredMaskData[ i ]--;
					else
						setData[i] -= currentData[i];
				}
				break;
			}
			case AbstractDataset.FLOAT64: {
				final double[] currentData = ((DoubleDataset)set).getData();
				final double[] setData = ((DoubleDataset)summedSet).getData();
				final double[] notMeasuredMaskData = ((DoubleDataset)summedNotMeasuredMask).getData();
				final double[] badMaskData = ((DoubleDataset)summedBadMask).getData();
				final double maxValidValue = maxValidNumber.doubleValue();
				final double notMeasuredValue = notMeasuredNumber.doubleValue();
				final double badValue = badNumber.doubleValue();
				final int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					if( (maxValidValueValid && currentData[ i ] > maxValidValue) || (badValueValid && currentData[ i ] == badValue) )
						badMaskData[ i ]--;
					else if( notMeasuredValueValid && currentData[ i ] == notMeasuredValue )
						notMeasuredMaskData[ i ]--;
					else
						setData[i] -= currentData[i];
				}
				break;
			}
			default:
				throw new RuntimeException("Not supported dataset type: " + set.getDtype() );
		}
	}

	/**
	 * This method multiplexes the summedSet, summedBadMask and summedNotMeasuredMask
	 * into one single dataset.
//	 * @param set the dataset to substract
	 * @param badNumber the value representing bad value. 
	 * @param notMeasuredValue the value which means the value is not measured. Typically -1 in CBF files.
	 */
	public static void joinSplittedSets(final AbstractDataset resultSet, final AbstractDataset summedSet, final AbstractDataset summedBadMask, final AbstractDataset summedNotMeasuredMask,
			final Number badNumber, final Number notMeasuredNumber) {
		final int type = resultSet.getDtype();
		//In case of BOOL, badNumber and notMeasuredNumber have no sense
		final boolean notMeasuredValueValid = type != AbstractDataset.BOOL && notMeasuredNumber != null;
		final boolean badValueValid = type != AbstractDataset.BOOL && badNumber != null;
		if( !badValueValid || !notMeasuredValueValid )
			throw new RuntimeException("badNumber (" + StringUtils.numberToString(badNumber) + ") or notMeasuredNumber (" + StringUtils.numberToString(notMeasuredNumber) + ") is invalid" );
		switch (type) {
			case AbstractDataset.BOOL: {
				final boolean[] currentData = ((BooleanDataset)resultSet).getData();
				final boolean[] setData = ((BooleanDataset)summedSet).getData();
				final int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					currentData[ i ] = setData[ i ];
				}
				break;
			}
			case AbstractDataset.INT32: {
				final int[] currentData = ((IntegerDataset)resultSet).getData();
				final int[] setData = ((IntegerDataset)summedSet).getData();
				final int[] notMeasuredMaskData = ((IntegerDataset)summedNotMeasuredMask).getData();
				final int[] badMaskData = ((IntegerDataset)summedBadMask).getData();
				final int badValue = badNumber.intValue();
				final int notMeasuredValue = notMeasuredNumber.intValue();
				final int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					if( badMaskData[ i ] > 0 )
						currentData[ i ] = badValue;
					else if( notMeasuredMaskData[ i ] > 0 )
						currentData[ i ] = notMeasuredValue;
					else
						currentData[ i ] = setData[ i ];
				}
				break;
			}
			case AbstractDataset.INT8: {
				final byte[] currentData = ((ByteDataset)resultSet).getData();
				final byte[] setData = ((ByteDataset)summedSet).getData();
				final byte[] notMeasuredMaskData = ((ByteDataset)summedNotMeasuredMask).getData();
				final byte[] badMaskData = ((ByteDataset)summedBadMask).getData();
				final byte badValue = badNumber.byteValue();
				final byte notMeasuredValue = notMeasuredNumber.byteValue();
				final int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					if( badMaskData[ i ] > 0 )
						currentData[ i ] = badValue;
					else if( notMeasuredMaskData[ i ] > 0 )
						currentData[ i ] = notMeasuredValue;
					else
						currentData[ i ] = setData[ i ];
				}
				break;
			}
			case AbstractDataset.INT16: {
				final short[] currentData = ((ShortDataset)resultSet).getData();
				final short[] setData = ((ShortDataset)summedSet).getData();
				final short[] notMeasuredMaskData = ((ShortDataset)summedNotMeasuredMask).getData();
				final short[] badMaskData = ((ShortDataset)summedBadMask).getData();
				final short badValue = badNumber.shortValue();
				final short notMeasuredValue = notMeasuredNumber.shortValue();
				final int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					if( badMaskData[ i ] > 0 )
						currentData[ i ] = badValue;
					else if( notMeasuredMaskData[ i ] > 0 )
						currentData[ i ] = notMeasuredValue;
					else
						currentData[ i ] = setData[ i ];
				}
				break;
			}
			case AbstractDataset.INT64: {
				final long[] currentData = ((LongDataset)resultSet).getData();
				final long[] setData = ((LongDataset)summedSet).getData();
				final long[] notMeasuredMaskData = ((LongDataset)summedNotMeasuredMask).getData();
				final long[] badMaskData = ((LongDataset)summedBadMask).getData();
				final long badValue = badNumber.longValue();
				final long notMeasuredValue = notMeasuredNumber.longValue();
				final int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					if( badMaskData[ i ] > 0 )
						currentData[ i ] = badValue;
					else if( notMeasuredMaskData[ i ] > 0 )
						currentData[ i ] = notMeasuredValue;
					else
						currentData[ i ] = setData[ i ];
				}
				break;
			}
			case AbstractDataset.FLOAT32: {
				final float[] currentData = ((FloatDataset)resultSet).getData();
				final float[] setData = ((FloatDataset)summedSet).getData();
				final float[] notMeasuredMaskData = ((FloatDataset)summedNotMeasuredMask).getData();
				final float[] badMaskData = ((FloatDataset)summedBadMask).getData();
				final float badValue = badNumber.floatValue();
				final float notMeasuredValue = notMeasuredNumber.floatValue();
				final int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					if( badMaskData[ i ] > 0 )
						currentData[ i ] = badValue;
					else if( notMeasuredMaskData[ i ] > 0 )
						currentData[ i ] = notMeasuredValue;
					else
						currentData[ i ] = setData[ i ];
				}
				break;
			}
			case AbstractDataset.FLOAT64: {
				final double[] currentData = ((DoubleDataset)resultSet).getData();
				final double[] setData = ((DoubleDataset)summedSet).getData();
				final double[] notMeasuredMaskData = ((DoubleDataset)summedNotMeasuredMask).getData();
				final double[] badMaskData = ((DoubleDataset)summedBadMask).getData();
				final double badValue = badNumber.doubleValue();
				final double notMeasuredValue = notMeasuredNumber.doubleValue();
				final int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					if( badMaskData[ i ] > 0 )
						currentData[ i ] = badValue;
					else if( notMeasuredMaskData[ i ] > 0 )
						currentData[ i ] = notMeasuredValue;
					else
						currentData[ i ] = setData[ i ];
				}
				break;
			}
			default:
				throw new RuntimeException("Not supported dataset type: " + resultSet.getDtype() );
		}
	}

}