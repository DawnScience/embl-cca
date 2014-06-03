package org.embl.cca.utils.ui.view.filenavigator;

import java.util.List;

import org.eclipse.swt.graphics.Image;
import org.embl.cca.utils.Activator;
import org.embl.cca.utils.ImageConstants;
import org.embl.cca.utils.datahandling.EFile;
import org.embl.cca.utils.ui.widget.support.treeviewer.TreeNode;

public class FileNode extends FileSystemEntryNode {

	public FileNode(final FileSystemContentProvider tcp, final FileSystemEntryNode parent, final EFile file) {
		super(tcp, parent, file);
	}

	@Override
	public Image getImage() {
		return Activator.getDefault().getImageRegistry().get(ImageConstants.IMG_FILE);
	}

	@Override
	public boolean canHaveChildren() {
		super.canHaveChildren(); //TODO ONLY FOR TESTING
		return false;
	}

	@Override
	protected TreeNodeState createChildren(final List<TreeNode> children) {
//		System.out.println(this.getClass().getName() + ": createChildren called, this: " + toString());
		if( getFile().exists() && !getFile().isDirectory())
			return TreeNodeState.TREENODE_NOT_FILLABLE;
		return TreeNodeState.TREENODE_NOT_EXISTING;
	}

	@Override
	public int getCategory() {
		return 1;
	}

	@Override
	public boolean equals(final Object object) {
		if( object == this ) return true;
//		if( object instanceof FileNode ) {
//			final FileNode fn = (FileNode)object;
//			//Could check fn key values, if there would be any
//		}
		return super.equals(object);
	}

	@Override
	public String toString() {
		return new StringBuilder("class=").append(getClass().getSimpleName())
			.append(", file=").append(getFile().toString()).append(", ").append(super.toString()).toString();
	}

}
