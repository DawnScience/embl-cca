package org.embl.cca.utils.ui.widget.support.treeviewer;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.embl.cca.utils.datahandling.EFile;
import org.embl.cca.utils.ui.view.filenavigator.FileSystemEntryNode;
import org.embl.cca.utils.ui.widget.support.treeviewer.TreeNode.TreeNodeState;

public abstract class TreeContentProvider implements ITreeContentProvider, ITreeNodeListener {
	protected TreeViewer treeViewer; //AbstractTreeViewer

	public TreeContentProvider() {
	}

	public TreeViewer getTreeViewer() {
		return treeViewer;
	}

	@Override
	public Object[] getElements(final Object inputElement) { //For root element
		return getChildren(inputElement);
	}

	@Override
	public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
		if( oldInput == newInput )
			return;
		this.treeViewer = (TreeViewer) viewer;
		if (oldInput != null) {
			removeListenerFrom((TreeNode) oldInput);
		}
		if (newInput != null) {
			addListenerTo((TreeNode) newInput);
		}
	}

	/** Because the domain model does not have a richer
	 * listener model, recursively add this listener
	 * to each child node of the given node. */
	protected void addListenerTo(final TreeNode node) {
		node.addTreeNodeListener(this);
		if( node.listChildren != null ) {
			final int iSup = node.listChildren.length;
			for (int i = 0; i < iSup; i++)
				addListenerTo(node.listChildren[i]);
		}
	}

	/** Because the domain model does not have a richer
	 * listener model, recursively remove this listener
	 * from each child node of the given node. */
	protected void removeListenerFrom(final TreeNode node) {
		node.removeTreeNodeListener(this);
		if( node.listChildren != null ) {
			final int iSup = node.listChildren.length;
			for (int i = 0; i < iSup; i++)
				removeListenerFrom(node.listChildren[i]);
		}
	}

	@Override
	public void childrenReady(final TreeNode node, final TreeNodeState result, final TreeNode[] previousChildren) {
		final TreeViewer treeViewer = getTreeViewer();
//		final long t1 = System.nanoTime();
		switch( result ) {
			case TREENODE_FILLED:
//							System.out.println("FolderListerJob: before adding children, this: " + TreeNode.this.toString());
				treeViewer.remove(node, previousChildren);
				treeViewer.add(node, node.listChildren);
//				System.out.println("BP1* treeNodes added, " + TreeNode.this.toString());
//							System.out.println("FolderListerJob: after adding children, this: " + TreeNode.this.toString());
//							treeViewer.refresh(TreeNode.this, false);
			case TREENODE_NOT_FILLABLE:
				treeViewer.expandToLevel(node, 1); //Tell TV: show children of node (user asked)
//				long t2 = System.nanoTime();
//				System.out.println("Filling node took " + (t2-t1)/1000000000. + " sec");
				System.out.println("! " + this.getClass().getName() + ".childrenReady, in TREENODE_NOT_FILLABLE branch");
				break;
			case TREENODE_NOT_EXISTING:
				treeViewer.refresh(node.getParent(), false); //Tell TV: parent does not have this child
				break;
		}
	}

	public TreeNode getSuperRootNode() {
		return (TreeNode)treeViewer.getInput();
	}

	public boolean isSuperRoot(final Object value) {
		return org.eclipse.jface.util.Util.equals((getSuperRootNode()).getValue(), value);
	}

	public abstract TreeNode createSuperRootNode();
}
