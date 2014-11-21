package org.embl.cca.dviewer.ui.editors;

import java.text.NumberFormat;
import java.text.ParseException;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.embl.cca.dviewer.ui.editors.utils.PHA;
import org.embl.cca.utils.datahandling.text.DecimalPaddedFormat;
import org.embl.cca.utils.datahandling.text.StringUtils;
import org.embl.cca.utils.extension.CommonExtension;
import org.embl.cca.utils.general.ISomethingChangeListener;
import org.embl.cca.utils.general.SomethingChangeEvent;
import org.embl.cca.utils.threading.ExecutableManager;
import org.embl.cca.utils.ui.widget.SpinnerSlider;

public class DViewerController {

	protected final IDViewerControllable controllable;
	protected final Composite layouterTopParent;

	protected final static NumberFormat decimalFormat = NumberFormat.getNumberInstance();

	protected Slider imageSlider;
	protected Label totalSliderImageLabel;
	protected final static DecimalPaddedFormat totalSliderImageFormat = new DecimalPaddedFormat("#0");
	protected Label imageFilesWindowWidthLabel;
	protected Spinner imageFilesWindowWidthSpinner;

	protected int imageSliderSelection;
	protected Button autoSelectLatestNewImageButton;
	protected SpinnerSlider phaRadiusUI;

	protected Composite parentComposite;
	protected ExecutableManager imageDisplayTracker;
	protected double lastUserMinimum;
	protected double lastUserMaximum;

	protected Button autoDisplayRemotedImage;
	protected Button displayRemotedImageDedicated;
	protected Label showEachLabel;
	protected Spinner showEachRemotedImageSpinner;
	protected Label showRemotedImageLabel;

	protected SpinnerSlider hRangeMinUI, hRangeMaxUI, kRangeMinUI, kRangeMaxUI, lRangeMinUI, lRangeMaxUI;
	protected Button hklEnabledButton;

	protected final ISomethingChangeListener somethingChangeListener = new ISomethingChangeListener() {
		/**
		 * {@inheritDoc}
		 * <p>
		 * GUI thread is assumed.
		 */
		@Override
		public void somethingChange(final SomethingChangeEvent event) {
			if( event.getSomethingName().equals(SomethingChangeEvent.MOUSE_POSITION) ) {
				;
			} else if( event.getSomethingName().equals(SomethingChangeEvent.IMAGE_ARRAY_SOMETHING)) {
				internalUpdateTotalSliderImageLabel();
			} else if( event.getSomethingName().equals(SomethingChangeEvent.AUTO_SELECT_LATEST_NEW_IMAGE)) {
				internalUpdateAutoSelectLatestNewImage();
			} else if( event.getSomethingName().equals(SomethingChangeEvent.AUTO_DISPLAY_REMOTED_IMAGE)) {
				internalUpdateAutoDisplayRemotedImage();
			} else if( event.getSomethingName().equals(SomethingChangeEvent.PHA_RADIUS)) {
				internalUpdatePhaRadius();
			} else if( event.getSomethingName().equals(SomethingChangeEvent.SHOW_EACH_NTH_IMAGE)) {
				internalUpdateShowEachNthImage();
			}
		}
	}; 

	final SelectionListener phaRadiusSelectionListener = new SelectionAdapter() {
		public void widgetSelected(final SelectionEvent e) {
			controllable.setPhaRadius(somethingChangeListener, phaRadiusUI.getSelectionAsInteger());
		}
	};

	final SelectionListener hRangeMinSelectionListener = new SelectionAdapter() {
		public void widgetSelected(final SelectionEvent e) {
			controllable.setHRangeMin(somethingChangeListener, hRangeMinUI.getSelectionAsInteger());
		}
	};
	final SelectionListener hRangeMaxSelectionListener = new SelectionAdapter() {
		public void widgetSelected(final SelectionEvent e) {
			controllable.setHRangeMax(somethingChangeListener, hRangeMaxUI.getSelectionAsInteger());
		}
	};
	final SelectionListener kRangeMinSelectionListener = new SelectionAdapter() {
		public void widgetSelected(final SelectionEvent e) {
			controllable.setKRangeMin(somethingChangeListener, kRangeMinUI.getSelectionAsInteger());
		}
	};
	final SelectionListener kRangeMaxSelectionListener = new SelectionAdapter() {
		public void widgetSelected(final SelectionEvent e) {
			controllable.setKRangeMax(somethingChangeListener, kRangeMaxUI.getSelectionAsInteger());
		}
	};
	final SelectionListener lRangeMinSelectionListener = new SelectionAdapter() {
		public void widgetSelected(final SelectionEvent e) {
			controllable.setLRangeMin(somethingChangeListener, lRangeMinUI.getSelectionAsInteger());
		}
	};
	final SelectionListener lRangeMaxSelectionListener = new SelectionAdapter() {
		public void widgetSelected(final SelectionEvent e) {
			controllable.setLRangeMax(somethingChangeListener, lRangeMaxUI.getSelectionAsInteger());
		}
	};

	public DViewerController(final IDViewerControllable controllable, final Composite layouterTopParent) {
		this.controllable = controllable;
		this.layouterTopParent = layouterTopParent;
	}

	public void dispose() {
		System.out.println("*** " + getClass().getSimpleName() + " is disposing!");
		controllable.removeSomethingListener(somethingChangeListener);
	}

	protected void internalUpdateTotalSliderImageLabel() {
		final int min = controllable.getImageArrayMin();
		final int sup = controllable.getImageArraySup(); //total
		final int batchIndex = controllable.getImageArrayBatchIndex(); //selection
		final int batchSize = controllable.getImageArrayBatchSize(); //imageFilesWindowWidth
		//Note: Slider is 1 based, controllable is 0 based!
		imageSlider.setValues(batchIndex+1, min+1, sup+1, batchSize, 1, Math.max(batchSize, sup/5));
		totalSliderImageFormat.setMaximumIntegerDigits(1+(int)Math.floor(Math.log10(sup)));
		totalSliderImageLabel.setText(new StringBuilder()
			.append(totalSliderImageFormat.format(controllable.getImageArrayBatchIndex()+1))
			.append('/').append(sup).toString());
		//Note: SpinnerSlider is 1 based, controllable is 0 based!
		imageFilesWindowWidthSpinner.setValues(batchSize, min+1, sup+1, 0, 1, Math.max(1,  sup/5));
		System.out.println("SS min=" + imageFilesWindowWidthSpinner.getMinimum() + ", max=" + imageFilesWindowWidthSpinner.getMaximum());
		CommonExtension.layoutIn(totalSliderImageLabel, layouterTopParent);
	}

	protected void internalUpdateAutoSelectLatestNewImage() {
		autoSelectLatestNewImageButton.setSelection(controllable.getAutoSelectLatestNewImage());
	}

	protected void internalUpdateAutoDisplayRemotedImage() {
		final boolean idibrr = controllable.isDisplayingImageByRemoteRequest(); //selectedDisplayImageByRemoteRequest
		autoDisplayRemotedImage.setText(idibrr ? "❙❙" : "▶");
		autoDisplayRemotedImage.setToolTipText((idibrr ? "Do not d" : "D") + "isplay image by remote request");
		CommonExtension.layoutIn(autoDisplayRemotedImage, layouterTopParent);
	}

	protected void internalUpdatePhaRadius() {
		phaRadiusUI.setSelectionAsInteger(controllable.getPhaRadius());
	}

	protected void internalUpdateShowEachNthImage() {
		showEachRemotedImageSpinner.setSelection(controllable.getShowEachNthImage());
	}

	public static RowLayout createRowLayout() {
		final RowLayout rowLayout = new RowLayout(SWT.HORIZONTAL);
		rowLayout.spacing = 3; //default: 3
		rowLayout.marginBottom = 0; //default: 3
		rowLayout.marginTop = 0; //default: 3
		rowLayout.marginLeft = 0; //default: 3
		rowLayout.marginRight = 0; //default: 3
		rowLayout.marginHeight = 0; //default: 0
		rowLayout.marginWidth = 0; //default: 0
		rowLayout.center = true;
		return rowLayout;
	}

	public static GridLayout createGridLayout(final int numColumns) {
		final GridLayout gridLayout = new GridLayout(numColumns, false);
		gridLayout.horizontalSpacing = 3; //default: 5
		gridLayout.verticalSpacing = 3; //default: 5
		gridLayout.marginBottom = 0; //default: 0
		gridLayout.marginTop = 0; //default: 0
		gridLayout.marginLeft = 0; //default: 0
		gridLayout.marginRight = 0; //default: 0
		gridLayout.marginHeight = 0; //default: 5
		gridLayout.marginWidth = 0; //default: 5
		return gridLayout;
	}

	public static TableWrapLayout createTableWrapLayout(final int numColumns) {
		final TableWrapLayout tableWrapLayout = new TableWrapLayout();
		tableWrapLayout.numColumns = numColumns;
		tableWrapLayout.horizontalSpacing = 3; //default: 5
		tableWrapLayout.verticalSpacing = 3; //default: 5
		tableWrapLayout.bottomMargin = 0; //default: 5
		tableWrapLayout.topMargin = 0; //default: 5
		tableWrapLayout.leftMargin = 0; //default: 5
		tableWrapLayout.rightMargin = 0; //default: 5
		return tableWrapLayout;
	}

	public void createImageEditorGUI(final Composite parent) {
		parentComposite = parent;

		final Composite mainControlsContainer = new Composite(parentComposite, SWT.NONE);
//		mainControlsContainer.setBackground(org.eclipse.swt.widgets.Display.getCurrent().getSystemColor(org.eclipse.swt.SWT.COLOR_GREEN));
		mainControlsContainer.setLayout(createRowLayout());

		final Composite imageSelectorContainer = new Composite(mainControlsContainer, SWT.NONE);
//		imageSelectorContainer.setBackground(org.eclipse.swt.widgets.Display.getCurrent().getSystemColor(org.eclipse.swt.SWT.COLOR_DARK_GREEN));
		imageSelectorContainer.setLayout(createTableWrapLayout(2));
		imageSlider = new Slider(imageSelectorContainer, SWT.HORIZONTAL | SWT.WRAP);
		totalSliderImageLabel = new Label(imageSelectorContainer, SWT.WRAP);
//		imageSlider.setBackground(org.eclipse.swt.widgets.Display.getCurrent().getSystemColor(org.eclipse.swt.SWT.COLOR_RED));
//		totalSliderImageLabel.setBackground(org.eclipse.swt.widgets.Display.getCurrent().getSystemColor(org.eclipse.swt.SWT.COLOR_YELLOW));
		TableWrapData td = new TableWrapData(TableWrapData.FILL_GRAB);
		td.valign = TableWrapData.MIDDLE;
		imageSlider.setLayoutData(td);
		td = new TableWrapData(TableWrapData.FILL_GRAB);
		td.valign = TableWrapData.MIDDLE;
		totalSliderImageLabel.setLayoutData(td);

		final Composite imageAmountSelectorContainer = new Composite(mainControlsContainer, SWT.NONE);
		imageAmountSelectorContainer.setLayout(createTableWrapLayout(2));
		imageFilesWindowWidthLabel = new Label(imageAmountSelectorContainer, SWT.WRAP);
		imageFilesWindowWidthSpinner = new Spinner(imageAmountSelectorContainer, SWT.WRAP);
		td = new TableWrapData(TableWrapData.FILL_GRAB);
		td.valign = TableWrapData.MIDDLE;
		imageFilesWindowWidthLabel.setLayoutData(td);
		td = new TableWrapData(TableWrapData.FILL_GRAB);
		td.valign = TableWrapData.MIDDLE;
		imageFilesWindowWidthSpinner.setLayoutData(td);

		if( !controllable.isRemoted() ) {
			autoSelectLatestNewImageButton = new Button(mainControlsContainer, SWT.CHECK | SWT.WRAP);
		} else {
			final Composite remoteControlsContainer = new Composite(mainControlsContainer, SWT.NONE);
			remoteControlsContainer.setLayout(createTableWrapLayout(5));
			autoDisplayRemotedImage = new Button(remoteControlsContainer, SWT.PUSH | SWT.WRAP);
			displayRemotedImageDedicated = new Button(remoteControlsContainer, SWT.PUSH | SWT.WRAP);
			showEachLabel = new Label(remoteControlsContainer, SWT.WRAP);
			showEachRemotedImageSpinner = new Spinner(remoteControlsContainer, SWT.WRAP);
			showRemotedImageLabel = new Label(remoteControlsContainer, SWT.WRAP);
			td = new TableWrapData(TableWrapData.FILL_GRAB);
			td.valign = TableWrapData.MIDDLE;
			autoDisplayRemotedImage.setLayoutData(td);
			td = new TableWrapData(TableWrapData.FILL_GRAB);
			td.valign = TableWrapData.MIDDLE;
			displayRemotedImageDedicated.setLayoutData(td);
			td = new TableWrapData(TableWrapData.FILL_GRAB);
			td.valign = TableWrapData.MIDDLE;
			showEachLabel.setLayoutData(td);
			td = new TableWrapData(TableWrapData.FILL_GRAB);
			td.valign = TableWrapData.MIDDLE;
			showEachRemotedImageSpinner.setLayoutData(td);
			td = new TableWrapData(TableWrapData.FILL_GRAB);
			td.valign = TableWrapData.MIDDLE;
			showRemotedImageLabel.setLayoutData(td);
		}
		phaRadiusUI = new SpinnerSlider(mainControlsContainer, SWT.HORIZONTAL | SWT.WRAP);

		final Composite hklControlsContainer = new Composite(parentComposite, SWT.NONE);
//		hklControlsContainer.setBackground(org.eclipse.swt.widgets.Display.getCurrent().getSystemColor(org.eclipse.swt.SWT.COLOR_DARK_GREEN));
		hklControlsContainer.setLayout(createRowLayout());

		hRangeMinUI = new SpinnerSlider(hklControlsContainer, SWT.HORIZONTAL | SWT.WRAP);
		hRangeMaxUI = new SpinnerSlider(hklControlsContainer, SWT.HORIZONTAL | SWT.WRAP);
		kRangeMinUI = new SpinnerSlider(hklControlsContainer, SWT.HORIZONTAL | SWT.WRAP);
		kRangeMaxUI = new SpinnerSlider(hklControlsContainer, SWT.HORIZONTAL | SWT.WRAP);
		lRangeMinUI = new SpinnerSlider(hklControlsContainer, SWT.HORIZONTAL | SWT.WRAP);
		lRangeMaxUI = new SpinnerSlider(hklControlsContainer, SWT.HORIZONTAL | SWT.WRAP);
	}

	public void initializeImageSelectorUI() {
		imageSlider.setToolTipText("Image selector");
		imageSlider.setThumb(controllable.getImageArrayBatchSize());
		imageSliderSelection = imageSlider.getSelection();
		//Setting monospace font to avoid jumping numbers left-right
		totalSliderImageLabel.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));
		totalSliderImageLabel.setToolTipText("Selected image/Number of images");
		imageSlider.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(final SelectionEvent e) {
				controllable.setBatchIndex(somethingChangeListener, imageSlider.getSelection() - 1);
			}
		});
		imageFilesWindowWidthLabel.setToolTipText("Number of images to sum up");
		imageFilesWindowWidthLabel.setText("Batch Amount");
		imageFilesWindowWidthSpinner.setToolTipText(imageFilesWindowWidthLabel.getToolTipText());
		imageFilesWindowWidthSpinner.addListener(SWT.Verify, new Listener() {
			@Override
			public void handleEvent(final Event e) {
				try {
					if( e.start == 0 && e.end == 0 ) //Swt bug: inserted text event, but we always get 0 offsets
						e.doit = true; //It must be accepted (and controllable will deny), because no way to tell where the text is inserted
					else {
						final String newValue = StringUtils.replaceRange(imageFilesWindowWidthSpinner.getText(), e.text, e.start, e.end );
						e.doit = controllable.isBatchSizeValid( decimalFormat.parse( newValue ).intValue() );
					}
				} catch (final ParseException e1) {
					e.doit = e.text.isEmpty();
				}
			}
		});
		imageFilesWindowWidthSpinner.addListener(SWT.Modify, new Listener() {
			public void handleEvent(final Event e) {
				try {
					controllable.setBatchSize(somethingChangeListener, decimalFormat.parse(imageFilesWindowWidthSpinner.getText()).intValue());
				} catch (final ParseException e1) {
				}
			}
		});
		somethingChangeListener.somethingChange(new SomethingChangeEvent(this, SomethingChangeEvent.IMAGE_ARRAY_SOMETHING));
		if( !controllable.isRemoted() ) {
			autoSelectLatestNewImageButton.setText("Auto latest");
			autoSelectLatestNewImageButton.setToolTipText("Automatically scan directory and display last batch");
			autoSelectLatestNewImageButton.setSelection(controllable.getAutoSelectLatestNewImage());
			autoSelectLatestNewImageButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(final SelectionEvent e) {
					controllable.setAutoSelectLatestNewImage(somethingChangeListener, autoSelectLatestNewImageButton.getSelection());
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
			showEachLabel.setToolTipText("Show each Nth received image");
			showEachLabel.setText("Show each");
			showEachRemotedImageSpinner.setToolTipText(showEachLabel.getToolTipText());
			showEachRemotedImageSpinner.addListener(SWT.Verify, new Listener() {
				@Override
				public void handleEvent(final Event e) {
					try {
						if( e.start == 0 && e.end == 0 ) //Swt bug: inserted text event, but we always get 0 offsets
							e.doit = true; //It must be accepted (and controllable will deny), because no way to tell where the text is inserted
						else {
							final String newValue = StringUtils.replaceRange(imageFilesWindowWidthSpinner.getText(), e.text, e.start, e.end );
							e.doit = controllable.isShowEachNthImageValid( decimalFormat.parse( newValue ).intValue() );
						}
					} catch (final ParseException e1) {
						e.doit = e.text.isEmpty();
					}
				}
			});
			showEachRemotedImageSpinner.addListener(SWT.Modify, new Listener() {
				public void handleEvent(final Event e) {
					try {
						controllable.setShowEachNthImage(somethingChangeListener, decimalFormat.parse(showEachRemotedImageSpinner.getText()).intValue());
					} catch (final ParseException e1) {
					}
				}
			});
			showRemotedImageLabel.setToolTipText(showEachLabel.getToolTipText());
			showRemotedImageLabel.setText("received image");
			somethingChangeListener.somethingChange(new SomethingChangeEvent(this, SomethingChangeEvent.SHOW_EACH_NTH_IMAGE));
		}
		phaRadiusUI.addSelectionListener(phaRadiusSelectionListener);
		phaRadiusUI.setValues(PHA.featureShortName + " Radius", controllable.getPhaRadius(),
				controllable.getPhaRadiusMin(), controllable.getPhaRadiusSup() - 1, 0, 1, 10, 1, 10);
		controllable.setPhaRadius(somethingChangeListener, phaRadiusUI.getSelectionAsInteger());

		//H, K, L controls
		final String hFeatureShortName = "H";
		final String kFeatureShortName = "K";
		final String lFeatureShortName = "L";
		hRangeMinUI.addSelectionListener(hRangeMinSelectionListener);
		//TODO since no HKL file, min=0, sup-1=-1, and slider complains!
		hRangeMinUI.setValues(hFeatureShortName + " min", controllable.getHMin(),
//				controllable.getHMin(), controllable.getHSup() - 1, 0, 1, 10, 1, 10);
				0, 5, 0, 1, 10, 1, 10);
		controllable.setHRangeMin(somethingChangeListener, hRangeMinUI.getSelectionAsInteger());

		hRangeMaxUI.addSelectionListener(hRangeMaxSelectionListener);
		hRangeMaxUI.setValues(hFeatureShortName + " max", controllable.getHSup() - 1,
//				controllable.getHMin(), controllable.getHSup() - 1, 0, 1, 10, 1, 10);
				-5, 5, 0, 1, 10, 1, 10);
		controllable.setHRangeMax(somethingChangeListener, hRangeMaxUI.getSelectionAsInteger());

		kRangeMinUI.addSelectionListener(kRangeMinSelectionListener);
		kRangeMinUI.setValues(kFeatureShortName + " min", controllable.getKMin(),
//				controllable.getKMin(), controllable.getKSup() - 1, 0, 1, 10, 1, 10);
				0, 25, 0, 1, 10, 1, 10);
		controllable.setKRangeMin(somethingChangeListener, kRangeMinUI.getSelectionAsInteger());

		kRangeMaxUI.addSelectionListener(kRangeMaxSelectionListener);
		kRangeMaxUI.setValues(kFeatureShortName + " max", controllable.getKSup() - 1,
//				controllable.getKMin(), controllable.getKSup() - 1, 0, 1, 10, 1, 10);
				-15, 25, 0, 1, 10, 1, 10);
		controllable.setKRangeMax(somethingChangeListener, kRangeMaxUI.getSelectionAsInteger());

		lRangeMinUI.addSelectionListener(lRangeMinSelectionListener);
		lRangeMinUI.setValues(lFeatureShortName + " min", controllable.getLMin(),
//				controllable.getLMin(), controllable.getLSup() - 1, 0, 1, 10, 1, 10);
				0, 155, 0, 1, 10, 1, 10);
		controllable.setLRangeMin(somethingChangeListener, lRangeMinUI.getSelectionAsInteger());

		lRangeMaxUI.addSelectionListener(lRangeMinSelectionListener);
		lRangeMaxUI.setValues(lFeatureShortName + " max", controllable.getLSup() - 1,
//				controllable.getLMin(), controllable.getLSup() - 1, 0, 1, 10, 1, 10);
				-255, 155, 0, 1, 10, 1, 10);
		controllable.setLRangeMax(somethingChangeListener, lRangeMaxUI.getSelectionAsInteger());

//		final Composite exec =new Composite(hklControls, SWT.NONE);
//		exec.setLayout(new GridLayout(1, false));
//		hklEnabledButton = new Button(exec, SWT.CHECK);
//		hklEnabledButton.setText("Display HKL values");
//		hklEnabledButton.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(final SelectionEvent e) {
//				//TODO
//	//			controllable.setAutoSelectLatestNewImage(DViewerController.this, autoSelectLatestNewImageButton.getSelection());
//			}
//		});
		controllable.addSomethingListener(somethingChangeListener);
	}

}
