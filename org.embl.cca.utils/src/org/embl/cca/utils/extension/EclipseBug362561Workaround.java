package org.embl.cca.utils.extension;

import java.util.EnumSet;
import java.util.Timer;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PerspectiveAdapter;
import org.embl.cca.utils.threading.CommonThreading;

/**
 * A workaround for an eclipse bug, number 362561. To make use of it, this
 * class must be instantiated as soon as possible, but not urgent before the
 * active page is created. Creating it more times does no harm, but unnecessary.
 * <p>
 * {@link https://bugs.eclipse.org/bugs/show_bug.cgi?id=362561}
 * </p>
 * @author Gábor Náray
 *
 */
public class EclipseBug362561Workaround {
	protected final static EnumSet<Bug362561WorkaroundState> Bug362561WorkaroundInactiveSet = EnumSet.of(Bug362561WorkaroundState.STARTUP, Bug362561WorkaroundState.INACTIVE);
	protected static enum Bug362561WorkaroundState {
		STARTUP, INACTIVE, PERSPECTIVE_ACTIVATION_WATCH, PERSPECTIVE_CHANGE_WATCH, PART_ACTIVIATION_WATCH;
		public static boolean isInactive(Bug362561WorkaroundState bug362561WorkaroundState) {
			return Bug362561WorkaroundInactiveSet.contains(bug362561WorkaroundState);
		}
	}
	protected static Bug362561WorkaroundState bug362561WorkaroundState = Bug362561WorkaroundState.STARTUP;
	protected static IWorkbenchPage bug362561WatchedPage = null;
	protected static IEditorPart bugfix362561ActiveEditor = null;
	protected static final int Bug362561TimerPeriod = 500;
	protected static Timer timer = new Timer("Bug362561 Workaround Startup", true);

	protected final static IPartListener2 bug362561PartListener = new PartAdapter() {
		@Override
		public void partActivated(IWorkbenchPartReference partRef) {
			if(bug362561WorkaroundState.equals(Bug362561WorkaroundState.PART_ACTIVIATION_WATCH)) {
				bug362561WorkaroundState = Bug362561WorkaroundState.PERSPECTIVE_ACTIVATION_WATCH;
				CommonThreading.execUIAsynced(new Runnable() {
					@Override
					public void run() {
						final IWorkbenchPage activePage = CommonExtension.getActivePage();
						activePage.activate(bugfix362561ActiveEditor);
					}
				});
				return;
			}
		}
	};
	protected final static IPerspectiveListener bug362561PerspectiveListener = new PerspectiveAdapter() {
		@Override
		public void perspectiveActivated(final IWorkbenchPage page,
				final IPerspectiveDescriptor perspective) {
			if( bug362561WorkaroundState.equals(Bug362561WorkaroundState.PERSPECTIVE_ACTIVATION_WATCH) )
				bug362561WorkaroundState = Bug362561WorkaroundState.PERSPECTIVE_CHANGE_WATCH;
		}
		@Override
		public void perspectiveChanged(final IWorkbenchPage page,
				final IPerspectiveDescriptor perspective, final String changeId) {
			if( bug362561WorkaroundState.equals(Bug362561WorkaroundState.PERSPECTIVE_CHANGE_WATCH)) {
				final IWorkbenchPage activePage = CommonExtension.getActivePage();
				bugfix362561ActiveEditor = activePage.getActiveEditor();
				for( final IViewReference viewReference : activePage.getViewReferences() ) {
					final IViewPart view = viewReference.getView(false);
					if( view != null ) {
						bug362561WorkaroundState = Bug362561WorkaroundState.PART_ACTIVIATION_WATCH;
						CommonThreading.execUIAsynced(new Runnable() {
							@Override
							public void run() {
								activePage.activate(view);
							}
						});
						return;
					}
				}
				bug362561WorkaroundState = Bug362561WorkaroundState.PERSPECTIVE_ACTIVATION_WATCH;
			}
		}
	};
	protected final static PageAdapter bug362561PageListener = new PageAdapter() {
		@Override
		public void pageOpened(final IWorkbenchPage page) {
			if(Bug362561WorkaroundState.isInactive(bug362561WorkaroundState))
				activateWorkaroundForBug362561(page);
		}
		@Override
		public void pageClosed(final IWorkbenchPage page) {
			if( !Bug362561WorkaroundState.isInactive(bug362561WorkaroundState) && page.equals(bug362561WatchedPage) )
				deactivateWorkaroundForBug362561();
		}
	};

	protected final static IFirstPageCreatedListener startupWorkaroundForBug362561 = new IFirstPageCreatedListener() {
		@Override
		public void firstPageCreated(final IWorkbenchPage page) {
			synchronized (bug362561WorkaroundState) {
				if(!bug362561WorkaroundState.equals(Bug362561WorkaroundState.STARTUP))
					return;
				page.getWorkbenchWindow().addPerspectiveListener(bug362561PerspectiveListener);
				page.getWorkbenchWindow().addPageListener(bug362561PageListener);
				bug362561WorkaroundState = Bug362561WorkaroundState.INACTIVE;
				bug362561PageListener.pageOpened(page); //Mimic page opened event, since it happened earlier
			}
		}
	};

	public EclipseBug362561Workaround() {
		new FirstPageCreatedPollingNotifier(startupWorkaroundForBug362561, Bug362561TimerPeriod);

	}

	protected static void deactivateWorkaroundForBug362561() {
		bug362561WatchedPage.removePartListener(bug362561PartListener);
		bug362561WatchedPage = null;
		bug362561WorkaroundState = Bug362561WorkaroundState.INACTIVE;
	}

	protected static void activateWorkaroundForBug362561(final IWorkbenchPage page) {
		bug362561WatchedPage = page;
		bug362561WatchedPage.addPartListener(bug362561PartListener);
		bug362561WorkaroundState = Bug362561WorkaroundState.PERSPECTIVE_ACTIVATION_WATCH;
	}

}
