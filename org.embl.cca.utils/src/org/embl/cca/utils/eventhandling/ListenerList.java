package org.embl.cca.utils.eventhandling;

import java.util.Iterator;
import java.util.Vector;

/**
 * This class is a thread safe list that is designed for storing lists of listeners.
 * Readers are given access to the underlying array data structure for reading,
 * with the trust that they will not modify the underlying array.
 * <p>
 * <a name="same">A listener list handles the <i>same</i> listener being added 
 * multiple times, and tolerates removal of listeners that are the same as other
 * listeners in the list.  For this purpose, listeners can be compared with each other 
 * using either equality or identity, as specified in the list constructor.
 * </p>
 * <p>
 * Use the <code>getListeners</code> method when notifying listeners. The recommended
 * code sequence for notifying all registered listeners of say,
 * <code>FooListener.eventHappened</code>, is:
 * 
 * <pre>
 * Vector<FooListener> listeners = myListenerList.getListeners();
 * for (int i = 0; i &lt; listeners.size(); ++i) {
 * 	listeners[i].eventHappened(event);
 * }
 * </pre>
 */
public class ListenerList<E> implements Iterable<E> {
//    implements List<E>, RandomAccess, Cloneable, java.io.Serializable, Collection<E>

	/**
	 * Mode constant (value 0) indicating that listeners should be considered
	 * the <a href="ListenerList.html#same">same</a> if they are equal.
	 */
	public static final int EQUALITY = 0;

	/**
	 * Mode constant (value 1) indicating that listeners should be considered
	 * the <a href="ListenerList.html#same">same</a> if they are identical.
	 */
	public static final int IDENTITY = 1;

	/**
	 * Indicates the comparison mode used to determine if two
	 * listeners are equivalent
	 */
	protected final boolean identity;

	/**
	 * The list of listeners.
	 */
	protected Vector<E> listeners;

	/**
	 * Creates a listener list in which listeners are compared using equality.
	 */
	public ListenerList() {
		this(EQUALITY);
	}

	/**
	 * Creates a listener list using the provided comparison mode.
	 * 
	 * @param mode The mode used to determine if listeners are the <a href="ListenerList.html#same">same</a>.
	 * @throws IllegalArgumentException if listener not found
	 */
	public ListenerList(int mode) {
		if (mode != EQUALITY && mode != IDENTITY)
			throw new IllegalArgumentException();
		this.identity = mode == IDENTITY;
		listeners = new Vector<E>();
	}

	/**
	 * Adds a listener to this list. This method has no effect if the <a href="ListenerList.html#same">same</a>
	 * listener is already registered.
	 * 
	 * @param listener the non-<code>null</code> listener to add
	 * @throws IllegalArgumentException if listener not found
	 */
	public void add(E listener) {
		if (listener == null)
			throw new IllegalArgumentException("The listener to add can not be null");
		synchronized (listeners) {
			if( identity ) {
				for( E item : listeners )
					if( item == listener )
						return;
			} else {
				if( listeners.contains(listener) )
					return;
			}
			listeners.add(listener);
		}
	}

	/**
	 * Returns a vector containing all the registered listeners.
	 * The resulting vector is affected by subsequent adds or removes.
	 * <p>
	 * Note: Callers of this method <b>must not</b> modify the returned array. 
	 *
	 * @return the list of registered listeners
	 */
	public Vector<E> getListeners() {
		return listeners;
	}

	/**
	 * Returns whether this listener list is empty.
	 *
	 * @return <code>true</code> if there are no registered listeners, and
	 *   <code>false</code> otherwise
	 */
	public boolean isEmpty() {
		return listeners.size() == 0;
	}

	/**
	 * Finds the <a href="ListenerList.html#same">same</a> listener in this list.
	 *
	 * @param listener the non-<code>null</code> listener to remove
	 * @return the index of found listener
	 * @throws IllegalArgumentException if listener not found
	 */
	public int indexOf(E listener) {
		if (listener == null)
			throw new IllegalArgumentException("The listener to find can not be null");
		int i = -1;
		synchronized (listeners) {
			if( identity ) {
				int iSup = listeners.size();
				for( i = 0; i < iSup; i++ )
					if( listeners.get(i) == listener )
						break;
				if( i == iSup )
					i = -1;
			} else {
				i = listeners.indexOf(listener);
			}
			return i;
		}
	}

	/**
	 * Finds the <a href="ListenerList.html#same">same</a> listener in this list.
	 *
	 * @param listener the non-<code>null</code> listener to remove
	 * @return true if listener is found, else false
	 * @throws IllegalArgumentException if listener not found
	 */
	public boolean contains(E listener) {
		return indexOf(listener) != -1;
	}

	/**
	 * Removes a listener from this list. Has no effect if the <a href="ListenerList.html#same">same</a> 
	 * listener was not already registered.
	 *
	 * @param listener the non-<code>null</code> listener to remove
	 * @throws IllegalArgumentException if listener not found
	 */
	public void remove(E listener) {
		if (listener == null)
			throw new IllegalArgumentException("The listener to remove can not be null");
		int i = -1;
		synchronized (listeners) {
			if( identity ) {
				int iSup = listeners.size();
				for( i = 0; i < iSup; i++ )
					if( listeners.get(i) == listener )
						break;
				if( i == iSup )
					i = -1;
			} else {
				i = listeners.indexOf(listener);
			}
			if( i != -1 )
				listeners.remove(i);
		}
	}

	/**
	 * Returns the number of registered listeners.
	 *
	 * @return the number of registered listeners
	 */
	public int size() {
		return listeners.size();
	}

	/**
	 * Removes all listeners from this list.
	 */
	public void clear() {
		listeners.clear();
	}

    /**
     * Returns an iterator over the elements in this list in proper sequence.
     * @return an iterator over the elements in this list in proper sequence
     */
	@Override
	public Iterator<E> iterator() {
		return listeners.iterator();
	}

}
