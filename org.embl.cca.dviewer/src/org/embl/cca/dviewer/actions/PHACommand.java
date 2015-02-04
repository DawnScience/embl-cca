package org.embl.cca.dviewer.actions;

import org.dawb.common.ui.util.EclipseUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.swt.graphics.Rectangle;
import org.embl.cca.dviewer.ui.editors.utils.PHA;
import org.embl.cca.dviewer.ui.editors.utils.PSF;

/**
 * Command exposed by this plugin to allow the PHA to be added to any plot.
 * 
 * @author fcp94556
 *
 */
public class PHACommand extends AbstractHandler implements IHandler {
	
	private IDataset orig;
	private IDataset trans;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		IImageTrace image = getImage(event);		
		IDataset    trans = getToggledData(image);
		
		image.setData(trans, image.getAxes(), false);

		return image;
	}

	private IDataset getToggledData(IImageTrace image) {
		
		if (image.getData()==trans) {
			return orig;
		} else {
			this.orig = image.getData();
			final PHA       pha  = new PHA(8); // TODO preference
			final Rectangle rect = PHA.getDataSetRectangle(orig);
			
			this.trans = pha.applyPHA(orig, rect, null); // TODO monitor
			return trans;
		}
	}

	private IImageTrace getImage(ExecutionEvent event) {
		
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

}
