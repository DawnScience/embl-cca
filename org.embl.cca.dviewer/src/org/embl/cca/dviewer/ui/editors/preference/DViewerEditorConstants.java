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
	public final static int PHA_RADIUS_MAX = 20;
	public final static int PHA_RADIUS_MIN = 1;

	public static final String PREFERENCE_DOWNSAMPLING_TYPE = "prefDownsamplingType";
	public static final String PREFERENCE_APPLY_PHA = "prefApplyPha";
	public static final String PREFERENCE_PHA_RADIUS = "prefPhaRadius";

	public final static String[] getSortedDownsampleTypeNames() {
		final String dTypeNames[] = new String[DownsampleType.values().length];
		for (final DownsampleType dType : DownsampleType.values())
			dTypeNames[dType.getIndex()] = dType.name();
		Arrays.sort(dTypeNames);
		return dTypeNames;
	}

}
