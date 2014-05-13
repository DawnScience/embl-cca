package org.embl.cca.utils.ui.widget;

import java.io.File;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;

public class OverwriteConfirmationDialog extends MessageDialog {

	public final static String UNAVAILABLE_METHOD="The method is not available";

	/**
	 * Create a message dialog. Note that the dialog will have no visual
	 * representation (no widgets) until it is told to open.
	 * <p>
	 * The labels of the buttons to appear in the button bar are supplied in
	 * this constructor as an array. The <code>open</code> method will return
	 * the index of the label in this array corresponding to the button that was
	 * pressed to close the dialog.
	 * </p>
	 * <p>
	 * <strong>Note:</strong> If the dialog was dismissed without pressing a
	 * button (ESC key, close box, etc.) then {@link SWT#DEFAULT} is returned.
	 * Note that the <code>open</code> method blocks.
	 * </p>
	 * 
	 * @param parentShell
	 *            the parent shell
	 * @param dialogTitle
	 *            the dialog title, or <code>null</code> if none
	 * @param dialogTitleImage
	 *            the dialog title image, or <code>null</code> if none
	 * @param dialogMessage
	 *            the dialog message
	 * @param dialogImageType
	 *            one of the following values:
	 *            <ul>
	 *            <li><code>MessageDialog.NONE</code> for a dialog with no image
	 *            </li>
	 *            <li><code>MessageDialog.ERROR</code> for a dialog with an
	 *            error image</li>
	 *            <li><code>MessageDialog.INFORMATION</code> for a dialog with
	 *            an information image</li>
	 *            <li><code>MessageDialog.QUESTION </code> for a dialog with a
	 *            question image</li>
	 *            <li><code>MessageDialog.WARNING</code> for a dialog with a
	 *            warning image</li>
	 *            </ul>
	 * @param dialogButtonLabels
	 *            an array of labels for the buttons in the button bar
	 * @param defaultIndex
	 *            the index in the button label array of the default button
	 */
	public OverwriteConfirmationDialog(final Shell parentShell,
			final String dialogTitle, final Image dialogTitleImage,
			final String dialogMessage, final int dialogImageType,
			final String[] dialogButtonLabels, final int defaultIndex) {
		super(parentShell, dialogTitle, dialogTitleImage, dialogMessage,
				dialogImageType, dialogButtonLabels, defaultIndex);
	}

	/**
	 * Returns the dialog's message, or an empty string if it does not have one.
	 * The message is a description of the purpose for which the dialog was opened.
	 * This message will be visible in the dialog while it is open.
	 *
	 * @return the message
	 */
	public String getMessage() {
		return message;
	}

	@Override
	protected String[] getButtonLabels() {
		return super.getButtonLabels();
	}

	/**
	 * @param kind
	 * @return
	 */
	protected static String[] getButtonLabels(final int kind) {
		final String[] dialogButtonLabels;
		switch (kind) {
			case ERROR:
			case INFORMATION:
			case WARNING: {
				dialogButtonLabels = new String[] { IDialogConstants.OK_LABEL };
				break;
			}
			case CONFIRM: {
				dialogButtonLabels = new String[] { IDialogConstants.OK_LABEL,
						IDialogConstants.CANCEL_LABEL };
				break;
			}
			case QUESTION: {
				dialogButtonLabels = new String[] { IDialogConstants.YES_LABEL,
						IDialogConstants.NO_LABEL };
				break;
			}
			case QUESTION_WITH_CANCEL: {
				dialogButtonLabels = new String[] { IDialogConstants.YES_LABEL,
						IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL };
				break;
			}
			default: {
				throw new IllegalArgumentException(
						"Illegal value for kind in MessageDialog.open()"); //$NON-NLS-1$
			}
		}
		return dialogButtonLabels;
	}

	/**
	 * Unavailable method, it always throws UnsupportedOperationException.
	 * 
	 * @param kind
	 *            the kind of dialog to open, one of {@link #ERROR},
	 *            {@link #INFORMATION}, {@link #QUESTION}, {@link #WARNING},
	 *            {@link #CONFIRM}, or {@link #QUESTION_WITH_CANCEL}.
	 * @param parent
	 *            the parent shell of the dialog, or <code>null</code> if none
	 * @param title
	 *            the dialog's title, or <code>null</code> if none
	 * @param message
	 *            the message
	 * @param style
	 *            {@link SWT#NONE} for a default dialog, or {@link SWT#SHEET} for
	 *            a dialog with sheet behavior
	 * @return <code>true</code> if the user presses the OK or Yes button,
	 *         <code>false</code> otherwise
	 * @exception UnsupportedOperationException
	 */
	@Deprecated
	public static boolean open(final int kind, final Shell parent, final String title,
			final String message, final int style) {
		throw new UnsupportedOperationException(UNAVAILABLE_METHOD);
	}

	/**
	 * Unavailable method, it always throws UnsupportedOperationException.
	 * 
	 * @param parent
	 *            the parent shell of the dialog, or <code>null</code> if none
	 * @param title
	 *            the dialog's title, or <code>null</code> if none
	 * @param message
	 *            the message
	 * @return <code>true</code> if the user presses the OK button,
	 *         <code>false</code> otherwise
	 * @exception UnsupportedOperationException
	 */
	@Deprecated
	public static boolean openConfirm(Shell parent, String title, String message) {
		throw new UnsupportedOperationException(UNAVAILABLE_METHOD);
	}

	/**
	 * Unavailable method, it always throws UnsupportedOperationException.
	 * 
	 * @param parent
	 *            the parent shell of the dialog, or <code>null</code> if none
	 * @param title
	 *            the dialog's title, or <code>null</code> if none
	 * @param message
	 *            the message
	 * @exception UnsupportedOperationException
	 */
	@Deprecated
	public static void openError(Shell parent, String title, String message) {
		throw new UnsupportedOperationException(UNAVAILABLE_METHOD);
	}

	/**
	 * Unavailable method, it always throws UnsupportedOperationException.
	 * 
	 * @param parent
	 *            the parent shell of the dialog, or <code>null</code> if none
	 * @param title
	 *            the dialog's title, or <code>null</code> if none
	 * @param message
	 *            the message
	 * @exception UnsupportedOperationException
	 */
	@Deprecated
	public static void openInformation(Shell parent, String title,
			String message) {
		throw new UnsupportedOperationException(UNAVAILABLE_METHOD);
	}

	/**
	 * Unavailable method, it always throws UnsupportedOperationException.
	 * 
	 * @param parent
	 *            the parent shell of the dialog, or <code>null</code> if none
	 * @param title
	 *            the dialog's title, or <code>null</code> if none
	 * @param message
	 *            the message
	 * @return <code>true</code> if the user presses the Yes button,
	 *         <code>false</code> otherwise
	 * @exception UnsupportedOperationException
	 */
	@Deprecated
	public static boolean openQuestion(final Shell parent, final String title,
			final String message) {
		throw new UnsupportedOperationException(UNAVAILABLE_METHOD);
	}

	/**
	 * Unavailable method, it always throws UnsupportedOperationException.
	 * 
	 * @param parent
	 *            the parent shell of the dialog, or <code>null</code> if none
	 * @param title
	 *            the dialog's title, or <code>null</code> if none
	 * @param message
	 *            the message
	 * @exception UnsupportedOperationException
	 */
	@Deprecated
	public static void openWarning(Shell parent, String title, String message) {
		throw new UnsupportedOperationException(UNAVAILABLE_METHOD);
	}

	/**
	 * Convenience method to open a simple Yes/No question dialog about
	 * overwriting a file.
	 * 
	 * @param parent
	 *            the parent shell of the dialog, or <code>null</code> if none
	 * @param file
	 *            the file which is going to be overwritten
	 * @return <code>true</code> if the user presses the Yes button,
	 *         <code>false</code> otherwise
	 */
	public static boolean open(final Shell parent, final File file) {
		Assert.isLegal(parent != null, "The parent argument can not be null.");
		Assert.isLegal(file != null, "The file argument can not be null.");
		final OverwriteConfirmationDialog dialog = new OverwriteConfirmationDialog(
			parent, "Confirm File Overwrite", null,
			new StringBuilder("A file named \"").append(file.getName()).append("\" already exists in \"")
				.append(file.getParentFile().getAbsolutePath())
				.append("\".\n\nWould you like to overwrite it?").toString(),
				QUESTION, getButtonLabels(QUESTION), 0);
		return dialog.open() == 0;
	}

	/**
	 * Convenience method to open a simple Yes/No question dialog
	 * overwriting a file.
	 * 
	 * @param parent
	 *            the parent shell of the dialog, or <code>null</code> if none
	 * @param file
	 *            the filename the name of file which is going to be overwritten
	 * @return <code>true</code> if the user presses the Yes button,
	 *         <code>false</code> otherwise
	 */
	public static boolean open(final Shell parent, final String filename) {
		return open(parent, new File(filename));
	}
}
