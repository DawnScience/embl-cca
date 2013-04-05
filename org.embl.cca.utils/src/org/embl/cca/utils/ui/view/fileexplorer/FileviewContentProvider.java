package org.embl.cca.utils.ui.view.fileexplorer;

import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.viewers.IContentProvider;

public class FileviewContentProvider implements IFileviewContentProvider {
	protected IContentProvider contentProvider = null;
	protected IFileColumnsLabelProvider fileColumnsLabelProvider = null;
	protected IContentProposalProvider contentProposalProvider = null;

	public FileviewContentProvider() {
		//throw new RuntimeException("Test");
	}

	@Override
	public IContentProvider getContentProvider() {
		if( contentProvider == null )
			contentProvider = new FileContentProvider();
		return contentProvider;
	}

	@Override
	public IFileContentProvider getFileContentProvider() {
		return (IFileContentProvider)getContentProvider();
	}

	@Override
	public IFileColumnsLabelProvider getFileColumnsLabelProvider() {
		if( fileColumnsLabelProvider == null )
			fileColumnsLabelProvider = new FileColumnsLabelProvider();
		return fileColumnsLabelProvider;
	}

	@Override
	public IContentProposalProvider getContentProposalProvider() {
		if( contentProposalProvider == null )
			contentProposalProvider = new FileContentProposalProvider();
		return contentProposalProvider;
	}
	@Override
	public IFileContentProposalProvider getFileContentProposalProvider() {
		return (IFileContentProposalProvider)getContentProposalProvider();
	}
}
