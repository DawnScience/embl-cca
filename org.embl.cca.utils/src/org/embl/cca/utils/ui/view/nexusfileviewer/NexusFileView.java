package org.embl.cca.utils.ui.view.nexusfileviewer;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;
import org.embl.cca.utils.Activator;
import org.embl.cca.utils.ui.widget.SpinnerSlider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The NexusFileView is a view extension for DAWN, it can display peaks from nexus file.
 * 
 * @author Gábor Náray
 * 
*/
public class NexusFileView extends ViewPart {
	private static final Logger logger = LoggerFactory
			.getLogger(NexusFileView.class);

	Group composite;
	Group displayWhatGroup;
	Button displayWhatUI[];
	Group displayByWhatGroup;
	Button displayByWhatUI[];
	SpinnerSlider displayByHUI, displayByKUI, displayByLUI;
	Label displayByResolutionRangeUI;
	SpinnerSlider displayByResolutionRangeStartUI, displayByResolutionRangeEndUI;

	public NexusFileView() {
		super();
	}

	public void createPartControl(final Composite parent) {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		parent.setLayout(new GridLayout(1, false));

//		final Composite top = new Composite(parent, SWT.NONE);
//		top.setLayout(new GridLayout(2, false));
//		top.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
//
//		composite = new Group(parent, SWT.NONE);
//		composite.setLayout(new GridLayout(1, false));
//		composite.setText("NeXus file viewer");

		displayWhatUI = new Button[4];
		displayWhatGroup = new Group(parent, SWT.NONE);
		displayWhatGroup.setLayout(new GridLayout(displayWhatUI.length, false));
		displayWhatGroup.setText("Display");
		displayWhatUI[0] = new Button(displayWhatGroup, SWT.RADIO);
		displayWhatUI[0].setText("background data");
		displayWhatUI[0].setSelection(true);
		displayWhatUI[1] = new Button(displayWhatGroup, SWT.RADIO );
		displayWhatUI[1].setText("peak data");
		displayWhatUI[1].setSelection(false);
		displayWhatUI[2] = new Button(displayWhatGroup, SWT.RADIO );
		displayWhatUI[2].setText("both merged");
		displayWhatUI[2].setSelection(false);
		displayWhatUI[3] = new Button(displayWhatGroup, SWT.RADIO );
		displayWhatUI[3].setText("both separated");
		displayWhatUI[3].setSelection(false);
		
//		displayWhatUI.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
//		displayWhatUI.setText("Apply " + PSF.featureName);
//		displayWhatUI.setToolTipText("Apply " + PSF.featureFullName + " (" + PSF.featureName + ") on the image");
//		displayWhatUI.setImage(Activator.getImage("/icons/psf.png"));
//		displayWhatUI.setSelection((Boolean)EditorPreferenceHelper.getStoreValue(preferenceStore, EditorConstants.PREFERENCE_APPLY_PSF));
//		displayWhatUI.addSelectionListener(new SelectionAdapter() {
//			public void widgetSelected(SelectionEvent e) {
////				psfStateSelected();
//	    	}
//        });
		boolean displayByHKLValues = true; //TODO disabling SpinnerSlider does not display it is disabled
		displayByWhatUI = new Button[2];
		displayByWhatGroup = new Group(parent, SWT.NONE);
		displayByWhatGroup.setLayout(new GridLayout(2, false));
		displayByWhatGroup.setText("Reflection selection");
		displayByWhatUI[0] = new Button(displayByWhatGroup, SWT.RADIO);
		displayByWhatUI[0].setText("HKL values");
		displayByWhatUI[0].setSelection(displayByHKLValues);
		displayByWhatUI[1] = new Button(displayByWhatGroup, SWT.RADIO );
		displayByWhatUI[1].setText("Resolution range");
		displayByWhatUI[1].setSelection(!displayByHKLValues);
		displayByHUI = new SpinnerSlider(displayByWhatGroup, SWT.HORIZONTAL);
		displayByHUI.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
		displayByHUI.setValues("H", 0, 1, 21, 0, 1, 10, 1, 10, -10, 10, true);
		//selection, minimum, maximum, preferredDigits, sliderIncrement, sliderPageIncrement, spinnerIncrement, spinnerPageIncrement
		displayByHUI.setEnabled(displayByHKLValues);
		displayByResolutionRangeStartUI = new SpinnerSlider(displayByWhatGroup, SWT.HORIZONTAL);
		displayByResolutionRangeStartUI.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
		displayByResolutionRangeStartUI.setValues("High", 0, 1, 100, 3, 1, 10, 1, 10, 0, 100, true);
//		displayByResolutionRangeStartUI.setEnabled(!displayByHKLValues);
		displayByKUI = new SpinnerSlider(displayByWhatGroup, SWT.HORIZONTAL);
		displayByKUI.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
		displayByKUI.setValues("K", 0, 1, 21, 0, 1, 10, 1, 10, -10, 10, true);
		displayByKUI.setEnabled(displayByHKLValues);
		displayByResolutionRangeEndUI = new SpinnerSlider(displayByWhatGroup, SWT.HORIZONTAL);
		displayByResolutionRangeEndUI.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
		displayByResolutionRangeEndUI.setValues("Low", 0, 1, 100, 3, 1, 10, 1, 10, 0, 100, true);
//		displayByResolutionRangeEndUI.setEnabled(!displayByHKLValues);
		displayByLUI = new SpinnerSlider(displayByWhatGroup, SWT.HORIZONTAL);
		displayByLUI.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
		displayByLUI.setValues("L", 0, 1, 21, 0, 1, 10, 1, 10, -10, 10, true);
		displayByLUI.setEnabled(displayByHKLValues);

//		buttons = new Composite(composite, SWT.NONE);
//		buttons.setLayout(new GridLayout(2, false));
//		buttons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
//
//		psfRadiusUI = new SpinnerSlider( buttons, SWT.None );
//		psfRadiusUI.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 2, 1));
//		psfRadiusUI.setToolTipText(PSF.featureName + " Radius");
//		psfRadiusUI.setValues(PSF.featureName + " Radius", (Integer)EditorPreferenceHelper.getStoreValue(preferenceStore, EditorConstants.PREFERENCE_PSF_RADIUS),
//				1, 100, 0, 1, 10, 1, 10);
//		psfRadiusUI.addSelectionListener(new SelectionAdapter() {
//			public void widgetSelected(SelectionEvent e) {
//				if(displayWhatUI.getSelection()) {
////					psfRadiusSelected();
//				}
//	    	}
//        });

//        saveSettingsUI = new Button(buttons, SWT.PUSH);
//		saveSettingsUI.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
//		saveSettingsUI.setText("Save settings");
//		saveSettingsUI.setToolTipText("Save " + PSF.featureFullName + " (" + PSF.featureName + ") settings");
//		saveSettingsUI.setImage(Activator.getImage("icons/apply.gif"));
//		saveSettingsUI.addSelectionListener(new SelectionAdapter() {
//			public void widgetSelected(SelectionEvent e) {
////				savePSFSettings();
//			}
//		});
//		saveSettingsUI.setEnabled(true);
//		
//        resetSettingsUI = new Button(buttons, SWT.PUSH);
//        resetSettingsUI.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
//        resetSettingsUI.setText("Reset settings");
//        resetSettingsUI.setToolTipText("Reset " + PSF.featureFullName + " (" + PSF.featureName + ") settings");
//        resetSettingsUI.setImage(Activator.getImage("icons/reset.gif"));
//        resetSettingsUI.addSelectionListener(new SelectionAdapter() {
//			public void widgetSelected(SelectionEvent e) {
////				resetPSFSettings();
//			}
//		});
//        resetSettingsUI.setEnabled(true);

//        psfRadiusSelected();
//		psfStateSelected();
//        traceListener.traceUpdated(new TraceEvent(image)); //Emulating the updating of trace
	}

	@Override
	public void setFocus() {
		if(composite != null)
			composite.setFocus();
	}

	@Override
	public void dispose() {
//		if( isDisposed() )
//			return;
		//Here should dispose created instances of descendants of org.eclipse.swt.graphics.Resource (has dispose), org.eclipse.swt.graphics.Device (has dispose)
		//With other words, here should dispose the created objects that are not widgets, but have dispose method, that is objects without parent.
		super.dispose();
	}

}
