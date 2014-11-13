package org.embl.cca.dviewer.ui.editors;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewPart;

public interface IViewPartHost extends IViewPart {
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
}
