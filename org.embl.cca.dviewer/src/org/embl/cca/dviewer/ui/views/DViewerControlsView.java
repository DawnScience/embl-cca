package org.embl.cca.dviewer.ui.views;

import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;
import org.embl.cca.dviewer.ui.editors.IDViewerControlsPageAdaptable;

public class DViewerControlsView extends PageBookView {

	public static final String ID = "org.embl.cca.dviewer.ui.views.DViewerControlsView";

	//Bugfix: detached view displays nothing when part is changed, it seems layout solves the issue
	final ControlListener detachedViewBugfixControlListener = new ControlListener() {
		@Override
		public void controlResized(final ControlEvent e) {
			final Composite c = ((Composite)e.getSource()).getParent(); //parent of book
			if( !c.isDisposed() )
				c.getParent().layout(true, true);
		}
		@Override
		public void controlMoved(ControlEvent e) {
		}
	};

	public final static String getViewName() {
		return DViewerControlsPage.DViewerControlsPageAsString;
	}

	@Override
	protected IPage createDefaultPage(final PageBook book) {
		book.addControlListener(detachedViewBugfixControlListener);
		final MessagePage messagePage = new MessagePage();
		initPage(messagePage);
		messagePage.createControl(book);
		messagePage.getControl().setBackground(org.eclipse.swt.widgets.Display.getCurrent().getSystemColor(org.eclipse.swt.SWT.COLOR_YELLOW));
		book.setBackground(org.eclipse.swt.widgets.Display.getCurrent().getSystemColor(org.eclipse.swt.SWT.COLOR_RED));
		return messagePage;
	}

	@Override
	protected boolean isImportant(final IWorkbenchPart part) {
		final Object isAdaptable = part.getAdapter(IDViewerControlsPageAdaptable.class);
		return isAdaptable != null;
//		return part instanceof IDViewerControllable;
	}

	@Override
	protected PageRec doCreatePage(final IWorkbenchPart part) {
		final DViewerControlsPage page = (DViewerControlsPage)part.getAdapter(DViewerControlsPage.class);
		if( page == null )
			return null;
		initPage(page);
		page.createControl(getPageBook());
		page.getControl().setBackground(org.eclipse.swt.widgets.Display.getCurrent().getSystemColor(org.eclipse.swt.SWT.COLOR_GREEN));
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

	@Override
	public void partActivated(final IWorkbenchPart part) {
		super.partActivated(part);
		String title = (String)getAdapter(String.class);
		if( title == null )
			title = getViewName();
		setPartName(title);
	}

	@Override
	protected Object getViewAdapter(@SuppressWarnings("rawtypes") final Class adapter) {
		return super.getViewAdapter(adapter);
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") final Class adapter) {
		if (adapter == DViewerControlsView.class) {
			return this;
		}
		return super.getAdapter(adapter);
	}

	@Override
	public void dispose() {
		if( !getPageBook().isDisposed() )
			getPageBook().removeControlListener(detachedViewBugfixControlListener);
		super.dispose();
	}
}
