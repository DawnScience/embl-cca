package org.embl.cca.utils.ui.view.fileexplorer;

import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.viewers.IContentProvider;

public interface IFileviewContentProvider {
	public IContentProvider getContentProvider();
	public IFileContentProvider getFileContentProvider();
	public IFileColumnsLabelProvider getFileColumnsLabelProvider();
	public IContentProposalProvider getContentProposalProvider();
	public IFileContentProposalProvider getFileContentProposalProvider();
}
