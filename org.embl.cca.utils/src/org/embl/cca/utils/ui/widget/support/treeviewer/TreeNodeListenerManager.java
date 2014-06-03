package org.embl.cca.utils.ui.widget.support.treeviewer;


import org.dawb.common.util.list.ListenerList;
import org.eclipse.core.runtime.Assert;
import org.embl.cca.utils.errorhandling.ExceptionUtils;
import org.embl.cca.utils.threading.CommonThreading;
import org.embl.cca.utils.ui.widget.support.treeviewer.TreeNode.TreeNodeState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreeNodeListenerManager {

	private static final Logger logger = LoggerFactory.getLogger(TreeNodeListenerManager.class);

	protected final ListenerList<ITreeNodeListener> treeNodeListeners;

	public TreeNodeListenerManager() {
		treeNodeListeners = new ListenerList<ITreeNodeListener>();
	}

	public void addTreeNodeListener(final ITreeNodeListener listener) {
		treeNodeListeners.add(listener);
	}

	public void removeTreeNodeListener(final ITreeNodeListener listener) {
		treeNodeListeners.remove(listener);
	}

	public void removeTreeNodeListeners() {
		treeNodeListeners.clear();
	}

	/**
	 * Fires a children ready event, from a GUI thread since the receivers
	 * (like the content provider) are usually GUI oriented.
	 * 
	 * @param key
	 *            the id of the something that changed
	 */
	public void fireChildrenReady(final TreeNode node, final TreeNodeState result, final TreeNode[] previousChildren) {
		Assert.isLegal(node != null, "The node argument must not be null");
		CommonThreading.execUISynced(new Runnable() {
			@Override
			public void run() {
				for( final ITreeNodeListener listener : treeNodeListeners) {
					try {
						listener.childrenReady(node, result, previousChildren);
					} catch (final RuntimeException e) {
						ExceptionUtils.logError(logger, ExceptionUtils.UNEXPECTED_ERROR, e, this);
					}
				}
			}
		});
	}

	public void dispose() {
		treeNodeListeners.clear();
	}
}
