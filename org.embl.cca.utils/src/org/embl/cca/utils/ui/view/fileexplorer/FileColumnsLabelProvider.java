package org.embl.cca.utils.ui.view.fileexplorer;

import java.util.Vector;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.widgets.TreeColumn;
import org.embl.cca.utils.Activator;

import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
import uk.ac.diamond.scisoft.analysis.rcp.preference.PreferenceConstants;

public class FileColumnsLabelProvider implements IFileColumnsLabelProvider {

//	protected static String[] titles = { "Name", "Date", "Type", "Size", "Comment", "Scan Command" }; //DLS settings
	protected static String[] titles = { "Name", "Date", "Index range", "Size", "Comment", "Scan Command" };
//	protected static int[]    defaultWidths = { 250, 120, 80, 150, 250, 300 }; //DLS settings
	protected static int[]    defaultWidths = { 250, 110, 70, 100, 150, 200 };
	protected int[] widths = defaultWidths.clone();
	protected IPreferenceStore store;
	protected boolean showDate;
	protected boolean showType;
	protected boolean showSize;
	protected boolean showComment;
	protected boolean showScanCmd;
	protected Vector<ColumnLabelProvider> columnLabelProvider = null;

	public FileColumnsLabelProvider() {
		store = Activator.getDefault().getPreferenceStore();
		showDate = store.getBoolean(PreferenceConstants.SHOW_DATE_COLUMN);
		showType = store.getBoolean(PreferenceConstants.SHOW_TYPE_COLUMN); //TODO Should be index range
		showSize = store.getBoolean(PreferenceConstants.SHOW_SIZE_COLUMN);
		showComment = store.getBoolean(PreferenceConstants.SHOW_COMMENT_COLUMN);
		showScanCmd = store.getBoolean(PreferenceConstants.SHOW_SCANCMD_COLUMN);
		if( !showDate )
			widths[1] = 0;
		if( !showType )
			widths[2] = 0;
		if( !showSize )
			widths[3] = 0;
		if( !showComment )
			widths[4] = 0;
		if( !showScanCmd )
			widths[5] = 0;
	}

	protected ColumnLabelProvider getFileLabelProvider(final int column) throws Exception {
		final int reqSize = column + 1;
		if( columnLabelProvider == null )
			columnLabelProvider = new Vector<ColumnLabelProvider>(reqSize);
		if( columnLabelProvider.size() <= column)
			columnLabelProvider.setSize(reqSize);
		if( columnLabelProvider.get(column) == null)
			columnLabelProvider.set(column, new FileLabelProvider(column));
		return columnLabelProvider.get(column);
	}

	@Override
	public int getColumnAmount() {
		return titles.length;
	}

	@Override
	public void setColumn(final int column, TreeViewerColumn tVCol) throws Exception {
		TreeColumn tCol = tVCol.getColumn();
		tCol.setText(titles[column]);
		tCol.setWidth(widths[column]);
		tCol.setMoveable(true);
		tVCol.setLabelProvider(getFileLabelProvider(column));
	}

	@Override
	public void setColumnVisible(final int column, TreeColumn tCol, boolean isVis) {
		widths[column] = isVis ? defaultWidths[column] : 0;
		tCol.setWidth(widths[column]);
	}
}
