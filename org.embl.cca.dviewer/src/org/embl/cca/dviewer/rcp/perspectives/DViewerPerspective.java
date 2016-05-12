/*
 * Copyright (c) 2012 Diamond Light Source Ltd. and EMBL
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.embl.cca.dviewer.rcp.perspectives;

import org.dawb.common.ui.views.PlotDataView;
import org.dawb.workbench.ui.diffraction.DiffractionCalibrationConstants;
import org.dawnsci.plotting.views.ToolPageView;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.embl.cca.utils.ui.view.filenavigator.FileView;

/**
 *   DViewerPerspective.
 *   Based on uk.ac.diamond.scisoft.analysis.rcp.DiffractionViewerPerspective.
 **/
public class DViewerPerspective implements IPerspectiveFactory {

	public static final String PERSPECTIVE_ID = "org.embl.cca.dviewer.rcp.perspectives.DViewerPerspective";
	final static String FIXED_VIEW_ID = ToolPageView.FIXED_VIEW_ID;

	/**
	 * Creates the initial layout for a page.
	 */
	public void createInitialLayout(final IPageLayout layout) {

		addFastViews(layout);
		addViewShortcuts(layout);
		addPerspectiveShortcuts(layout);

		layout.setEditorAreaVisible(true);

		final String editorArea = layout.getEditorArea();

		final IFolderLayout toolPageLayout = layout.createFolder("vertToolFolder", IPageLayout.RIGHT, 0.7f, editorArea);
		toolPageLayout.addView(new StringBuffer(FIXED_VIEW_ID).append(':').append(DiffractionCalibrationConstants.DIFFRACTION_ID).toString());
		toolPageLayout.addView(PlotDataView.ID);
		toolPageLayout.addView(ToolPageView.TOOLPAGE_2D_VIEW_ID);

		final IFolderLayout folderLayout = layout.createFolder("horiToolFolder", IPageLayout.BOTTOM, 0.85f, editorArea);
		//InfoPixelTool is retired, it was/is/will be buggy
//		folderLayout.addView(new StringBuffer(FIXED_VIEW_ID).append(':').append(InfoPixelTool2D.INFOPIXELTOOL2D_ID).toString());
//		folderLayout.addView(ValuePageView.ID);
		folderLayout.addView(IPageLayout.ID_PROGRESS_VIEW);
//		folderLayout.addView(IConsoleConstants.ID_CONSOLE_VIEW);
		folderLayout.addView(ToolPageView.TOOLPAGE_1D_VIEW_ID); //TODO probably remove this, should know what it can contain

		final IFolderLayout navigatorFolder = layout.createFolder("navigatorFolder", IPageLayout.LEFT, 0.4f, editorArea);
//		navigatorFolder.addView(ProjectExplorer.VIEW_ID);
		navigatorFolder.addView(FileView.ID);
	}

	/**
	 * Add fast views to the perspective.
	 */
	private void addFastViews(final IPageLayout layout) {
	}

	/**
	 * Add view shortcuts to the perspective.
	 */
	private void addViewShortcuts(final IPageLayout layout) {
	}

	/**
	 * Add perspective shortcuts to the perspective.
	 */
	private void addPerspectiveShortcuts(final IPageLayout layout) {
	}

}
