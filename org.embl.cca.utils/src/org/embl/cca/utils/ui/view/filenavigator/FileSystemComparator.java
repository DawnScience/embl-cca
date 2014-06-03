package org.embl.cca.utils.ui.view.filenavigator;

import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.embl.cca.utils.ui.widget.support.treeviewer.TreeComparator;

public class FileSystemComparator extends TreeComparator {
	//TODO Separate DIRS_FIRST, as it is an independent option
	//TODO Can add another sorters, like size, date
	public enum FileSortType {
		ALPHA_NUMERIC, ALPHA_NUMERIC_DIRS_FIRST, SIZE;
	}

	protected FileSortType sort;
	protected final TreeViewer treeViewer; //AbstractTreeViewer

	public FileSystemComparator(final TreeViewer treeViewer) {
		this(treeViewer, FileSortType.ALPHA_NUMERIC_DIRS_FIRST);
	}

	public FileSystemComparator(final TreeViewer treeViewer, final FileSortType sort) {
		super();
		this.treeViewer = treeViewer;
		this.sort = sort;
	}

	public FileSortType getSort() {
		return sort;
	}

	public void setSort(final FileSortType sort) {
		if( getSort().equals(sort))
			return;
		final Object[] expandedElements = treeViewer.getExpandedElements();
		this.sort = sort;
		treeViewer.refresh();
		treeViewer.setExpandedElements(expandedElements);
	}

	public int compare(final Viewer viewer, final Object e1, final Object e2) {
		if( sort.equals(FileSortType.ALPHA_NUMERIC_DIRS_FIRST) ) {
			final int cat1 = category(e1);
			final int cat2 = category(e2);
			if (cat1 != cat2)
				return cat1 - cat2;
		}
		final FileSystemEntryNode fn1 = (FileSystemEntryNode)e1;
		final FileSystemEntryNode fn2 = (FileSystemEntryNode)e2;
		switch(sort) {
		case ALPHA_NUMERIC:
		case ALPHA_NUMERIC_DIRS_FIRST:
			return Util.compare(fn1.getName(), fn2.getName());
		case SIZE: //TODO Sort the nulls at biggest: in current compare, those are least
			return Util.compare(fn1.getSize(), fn2.getSize());
		}
		return 0;
	}
}
