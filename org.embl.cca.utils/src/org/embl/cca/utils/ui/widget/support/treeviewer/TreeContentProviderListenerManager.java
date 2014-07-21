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

import org.embl.cca.utils.errorhandling.ExceptionUtils;
import org.embl.cca.utils.ui.widget.support.treeviewer.TreeNode.TreeNodeState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreeContentProviderListenerManager extends TreeSomethingListenerManager<ITreeSomethingListener> {

	private static final Logger logger = LoggerFactory.getLogger(TreeContentProviderListenerManager.class);

	public TreeContentProviderListenerManager() {
		super();
	}

	/**
	 * Fires a node ready event, assuming current thread being GUI thread.
	 * 
	 * @param identifier The id of the ready node.
	 * @param result The result.
	 */
	public void fireNodeReady(final Object identifier, final TreeNodeState result) {
		for( final ITreeSomethingListener listener : treeSomethingListeners) {
			try {
				((ITreeContentProviderListener)listener).nodeReady(identifier, result);
			} catch (final RuntimeException e) {
				ExceptionUtils.logError(logger, ExceptionUtils.UNEXPECTED_ERROR, e, this);
			}
		}
	}
}
