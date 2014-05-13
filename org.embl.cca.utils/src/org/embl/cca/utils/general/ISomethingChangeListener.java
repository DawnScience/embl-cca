package org.embl.cca.utils.general;

import java.util.EventListener;

/**
 * Listener for something changes.
 * <p>
 * Usage:
 * <pre>
 * ISomethingChangeListener listener =
 *   new ISomethingChangeListener() {
 *      public void somethingChange(final SomethingChangeEvent event) {
 *         ... // code to deal with occurrence of something change
 *      }
 *   };
 * emitter.addSomethingChangeListener(listener);
 * ...
 * emitter.removeSomethingChangeListener(listener);
 * </pre>
 * </p>
 */
public interface ISomethingChangeListener extends EventListener {
	/**
	 * Notification that something has changed.
	 * <p>
	 * This method gets called when the observed object fires a something
	 * change event.
	 * </p>
	 *
	 * @param event the something change event object describing which something
	 * changed
	 */
	public void somethingChange(final SomethingChangeEvent event);
}
