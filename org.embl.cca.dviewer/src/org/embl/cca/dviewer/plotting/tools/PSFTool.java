package org.embl.cca.dviewer.plotting.tools;

import java.util.Collection;

import org.dawnsci.plotting.api.tool.AbstractToolPage;
import org.dawnsci.plotting.api.trace.IImageTrace;
import org.dawnsci.plotting.api.trace.ITrace;
import org.dawnsci.plotting.api.trace.ITraceListener;
import org.dawnsci.plotting.api.trace.TraceEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.embl.cca.dviewer.Activator;
import org.embl.cca.dviewer.ui.editors.preference.EditorConstants;
import org.embl.cca.dviewer.ui.editors.preference.EditorPreferenceHelper;
import org.embl.cca.dviewer.ui.editors.utils.PSF;
import org.embl.cca.utils.threading.CommonThreading;
import org.embl.cca.utils.threading.ExecutableManager;
import org.embl.cca.utils.threading.TrackableJob;
import org.embl.cca.utils.ui.widget.SpinnerSlider;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;

/**
 * The PSFTool is a tool extension for DAWN, it can highlight spots thus humans can see them better.
 * 
 * @author Gábor Náray
 * 
*/
public class PSFTool extends AbstractToolPage {

	Group composite;
	Button applyPSFUI;
	Composite buttons;
	SpinnerSlider psfRadiusUI;
	Button saveSettingsUI;
	Button resetSettingsUI;

	protected PSF psf;
	protected int psfRadiusSelected;
	protected double psfMinValueSelected;
	protected boolean psfStateSelected;

	IImageTrace image;
	ITraceListener traceListener;
	ExecutableManager psfRadiusManager = null;
	boolean imageUpdating = false;

	/**
	 * The objects which contain the image.
	 */
	AbstractDataset originalSet, psfSet; 

	public PSFTool() {
		psf = new PSF();
		traceListener = new ITraceListener.Stub() {
//			@Override
//			public void tracesAltered(TraceEvent evt) {
//				update(evt);
//			}
//
//			@Override
//			public void tracesCleared(TraceEvent evt) {
//				update(evt);
//			}
//
//			@Override
//			public void tracesPlotted(TraceEvent evt) {
//				update(evt);
//			}
//
//			@Override
//			public void traceCreated(TraceEvent evt) {
//				update(evt);
//			}
//
//			@Override
//			public void traceUpdated(TraceEvent evt) {
//				update(evt);
//			}
//
//			@Override
//			public void traceAdded(TraceEvent evt) {
//				if (evt.getSource() instanceof IImageTrace) {
//					originalSet = null; 
//					applyPSF(); //TODO
//				}
//				update(evt);
//			}
//
//			@Override
//			public void traceRemoved(TraceEvent evt) {
//				update(evt);
//			}
//			
			@Override
			protected void update(TraceEvent evt) {
				if (evt.getSource() instanceof IImageTrace) {
					System.out.println("PSFTool: ImageTrace updated!!!");
					if( !imageUpdating ) //Checking to avoid circular PSF applying
						imageUpdated( (IImageTrace)evt.getSource() );
				}
			}
		};
	}

	@Override
	public ToolPageRole getToolPageRole() {
		return ToolPageRole.ROLE_2D;
	}

	@Override
	public void createControl(Composite parent) {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();

		composite = new Group(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		final IImageTrace image = getImageTrace();
		if (image!=null) {
			composite.setText(PSF.featureName + "ing '"+image.getName()+"'");
		} else {
			composite.setText(PSF.featureName + "ing ");
		}

		applyPSFUI = new Button(composite, SWT.TOGGLE );
		applyPSFUI.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 2, 1));
		applyPSFUI.setText("Apply " + PSF.featureName);
		applyPSFUI.setToolTipText("Apply " + PSF.featureFullName + " (" + PSF.featureName + ") on the image");
		applyPSFUI.setImage(Activator.getImage("/icons/psf.png"));
		applyPSFUI.setSelection((Boolean)EditorPreferenceHelper.getStoreValue(preferenceStore, EditorConstants.PREFERENCE_APPLY_PSF));
		applyPSFUI.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				psfStateSelected();
	    	}
        });

		buttons = new Composite(composite, SWT.NONE);
		buttons.setLayout(new GridLayout(2, false));
		buttons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		psfRadiusUI = new SpinnerSlider( buttons, SWT.None );
		psfRadiusUI.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 2, 1));
		psfRadiusUI.setToolTipText(PSF.featureName + " Radius");
		psfRadiusUI.setValues(PSF.featureName + " Radius", (Integer)EditorPreferenceHelper.getStoreValue(preferenceStore, EditorConstants.PREFERENCE_PSF_RADIUS),
				1, 100, 0, 1, 10, 1, 10);
		psfRadiusUI.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if(applyPSFUI.getSelection())
					psfRadiusSelected();
	    	}
        });

        saveSettingsUI = new Button(buttons, SWT.PUSH);
		saveSettingsUI.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		saveSettingsUI.setText("Save settings");
		saveSettingsUI.setToolTipText("Save " + PSF.featureFullName + " (" + PSF.featureName + ") settings");
		saveSettingsUI.setImage(Activator.getImage("icons/apply.gif"));
		saveSettingsUI.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				savePSFSettings();
			}
		});
		saveSettingsUI.setEnabled(true);
		
        resetSettingsUI = new Button(buttons, SWT.PUSH);
        resetSettingsUI.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
        resetSettingsUI.setText("Reset settings");
        resetSettingsUI.setToolTipText("Reset " + PSF.featureFullName + " (" + PSF.featureName + ") settings");
        resetSettingsUI.setImage(Activator.getImage("icons/reset.gif"));
        resetSettingsUI.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				resetPSFSettings();
			}
		});
        resetSettingsUI.setEnabled(true);

        psfRadiusSelected();
		psfStateSelected();
//        traceListener.traceUpdated(new TraceEvent(image)); //Emulating the updating of trace
	}

	protected void savePSFSettings() {
		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		EditorPreferenceHelper.setStoreValue(preferenceStore, EditorConstants.PREFERENCE_APPLY_PSF, applyPSFUI.getSelection());
		EditorPreferenceHelper.setStoreValue(preferenceStore, EditorConstants.PREFERENCE_PSF_RADIUS, psfRadiusUI.getSelection());
	}

	protected void resetPSFSettings() {
//		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
		int oldPSFRadius = psfRadiusUI.getSelectionAsInteger();
//		EditorPreferenceHelper.setStoreValueByDefault( preferenceStore, EditorConstants.PREFERENCE_PSF_RADIUS );
		psfRadiusUI.setSelection((Integer)EditorPreferenceHelper.getDefaultValue(EditorConstants.PREFERENCE_PSF_RADIUS));
		if( oldPSFRadius != psfRadiusUI.getSelectionAsInteger() )
			psfRadiusSelected();
		boolean oldApplyPSF = applyPSFUI.getSelection();
//		EditorPreferenceHelper.setStoreValueByDefault( preferenceStore, EditorConstants.PREFERENCE_APPLY_PSF );
		applyPSFUI.setSelection((Boolean)EditorPreferenceHelper.getDefaultValue(EditorConstants.PREFERENCE_APPLY_PSF));
		if( oldApplyPSF != applyPSFUI.getSelection() )
			psfStateSelected();
	}

	/**
	 * this method gives access to the image trace plotted in the
	 * main plotter or null if one is not plotted.
	 * @return
	 */
	public IImageTrace getImageTrace() {
		try {
			final Collection<ITrace> traces = getPlottingSystem().getTraces(IImageTrace.class);
			if (traces==null || traces.size()<=0) return null;
			final ITrace trace = traces.iterator().next();
			System.out.println("plotSys=" + getPlottingSystem().toString() + ", trace=" + trace.toString() );
			return trace instanceof IImageTrace ? (IImageTrace)trace : null;
		} catch (Exception ne) {
			return null;
		}
	}

	@Override
	public Control getControl() {
		return composite;
	}

	@Override
	public void setFocus() {
		if(composite != null)
			composite.setFocus();
	}

	@Override
	public void activate() { //This is called before createControl, thus can not do much here
		super.activate();
		if (getPlottingSystem()!=null)
			getPlottingSystem().addTraceListener(traceListener); // If it changes get reference to image.
	}
	
	@Override
	public void deactivate() {
		if (getPlottingSystem()!=null)
			getPlottingSystem().removeTraceListener(traceListener);
		super.deactivate();
	}
	
	@Override
	public void dispose() {
		if( isDisposed() )
			return;
		//Here should dispose created instances of descendants of org.eclipse.swt.graphics.Resource (has dispose), org.eclipse.swt.graphics.Device (has dispose)
		//With other words, here should dispose the created objects that are not widgets, but have dispose method, that is objects without parent.
		super.dispose();
	}

	protected void imageUpdated( IImageTrace image ) {
		synchronized (psf) {
			originalSet = null;
			final double min = image.getMin().doubleValue();
			final double max = image.getMax().doubleValue(); //In real, max() is a (weird) mean
			final double realMax = image.getData().max().doubleValue();
			final double realMean = ((Number)image.getData().mean()).doubleValue();
			final double ourMean = Math.min(Math.ceil(realMean * 6), realMax); //6 is an experimental number, should find out by algorithm
			System.out.println( "createPlot min=" + min + ", max=" + max + ", realMax=" + realMax + ", realMean=" + realMean + ", ourMean=" + ourMean);
			updatePSFMinValue(((Number)image.getMax()).doubleValue()); //FIXME Will be ourMean
			//updateImage(image); //Instead of updateImage, we call updatePSFMinValue, which will call updateImage if necessary 
		}
	}

	public int getPSFRadiusSelected() {
		return psfRadiusSelected;
	}

	public boolean getPsfStateSelected() {
		return psfStateSelected;
	}

	protected void psfRadiusSelected() {
		synchronized (psf) {
			if (psfRadiusUI == null || psfRadiusUI.isDisposed())
				return;
			updatePSFRadius(psfRadiusUI.getSelectionAsInteger());
		}
	}
	public void updatePSFRadius(final int psfRadius) {
		if( psfRadiusSelected == psfRadius )
			return;
		psfRadiusSelected = psfRadius;
		psfSet = null;
		if( psfStateSelected )
			updateImage(getImageTrace());
	}

	protected void psfStateSelected() {
		synchronized (psf) {
			if (applyPSFUI == null || applyPSFUI.isDisposed())
				return;
			buttons.setVisible(applyPSFUI.getSelection());
			updatePSFState(applyPSFUI.getSelection());
		}
	}

	public void updatePSFState(final boolean psfState) {
		if( psfStateSelected == psfState )
			return;
		psfStateSelected = psfState;
		updateImage(getImageTrace());
	}

	public void updatePSFMinValue(final double psfMinValue) {
		if( psfMinValueSelected == psfMinValue )
			return;
		psfMinValueSelected = psfMinValue;
		psfSet = null;
		if( psfStateSelected )
			updateImage(getImageTrace());
	}

	protected void updateImage(final IImageTrace image) {
		if (image == null)
			return;

		synchronized (psf) {
			final TrackableJob job = new TrackableJob(psfRadiusManager, "Apply " + PSF.featureName) {
				AbstractDataset originalSetJob = originalSet;
				AbstractDataset psfSetJob = psfSet;
				PSF psfJob = psf;
				final IImageTrace imageJob = image;
				final int psfRadiusJob = psfRadiusSelected;
				final double psfMinValueJob = psfMinValueSelected;
				final boolean applyPSFJob = psfStateSelected;
				public IStatus runThis(IProgressMonitor monitor) {
					IStatus result = Status.CANCEL_STATUS;
					do {
						if (isAborting())
							break;
						if( originalSetJob == null ) {
							originalSetJob = imageJob.getData();
							psfSetJob = null;
						}
						long t0 = System.nanoTime();
						if( psfSetJob == null ) {
							psfSetJob = originalSetJob.synchronizedCopy();
							long t1 = System.nanoTime();
							System.out.println( "DEBUG: Copying data image took [msec]= " + ( t1 - t0 ) / 1000000 );
							psfJob.setRadius(psfRadiusJob);
							psfJob.setMinValue(psfMinValueJob);
							psfJob.applyPSF(psfSetJob, new Rectangle(0, 0, originalSetJob.getShape()[1], originalSetJob.getShape()[0]));
						}
						if (isAborting())
							break;
						Runnable run;
						synchronized (psf) {
							originalSet = originalSetJob;
							psfSet = psfSetJob;
							psf = psfJob;
							run = new Runnable() {
								public void run() {
									if( !isAborting() ) {
										imageUpdating = true;
										imageJob.setData(!applyPSFJob ? originalSet : psfSet, image.getAxes(), false);
										imageUpdating = false;
									}
								}
							};
						}
						//Must sync call outside of psf lock, else it can cause deadlock with another sync call to this method
						CommonThreading.execFromUIThreadNowOrSynced(run);
						result = Status.OK_STATUS;
					} while (false);
					if (isAborting()) {
						setAborted();
						return Status.CANCEL_STATUS;
					}
					return result;
				}
			};
			job.setUser(false);
			job.setPriority(Job.BUILD);
			psfRadiusManager = ExecutableManager.setRequest(job);
		}
	}
}
