package org.embl.cca.utils.extension;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IShowEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class CommonExtension {
	/**
	 * Creates and returns a new instance of the executable extension 
	 * identified by the class attribute of the first applicable configuration
	 * element from all configuration elements of all extensions configured
	 * into the identified extension point.
	 * <p>
	 * The specified class is instantiated using its 0-argument public constructor.
	 * <p>
	 * @see IConfigurationElement.createExecutableExtension
	 * 
	 * @param extensionPointId
	 * The string specifying the identified extension point.
	 * @param c
	 * The class expected as the result of creation.
	 * @return The created object, which is c class based, or null if the conditions could not be satisfied.
	 * @throws CoreException
	 */
	protected static Object getInstanceOf(String extensionPointId, Class<?> c) throws CoreException {
		final IConfigurationElement[] elems = Platform.getExtensionRegistry().getConfigurationElementsFor(extensionPointId);
		for (IConfigurationElement elem : elems) {
			final Object factory = elem.createExecutableExtension("class");
			if( c.isInstance(factory) )
				return factory;
		}
		return null;
	}

	/**
	 * Opens an editor on the given input.
	 * <p>
	 * If this page already has an editor open on the target input and that
	 * editor is IShowEditorInput then that editor is notified and brought to
	 * top if requested; otherwise, a new editor is opened, and brought to top.
	 * In both case, only if editor is brought to top, then it can be activated.
	 * Two editor inputs are considered the same if they equal. See
	 * <code>Object.equals(Object)<code>
	 * and <code>IEditorInput</code>. If <code>activate == true</code> the
	 * editor will be activated.
	 * </p>
	 * <p>
	 * The editor type is determined by mapping <code>editorId</code> to an
	 * editor extension registered with the workbench. An editor id is passed
	 * rather than an editor object to prevent the accidental creation of more
	 * than one editor for the same input. It also guarantees a consistent
	 * lifecycle for editors, regardless of whether they are created by the user
	 * or restored from saved data.
	 * </p>
	 * 
	 * @param workbenchPage
	 *            the workbench page where to open editor
	 * @param input
	 *            the editor input
	 * @param editorId
	 *            the id of the editor extension to use
	 * @param bringToTop
	 *            if <code>true</code> the editor will be brought to top
	 * @param activate
	 *            if <code>true</code> the editor will be activated
	 * @return an open editor, or <code>null</code> if an external editor was
	 *         opened
	 * @exception PartInitException
	 *                if the editor could not be created or initialized
	 */
	public static IEditorPart openEditor(final IWorkbenchPage workbenchPage,
			final IEditorInput input, final String editorId,
			final boolean bringToTop, final boolean activate) throws PartInitException {
//		return openEditor(input, editorID, activate, MATCH_INPUT); //Original from WorkbenchPage.class
		IEditorPart iEP = null;
		if( bringToTop ) {
			iEP = workbenchPage.openEditor(input, editorId, activate );
		} else {
			iEP = workbenchPage.findEditor(input);
			if( iEP != null ) {
				if (iEP instanceof IShowEditorInput) {
					((IShowEditorInput) iEP).showEditorInput(input);
				}
				//iWP.showEditor(activate, iEP); //Original from WorkbenchPage.class, but can not use it, because protected, thus not in interface
			} else {
				iEP = workbenchPage.openEditor(input, editorId, activate );
			}
		}
		return iEP;
	}

	/**
	 * Opens an editor on the given input.
	 * <p>
	 * If current page already has an editor open on the target input and that
	 * editor is IShowEditorInput then that editor is notified and brought to
	 * top if requested; otherwise, a new editor is opened, and brought to top.
	 * In both case, only if editor is brought to top, then it can be activated.
	 * Two editor inputs are considered the same if they equal. See
	 * <code>Object.equals(Object)<code>
	 * and <code>IEditorInput</code>. If <code>activate == true</code> the
	 * editor will be activated.
	 * </p>
	 * <p>
	 * The editor type is determined by mapping <code>editorId</code> to an
	 * editor extension registered with the workbench. An editor id is passed
	 * rather than an editor object to prevent the accidental creation of more
	 * than one editor for the same input. It also guarantees a consistent
	 * lifecycle for editors, regardless of whether they are created by the user
	 * or restored from saved data.
	 * </p>
	 * 
	 * @param input
	 *            the editor input
	 * @param editorId
	 *            the id of the editor extension to use
	 * @param bringToTop
	 *            if <code>true</code> the editor will be brought to top
	 * @param activate
	 *            if <code>true</code> the editor will be activated
	 * @return an open editor, or <code>null</code> if an external editor was
	 *         opened
	 * @exception PartInitException
	 *                if the editor could not be created or initialized
	 */
	public static IEditorPart openEditor(final IEditorInput input, final String editorId,
			final boolean bringToTop, final boolean activate) throws PartInitException {
		return openEditor(getCurrentPage(), input, editorId, bringToTop, activate);
	}

	/**
	 * Gets the current page, even during startup. Current means either active,
	 * or default when active is null.
	 * @return the current page, or null if there is no current page
	 * @see #getActivePage
	 * @see #getDefaultPage
	 */
	public static IWorkbenchPage getCurrentPage() {
		final IWorkbenchPage activePage = getActivePage();
		if (activePage!=null) return activePage;
		return getDefaultPage();
	}
	
	/**
	 * Gets the active page.
	 * @return the active page, or null if there is no active page
	 */
	public static IWorkbenchPage getActivePage() {
		final IWorkbench bench = PlatformUI.getWorkbench();
		if (bench==null) return null;
		final IWorkbenchWindow window = bench.getActiveWorkbenchWindow();
		if (window==null) return null;
		return window.getActivePage();
	}
	
	
	/**
	 * Gets the default page. Default means the active page of first
	 * workbench window.
	 * @return the active page of first workbench window, or null if there is
	 * no workbench window or active page
	 */
	public static IWorkbenchPage getDefaultPage() {
		final IWorkbenchWindow[] windows = getWorkbenchWindows();
		if (windows==null) return null;
		return windows[0].getActivePage();
	}

	/**
	 * Gets the workbench windows.
	 * @return the workbench windows, or null if there is no workbench window
	 */
	public static IWorkbenchWindow[] getWorkbenchWindows() {
		final IWorkbench bench = PlatformUI.getWorkbench();
		if (bench==null) return null;
		return bench.getWorkbenchWindows();
	}

	/**
	 * Gets the workbench windows as list.
	 * @return the workbench windows as list, which might be empty
	 */
	public static List<IWorkbenchWindow> getWorkbenchWindowList() {
		return Arrays.asList(getWorkbenchWindows());
	}

	/**
	 * Gets the current editor, which is the active editor of current page.
	 * @return the current editor, or null if there is no current editor
	 * @see #getCurrentPage
	 */
	public static IEditorPart getCurrentEditor() {
		final IWorkbenchPage page = getCurrentPage();
		return page.getActiveEditor();
	}

	/**
	 * Simplified calling layout of nth ancestor of control.
	 * The ancestor should not be <code>null</code>, but if it is <code>null</code>, nothing happens.
	 * @param control the control which requests the layout in an ancestor
	 * @param ancestor the ancestor of control to layout 
	 */
	public static void layoutIn(final Control control, final Composite ancestor) {
	if( ancestor != null )
		ancestor.layout(new Control[] {control});
	}

	//http://stackoverflow.com/questions/586414/why-does-an-swt-composite-sometimes-require-a-call-to-resize-to-layout-correct
	//The webpage describes this more complicated solution, which is overkill in my opinion
	public static void revalidateLayout(final Control control) {
		Control c = control;
		do {
			if (c instanceof ExpandBar) {
				final ExpandBar expandBar = (ExpandBar) c;
				for (final ExpandItem expandItem : expandBar.getItems()) {
					expandItem
						.setHeight(expandItem.getControl().computeSize(expandBar.getSize().x, SWT.DEFAULT, true).y);
				}
			}
			c = c.getParent();

		} while (c != null && c.getParent() != null && !(c instanceof ScrolledComposite));

		if (c instanceof ScrolledComposite) {
			final ScrolledComposite scrolledComposite = (ScrolledComposite) c;
			if (scrolledComposite.getExpandHorizontal() || scrolledComposite.getExpandVertical()) {
				scrolledComposite
					.setMinSize(scrolledComposite.getContent().computeSize(SWT.DEFAULT, SWT.DEFAULT, true));
			} else {
				scrolledComposite.getContent().pack(true);
			}
		}
		if (c instanceof Composite) {
			final Composite composite = (Composite) c;
//			composite.layout(true, true);
			//Alternative?:
			composite.changed(new Control[] {control});
			composite.layout(new Control[] {control});
		}
	}

}
