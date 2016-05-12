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
import org.embl.cca.utils.datahandling.file.VirtualCollectionFile;

/**
 * This class is based on org.eclipse.ui.part.FileEditorInput.
 */
public class FileEditorInput implements IEditorInput, IPersistableElement {

	protected final class WorkbenchAdapter implements IWorkbenchAdapter {
		@Override
		public Object[] getChildren(final Object o) {
			return new Object[0];
		}

		@Override
		public ImageDescriptor getImageDescriptor(final Object object) {
			return FileEditorInput.this.getImageDescriptor();
		}

		@Override
		public String getLabel(final Object o) {
			return FileEditorInput.this.getName();
		}

		@Override
		public Object getParent(final Object o) {
			return FileEditorInput.this.getFile().getParent();
		}
	}

	protected File file;
	protected final WorkbenchAdapter workbenchAdapter = new WorkbenchAdapter();

	/**
	 * @param file
	 */
	public FileEditorInput(final File file) {
		Assert.isNotNull(file);
		this.file = file;
	}

	@Override
	public int hashCode() {
		return file.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof FileEditorInput)) {
			return false;
		}
		FileEditorInput other = (FileEditorInput) obj;
		return file.equals(other.getFile());
	}

	@Override
	public boolean exists() {
		return file.exists();
	}

	@Override
	public String getFactoryId() {
		return FileEditorInputFactory.getFactoryId();
	}

	public File getFile() {
		return file;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return Activator.getImageDescriptor(file);
	}

	@Override
	public String getName() {
		return file.getName();
	}

	@Override
	public IPersistableElement getPersistable() {
		return this;
	}

	@Override
	public String getToolTipText() {
		return file.getAbsolutePath();
	}

	@Override
	public void saveState(final IMemento memento) {
		FileEditorInputFactory.saveState(memento, this);
	}

	@Override
	public String toString() {
		return getClass().getName() + "(" + getFile().getAbsolutePath() + ")";
	}

	@Override
	public <T> T getAdapter(final Class<T> clazz) {
		if (IWorkbenchAdapter.class.equals(clazz))
			return clazz.cast(workbenchAdapter);
		//For compatibility, trying to match simpler classes first
		if( clazz.isAssignableFrom(File.class) || String.class.isAssignableFrom(clazz) ) { //adapter <= File || String
			File resultFile = file;
			//For File or String, a file with common filename must be returned
			if( file instanceof VirtualCollectionFile )
				resultFile = ((VirtualCollectionFile)file).getFirstFileOfAll();
			if( String.class.isAssignableFrom(clazz) )
				return clazz.cast(new String(resultFile.getAbsolutePath()));
			return clazz.cast(resultFile.getAbsoluteFile());
		}
		if( clazz.isAssignableFrom(VirtualCollectionFile.class) && file instanceof VirtualCollectionFile ) //adapter <= VirtualCollectionFile
			return clazz.cast(file.getAbsoluteFile());
		return Platform.getAdapterManager().getAdapter(this, clazz);
	}

}
