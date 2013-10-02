package org.embl.cca.utils.ui.view.fileexplorer.preference;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.embl.cca.utils.Activator;

public class FileExplorerPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		//File explorer preferences
		store.setDefault(FileExplorerPreferenceConstants.SHOW_DATE_COLUMN, true);
		store.setDefault(FileExplorerPreferenceConstants.SHOW_TYPE_COLUMN, true);
		store.setDefault(FileExplorerPreferenceConstants.SHOW_SIZE_COLUMN, true);
		store.setDefault(FileExplorerPreferenceConstants.SHOW_SCANCMD_COLUMN, false);
		store.setDefault(FileExplorerPreferenceConstants.SHOW_COMMENT_COLUMN, false);

	}
}
