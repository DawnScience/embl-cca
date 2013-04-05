package org.embl.cca.utils.ui.view.fileexplorer;

import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.swt.widgets.TreeColumn;

public interface IFileColumnsLabelProvider {
	public int getColumnAmount();
	public void setColumn(final int column, TreeViewerColumn tVCol) throws Exception;
	public void setColumnVisible(final int column, TreeColumn tVCol, boolean isVis);
}
