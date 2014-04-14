/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.embl.cca.dviewer.ui.editors.preference;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import org.embl.cca.dviewer.DViewerActivator;

/**
 * Class used to initialize default preference values.
 * 
 * @author Gábor Náray
 * 
*/
public class EditorPreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		//Better adjust current values if they are invalid
		final IPreferenceStore store = DViewerActivator.getLocalPreferenceStore();
		EditorPreferenceHelper.setStoreDefaultValue(store, DViewerEditorConstants.PREFERENCE_DOWNSAMPLING_TYPE);
//		EditorPreferenceHelper.fixStoreValue(store, DViewerEditorConstants.PREFERENCE_DOWNSAMPLING_TYPE);
		EditorPreferenceHelper.setStoreDefaultValue(store, DViewerEditorConstants.PREFERENCE_APPLY_PHA);
		EditorPreferenceHelper.setStoreDefaultValue(store, DViewerEditorConstants.PREFERENCE_PHA_RADIUS);
//		EditorPreferenceHelper.fixStoreValue(store, DViewerEditorConstants.PREFERENCE_PHA_RADIUS);
	}

}
