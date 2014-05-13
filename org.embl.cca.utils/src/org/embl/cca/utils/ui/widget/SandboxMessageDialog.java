package org.embl.cca.utils.ui.widget;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * This class is for experimenting with things in a dialog, for developers.
 * 
 */
public class SandboxMessageDialog extends MessageDialog {

	public SandboxMessageDialog(final Shell parentShell,
			final String dialogTitle, final Image dialogTitleImage,
			final String dialogMessage, final int dialogImageType,
			final String[] dialogButtonLabels, final int defaultIndex) {
		super(parentShell, dialogTitle, dialogTitleImage, dialogMessage,
				dialogImageType, dialogButtonLabels, defaultIndex);
	}

	/**
	 * @see org.eclipse.jface.dialogs.Dialog#createContents(Composite)
	 */
	@Override
	protected Control createContents(final Composite parent) {
		return super.createContents(parent);
	}

	@Override
	protected Control createCustomArea(final Composite parent) {
		return super.createCustomArea(parent);
	}

	@Override
	protected Control createDialogArea(final Composite parent) {
		return super.createDialogArea(parent);
		/* This GUI is not tested, it is only an example */
//		// create the top level composite for the dialog area
//		Composite composite = new Composite(parent, SWT.NONE);
//		GridLayout layout = new GridLayout(); // 2, true
//		layout.marginHeight = 0;
//		layout.marginWidth = 0;
//		composite.setLayout(layout);
//		GridData data = new GridData(GridData.FILL_BOTH);
//		data.horizontalSpan = 2;
//		composite.setLayoutData(data);
//
////		// Show the bold message
////		Label label = new Label(composite, SWT.NONE);
////		label.setText(title);
////		data = new GridData();
////		data.horizontalSpan = 2;
////		label.setLayoutData(data);
//
//		// Show not bold the message
//		Label label2 = new Label(composite, SWT.NONE);
//		label2.setText(message);
//		data = new GridData();
//		data.horizontalSpan = 2;
//		label2.setLayoutData(data);
//
//		// Create the OK button and add a handler
//		// so that pressing it will set input
//		// to the entered value
//		Button ok = new Button(composite, SWT.PUSH);
//		ok.setImage(Activator.getDefault().getImageRegistry().get(ImageConstants.IMG_SAVE_AS));
//		ok.setText("Replace");
//		data = new GridData(GridData.FILL_HORIZONTAL);
//		ok.setLayoutData(data);
//
//		// Create the cancel button and add a handler
//		// so that pressing it will set input to null
//		Button cancel = new Button(composite, SWT.PUSH);
//		cancel.setImage(Activator.getDefault().getImageRegistry().get(ImageConstants.IMG_CANCEL));
//		cancel.setText("Cancel");
//		data = new GridData(GridData.FILL_HORIZONTAL);
//		cancel.setLayoutData(data);
//
//		ok.setFocus();
//		return composite;
//	}

//	public String getTitle() {
//		return title;
//	}
//
//	public void setTitle(final String string) {
//		if (string == null) SWT.error(SWT.ERROR_NULL_ARGUMENT);
//		title = string;
	}

}
