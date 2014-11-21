package org.embl.cca.dviewer.ui.editors;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

public interface IEditorPartHost extends IEditorPart {
	//These methods for parent
	public Composite getContainer();
	public void setToolbarParent(final Composite toolbarParent);

	//These methods for client
	public String getPartName();
	public void setPartName(final String partName);
	public String getTitle();
	public void setTitle(final String title);
	public String getContentDescription();
	public void setContentDescription(final String description);
	public IEditorInput getEditorInput();
	public void setInputWithNotify(final IEditorInput input);
	public void setInput(final IEditorInput input);
}
