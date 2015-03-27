package org.embl.cca.utils.extension;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IShowEditorInput;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
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
	protected static Object getInstanceOf(final String extensionPointId, Class<?> c) throws CoreException {
		final IConfigurationElement[] elems = Platform.getExtensionRegistry().getConfigurationElementsFor(extensionPointId);
		for (final IConfigurationElement elem : elems) {
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

	public static IViewPart findView(final String viewId, final boolean restore) {
		final IWorkbenchPage page = getActivePage();
		final IViewReference ref = page.findViewReference(viewId);
		if( ref == null )
			return null;
		return ref.getView(restore);
	}

	public static IViewReference findViewRef(final String viewId, final boolean restore) {
		final IWorkbenchPage page = getActivePage();
		return page.findViewReference(viewId);
	}

	/**
	 * Shows the view identified by the given view id in this page and gives it
	 * focus. If there is a view identified by the given view id (and with no
	 * secondary id) already open in this page, it is given focus.
	 * If detach is true, the shown view is detached if not detached already.
	 * 
	 * @param viewId
	 *            the id of the view extension to use
	 * @param detach true if the view has to be detached
	 * @return the shown view
	 * @exception PartInitException
	 *                if the view could not be initialized
	 */
	@SuppressWarnings("restriction")
	public static IViewPart openView(final String viewId, final boolean detach) throws PartInitException {
		final IWorkbenchPage page = getActivePage();
		final IViewPart viewPart = page.showView(viewId);
		final boolean detached = viewPart.getSite().getShell().getText().isEmpty(); //bit of hack
		if( detach && !detached ) {
			if( page instanceof org.eclipse.ui.internal.WorkbenchPage ) {
				detachPart(viewPart);
//				final IViewReference ref = page.findViewReference(viewId);
//				((org.eclipse.ui.internal.WorkbenchPage)page).detachView(ref);
			}
		}
		return viewPart;
	}
	
	private static void detachPart(IViewPart viewPart) {
		EModelService s = (EModelService) viewPart.getSite().getService(EModelService.class);
		MPartSashContainerElement p = (MPart) viewPart.getSite().getService(MPart.class);
		if (p.getCurSharedRef() != null) {
			p = p.getCurSharedRef();
		}
		s.detach(p, 100, 100, 300, 300);
	}

	/**
	 * Shows the view identified by the given view id in this page and gives it
	 * focus. If there is a view identified by the given view id (and with no
	 * secondary id) already open in this page, it is given focus.
	 * If detach is true, the shown view is detached if not detached already.
	 * 
	 * @param viewId
	 *            the id of the view extension to use
	 * @param detach true if the view has to be detached
	 * @return the shown view, or <code>null</code> if it could not be initialized
	 */
	public static IViewPart openViewWithErrorDialog(final String viewId, final boolean detach) {
		try {
			return openView(viewId, detach);
		} catch (final PartInitException e) {
			final Shell shell = getActiveShell();
			Assert.isNotNull(shell, "Environment error: can not find shell");
			MessageDialog.openError(shell,
					"View Opening Error", new StringBuilder("Could not open the view: \"")
				.append(viewId).append("\".\n\n").append(e.getMessage()).toString());
			return null;
		}
	}

	/**
	 * Sets the state of the view identified by the given view id in this page.
	 * 
	 * @param viewId
	 *            the id of the view extension to use
	 * @param state new state of view (IWorkbenchPage.STATE_MAXIMIZED, IWorkbenchPage.MINIMIZED, IWorkbenchPage.RESTORED)
	 */
	public static void setViewState(final String viewId, final int state) {
		final IWorkbenchPage page = getActivePage();
		final IViewReference ref = page.findViewReference(viewId);
		final int currentState = page.getPartState(ref);
		if(currentState != state) {
			page.setPartState(ref, state);
//			page.toggleZoom(ref);
//			ActionFactory.MAXIMIZE.create(page.getWorkbenchWindow()).run();
		}
	}

	/**
	 * Sets the state of shell of view identified by the given view id in this page.
	 * The shell of view is the real window containing the view as well.
	 * 
	 * @param viewId
	 *            the id of the view extension to use
	 * @param state new state of shell of view (IWorkbenchPage.STATE_MAXIMIZED, IWorkbenchPage.MINIMIZED)
	 */
	public static void setViewShellState(final String viewId, final int state) {
		final Shell shell = getShell(viewId);
		switch (state) {
		case IWorkbenchPage.STATE_MAXIMIZED:
			shell.setMaximized(true);
			break;
		case IWorkbenchPage.STATE_MINIMIZED:
			shell.setMinimized(true);
			break;
		}
	}

	/**
	 * Gets the shell of view identified by the given view id in this page.
	 * The shell of view is the real window containing the view as well.
	 * 
	 * @param viewId
	 *            the id of the view extension to use
	 * @return the shell of view, or null if the view was not instantiated
	 * @see #getActivePage
	 */
	public static Shell getShell(final String viewId) {
		final IWorkbenchPage page = getActivePage();
		final IViewReference ref = page.findViewReference(viewId);
		return ref.getView(false).getSite().getShell();
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
	 * Gets the current shell, even during startup. Current means either active,
	 * or default when active is null.
	 * @return the current shell, or null if there is no current shell
	 * @see #getActiveShell
	 * @see #getDefaultShell
	 */
	public static Shell getCurrentShell() {
		final Shell activeShell = getActiveShell();
		if (activeShell !=null) return activeShell;
		return getDefaultShell();
	}

	/**
	 * Gets the active shell.
	 * @return the active shell, or null if there is no active shell
	 */
	public static Shell getActiveShell() {
		final IWorkbench bench = PlatformUI.getWorkbench();
		if (bench==null) return null;
		final IWorkbenchWindow window = bench.getActiveWorkbenchWindow();
		if (window==null) return null;
		return window.getShell();
	}

	/**
	 * Gets the default shell. Default means the active shell of first
	 * workbench window.
	 * @return the active shell of first workbench window, or null if there is
	 * no workbench window or active shell
	 */
	public static Shell getDefaultShell() {
		final IWorkbenchWindow[] windows = getWorkbenchWindows();
		if (windows==null) return null;
		return windows[0].getShell();
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
