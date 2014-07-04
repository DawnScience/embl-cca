/*
 * Copyright 2014 EMBL
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
