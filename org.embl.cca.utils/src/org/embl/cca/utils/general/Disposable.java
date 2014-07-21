package org.embl.cca.utils.general;

public interface Disposable {

//	protected boolean disposed = false;

	public void dispose();
/*
	{	
		if( isDisposed() )
			return;
		//dispose this object
		disposed = true;
	}
*/
	public boolean isDisposed();
/*
	{
		return disposed;
	}
*/
}
