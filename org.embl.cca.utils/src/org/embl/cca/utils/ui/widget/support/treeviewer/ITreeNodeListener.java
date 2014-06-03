package org.embl.cca.utils.ui.widget.support.treeviewer;

import org.embl.cca.utils.ui.widget.support.treeviewer.TreeNode.TreeNodeState;

public interface ITreeNodeListener {
	/**
	 * This method is called back, when children are ready.
	 * @param node The node of which children are ready.
	 * @param result The result.
	 * @param previousChildren The previous children of the node.
	 *   This parameter is passed because TreeViewer does not support
	 *   replacing or clearing children.
	 */
	public void childrenReady(final TreeNode node, final TreeNodeState result, final TreeNode[] previousChildren);
}
