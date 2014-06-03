package org.embl.cca.utils.ui.widget.support.treeviewer;

import org.eclipse.jface.viewers.ViewerComparator;

public class TreeComparator extends ViewerComparator {
	@Override
	public int category(final Object element) {
		return ((TreeNode)element).getCategory();
	}
}
