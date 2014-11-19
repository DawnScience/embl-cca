package org.embl.cca.dviewer.ui.views;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
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
	protected DViewerImageArrayEditorPart origin; //TODO this should be Input, because we are totally independent from originator (what about remotes?)
	protected DViewerImageArrayViewPart viewer;

	protected final DViewerListenerManager listenerManager;

	/**
	 * Creates a DViewerImagePage for IDViewerControllable controllable.
	 * @param controllable
	 * @return
	 */
	public static DViewerImagePage getPageFor(final DViewerImageArrayEditorPart controllable) {
		return new DViewerImagePage(controllable);
	}

	public DViewerImagePage(final DViewerImageArrayEditorPart controllable) {
		origin = controllable;
		listenerManager = new DViewerListenerManager();
	}

	@Override
	public void dispose() {
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
	public Object getAdapter(@SuppressWarnings("rawtypes") final Class adapter) {
		if (String.class.equals(adapter)) {
			return viewer.getTitle();
		}
		return viewer.getAdapter(adapter);
	}

}
