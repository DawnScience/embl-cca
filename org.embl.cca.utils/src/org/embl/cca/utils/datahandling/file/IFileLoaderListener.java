package org.embl.cca.utils.datahandling.file;

import org.eclipse.core.runtime.IProgressMonitor;

public interface IFileLoaderListener {
	public void fileIsReady(Object source, IProgressMonitor monitor);
}
