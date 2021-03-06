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

import java.util.Arrays;

import org.eclipse.dawnsci.plotting.api.trace.IImageTrace.DownsampleType;

/**
 * Constant definitions for plug-in preferences
 * 
 * @author Gábor Náray
 * 
 */
public class DViewerEditorConstants {
	public final static int BATCH_SIZE_MIN = 1; //Reasonable, should not be changed

	public final static int PHA_RADIUS_MAX = 20;
	public final static int PHA_RADIUS_MIN = 1; //Reasonable, should not be changed

	public final static int SHOW_EACH_NTH_IMAGE_MAX = 100;
	public final static int SHOW_EACH_NTH_IMAGE_MIN = 1; //Reasonable, should not be changed

	public static final String PREFERENCE_DOWNSAMPLING_TYPE = "org.embl.cca.dviewer.ui.prefDownsamplingType";
	public static final String PREFERENCE_APPLY_PHA = "org.embl.cca.dviewer.ui.prefApplyPha";
	public static final String PREFERENCE_PHA_RADIUS = "org.embl.cca.dviewer.ui.prefPhaRadius";
	public static final String PREFERENCE_VALID_VALUE_MIN = "org.embl.cca.dviewer.ui.validValueLower";

	public final static String[] getSortedDownsampleTypeNames() {
		final String dTypeNames[] = new String[DownsampleType.values().length];
		for (final DownsampleType dType : DownsampleType.values())
			dTypeNames[dType.getIndex()] = dType.name();
		Arrays.sort(dTypeNames);
		return dTypeNames;
	}

}
