package org.embl.cca.dviewer.ui.editors;

import org.dawb.common.util.list.ListenerList;
import org.eclipse.core.runtime.Assert;
import org.embl.cca.utils.errorhandling.ExceptionUtils;
import org.embl.cca.utils.general.ISomethingChangeListener;
import org.embl.cca.utils.general.SomethingChangeEvent;
import org.embl.cca.utils.threading.CommonThreading;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DViewerListenerManager {

	private static final Logger logger = LoggerFactory.getLogger(DViewerListenerManager.class);

	protected final ListenerList<ISomethingChangeListener> somethingChangeListeners;

	public DViewerListenerManager() {
		somethingChangeListeners = new ListenerList<ISomethingChangeListener>();
	}

	public void addSomethingListener(final ISomethingChangeListener listener) {
		somethingChangeListeners.add(listener);
	}

	public void removeSomethingListener(final ISomethingChangeListener listener) {
		somethingChangeListeners.remove(listener);
	}

	/**
	 * Fires a something changed event, from a GUI thread since the receivers
	 * are usually GUI oriented.
	 * 
	 * @param key
	 *            the id of the something that changed
	 */
	public void fireSomethingChanged(final String key) {
		Assert.isLegal(key != null, "The key argument must not be null");
		final SomethingChangeEvent event = new SomethingChangeEvent(this, key);
		CommonThreading.execUISynced(new Runnable() {
			@Override
			public void run() {
				for( final ISomethingChangeListener listener : somethingChangeListeners) {
					try {
						listener.somethingChange(event);
					} catch (final RuntimeException e) {
						ExceptionUtils.logError(logger, ExceptionUtils.UNEXPECTED_ERROR, e, this);
					}
				}
			}
		});
	}

	/**
	 * Sends a something changed event to the receiver, from a GUI thread since
	 * the receiver is usually GUI oriented.
	 * This method is used when the receiver as sender wanted to do something
	 * in this class, but it failed, and it is notified about the correct
	 * current state.
	 * 
	 * @param key
	 *            the id of the something that changed
	 * @param receiver
	 *            the receiver which receives the event
	 */
	public void sendSomethingChanged(final String key, final ISomethingChangeListener receiver) {
		Assert.isLegal(key != null && receiver != null, "Both the key and receiver arguments must not be null");
		final SomethingChangeEvent event = new SomethingChangeEvent(this, key);
		CommonThreading.execUISynced(new Runnable() {
			@Override
			public void run() {
				try {
					receiver.somethingChange(event);
				} catch (final RuntimeException e) {
					ExceptionUtils.logError(logger, ExceptionUtils.UNEXPECTED_ERROR, e, this);
				}
			}
		});
	}

	public void dispose() {
		somethingChangeListeners.clear();
	}
}
