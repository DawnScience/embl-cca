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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.graphics.Image;
import org.embl.cca.utils.extension.CommonExtension;
import org.embl.cca.utils.general.Disposable;
import org.embl.cca.utils.general.Util;

/**
 * A data structure that is useful for implemented tree models, using the
 * node based functionality model. It means that the providers pass the most
 * functionality to nodes, thus the providers can be reused in different tree
 * models, while the nodes are application specific. In this node based
 * functionality model the common, application independent functionality is
 * placed in this class.
 * Normally this class would extend org.eclipse.jface.viewers.TreeNode, but
 * this class needs direct (and better) access to member values, which are
 * private.
 * 
 * @author Gábor Náray
 *
 */
public abstract class TreeNode implements Disposable {
	public static enum TreeNodeState {TREENODE_FILLED,
		TREENODE_NOT_EXISTING, TREENODE_NOT_FILLABLE;
	};
	public final String NULL_STR = "null";

	protected boolean disposed = false;

	/**
	 * The array of child tree nodes for this tree node. If there are no
	 * children, then this value may either by an empty array or
	 * <code>null</code>. There should be no <code>null</code> children in
	 * the array.
	 */
	protected TreeNode[] listChildren;

	/**
	 * True when children are determined, false otherwise. Setting this as
	 * false forces determining children at next occasion.
	 */
	protected boolean childrenValid;
	protected TreeNodeState state = null;
	protected final TreeNodeListenerManager listenerManager;
	/**
	 * The creator <code>TreeContentProvider</code>.
	 */
	protected final TreeContentProvider tcp;

	/**
	 * The parent tree node for this tree node. This value may be
	 * <code>null</code> if there is no parent.
	 */
	protected TreeNode parent;

	/**
	 * The identifier is a value contained in this node. This value may be
	 * anything, but if it is the same for different nodes
	 * (for example <code>null</code>), then identifying and finding a node
	 * is impossible.
	 */
	protected Object identifier;

	protected final Boolean fChildrenLock = new Boolean(true); //This is a lock, has no value
	protected final ChildrenListerJob childrenListerJob;

	protected final Map<List<TreeNode>, TreeContentProvider.TreeNodeRequest> nodePathRequests;

	protected class ChildrenListerJob extends Job {
		public ChildrenListerJob() {
			super("Get children");
			setUser(false);
			setSystem(true);
			setPriority(Job.LONG);
		}

		public void reschedule() {
			cancel();
			schedule();
		}

		@Override
		protected IStatus run(final IProgressMonitor monitor) {
			monitor.beginTask("Getting children", IProgressMonitor.UNKNOWN);
			final List<TreeNode> resultChildren = new ArrayList<TreeNode>();
			if( CommonExtension.debugMode )
				System.out.println("! FolderListerJob: before createChildren, this: " + TreeNode.this.toString());
			final TreeNodeState result = createChildren(resultChildren);
			final TreeNode[] previousChildren;
			synchronized (fChildrenLock) {
				previousChildren = listChildren;
				listChildren = resultChildren.toArray(new TreeNode[resultChildren.size()]);
				childrenValid = true;
				state = result;
			}
			if( CommonExtension.debugMode )
				System.out.println("! FolderListerJob: after createChildren, this: " + TreeNode.this.toString());
			listenerManager.fireChildrenReady(TreeNode.this, result, previousChildren);
			return Status.OK_STATUS;
		}
	}

	/**
	 * Constructs a new instance of <code>TreeNode</code>.
	 * 
	 * @param tcp The creator <code>TreeContentProvider</code>.
	 * @param parent The parent of this new <code>TreeNode</code>.
	 * @param identifier The identifier is a value held by this node;
	 * may be anything, but if it is the same for different nodes
	 * (for example <code>null</code>), then identifying and finding a node
	 * is impossible.
	 */
	public TreeNode(final TreeContentProvider tcp, final TreeNode parent, final Object identifier) {
		nodePathRequests = Collections.synchronizedMap(new HashMap<List<TreeNode>, TreeContentProvider.TreeNodeRequest>());
		listenerManager = new TreeNodeListenerManager();
		this.tcp = tcp;
		this.parent = parent;
		this.childrenListerJob = new ChildrenListerJob();
		this.identifier = identifier;
		childrenValid = false;
		addTreeNodeListener(tcp);
	}

	public Image getImage() {
		return null;
	}

	public TreeContentProvider getContentProvider() {
		return tcp;
	}

	/**
	 * Returns the parent node.
	 * 
	 * @return The parent node; may be <code>null</code> if there are no
	 *         parent nodes.
	 */
	public TreeNode getParent() {
//		if( CommonExtension.debugMode )
//			System.out.println(this.getClass().getName() + ": getParent called, this: " + toString());
		return parent;
	}

	/**
	 * Returns the value held by this node.
	 * 
	 * @return The value; may be anything.
	 */
	public Object getValue() {
		return identifier;
	}

	public boolean isChildrenValid() {
		return listChildren != null && childrenValid;
	}

	/**
	 * Returns whether the tree has any children.
	 * 
	 * @return <code>true</code> if its array of children is not
	 *         <code>null</code> and is non-empty; <code>false</code>
	 *         otherwise.
	 */
	public boolean hasChildren() {
//		if( CommonExtension.debugMode )
//			System.out.println(this.getClass().getName() + ": hasChildren called, this: " + toString());
		synchronized (fChildrenLock) {
			return isChildrenValid() ? listChildren.length > 0 : canHaveChildren();
		}
	}

	public void refresh() {
		synchronized (fChildrenLock) {
			childrenValid = false;
		}
	}

	/**
	 * Returns the child nodes. Empty arrays are converted to <code>null</code>
	 * before being returned.
	 * 
	 * @return The child nodes; may not be <code>null</code>.
	 *         There should be no <code>null</code> children in the array.
	 */
	public TreeNode[] getChildren() {
//		if( CommonExtension.debugMode )
//			System.out.println(this.getClass().getName() + ": getChildren(tv) called, this: " + toString());
		synchronized (fChildrenLock) {
//			final boolean refreshRequested = tcp.getRefreshRequest(this);
			final boolean initChildren = listChildren == null;
			if( initChildren )
				listChildren = new TreeNode[0];
			if( initChildren || !childrenValid ) {
				if( canHaveChildren() ) {
					if( CommonExtension.debugMode )
						System.out.println("! " + this.getClass().getName() + ": FolderListerJob.rescheduling, this: " + TreeNode.this.toString());
//					new Exception("rescheduling stack:").printStackTrace();
//					if( CommonExtension.debugMode )
//						System.out.println(ExceptionUtils.makeErrorMessage(new StringBuilder("HALLO"), new Exception("info"), this));
					childrenListerJob.reschedule();
				} else if( !childrenValid )
					childrenValid = true;
			}
			return listChildren;
		}
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

	@Override
	public boolean equals(final Object object) {
		if( object == this ) return true;
		if( !(object instanceof TreeNode) ) return false;
		final TreeNode tn = (TreeNode)object;
		return org.eclipse.jface.util.Util.equals(identifier, tn.identifier);
	}

	@Override
	public int hashCode() {
		return Util.hashCode(identifier);
	}

	@Override
	public String toString() {
		return new StringBuilder("parent=")
			.append(parent == null ? NULL_STR : parent.getName())
			.append(", state=").append(state == null ? NULL_STR : state.toString())
			.append(", children=").append(listChildren == null ? NULL_STR : Arrays.toString(listChildren)).toString();
	}

	public void addTreeNodeListener(final ITreeNodeListener listener) {
		listenerManager.addListener(listener);
	}

	public void removeTreeNodeListener(final ITreeNodeListener listener) {
		listenerManager.removeListener(listener);
	}

	public void removeTreeNodeListeners() {
		listenerManager.removeListeners();
	}

	/* subclasses should override this method */
	public abstract String getName();

	/* subclasses should override this method */
	protected abstract TreeNodeState createChildren(final List<TreeNode> children);

	/* subclasses should override this method */
	public boolean canHaveChildren() {
//		if( CommonExtension.debugMode ) {
//			final TreeLabelProvider tlp = (TreeLabelProvider)tcp.getTreeViewer().getLabelProvider();
//			System.out.println("! " + this.getClass().getName() + ".canHaveChildren called for " + tlp.getColumnText(this, 0)); //.getName()
//		}
		return false;
	}

	/* subclasses should override this method */
	public int getCategory() {
		return 0;
	}

	public abstract boolean isSuperRoot();
}
