package org.embl.cca.utils.threading;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;

/**
 * The <code>CommonThreading</code> class is a common class for threading.
 * 
 * @author  Gábor Náray
 * @version 1.00 31/7/2012
 * @since   20120731
 */
public class CommonThreading {
	public enum ExecMode {
		SYNC,
		ASYNC
	}

	public static void execFromUIThreadNowOr(Runnable run, ExecMode execMode ) {
		if (Display.getDefault().getThread() == Thread.currentThread()) {
			run.run();
		} else {
			if( execMode == ExecMode.SYNC )
				PlatformUI.getWorkbench().getDisplay().syncExec(run);
//				Display.getDefault().syncExec(run);
			else
				PlatformUI.getWorkbench().getDisplay().asyncExec(run);
//				Display.getDefault().asyncExec(run);
		}
	}

	public static void execFromUIThreadNowOrSynced(Runnable run) {
		execFromUIThreadNowOr(run, ExecMode.SYNC);
	}

	public static void execFromUIThreadNowOrAsynced(Runnable run) {
		execFromUIThreadNowOr(run, ExecMode.ASYNC);
	}

	public static void execSynced(Runnable run ) {
		PlatformUI.getWorkbench().getDisplay().syncExec(run);
	}

	public static void execAsynced(Runnable run ) {
		PlatformUI.getWorkbench().getDisplay().asyncExec(run);
	}
}
