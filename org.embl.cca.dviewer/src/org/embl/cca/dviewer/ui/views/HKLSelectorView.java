package org.embl.cca.dviewer.ui.views;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;
import org.embl.cca.dviewer.ui.editors.IDViewerControllable;

public class HKLSelectorView extends PageBookView {

	public static final String ID = "org.embl.cca.utils.ui.views.HKLSelectorView";

	@Override
	protected IPage createDefaultPage(final PageBook book) {
		final MessagePage messagePage = new MessagePage();
		initPage(messagePage);
		messagePage.createControl(book);
		return messagePage;
	}

	@Override
	protected boolean isImportant(final IWorkbenchPart part) {
		return part instanceof IDViewerControllable;
	}

	@Override
	protected PageRec doCreatePage(final IWorkbenchPart part) {
		if( !isImportant(part) )
			return null;
		final HKLSelectorPage page = (HKLSelectorPage)part.getAdapter(HKLSelectorPage.class);
		if( page == null )
			return null;
		initPage(page);
		page.createControl(getPageBook());
		return new PageRec(part, page);
	}

	@Override
	protected void doDestroyPage(final IWorkbenchPart part, final PageRec pageRecord) {
		pageRecord.page.dispose();
	}

	@Override
	protected IWorkbenchPart getBootstrapPart() {
		final IWorkbenchPage page = getSite().getPage();
		if(page != null) {
			// check whether the active part is important to us
			final IWorkbenchPart activePart = page.getActivePart();
			return isImportant(activePart) ? activePart : null;
		}
		return null;
	}

	public void partActivated(final IWorkbenchPart part) {
		super.partActivated(part);

		final IPage page = getCurrentPage();
		String title = page instanceof IAdaptable ? (String)((IAdaptable)page).getAdapter(String.class) : null;
		if( title == null )
			title = HKLSelectorPage.HKLSelectorPageAsString;
		setPartName(title);
	}
}
