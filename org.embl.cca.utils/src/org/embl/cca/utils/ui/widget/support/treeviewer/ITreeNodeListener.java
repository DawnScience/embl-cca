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
