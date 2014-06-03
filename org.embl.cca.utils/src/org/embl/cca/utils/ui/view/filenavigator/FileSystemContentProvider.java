package org.embl.cca.utils.ui.view.filenavigator;

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
	 * @param fscp the file system content provider
	 * @param parent the parent node
	 * @param file the file what the node contains
	 * @return the created node
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
		if( file.getParentFile() != null )
			return file.getParentFile();
		final EFile superRootFile = ((FileSystemEntryNode)treeViewer.getInput()).getFile();
		if( file.equals(superRootFile))
			return null;
		return superRootFile;
	}

	protected TreeItem findTreeItem(final EFile file) {
		final EFile parentFile = getParentFile(file); 
		if( parentFile == null )
			return null;
		final TreeItem parent = findTreeItem(parentFile);
		final TreeItem[] children = parent == null ? treeViewer.getTree().getItems() : parent.getItems();
		final int iSup = children.length;
		for( int i = 0; i < iSup; i++ ) {
			if( org.eclipse.jface.util.Util.equals(((FileSystemEntryNode)children[i].getData()).getFile(), file) )
				return children[i];
		}
		throw new RuntimeException("Item not found.");
	}

	protected FileSystemEntryNode findNode(final EFile file) {
		try {
			final TreeItem result = findTreeItem(file);
			return result == null ? getSuperRootNode() : (FileSystemEntryNode)result.getData();
		} catch( final RuntimeException e ) {
			return null;
		}
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
