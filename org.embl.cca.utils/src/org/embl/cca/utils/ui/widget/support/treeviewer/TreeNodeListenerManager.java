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


import org.eclipse.core.runtime.Assert;
import org.embl.cca.utils.errorhandling.ExceptionUtils;
import org.embl.cca.utils.threading.CommonThreading;
import org.embl.cca.utils.ui.widget.support.treeviewer.TreeNode.TreeNodeState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreeNodeListenerManager extends TreeSomethingListenerManager<ITreeNodeListener> {

	private static final Logger logger = LoggerFactory.getLogger(TreeNodeListenerManager.class);

	public TreeNodeListenerManager() {
		super();
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
				for( final ITreeNodeListener listener : treeSomethingListeners) {
					try {
						listener.childrenReady(node, result, previousChildren);
					} catch (final RuntimeException e) {
						ExceptionUtils.logError(logger, ExceptionUtils.UNEXPECTED_ERROR, e, this);
					}
				}
			}
		});
	}
}
