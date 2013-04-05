package org.embl.cca.utils.ui.view.fileexplorer;

import java.io.File;

import org.eclipse.jface.viewers.TreePath;
import org.eclipse.ui.IEditorInput;
import org.embl.cca.utils.ui.view.fileexplorer.FileView.FileSortType;

public interface IFileContentProvider {
	public TreePath getTreePath(File file);
	public TreePath getTreePath(Object object);

	public FileSortType getSort();
	public void setSort(FileSortType sort);

	public void refresh(File file);
	public IEditorInput getEditorInput(File file);
}
