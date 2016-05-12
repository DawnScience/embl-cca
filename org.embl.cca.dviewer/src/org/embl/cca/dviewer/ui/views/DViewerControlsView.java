package org.embl.cca.dviewer.ui.views;

import org.apache.commons.collections4.Predicate;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IPage;
import org.eclipse.ui.part.MessagePage;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.PageBookView;
import org.embl.cca.dviewer.DViewerStartup;
import org.embl.cca.dviewer.ui.editors.IDViewerControlsPageAdaptable;
import org.embl.cca.utils.Activator;
import org.embl.cca.utils.threading.CommonThreading;

public class DViewerControlsView extends PageBookView {

	public static final String ID = "org.embl.cca.dviewer.ui.views.DViewerControlsView";
	/**
	 * The memento that was used to persist the state of this view.
	 * May be <code>null</code>.
	 */
	private IMemento fMemento;
	IWorkbenchPart dViewerImageViewLastKnownContributingPart = null;
	protected static final String DViewerControlsSettings = "dViewerControlsSettings";
	protected static final String DViewerControlsKey1 = "dViewerControlsKey1";


	public DViewerControlsView() {
		super();
	}

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
//		messagePage.getControl().setBackground(org.eclipse.swt.widgets.Display.getCurrent().getSystemColor(org.eclipse.swt.SWT.COLOR_YELLOW));
//		book.setBackground(org.eclipse.swt.widgets.Display.getCurrent().getSystemColor(org.eclipse.swt.SWT.COLOR_RED));
		return messagePage;
	}

	@Override
	protected boolean isImportant(final IWorkbenchPart part) {
		return part.getAdapter(IDViewerControlsPageAdaptable.class) != null;
	}

	@Override
	protected PageRec doCreatePage(final IWorkbenchPart part) {
		final DViewerControlsPage page = (DViewerControlsPage)part.getAdapter(DViewerControlsPage.class);
		if( page == null )
			return null;
		initPage(page);
		final IDialogSettings settings = Activator.getDefault().getDialogSettings();
		IDialogSettings section = settings.getSection(DViewerControlsSettings);
		if (section != null) {
		    //TODO Load stuff by section.get(key) pairs
//	        section.get(DViewerControlsKey1);
	    }
		
		page.createControl(getPageBook());
//		page.getControl().setBackground(org.eclipse.swt.widgets.Display.getCurrent().getSystemColor(org.eclipse.swt.SWT.COLOR_GREEN));
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
		//If the activated part is DViewerImageView, then the current page
		//might be outdated as created for a part different than the part of
		//DViewerImageView's current page. In this case the current page
		//must be updated, after activation is completed (DViewerImageView
		//mainly), thus updating is done UISynced.
		if( part instanceof DViewerImageView) {
			CommonThreading.execUISynced(new Runnable() {
				@Override
				public void run() {
					final DViewerImageView dViewerImageView = (DViewerImageView)part;
					final IWorkbenchPart dVIVCPart = dViewerImageView.getCurrentContributingPart();
					if( !Util.equals(dVIVCPart, dViewerImageViewLastKnownContributingPart) ) {
						dViewerImageViewLastKnownContributingPart = dVIVCPart;
						DViewerControlsView.this.partClosed(dViewerImageView);
						DViewerControlsView.this.partActivated(dViewerImageView);
					}
				}
			});
		}
		super.partActivated(part);
		String title = (String)getAdapter(String.class);
		if( title == null )
			title = getViewName();
		setPartName(title);
	}

	@Override
	public void partClosed(final IWorkbenchPart part) {
		final IDialogSettings settings = Activator.getDefault().getDialogSettings();
		IDialogSettings section = settings.getSection(DViewerControlsSettings);
	    if (section == null) { // if it doesn't exist, create it
	        section = settings.addNewSection(DViewerControlsSettings);
	    }
	    //TODO Save stuff by section.put(key, value) pairs
//	    section.put(DViewerControlsKey1, "test");
	    super.partClosed(part);
	}

	@Override
	protected <T> T getViewAdapter(final Class<T> adapter) {
		return super.getViewAdapter(adapter);
	}

	@Override
	public <T> T getAdapter(final Class<T> clazz) {
		if (clazz == DViewerControlsView.class) {
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
