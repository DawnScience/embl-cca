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
import org.embl.cca.utils.datahandling.file.VirtualCollectionFile;

public class FileEditorInput implements IEditorInput/*, IPersistableElement*/ {

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
			return ((FileEditorInput) o).getName();
		}

		/*
		 * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
		 */
		@Override
		public Object getParent(Object o) {
			return null;
		}
	}

	protected File file;
	protected WorkbenchAdapter workbenchAdapter = new WorkbenchAdapter();

	/**
	 * @param file
	 */
	public FileEditorInput(File file) {
		Assert.isNotNull(file);
		this.file = file;
	}

	@Override
	public <T> T getAdapter(Class<T> clazz) {
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
		return file.getName();
	}

	@Override
	public String getToolTipText() {
		return file.getName();
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
