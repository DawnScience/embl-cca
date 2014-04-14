package org.embl.cca.utils.threading;

import org.eclipse.swt.SWTException;
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
	public static enum ExecMode {
		SYNC,
		ASYNC
	}

	/**
	 * Returns the current display if there is one.
	 * 
	 * @return the current display, or <code>null</code> if none
	 */
	public static Display getCurrentDisplay() {
		return PlatformUI.getWorkbench() == null ? null : PlatformUI.getWorkbench().getDisplay();
	}

	/**
	 * Returns true if there is a display having the current thread.
	 * 
	 * @return true if there is a display having the current thread
	 */
	public static boolean isCurrentThreadGUI() {
		return Display.getCurrent() != null;
	}

	/**
	 * Causes the <code>run()</code> method of the runnable to
	 * be invoked by the user-interface thread at the next 
	 * reasonable opportunity. The thread which calls this method
	 * is suspended until the runnable completes.  Specifying <code>null</code>
	 * as the runnable simply wakes the user-interface thread.
	 * <p>
	 * Note that at the time the runnable is invoked, widgets 
	 * that have the receiver as their display may have been
	 * disposed. Therefore, it is necessary to check for this
	 * case inside the runnable before accessing the widget.
	 * </p>
	 * 
	 * @param runnable code to run on the user-interface thread or <code>null</code>
	 *
	 * @exception SWTException <ul>
	 *    <li>ERROR_FAILED_EXEC - if an exception occurred when executing the runnable</li>
	 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
	 * </ul>
	 *
	 * @see Display#syncExec
	 * @see #asyncExec
	 */
	public static void execUISynced(final Runnable runnable) {
		getCurrentDisplay().syncExec(runnable);
	}

	/**
	 * Causes the <code>run()</code> method of the runnable to
	 * be invoked by the user-interface thread at the next 
	 * reasonable opportunity. The caller of this method continues 
	 * to run in parallel, and is not notified when the
	 * runnable has completed.  Specifying <code>null</code> as the
	 * runnable simply wakes the user-interface thread when run.
	 * <p>
	 * Note that at the time the runnable is invoked, widgets 
	 * that have the receiver as their display may have been
	 * disposed. Therefore, it is necessary to check for this
	 * case inside the runnable before accessing the widget.
	 * </p>
	 *
	 * @param runnable code to run on the user-interface thread or <code>null</code>
	 *
	 * @exception SWTException <ul>
	 *    <li>ERROR_DEVICE_DISPOSED - if the receiver has been disposed</li>
	 * </ul>
	 * 
	 * @see Display#asyncExec
	 * @see #syncExec
	 */
	public static void execUIAsynced(final Runnable runnable) {
		getCurrentDisplay().asyncExec(runnable);
	}

	/**
	 * Causes the <code>run()</code> method of the runnable to
	 * be invoked by the user-interface thread at the next 
	 * reasonable opportunity. If execMode is <code>SYNC</code> then the thread which calls
	 * this method is suspended until the runnable completes, or if execMode
	 * is <code>ASYNC</code> then the caller of this method continues 
	 * to run in parallel, and is not notified when the
	 * runnable has completed. Specifying <code>null</code>
	 * as the runnable simply wakes the user-interface thread.
	 * <p>
	 * Note that at the time the runnable is invoked, widgets 
	 * that have the receiver as their display may have been
	 * disposed. Therefore, it is necessary to check for this
	 * case inside the runnable before accessing the widget.
	 * </p>
	 * 
	 * @param runnable code to run on the user-interface thread or <code>null</code>
	 * @param execMode <code>SYNC</code> for synced, <code>aSYNC</code> for asynced execution
	 */
	public static void execUI(final Runnable runnable, final ExecMode execMode ) {
		if( execMode.equals(ExecMode.SYNC) ) {
			execUISynced(runnable);
		} else {
			execUIAsynced(runnable);
		}
	}

}
