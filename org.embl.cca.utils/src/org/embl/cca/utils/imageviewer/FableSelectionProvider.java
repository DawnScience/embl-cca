package org.embl.cca.utils.imageviewer;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;

public class FableSelectionProvider implements ISelectionProvider {
	private final ListenerList<ISelectionChangedListener> selectionListeners = new ListenerList<ISelectionChangedListener>();
	private FableSelection dSelection = null;

	public FableSelectionProvider() {
	}

	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		selectionListeners.add(listener);
	}

	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		selectionListeners.remove(listener);
	}

	@Override
	public ISelection getSelection() {
		return dSelection == null ? new StructuredSelection() : dSelection; // Eclipse requires that we do not return null
	}

	@Override
	public void setSelection(ISelection selection) {
		if (selection instanceof FableSelection)
			dSelection = (FableSelection) selection;
		else
			return;

		SelectionChangedEvent e = new SelectionChangedEvent(this, dSelection);
		for (ISelectionChangedListener listener : selectionListeners)
			listener.selectionChanged(e);
	}

}
