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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IElementFactory;
import org.eclipse.ui.IMemento;

/**
 * Factory for saving and restoring a <code>FileEditorInput</code>.
 * The stored representation of a <code>FileEditorInput</code> remembers
 * the full path of the file (that is, <code>File.getAbsolutePath</code>).
 * <p>
 * The workbench will automatically create instances of this class as required.
 * </p>
 * This class is based on org.eclipse.ui.part.FileEditorInputFactory.
*/
public class FileEditorInputFactory implements IElementFactory {
    /**
     * Factory id. The workbench plug-in registers a factory by this name
     * with the "org.eclipse.ui.elementFactories" extension point.
     */
    protected static final String ID_FACTORY = "org.embl.cca.utils.datahandling.FileEditorInputFactory";

    /**
     * Tag for the File.getAbsolutePath of the file.
     */
    protected static final String TAG_PATH = "path";

    /**
     * Creates a new factory.
     */
    public FileEditorInputFactory() {
    }

    /* (non-Javadoc)
     * Method declared on IElementFactory.
     */
    @Override
	public IAdaptable createElement(final IMemento memento) {
        // Get the file name.
        final String fileName = memento.getString(TAG_PATH);
        if (fileName == null) {
			return null;
		}
		return new FileEditorInput(new FileWithTag(fileName));
    }

    /**
     * Returns the element factory id for this class.
     *
     * @return the element factory id
     */
    public static String getFactoryId() {
        return ID_FACTORY;
    }

    /**
     * Saves the state of the given file editor input into the given memento.
     *
     * @param memento the storage area for element state
     * @param input the file editor input
     */
    public static void saveState(final IMemento memento, final FileEditorInput input) {
        memento.putString(TAG_PATH, input.getFile().getAbsolutePath());
    }
}
