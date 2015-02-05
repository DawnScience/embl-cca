package org.embl.cca.dviewer.actions;

import org.dawb.common.ui.util.EclipseUtils;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.embl.cca.dviewer.DViewerActivator;
import org.embl.cca.dviewer.ui.editors.preference.DViewerEditorConstants;
import org.embl.cca.dviewer.ui.editors.utils.PHA;

public class PHAJob extends Job {

	private IDataset orig;
	private IDataset trans;
	private ExecutionEvent event;

	public PHAJob(String name) {
		super(name);
		setPriority(Job.INTERACTIVE);
		setUser(true);
		setSystem(false);
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {
		
		final IImageTrace image = getImage(event);	
		if (image==null) return Status.CANCEL_STATUS;
		
		final IDataset    trans = getToggledData(image, monitor);
		
		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				image.setData(trans, image.getAxes(), false);
			}
		});
	
		return Status.OK_STATUS;
	}

	private IDataset getToggledData(IImageTrace image, IProgressMonitor monitor) {
		
		if (image.getData()==trans) {
			return orig;
		} else {
			this.orig = image.getData();
					
			int radius = DViewerActivator.getLocalPreferenceStore().getInt(DViewerEditorConstants.PREFERENCE_PHA_RADIUS);
			try {
				radius = Integer.getInteger("org.dawnsci.plotting.pha.radius");
			} catch (NullPointerException ne) {
				// leave radius as is.
			}
			if (radius<1) radius = 2;
			
			double lower = DViewerActivator.getLocalPreferenceStore().getDouble(DViewerEditorConstants.PREFERENCE_VALID_VALUE_MIN);
			try {
				lower = Integer.getInteger("org.dawnsci.plotting.pha.validValueMin");
			} catch (NullPointerException ne) {
				// leave radius as is.
			}
			if (lower<0) lower = 0d;

			
			final PHA       pha  = new PHA(radius, lower); // TODO preference
			final Rectangle rect = PHA.getDataSetRectangle(orig);
			
			this.trans = pha.applyPHA(orig, rect, monitor); // TODO monitor
			return trans;
		}
	}
	

	private IImageTrace getImage(ExecutionEvent event) {
		
		if (event==null) return null;
		final Object context = event.getApplicationContext();
		IImageTrace image = context instanceof IImageTrace
				          ? (IImageTrace)context
				          : null;
				          
		if (image == null) {
			
			IPlottingSystem system = context instanceof IPlottingSystem
					               ? (IPlottingSystem)context
					               : (IPlottingSystem)EclipseUtils.getPage().getActivePart().getAdapter(IPlottingSystem.class);
			
		    image = (IImageTrace)system.getTraces(IImageTrace.class).iterator().next();
		}
		
		return image;
	}


	public void schedule(ExecutionEvent event) {
		cancel();
		this.event = event;
		schedule();
	}
}
