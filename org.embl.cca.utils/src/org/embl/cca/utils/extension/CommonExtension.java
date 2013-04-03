package org.embl.cca.utils.extension;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IShowEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;

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
	public static IEditorPart openEditor(IWorkbenchPage iWP, IEditorInput input, String editorId,
			boolean bringToTop, boolean activate) throws PartInitException {
//		return openEditor(input, editorID, activate, MATCH_INPUT); //Original from WorkbenchPage.class
		IEditorPart iEP = null;
		if( bringToTop ) {
			iEP = iWP.openEditor(input, editorId, activate );
		} else {
			iEP = iWP.findEditor(input);
			if( iEP != null ) {
				if (iEP instanceof IShowEditorInput) {
					((IShowEditorInput) iEP).showEditorInput(input);
				}
				//iWP.showEditor(activate, iEP); //Original from WorkbenchPage.class, but can not use it, because protected, thus not in interface
			} else {
				iEP = iWP.openEditor(input, editorId, activate );
			}
		}
		return iEP;
	}
}
