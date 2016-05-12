/*-
 * Copyright 2012 Diamond Light Source Ltd. and EMBL
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
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.embl.cca.utils.Activator;
import org.embl.cca.utils.general.Util;

/**
 * This class is based on org.embl.cca.utils.datahandling.FileEditorInput.
 */
public class FilePathEditorInput implements IEditorInput, IPersistableElement {

	protected final class WorkbenchAdapter implements IWorkbenchAdapter {
		@Override
		public Object[] getChildren(final Object o) {
			return new Object[0];
		}

		@Override
		public ImageDescriptor getImageDescriptor(final Object object) {
			return FilePathEditorInput.this.getImageDescriptor();
		}

		@Override
		public String getLabel(final Object o) {
			return FilePathEditorInput.this.getName();
		}

		@Override
		public Object getParent(final Object o) {
			return FilePathEditorInput.this.getFile().getParent();
		}
	}

	/**
	 * Can not be null.
	 */
	protected String filePath;
	/**
	 * Can be null.
	 */
	protected String equalityId;
	/**
	 * Can be null.
	 */
	protected String name;
	protected final WorkbenchAdapter workbenchAdapter = new WorkbenchAdapter();

	/**
	 * @param filePath
	 */
	public FilePathEditorInput(final String filePath) {
		this(filePath, null);
	}

	/**
	 * @param filePath
	 * @param equalityId
	 */
	public FilePathEditorInput(final String filePath, final String equalityId) {
		this(filePath, equalityId, filePath);
	}

	/**
	 * @param filePath
	 * @param equalityId
	 * @param name
	 */
	public FilePathEditorInput(final String filePath, final String equalityId, final String name) {
		Assert.isNotNull(filePath);
		this.filePath = filePath;
		this.equalityId = equalityId;
		this.name = name;
	}

	@Override
	public int hashCode() {
		return Util.hashCode(new Object[] {filePath, equalityId, name});
	}

	@Override
	public boolean equals(final Object obj) {
		if( this == obj )
			return true;
		if (obj instanceof FilePathEditorInput) {
			FilePathEditorInput other = (FilePathEditorInput)obj;
			if( equalityId != null && other.equalityId != null )
				return equalityId.equals(other.equalityId);
			if( equalityId == null && other.equalityId == null )
				return filePath.equals(other.filePath);
			return false;
		}
		return super.equals(obj);
	}

	@Override
	public boolean exists() {
		return getFile().exists();
	}

	@Override
	public String getFactoryId() {
		return FilePathEditorInputFactory.getFactoryId();
	}

	public String getFilePath() {
		return filePath;
	}

	/**
	 * This method is for convenience. It returns the File corresponding
	 * to filePath.
	 * @return the File corresponding to filePath
	 */
	public File getFile() {
		return new File(filePath);
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return Activator.getImageDescriptor(getFile());
	}

	@Override
	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getEqualityId() {
		return equalityId;
	}

	public boolean equalityIdEquals(final String equalityId) {
		return this.equalityId != null && this.equalityId.equals(equalityId);
	}

	@Override
	public String getToolTipText() {
		return filePath;
	}

	@Override
	public IPersistableElement getPersistable() {
		return this;
	}

	@Override
	public void saveState(final IMemento memento) {
		FilePathEditorInputFactory.saveState(memento, this);
	}

	@Override
	public String toString() {
		return getClass().getName() + "(" + filePath + ")";
	}

	@Override
	public <T> T getAdapter(final Class<T> clazz) {
		if(IWorkbenchAdapter.class.equals(clazz))
			return clazz.cast(workbenchAdapter);
		if(clazz.isAssignableFrom(File.class)) { //adapter <= File
			return clazz.cast(new File(filePath));
		}
		if(String.class.isAssignableFrom(clazz))
			return clazz.cast(new String(filePath));
		return Platform.getAdapterManager().getAdapter(this, clazz);
	}

}
