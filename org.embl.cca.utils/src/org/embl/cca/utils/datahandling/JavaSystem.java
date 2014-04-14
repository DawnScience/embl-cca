package org.embl.cca.utils.datahandling;

import java.io.*;
import java.util.Properties;
import java.nio.channels.Channel;

/**
 * The <code>System</code> class contains several useful class fields and
 * methods. It cannot be instantiated.
 * 
 * <p>
 * Among the facilities provided by the <code>System</code> class are standard
 * input, standard output, and error output streams; access to externally
 * defined properties and environment variables; a means of loading files and
 * libraries; and a utility method for quickly copying a portion of an array.
 * 
 * @author unascribed
 * @version 1.162, 04/01/09
 * @since JDK1.0
 */
public final class JavaSystem {
	/** Don't let anyone instantiate this class */
	private JavaSystem() {
	}

	/**
	 * The "standard" input stream. This stream is already open and ready to
	 * supply input data. Typically this stream corresponds to keyboard input or
	 * another input source specified by the host environment or user.
	 */
	public static InputStream in() {
		return System.in;
	}

	/**
	 * The "standard" output stream. This stream is already open and ready to
	 * accept output data. Typically this stream corresponds to display output
	 * or another output destination specified by the host environment or user.
	 * <p>
	 * For simple stand-alone Java applications, a typical way to write a line
	 * of output data is: <blockquote>
	 * 
	 * <pre>
	 * System.out.println(data)
	 * </pre>
	 * 
	 * </blockquote>
	 * <p>
	 * See the <code>println</code> methods in class <code>PrintStream</code>.
	 * 
	 * @see java.io.PrintStream#println()
	 * @see java.io.PrintStream#println(boolean)
	 * @see java.io.PrintStream#println(char)
	 * @see java.io.PrintStream#println(char[])
	 * @see java.io.PrintStream#println(double)
	 * @see java.io.PrintStream#println(float)
	 * @see java.io.PrintStream#println(int)
	 * @see java.io.PrintStream#println(long)
	 * @see java.io.PrintStream#println(java.lang.Object)
	 * @see java.io.PrintStream#println(java.lang.String)
	 */
	public static PrintStream out() {
		return System.out;
	}

	/**
	 * The "standard" error output stream. This stream is already open and ready
	 * to accept output data.
	 * <p>
	 * Typically this stream corresponds to display output or another output
	 * destination specified by the host environment or user. By convention,
	 * this output stream is used to display error messages or other information
	 * that should come to the immediate attention of a user even if the
	 * principal output stream, the value of the variable <code>out</code>, has
	 * been redirected to a file or other destination that is typically not
	 * continuously monitored.
	 */
	public static PrintStream err() {
		return System.err;
	}

	/**
	 * Reassigns the "standard" input stream.
	 * 
	 * <p>
	 * First, if there is a security manager, its <code>checkPermission</code>
	 * method is called with a <code>RuntimePermission("setIO")</code>
	 * permission to see if it's ok to reassign the "standard" input stream.
	 * <p>
	 * 
	 * @param in
	 *            the new standard input stream.
	 * 
	 * @throws SecurityException
	 *             if a security manager exists and its
	 *             <code>checkPermission</code> method doesn't allow reassigning
	 *             of the standard input stream.
	 * 
	 * @see SecurityManager#checkPermission
	 * @see java.lang.RuntimePermission
	 * 
	 * @since JDK1.1
	 */
	public static void setIn(InputStream in) {
		System.setIn(in);
	}

	/**
	 * Reassigns the "standard" output stream.
	 * 
	 * <p>
	 * First, if there is a security manager, its <code>checkPermission</code>
	 * method is called with a <code>RuntimePermission("setIO")</code>
	 * permission to see if it's ok to reassign the "standard" output stream.
	 * 
	 * @param out
	 *            the new standard output stream
	 * 
	 * @throws SecurityException
	 *             if a security manager exists and its
	 *             <code>checkPermission</code> method doesn't allow reassigning
	 *             of the standard output stream.
	 * 
	 * @see SecurityManager#checkPermission
	 * @see java.lang.RuntimePermission
	 * 
	 * @since JDK1.1
	 */
	public static void setOut(PrintStream out) {
		System.setOut(out);
	}

	/**
	 * Reassigns the "standard" error output stream.
	 * 
	 * <p>
	 * First, if there is a security manager, its <code>checkPermission</code>
	 * method is called with a <code>RuntimePermission("setIO")</code>
	 * permission to see if it's ok to reassign the "standard" error output
	 * stream.
	 * 
	 * @param err
	 *            the new standard error output stream.
	 * 
	 * @throws SecurityException
	 *             if a security manager exists and its
	 *             <code>checkPermission</code> method doesn't allow reassigning
	 *             of the standard error output stream.
	 * 
	 * @see SecurityManager#checkPermission
	 * @see java.lang.RuntimePermission
	 * 
	 * @since JDK1.1
	 */
	public static void setErr(PrintStream err) {
		System.setErr(err);
	}

	/**
	 * Returns the unique {@link java.io.Console Console} object associated with
	 * the current Java virtual machine, if any.
	 * 
	 * @return The system console, if any, otherwise <tt>null</tt>.
	 * 
	 * @since 1.6
	 */
	public static Console console() {
		return System.console();
	}

	/**
	 * Returns the channel inherited from the entity that created this Java
	 * virtual machine.
	 * 
	 * <p>
	 * This method returns the channel obtained by invoking the
	 * {@link java.nio.channels.spi.SelectorProvider#inheritedChannel
	 * inheritedChannel} method of the system-wide default
	 * {@link java.nio.channels.spi.SelectorProvider} object.
	 * </p>
	 * 
	 * <p>
	 * In addition to the network-oriented channels described in
	 * {@link java.nio.channels.spi.SelectorProvider#inheritedChannel
	 * inheritedChannel}, this method may return other kinds of channels in the
	 * future.
	 * 
	 * @return The inherited channel, if any, otherwise <tt>null</tt>.
	 * 
	 * @throws IOException
	 *             If an I/O error occurs
	 * 
	 * @throws SecurityException
	 *             If a security manager is present and it does not permit
	 *             access to the channel.
	 * 
	 * @since 1.5
	 */
	public static Channel inheritedChannel() throws IOException {
		return System.inheritedChannel();
	}

	/**
	 * Sets the System security.
	 * 
	 * <p>
	 * If there is a security manager already installed, this method first calls
	 * the security manager's <code>checkPermission</code> method with a
	 * <code>RuntimePermission("setSecurityManager")</code> permission to ensure
	 * it's ok to replace the existing security manager. This may result in
	 * throwing a <code>SecurityException</code>.
	 * 
	 * <p>
	 * Otherwise, the argument is established as the current security manager.
	 * If the argument is <code>null</code> and no security manager has been
	 * established, then no action is taken and the method simply returns.
	 * 
	 * @param s
	 *            the security manager.
	 * @exception SecurityException
	 *                if the security manager has already been set and its
	 *                <code>checkPermission</code> method doesn't allow it to be
	 *                replaced.
	 * @see #getSecurityManager
	 * @see SecurityManager#checkPermission
	 * @see java.lang.RuntimePermission
	 */
	public static void setSecurityManager(final SecurityManager s) {
		System.setSecurityManager(s);
	}

	/**
	 * Gets the system security interface.
	 * 
	 * @return if a security manager has already been established for the
	 *         current application, then that security manager is returned;
	 *         otherwise, <code>null</code> is returned.
	 * @see #setSecurityManager
	 */
	public static SecurityManager getSecurityManager() {
		return System.getSecurityManager();
	}

	/**
	 * Returns the current time in milliseconds. Note that while the unit of
	 * time of the return value is a millisecond, the granularity of the value
	 * depends on the underlying operating system and may be larger. For
	 * example, many operating systems measure time in units of tens of
	 * milliseconds.
	 * 
	 * <p>
	 * See the description of the class <code>Date</code> for a discussion of
	 * slight discrepancies that may arise between "computer time" and
	 * coordinated universal time (UTC).
	 * 
	 * @return the difference, measured in milliseconds, between the current
	 *         time and midnight, January 1, 1970 UTC.
	 * @see java.util.Date
	 */
	public static long currentTimeMillis() {
		return System.currentTimeMillis();
	}

	/**
	 * Returns the current value of the most precise available system timer, in
	 * nanoseconds.
	 * 
	 * <p>
	 * This method can only be used to measure elapsed time and is not related
	 * to any other notion of system or wall-clock time. The value returned
	 * represents nanoseconds since some fixed but arbitrary time (perhaps in
	 * the future, so values may be negative). This method provides nanosecond
	 * precision, but not necessarily nanosecond accuracy. No guarantees are
	 * made about how frequently values change. Differences in successive calls
	 * that span greater than approximately 292 years (2<sup>63</sup>
	 * nanoseconds) will not accurately compute elapsed time due to numerical
	 * overflow.
	 * 
	 * <p>
	 * For example, to measure how long some code takes to execute:
	 * 
	 * <pre>
	 * long startTime = System.nanoTime();
	 * // ... the code being measured ...
	 * long estimatedTime = System.nanoTime() - startTime;
	 * </pre>
	 * 
	 * @return The current value of the system timer, in nanoseconds.
	 * @since 1.5
	 */
	public static long nanoTime() {
		return System.nanoTime();
	}

	/**
	 * Copies an array from the specified source array, beginning at the
	 * specified position, to the specified position of the destination array. A
	 * subsequence of array components are copied from the source array
	 * referenced by <code>src</code> to the destination array referenced by
	 * <code>dest</code>. The number of components copied is equal to the
	 * <code>length</code> argument. The components at positions
	 * <code>srcPos</code> through <code>srcPos+length-1</code> in the source
	 * array are copied into positions <code>destPos</code> through
	 * <code>destPos+length-1</code>, respectively, of the destination array.
	 * <p>
	 * If the <code>src</code> and <code>dest</code> arguments refer to the same
	 * array object, then the copying is performed as if the components at
	 * positions <code>srcPos</code> through <code>srcPos+length-1</code> were
	 * first copied to a temporary array with <code>length</code> components and
	 * then the contents of the temporary array were copied into positions
	 * <code>destPos</code> through <code>destPos+length-1</code> of the
	 * destination array.
	 * <p>
	 * If <code>dest</code> is <code>null</code>, then a
	 * <code>NullPointerException</code> is thrown.
	 * <p>
	 * If <code>src</code> is <code>null</code>, then a
	 * <code>NullPointerException</code> is thrown and the destination array is
	 * not modified.
	 * <p>
	 * Otherwise, if any of the following is true, an
	 * <code>ArrayStoreException</code> is thrown and the destination is not
	 * modified:
	 * <ul>
	 * <li>The <code>src</code> argument refers to an object that is not an
	 * array.
	 * <li>The <code>dest</code> argument refers to an object that is not an
	 * array.
	 * <li>The <code>src</code> argument and <code>dest</code> argument refer to
	 * arrays whose component types are different primitive types.
	 * <li>The <code>src</code> argument refers to an array with a primitive
	 * component type and the <code>dest</code> argument refers to an array with
	 * a reference component type.
	 * <li>The <code>src</code> argument refers to an array with a reference
	 * component type and the <code>dest</code> argument refers to an array with
	 * a primitive component type.
	 * </ul>
	 * <p>
	 * Otherwise, if any of the following is true, an
	 * <code>IndexOutOfBoundsException</code> is thrown and the destination is
	 * not modified:
	 * <ul>
	 * <li>The <code>srcPos</code> argument is negative.
	 * <li>The <code>destPos</code> argument is negative.
	 * <li>The <code>length</code> argument is negative.
	 * <li><code>srcPos+length</code> is greater than <code>src.length</code>,
	 * the length of the source array.
	 * <li><code>destPos+length</code> is greater than <code>dest.length</code>,
	 * the length of the destination array.
	 * </ul>
	 * <p>
	 * Otherwise, if any actual component of the source array from position
	 * <code>srcPos</code> through <code>srcPos+length-1</code> cannot be
	 * converted to the component type of the destination array by assignment
	 * conversion, an <code>ArrayStoreException</code> is thrown. In this case,
	 * let <b><i>k</i></b> be the smallest nonnegative integer less than length
	 * such that <code>src[srcPos+</code><i>k</i><code>]</code> cannot be
	 * converted to the component type of the destination array; when the
	 * exception is thrown, source array components from positions
	 * <code>srcPos</code> through <code>srcPos+</code><i>k</i><code>-1</code>
	 * will already have been copied to destination array positions
	 * <code>destPos</code> through <code>destPos+</code><i>k</I><code>-1</code>
	 * and no other positions of the destination array will have been modified.
	 * (Because of the restrictions already itemized, this paragraph effectively
	 * applies only to the situation where both arrays have component types that
	 * are reference types.)
	 * 
	 * @param src
	 *            the source array.
	 * @param srcPos
	 *            starting position in the source array.
	 * @param dest
	 *            the destination array.
	 * @param destPos
	 *            starting position in the destination data.
	 * @param length
	 *            the number of array elements to be copied.
	 * @exception IndexOutOfBoundsException
	 *                if copying would cause access of data outside array
	 *                bounds.
	 * @exception ArrayStoreException
	 *                if an element in the <code>src</code> array could not be
	 *                stored into the <code>dest</code> array because of a type
	 *                mismatch.
	 * @exception NullPointerException
	 *                if either <code>src</code> or <code>dest</code> is
	 *                <code>null</code>.
	 */
	public static void arraycopy(Object src, int srcPos, Object dest,
			int destPos, int length) {
		System.arraycopy(src, srcPos, dest, destPos, length);
	}

	/**
	 * Returns the same hash code for the given object as would be returned by
	 * the default method hashCode(), whether or not the given object's class
	 * overrides hashCode(). The hash code for the null reference is zero.
	 * 
	 * @param x
	 *            object for which the hashCode is to be calculated
	 * @return the hashCode
	 * @since JDK1.1
	 */
	public static int identityHashCode(Object x) {
		return System.identityHashCode(x);
	}

	/**
	 * Determines the current system properties.
	 * <p>
	 * First, if there is a security manager, its
	 * <code>checkPropertiesAccess</code> method is called with no arguments.
	 * This may result in a security exception.
	 * <p>
	 * The current set of system properties for use by the
	 * {@link #getProperty(String)} method is returned as a
	 * <code>Properties</code> object. If there is no current set of system
	 * properties, a set of system properties is first created and initialized.
	 * This set of system properties always includes values for the following
	 * keys:
	 * <table summary="Shows property keys and associated values">
	 * <tr>
	 * <th>Key</th>
	 * <th>Description of Associated Value</th>
	 * </tr>
	 * <tr>
	 * <td><code>java.version</code></td>
	 * <td>Java Runtime Environment version</td>
	 * </tr>
	 * <tr>
	 * <td><code>java.vendor</code></td>
	 * <td>Java Runtime Environment vendor</td></tr
	 * <tr>
	 * <td><code>java.vendor.url</code></td>
	 * <td>Java vendor URL</td>
	 * </tr>
	 * <tr>
	 * <td><code>java.home</code></td>
	 * <td>Java installation directory</td>
	 * </tr>
	 * <tr>
	 * <td><code>java.vm.specification.version</code></td>
	 * <td>Java Virtual Machine specification version</td>
	 * </tr>
	 * <tr>
	 * <td><code>java.vm.specification.vendor</code></td>
	 * <td>Java Virtual Machine specification vendor</td>
	 * </tr>
	 * <tr>
	 * <td><code>java.vm.specification.name</code></td>
	 * <td>Java Virtual Machine specification name</td>
	 * </tr>
	 * <tr>
	 * <td><code>java.vm.version</code></td>
	 * <td>Java Virtual Machine implementation version</td>
	 * </tr>
	 * <tr>
	 * <td><code>java.vm.vendor</code></td>
	 * <td>Java Virtual Machine implementation vendor</td>
	 * </tr>
	 * <tr>
	 * <td><code>java.vm.name</code></td>
	 * <td>Java Virtual Machine implementation name</td>
	 * </tr>
	 * <tr>
	 * <td><code>java.specification.version</code></td>
	 * <td>Java Runtime Environment specification version</td>
	 * </tr>
	 * <tr>
	 * <td><code>java.specification.vendor</code></td>
	 * <td>Java Runtime Environment specification vendor</td>
	 * </tr>
	 * <tr>
	 * <td><code>java.specification.name</code></td>
	 * <td>Java Runtime Environment specification name</td>
	 * </tr>
	 * <tr>
	 * <td><code>java.class.version</code></td>
	 * <td>Java class format version number</td>
	 * </tr>
	 * <tr>
	 * <td><code>java.class.path</code></td>
	 * <td>Java class path</td>
	 * </tr>
	 * <tr>
	 * <td><code>java.library.path</code></td>
	 * <td>List of paths to search when loading libraries</td>
	 * </tr>
	 * <tr>
	 * <td><code>java.io.tmpdir</code></td>
	 * <td>Default temp file path</td>
	 * </tr>
	 * <tr>
	 * <td><code>java.compiler</code></td>
	 * <td>Name of JIT compiler to use</td>
	 * </tr>
	 * <tr>
	 * <td><code>java.ext.dirs</code></td>
	 * <td>Path of extension directory or directories</td>
	 * </tr>
	 * <tr>
	 * <td><code>os.name</code></td>
	 * <td>Operating system name</td>
	 * </tr>
	 * <tr>
	 * <td><code>os.arch</code></td>
	 * <td>Operating system architecture</td>
	 * </tr>
	 * <tr>
	 * <td><code>os.version</code></td>
	 * <td>Operating system version</td>
	 * </tr>
	 * <tr>
	 * <td><code>file.separator</code></td>
	 * <td>File separator ("/" on UNIX)</td>
	 * </tr>
	 * <tr>
	 * <td><code>path.separator</code></td>
	 * <td>Path separator (":" on UNIX)</td>
	 * </tr>
	 * <tr>
	 * <td><code>line.separator</code></td>
	 * <td>Line separator ("\n" on UNIX)</td>
	 * </tr>
	 * <tr>
	 * <td><code>user.name</code></td>
	 * <td>User's account name</td>
	 * </tr>
	 * <tr>
	 * <td><code>user.home</code></td>
	 * <td>User's home directory</td>
	 * </tr>
	 * <tr>
	 * <td><code>user.dir</code></td>
	 * <td>User's current working directory</td>
	 * </tr>
	 * </table>
	 * <p>
	 * Multiple paths in a system property value are separated by the path
	 * separator character of the platform.
	 * <p>
	 * Note that even if the security manager does not permit the
	 * <code>getProperties</code> operation, it may choose to permit the
	 * {@link #getProperty(String)} operation.
	 * 
	 * @return the system properties
	 * @exception SecurityException
	 *                if a security manager exists and its
	 *                <code>checkPropertiesAccess</code> method doesn't allow
	 *                access to the system properties.
	 * @see #setProperties
	 * @see java.lang.SecurityException
	 * @see java.lang.SecurityManager#checkPropertiesAccess()
	 * @see java.util.Properties
	 */
	public static Properties getProperties() {
		return System.getProperties();
	}

	/**
	 * Sets the system properties to the <code>Properties</code> argument.
	 * <p>
	 * First, if there is a security manager, its
	 * <code>checkPropertiesAccess</code> method is called with no arguments.
	 * This may result in a security exception.
	 * <p>
	 * The argument becomes the current set of system properties for use by the
	 * {@link #getProperty(String)} method. If the argument is <code>null</code>
	 * , then the current set of system properties is forgotten.
	 * 
	 * @param props
	 *            the new system properties.
	 * @exception SecurityException
	 *                if a security manager exists and its
	 *                <code>checkPropertiesAccess</code> method doesn't allow
	 *                access to the system properties.
	 * @see #getProperties
	 * @see java.util.Properties
	 * @see java.lang.SecurityException
	 * @see java.lang.SecurityManager#checkPropertiesAccess()
	 */
	public static void setProperties(Properties props) {
		System.setProperties(props);
	}

	/**
	 * Gets the system property indicated by the specified key.
	 * <p>
	 * First, if there is a security manager, its
	 * <code>checkPropertyAccess</code> method is called with the key as its
	 * argument. This may result in a SecurityException.
	 * <p>
	 * If there is no current set of system properties, a set of system
	 * properties is first created and initialized in the same manner as for the
	 * <code>getProperties</code> method.
	 * 
	 * @param key
	 *            the name of the system property.
	 * @return the string value of the system property, or <code>null</code> if
	 *         there is no property with that key.
	 * 
	 * @exception SecurityException
	 *                if a security manager exists and its
	 *                <code>checkPropertyAccess</code> method doesn't allow
	 *                access to the specified system property.
	 * @exception NullPointerException
	 *                if <code>key</code> is <code>null</code>.
	 * @exception IllegalArgumentException
	 *                if <code>key</code> is empty.
	 * @see #setProperty
	 * @see java.lang.SecurityException
	 * @see java.lang.SecurityManager#checkPropertyAccess(java.lang.String)
	 * @see java.lang.System#getProperties()
	 */
	public static String getProperty(String key) {
		return System.getProperty(key);
	}

	/**
	 * Gets the system property indicated by the specified key.
	 * <p>
	 * First, if there is a security manager, its
	 * <code>checkPropertyAccess</code> method is called with the
	 * <code>key</code> as its argument.
	 * <p>
	 * If there is no current set of system properties, a set of system
	 * properties is first created and initialized in the same manner as for the
	 * <code>getProperties</code> method.
	 * 
	 * @param key
	 *            the name of the system property.
	 * @param def
	 *            a default value.
	 * @return the string value of the system property, or the default value if
	 *         there is no property with that key.
	 * 
	 * @exception SecurityException
	 *                if a security manager exists and its
	 *                <code>checkPropertyAccess</code> method doesn't allow
	 *                access to the specified system property.
	 * @exception NullPointerException
	 *                if <code>key</code> is <code>null</code>.
	 * @exception IllegalArgumentException
	 *                if <code>key</code> is empty.
	 * @see #setProperty
	 * @see java.lang.SecurityManager#checkPropertyAccess(java.lang.String)
	 * @see java.lang.System#getProperties()
	 */
	public static String getProperty(String key, String def) {
		return System.getProperty(key, def);
	}

	/**
	 * Returns <code>true</code> if and only if the system property named by the
	 * argument exists and is equal to the string {@code "true"}. (Beginning
	 * with version 1.0.2 of the Java<small><sup>TM</sup></small> platform, the
	 * test of this string is case insensitive.) A system property is accessible
	 * through <code>getProperty</code>, a method defined by the
	 * <code>System</code> class.
	 * <p>
	 * If there is no property with the specified name, or if the specified name
	 * is empty or null, then <code>false</code> is returned.
	 * 
	 * @param key
	 *            the name of the system property.
	 * @return the <code>boolean</code> value of the system property.
	 * @see java.lang.System#getProperty(java.lang.String)
	 * @see java.lang.System#getProperty(java.lang.String, java.lang.String)
	 * @see org.embl.cca.utils.datahandling.JavaSystem#getProperty(java.lang.String,
	 *      boolean)
	 */
	public static boolean getPropertyAsBoolean(String key) {
		return getPropertyAsBoolean(key, true);
	}

	/**
	 * Returns <code>true</code> if and only if the system property named by the
	 * argument exists and is equal to the string {@code "true"}. The ignoreCase
	 * argument specifies if the test of this string is case insensitive. A
	 * system property is accessible through <code>getProperty</code>, a method
	 * defined by the <code>System</code> class.
	 * <p>
	 * If there is no property with the specified name, or if the specified name
	 * is empty or null, then <code>false</code> is returned.
	 * 
	 * @param key
	 *            the name of the system property.
	 * @param ignoreCase
	 *            true to ignore case sensitivity, false to consider it
	 * @return the <code>boolean</code> value of the system property.
	 * @see java.lang.System#getProperty(java.lang.String)
	 * @see java.lang.System#getProperty(java.lang.String, java.lang.String)
	 * @see org.embl.cca.utils.datahandling.JavaSystem#getProperty(java.lang.String)
	 * @see org.embl.cca.utils.datahandling.JavaSystem#getProperty(java.lang.String,
	 *      java.lang.String)
	 * @see org.embl.cca.utils.datahandling.JavaSystem#getPropertyAsBoolean(java.lang.String)
	 * @see org.embl.cca.utils.datahandling.JavaSystem#getPropertyAsBoolean(java.lang.String,
	 *      java.lang.String, boolean)
	 */
	public static boolean getPropertyAsBoolean(String key, boolean ignoreCase) {
		return getPropertyAsBoolean(key, false, ignoreCase);
	}

	/**
	 * Returns <code>true</code> if and only if the system property named by the
	 * key argument exists and is equal to the string {@code "true"}, or does not
	 * exist and def argument is equal to the string {@code "true"}. The ignoreCase
	 * argument specifies if the test of this string is case insensitive. A
	 * system property is accessible through <code>getProperty</code>, a method
	 * defined by the <code>System</code> class.
	 * <p>
	 * If the specified name is empty or null, then <code>false</code> is returned.
	 * If there is no property with the specified name and def argument is equal to
	 * the string {@code "true"} considering ignoreCase argument,
	 * then true is returned, else false.
	 * 
	 * @param key
	 *            the name of the system property.
	 * @param def
	 *            a default value.
	 * @return the <code>boolean</code> value of the system property or the
	 *         default value if there is no property with that key.
	 * @return the <code>boolean</code> value of the system property or
	 *         true if there is no property with that key and def argument is
	 *         equal to the string {@code "true"}, or false if
	 *         the key argument is empty or null.
	 * @param ignoreCase
	 *            true to ignore case sensitivity, false to consider it
	 * @see java.lang.System#getProperty(java.lang.String)
	 * @see java.lang.System#getProperty(java.lang.String, java.lang.String)
	 * @see org.embl.cca.utils.datahandling.JavaSystem#getProperty(java.lang.String)
	 * @see org.embl.cca.utils.datahandling.JavaSystem#getProperty(java.lang.String,
	 *      java.lang.String)
	 * @see org.embl.cca.utils.datahandling.JavaSystem#getPropertyAsBoolean(java.lang.String)
	 * @see org.embl.cca.utils.datahandling.JavaSystem#getPropertyAsBoolean(java.lang.String,
	 *      boolean)
	 * 
	 * @see #setProperty
	 * @see java.lang.SecurityManager#checkPropertyAccess(java.lang.String)
	 * @see java.lang.System#getProperties()
	 */
	public static boolean getPropertyAsBoolean(final String key, final String def,
			final boolean ignoreCase) {
		return getPropertyAsBoolean(key, toBoolean(def, ignoreCase), ignoreCase);
	}

	/**
	 * Returns <code>true</code> if and only if the system property named by the
	 * key argument exists and is equal to the string {@code "true"}, or does not
	 * exist and def argument is true. The ignoreCase
	 * argument specifies if the test of this string is case insensitive. A
	 * system property is accessible through <code>getProperty</code>, a method
	 * defined by the <code>System</code> class.
	 * <p>
	 * If the specified name is empty or null, then <code>false</code> is returned.
	 * If there is no property with the specified name, then def argument is returned.
	 * 
	 * @param key
	 *            the name of the system property.
	 * @param def
	 *            a default value.
	 * @return the <code>boolean</code> value of the system property or the
	 *         def argument if there is no property with that key, or false if
	 *         the key argument is empty or null.
	 * @param ignoreCase
	 *            true to ignore case sensitivity, false to consider it
	 * @see java.lang.System#getProperty(java.lang.String)
	 * @see java.lang.System#getProperty(java.lang.String, java.lang.String)
	 * @see org.embl.cca.utils.datahandling.JavaSystem#getProperty(java.lang.String)
	 * @see org.embl.cca.utils.datahandling.JavaSystem#getProperty(java.lang.String,
	 *      java.lang.String)
	 * @see org.embl.cca.utils.datahandling.JavaSystem#getPropertyAsBoolean(java.lang.String)
	 * @see org.embl.cca.utils.datahandling.JavaSystem#getPropertyAsBoolean(java.lang.String,
	 *      boolean)
	 * 
	 * @see #setProperty
	 * @see java.lang.SecurityManager#checkPropertyAccess(java.lang.String)
	 * @see java.lang.System#getProperties()
	 */
	public static boolean getPropertyAsBoolean(String key, boolean def,
			boolean ignoreCase) {
		boolean result = false;
		try {
			final String value = getProperty(key);
			result = value == null ? def : toBoolean(value, ignoreCase);
		} catch (IllegalArgumentException e) { //key is empty
		} catch (NullPointerException e) { //key is null
		}
		return result;
	}

	/**
	 * Returns <code>true</code> if and only if the specified name by the
	 * argument is equal to the string {@code "true"}. (Beginning with version
	 * 1.0.2 of the Java<small><sup>TM</sup></small> platform, the test of this
	 * string is case insensitive.) A system property is accessible through
	 * <code>getProperty</code>, a method defined by the <code>System</code>
	 * class.
	 * <p>
	 * 
	 * @param name
	 *            name.
	 * @return the <code>boolean</code> value of the name.
	 */
	protected static boolean toBoolean(String name, boolean ignoreCase) {
		final String trueAsString = "true";
		return ((name != null) && (ignoreCase ? name
				.equalsIgnoreCase(trueAsString) : name.equals(trueAsString)));
	}

	/**
	 * Returns the converted <code>int</code> value of the system property
	 * named by the key argument.
	 * A system property is accessible through <code>getProperty</code>, a method
	 * defined by the <code>System</code> class.
	 * <p>
	 * If the specified key is empty or null, or the property not found,
	 * then <code>0</code> is returned. If the property value of the specified
	 * key is not parsable integer, NumberFormatException is thrown.
	 * 
	 * @param key
	 *            the name of the system property.
	 * @return the <code>int</code> value of the system property if the
	 *         property exists and is a parsable integer, else 0.
     * @exception  NumberFormatException  if the property does not contain a
     *             parsable integer.
	 * @see org.embl.cca.utils.datahandling.JavaSystem#getProperty(java.lang.String)
	 * 
	 * @see #setProperty
	 * @see java.lang.SecurityManager#checkPropertyAccess(java.lang.String)
	 * @see java.lang.System#getProperties()
	 */
	public static int getPropertyAsInt(final String key) throws NumberFormatException {
		int result = 0;
		try {
			final String value = getProperty(key);
			result = value == null ? 0 : toInt(value);
		} catch (NumberFormatException e) { //value is invalid
			throw e;
		} catch (IllegalArgumentException e) { //key is empty
		} catch (NullPointerException e) { //key is null
		}
		return result;
	}

	/**
	 * Returns the converted <code>int</code> value of the system property
	 * named by the key argument.
	 * A system property is accessible through <code>getProperty</code>, a method
	 * defined by the <code>System</code> class.
	 * <p>
	 * If the specified key is empty or null, or the property value of the
	 * specified key is not integer, then <code>0</code> is returned.
	 * If there is no property with the specified key, then def argument
	 * is returned.
	 * 
	 * @param key
	 *            the name of the system property.
	 * @param def
	 *            a default value.
	 * @return the <code>int</code> value of the system property if the
	 *         property exists and is a valid integer, the def argument if
	 *         the property does not exist, else 0.
	 * @see org.embl.cca.utils.datahandling.JavaSystem#getProperty(java.lang.String)
	 * 
	 * @see #setProperty
	 * @see java.lang.SecurityManager#checkPropertyAccess(java.lang.String)
	 * @see java.lang.System#getProperties()
	 */
	public static int getPropertyAsInt(final String key, final int def) {
		int result = def;
		try {
			result = getPropertyAsInt(key);
		} catch (NumberFormatException e) { //value is invalid
		}
		return result;
	}
	/**
	 * Returns the converted <code>int</code> value of the specified name.
	 * A system property is accessible through
	 * <code>getProperty</code>, a method defined by the <code>System</code>
	 * class.
	 * <p>
	 * 
	 * @param name
	 *            name.
	 * @return the <code>int</code> value of the name.
	 */
	protected static int toInt(final String name) throws NumberFormatException {
		return Integer.parseInt(name);
	}

	/**
	 * Sets the system property indicated by the specified key.
	 * <p>
	 * First, if a security manager exists, its
	 * <code>SecurityManager.checkPermission</code> method is called with a
	 * <code>PropertyPermission(key, "write")</code> permission. This may result
	 * in a SecurityException being thrown. If no exception is thrown, the
	 * specified property is set to the given value.
	 * <p>
	 * 
	 * @param key
	 *            the name of the system property.
	 * @param value
	 *            the value of the system property.
	 * @return the previous value of the system property, or <code>null</code>
	 *         if it did not have one.
	 * 
	 * @exception SecurityException
	 *                if a security manager exists and its
	 *                <code>checkPermission</code> method doesn't allow setting
	 *                of the specified property.
	 * @exception NullPointerException
	 *                if <code>key</code> or <code>value</code> is
	 *                <code>null</code>.
	 * @exception IllegalArgumentException
	 *                if <code>key</code> is empty.
	 * @see #getProperty
	 * @see java.lang.System#getProperty(java.lang.String)
	 * @see java.lang.System#getProperty(java.lang.String, java.lang.String)
	 * @see java.util.PropertyPermission
	 * @see SecurityManager#checkPermission
	 * @since 1.2
	 */
	public static String setProperty(String key, String value) {
		return System.setProperty(key, value);
	}

	/**
	 * Removes the system property indicated by the specified key.
	 * <p>
	 * First, if a security manager exists, its
	 * <code>SecurityManager.checkPermission</code> method is called with a
	 * <code>PropertyPermission(key, "write")</code> permission. This may result
	 * in a SecurityException being thrown. If no exception is thrown, the
	 * specified property is removed.
	 * <p>
	 * 
	 * @param key
	 *            the name of the system property to be removed.
	 * @return the previous string value of the system property, or
	 *         <code>null</code> if there was no property with that key.
	 * 
	 * @exception SecurityException
	 *                if a security manager exists and its
	 *                <code>checkPropertyAccess</code> method doesn't allow
	 *                access to the specified system property.
	 * @exception NullPointerException
	 *                if <code>key</code> is <code>null</code>.
	 * @exception IllegalArgumentException
	 *                if <code>key</code> is empty.
	 * @see #getProperty
	 * @see #setProperty
	 * @see java.util.Properties
	 * @see java.lang.SecurityException
	 * @see java.lang.SecurityManager#checkPropertiesAccess()
	 * @since 1.5
	 */
	public static String clearProperty(String key) {
		return System.clearProperty(key);
	}

	/**
	 * Gets the value of the specified environment variable. An environment
	 * variable is a system-dependent external named value.
	 * 
	 * <p>
	 * If a security manager exists, its {@link SecurityManager#checkPermission
	 * checkPermission} method is called with a
	 * <code>{@link RuntimePermission}("getenv."+name)</code> permission. This
	 * may result in a {@link SecurityException} being thrown. If no exception
	 * is thrown the value of the variable <code>name</code> is returned.
	 * 
	 * <p>
	 * <a name="EnvironmentVSSystemProperties"><i>System properties</i> and
	 * <i>environment variables</i></a> are both conceptually mappings between
	 * names and values. Both mechanisms can be used to pass user-defined
	 * information to a Java process. Environment variables have a more global
	 * effect, because they are visible to all descendants of the process which
	 * defines them, not just the immediate Java subprocess. They can have
	 * subtly different semantics, such as case insensitivity, on different
	 * operating systems. For these reasons, environment variables are more
	 * likely to have unintended side effects. It is best to use system
	 * properties where possible. Environment variables should be used when a
	 * global effect is desired, or when an external system interface requires
	 * an environment variable (such as <code>PATH</code>).
	 * 
	 * <p>
	 * On UNIX systems the alphabetic case of <code>name</code> is typically
	 * significant, while on Microsoft Windows systems it is typically not. For
	 * example, the expression
	 * <code>System.getenv("FOO").equals(System.getenv("foo"))</code> is likely
	 * to be true on Microsoft Windows.
	 * 
	 * @param name
	 *            the name of the environment variable
	 * @return the string value of the variable, or <code>null</code> if the
	 *         variable is not defined in the system environment
	 * @throws NullPointerException
	 *             if <code>name</code> is <code>null</code>
	 * @throws SecurityException
	 *             if a security manager exists and its
	 *             {@link SecurityManager#checkPermission checkPermission}
	 *             method doesn't allow access to the environment variable
	 *             <code>name</code>
	 * @see #getenv()
	 * @see ProcessBuilder#environment()
	 */
	public static String getenv(String name) {
		return System.getenv(name);
	}

	/**
	 * Returns an unmodifiable string map view of the current system
	 * environment. The environment is a system-dependent mapping from names to
	 * values which is passed from parent to child processes.
	 * 
	 * <p>
	 * If the system does not support environment variables, an empty map is
	 * returned.
	 * 
	 * <p>
	 * The returned map will never contain null keys or values. Attempting to
	 * query the presence of a null key or value will throw a
	 * {@link NullPointerException}. Attempting to query the presence of a key
	 * or value which is not of type {@link String} will throw a
	 * {@link ClassCastException}.
	 * 
	 * <p>
	 * The returned map and its collection views may not obey the general
	 * contract of the {@link Object#equals} and {@link Object#hashCode}
	 * methods.
	 * 
	 * <p>
	 * The returned map is typically case-sensitive on all platforms.
	 * 
	 * <p>
	 * If a security manager exists, its {@link SecurityManager#checkPermission
	 * checkPermission} method is called with a
	 * <code>{@link RuntimePermission}("getenv.*")</code> permission. This may
	 * result in a {@link SecurityException} being thrown.
	 * 
	 * <p>
	 * When passing information to a Java subprocess, <a
	 * href=#EnvironmentVSSystemProperties>system properties</a> are generally
	 * preferred over environment variables.
	 * 
	 * @return the environment as a map of variable names to values
	 * @throws SecurityException
	 *             if a security manager exists and its
	 *             {@link SecurityManager#checkPermission checkPermission}
	 *             method doesn't allow access to the process environment
	 * @see #getenv(String)
	 * @see ProcessBuilder#environment()
	 * @since 1.5
	 */
	public static java.util.Map<String, String> getenv() {
		return System.getenv();
	}

	/**
	 * Terminates the currently running Java Virtual Machine. The argument
	 * serves as a status code; by convention, a nonzero status code indicates
	 * abnormal termination.
	 * <p>
	 * This method calls the <code>exit</code> method in class
	 * <code>Runtime</code>. This method never returns normally.
	 * <p>
	 * The call <code>System.exit(n)</code> is effectively equivalent to the
	 * call: <blockquote>
	 * 
	 * <pre>
	 * Runtime.getRuntime().exit(n)
	 * </pre>
	 * 
	 * </blockquote>
	 * 
	 * @param status
	 *            exit status.
	 * @throws SecurityException
	 *             if a security manager exists and its <code>checkExit</code>
	 *             method doesn't allow exit with the specified status.
	 * @see java.lang.Runtime#exit(int)
	 */
	public static void exit(int status) {
		System.exit(status);
	}

	/**
	 * Runs the garbage collector.
	 * <p>
	 * Calling the <code>gc</code> method suggests that the Java Virtual Machine
	 * expend effort toward recycling unused objects in order to make the memory
	 * they currently occupy available for quick reuse. When control returns
	 * from the method call, the Java Virtual Machine has made a best effort to
	 * reclaim space from all discarded objects.
	 * <p>
	 * The call <code>System.gc()</code> is effectively equivalent to the call:
	 * <blockquote>
	 * 
	 * <pre>
	 * Runtime.getRuntime().gc()
	 * </pre>
	 * 
	 * </blockquote>
	 * 
	 * @see java.lang.Runtime#gc()
	 */
	public static void gc() {
		System.gc();
	}

	/**
	 * Runs the finalization methods of any objects pending finalization.
	 * <p>
	 * Calling this method suggests that the Java Virtual Machine expend effort
	 * toward running the <code>finalize</code> methods of objects that have
	 * been found to be discarded but whose <code>finalize</code> methods have
	 * not yet been run. When control returns from the method call, the Java
	 * Virtual Machine has made a best effort to complete all outstanding
	 * finalizations.
	 * <p>
	 * The call <code>System.runFinalization()</code> is effectively equivalent
	 * to the call: <blockquote>
	 * 
	 * <pre>
	 * Runtime.getRuntime().runFinalization()
	 * </pre>
	 * 
	 * </blockquote>
	 * 
	 * @see java.lang.Runtime#runFinalization()
	 */
	public static void runFinalization() {
		System.runFinalization();
	}

	/**
	 * Enable or disable finalization on exit; doing so specifies that the
	 * finalizers of all objects that have finalizers that have not yet been
	 * automatically invoked are to be run before the Java runtime exits. By
	 * default, finalization on exit is disabled.
	 * 
	 * <p>
	 * If there is a security manager, its <code>checkExit</code> method is
	 * first called with 0 as its argument to ensure the exit is allowed. This
	 * could result in a SecurityException.
	 * 
	 * @deprecated This method is inherently unsafe. It may result in finalizers
	 *             being called on live objects while other threads are
	 *             concurrently manipulating those objects, resulting in erratic
	 *             behavior or deadlock.
	 * @param value
	 *            indicating enabling or disabling of finalization
	 * @throws SecurityException
	 *             if a security manager exists and its <code>checkExit</code>
	 *             method doesn't allow the exit.
	 * 
	 * @see java.lang.Runtime#exit(int)
	 * @see java.lang.Runtime#gc()
	 * @see java.lang.SecurityManager#checkExit(int)
	 * @since JDK1.1
	 */
	@Deprecated
	public static void runFinalizersOnExit(boolean value) {
		System.runFinalizersOnExit(value);
	}

	/**
	 * Loads a code file with the specified filename from the local file system
	 * as a dynamic library. The filename argument must be a complete path name.
	 * <p>
	 * The call <code>System.load(name)</code> is effectively equivalent to the
	 * call: <blockquote>
	 * 
	 * <pre>
	 * Runtime.getRuntime().load(name)
	 * </pre>
	 * 
	 * </blockquote>
	 * 
	 * @param filename
	 *            the file to load.
	 * @exception SecurityException
	 *                if a security manager exists and its
	 *                <code>checkLink</code> method doesn't allow loading of the
	 *                specified dynamic library
	 * @exception UnsatisfiedLinkError
	 *                if the file does not exist.
	 * @exception NullPointerException
	 *                if <code>filename</code> is <code>null</code>
	 * @see java.lang.Runtime#load(java.lang.String)
	 * @see java.lang.SecurityManager#checkLink(java.lang.String)
	 */
	public static void load(String filename) {
		System.load(filename);
	}

	/**
	 * Loads the system library specified by the <code>libname</code> argument.
	 * The manner in which a library name is mapped to the actual system library
	 * is system dependent.
	 * <p>
	 * The call <code>System.loadLibrary(name)</code> is effectively equivalent
	 * to the call <blockquote>
	 * 
	 * <pre>
	 * Runtime.getRuntime().loadLibrary(name)
	 * </pre>
	 * 
	 * </blockquote>
	 * 
	 * @param libname
	 *            the name of the library.
	 * @exception SecurityException
	 *                if a security manager exists and its
	 *                <code>checkLink</code> method doesn't allow loading of the
	 *                specified dynamic library
	 * @exception UnsatisfiedLinkError
	 *                if the library does not exist.
	 * @exception NullPointerException
	 *                if <code>libname</code> is <code>null</code>
	 * @see java.lang.Runtime#loadLibrary(java.lang.String)
	 * @see java.lang.SecurityManager#checkLink(java.lang.String)
	 */
	public static void loadLibrary(String libname) {
		System.loadLibrary(libname);
	}

	/**
	 * Maps a library name into a platform-specific string representing a native
	 * library.
	 * 
	 * @param libname
	 *            the name of the library.
	 * @return a platform-dependent native library name.
	 * @exception NullPointerException
	 *                if <code>libname</code> is <code>null</code>
	 * @see java.lang.System#loadLibrary(java.lang.String)
	 * @see java.lang.ClassLoader#findLibrary(java.lang.String)
	 * @since 1.2
	 */
	public static String mapLibraryName(String libname) {
		return System.mapLibraryName(libname);
	}

}
