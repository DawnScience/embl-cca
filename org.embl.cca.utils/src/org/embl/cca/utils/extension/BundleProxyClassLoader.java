//http://wiki.eclipse.org/index.php/BundleProxyClassLoader_recipe
package org.embl.cca.utils.extension;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import org.osgi.framework.Bundle;

public class BundleProxyClassLoader extends ClassLoader {
	protected Bundle bundle;
	protected ClassLoader parent;

	/**
	 * Creates a new class loader combining the <tt>ClassLoader</tt> returned by
	 * the method {@link #getSystemClassLoader()
	 * <tt>getSystemClassLoader()</tt>} as the parent class loader, and the
	 * bundle's class loader.
	 * 
	 * <p>
	 * If there is a security manager, its
	 * {@link SecurityManager#checkCreateClassLoader()
	 * <tt>checkCreateClassLoader</tt>} method is invoked. This may result in a
	 * security exception.
	 * </p>
	 * 
	 * @param bundle
	 *            The bundle of which class loader is used to load resources
	 * @throws SecurityException
	 *             If a security manager exists and its
	 *             <tt>checkCreateClassLoader</tt> method doesn't allow creation
	 *             of a new class loader.
	 * @see java.lang.ClassLoader
	 */
	public BundleProxyClassLoader(Bundle bundle) {
		super();
		this.bundle = bundle;
	}

	/**
	 * Creates a new class loader combining the specified parent class loader
	 * for delegation, and the bundle's class loader.
	 * 
	 * <p>
	 * If there is a security manager, its
	 * {@link SecurityManager#checkCreateClassLoader()
	 * <tt>checkCreateClassLoader</tt>} method is invoked. This may result in a
	 * security exception.
	 * </p>
	 * 
	 * @param bundle
	 *            The bundle of which class loader is used to load resources
	 * @param parent
	 *            The parent class loader
	 * 
	 * @throws SecurityException
	 *             If a security manager exists and its
	 *             <tt>checkCreateClassLoader</tt> method doesn't allow creation
	 *             of a new class loader.
	 * 
	 * @since 1.2
	 * @see java.lang.ClassLoader
	 */
	public BundleProxyClassLoader(Bundle bundle, ClassLoader parent) {
		super(parent);
		this.parent = parent;
		this.bundle = bundle;
	}

	// Note: Both ClassLoader.getResources(...) and bundle.getResources(...)
	// consult the boot classloader. As a result,
	// BundleProxyClassLoader.getResources(...) might return duplicate results
	// from the boot classloader. Prior to Java 5 Classloader.getResources was
	// marked final. If your target environment requires at least Java 5 you can
	// prevent the occurence of duplicate boot classloader resources by
	// overriding ClassLoader.getResources(...) instead of
	// ClassLoader.findResources(...).
	// public Enumeration findResources(String name) throws IOException {
	// return bundle.getResources(name);
	// }

	/**
	 * Finds the resource with the given name. A resource is some data (images,
	 * audio, text, etc) that can be accessed by class code in a way that is
	 * independent of the location of the code.
	 * 
	 * <p>
	 * The name of a resource is a '<tt>/</tt>'-separated path name that
	 * identifies the resource.
	 * 
	 * <p>
	 * This method will first search the parent class loader for the resource;
	 * if the parent is <tt>null</tt> the path of the class loader built-in to
	 * the virtual machine is searched. That failing, this method will invoke
	 * {@link #findResource(String)} to find the resource.
	 * </p>
	 * 
	 * @param name
	 *            The resource name
	 * 
	 * @return A <tt>URL</tt> object for reading the resource, or <tt>null</tt>
	 *         if the resource could not be found or the invoker doesn't have
	 *         adequate privileges to get the resource.
	 * 
	 * @since 1.1
	 */
	@Override
	public URL getResource(String name) {
		return (parent == null) ? findResource(name) : super.getResource(name);
	}

	/**
	 * Finds all the resources with the given name. A resource is some data
	 * (images, audio, text, etc) that can be accessed by class code in a way
	 * that is independent of the location of the code.
	 * 
	 * <p>
	 * The name of a resource is a <tt>/</tt>-separated path name that
	 * identifies the resource.
	 * 
	 * <p>
	 * The search order is described in the documentation for
	 * {@link #getResource(String)}.
	 * </p>
	 * 
	 * @param name
	 *            The resource name
	 * 
	 * @return An enumeration of {@link java.net.URL <tt>URL</tt>} objects for
	 *         the resource. If no resources could be found, the enumeration
	 *         will be empty. Resources that the class loader doesn't have
	 *         access to will not be in the enumeration.
	 * 
	 * @throws IOException
	 *             If I/O errors occur
	 * 
	 * @see #findResources(String)
	 * 
	 * @since 1.2
	 */
	@Override
	public Enumeration<URL> getResources(String name) throws IOException {
		return bundle.getResources(name);
	}

	/**
	 * Finds the resource with the given name. Class loader implementations
	 * should override this method to specify where to find resources. </p>
	 * 
	 * @param name
	 *            The resource name
	 * 
	 * @return A <tt>URL</tt> object for reading the resource, or <tt>null</tt>
	 *         if the resource could not be found
	 * 
	 * @since 1.2
	 */
	@Override
	public URL findResource(String name) {
		return bundle.getResource(name);
	}

	/**
	 * Finds the class with the specified <a href="#name">binary name</a>. This
	 * method should be overridden by class loader implementations that follow
	 * the delegation model for loading classes, and will be invoked by the
	 * {@link #loadClass <tt>loadClass</tt>} method after checking the parent
	 * class loader for the requested class. The default implementation throws a
	 * <tt>ClassNotFoundException</tt>. </p>
	 * 
	 * @param name
	 *            The <a href="#name">binary name</a> of the class
	 * 
	 * @return The resulting <tt>Class</tt> object
	 * 
	 * @throws ClassNotFoundException
	 *             If the class could not be found
	 * 
	 * @since 1.2
	 */
	@Override
	public Class<?> findClass(String name) throws ClassNotFoundException {
		return bundle.loadClass(name);
	}

	/**
	 * Loads the class with the specified <a href="#name">binary name</a>. The
	 * default implementation of this method searches for classes in the
	 * following order:
	 * 
	 * <p>
	 * <ol>
	 * 
	 * <li>
	 * <p>
	 * Invoke {@link #findLoadedClass(String)} to check if the class has already
	 * been loaded.
	 * </p>
	 * </li>
	 * 
	 * <li>
	 * <p>
	 * Invoke the {@link #loadClass(String) <tt>loadClass</tt>} method on the
	 * parent class loader. If the parent is <tt>null</tt> the class loader
	 * built-in to the virtual machine is used, instead.
	 * </p>
	 * </li>
	 * 
	 * <li>
	 * <p>
	 * Invoke the {@link #findClass(String)} method to find the class.
	 * </p>
	 * </li>
	 * 
	 * </ol>
	 * 
	 * <p>
	 * If the class was found using the above steps, and the <tt>resolve</tt>
	 * flag is true, this method will then invoke the
	 * {@link #resolveClass(Class)} method on the resulting <tt>Class</tt>
	 * object.
	 * 
	 * <p>
	 * Subclasses of <tt>ClassLoader</tt> are encouraged to override
	 * {@link #findClass(String)}, rather than this method.
	 * </p>
	 * 
	 * @param name
	 *            The <a href="#name">binary name</a> of the class
	 * 
	 * @param resolve
	 *            If <tt>true</tt> then resolve the class
	 * 
	 * @return The resulting <tt>Class</tt> object
	 * 
	 * @throws ClassNotFoundException
	 *             If the class could not be found
	 */
	@Override
	protected Class<?> loadClass(String name, boolean resolve)
			throws ClassNotFoundException {
		Class<?> clazz = (parent == null) ? findClass(name) : super.loadClass(
				name, false);
		if (resolve)
			super.resolveClass(clazz);
		return clazz;
	}
}
