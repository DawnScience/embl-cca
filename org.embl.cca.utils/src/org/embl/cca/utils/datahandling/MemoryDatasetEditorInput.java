package org.embl.cca.utils.datahandling;

import java.io.File;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;
import org.embl.cca.utils.datahandling.text.StringUtils;
import org.embl.cca.utils.general.Util;

import uk.ac.diamond.scisoft.analysis.dataset.Dataset;

public class MemoryDatasetEditorInput implements IEditorInput {
	protected final String name;
	protected final Dataset dataset;
	protected final String filePath;
	protected final boolean newInput;
	protected final IProgressMonitor monitor;

	public MemoryDatasetEditorInput(final Dataset dataset) {
		this(dataset, true, null);
	}

	public MemoryDatasetEditorInput(final Dataset dataset, final String filePath) {
		this(dataset, filePath, true, null);
	}

	public MemoryDatasetEditorInput(final Dataset dataset, final boolean newInput, final IProgressMonitor monitor) {
		this(dataset, null, newInput, monitor);
	}
	public MemoryDatasetEditorInput(final Dataset dataset, final String filePath, final boolean newInput, final IProgressMonitor monitor) {
		Assert.isLegal(dataset != null, "The dataset parameter can not be null");
		Assert.isLegal(dataset.getName() != null, "The name of dataset can not be null");
		this.name = dataset.getName();
		this.dataset = dataset;
		this.filePath = filePath;
		this.newInput = newInput;
		this.monitor = monitor;
	}

	@Override
	public boolean equals(final Object obj) {
		if( this == obj )
			return true;
		if (obj instanceof MemoryDatasetEditorInput) {
			final MemoryDatasetEditorInput input = (MemoryDatasetEditorInput)obj;
			return name.equals(input.name)
				&& dataset.equals(input.dataset)
				&& newInput == input.newInput
				&& StringUtils.equalStringsEvenNulls(filePath, input.filePath);
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		return Util.hashCode(new Object[] {name, dataset, newInput, filePath});
	}

	/**
	 * Returns an object which is an instance of the given class
	 * associated with this object. Returns <code>null</code> if
	 * no such object can be found.
	 *
	 * @param adapter the adapter class to look up
	 * @return a object castable to the given class, 
	 *    or <code>null</code> if this object does not
	 *    have an adapter for the given class
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") final Class adapter) {
		if( adapter.isAssignableFrom(Dataset.class)) { //adapter <= Dataset
			return dataset;
		} else if( adapter.isAssignableFrom(String.class)) { //adapter <= String
			return name;
		} else if( adapter.isAssignableFrom(File.class)) { //adapter <= File
			if (adapter == File.class) {
				if( filePath != null )
					return new File(filePath);
			}
		}
		/**
		 * This implementation of the method declared by <code>IAdaptable</code>
		 * passes the request along to the platform's adapter manager; roughly
		 * <code>Platform.getAdapterManager().getAdapter(this, adapter)</code>.
		 */
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}

	/**
	 * Returns whether the editor input exists.
	 * <p>
	 * This method is primarily used to determine if an editor input should
	 * appear in the "File Most Recently Used" menu. An editor input will appear
	 * in the list until the return value of <code>exists</code> becomes
	 * <code>false</code> or it drops off the bottom of the list.
	 * </p> 
	 * @return <code>true</code> if the editor input exists;
	 *         <code>false</code> otherwise
	 */
	@Override
	public boolean exists() {
		return false;
	}

	/**
	 * Returns the image descriptor for this input.
	 * 
	 * <p>
	 * Note: although a null return value has never been permitted from this
	 * method, there are many known buggy implementations that return null.
	 * Clients that need the image for an editor are advised to use
	 * IWorkbenchPart.getImage() instead of IEditorInput.getImageDescriptor(),
	 * or to recover from a null return value in a manner that records the ID of
	 * the problematic editor input. Implementors that have been returning null
	 * from this method should pick some other default return value (such as
	 * ImageDescriptor.getMissingImageDescriptor()).
	 * </p>
	 * 
	 * @return the image descriptor for this input; may be <code>null</code> if
	 * there is no image.
	 */
	@Override
	public ImageDescriptor getImageDescriptor() {
		return ImageDescriptor.getMissingImageDescriptor();
	}

	/**
	 * Returns the name of this editor input for display purposes.
	 * <p>
	 * For instance, when the input is from a file, the return value would
	 * ordinarily be just the file name.
	 * </p>
	 * @return the name string; never <code>null</code>
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Returns an object that can be used to save the state of this editor
	 * input.
	 * 
	 * @return the persistable element, or <code>null</code> if this editor
	 *         input cannot be persisted
	 */
	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	/**
	 * Returns the tool tip text for this editor input. This text is used to
	 * differentiate between two input with the same name. For instance,
	 * MyClass.java in folder X and MyClass.java in folder Y. The format of the
	 * text varies between input types.
	 * </p>
	 * 
	 * @return the tool tip text; never <code>null</code>.
	 */
	@Override
	public String getToolTipText() {
		return "";
	}

	/**
	 * Returns the dataset of this editor input.
	 * 
	 * @return the dataset; never <code>null</code>
	 */
	public Dataset getDataset() {
		return dataset;
	}

	/**
	 * Returns the file path of this editor input, or null if no file is
	 * involved.
	 * <p>
	 * For instance, when the input is from a file, the return value would
	 * be the file path.
	 * </p>
	 * @return the file path string; it can be <code>null</code>
	 */
	public String getFilePath() {
		return filePath;
	}

	/**
	 * Returns true if newInput is true.
	 * 
	 * @return the newInput
	 */
	public boolean isNewInput() {
		return newInput;
	}

	/**
	 * Returns the monitor of this editor input if there is one.
	 * 
	 * @return the monitor, it can be <code>null</code>
	 */
	public IProgressMonitor getMonitor() {
		return monitor;
	}

	@Override
	public String toString() {
		return name + ": " + dataset.toString();
	}

}