package org.embl.cca.utils.ui.view.filenavigator;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.widgets.TreeItem;
import org.embl.cca.utils.datahandling.EFile;
import org.embl.cca.utils.datahandling.text.StringUtils;
import org.embl.cca.utils.ui.widget.support.treeviewer.TreeContentProvider;
import org.embl.cca.utils.ui.widget.support.treeviewer.TreeNode;

public class FileSystemContentProvider extends TreeContentProvider {

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

	protected EFile getParentFile(final EFile file) {
		Assert.isNotNull(file, "The file is null, it must not be null.");
		if( file.getParentFile() != null )
			return file.getParentFile();
		final EFile superRootFile = ((FileSystemEntryNode)treeViewer.getInput()).getFile();
		if( file.equals(superRootFile))
			return null;
		return superRootFile;
	}

	public boolean hasTreeItemTheFile(final TreeItem treeItem, final EFile file) {
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
		Assert.isNotNull(file, "The file is null, it must not be null.");
		final EFile parentFile = getParentFile(file); 
		if( parentFile == null )
			return null;
		final TreeItem parent = findTreeItem(parentFile);
		final TreeItem[] children = parent == null ? treeViewer.getTree().getItems() : parent.getItems();
		final int iSup = children.length;
		for( int i = 0; i < iSup; i++ ) {
			if( hasTreeItemTheFile(children[i], file))
//			if( org.eclipse.jface.util.Util.equals(((FileSystemEntryNode)children[i].getData()).getFile(), file) )
				return children[i];
		}
		return parent;
	}

	/**
	 * Returns the TreeNode having the full path of file, or null if not found,
	 * or null if null nodes are allowed (not recommended).
	 * @param file The file for which the TreeItem is required.
	 * @return The TreeItem having the longest path part of file.
	 */
	protected FileSystemEntryNode findNode(final EFile file) {
		Assert.isNotNull(file, "The file is null, it must not be null.");
			final TreeItem result = findTreeItem(file);
			if( !hasTreeItemTheFile(result, file) )
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
	protected FileSystemEntryNode findNodeOrAncestor(final EFile file) {
		Assert.isNotNull(file, "The file is null, it must not be null.");
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
		final FileSystemEntryNode fsen = (FileSystemEntryNode)item.getData();
		if( !(item.getData() instanceof FolderNode || item.getData() instanceof FileNode) ) {
			if( fsen != null )
				itemName = fsen.getName();
			else {
				if( data != null )
					itemName = data.toString();
				else
					itemName = item.getText() + "(data=null)";
			}
			System.out.println("BUG: item has wrong data, name=" + treePath + "/" + itemName);
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
