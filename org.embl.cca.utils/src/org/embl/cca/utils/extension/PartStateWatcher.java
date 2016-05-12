package org.embl.cca.utils.extension;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import org.apache.commons.collections4.Predicate;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;

/**
 * This is a workaround for finding activated and visible parts even before
 * part listeners could be added to the workbench page.
 * The find* methods might be called from restored parts before the init
 * method is called, so only the listener is added in the init method,
 * and activatedParts is set from the lazyInit on first call.
 */
public class PartStateWatcher {
	/**
	 * The activatedParts that contains the somewhen activated, now visible parts.
	 * It also keeps track of order of activations.
	 */
	protected Set<IWorkbenchPart> activatedParts = Collections.synchronizedSet(new LinkedHashSet<IWorkbenchPart>());

	protected final IPartListener2 partStateListener = new PartAdapter() {
		@Override
		public void partHidden(final IWorkbenchPartReference partRef) {
			final IWorkbenchPart workbenchPart = partRef.getPart(false);
			if( workbenchPart == null )
				return;
			activatedParts.remove(workbenchPart);
		}
		@Override
		public void partClosed(final IWorkbenchPartReference partRef) {
			final IWorkbenchPart workbenchPart = partRef.getPart(false);
			if( workbenchPart == null )
				return;
			activatedParts.remove(workbenchPart);
		}
		@Override
		public void partActivated(final IWorkbenchPartReference partRef) {
			final IWorkbenchPart workbenchPart = partRef.getPart(false);
			if( workbenchPart == null )
				return;
			if( activatedParts.contains(workbenchPart))
				activatedParts.remove(workbenchPart); //Removing for updating order
			activatedParts.add(workbenchPart);
		}
	};

	public PartStateWatcher() {
	}

	public void init() {
		CommonExtension.getCurrentPage().addPartListener(partStateListener);
	}

	public void dispose() {
		CommonExtension.getCurrentPage().removePartListener(partStateListener);
		activatedParts.clear();
	}

	protected void lazyInit() {
		if( !activatedParts.isEmpty() ) 
			return;
		for( final IViewReference viewReference : CommonExtension.getActivePage().getViewReferences()) {
			final IViewPart viewPart = viewReference.getView(false);
			if( viewPart != null && viewReference.getPage().isPartVisible(viewPart) )
				activatedParts.add(viewPart);
		}
		for( final IEditorReference editorReference : CommonExtension.getActivePage().getEditorReferences()) {
			final IEditorPart editorPart = editorReference.getEditor(false);
			if( editorPart != null && editorReference.getPage().isPartVisible(editorPart))
				activatedParts.add(editorPart);
		}
		final IEditorPart editorPart = CommonExtension.getCurrentEditor();
		if( editorPart != null )
			activatedParts.add(editorPart);
	}

	public IWorkbenchPart findLastMatchingActivatedPart(final Predicate<IWorkbenchPart> matcher) {
		lazyInit();
		final Iterator<IWorkbenchPart> activatedPartsIterator = activatedParts.iterator();
		//Reversing order of iterator
		final LinkedList<IWorkbenchPart> activatedPartsReversed = new LinkedList<>();
		while( activatedPartsIterator.hasNext() )
			activatedPartsReversed.addFirst(activatedPartsIterator.next());
		final Iterator<IWorkbenchPart> activatedPartsReversedIterator = activatedPartsReversed.iterator();
		while( activatedPartsReversedIterator.hasNext() ) {
			final IWorkbenchPart activatedPart = activatedPartsReversedIterator.next();
			if( matcher.evaluate(activatedPart) )
				return activatedPart;
		}
		return null;
	}

}
