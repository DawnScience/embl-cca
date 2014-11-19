package org.embl.cca.dviewer.ui.editors;

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
	public int getShowEachNthImageMin();
	public int getShowEachNthImageSup();
	public boolean isShowEachNthImageValid(final int showEachNthImage);
	public int getShowEachNthImage();
	public void setShowEachNthImage(final ISomethingChangeListener sender, final int showEachNthImage);

	public int getImageArrayMin();
	public int getImageArraySup();
	public int getImageArrayBatchIndex();
	public int getImageArrayBatchSize();
	public boolean isBatchIndexValid(final int value);
	public void setBatchIndex(final ISomethingChangeListener sender, final int batchIndex);
	public boolean isBatchSizeValid(final int value);
	public void setBatchSize(final ISomethingChangeListener sender, final int batchSize);
}
