package org.embl.cca.utils.imageviewer;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class MemoryImageEditorInput extends TwoDimFloatArrayData implements IEditorInput {
	String name;

	public MemoryImageEditorInput(String name, int width, int height, float [] data) {
		super( width, height, data );
		this.name = name;
	}

	public MemoryImageEditorInput getSelectedArea( Rectangle selection ) {
		Rectangle constrained = new Rectangle( 0, 0, getWidth(), getHeight() ).intersection(selection);
		int iMax = constrained.x + constrained.width;
		int jMax = constrained.y + constrained.height;
		float[] newData = new float[ constrained.width * constrained.height ];
		int d = 0;
		int s = constrained.y * width + constrained.x;
		for( int j = constrained.y; j < jMax; j++ ) {
			int sj = s;
			for( int i = constrained.x; i < iMax; i++ ) {
				newData[ d++ ] = data[ s++ ];
			}
			s = sj + width;
		}
		return new MemoryImageEditorInput( name, constrained.width, constrained.height, newData );
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof MemoryImageEditorInput)
			return name.equals(((MemoryImageEditorInput)o).name);
		return false;		
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
	@Override
	public Object getAdapter(Class adapter) {
		return null;
	}

	/**
	 * Returns whether the editor input exists.
	 * <p>
	 * This method is primarily used to determine if an editor input should
	 * appear in the "File Most Recently Used" menu. An editor input will appear
	 * in the list until the return value of <code>exists</code> becomes
	 * <code>false</code> or it drops off the bottom of the list.
	 * 
	 * @return <code>true</code> if the editor input exists;
	 *         <code>false</code> otherwise
	 */
	@Override
	public boolean exists() {
		return true;
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
		return null;
	}

	/**
	 * Returns the name of this editor input for display purposes.
	 * <p>
	 * For instance, when the input is from a file, the return value would
	 * ordinarily be just the file name.
	 * 
	 * @return the name string; never <code>null</code>;
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
	
}