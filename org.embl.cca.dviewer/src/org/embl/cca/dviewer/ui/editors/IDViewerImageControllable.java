package org.embl.cca.dviewer.ui.editors;

import org.eclipse.dawnsci.plotting.api.trace.IImageTrace.DownsampleType;
import org.embl.cca.dviewer.ui.editors.utils.Point2DD;
import org.embl.cca.utils.general.ISomethingChangeListener;

public interface IDViewerImageControllable {
	public boolean getPha();
	public void setPha(final ISomethingChangeListener sender, final boolean phaState);
	public int getPhaRadiusMin();
	public int getPhaRadiusSup();
	public boolean isPhaRadiusValid(final int value);
	public int getPhaRadius();
	public void setPhaRadius(final ISomethingChangeListener sender, final int phaRadius);

	public int getHMin();
	public int getHSup();
	public boolean isHValid(final int value);
	public int getHRangeMin();
	public void setHRangeMin(final ISomethingChangeListener sender, final int hRangeMin);
	public int getHRangeMax();
	public void setHRangeMax(final ISomethingChangeListener sender, final int hRangeMax);

	public int getKMin();
	public int getKSup();
	public boolean isKValid(final int value);
	public int getKRangeMin();
	public void setKRangeMin(final ISomethingChangeListener sender, final int kRangeMin);
	public int getKRangeMax();
	public void setKRangeMax(final ISomethingChangeListener sender, final int kRangeMax);

	public int getLMin();
	public int getLSup();
	public boolean isLValid(final int value);
	public int getLRangeMin();
	public void setLRangeMin(final ISomethingChangeListener sender, final int lRangeMin);
	public int getLRangeMax();
	public void setLRangeMax(final ISomethingChangeListener sender, final int lRangeMax);

	public DownsampleType getDownsampleType();
	public void setDownsampleType(final ISomethingChangeListener sender, final DownsampleType downsampleType);
	public Point2DD getMouseAxisPos();
	public String getStatusText();

	public void requestDViewerView();
	public void requestDViewerControls();
}
