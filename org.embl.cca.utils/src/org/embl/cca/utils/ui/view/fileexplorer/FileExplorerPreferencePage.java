package org.embl.cca.utils.ui.view.fileexplorer;

import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.embl.cca.utils.Activator;

import uk.ac.diamond.scisoft.analysis.rcp.preference.FileNavigatorPreferencePage;

public class FileExplorerPreferencePage extends FileNavigatorPreferencePage implements IWorkbenchPreferencePage {

	public FileExplorerPreferencePage() {
		super();
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Preferences for viewing a file system using the File Explorer:");
	}
}
