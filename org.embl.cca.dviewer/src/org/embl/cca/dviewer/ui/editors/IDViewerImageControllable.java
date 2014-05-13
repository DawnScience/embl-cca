package org.embl.cca.dviewer.ui.editors;

import org.dawnsci.plotting.api.trace.IImageTrace.DownsampleType;
import org.embl.cca.utils.general.ISomethingChangeListener;

public interface IDViewerImageControllable {
	public boolean getPha();
	public void setPha(final ISomethingChangeListener sender, final boolean phaState);
	public int getPhaRadiusMin();
	public int getPhaRadiusSup();
	public boolean isPhaRadiusValid(final int value);
	public int getPhaRadius();
	public void setPhaRadius(final ISomethingChangeListener sender, final int phaRadius);

	public DownsampleType getDownsampleType();
	public void setDownsampleType(final ISomethingChangeListener sender, final DownsampleType downsampleType);
}
