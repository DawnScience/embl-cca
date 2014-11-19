package org.embl.cca.dviewer.ui.views;

import org.dawb.common.ui.util.GridUtils;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.part.Page;
import org.embl.cca.dviewer.ui.editors.DViewerController;
import org.embl.cca.dviewer.ui.editors.IDViewerControllable;

public class DViewerControlsPage extends Page implements IAdaptable {

	public final static String DViewerControlsPageAsString = "dViewer Controls";

	protected final IDViewerControllable controllable;
	protected Composite mainContainer;

	/**
	 * Creates a HKLSelectorPage for ed editor part.
	 * @param ed
	 * @return
	 */
	public static DViewerControlsPage getPageFor(final IDViewerControllable controllable) {
		return new DViewerControlsPage(controllable);
	}

	public DViewerControlsPage(final IDViewerControllable controllable) {
		this.controllable = controllable;
	}

	@Override
	public void createControl(final Composite parent) {
		mainContainer = new Composite(parent, SWT.NONE);
		final GridLayout mainGridLayout = new GridLayout(1, false);
		mainContainer.setLayout(mainGridLayout);
//		mainContent.setBackground(org.eclipse.swt.widgets.Display.getCurrent().getSystemColor(org.eclipse.swt.SWT.COLOR_GREEN));
		GridUtils.removeMargins(mainContainer); //valid for GridLayout only

		final DViewerController dViewerController = new DViewerController(controllable, mainContainer);
		dViewerController.createImageEditorGUI(mainContainer); //create controls basically (without initializing)
		for( final Control c : mainContainer.getChildren() ) {
			c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		}
		dViewerController.initializeImageSelectorUI(); //initialize controls

		parent.pack(true);
	}

	@Override
	public Control getControl() {
		return mainContainer;
	}

	@Override
	public void setFocus() {
		mainContainer.setFocus();
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") final Class adapter) {
		if (adapter == String.class) {
			return DViewerControlsPageAsString;
		}
		return null;
	}

}
