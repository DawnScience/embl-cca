package org.embl.cca.dviewer.ui.views;

import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;
import org.embl.cca.dviewer.DViewerStartup;
import org.embl.cca.dviewer.ui.editors.IDViewerControllable;
import org.embl.cca.utils.extension.PartAdapter;
import org.embl.cca.utils.threading.CommonThreading;
import org.apache.commons.collections4.Predicate;

public class DViewerImageView extends PageBookView {

	public static final String ID = "org.embl.cca.dviewer.ui.views.DViewerImageView";
	/**
	 * The memento that was used to persist the state of this view.
	 * May be <code>null</code>.
	 */
	private IMemento fMemento;

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
	protected boolean isImportant(final IWorkbenchPart workbenchPart) {
		return workbenchPart instanceof IEditorPart && workbenchPart instanceof IDViewerControllable;
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
		partCreatedAndActivatedListener = new PartAdapter() { //One shot listener to request dViewer controls
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
		return DViewerStartup.partActivationWatcher.findLastMatchingActivatedPart(
				new Predicate<IWorkbenchPart>() {
					@Override
					public boolean evaluate(final IWorkbenchPart workbenchPart) {
						return isImportant(workbenchPart);
					}
				});
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
	public IWorkbenchPart getCurrentContributingPart() {
		return super.getCurrentContributingPart();
	}

	@Override
	protected <T> T getViewAdapter(final Class<T> adapter) {
		return super.getViewAdapter(adapter);
	}

	@Override
	public <T> T getAdapter(final Class<T> clazz) {
		if (clazz == DViewerImageView.class) {
			return clazz.cast(this);
		}
		return super.getAdapter(clazz);
	}

	@Override
	public void dispose() {
		if( !getPageBook().isDisposed() )
			getPageBook().removeControlListener(detachedViewBugfixControlListener);
		super.dispose();
	}

	/**
	 * Returns the memento that contains the persisted state of
	 * the view.  May be <code>null</code>.
	 * @return the current {@link IMemento}
	 */
	protected IMemento getMemento() {
		return fMemento;
	}

	/** 
	 * Sets the memento that contains the persisted state of the 
	 * view.
	 * @param memento the new {@link IMemento}
	 */
	protected void setMemento(IMemento memento) {
		fMemento = memento;
	}

    @Override
	public void init(final IViewSite site, final IMemento memento) throws PartInitException {
		super.init(site, memento);
		//see http://blog.eclipse-tips.com/2009/08/remember-state.html
		//store the memento to be used when this view is created.
		setMemento(memento);
    }


    @Override
	public void saveState(IMemento memento) {
    	super.saveState(memento);
    	//Currently there is no state in this class to save
    }
}
