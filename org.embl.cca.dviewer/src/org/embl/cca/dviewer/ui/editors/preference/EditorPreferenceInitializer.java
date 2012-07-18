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

import org.dawb.common.ui.plot.trace.IImageTrace.DownsampleType;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import org.embl.cca.dviewer.Activator;

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
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(EditorConstants.PREFERENCE_DOWNSAMPLING_TYPE, DownsampleType.MEAN.getIndex());
		store.setDefault(EditorConstants.PREFERENCE_APPLY_PSF, false);
	}

}
