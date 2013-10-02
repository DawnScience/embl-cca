package org.embl.cca.utils.ui.view.fileexplorer.preference;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.embl.cca.utils.Activator;

public class FileExplorerPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private BooleanFieldEditor showDate;
	private BooleanFieldEditor showType;
	private BooleanFieldEditor showSize;
	private BooleanFieldEditor showScanCmd;
	private BooleanFieldEditor showComment;

	public FileExplorerPreferencePage() {
		super(GRID);
	}

    /**
     * Adds the given field editor to this page.
     *
     * @param editor the field editor
     */
	protected void addField(FieldEditor editor) {
		if( editor.getPreferenceName().equals(FileExplorerPreferenceConstants.SHOW_TYPE_COLUMN))
			editor.setLabelText("Show index range column");
		super.addField(editor);
	}

//	@Override
//	protected void createFieldEditors() {
//		super.createFieldEditors();
//	}

	@Override
	protected void createFieldEditors() {

		showDate = new BooleanFieldEditor(FileExplorerPreferenceConstants.SHOW_DATE_COLUMN,"Show date column",getFieldEditorParent());
		addField(showDate);

		showType = new BooleanFieldEditor(FileExplorerPreferenceConstants.SHOW_TYPE_COLUMN,"Show type column",getFieldEditorParent());
		addField(showType);

		showSize = new BooleanFieldEditor(FileExplorerPreferenceConstants.SHOW_SIZE_COLUMN, "Show size column",getFieldEditorParent());
		addField(showSize);

		showComment = new BooleanFieldEditor(FileExplorerPreferenceConstants.SHOW_COMMENT_COLUMN, "Show comment column",getFieldEditorParent());
		addField(showComment);

		showScanCmd = new BooleanFieldEditor(FileExplorerPreferenceConstants.SHOW_SCANCMD_COLUMN, "Show scan command column",getFieldEditorParent());
		addField(showScanCmd);
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Preferences for viewing a file system using the File Explorer:");
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
