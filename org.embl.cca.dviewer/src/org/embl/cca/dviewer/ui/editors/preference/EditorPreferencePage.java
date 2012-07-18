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

import org.dawb.common.ui.image.PaletteFactory;
import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.trace.IImageTrace;
import org.dawb.common.ui.plot.trace.IImageTrace.DownsampleType;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.embl.cca.dviewer.Activator;


/**
 * The Imageviewer preference page for changing preferences which are then
 * stored in the preference store.
 * 
 * @author Gábor Náray
 * 
*/
public class EditorPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public EditorPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Preferences for dViewer editor.");
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	public void createFieldEditors() {

//		final ITrace trace = plottingSystem.updatePlot2D( !psfAction.isChecked() ? originalSet : psfSet, null, monitor );
//		if (trace instanceof IImageTrace) {
//			final IImageTrace imageTrace = (IImageTrace) trace;
//			System.out.println( "DownsampleType = " + imageTrace.getDownsampleType().getLabel() );
//		}
		DownsampleType[] downsampleTypes = DownsampleType.values();
		String[][] choices = new String[downsampleTypes.length][2];
		int i = -1;
		for (DownsampleType downsampleType : downsampleTypes) {
			i++;
			choices[i] = new String[] { downsampleType.getLabel(), "" + downsampleType.getIndex() };
		}
		addField(new ComboFieldEditor(EditorConstants.PREFERENCE_DOWNSAMPLING_TYPE,
				"&Downsampling type :", choices, getFieldEditorParent()));

		addField(new BooleanFieldEditor(EditorConstants.PREFERENCE_APPLY_PSF, "Apply PSF", getFieldEditorParent()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

}
