package org.embl.cca.utils.imageviewer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * The <code>Utils</code> class contains useful methods
 * that could be implemented at better places but would
 * be too much effort.
 * <p>
 * </p>
 *
 * @author Gábor Náray
 * @version 1.00 07/12/2011
 * @since   20111207
 */
public class ConverterUtils {
	final static String formatter = "%.2f";
	public static String floatToString( float v ) {
		String result = null;
		if( (int)v != v )
			result = String.valueOf(v);
		else
			result = String.valueOf( (int)v );
		return result;
	}

	/**
	 * @return String of float value, with default format (see formatter)
	 */
	public static String floatAsString( double v ) {
		return String.format(formatter, v);
	}

	/**
	 * @return String of double value, handling int value separately
	 */
	public static String doubleToString( double v ) {
		String result = null;
		if( (int)v != v )
			result = String.valueOf(v);
		else
			result = String.valueOf( (int)v );
		return result;
	}

	/**
	 * @return String of double value, with default format (see formatter)
	 */
	public static String doubleAsString( double v ) {
		return String.format(formatter, v);
	}

	/**
	 * @return String of double value, with default format (see formatter), filling with spaces at right side to form width characters
	 */
	public static String doubleAsStringAndRightFill( double v, int width ) {
		String result = String.format(formatter, v);
		int needWidth = width - result.length(); 
		if( needWidth > 0 ) {
			char[] arr = new char[needWidth];
			Arrays.fill(arr, ' ');
			result += new String(arr);
		}
		return result;
	}

	public static <K,V> HashMap<K,V> toHashMap(K[] keys, V[] values) {
		if (keys == null || values == null)
			return null;
        if (keys.length != values.length)
        	throw new IllegalArgumentException("'keys' and 'values' arrays differ in size");
		final HashMap<K,V> map = new HashMap<K,V>((int) (keys.length * 1.5));
		for (int i = 0; i < keys.length; i++)
			map.put((K)keys[ i ], (V)values[ i ]);
		return map;
	}

	public static HashMap toHashMap(Object[] array) {
		if (array == null) {
			return null;
		}
		final HashMap map = new HashMap((int) (array.length * 1.5));
		for (int i = 0; i < array.length; i++) {
			Object object = array[i];
			if (object instanceof Map.Entry) {
				Map.Entry entry = (Map.Entry)object;
				map.put(entry.getKey(), entry.getValue());
			} else if (object instanceof Object[]) {
				Object[] entry = (Object[])object;
				if (entry.length < 2) {
					throw new IllegalArgumentException("Array element " + i + ", '"
							+ object
							+ "', has a length less than 2");
				}
				map.put(entry[0], entry[1]);
			} else {
				throw new IllegalArgumentException("Array element " + i + ", '"
						+ object
						+ "', is neither of type Map.Entry nor an Array");
			}
		}
		return map;
	}

}
