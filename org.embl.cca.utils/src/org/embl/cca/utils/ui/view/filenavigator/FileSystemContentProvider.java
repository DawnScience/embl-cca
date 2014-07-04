/*
 * Copyright 2014 Diamond Light Source Ltd. and EMBL
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

package org.embl.cca.utils.ui.view.filenavigator;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.widgets.TreeItem;
import org.embl.cca.utils.datahandling.EFile;
import org.embl.cca.utils.datahandling.text.StringUtils;
import org.embl.cca.utils.ui.widget.support.treeviewer.TreeContentProvider;
import org.embl.cca.utils.ui.widget.support.treeviewer.TreeNode;

public class FileSystemContentProvider extends TreeContentProvider {
	public static final String MSG_NULL_FILE = "The file is null, it must not be null.";

	public FileSystemContentProvider() {
		super();
	}

	@Override
	public Object[] getChildren(final Object parentElement) {
		return ((FileSystemEntryNode) parentElement).getChildren();
	}

	@Override
	public Object getParent(final Object element) {
		return ((FileSystemEntryNode) element).getParent();
	}

	@Override
	public boolean hasChildren(final Object element) {
		return ((FileSystemEntryNode) element).hasChildren();
	}

	/**
	 * Creates a node, this is the node factory.
	 * @param fscp the file system content provider.
	 * @param parent the parent node.
	 * @param file the file what the node contains.
	 * @return the created node.
	 */
	protected static FileSystemEntryNode createNode(final FileSystemContentProvider fscp, final FileSystemEntryNode parent, final EFile file) {
		if (file.isDirectory())
			return new FolderNode(fscp, parent, file);
		else
			return new FileNode(fscp, parent, file);
	}

	@Override
	public TreeNode createSuperRootNode() {
		return new FolderNode(this, new EFile(StringUtils.EMPTY_STRING));
	}

	@Override
	public FileSystemEntryNode getSuperRootNode() {
		return (FileSystemEntryNode)super.getSuperRootNode();
	}

	@Override
	protected EFile getParentValue(final Object value) {
		final EFile file = (EFile)value;
		Assert.isNotNull(value, MSG_NULL_FILE);
		if( file.getParentFile() != null )
			return file.getParentFile();
		final EFile superRootFile = getSuperRootNode().getFile();
		if( file.equals(superRootFile))
			return null;
		return superRootFile;
	}

	@Override
	protected FileSystemEntryNode createFakeNode(final Object parentValue, final Object value) {
		return FileSystemEntryNode.createFakeNode(this, (FileSystemEntryNode)parentValue, (EFile)value);
	}

	/**
	 * Returns true if file equals to file of tree node of treeItem.
	 * @param treeItem The TreeItem which contains the tree node containing
	 * the file for equality check. Note: its tree node can be null if
	 * the treeItem is a dummy child (placeholder).
	 * @param file The file for equality check.
	 * @return True if file equals to file of tree node of treeItem,
	 * which implies that treeItem is not dummy.
	 */
	protected boolean hasTreeItemTheFile(final TreeItem treeItem, final EFile file) {
		if( treeItem.getData() == null )
			return false;
		return org.eclipse.jface.util.Util.equals(((FileSystemEntryNode)treeItem.getData()).getFile(), file);
	}

	/**
	 * Returns the TreeItem having the longest path part of file. In best case,
	 * it is the TreeItem having the full path of file. To distinguish between
	 * the two cases, call hasTreeItemTheFile(result, file), which returns true
	 * in the latter (best) case. This method never returns null, except for
	 * the super root file, which should not be passed to this method.
	 * @param file The file for which the TreeItem is required.
	 * @return The TreeItem having the longest path part of file.
	 */
	protected TreeItem findTreeItem(final EFile file) {
		Assert.isNotNull(file, MSG_NULL_FILE);
		final EFile parentFile = getParentValue(file); 
		if( parentFile == null ) //Then file is SuperRootFile
			return null;
		final TreeItem parent = findTreeItem(parentFile);
		final TreeItem[] children = parent == null ? treeViewer.getTree().getItems() : parent.getItems();
		final int iSup = children.length;
		for( int i = 0; i < iSup; i++ ) {
			if( hasTreeItemTheFile(children[i], file))
				return children[i];
		}
		return parent;
	}

	/**
	 * Returns the TreeNode having the full path of file, or null if not found,
	 * or null if null nodes are allowed (not recommended).
	 * @param file The file for which the TreeItem is required.
	 * @return The TreeItem having the full path of file.
	 */
	public FileSystemEntryNode findNode(final EFile file) {
		Assert.isNotNull(file, MSG_NULL_FILE);
		final TreeItem result = findTreeItem(file);
		if( result != null && !hasTreeItemTheFile(result, file) )
			return null;
		return result == null ? getSuperRootNode() : (FileSystemEntryNode)result.getData();
	}

	/**
	 * Returns the TreeNode having the longest path part of file. In best case,
	 * it is the TreeNode having the full path of file. To distinguish between
	 * the two cases, call hasTreeItemTheFile(result, file), which returns true
	 * in the latter (best) case. This method never returns null, except if
	 * null nodes are allowed (not recommended).
	 * @param file The file for which the TreeItem is required.
	 * @return The TreeItem having the longest path part of file.
	 */
	public FileSystemEntryNode findNodeOrAncestor(final EFile file) {
		Assert.isNotNull(file, MSG_NULL_FILE);
		final TreeItem result = findTreeItem(file);
		return result == null ? getSuperRootNode() : (FileSystemEntryNode)result.getData();
	}

	/**
	 * Refreshes the node (and below) which represents the specified file.
	 * If the file is null, then the full tree is refreshed.
	 * @param file the file of which node is refreshed.
	 */
	public void refresh(final EFile file) {
		final FileSystemEntryNode node = file == null ? getSuperRootNode() : findNode(file);
		if( node == null )
			throw new UnsupportedOperationException("Only file of which node is displayed in TreeViewer can be refreshed.");
		node.refresh();
		treeViewer.refresh(node);
	}

	public void debug(final String treePath, final TreeItem item) {
		final Object data = item.getData();
		if( item.getItemCount() == 0 && data == null ) //Must be a dummy internal element for not complete parents
			return;
		String itemName;
		String className;
		final FileSystemEntryNode fsen = (FileSystemEntryNode)data;
		if( !(item.getData() instanceof FolderNode || item.getData() instanceof FileNode) ) {
			if( fsen != null ) {
				itemName = fsen.getName();
				className = fsen.getClass().getSimpleName();
			} else {
				if( data != null ) {
					itemName = data.toString();
					className = data.getClass().getSimpleName();
				} else {
					itemName = item.getText() + "(data=null)";
					className = "(null)";
				}
			}
			System.out.println("BUG: item has wrong data, name=" + treePath + "/" + itemName + ", class=" + className);
		} else
			itemName = fsen.getName();
		final TreeItem[] items = item.getItems();
		for( int i = 0; i < items.length; i++ )
			debug(treePath + "/" + itemName, items[i]);
	}

	public void debug() {
		System.out.println("debug called");
		final TreeItem[] items = treeViewer.getTree().getItems();
		for( int i = 0; i < items.length; i++ )
			debug("ROOT", items[i]);
	}

	@Override
	public void dispose() {
	}

}
