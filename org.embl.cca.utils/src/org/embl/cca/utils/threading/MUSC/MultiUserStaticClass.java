package org.embl.cca.utils.threading.MUSC;

import org.embl.cca.utils.eventhandling.ListenerList;
import org.embl.cca.utils.threading.MUSC.IMultiUserStaticClassListener.WrappedObject;

public class MultiUserStaticClass<CLASS> {
	protected ListenerList<IMultiUserStaticClassListener<CLASS>> users;
	protected CLASS object;
	protected final Boolean objectLock; //This is a lock, has no value

	public MultiUserStaticClass() {
		users = new ListenerList<IMultiUserStaticClassListener<CLASS>>(ListenerList.IDENTITY);
		object = null;
		objectLock = new Boolean(true);
	}

	public void registerMe(IMultiUserStaticClassListener<CLASS> user) {
		if( user == null )
			throw new IllegalArgumentException("The user argument can not be null");
		synchronized (objectLock) {
			boolean result = users.isEmpty();
			users.add(user);
			if( result )
				fireCreateObject();
		}
	}

	public void unregisterMe(IMultiUserStaticClassListener<CLASS> user) {
		synchronized (objectLock) {
			users.remove(user);
			if( users.isEmpty() )
				fireDisposeObject(user);
		}
	}

	public void registerObject(CLASS object) {
		if( this.object != null )
		this.object = object;
	}

	public void unregisterObject() {
		object = null;
	}

	public CLASS getObject() {
		return object;
	}

	protected void fireCreateObject() {
		WrappedObject<CLASS> wrappedObject = new WrappedObject<CLASS>(null);
		users.getListeners().firstElement().createObject(wrappedObject);
		object = wrappedObject.getObject();
		if( object == null )
			throw new RuntimeException("Listener did not returned the object");
	}

	protected void fireDisposeObject(IMultiUserStaticClassListener<CLASS> user) {
		WrappedObject<CLASS> wrappedObject = new WrappedObject<CLASS>(object);
		user.disposeObject(wrappedObject);
		object = null;
	}
}
