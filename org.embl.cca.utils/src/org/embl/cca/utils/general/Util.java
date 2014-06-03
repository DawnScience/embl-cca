package org.embl.cca.utils.general;

import org.embl.cca.utils.datahandling.EFile;

/**
 * <p>
 * A static class providing utility methods.
 * </p>
 */
public final class Util {
	public final static int HashMultiplier = 19; //better if prime
	public final static int HashForNull = 1234567891; //not necessarily prime

	/**
	 * This class should never be constructed.
	 */
	private Util() {
	}

	/**
	 * Provides a hash code for the object -- defending against
	 * <code>null</code>. For <code>null</code>
	 * <code>object</code> the <code>HashForNull</code> value
	 * is calculated.
	 * 
	 * @param object
	 *            The object for which a hash code is required.
	 * @return <code>object.hashCode</code> or <code>HashForNull</code> if
	 *         <code>object</code> if <code>null</code>.
	 */
	public static final int hashCode(final Object object) {
		return object != null ? object.hashCode() : HashForNull;
	}

	/**
	 * Computes the hash code for an array of objects, but with defense against
	 * <code>null</code>. For <code>null</code>
	 * <code>objects</code> the <code>HashForNull</code> value
	 * is calculated.
	 * 
	 * @param objects
	 *            The array of objects for which a hash code is needed; may be
	 *            <code>null</code>.
	 * @return The hash code for <code>objects</code>; or <code>0</code> if
	 *         <code>objects</code> is <code>null</code> or <code>objects</code> is empty.
	 */
	public static final int hashCode(final Object[] objects) {
		if (objects == null || objects.length == 0) {
			return 0;
		}

		int hashCode = hashCode(objects[0]);
		for (int i = 1; i < objects.length; i++)
			hashCode = hashCode * HashMultiplier + hashCode(objects[i]);
		return hashCode;
	}

	public static boolean isWindowsOS() {
		return (System.getProperty("os.name").indexOf("Windows") == 0);
	}

	public static String getRootPathForDisplay(final EFile file) {
		if( file.getPathWithoutProtocol().isEmpty())
			return "<NONAME>";
		if (isWindowsOS()) {
			return new StringBuilder("(").append(file.getAbsolutePathWithoutProtocol()
				.substring(0, file.getPathWithoutProtocol().length()-1)).append(")").toString();
		} else
			return file.getPath();
	}

}
