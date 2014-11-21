package org.embl.cca.dviewer.ui.views;

import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;
import org.embl.cca.dviewer.ui.editors.IDViewerControllable;
import org.embl.cca.utils.extension.PartAdapter;
import org.embl.cca.utils.threading.CommonThreading;

public class DViewerImageView extends PageBookView {

	public static final String ID = "org.embl.cca.dviewer.ui.views.DViewerImageView";

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
		return DViewerImagePage.DViewerImagePageAsString;
	}

	@Override
	protected IPage createDefaultPage(final PageBook book) {
		book.addControlListener(detachedViewBugfixControlListener);
		final MessagePage messagePage = new MessagePage();
		initPage(messagePage);
		messagePage.createControl(book);
		return messagePage;
	}

	@Override
	protected boolean isImportant(final IWorkbenchPart part) {
//		final Object isAdaptable = part.getAdapter(DViewerImagePageAdaptable.class);
//		return isAdaptable != null;
		final boolean result = part instanceof IDViewerControllable;
		return result;
	}

	IPartListener2 partCreatedAndActivatedListener;
	@Override
	protected PageRec doCreatePage(final IWorkbenchPart part) {
		final DViewerImagePage page = (DViewerImagePage)part.getAdapter(DViewerImagePage.class);
		if( page == null )
			return null;
		page.setViewSite(getViewSite()); //Did not find other way of passing ViewSite
		initPage(page);
		page.createControl(getPageBook());
		partCreatedAndActivatedListener = new PartAdapter() {
			@Override
			public void partActivated(final IWorkbenchPartReference partRef) {
				if( DViewerImageView.this.equals(partRef.getPart(false))) {
					getSite().getPage().removePartListener(partCreatedAndActivatedListener);
					CommonThreading.execUIAsynced(new Runnable() {
						@Override
						public void run() {
							page.viewer.requestDViewerControls();
						}
					});
				}
			}
		};
		getSite().getPage().addPartListener(partCreatedAndActivatedListener);
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
		if (adapter == DViewerImageView.class) {
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
