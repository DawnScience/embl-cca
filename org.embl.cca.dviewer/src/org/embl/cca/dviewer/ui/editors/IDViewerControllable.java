package org.embl.cca.dviewer.ui.editors;

import org.eclipse.swt.widgets.Control;
import org.embl.cca.utils.general.ISomethingChangeListener;

public interface IDViewerControllable extends IDViewerImageControllable {
	public void addSomethingListener(final ISomethingChangeListener listener);
	public void removeSomethingListener(final ISomethingChangeListener listener);

	public boolean getAutoSelectLatestNewImage();
	public void setAutoSelectLatestNewImage(final ISomethingChangeListener sender, final boolean autoFollow);
	public boolean isRemoted();
	public boolean isDisplayingImageByRemoteRequest();
	public void toggleAutoDisplayRemotedImage() throws IllegalStateException;
	/**
	 * Opens the current remoted image in a dedicated editor.
	 */
	public void displayRemotedImageDedicated();

	public int getImageArrayMin();
	public int getImageArraySup();
	public int getImageArrayBatchIndex();
	public int getImageArrayBatchSize();
	public void setBatchIndex(final ISomethingChangeListener sender, final int batchIndex);
	public void setBatchSize(final ISomethingChangeListener sender, final int batchSize);
	public boolean isBatchSizeValid(final int value);

	/**
	 * Revalidates the layout of implementor due to change of control
	 * @param control the control requiring revalidating the layout
	 */
	public void revalidateLayout(final Control control);
}
