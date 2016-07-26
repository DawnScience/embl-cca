package org.embl.cca.dviewer.actions;

import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dawnsci.plotting.api.filter.AbstractDelayedFilter;
import org.eclipse.january.dataset.IDataset;
import org.eclipse.swt.graphics.Rectangle;
import org.embl.cca.dviewer.DViewerActivator;
import org.embl.cca.dviewer.ui.editors.preference.DViewerEditorConstants;
import org.embl.cca.dviewer.ui.editors.utils.PHA;

public class PHAFilter extends AbstractDelayedFilter {

	@Override
	public int getRank() {
		return 2;
	}

	@Override
	protected Object[] filter(IDataset orig, List<IDataset> axes) throws Exception {

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
		
		IDataset trans = pha.applyPHA(orig, rect, new NullProgressMonitor()); // TODO monitor
		return new Object[]{trans, axes};
		
	}
}
