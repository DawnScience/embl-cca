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

import java.io.File;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.text.DecimalFormat;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.graphics.Image;
import org.embl.cca.utils.Activator;
import org.embl.cca.utils.ImageConstants;
import org.embl.cca.utils.datahandling.EFile;
import org.embl.cca.utils.datahandling.text.StringUtils;
import org.embl.cca.utils.threading.CommonThreading;
import org.embl.cca.utils.ui.widget.support.treeviewer.TreeNode;

public class FileSystemEntryNode extends TreeNode implements IAdaptable {
	protected Boolean exist;
	protected Boolean readable;
	protected Boolean writable;
	protected Long size;
	protected FileTime lastModified;

	public FileSystemEntryNode(final FileSystemContentProvider tcp, final EFile file) {
		this(tcp, null, file);
	}

	public FileSystemEntryNode(final FileSystemContentProvider tcp, final FileSystemEntryNode parent, final EFile file) {
		this(tcp, parent, file, false);
	}

	protected FileSystemEntryNode(final FileSystemContentProvider tcp, final FileSystemEntryNode parent, final EFile file, final boolean fake) {
		super(tcp, parent, file);
		if( !fake && !isSuperRoot() )
			queryFolderProperties();
	}

	protected static FileSystemEntryNode createFakeNode(final FileSystemContentProvider tcp, final EFile file) {
		return new FileSystemEntryNode(tcp, null, file, true);
	}

	protected static FileSystemEntryNode createFakeNode(final FileSystemContentProvider tcp, final FileSystemEntryNode parent, final EFile file) {
		return new FileSystemEntryNode(tcp, parent, file, true);
	}

	protected void queryFolderProperties() {
		new Thread() {
			public void run() {
				size = getFile().length();
				lastModified = FileTime.fromMillis(getFile().lastModified());
				exist = getFile().exists();
				readable = getFile().isReadable();
				writable = getFile().isWritable();
				synchronized (getContentProvider()) {
					CommonThreading.execUISynced(new Runnable() {
						@Override
						public void run() {
							getContentProvider().getTreeViewer().update(FileSystemEntryNode.this, null);
						}
					});
				}
			};
		}.start();
	}

	protected EFile getFile() {
		return (EFile)identifier;
	}

	@Override
	public String getName() {
		return getFile().getName();
	}

	public Path getPath() {
		return getFile().toPath();
	}

	public String getAbsolutePathString() {
		return getFile().getAbsolutePath();
	}

	/**
	 * Returns true if file exists, else false. If exist is null
	 * (unknown), then true is returned. 
	 * @return true if file exists, else false
	 */
	public boolean exists() {
		return isExists(true);
	}

	/**
	 * Returns true if file exists, else false. If exist is null
	 * (unknown), then defaultIfUnknown is returned.
	 * @param defaultIfUnknown this value is returned if exist is null
	 * @return true if file exists, else false
	 */
	public boolean isExists(final boolean defaultIfUnknown) {
		return exist == null ? defaultIfUnknown : exist;
	}

	/**
	 * Returns true if file is readable, else false. If readable is null
	 * (unknown), then true is returned. 
	 * @return true if file is readable, else false
	 */
	public boolean isReadable() {
		return isReadable(true);
	}

	/**
	 * Returns true if file is readable, else false. If readable is null
	 * (unknown), then defaultIfUnknown is returned.
	 * @param defaultIfUnknown this value is returned if readable is null
	 * @return true if file is readable, else false
	 */
	public boolean isReadable(final boolean defaultIfUnknown) {
		return readable == null ? defaultIfUnknown : readable;
	}

	/**
	 * Returns true if file is writable, else false. If writable is null
	 * (unknown), then true is returned. 
	 * @return true if file is writable, else false
	 */
	public boolean isWritable() {
		return isWritable(true);
	}

	/**
	 * Returns true if file is writable, else false. If writable is null
	 * (unknown), then defaultIfUnknown is returned.
	 * @param defaultIfUnknown this value is returned if writable is null.
	 * @return true if file is writable, else false.
	 */
	public boolean isWritable(final boolean defaultIfUnknown) {
		return writable == null ? defaultIfUnknown : writable;
	}

	/**
	 * Returns the size of file system entry, it may be <code>null</code>.
	 * @return the size of file system entry, it may be <code>null</code>.
	 */
	public Long getSize() {
		return size;
	}

	/**
	 * Returns the size of file system entry. If size is null
	 * (unknown), then defaultIfUnknown is returned.
	 * @param defaultIfUnknown this value is returned if size is null.
	 * @return the size of file system entry.
	 */
	public long getSize(final long defaultIfUnknown) {
		return size == null ? defaultIfUnknown : size;
	}

	protected static final DecimalFormat df = new DecimalFormat("#.###");
	/**
	 * Returns the size of file system entry as String. If size is null
	 * (unknown), then defaultIfUnknown is returned.
	 * @param defaultIfUnknown this value is returned if size is null.
	 * @return the size of file system entry.
	 */
	public String getSizeAsString(final String defaultIfUnknown) {
		return size == null ? defaultIfUnknown : StringUtils.formatAsFileSize(size, df);
	}

	@Override
	public boolean equals(final Object object) {
		if( object == this ) return true;
//		if( object instanceof FileSystemEntryNode ) {
//			final FileSystemEntryNode fn = (FileSystemEntryNode)object;
//			//Could check fn key values, if there would be any
//		}
		return super.equals(object);
	}

	public Image getImage(final FileSystemLabelProvider fslp) {
		//Converting to File, so the service is not confused by protocol
		final Image result;
		if( getFile().getName().equals("XDS_ASCII.HKL") ) {
			result = Activator.getImage(ImageConstants.IMG_HKL);
		} else {
			result = fslp.getService().getIconForFile(getFile().toFile());
		}
		return result;
	}

	@Override
	public FileSystemContentProvider getContentProvider() {
		return (FileSystemContentProvider)super.getContentProvider();
	}


	@Override
	public FileSystemEntryNode getParent() {
		return (FileSystemEntryNode)super.getParent();
	}

	@Override
	public void refresh() {
		super.refresh();
		if( !isSuperRoot() )
			queryFolderProperties();
	}

	/* subclasses should override this method */
	@Override
	protected TreeNodeState createChildren(final List<TreeNode> children) {
		return TreeNodeState.TREENODE_FILLED;
	}

	@Override
	public boolean isSuperRoot() {
		return getParent() == null;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * Subclasses may override this method (however, if they do so, they
	 * should invoke the method on their superclass to ensure that the
	 * Platform's adapter manager is consulted).
	 */
	@Override
	public <T> T getAdapter(final Class<T> clazz) {
		if (File.class.equals(clazz)) //For compatibility with older stuff
			return clazz.cast(getFile().toFile());
		else if(EFile.class.equals(clazz)) //In case a newer stuff wants it
			return clazz.cast(getFile());
		/**
		 * This implementation of the method declared by <code>IAdaptable</code>
		 * passes the request along to the platform's adapter manager; roughly
		 * <code>Platform.getAdapterManager().getAdapter(this, adapter)</code>.
		 */
		return Platform.getAdapterManager().getAdapter(this, clazz);
	}
}
