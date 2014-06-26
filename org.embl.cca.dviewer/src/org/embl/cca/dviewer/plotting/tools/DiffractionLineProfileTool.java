package org.embl.cca.dviewer.plotting.tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import javax.vecmath.Vector3d;

import org.dawb.common.ui.monitor.ProgressMonitorWrapper;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.annotation.AnnotationUtils;
import org.dawnsci.plotting.api.annotation.IAnnotation;
import org.dawnsci.plotting.api.preferences.BasePlottingConstants;
import org.dawnsci.plotting.api.preferences.ToolbarConfigurationConstants;
import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.region.IRegion.RegionType;
import org.dawnsci.plotting.api.region.RegionUtils;
import org.dawnsci.plotting.api.trace.IImageTrace;
import org.dawnsci.plotting.api.trace.ILineTrace;
import org.dawnsci.plotting.api.trace.ITrace;
import org.dawnsci.plotting.api.trace.TraceUtils;
import org.dawnsci.plotting.tools.fitting.FittedFunction;
import org.dawnsci.plotting.tools.fitting.FittedFunctions;
import org.dawnsci.plotting.tools.fitting.FittedPeaksInfo;
import org.dawnsci.plotting.tools.fitting.FittingUtils;
import org.dawnsci.plotting.tools.profile.ProfileTool;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.IPageSite;
import org.embl.cca.dviewer.DViewerActivator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
import uk.ac.diamond.scisoft.analysis.roi.IROI;
import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
import uk.ac.diamond.scisoft.analysis.roi.ROIProfile;
import uk.ac.diamond.scisoft.analysis.roi.RectangularROI;

public class DiffractionLineProfileTool extends ProfileTool {

	private Composite composite;
	private static final Logger logger = LoggerFactory.getLogger(DiffractionLineProfileTool.class);
	
	Vector<FittedFunctions> AllFittedPeaks=new Vector<FittedFunctions>();
//	private FittedPeaks   fittedPeaks;
	private FittingJob    fittingJob;

	@Override
	public void createControl(Composite parent) {


		this.fittingJob = new FittingJob();
		
		
		this.composite = new Composite(parent, SWT.NONE);
		
		
		final IPageSite site = getSite();
		
		final Action refit = new Action("Fit the Traces.", DViewerActivator.getImageDescriptor("icons/plot-tool-peak-fit.png")) {
			public void run() {
				fitTraces();
			}
		};
		site.getActionBars().getToolBarManager().add(refit);
		final Action delfit = new Action("Delete Fit Results.", DViewerActivator.getImageDescriptor("icons/plot-tool-peak-fit-clear.png")) {
			public void run() {
				deleteFitTraces();
			}
		};
		site.getActionBars().getToolBarManager().add(delfit);
		site.getActionBars().getToolBarManager().add(new Separator());
		
		super.createControl(parent);
		
		site.getActionBars().getToolBarManager().remove(BasePlottingConstants.REMOVE_REGION);
		site.getActionBars().getToolBarManager().remove("org.csstudio.swt.xygraph.undo.ZoomType.HORIZONTAL_ZOOM");
		site.getActionBars().getToolBarManager().remove("org.csstudio.swt.xygraph.undo.ZoomType.ZOOM_OUT");
		site.getActionBars().getToolBarManager().remove("org.csstudio.swt.xygraph.undo.ZoomType.RUBBERBAND_ZOOM");
		site.getActionBars().getToolBarManager().remove(ToolbarConfigurationConstants.TOOL1D.getId());
		//site.getActionBars().getToolBarManager().remove("org.csstudio.swt.xygraph.autoscale");
		site.getActionBars().getToolBarManager().remove("org.csstudio.swt.xygraph.undo.ZoomType.VERTICAL_ZOOM");
		site.getActionBars().getToolBarManager().remove("org.csstudio.swt.xygraph.undo.ZoomType.ZOOM_IN");
		site.getActionBars().getToolBarManager().remove("org.csstudio.swt.xygraph.undo.ZoomType.NONE");
		site.getActionBars().getToolBarManager().remove("org.csstudio.swt.xygraph.undo.ZoomType.PANNING");
		//site.getActionBars().getToolBarManager().remove("Create new profile.");
		
		

	
	}

	
	
	private final class FittingJob extends Job {
		//IPlottingSystem plottingSystem;
		public FittingJob() {
			super("Fit peaks");
			setPriority(Job.INTERACTIVE);
			//this.plottingSystem=iPlottingSystem;
		}
		

		@Override
		protected IStatus run(IProgressMonitor monitor) {

			
			if (composite==null)        return Status.CANCEL_STATUS;
			if (composite.isDisposed()) return Status.CANCEL_STATUS;

			//getPlottingSystem().removeRegionListener(LineProfileTool.this);
			List<ILineTrace> selectedTraces=new ArrayList<ILineTrace>(31);
			for (ITrace tr : profilePlottingSystem.getTraces()) {
				if (tr instanceof ILineTrace)
					selectedTraces.add((ILineTrace)tr);
			}
			if (selectedTraces.isEmpty())    return Status.CANCEL_STATUS;


			for (ILineTrace selectedTrace : selectedTraces) {
				
				// We peak fit only the first of the data sets plotted for now.
				AbstractDataset x  = (AbstractDataset)selectedTrace.getXData();
				AbstractDataset y  = (AbstractDataset)selectedTrace.getYData();
	
				try {
					final FittedFunctions bean = FittingUtils.getFittedPeaks(new FittedPeaksInfo(x, y, new ProgressMonitorWrapper(monitor), getPlottingSystem(), selectedTrace,10));
					createFittedPeaks(bean);
					
				} catch (Exception ne) {
					logger.error("Cannot fit peaks!", ne);
					return Status.CANCEL_STATUS;
				}
				
				
			}
			return Status.OK_STATUS;
		}


		public void fit() {
			cancel();
			schedule();
		}
	};
	

	
	/**
	 * Thread safe
	 * @param peaks
	 */
	protected synchronized void createFittedPeaks(final FittedFunctions newBean) {
		
		if (newBean==null) {
			logger.error("Cannot find peaks in the given selection.");
			return;
		}
		composite.getDisplay().syncExec(new Runnable() {
			
		    public void run() {
		    	try {
		    		
		    		
		    		boolean requireFWHMSelections = false;//Activator.getDefault().getPreferenceStore().getBoolean(FittingConstants.SHOW_FWHM_SELECTIONS);
		    		boolean requirePeakSelections = true;//Activator.getDefault().getPreferenceStore().getBoolean(FittingConstants.SHOW_PEAK_SELECTIONS);
		    		boolean requireTrace = false;//Activator.getDefault().getPreferenceStore().getBoolean(FittingConstants.SHOW_FITTING_TRACE);
		    		boolean requireAnnot = false;//Activator.getDefault().getPreferenceStore().getBoolean(FittingConstants.SHOW_ANNOTATION_AT_PEAK);


					Vector3d annData=newBean.calcReflectionDistance();
					final IAnnotation ann1;
					try {
						ann1 = AnnotationUtils.replaceCreateAnnotation(profilePlottingSystem, "Distance: "+String.format("%.1f", annData.z)+"\u00c5");
						ann1.setLocation(annData.x,annData.y);
						ann1.setAnnotationColor(((ILineTrace)(newBean.getFunctionList().get(0).getDataTrace())).getTraceColor());
						profilePlottingSystem.addAnnotation(ann1);
						newBean.setDistAnnotation(ann1);
						if (!requirePeakSelections) ann1.setVisible(false);
					} catch (Exception e) {
					}
		    		
		    		int ipeak = 1;
					// Draw the regions
					for (FittedFunction fp : newBean.getFunctionList()) {
						
						if (fp.isSaved()) continue;
						
						RectangularROI rb = fp.getRoi();
						final IRegion area = RegionUtils.replaceCreateRegion(profilePlottingSystem, "Peak Area "+ipeak, RegionType.XAXIS);
						area.setRegionColor(ColorConstants.orange);
						area.setROI(rb);
						area.setMobile(false);
						profilePlottingSystem.addRegion(area);
						fp.setFwhm(area);
						if (!requireFWHMSelections) area.setVisible(false);
												
						final AbstractDataset[] pair = fp.getPeakFunctions();
						final ILineTrace trace = TraceUtils.replaceCreateLineTrace(profilePlottingSystem, "Peak "+ipeak);
						trace.setData(pair[0], pair[1]);
						trace.setLineWidth(1);
						trace.setTraceColor(ColorConstants.black);
						trace.setUserTrace(false);
						profilePlottingSystem.addTrace(trace);
						fp.setTrace(trace);
						if (!requireTrace) trace.setVisible(false);

						final IAnnotation ann = AnnotationUtils.replaceCreateAnnotation(profilePlottingSystem, "Peak "+ipeak);
                    	ann.setLocation(fp.getPosition(), fp.getPeakValue());                  	
                    	profilePlottingSystem.addAnnotation(ann);                   	
                    	fp.setAnnotation(ann);
                    	if (!requireAnnot) ann.setVisible(false);
                    	
						final IRegion line = RegionUtils.replaceCreateRegion(profilePlottingSystem, "Peak Line "+ipeak, RegionType.XAXIS_LINE);
						line.setRegionColor(ColorConstants.black);
						line.setAlpha(150);
						line.setLineWidth(1);
						profilePlottingSystem.addRegion(line);
						line.setROI(new LinearROI(rb.getMidPoint(), rb.getMidPoint()));
						line.setMobile(false);
						fp.setCenter(line);
						if (!requirePeakSelections) line.setVisible(false);


					    ++ipeak;
					}
				
					
					DiffractionLineProfileTool.this.AllFittedPeaks.add(newBean);
					//viewer.setInput(newBean);
                    //viewer.refresh();
                    
                    //algorithmMessage.setText(getAlgorithmSummary());
                    //algorithmMessage.getParent().layout();
                    //updatePlotServerConnection(newBean);
                    
		    	} catch (Exception ne) {
		    		logger.error("Cannot create fitted peaks!", ne);
		    	}
		    } 
		});
	}
	
	
	
	public void fitTraces() {
		
		fittingJob.fit();
		
	}
	
	public void deleteFitTraces() {
		
		for (int ct=AllFittedPeaks.size()-1;ct>=0;ct--) {
			if (AllFittedPeaks.elementAt(ct) instanceof FittedFunctions) {
				FittedFunctions fp = (FittedFunctions)AllFittedPeaks.elementAt(ct);
				AllFittedPeaks.remove(ct);
				fp.setPeaksVisible(false);
				fp.removeSelections(profilePlottingSystem, true);
				fp.dispose();
				fp = null;
		}
	}
		
	}
	@Override
	protected void configurePlottingSystem(IPlottingSystem plotter) {
		plotter.getSelectedXAxis().setTitle("Pixel");
		plotter.getSelectedYAxis().setTitle("Intensity");
	}

	@Override
	protected void createProfile(	IImageTrace  image, 
						            IRegion      region, 
						            IROI         rbs, 
						            boolean      tryUpdate,
				                    boolean      isDrag,
						            IProgressMonitor monitor) {
        
		if (monitor.isCanceled()) return;
		if (image==null) return;
		
		if (!isRegionTypeSupported(region.getRegionType())) return;

		final LinearROI bounds = (LinearROI) (rbs==null ? region.getROI() : rbs);
		if (bounds==null) return;
		if (!region.isVisible()) return;

		if (monitor.isCanceled()) return;
		AbstractDataset[] profileData = ROIProfile.line((AbstractDataset)image.getData(), (AbstractDataset)image.getMask(), bounds, 1d, true);
        if (profileData==null) return;

		if (monitor.isCanceled()) return;
		
		final AbstractDataset intensity = profileData[0];
		intensity.setName(region.getName());
		final AbstractDataset indices = IntegerDataset.createRange(0, intensity.getSize(), 1d);
		indices.setName("Pixel");
		
		final ILineTrace trace = (ILineTrace)profilePlottingSystem.getTrace(region.getName());
		if (tryUpdate && trace!=null) {
			if (trace!=null && !monitor.isCanceled()) getControl().getDisplay().syncExec(new Runnable() {
				public void run() {
					trace.setData(indices, intensity);
				}
			});
			
		} else {
			if (monitor.isCanceled()) return;
			Collection<ITrace> plotted = profilePlottingSystem.updatePlot1D(indices, Arrays.asList(new IDataset[]{intensity}), monitor);
			registerTraces(region, plotted);
			
		}
		
	}

	@Override
	protected boolean isRegionTypeSupported(RegionType type) {
		return type==RegionType.LINE;
	}

	@Override
	protected RegionType getCreateRegionType() {
		return RegionType.LINE;
	}

	
	@Override
	public DataReductionInfo export(DataReductionSlice slice) throws Exception {
		
		final IImageTrace   image   = getImageTrace();
		final Collection<IRegion> regions = getPlottingSystem().getRegions();
		
		for (IRegion region : regions) {
			if (!isRegionTypeSupported(region.getRegionType())) continue;
			if (!region.isVisible())    continue;
			if (!region.isUserRegion()) continue;
			
			AbstractDataset[] profileData = ROIProfile.line((AbstractDataset)slice.getData(), (AbstractDataset)image.getMask(), (LinearROI)region.getROI(), 1d, false);
			final AbstractDataset intensity = profileData[0];
			intensity.setName(region.getName().replace(' ', '_'));
			slice.appendData(intensity);
		}
        return new DataReductionInfo(Status.OK_STATUS);
	}
}
