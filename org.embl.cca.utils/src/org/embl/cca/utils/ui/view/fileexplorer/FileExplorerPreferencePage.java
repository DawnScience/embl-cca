package org.embl.cca.utils.ui.view.fileexplorer;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.embl.cca.utils.Activator;

import uk.ac.diamond.scisoft.analysis.rcp.preference.FileNavigatorPreferencePage;
import uk.ac.diamond.scisoft.analysis.rcp.preference.PreferenceConstants;

public class FileExplorerPreferencePage extends FileNavigatorPreferencePage implements IWorkbenchPreferencePage {

	public FileExplorerPreferencePage() {
		super();
	}

    /**
     * Adds the given field editor to this page.
     *
     * @param editor the field editor
     */
	protected void addField(FieldEditor editor) {
		if( editor.getPreferenceName().equals(PreferenceConstants.SHOW_TYPE_COLUMN))
			editor.setLabelText("Show index range column");
		super.addField(editor);
	}

//	@Override
//	protected void createFieldEditors() {
//		super.createFieldEditors();
//	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Preferences for viewing a file system using the File Explorer:");
	}
}
