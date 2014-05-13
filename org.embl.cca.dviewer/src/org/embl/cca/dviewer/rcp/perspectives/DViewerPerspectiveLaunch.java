package org.embl.cca.dviewer.rcp.perspectives;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

/**
 * Data Browsing Perspective launcher
 *
 * @author Gábor Náray
 *
 **/
public class DViewerPerspectiveLaunch implements IWorkbenchWindowActionDelegate {

	@Override
	public void run(final IAction action) {
		try {
			PlatformUI.getWorkbench().showPerspective(DViewerPerspective.PERSPECTIVE_ID,PlatformUI.getWorkbench().getActiveWorkbenchWindow());
		} catch (final WorkbenchException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void selectionChanged(final IAction action, final ISelection selection) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void init(final IWorkbenchWindow window) {
		// TODO Auto-generated method stub
		
	}

}
