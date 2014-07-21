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

public interface ITreeContentProviderListener extends ITreeSomethingListener {
	/**
	 * This method is called back when a node is ready, which means its
	 * children are ready (set).
	 * @param node The identifier of ready node.
	 * @param result The result.
	 */
	public void nodeReady(final Object identifier, final TreeNodeState result);
}
