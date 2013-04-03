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
//		System.out.println("CommonThreading.execFromUIThreadNowOr called (" + run.toString() + ")");
		if (Display.getDefault().getThread() == Thread.currentThread()) {
//			System.out.println("CommonThreading.execFromUIThreadNowOr: current thread! (" + run.toString() + ")");
			run.run();
		} else {
//			System.out.println("CommonThreading.execFromUIThreadNowOr: NOT current thread! (" + run.toString() + ")");
			if( execMode == ExecMode.SYNC ) {
//				System.out.println("CommonThreading.execFromUIThreadNowOr: SYNC call (" + run.toString() + ")");
				PlatformUI.getWorkbench().getDisplay().syncExec(run);
//				Display.getDefault().syncExec(run);
			} else {
//				System.out.println("CommonThreading.execFromUIThreadNowOr: ASYNC call (" + run.toString() + ")");
				PlatformUI.getWorkbench().getDisplay().asyncExec(run);
//				Display.getDefault().asyncExec(run);
			}
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
