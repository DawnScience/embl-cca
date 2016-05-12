package org.embl.cca.utils.extension;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartSashContainerElement;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Rectangle;
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
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class CommonExtension {
	public final static boolean debugMode = Boolean.getBoolean("DEBUG");

	public static Rectangle DETACH_RECTANGLE_DEFAULT = new Rectangle(100, 100, 300, 300);

	/**
	 * Creates and returns a new instance of the executable extension 
	 * identified by the class attribute of the first applicable configuration
	 * element from all configuration elements of all extensions configured
	 * into the identified extension point.
	 * <p>
	 * The specified class is instantiated using its 0-argument public constructor.
	 * <p>
	 * @see IConfigurationElement#createExecutableExtension
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
		final IViewReference ref = findViewRef(viewId, restore);
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
	 * @return the shown view, or <code>null</code> if it could not be initialized
	 */
	public static IViewPart openViewWithErrorDialog(final String viewId, final boolean detach) {
		return openViewWithErrorDialog(viewId, detach, DETACH_RECTANGLE_DEFAULT);
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
	 * @param rectangle the rectangle of detached window
	 * @return the shown view, or <code>null</code> if it could not be initialized
	 */
	public static IViewPart openViewWithErrorDialog(final String viewId, final boolean detach, final Rectangle rectangle) {
		try {
			return openView(viewId, detach, rectangle);
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
	public static IViewPart openView(final String viewId, final boolean detach) throws PartInitException {
		return openView(viewId, detach, DETACH_RECTANGLE_DEFAULT);
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
	 * @param rectangle the rectangle of detached window
	 * @return the shown view
	 * @exception PartInitException
	 *                if the view could not be initialized
	 */
	public static IViewPart openView(final String viewId, final boolean detach, final Rectangle rectangle) throws PartInitException {
		final IWorkbenchPage page = getActivePage();
		final IViewPart viewPart = page.showView(viewId);
		final boolean detached = isDetached(viewPart);
		if( detach && !detached ) {
			detachViewPart(viewPart, rectangle);
		}
		return viewPart;
	}

	/**
	 * Shows the view identified by the given view id in this page and gives it
	 * focus. If there is a view identified by the given view id (and with no
	 * secondary id) already open in this page, it is closed firstly.
	 * If detach is true, the shown view is detached.
	 * 
	 * @param viewId
	 *            the id of the view extension to use
	 * @param detach true if the view has to be detached
	 * @return the shown view, or <code>null</code> if it could not be initialized
	 */
	public static IViewPart reopenViewWithErrorDialog(final String viewId, final boolean detach) {
		return reopenViewWithErrorDialog(viewId, detach, CommonExtension.getRectangle(viewId));
	}

	/**
	 * Shows the view identified by the given view id in this page and gives it
	 * focus. If there is a view identified by the given view id (and with no
	 * secondary id) already open in this page, it is closed firstly.
	 * If detach is true, the shown view is detached if not detached already.
	 * 
	 * @param viewId
	 *            the id of the view extension to use
	 * @param detach true if the view has to be detached
	 * @param rectangle the rectangle of detached window
	 * @return the shown view, or <code>null</code> if it could not be initialized
	 */
	public static IViewPart reopenViewWithErrorDialog(final String viewId, final boolean detach, final Rectangle rectangle) {
		try {
			return reopenView(viewId, detach, rectangle);
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
	 * Shows the view identified by the given view id in this page and gives it
	 * focus. If there is a view identified by the given view id (and with no
	 * secondary id) already open in this page, it is closed firstly.
	 * If detach is true, the shown view is detached if not detached already.
	 * 
	 * @param viewId
	 *            the id of the view extension to use
	 * @param detach true if the view has to be detached
	 * @return the shown view
	 * @exception PartInitException
	 *                if the view could not be initialized
	 */
	public static IViewPart reopenView(final String viewId, final boolean detach) throws PartInitException {
		return reopenView(viewId, detach, CommonExtension.getRectangle(viewId));
	}

	/**
	 * Shows the view identified by the given view id in this page and gives it
	 * focus. If there is a view identified by the given view id (and with no
	 * secondary id) already open in this page, it is closed firstly.
	 * If detach is true, the shown view is detached if not detached already.
	 * 
	 * @param viewId
	 *            the id of the view extension to use
	 * @param detach true if the view has to be detached
	 * @param rectangle the rectangle of detached window
	 * @return the shown view
	 * @exception PartInitException
	 *                if the view could not be initialized
	 */
	public static IViewPart reopenView(final String viewId, final boolean detach, final Rectangle rectangle) throws PartInitException {
		CommonExtension.closePart(viewId, true);
		final IWorkbenchPage page = getActivePage();
		final IViewPart viewPart = page.showView(viewId);
		final boolean detached = isDetached(viewPart);
		if( detach && !detached ) {
			detachViewPart(viewPart, rectangle);
		}
		return viewPart;
	}

	/**
	 * Finds and closes the part with the given id.
	 * @param partId the id of the part to close, must not be <code>null</code>
	 * @param force if the part should be removed from the model regardless of its
	 *            {@link #REMOVE_ON_HIDE_TAG} tag
	 * @return the part for the specified id, or <code>null</code> if no such part could be found
	 * @see EPartService#hidePart
	 */
	public static void closePart(final String partId, final boolean force) {
		final EPartService partService = getService(EPartService.class);
		partService.hidePart(getPart(partId), force);
	}

	/**
	 * Finds and returns the id for the given workbench part (E3).
	 * @param workbenchPart the workbench part to search for, must not be <code>null</code>
	 * @return the id for the specified workbench part, or <code>NullPointerException</code> if there is no workbench part
	 */
	public static String getId(final IWorkbenchPart workbenchPart) {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getReference(workbenchPart).getId();
	}

	/**
	 * Finds and returns the first window containing the given element.
	 * @param mUIElement the element to search for, must not be <code>null</code>
	 * @return the window for the specified element, or <code>null</code> if the part is not in a window
	 */
	public static MUIElement findContainerWindow(final MUIElement mUIElement) {
		final EModelService modelService = getService(EModelService.class);
		MUIElement mContainerWindow = mUIElement;
		while( mContainerWindow != null && !(mContainerWindow instanceof MWindow) ) {
			mContainerWindow = modelService.getContainer(mContainerWindow);
		}
		return mContainerWindow;
	}

	/**
	 * Finds and returns the first window containing the element with the given id.
	 * @param partId the id of the part to search for, must not be <code>null</code>
	 * @return the window for the part of specified id, or <code>null</code> if the part is not in a window
	 */
	public static MUIElement findContainerWindow(final String partId) {
		return findContainerWindow(getPartholder(partId));
	}

	/**
	 * Returns if the given workbench part is detached (E3) using E4 API.
	 * <p>
	 * This field determines whether or not the given workbench part is detached.
	 * </p>
	 * @param workbenchPart the part to search for, must not be <code>null</code>
	 * @return true if the specified workbench part is detached, else false.
	 * @see #isDetached(String)
	 * @see #detachWorkbenchPart(IWorkbenchPart)
	 */
	@Deprecated
	public static boolean isDetached(final IWorkbenchPart workbenchPart) {
		//http://eclipsesource.com/blogs/2010/06/23/tip-how-to-detect-that-a-view-was-detached/
		//return viewPart.getSite().getShell().getText().isEmpty(); //E3 style
		final String workbenchId = getId(workbenchPart);
		return isDetached(workbenchId);
	}

	/**
	 * Returns if the part with given id is detached.
	 * <p>
	 * This field determines whether or not the part with given id is detached.
	 * </p>
	 * @param partId the id of the part to search for, must not be <code>null</code>
	 * @return true if the workbench part with specified id is detached, else false.
	 * @see #detachWorkbenchPart(IWorkbenchPart)
	 */
	public static boolean isDetached(final String partId) {
		//https://dev.eclipse.org/mhonarc/lists/e4-dev/msg07352.html
		final EModelService modelService = getService(EModelService.class);
		final MUIElement mUIElement = getPartholder(partId);
		final MUIElement mContainingWindow = findContainerWindow(mUIElement);
//		return !(modelService.getContainer(mContainingWindow) instanceof MApplication); //Alternative condition
		return modelService.getTopLevelWindowFor(mUIElement) != mContainingWindow;
	}

	public static boolean isMaxScreened(final String partId) {
		final Shell shell = getShell(partId);
		return shell.getMaximized();
	}

	/**
	 * Sets the max screen state of shell of part identified by the given id.
	 * The shell of part is the real window containing the part as well.
	 * 
	 * @param partId
	 *            the id of the part to use
	 */
	public static void setMaxScreened(final String partId) {
		final Shell shell = getShell(partId);
		shell.setMaximized(true);
	}

	public static boolean isMinScreened(final String partId) {
		final Shell shell = getShell(partId);
		return shell.getMinimized();
	}

	/**
	 * Sets the min screen state of shell of part identified by the given id.
	 * The shell of part is the real window containing the part as well.
	 * 
	 * @param partId
	 *            the id of the part to use
	 */
	public static void setMinScreened(final String partId) {
		final Shell shell = getShell(partId);
		shell.setMinimized(true);
	}

	/**
	 * Returns if the given workbench part is visible (E3) using E4 API.
	 * <p>
	 * This field determines whether or not the given workbench part is visible.
	 * </p>
	 * @param workbenchPart the part to search for, must not be <code>null</code>
	 * @return true if the specified workbench part is visible, else false.
	 * @see #isVisible(String)
	 */
	@Deprecated
	public static boolean isVisible(final IWorkbenchPart workbenchPart) {
		final String workbenchId = getId(workbenchPart);
		return isVisible(workbenchId);
	}

	/**
	 * Returns if the part with given id is visible.
	 * <p>
	 * This field determines whether or not the part with given id is visible.
	 * </p>
	 * @param partId the id of the part to search for, must not be <code>null</code>
	 * @return true if the part with specified id is visible, else false.
	 */
	public static boolean isVisible(final String partId) {
		final EPartService partService = getService(EPartService.class);
		return partService.isPartVisible(getPart(partId));
	}

	/**
	 * Gets the service for the given class. For example:
	 * getService(EModelService.class)
	 * @param clazz the clazz of required service
	 * @return clazz type of found service, or null if not found
	 */
	public static <T> T getService(final Class<T> clazz) {
		return PlatformUI.getWorkbench().getService(clazz);
	}

	/**
	 * Finds and returns the partholder for the part with given id.
	 * The partholder is the part itself if not shared between perspectives
	 * (i.e. as views), else the placeholder of the shared part.
	 * This method helps detaching methods and hopefully others.
	 * @param partId the id of part to search for, must not be <code>null</code>
	 * @return the partholder for the part with specified id, or <code>null</code> if there is no partholder
	 */
	public static MUIElement getPartholder(final String partId) { 
		final MPart mPart = getPart(partId);
		if( mPart != null ) {
			final MPlaceholder mPlaceholder = getPlaceholder(mPart);
			if( mPlaceholder != null )
				return mPlaceholder;
		}
		return mPart;
	}

	/**
	 * Gets the placeholder for the given part.
	 * Note that only(?) views can have placeholders which are shared
	 * between perspectives.
	 * @param part the part having its placeholder, must not be <code>null</code>
	 * @return the placeholder for the specified part, or <code>null</code> if there is no placeholder
	 * @see MUIElement#getCurSharedRef()
	 */
	public static MPlaceholder getPlaceholder(final MPart part) {
		return part.getCurSharedRef();
	}

	/**
	 * Finds and returns the partholder for the given workbench part (E3) using E4 API.
	 * @param workbenchPart the workbench part to search for, must not be <code>null</code>
	 * @return the partholder for the specified workbench part, or <code>null</code> if there is no partholder
	 * @see #getPartholder(String)
	 */
	@Deprecated
	public static MUIElement getPartholder(final IWorkbenchPart workbenchPart) { 
		final String workbenchId = getId(workbenchPart);
		return getPartholder(workbenchId);
	}

	/**
	 * Finds and returns the placeholder for the view with given id.
	 * @param viewId the id of view part to search for, must not be <code>null</code>
	 * @return the placeholder for the view part with specified id, or <code>null</code> if there is no placeholder
	 */
	public static MPlaceholder getPlaceholder(final String viewId) {
		final MPart mPart = getPart(viewId);
		if( mPart != null )
			return getPlaceholder(mPart);
		return null;
	}

	/**
	 * Finds and returns the placeholder for the given view part (E3) using E4 API.
	 * @param viewPart the view part to search for, must not be <code>null</code>
	 * @return the placeholder for the specified view part, or <code>null</code> if there is no placeholder
	 */
	@Deprecated
	public static MPlaceholder getPlaceholder(final IViewPart viewPart) { 
		final String viewId = getId(viewPart);
		return getPlaceholder(viewId);
	}

	/**
	 * Finds and returns a part with the given id.
	 * @param partId
	 *            the id of the part to search for, must not be <code>null</code>
	 * @return the part with the specified id, or <code>null</code> if no such part could be found
	 */
	public static MPart getPart(final String partId) {
		final EPartService partService = getService(EPartService.class);
		return partService.findPart(partId);
	}

	/**
	 * Finds and returns a part for the given editor part (E3) using E4 API.
	 * @param editorPart the editor part to search for, must not be <code>null</code>
	 * @return the part for the specified editor part, or <code>null</code> if no such part could be found
	 */
	@Deprecated
	public static MPart getPart(final IEditorPart editorPart) { 
		final String editorId = getId(editorPart);
		return getPart(editorId);
	}

	/**
	 * Finds and returns a view part with the given id.
	 * @param viewId
	 *            the id of the view part to search for, must not be <code>null</code>
	 * @return the view part with the specified id, or <code>null</code> if no such part could be found
	 */
	@Deprecated
	public static IViewPart getViewPart(final String viewId) {
		return findView(viewId, false);
	}

	/**
	 * Finds and returns the rectangle for the part with the given id.
	 * @param partId the id of the part to search for, must not be <code>null</code>
	 * @return the rectangle for the part with the specified id, or <code>null</code> if no such part could be found
	 */
	public static Rectangle getRectangle(final String partId) {
		final MWindow mContainingWindow = (MWindow)findContainerWindow(getPartholder(partId));
		return new Rectangle(mContainingWindow.getX(), mContainingWindow.getY(), mContainingWindow.getWidth(), mContainingWindow.getHeight());
	}

	/**
	 * Detaches the given view part (E3) using E4 API.
	 * Origin at https://tomsondev.bestsolution.at/2012/07/13/so-you-used-internal-api/
	 * @param viewPart the view part to be detached
	 */
	@Deprecated 
	public static void detachViewPart(final IViewPart viewPart) {
		detachViewPart(viewPart, DETACH_RECTANGLE_DEFAULT);
	}

	/**
	 * Detaches the given view part (E3) using E4 API.
	 * Origin at https://tomsondev.bestsolution.at/2012/07/13/so-you-used-internal-api/
	 * @param viewPart the view part to be detached
	 * @param rectangle the rectangle of detached window
	 */
	@Deprecated 
	public static void detachViewPart(final IViewPart viewPart, final Rectangle rectangle) {
		final EModelService modelService = getService(EModelService.class);
		modelService.detach(getPlaceholder(viewPart), rectangle.x, rectangle.y, rectangle.width, rectangle.height);
	}

	/**
	 * Brings to top the part with the given id.
	 * @param partId the id of the part to search for, must not be <code>null</code>
	 */
	public static void bringToTop(final String partId) {
		final EModelService modelService = getService(EModelService.class);
		modelService.bringToTop(getPlaceholder(partId));
	}

	/**
	 * Brings to top the given view part (E3) using E4 API.
	 * @param viewPart the view part to be detached
	 */
	@Deprecated
	public static void bringToTopViewPart(final IViewPart viewPart) {
		final EModelService modelService = getService(EModelService.class);
		modelService.bringToTop(getPlaceholder(viewPart));
	}

	/**
	 * Detaches the given editor part (E3) using E4 API.
	 * @param editorPart the editor part to be detached
	 */
	@Deprecated
	public static void detachEditorPart(final IEditorPart editorPart) {
		detachEditorPart(editorPart, DETACH_RECTANGLE_DEFAULT);
	}

	/**
	 * Detaches the given editor part (E3) using E4 API.
	 * @param editorPart the editor part to be detached
	 * @param rectangle the rectangle of detached window
	 */
	@Deprecated
	public static void detachEditorPart(final IEditorPart editorPart, final Rectangle rectangle) {
		final EModelService modelService = getService(EModelService.class);
		modelService.detach(getPart(editorPart), rectangle.x, rectangle.y, rectangle.width, rectangle.height);
	}

	/**
	 * Brings to top the given editor part (E3) using E4 API.
	 * @param editorPart the editor part to be detached
	 */
	@Deprecated
	public static void bringToTopEditorPart(final IEditorPart editorPart) {
		final EModelService modelService = getService(EModelService.class);
		modelService.bringToTop(getPart(editorPart));
	}

	/**
	 * Detaches the given workbench part (E3) using E4 API.
	 * @param workbenchPart the workbench part to be detached
	 */
	@Deprecated
	public static void detachWorkbenchPart(final IWorkbenchPart workbenchPart) {
		detachWorkbenchPart(workbenchPart, DETACH_RECTANGLE_DEFAULT);
	}
	/**
	 * Detaches the given workbench part (E3) using E4 API.
	 * @param workbenchPart the workbench part to be detached
	 * @param rectangle the rectangle of detached window
	 */
	@Deprecated
	public static void detachWorkbenchPart(final IWorkbenchPart workbenchPart, final Rectangle rectangle) {
		final EModelService modelService = getService(EModelService.class);
		final String workbenchId = getId(workbenchPart);
		modelService.detach((MPartSashContainerElement)getPartholder(workbenchId), rectangle.x, rectangle.y, rectangle.width, rectangle.height);
	}

	/**
	 * Brings to top the given workbench part (E3) using E4 API.
	 * @param workbenchPart the workbench part to be detached
	 */
	@Deprecated
	public static void bringToTopWorkbenchPart(final IWorkbenchPart workbenchPart) {
		final EModelService modelService = getService(EModelService.class);
		final String workbenchId = getId(workbenchPart);
		modelService.bringToTop(getPartholder(workbenchId));
	}

	/**
	 * Brings to top the part with the given id.
	 * @param partId the id of the part to search for, must not be <code>null</code>
	 */
	public static void setFocus(final String partId) {
		final MPart mPart = getPart(partId);
		final EPartService partService = getService(EPartService.class);
		partService.activate(mPart, true);
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
	 * Gets the active part of active page. If the Eclipse model returns null,
	 * then the active part known by this workaround is returned.
	 * @return the active part of active page, or null if there is
	 * no workbench window or active page or active part
	 */
	public static IWorkbenchPart getActivePart() {
		IWorkbenchPart activePart = null;
		final IWorkbenchPage page = getActivePage();
		if( page != null )
			activePart = page.getActivePart();
		if( activePart == null )
			activePart = EclipseBug362561Workaround.getActivePart();
		return activePart;
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
		final IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench==null) return null;
		final IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
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
