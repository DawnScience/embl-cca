package org.embl.cca.utils.ui.view.filenavigator.preference;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.embl.cca.utils.Activator;

public class FileNavigatorPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		final IPreferenceStore store = Activator.getDefault().getPreferenceStore();

		//File navigator preferences
		store.setDefault(FileNavigatorPreferenceConstants.SHOW_DATE_COLUMN, true);
		store.setDefault(FileNavigatorPreferenceConstants.SHOW_INDEXRANGE_COLUMN, true);
		store.setDefault(FileNavigatorPreferenceConstants.SHOW_SIZE_COLUMN, true);
		store.setDefault(FileNavigatorPreferenceConstants.SHOW_SCANCMD_COLUMN, false);
		store.setDefault(FileNavigatorPreferenceConstants.SHOW_COMMENT_COLUMN, false);

	}
}
