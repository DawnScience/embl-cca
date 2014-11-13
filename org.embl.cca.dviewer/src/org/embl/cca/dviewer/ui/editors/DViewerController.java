package org.embl.cca.dviewer.ui.editors;

import java.text.NumberFormat;
import java.text.ParseException;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.embl.cca.dviewer.ui.editors.utils.PHA;
import org.embl.cca.utils.datahandling.text.DecimalPaddedFormat;
import org.embl.cca.utils.datahandling.text.StringUtils;
import org.embl.cca.utils.general.ISomethingChangeListener;
import org.embl.cca.utils.general.SomethingChangeEvent;
import org.embl.cca.utils.threading.ExecutableManager;
import org.embl.cca.utils.ui.widget.SpinnerSlider;

public class DViewerController implements ISomethingChangeListener {

	protected final IDViewerControllable controllable;

	final static protected NumberFormat decimalFormat = NumberFormat.getNumberInstance();

	protected Slider imageSlider;
	protected Label totalSliderImageLabel;
	final protected DecimalPaddedFormat totalSliderImageFormat = new DecimalPaddedFormat("#0");
	protected Label imageFilesWindowWidthLabel;
	protected Text imageFilesWindowWidthText;
	protected int imageSliderSelection;
//	private int imageFilesWindowWidth; //aka batchAmount
	protected Button autoSelectLatestNewImageButton;
	protected SpinnerSlider phaRadiusUI;

	private Label statusLabel = null;

	protected Composite parentComposite = null;
	protected ExecutableManager imageDisplayTracker = null;
	protected double lastUserMinimum;
	protected double lastUserMaximum;

	protected Button autoDisplayRemotedImage;
	protected Button displayRemotedImageDedicated;

	public DViewerController(final IDViewerControllable controllable) {
		this.controllable = controllable;
	}

	public void dispose() {
		controllable.removeSomethingListener(this);
	}

	protected void internalUpdateTotalSliderImageLabel() {
		final int sliderMax = imageSlider.getMaximum() - 1;
		totalSliderImageFormat.setMaximumIntegerDigits(1+(int)Math.floor(Math.log10(sliderMax)));
		totalSliderImageLabel.setText(new StringBuilder()
			.append(totalSliderImageFormat.format(controllable.getImageArrayBatchIndex()+1))
			.append('/').append(sliderMax).toString());
//		CommonExtension.layoutInParent(totalSliderImageLabel);
		controllable.revalidateLayout(totalSliderImageLabel);
	}

	protected void internalUpdateAutoSelectLatestNewImage() {
		autoSelectLatestNewImageButton.setSelection(controllable.getAutoSelectLatestNewImage());
	}

	protected void internalUpdateAutoDisplayRemotedImage() {
		final boolean idibrr = controllable.isDisplayingImageByRemoteRequest(); //selectedDisplayImageByRemoteRequest
		autoDisplayRemotedImage.setText(idibrr ? "❙❙" : "▶");
		autoDisplayRemotedImage.setToolTipText((idibrr ? "Do not d" : "D") + "isplay image by remote request");
//		CommonExtension.layoutInParent(autoDisplayRemotedImage);
		controllable.revalidateLayout(autoDisplayRemotedImage);
	}

	protected void internalUpdatePhaRadius() {
		phaRadiusUI.setSelectionAsInteger(controllable.getPhaRadius());
	}

	protected void internalUpdateStatus() {
		statusLabel.setText(controllable.getStatusText());
//		CommonExtension.layoutInParent(statusLabel);
//		statusLabel.getParent().getParent().layout(new Control[] {statusLabel});
//		CommonExtension.revalidateLayout(statusLabel);
		controllable.revalidateLayout(statusLabel);
	}

	public void createImageEditorGUI(final Composite parent) {
		parentComposite = parent;
		imageSlider = new Slider(parentComposite, SWT.HORIZONTAL | SWT.WRAP);
		totalSliderImageLabel = new Label(parentComposite, SWT.WRAP);
		imageFilesWindowWidthLabel = new Label(parentComposite, SWT.WRAP);
		imageFilesWindowWidthText = new Text(parentComposite, SWT.BORDER | SWT.RIGHT | SWT.WRAP);
		if( !controllable.isRemoted() ) {
			autoSelectLatestNewImageButton = new Button(parentComposite, SWT.CHECK | SWT.WRAP);
		} else {
			autoDisplayRemotedImage = new Button(parentComposite, SWT.PUSH | SWT.WRAP);
			displayRemotedImageDedicated = new Button(parentComposite, SWT.PUSH | SWT.WRAP);
		}
		phaRadiusUI = new SpinnerSlider(parentComposite, SWT.HORIZONTAL | SWT.WRAP);
	}

	public void createImageEditorStatusBar(final Composite parent) {
		statusLabel = new Label(parent, SWT.WRAP);
	}

	public void initializeImageSelectorUI() {
		imageSlider.setToolTipText("Image selector");
		imageSlider.setThumb(controllable.getImageArrayBatchSize());
		imageSliderSelection = imageSlider.getSelection();
		//Setting monospace font to avoid jumping numbers left-right
		totalSliderImageLabel.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));
		totalSliderImageLabel.setToolTipText("Selected image/Number of images");
		internalUpdateTotalSliderImageLabel();
		imageSlider.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				controllable.setBatchIndex(DViewerController.this, imageSlider.getSelection() - 1);
//				updateSliderByUser( imageSlider.getSelection() );
			}
		});
		imageFilesWindowWidthLabel.setToolTipText("Number of images to sum up");
		imageFilesWindowWidthLabel.setText("Batch Amount");
		imageFilesWindowWidthText.setToolTipText(imageFilesWindowWidthLabel.getToolTipText());
		imageFilesWindowWidthText.setText(new StringBuilder().append(controllable.getImageArrayBatchSize()).toString());
//		imageFilesWindowWidthText.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		imageFilesWindowWidthText.addVerifyListener(new VerifyListener() {
			@Override
			public void verifyText(final VerifyEvent e) {
				try {
					final String newValue = StringUtils.replaceRange(imageFilesWindowWidthText.getText(), e.text, e.start, e.end );
					e.doit = controllable.isBatchSizeValid( decimalFormat.parse( newValue ).intValue() );
				} catch (final ParseException e1) {
					e.doit = e.text.isEmpty();
				}
			}
		});
		imageFilesWindowWidthText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(final ModifyEvent e) {
				if( imageFilesWindowWidthText.getText().isEmpty() )
					return;
				try {
					controllable.setBatchSize(DViewerController.this, decimalFormat.parse( imageFilesWindowWidthText.getText() ).intValue());
//					updateBatchAmount( decimalFormat.parse( imageFilesWindowWidthText.getText() ).intValue() );
				} catch (final ParseException e1) {
				}
			}
		});
		if( !controllable.isRemoted() ) {
			autoSelectLatestNewImageButton.setText("Auto latest");
			autoSelectLatestNewImageButton.setToolTipText("Automatically scan directory and display last batch");
			autoSelectLatestNewImageButton.setSelection(controllable.getAutoSelectLatestNewImage());
			autoSelectLatestNewImageButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					controllable.setAutoSelectLatestNewImage(DViewerController.this, autoSelectLatestNewImageButton.getSelection());
				}
			});
		} else {
			internalUpdateAutoDisplayRemotedImage();
			autoDisplayRemotedImage.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					controllable.toggleAutoDisplayRemotedImage();
				}
			});
			displayRemotedImageDedicated.setText("O");
			displayRemotedImageDedicated.setToolTipText("Display image in a dedicated image editor");
			displayRemotedImageDedicated.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					controllable.displayRemotedImageDedicated();
				}
			});
		}
		phaRadiusUI.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
//				controllable.displayRemotedImageDedicated(DViewerController.this, psfRadiusUI.getSelection());
//				updatePsfRadiusSlider( psfRadiusUI.getSelectionAsInteger() );
				controllable.setPhaRadius(DViewerController.this, phaRadiusUI.getSelectionAsInteger());
			}
		});
//		phaRadiusUI.setValues(PHA.featureShortName + " Radius", (Integer)EditorPreferenceHelper.getStoreValue(DViewerActivator.getLocalPreferenceStore(), DViewerEditorConstants.PREFERENCE_PHA_RADIUS),
//				controllable.getPhaRadiusMin(), controllable.getPhaRadiusSup() - 1, 0, 1, 10, 1, 10);
		phaRadiusUI.setValues(PHA.featureShortName + " Radius", controllable.getPhaRadius(),
				controllable.getPhaRadiusMin(), controllable.getPhaRadiusSup() - 1, 0, 1, 10, 1, 10);
		controllable.setPhaRadius(DViewerController.this, phaRadiusUI.getSelectionAsInteger());
//		updatePsfRadiusSlider( Activator.getDefault().getPreferenceStore().getInt(EditorConstants.PREFERENCE_PSF_RADIUS) );

		//StatusBar
		//Setting monospace font to avoid jumping numbers left-right
		statusLabel.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));

		controllable.addSomethingListener(this);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * GUI thread is assumed.
	 */
	@Override
	public void somethingChange(final SomethingChangeEvent event) {
		if( event.getSomethingName().equals(SomethingChangeEvent.MOUSE_POSITION) ) {
			internalUpdateStatus();
		} else if( event.getSomethingName().equals(SomethingChangeEvent.IMAGE_ARRAY_SOMETHING)) {
			final int min = controllable.getImageArrayMin();
			final int sup = controllable.getImageArraySup(); //total
			final int batchIndex = controllable.getImageArrayBatchIndex(); //selection
			final int batchSize = controllable.getImageArrayBatchSize(); //imageFilesWindowWidth
			//Note: Slider is 1 based, controllable is 0 based!
			imageSlider.setValues(batchIndex+1, min+1, sup+1, batchSize, 1, Math.max(batchSize, sup/5));
			internalUpdateTotalSliderImageLabel();
		} else if( event.getSomethingName().equals(SomethingChangeEvent.AUTO_SELECT_LATEST_NEW_IMAGE)) {
			internalUpdateAutoSelectLatestNewImage();
		} else if( event.getSomethingName().equals(SomethingChangeEvent.AUTO_DISPLAY_REMOTED_IMAGE)) {
			internalUpdateAutoDisplayRemotedImage();
		} else if( event.getSomethingName().equals(SomethingChangeEvent.PHA_RADIUS)) {
			internalUpdatePhaRadius();
		}
	}

}
