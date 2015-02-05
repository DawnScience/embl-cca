package org.embl.cca.dviewer.ui.editors.preference;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.embl.cca.dviewer.DViewerActivator;
import org.embl.cca.utils.datahandling.RangeIntegerValidator;

public class PHAPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public PHAPreferencePage() {
		super("PHA Algorithm Preferences", GRID);
	}
	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(DViewerActivator.getLocalPreferenceStore());
		setDescription("Please define the parameters for the PHA algorithm. The image will not refresh when you do this, please toggle the pha algorithm button to do that.");
	}

	@Override
	protected void createFieldEditors() {
		
		final RangeIntegerValidator validator = (RangeIntegerValidator)EditorPreferenceInitializer.PhaRadius.getValidator();
		final IntegerFieldEditor phaRadiusEditor = new IntegerFieldEditor(DViewerEditorConstants.PREFERENCE_PHA_RADIUS,
			"Radius", getFieldEditorParent(), 1+Math.round((float)Math.log10(validator.getMax())));
		phaRadiusEditor.setValidRange(validator.getMin(), validator.getMax());
		addField(phaRadiusEditor);

	}


}
