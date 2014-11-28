package org.embl.cca.utils.ui.view.filenavigator;

import org.embl.cca.utils.datahandling.EFile;

public interface IOpenFileListener {
	/**
	 * Open file event handler to customize opening file in FileView.
	 * @param file the file to open
	 * @return true if open file is done (not depending on success), else false
	 */
	public boolean openFile(final EFile file);
}
