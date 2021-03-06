package org.embl.cca.utils.datahandling;

import org.eclipse.january.dataset.DTypeUtils;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.metadata.IMetadata;

public class DatasetNumber extends Number  implements Comparable<DatasetNumber>{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2245073853201104044L;

	protected Dataset set;

    /**
     * The value of the <code>DatasetNumber</code>.
     *
     * @serial
     */
    protected final Number value;

    /**
     * Constructs a newly allocated <code>DatasetNumber</code> object that
     * represents the specified <code>Number</code> value.
     *
	 * @param set the dataset which determines the real type of DatasetNumber
     * @param   value   the value to be represented by the 
     *			<code>DatasetNumber</code> object.
     */
	public DatasetNumber(Dataset set, Number value) {
		this.set = set;
		this.value = value;
	}

//If needed, this constructor can be implemented
//    public DatasetNumber(Dataset set, String s) throws NumberFormatException {
//    	this.value = parseInt(s, 10);
//    }

	/**
	 * This method should be in Dataset, but it is not, so implemented here.
	 * @return the maximum number according to dataset type
	 */
	protected Number getMaximumNumber() {
		switch (set.getDType()) {
		case Dataset.BOOL:
			return Integer.valueOf(1); //Hacking, since Boolean is not Number, and using Integer similarly to Dataset.fromDoubleToNumber
		case Dataset.INT32:
			return Integer.MAX_VALUE;
		case Dataset.INT8:
			return Byte.MAX_VALUE;
		case Dataset.INT16:
			return Short.MAX_VALUE;
		case Dataset.INT64:
			return Long.MAX_VALUE;
		case Dataset.FLOAT32:
			return Float.MAX_VALUE;
		case Dataset.FLOAT64:
			return Double.MAX_VALUE;
		}
		throw new RuntimeException("Not supported dataset type: " + set.getDType() );
	}

	/**
	 * This method should be in Dataset, but it is not, so implemented here.
	 * @param x the number to check if it is maximum
	 * @return true if x is maximum number according to dataset type
	 */
	protected boolean isMaximumNumber(Number x) {
		return getMaximumNumber().equals(x);
	}

	/**
	 * This method should be public in Dataset, but it is not.
	 * @param set the dataset which specifies the type of result
	 * @param x the number of which type to convert
	 * @return the type converted number
	 */
	protected Number fromDoubleToNumber(double x) {
		return fromDoubleToNumber(set, x);
	}

	/**
	 * This method should be public in Dataset, but it is not, so hacking to use it.
	 * @param set the dataset which specifies the type of result
	 * @param x the number of which type to convert
	 * @return the type converted number
	 */
	public static Number fromDoubleToNumber(Dataset set, double x) {
		return DTypeUtils.fromDoubleToBiggestNumber(x, set.getDType());
	}

	/**
	 * 
	 * @return the maximum valid number in the dataset
	 */
	protected Number getMaxValidNumber() {
		return getMaxValidNumber(set);
	}

	/**
	 * This method should be in a dedicated class, which should be easily configurable by user, because here
	 * user specified strings are interpreted. 
	 * @return the maximum valid number in the dataset
	 */
	public static Number getMaxValidNumber(Dataset set) {
		try {
			IMetadata metadata = set.getMetadata();
			if( metadata == null )
				throw new RuntimeException("Metadata is not available for dataset: " + set.getName());
			String value = metadata.getMetaValue("Count_cutoff").toString().split("counts")[0];
			return fromDoubleToNumber(set, Double.parseDouble(value));
		} catch (Exception e) {
			if( e instanceof ClassCastException)
				throw (ClassCastException)e; 
			throw new RuntimeException(e);
		}
	}

	/**
	 * 
	 * @param defaultNumber the returned number if the method fails
	 * @return the maximum valid number in the dataset or defaultNumber if its value is not available
	 */
	protected Number getMaxValidNumber(boolean returnNullOnFailure) {
		return getMaxValidNumber(set, returnNullOnFailure);
	}

	/**
	 * 
	 * @param defaultNumber the returned number if the method fails
	 * @return the maximum valid number in the dataset or defaultNumber if its value is not available
	 */
	public static Number getMaxValidNumber(Dataset set, boolean returnNullOnFailure) {
		try {
			return getMaxValidNumber(set);
		} catch (Exception e) {
			if( returnNullOnFailure )
				return null;
			throw new RuntimeException(e);
		}
	}

	protected Number getNumber(Number value, boolean returnNullOnFailure) {
		return getNumber(set, value, returnNullOnFailure);
	}

	public static Number getNumber(Dataset set, Number value, boolean returnNullOnFailure) {
		try {
			return fromDoubleToNumber(set, value.doubleValue());
		} catch (Exception e) {
			if( returnNullOnFailure )
				return null;
			throw new RuntimeException(e);
		}
	}

    /**
     * Returns the value of this <code>DatasetNumber</code> as a
     * <code>byte</code>.
     */
	@Override
    public byte byteValue() {
		return value.byteValue();
    }

    /**
     * Returns the value of this <code>DatasetNumber</code> as a
     * <code>short</code>.
     */
	@Override
    public short shortValue() {
		return value.shortValue();
    }

    /**
     * Returns the value of this <code>DatasetNumber</code> as an
     * <code>int</code>.
     */
	@Override
	public int intValue() {
		return value.intValue();
	}

    /**
     * Returns the value of this <code>DatasetNumber</code> as a
     * <code>long</code>.
     */
	@Override
	public long longValue() {
		return value.longValue();
	}

    /**
     * Returns the value of this <code>DatasetNumber</code> as a
     * <code>float</code>.
     */
	@Override
	public float floatValue() {
		return value.floatValue();
	}

    /**
     * Returns the value of this <code>DatasetNumber</code> as a
     * <code>double</code>.
     */
	@Override
	public double doubleValue() {
		return value.doubleValue();
	}

    /**
     * Returns the value of this <code>DatasetNumber</code> as a
     * <code>boolean</code>.
     */
	public boolean booleanValue() {
		switch (set.getDType()) {
		case Dataset.BOOL:
			return value.intValue() != 0; //Hacking, since Boolean is not Number, and using Integer similarly to Dataset.fromDoubleToNumber
		case Dataset.INT32:
			return value.intValue() != 0;
		case Dataset.INT8:
			return value.byteValue() != 0;
		case Dataset.INT16:
			return value.shortValue() != 0;
		case Dataset.INT64:
			return value.longValue() != 0;
		case Dataset.FLOAT32:
			return value.floatValue() != 0;
		case Dataset.FLOAT64:
			return value.doubleValue() != 0;
		}
		throw new RuntimeException("Not supported dataset type: " + set.getDType() );
	}

    /**
     * Compares this object to the specified object.  The result is
     * <code>true</code> if and only if the argument is not
     * <code>null</code> and is an <code>DatasetNumber</code> object that
     * contains the same <code>Number</code> value as this object.
     *
     * @param   obj   the object to compare with.
     * @return  <code>true</code> if the objects are the same;
     *          <code>false</code> otherwise.
     */
	@Override
    public boolean equals(Object obj) {
	if (obj instanceof DatasetNumber) {
	    return value.equals(((DatasetNumber)obj).value);
	}
	return false;
    }

    /**
     * Compares two <code>DatasetNumber</code> objects numerically.
     *
     * @param   anotherNumber   the <code>DatasetNumber</code> to be compared.
     * @return	the value <code>0</code> if this <code>DatasetNumber</code> is
     * 		equal to the argument <code>DatasetNumber</code>; a value less than
     * 		<code>0</code> if this <code>DatasetNumber</code> is numerically less
     * 		than the argument <code>DatasetNumber</code>; and a value greater 
     * 		than <code>0</code> if this <code>DatasetNumber</code> is numerically
     * 		 greater than the argument <code>DatasetNumber</code> (signed
     * 		 comparison).
     * @since   1.2
     */
	@Override
    public int compareTo(DatasetNumber anotherNumber) {
		int type = DTypeUtils.getBestDType(set.getDType(), anotherNumber.set.getDType());
		switch (type) {
			case Dataset.BOOL: {
				int thisVal = value.intValue(); //Hacking, since Boolean is not Number, and using Integer similarly to Dataset.fromDoubleToNumber
				int anotherVal = anotherNumber.intValue();
				return (thisVal<anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
			}
			case Dataset.INT32: {
				int thisVal = value.intValue();
				int anotherVal = anotherNumber.intValue();
				return (thisVal<anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
			}
			case Dataset.INT8: {
				byte thisVal = value.byteValue();
				byte anotherVal = anotherNumber.byteValue();
				return (thisVal<anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
			}
			case Dataset.INT16: {
				short thisVal = value.shortValue();
				short anotherVal = anotherNumber.shortValue();
				return (thisVal<anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
			}
			case Dataset.INT64: {
				long thisVal = value.longValue();
				long anotherVal = anotherNumber.longValue();
				return (thisVal<anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
			}
			case Dataset.FLOAT32: {
				float thisVal = value.floatValue();
				float anotherVal = anotherNumber.floatValue();
				return (thisVal<anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
			}
			case Dataset.FLOAT64: {
				double thisVal = value.doubleValue();
				double anotherVal = anotherNumber.doubleValue();
				return (thisVal<anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
			}
		}
		throw new RuntimeException("Not supported dataset type: " + set.getDType() );
    }
}
