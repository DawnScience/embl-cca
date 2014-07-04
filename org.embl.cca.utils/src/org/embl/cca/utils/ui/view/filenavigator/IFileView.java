package org.embl.cca.utils.ui.view.filenavigator;

import org.dawb.common.util.io.IFileSelector;
import org.embl.cca.utils.datahandling.EFile;

/**
 * This class is the interface for FileView. 
 * @see org.embl.cca.utils.ui.view.filenavigator.FileView
 */
public interface IFileView extends IFileSelector {
	public void collapseAll();
	public void showPreferences();
	public void refresh();
	public void openSelectedFile();
	public EFile getSelectedFile();
}
