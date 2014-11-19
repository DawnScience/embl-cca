package org.embl.cca.utils.extension;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.runtime.Assert;
import org.eclipse.ui.IWorkbenchPage;
import org.embl.cca.utils.threading.CommonThreading;

public class FirstPageCreatedPollingNotifier {

	protected IFirstPageCreatedListener firstPageCreatedListener;

	protected final static int defaultPollingTimerPeriod = 500;
	protected final int pollingTimerPeriod;
	protected final Timer timer = new Timer(this.getClass().getSimpleName(), true);

	public FirstPageCreatedPollingNotifier(final IFirstPageCreatedListener runnable) {
		this(runnable, defaultPollingTimerPeriod);
	}

	public FirstPageCreatedPollingNotifier(final IFirstPageCreatedListener runnable, final int pollingTimerPeriod) {
		Assert.isNotNull(runnable, "The runnable can not be null");
		Assert.isTrue(pollingTimerPeriod > 0, "The pollingTimerPeriod must be > 0");
		this.firstPageCreatedListener = runnable;
		this.pollingTimerPeriod = pollingTimerPeriod;
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				CommonThreading.execUISynced(new Runnable() {
					@Override
					public void run() {
						final IWorkbenchPage workbenchPage = CommonExtension.getActivePage();
						if( workbenchPage != null ) {
							timer.cancel();
							notifyRunnable(workbenchPage);
						}
					}
				});
			}
		}, pollingTimerPeriod, pollingTimerPeriod);
	}

	protected void notifyRunnable(final IWorkbenchPage page) {
		firstPageCreatedListener.firstPageCreated(page);
	}
}
