package org.embl.cca.utils.general;

import java.util.EventObject;

import org.eclipse.core.runtime.Assert;

/**
 * An event object describing a change to a named something.
 * <p>
 * This concrete class was designed to be instantiated, but may
 * also be subclassed if required.
 * </p>
 * <p>
 * The org.embl.cca site contains classes that report something
 * change events for internal state changes that may be of interest
 * to external parties. A special listener interface
 * (<code>ISomethingChangeListener</code>) is defined for this purpose,
 * and a typical class allow listeners to be registered via
 * an <code>addSomethingChangeListener</code> method.
 * </p>
 *
 * @see ISomethingChangeListener
 */
public class SomethingChangeEvent extends EventObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7510508183329836552L;

	/**
	 * The name of the changed something.
	 */
	protected final String somethingName;

	public final static String IMAGE_ARRAY_SOMETHING = "image_array";
	public final static String AUTO_SELECT_LATEST_NEW_IMAGE = "auto_select_latest_new_image";
	public final static String AUTO_DISPLAY_REMOTED_IMAGE = "auto_display_remoted_image";
	public final static String PHA_RADIUS = "pha_radius";
	public final static String DOWNSAMPLE_TYPE = "downsample_type";
	public final static String MOUSE_POSITION = "mouse_position";

	/**
	 * Creates a new something change event.
	 *
	 * @param source the object whose property has changed
	 * @param somethingName the something that has changed (must not be <code>null</code>)
	 */
	public SomethingChangeEvent(final Object source, final String somethingName) {
		super(source);
		Assert.isNotNull(somethingName);
		this.somethingName = somethingName;
	}

	/**
	 * Returns the name of the something that changed.
	 * <p>
	 * Warning: there is no guarantee that the something name returned
	 * is a constant string. Callers must compare something names using
	 * equals, not ==.
	 * </p>
	 *
	 * @return the name of the something that changed
	 */
	public String getSomethingName() {
		return somethingName;
	}
}
