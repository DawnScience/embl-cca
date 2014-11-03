package org.embl.cca.dviewer.rcp.perspectives;

import org.dawb.common.ui.views.PlotDataView;
import org.dawb.workbench.ui.diffraction.DiffractionCalibrationConstants;
import org.dawnsci.plotting.tools.InfoPixelTool2D;
import org.dawnsci.plotting.views.ToolPageView;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.embl.cca.utils.ui.view.filenavigator.FileView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import uk.ac.diamond.sda.navigator.views.FileView;
//import uk.ac.diamond.sda.polling.preferences.PreferenceConstants;

/**
 *   DataPerspective
 *
 *   @author gerring
 *   @date Jul 19, 2010
 *   @project org.edna.workbench.application
 **/
public class DViewerPerspective implements IPerspectiveFactory {

	public static final String PERSPECTIVE_ID = "org.embl.cca.dviewer.rcp.perspectives.DViewerPerspective";

	private static Logger logger = LoggerFactory.getLogger(DViewerPerspective.class);

	/**
	 * Creates the initial layout for a page.
	 */
	public void createInitialLayout(final IPageLayout layout) {

		addFastViews(layout);
		addViewShortcuts(layout);
		addPerspectiveShortcuts(layout);

//		try {
//		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(MetadataPageView.ID);
//	} catch (PartInitException e) {
//		logger.error("can't open Metadata View", e);
//	}

		layout.setEditorAreaVisible(true);

		IFolderLayout folderLayout = null;
		final String editorArea = layout.getEditorArea();
		final String FIXED_VIEW_ID = ToolPageView.FIXED_VIEW_ID;

		folderLayout = layout.createFolder("vertToolFolder", IPageLayout.RIGHT, 0.7f, editorArea);
		folderLayout.addView(new StringBuffer(FIXED_VIEW_ID).append(':').append(DiffractionCalibrationConstants.DIFFRACTION_ID).toString());
		folderLayout.addView(PlotDataView.ID);
		folderLayout.addView(ToolPageView.TOOLPAGE_2D_VIEW_ID);

		folderLayout = layout.createFolder("horiToolFolder", IPageLayout.BOTTOM, 0.85f, editorArea);
		//InfoPixelTool is retired, it was/is/will be buggy
//		folderLayout.addView(new StringBuffer(FIXED_VIEW_ID).append(':').append(InfoPixelTool2D.INFOPIXELTOOL2D_ID).toString());
//		folderLayout.addView(ValuePageView.ID);
		folderLayout.addView(IPageLayout.ID_PROGRESS_VIEW);
//		folderLayout.addView(IConsoleConstants.ID_CONSOLE_VIEW);
		folderLayout.addView(ToolPageView.TOOLPAGE_1D_VIEW_ID); //TODO probably remove this, should know what it can contain

//		final ToolPageView view = (ToolPageView)getPage().showView(FIXED_VIEW_ID,
//			tool.getToolId(),
//			IWorkbenchPage.VIEW_ACTIVATE);
//		view.update(orig);
		IFolderLayout navigatorFolder = layout.createFolder("navigatorFolder", IPageLayout.LEFT, 0.4f, editorArea);
//		navigatorFolder.addView(ProjectExplorer.VIEW_ID);
		navigatorFolder.addView(FileView.ID);



//		final PreferenceManager preferenceManager = PlatformUI.getWorkbench()
//				.getPreferenceManager();
//		List l = preferenceManager.getElements(PreferenceManager.PRE_ORDER);
//		for (Object e : l) {
//			System.out.println("element: " + e.toString());
//			WorkbenchPreferenceNode wpn = (WorkbenchPreferenceNode) e; // IPreferenceNode
//			if (wpn.getLocalId().startsWith("org.dawnsci.plotting.system")) {
//				System.out.println("id=" + wpn.getId() + ", localId="
//						+ wpn.getLocalId() + ", category=" + wpn.getCategory()
//						+ ", pluginId=" + wpn.getPluginId() + ", label="
//						+ wpn.getLabel() + ", labelText=" + wpn.getLabelText());
//				for (IPreferenceNode sn : wpn.getSubNodes()) {
//					System.out.println("subnode: id=" + sn.getId()
//							+ ", labelText=" + sn.getLabelText());
//				}
//				IConfigurationElement ce = wpn.getConfigurationElement();
////					ce.getAttribute(name);
//				for (Object kp : wpn.getKeywordReferences()) {
//					System.out.println("keywordprefs: id=" + (String) kp);
//				}
//			}
//		}
//		IPreferenceNode[] arr = preferenceManager.getRootSubNodes();
//		for (IPreferenceNode pn : arr) {
//			System.out.println("Label:" + pn.getLabelText() + " ID:"
//					+ pn.getId());
//		}
//		preferenceManager.removeAll();
//
//		IEditorReference[] ers = PlatformUI.getWorkbench()
//				.getActiveWorkbenchWindow().getActivePage()
//				.getEditorReferences();
//		for (IEditorReference er : ers) {
//			IEditorPart ep = er.getEditor(false);
//			if (ep == null) {
//				System.out.println("This editor is null: " + er.getId() + ", "
//						+ er.getName() + ", " + er.getPartName());
//				continue;
//			}
//			IToolBarManager tbm = ep.getEditorSite().getActionBars()
//					.getToolBarManager();
////			IToolBarManager toolbarManager =
////				getViewSite().getActionBars().getToolBarManager();
////			IToolBarManager tbm = bars.getToolBarManager();
//			if (tbm != null) {
//				IContributionItem[] items = tbm.getItems();
//				for (IContributionItem item : items)
//					System.err.println("tbm.item:\t" + item);
//			}
//			if (tbm != null) {
//				ToolBar tb = ((ToolBarManager) tbm).getControl();
//				ToolItem tis[] = tb.getItems();
//				for (ToolItem item : tis)
//					System.err.println("tb.item:\t" + item.getText() + ", "
//							+ item.toString());
//			}
//			IMenuManager mm = ep.getEditorSite().getActionBars()
//					.getMenuManager();
//			if (mm != null) {
//				IContributionItem[] items = mm.getItems();
//				for (IContributionItem item : items)
//					System.err.println("mm.item:\t" + item.getId());
//			}
//			int a = 0;
//		}

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
