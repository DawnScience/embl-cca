/*-
 * Copyright 2012 Diamond Light Source Ltd.
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

package org.embl.cca.utils.datahandling;

import java.io.File;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.embl.cca.utils.general.Util;

public class FilePathEditorInput implements IEditorInput/*, IPersistableElement*/ {

	/**
	 * The workbench adapter which simply provides the label.
	 *
	 * @since 3.3
	 */
	protected static class WorkbenchAdapter implements IWorkbenchAdapter {
		/*
		 * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
		 */
		@Override
		public Object[] getChildren(Object o) {
			return null;
		}

		/*
		 * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
		 */
		@Override
		public ImageDescriptor getImageDescriptor(Object object) {
			return null;
		}

		/*
		 * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
		 */
		@Override
		public String getLabel(Object o) {
			return ((FilePathEditorInput) o).getName();
		}

		/*
		 * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
		 */
		@Override
		public Object getParent(Object o) {
			return null;
		}
	}

	protected String filePath;
	protected String equalityID;
	protected String name;
	protected WorkbenchAdapter workbenchAdapter = new WorkbenchAdapter();

	/**
	 * @param filePath
	 */
	public FilePathEditorInput(String filePath) {
		this(filePath, null);
	}

	/**
	 * @param filePath
	 * @param equalityID
	 */
	public FilePathEditorInput(String filePath, String equalityID) {
		this(filePath, equalityID, filePath);
	}

	/**
	 * @param filePath
	 * @param equalityID
	 * @param name
	 */
	public FilePathEditorInput(String filePath, String equalityID, String name) {
		Assert.isNotNull(filePath);
		this.filePath = filePath;
		this.equalityID = equalityID;
		this.name = name;
	}

	@Override
	public boolean equals(Object obj) {
		if( this == obj )
			return true;
		if (obj instanceof FilePathEditorInput) {
			FilePathEditorInput other = (FilePathEditorInput)obj;
			if( equalityID != null && other.equalityID != null )
				return equalityID.equals(other.equalityID);
			if( equalityID == null && other.equalityID == null )
				return filePath.equals(other.filePath);
			return false;
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return Util.hashCode(new Object[] {filePath, equalityID, name});
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") final Class adapter) {
		if(IWorkbenchAdapter.class.equals(adapter))
			return workbenchAdapter;
		if(adapter.isAssignableFrom(File.class)) { //adapter <= File
			return new File(filePath);
		}
		if(String.class.isAssignableFrom(adapter))
			return new String(filePath);
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}

	@Override
	public boolean exists() {
//		return fileStore.fetchInfo().exists();
		return true;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEqualityID() {
		return equalityID;
	}

	public boolean equalityIDEquals(String equalityID) {
		return this.equalityID != null && this.equalityID.equals(equalityID);
	}

	@Override
	public String getToolTipText() {
		return filePath;
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
//		return this;
	}

//	@Override
//	public void saveState(IMemento memento) {
////		FileStoreEditorInputFactory.saveState(memento, this);
//	}
//
//	@Override
//	public String getFactoryId() {
////		return FileStoreEditorInputFactory.ID;
//		return null;
//	}

}
