package org.embl.cca.utils.imageviewer;

import java.util.ArrayList;

public class ListenerList<E> extends ArrayList<E> implements Iterable<E> {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7160101602112825682L;

	/**
	 * Creates a listener list in which listeners are compared using equality.
	 */
	public ListenerList() {
		super();
	}

	/**
	 * Creates a listener list using the provided comparison mode.
	 * 
	 * @param mode The mode used to determine if listeners are the <a href="ListenerList.html#same">same</a>.
	 */
	public ListenerList(int mode) {
		super( mode );
	}

}
