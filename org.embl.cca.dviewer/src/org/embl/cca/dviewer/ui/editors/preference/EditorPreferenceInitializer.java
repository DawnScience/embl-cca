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

import java.util.Hashtable;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace.DownsampleType;
import org.embl.cca.dviewer.DViewerActivator;
import org.embl.cca.dviewer.ui.editors.utils.PHA;
import org.embl.cca.utils.datahandling.ADataValidator;
import org.embl.cca.utils.datahandling.RangeIntegerValidator;
import org.embl.cca.utils.datahandling.preference.PreferenceData;

/**
 * Class used to initialize default preference values.
 * 
 * @author Gábor Náray
 * 
*/
public class EditorPreferenceInitializer extends AbstractPreferenceInitializer {

	public static final PreferenceData DownsamplingType = new PreferenceData(
		DViewerEditorConstants.PREFERENCE_DOWNSAMPLING_TYPE, String.class, DViewerActivator.getLocalPreferenceStore(),
			new ADataValidator() {
				@Override
				public boolean isValid(final Object value) {
					try {
						final String string = (String)value;
						final int v = Integer.valueOf((String)value);
						return v>=0 && v<DownsampleType.values().length;
					} catch( final NumberFormatException | ClassCastException e ) {
						return false;
					}
				}
			});

	public static final PreferenceData ApplyPha = new PreferenceData(
		DViewerEditorConstants.PREFERENCE_APPLY_PHA, boolean.class, DViewerActivator.getLocalPreferenceStore(),
		null);

	public static final PreferenceData PhaRadius = new PreferenceData(
		DViewerEditorConstants.PREFERENCE_PHA_RADIUS, int.class, DViewerActivator.getLocalPreferenceStore(),
		new RangeIntegerValidator(DViewerEditorConstants.PHA_RADIUS_MIN, DViewerEditorConstants.PHA_RADIUS_MAX));

	static Hashtable<String, PreferenceData> preferenceDataMap = new Hashtable<String, PreferenceData>();
	static {
		preferenceDataMap.put(DownsamplingType.getName(), DownsamplingType);
		preferenceDataMap.put(ApplyPha.getName(), ApplyPha);
		preferenceDataMap.put(PhaRadius.getName(), PhaRadius);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @seeorg.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
//		final IPreferenceStore store = DViewerActivator.getLocalPreferenceStore();
		DownsamplingType.setDefault(String.valueOf(DownsampleType.MEAN.getIndex()));
		ApplyPha.setDefault(false);
		PhaRadius.setDefault(PHA.radiusDefault);
		
		DViewerActivator.getLocalPreferenceStore().setDefault(DViewerEditorConstants.PREFERENCE_VALID_VALUE_MIN, 0d);
//		EditorPreferenceHelper.setStoreDefaultValues(store);

//		//Better adjust current values if they are invalid
//		EditorPreferenceHelper.setStoreDefaultValue(store, DViewerEditorConstants.PREFERENCE_DOWNSAMPLING_TYPE);
////		EditorPreferenceHelper.fixStoreValue(store, DViewerEditorConstants.PREFERENCE_DOWNSAMPLING_TYPE);
//		EditorPreferenceHelper.setStoreDefaultValue(store, DViewerEditorConstants.PREFERENCE_APPLY_PHA);
//		EditorPreferenceHelper.setStoreDefaultValue(store, DViewerEditorConstants.PREFERENCE_PHA_RADIUS);
////		EditorPreferenceHelper.fixStoreValue(store, DViewerEditorConstants.PREFERENCE_PHA_RADIUS);
	}

}
