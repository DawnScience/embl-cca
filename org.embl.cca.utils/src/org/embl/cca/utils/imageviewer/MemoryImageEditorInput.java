package org.embl.cca.utils.imageviewer;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class MemoryImageEditorInput implements IEditorInput {

	ImageData img;
	String name;
	int [] intData;
	float [] floatData;
	
	public void setIntArray(int [] data) {
		this.intData=data;
	}
	public int [] getIntDataset() {
		return intData;
	}
	
	public void setFloatArray(float [] data) {
		this.floatData=data;
	}
	public float [] getFloatArray() {
		return floatData;
	}
	
	@Override
	public boolean equals (Object o) {
		if (o instanceof MemoryImageEditorInput)
			return name.equals(((MemoryImageEditorInput)o).name);
		return false;		
	}
	
	public MemoryImageEditorInput(String name,ImageData img) {
		this.name=name;
		this.img=img;
	}
	
	@Override
	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean exists() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		// TODO Auto-generated method stub
		return ImageDescriptor.createFromImageData(img);
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return name;
	}

	@Override
	public IPersistableElement getPersistable() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getToolTipText() {
		// TODO Auto-generated method stub
		return "";
	}
	
}