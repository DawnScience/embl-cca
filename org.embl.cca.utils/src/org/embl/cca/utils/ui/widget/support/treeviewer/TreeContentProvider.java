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

package org.embl.cca.utils.ui.widget.support.treeviewer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Widget;
import org.embl.cca.utils.datahandling.EFile;
import org.embl.cca.utils.datahandling.collection.ListUtils;
import org.embl.cca.utils.general.Disposable;
import org.embl.cca.utils.ui.widget.support.treeviewer.TreeNode.TreeNodeState;

public abstract class TreeContentProvider implements ITreeContentProvider, ITreeNodeListener, Disposable {
	public static final String MSG_NULL_VALUE = "The value is null, it must not be null.";
	public static enum TreeNodeRequest {TREENODE_EXPAND, TREENODE_SELECT};

	protected boolean disposed = false;

	protected final TreeSomethingListenerManager<ITreeSomethingListener> listenerManager;

	protected TreeViewer treeViewer; //AbstractTreeViewer

	public TreeContentProvider(final TreeSomethingListenerManager<ITreeSomethingListener> listenerManager) {
		this.listenerManager = listenerManager != null ? listenerManager : new TreeContentProviderListenerManager();
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

	public List<TreeNode> getTempNodePath(final Object value) {
		Assert.isNotNull(value, MSG_NULL_VALUE);
		if( isSuperRootValue(value) )
			return new ArrayList<TreeNode>();
		final Object parentValue = getParentValue(value);
		if( isSuperRootValue(parentValue) ) {
			final ArrayList<TreeNode> result = new ArrayList<TreeNode>();
			result.add(createFakeNode(null, value));
			return result;
		}
		final List<TreeNode> result = getTempNodePath(parentValue);
		result.add(createFakeNode(result.get(result.size() - 1), value));
		return result;
	}

	public abstract void debug();

	/**
	 * The core of requestFor* methods.
	 * @param node The node for which the request is served.
	 * @param treeNodeRequest The request type.
	 * @param existingTreePath The tree path where serving the request starts from.
	 * @param requiredTreePath The tree path of request which is not in the tree.
	 */
	protected void serveRequest(final TreeNode node, final TreeNodeRequest treeNodeRequest, final List<TreeNode> existingTreePath, final List<TreeNode> requiredTreePath) {
		if( !treeViewer.isExpandable(node)) {//Same as node.hasChildren()
			finishRequest(treeNodeRequest, node);
			return;
		}
		final boolean isChildrenValid = node.isChildrenValid();
		synchronized (node.nodePathRequests) {
			//Check state. If isChildrenValid, do not store request, because the request processing childrenReady will not be called back
			//Otherwise, store request if not in progress, or if this request overrides
			final boolean inProgress = node.nodePathRequests.containsKey(requiredTreePath);
			if( !isChildrenValid && (!inProgress || (treeNodeRequest.equals(TreeNodeRequest.TREENODE_SELECT)
					&& node.nodePathRequests.get(requiredTreePath).equals(TreeNodeRequest.TREENODE_EXPAND))) )
				node.nodePathRequests.put(requiredTreePath, treeNodeRequest);
			if( !inProgress ) //If inProgress, another request handler is working on expansion
				treeViewer.expandToLevel(new TreePath(existingTreePath.toArray()), 1);
			if( isChildrenValid ) {
				if( requiredTreePath.size() == 0 )
					finishRequest(treeNodeRequest, node);
				else //There will not be call back, have to continue here
					requestForPath(treeNodeRequest, existingTreePath, requiredTreePath);
			}
		}
	}

	/**
	 * Method for continuing the handling of request started by requestForValue.
	 * @param treeNodeRequest The request type.
	 * @param existingTreePath The tree path where serving the request starts from.
	 * If it is null, then it will be calculated.
	 * @param requiredTreePath The tree path of request which is not in the tree.
	 */
	protected void requestForPath(final TreeNodeRequest treeNodeRequest, List<TreeNode> existingTreePath, final List<TreeNode> requiredTreePath) {
		final TreeNode requiredNode = requiredTreePath.get(0); //0. element exists
		final Widget widget = treeViewer.testFindItem(requiredNode);
		if( widget == null ) //Unluck, the expected node is not there
			return;
		requiredTreePath.remove(0); //Dropping gotten 0. element
		if( existingTreePath != null )
			existingTreePath.add(requiredNode);
		else
			existingTreePath = getTempNodePath(((TreeNode)widget.getData()).getValue());
		serveRequest((TreeNode)widget.getData(), treeNodeRequest, existingTreePath, requiredTreePath);
	}

	/**
	 * Method for handling requests for any (even not existing)
	 * node, based on its specified value.
	 * Currently, expansion and selection is implemented. The selection does
	 * the same as expansion, in addition it selects when expansion is ready.
	 * @param treeNodeRequest The request type.
	 * @param value The value of node for which the request is served.
	 */
	protected void requestForValue(final TreeNodeRequest treeNodeRequest, final Object value) {
		final List<TreeNode> existingTreePath = getTempNodePath(value);
		final int iMax = existingTreePath.size() - 1;
		for( int i = iMax; i >= 0; i-- ) {
			final TreeNode tempNode = (TreeNode)existingTreePath.get(i);
			final Widget widget = treeViewer.testFindItem(tempNode);
			if( widget != null ) {
				final List<TreeNode> requiredTreePath = ListUtils.split(existingTreePath, i+1); //i. found, i+1. will be handled soon
				serveRequest((TreeNode)widget.getData(), treeNodeRequest, existingTreePath, requiredTreePath);
				break;
			}
		}
	}

	/**
	 * Final step of handling requestFor* methods.
	 * @param treeNodeRequest The request type.
	 * @param node The node for which the request is served.
	 */
	protected void finishRequest(final TreeNodeRequest treeNodeRequest, final TreeNode node) {
		if( treeNodeRequest.equals(TreeNodeRequest.TREENODE_SELECT) ) {
			final List<TreeNode> nodePath = getTempNodePath(node.getValue());
			//TODO Despite of revealing, at first expanding the parent is out of window
			treeViewer.reveal(new TreePath(nodePath.subList(0, nodePath.size() - 1).toArray()));
			treeViewer.setSelection(new StructuredSelection(new TreePath(nodePath.toArray())));
		}
	}

	@Override
	public void childrenReady(final TreeNode node, final TreeNodeState result, final TreeNode[] previousChildren) {
		synchronized (node.nodePathRequests) {
			try {
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
						treeViewer.expandToLevel(node, 1); //Tell TV: show children of node (user asked)
//				final FileSystemEntryNode fsen = (FileSystemEntryNode)node;
						final Iterator<Entry<List<TreeNode>, TreeNodeRequest>> it = node.nodePathRequests.entrySet().iterator();
						while( it.hasNext() ) {
							final Entry<List<TreeNode>, TreeNodeRequest> nodePathAndRequest = it.next();
							List<TreeNode> nodePath = nodePathAndRequest.getKey();
							if( nodePath.size() == 0 )
								finishRequest(nodePathAndRequest.getValue(), node);
							else
								requestForPath(nodePathAndRequest.getValue(), null, nodePath);
						}
						break;
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
				fireNodeReady(node.identifier, result);
			} finally {
				node.nodePathRequests.clear();
				debug();
			}
		}
	}

	public void addTreeContentProviderListener(final ITreeSomethingListener listener) {
		listenerManager.addListener(listener);
	}

	public void removeTreeContentProviderListener(final ITreeSomethingListener listener) {
		listenerManager.removeListener(listener);
	}

	public void removeTreeContentProviderListeners() {
		listenerManager.removeListeners();
	}

	@Override
	public boolean isDisposed() {
		return disposed;
	}

	public void dispose() { //TODO When should this be called?
		if( isDisposed() )
			return;
		//dispose this object
		listenerManager.dispose();
		disposed = true;
	}

	public void expandByValue(final EFile file) {
		requestForValue(TreeNodeRequest.TREENODE_EXPAND, file);
	}

	/**
	 * Sets a new selection for the tree viewer and makes it visible.
	 * Note: if file is the super root, nothing happens, because
	 * selecting super root has no sense.
	 * 
	 * @param file The new selection of file. If null, selection is cleared.
	 */
	public void setSelection(final EFile file) {
		if( file == null )
			treeViewer.setSelection(null);
		else
			requestForValue(TreeNodeRequest.TREENODE_SELECT, file);
	}

	public TreeNode getSuperRootNode() {
		return (TreeNode)treeViewer.getInput();
	}

	public boolean isSuperRootValue(final Object value) {
		return org.eclipse.jface.util.Util.equals((getSuperRootNode()).getValue(), value);
	}

	public abstract TreeNode createSuperRootNode();

	protected abstract Object getParentValue(final Object value);

	protected abstract TreeNode createFakeNode(final Object parentValue, final Object value);

	/* This method is to override if listenerManager is specified
	 * in constructor, otherwise exception will be thrown (because can not
	 * cast the listenerManager).
	 */
	protected void fireNodeReady(final Object identifier, final TreeNodeState result) {
		((TreeContentProviderListenerManager)listenerManager).fireNodeReady(identifier, result);
	}
}
