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

package org.embl.cca.utils.ui.view.filenavigator;

import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.graphics.Image;
import org.embl.cca.utils.Activator;
import org.embl.cca.utils.ImageConstants;
import org.embl.cca.utils.datahandling.EFile;
import org.embl.cca.utils.datahandling.FileWithTag;
import org.embl.cca.utils.datahandling.file.FileLoader;
import org.embl.cca.utils.ui.widget.support.treeviewer.TreeNode;

public class FolderNode extends FileSystemEntryNode {

	public FolderNode(final FileSystemContentProvider tcp, final EFile file) {
		this(tcp, null, file);
	}

	public FolderNode(final FileSystemContentProvider tcp, final FileSystemEntryNode parent, final EFile file) {
		super(tcp, parent, file);
	}

	@Override
	public Image getImage() {
		return Activator.getDefault().getImageRegistry().get(ImageConstants.IMG_FOLDER);
	}

	@Override
	public boolean canHaveChildren() {
		super.canHaveChildren(); //TODO ONLY FOR TESTING
		return isReadable();
	}

	protected FileWithTag[] packCollections(final FileWithTag[] filesDirs) {
		Assert.isNotNull(filesDirs, "The filesDirs is null, it must not be null.");
		final ArrayList<FileWithTag> result = new ArrayList<FileWithTag>();
		@SuppressWarnings("serial")
		Vector<FileWithTag> thisFilesDirs = new Vector<FileWithTag>(0) {{
			this.elementData = (Object[])filesDirs.clone();
			this.elementCount = this.elementData.length;
		}};
		final FileLoader fl = new FileLoader() {
			final FileWithTag[] thisFilesDirs = filesDirs;
			protected FileWithTag[] listFiles(final FileWithTag parent, final FileFilter filter) {
				return (FileWithTag[])FileWithTag.filterFiles(FileWithTag.class, thisFilesDirs, filter);
			}
		};
		while( thisFilesDirs.size() > 0 ) {
			final FileWithTag file = thisFilesDirs.get(0);
//			try {
				if( file.isFile() ) {
					fl.setFilePath(file.getAbsolutePath());
					if( fl.isCollection() ) {
						result.add(fl.getCollectionDelegate());
						thisFilesDirs.removeAll(fl.getFile().getFilesFromAll());
						continue;
					}
				}
				result.add(file);
//			} catch (FileNotFoundException e) {
//			}
			thisFilesDirs.remove(0);
		}

		return result.toArray(new FileWithTag[0]);
	}

	protected FileWithTag[] getFileArray() {
		if( isSuperRoot() )
			return FileWithTag.listRoots();
		final FileWithTag[] files = ((FileWithTag)getFile()).listFiles();
		if( files == null )
			return null;
		return packCollections(files);
	}

	@Override
	protected TreeNodeState createChildren(final List<TreeNode> children) {
		System.out.println("! " + this.getClass().getName() + ": createChildren called, this: " + toString());
		final FileWithTag[] kids = getFileArray();
		if( kids == null ) {
			if (getFile().exists() && getFile().isDirectory())
				return TreeNodeState.TREENODE_NOT_FILLABLE;
			return TreeNodeState.TREENODE_NOT_EXISTING;
		}
		for (final FileWithTag file : kids) {
			children.add(FileSystemContentProvider.createNode(getContentProvider(), this, file));
		}
		System.out.println("! " + this.getClass().getName() + ": createChildren ready, " + toString() + ", children adding: " + children.toString());
		return TreeNodeState.TREENODE_FILLED;
	}

	@Override
	public int getCategory() {
		return 0;
	}

	@Override
	public boolean equals(final Object object) {
		if( object == this ) return true;
//		if( object instanceof FolderNode ) {
//			final FolderNode fn = (FolderNode)object;
//			//Could check fn key values, if there would be any
//		}
		return super.equals(object);
	}

	@Override
	public String toString() {
		return new StringBuilder("class=").append(getClass().getSimpleName())
			.append(", folder=").append(getFile().toString()).append(", ").append(super.toString()).toString();
	}

}
