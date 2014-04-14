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

import org.dawnsci.plotting.api.trace.IImageTrace.DownsampleType;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.ComboFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.embl.cca.dviewer.DViewerActivator;
import org.embl.cca.dviewer.ui.editors.DViewerImageEditorPart;
import org.embl.cca.dviewer.ui.editors.utils.PHA;


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
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	@Override
	public void createFieldEditors() {
		final String dTypeNames[] = DViewerEditorConstants.getSortedDownsampleTypeNames();
		final String[][] choices = new String[dTypeNames.length][2];
		int i = 0;
		for (final String dTypeName : dTypeNames) {
			final DownsampleType dType = DownsampleType.valueOf(dTypeName);
			choices[i++] = new String[] { dType.getLabel(), "" + dType.getIndex() };
		}
		addField(new ComboFieldEditor(DViewerEditorConstants.PREFERENCE_DOWNSAMPLING_TYPE,
				"&Downsampling type :", choices, getFieldEditorParent()));

		addField(new BooleanFieldEditor(DViewerEditorConstants.PREFERENCE_APPLY_PHA, "Apply " + PHA.featureShortName, getFieldEditorParent()));
		final IntegerFieldEditor phaRadiusEditor = new IntegerFieldEditor(DViewerEditorConstants.PREFERENCE_PHA_RADIUS,
			PHA.featureShortName + " radius", getFieldEditorParent(), 1+Math.round((float)Math.log10(DViewerEditorConstants.PHA_RADIUS_MAX)));
		phaRadiusEditor.setValidRange(DViewerEditorConstants.PHA_RADIUS_MIN, DViewerEditorConstants.PHA_RADIUS_MAX);
		addField(phaRadiusEditor);
//		phaRadiusEditor.isValid();
//		phaRadiusEditor.checkState()
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(final IWorkbench workbench) {
		setPreferenceStore(DViewerActivator.getLocalPreferenceStore());
		setDescription("Preferences for dViewer editor.");
	}

	@Override
	protected void checkState() {
		super.checkState();
	}
}
