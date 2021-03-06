package org.embl.cca.dviewer.ui.views;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.Page;
import org.embl.cca.dviewer.ui.editors.DViewerImageArrayEditorPart;
import org.embl.cca.dviewer.ui.editors.DViewerImageArrayViewPart;
import org.embl.cca.dviewer.ui.editors.DViewerListenerManager;

public class DViewerImagePage extends Page implements IAdaptable {

	public final static String DViewerImagePageAsString = "dViewer View";

	protected IViewSite site;
	protected DViewerImageArrayEditorPart origin;
	protected DViewerImageArrayViewPart viewer;

	protected final DViewerListenerManager listenerManager;

	final IPropertyListener inputListener = new IPropertyListener() {
		@Override
		public void propertyChanged(final Object source, final int propId) {
			if( origin.equals(source) && propId == IEditorPart.PROP_INPUT ) {
				viewer.showEditorInput(origin.getEditorInput());
			}
		}
	};

	/**
	 * Creates a DViewerImagePage for IDViewerControllable controllable.
	 * @param controllable
	 * @return
	 */
	public static DViewerImagePage getPageFor(final DViewerImageArrayEditorPart controllable) {
		return new DViewerImagePage(controllable);
	}

	public DViewerImagePage(final DViewerImageArrayEditorPart controllable) {
		Assert.isNotNull(controllable, "The controllable is null, it must not be null");
		origin = controllable;
		listenerManager = new DViewerListenerManager();
	}

	@Override
	public void dispose() {
		origin.removePropertyListener(inputListener);
		listenerManager.dispose();
		super.dispose();
	}

	protected void setViewSite(final IViewSite site) {
		this.site = site;
	}

	protected IViewSite getViewSite() {
		return site;
	}

	@Override
	public void createControl(final Composite parent) {
		try {
			viewer = new DViewerImageArrayViewPart(PlotType.IMAGE, listenerManager);
		} catch( final RuntimeException e ) {
			MessageDialog.open(MessageDialog.ERROR, getSite().getWorkbenchWindow().getShell(),
				"View Initializing Error", e.getLocalizedMessage(), MessageDialog.NONE);
			return;
		}
		Assert.isLegal(origin instanceof IReusableEditor);

		try {
			viewer.init(getViewSite());
		} catch (final PartInitException e) {
			final Shell shell = getSite().getShell();
			Assert.isNotNull(shell, "Environment error: can not find shell");
			MessageDialog.openError(shell,
					"View Initializing Error", new StringBuilder("Cannot initiate \"")
				.append(viewer.getClass().getName()).append("\".\n\n").append(e.getMessage()).toString());
			return;
		}
		viewer.setInput(origin.getEditorInput());
		viewer.createPartControl(parent);
		origin.addPropertyListener(inputListener);
	}

	@Override
	public Control getControl() {
		return viewer.getContainer();
	}

	@Override
	public void setFocus() {
		viewer.setFocus();
	}

	@Override
	public <T> T getAdapter(final Class<T> clazz) {
		if (String.class.equals(clazz)) {
			return clazz.cast(viewer.getTitle());
		}
		return viewer.getAdapter(clazz);
	}

}
