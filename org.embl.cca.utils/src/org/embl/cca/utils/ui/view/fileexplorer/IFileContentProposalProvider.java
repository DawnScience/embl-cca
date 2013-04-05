package org.embl.cca.utils.ui.view.fileexplorer;

import java.io.File;

public interface IFileContentProposalProvider {
	public String fileToString(File file);
	public File stringToFile(String path);
}
