package org.embl.cca.dviewer.ui.editors;

import java.text.NumberFormat;
import java.text.ParseException;

import org.dawb.common.ui.util.GridUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.embl.cca.dviewer.ui.editors.utils.PHA;
import org.embl.cca.utils.datahandling.text.StringUtils;
import org.embl.cca.utils.general.ISomethingChangeListener;
import org.embl.cca.utils.general.SomethingChangeEvent;
import org.embl.cca.utils.threading.ExecutableManager;
import org.embl.cca.utils.ui.widget.SpinnerSlider;

public class DViewerController implements ISomethingChangeListener {

	protected final IDViewerControllable controllable;

	final static private NumberFormat decimalFormat = NumberFormat.getNumberInstance();

	private Label totalSliderImageLabel;
	private Slider imageSlider;
	private int imageSliderSelection;
	private Text imageFilesWindowWidthText;
//	private int imageFilesWindowWidth; //aka batchAmount
	Button autoSelectLatestNewImageButton;
	private SpinnerSlider phaRadiusUI;
	ExecutableManager phaRadiusManager = null;

	protected Composite controlComposite = null;
	private Text minValueText = null;
//	private LogScale userMinimumScale = null;
	private SpinnerSlider userMinimumScale = null;
	private Text suggestedMinimumText = null;
	private Text maxValueText = null;
//	private LogScale userMaximumScale = null;
	private SpinnerSlider userMaximumScale = null;
	private Text suggestedMaximumText = null;
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

	public void createImageEditorGUI(final Composite parent) {
		/* Top line containing image selector sliders */
		createImageSelectorUI(parent);
		/* bottom line containing status and load image controls */
		createImageControlUI(parent);
		controllable.addSomethingListener(this);
	}

	protected void internalUpdateTotalSliderImageLabel() {
		totalSliderImageLabel.setText(new StringBuilder(String.valueOf(controllable.getImageArrayBatchIndex()+1)).append('/').append(controllable.getImageArraySup()).toString());
		totalSliderImageLabel.getParent().pack();
	}

	protected void internalUpdateAutoSelectLatestNewImage() {
		autoSelectLatestNewImageButton.setSelection(controllable.getAutoSelectLatestNewImage());
	}

	protected void internalUpdateAutoDisplayRemotedImage() { //TODO will be internal
		final boolean idibrr = controllable.isDisplayingImageByRemoteRequest(); //selectedDisplayImageByRemoteRequest
		autoDisplayRemotedImage.setText(idibrr ? "❙❙" : "▶");
		autoDisplayRemotedImage.setToolTipText((idibrr ? "Do not d" : "D") + "isplay image by remote request");
		autoDisplayRemotedImage.getParent().pack();
	}

	protected void internalUpdatePhaRadius() {
		phaRadiusUI.setSelectionAsInteger(controllable.getPhaRadius());
	}

	public void createImageSelectorUI(final Composite parent) {
		final Composite sliderMain = new Composite(parent, SWT.NONE);
		sliderMain.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		sliderMain.setLayout(new GridLayout(8, false));
//		GridUtils.removeMargins(sliderMain);
		
		imageSlider = new Slider(sliderMain, SWT.HORIZONTAL);
		imageSlider.setToolTipText("Image selector");
		imageSlider.setThumb(controllable.getImageArrayBatchSize());
		imageSliderSelection = imageSlider.getSelection();
//		imageSlider.setBounds(115, 50, 25, 15);
		totalSliderImageLabel = new Label(sliderMain, SWT.NONE);
		totalSliderImageLabel.setToolTipText("Selected image/Number of images");
		internalUpdateTotalSliderImageLabel();
		imageSlider.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				controllable.setBatchIndex(DViewerController.this, imageSlider.getSelection() - 1);
//				updateSliderByUser( imageSlider.getSelection() );
			}
		});
		final Label imageFilesWindowWidthLabel = new Label(sliderMain, SWT.NONE);
		imageFilesWindowWidthLabel.setToolTipText("Number of images to sum up");
		imageFilesWindowWidthLabel.setText("Batch Amount");
		imageFilesWindowWidthText = new Text(sliderMain, SWT.BORDER | SWT.RIGHT);
		imageFilesWindowWidthText.setToolTipText(imageFilesWindowWidthLabel.getToolTipText());
		imageFilesWindowWidthText.setText(new StringBuilder(String.valueOf(controllable.getImageArrayBatchSize())).toString());
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
			autoSelectLatestNewImageButton = new Button(sliderMain, SWT.CHECK);
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
			autoDisplayRemotedImage = new Button(sliderMain, SWT.PUSH);
			internalUpdateAutoDisplayRemotedImage();
			autoDisplayRemotedImage.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					controllable.toggleAutoDisplayRemotedImage();
				}
			});
			displayRemotedImageDedicated = new Button(sliderMain, SWT.PUSH);
			displayRemotedImageDedicated.setText("O");
			displayRemotedImageDedicated.setToolTipText("Display image in a dedicated image editor");
			displayRemotedImageDedicated.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					controllable.displayRemotedImageDedicated();
				}
			});
		}
		phaRadiusUI = new SpinnerSlider(sliderMain, SWT.HORIZONTAL);
		phaRadiusUI.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
////		psfRadiusUI.setToolTipText(PSF.featureName + " radius selector");
////		psfRadiusUI.setThumb(1);
//		psfRadiusUI.setBounds(115, 50, 25, 15);
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
	}

	public void createImageControlUI(Composite parent) {
		/**
		 * A text to adjust 7 sized width of GUI displaying value.
		 */
		final String GUIValue7WidthSetter = "0000000";
		final int logScaleMin = 0;
		final int logScaleMax = 31;

		if( true ) //TODO Temporary disabled until something is implemented here
			return;
//		final boolean isAutoScale = Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_AUTOSCALE);

//		Display display = parent.getDisplay();
		controlComposite = new Composite(parent, SWT.NONE);
		controlComposite.setLayout(new GridLayout(7, false));
		controlComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		GridUtils.removeMargins(controlComposite);

		// Minimum original
		Label label = new Label(controlComposite, SWT.NONE); //Column 1
		label.setText("Min Value=");
		label.setToolTipText("The minimum value found in image");
		minValueText = new Text(controlComposite, SWT.RIGHT); //Column 2
		minValueText.setText(GUIValue7WidthSetter);
		minValueText.setEditable(false);
		minValueText.setToolTipText(label.getToolTipText());

		// Suggested minimum
		label = new Label(controlComposite, SWT.NONE); //Column 3
		label.setText("Suggested=");
		label.setToolTipText("The suggested minimum intensity used by the palette");
		suggestedMinimumText = new Text(controlComposite, SWT.RIGHT); //Column 4
		suggestedMinimumText.setText(GUIValue7WidthSetter);
		suggestedMinimumText.setEditable(false);
		suggestedMinimumText.setToolTipText(label.getToolTipText());

		//Empty place
		label = new Label(controlComposite, SWT.NONE); //Column 5
		label.setVisible(false);

		// Minimum current
//		label = new Label(controlComposite, SWT.NONE); //Column 6
//		label.setText("Min Intensity:");
//		label.setToolTipText("The minimum intensity used by the palette");
//		userMinimumScale = new LogScale(controlComposite, SWT.NONE); //Column 7
//		userMinimumScale.setMinimum(logScaleMin);
//		userMinimumScale.setMaximum(logScaleMax);
//		userMinimumScale.setToolTipText("The currently set minimum intensity used by the palette");
//		userMinimumScale.addSelectionListener(new SelectionListener() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				if( userMinimumScale == null || userMinimumScale.isDisposed()) return;
//				final float v = (float)userMinimumScale.getLogicalSelection();
//				updateIntensityMin(v);
//			}
//
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e) {
//				widgetSelected(e);
//			}
//		});
//		label = new Label(controlComposite, SWT.NONE); //Column 8
//		label.setText("Current=");
//		label.setToolTipText(userMinimumScale.getToolTipText());
//		userMinimumText = new Text(controlComposite, SWT.BORDER | SWT.RIGHT); //Column 7
//		userMinimumText.setText(GUIValue7WidthSetter);
//		userMinimumText.setToolTipText(userMinimumScale.getToolTipText());
//		userMinimumText.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
//		userMinimumText.addModifyListener(new ModifyListener() {
//			@Override
//			public void modifyText(ModifyEvent e) {
//				if( userMinimumText == null || userMinimumText.isDisposed()) return;
//				if( !userMinimumText.isEnabled() || userMinimumText.getText().isEmpty() ) return;
//				try {
//					updateIntensityMin(decimalFormat.parse(userMinimumText.getText()).floatValue());
//				} catch (ParseException ex) {
//					logger.warn("Unable to parse minimum value: "+ userMinimumText.getText());
//				}
//			}
//		});
////		userMinimumText.setEnabled(!isAutoScale);
////		userMinimumScale.setEnabled(!isAutoScale);
/*		userMinimumScale = new SpinnerSlider( controlComposite, SWT.NONE ); //Column 6
		userMinimumScale.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 2, 1));
		userMinimumScale.setToolTipText("The minimum threshold used by the palette");
		userMinimumScale.setValues("Min Threshold", 0,
				logScaleMin, logScaleMax, 3, 1, 10, 1, 10, 0, 11, false); //TODO want digits=3, but does not work in SpinnerSlider yet
		userMinimumScale.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
//				if( userMinimumScale == null || userMinimumScale.isDisposed()) return;
				final double v = (double)userMinimumScale.getSelectionAsDouble();
				System.out.println("GRRR: userMinimumScale.widgetSelected: updateIntensityMin(getSelectionAsDouble=" + v + ")");
				updateIntensityMin(v);
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		userMinimumScale.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
//				System.out.println("GRRR: userMinimumScale.modifyText, doing nothing!");
				final double v = (double)userMinimumScale.getSelectionAsDouble();
				System.out.println("GRRR: userMinimumScale.modifyText, updateIntensityMin(" + v + ")");
				updateIntensityMin(v);
//				userMinimumScale.selectCurrentValue(); //Updating selection (and its dependants) when text changes
//				userMinimumScale.setSelectionAsDouble(userMinimumScale.getSelectionAsDouble()); //Looks funny, but that is the way
			}
		});

		// Maximum original
		label = new Label(controlComposite, SWT.NONE); //Column 1
		label.setText("Max Value=");
		label.setToolTipText("The maximum intensity used by the palette");
		maxValueText = new Text(controlComposite, SWT.RIGHT); //Column 2
		maxValueText.setText(GUIValue7WidthSetter);
		maxValueText.setEditable(false);
		maxValueText.setToolTipText(label.getToolTipText());

		// Suggested maximum
		label = new Label(controlComposite, SWT.NONE); //Column 3
		label.setText("Suggested=");
		label.setToolTipText("The suggested maximum intensity used by the palette");
		suggestedMaximumText = new Text(controlComposite, SWT.RIGHT); //Column 4
		suggestedMaximumText.setText(GUIValue7WidthSetter);
		suggestedMaximumText.setEditable(false);
		suggestedMaximumText.setToolTipText(label.getToolTipText());

		//Use suggested
		Button button = new Button(controlComposite, SWT.PUSH); //Column 5
		button.setText("Use suggested");
		button.setToolTipText("Use suggested value");
		button.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
//				setUserMinimum(getSuggestedMinimum());
//				setUserMaximum(getSuggestedMaximum());
				setIntensityMinMax(getSuggestedMinimum(), getSuggestedMaximum());
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		// Maximum current
//		label = new Label(controlComposite, SWT.NONE); //Column 6
//		label.setText("Max Intensity:");
//		label.setToolTipText("The maximum intensity used by the palette");
//		userMaximumScale = new LogScale(controlComposite, SWT.NONE); //Column 7
//		userMaximumScale.setMinimum(logScaleMin);
//		userMaximumScale.setMaximum(logScaleMax);
//		userMaximumScale.setToolTipText("The currently set maximum intensity used by the palette");
//		label = new Label(controlComposite, SWT.NONE); //Column 8
//		label.setText("Current=");
//		label.setToolTipText(userMaximumScale.getToolTipText());
//		userMaximumScale.addSelectionListener(new SelectionListener() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				if( userMaximumScale == null || userMaximumScale.isDisposed()) return;
//				final float v = (float)userMaximumScale.getLogicalSelection();
//				updateIntensityMax(v);
//			}
//
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e) {
//				widgetSelected(e);
//			}
//		});
//		userMaximumText = new Text(controlComposite, SWT.BORDER | SWT.RIGHT); //Column 9
//		userMaximumText.setToolTipText(userMaximumScale.getToolTipText());
//		userMaximumText.setText(GUIValue7WidthSetter);
//		userMaximumText.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
//		userMaximumText.addModifyListener(new ModifyListener() {
//			@Override
//			public void modifyText(ModifyEvent e) {
//				if( userMaximumText == null || userMaximumText.isDisposed()) return;
//				if( !userMaximumText.isEnabled() || userMaximumText.getText().isEmpty() ) return;
//				try {
//					updateIntensityMax(decimalFormat.parse(userMaximumText.getText()).floatValue());
//				} catch (ParseException ex) {
//					logger.warn("Unable to parse maximum value: "+ userMaximumText.getText());
//				}
//			}
//			;
//		});
////		userMaximumText.setEnabled(!isAutoScale);
////		userMaximumScale.setEnabled(!isAutoScale);
		userMaximumScale = new SpinnerSlider( controlComposite, SWT.NONE ); //Column 6
		userMaximumScale.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 2, 1));
		userMaximumScale.setToolTipText("The maximum threshold used by the palette");
		userMaximumScale.setValues("Max Threshold", 0,
				logScaleMin, logScaleMax, 3, 1, 10, 1, 10, 0, 1, false); //TODO want digits=3, but does not work in SpinnerSlider yet
		userMaximumScale.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
//				if( userMaximumScale == null || userMaximumScale.isDisposed()) return;
				final double v = (double)userMaximumScale.getSelectionAsDouble();
				System.out.println("GRRR: userMaximumScale.widgetSelected: updateIntensityMax(getSelectionAsDouble=" + v + ")");
				updateIntensityMax(v);
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		userMaximumScale.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
//				System.out.println("GRRR: userMinimumScale.modifyText, doing nothing!");
				final double v = (double)userMaximumScale.getSelectionAsDouble();
				System.out.println("GRRR: userMaximumScale.modifyText, updateIntensityMax(" + v + ")");
				updateIntensityMax(v);
//				userMaximumScale.selectCurrentValue(); //Updating selection (and its dependants) when text changes
//				userMaximumScale.setSelectionAsDouble(userMaximumScale.getSelectionAsDouble()); //Looks funny, but that is the way
			}
		});
*/
	}

	/**
	 * {@inheritDoc}
	 * GUI thread is assumed.
	 */
	@Override
	public void somethingChange(final SomethingChangeEvent event) {
//		if( CommonThreading.isCurrentThreadGUI() )
//			return; //Because we get notifications for changes caused by this, which we want to ignore
		if( event.getSomethingName().equals(SomethingChangeEvent.IMAGE_ARRAY_SOMETHING)) {
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
