package org.embl.cca.utils.ui.view.filenavigator.preference;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.embl.cca.utils.Activator;

public class FileNavigatorPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private BooleanFieldEditor showDate;
	private BooleanFieldEditor showType;
	private BooleanFieldEditor showSize;
	private BooleanFieldEditor showScanCmd;
	private BooleanFieldEditor showComment;

	public FileNavigatorPreferencePage() {
		super(GRID);
	}

	/**
	 * Adds the given field editor to this page.
	 *
	 * @param editor the field editor
	 */
	protected void addField(final FieldEditor editor) {
		if( editor.getPreferenceName().equals(FileNavigatorPreferenceConstants.SHOW_INDEXRANGE_COLUMN))
			editor.setLabelText("Show index range column");
		super.addField(editor);
	}

	@Override
	protected void createFieldEditors() {

		showDate = new BooleanFieldEditor(FileNavigatorPreferenceConstants.SHOW_DATE_COLUMN,"Show date column",getFieldEditorParent());
		addField(showDate);

		showType = new BooleanFieldEditor(FileNavigatorPreferenceConstants.SHOW_INDEXRANGE_COLUMN,"Show type column",getFieldEditorParent());
		addField(showType);

		showSize = new BooleanFieldEditor(FileNavigatorPreferenceConstants.SHOW_SIZE_COLUMN, "Show size column",getFieldEditorParent());
		addField(showSize);

		showComment = new BooleanFieldEditor(FileNavigatorPreferenceConstants.SHOW_COMMENT_COLUMN, "Show comment column",getFieldEditorParent());
		addField(showComment);

		showScanCmd = new BooleanFieldEditor(FileNavigatorPreferenceConstants.SHOW_SCANCMD_COLUMN, "Show scan command column",getFieldEditorParent());
		addField(showScanCmd);
	}

	@Override
	public void init(final IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Preferences for browsing a file system using the File Navigator:");
	}

	/**
	 * Adjust the layout of the field editors so that
	 * they are properly aligned.
	 */
	@Override
	protected void adjustGridLayout() {
		super.adjustGridLayout();
		((GridLayout) getFieldEditorParent().getLayout()).numColumns = 1;
	}

	@Override
	protected void checkState() {
		super.checkState();
		setErrorMessage(null);
		setValid(true);
	}
}
