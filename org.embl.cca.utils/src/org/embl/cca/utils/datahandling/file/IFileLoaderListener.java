package org.embl.cca.utils.datahandling.file;

import org.eclipse.core.runtime.IProgressMonitor;

public interface IFileLoaderListener {
	public void fileLoadingDone(final Object source, final boolean newFile, final IProgressMonitor monitor);
	public void fileLoadingCancelled(final Object source, final boolean newFile, final IProgressMonitor monitor);
	public void fileLoadingFailed(final Object source, final boolean newFile, final IProgressMonitor monitor);
}
