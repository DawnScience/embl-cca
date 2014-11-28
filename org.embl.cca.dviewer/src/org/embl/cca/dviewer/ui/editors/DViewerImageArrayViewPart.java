package org.embl.cca.dviewer.ui.editors;

import java.util.Map;

import org.dawnsci.common.widgets.editor.ITitledEditor;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace.DownsampleType;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.embl.cca.dviewer.ui.editors.utils.Point2DD;
import org.embl.cca.utils.datahandling.EFile;
import org.embl.cca.utils.general.ISomethingChangeListener;

public class DViewerImageArrayViewPart extends ViewPart implements IViewPartHost, ITitledEditor,
	IDViewerControllable {

	public static final String ID = "org.embl.cca.dviewer.ui.editors.DViewerImageArrayViewPart";

	final DViewerImageArrayEditorAndViewPart innerViewPart;

	/**
	 * In ViewPart the editor input does not exist, have to manage it here.
	 * Editor input, or <code>null</code> if none.
	 */
	protected IEditorInput editorInput = null;

	public DViewerImageArrayViewPart(final PlotType defaultPlotType, final DViewerListenerManager listenerManager) {
		innerViewPart = new DViewerImageArrayEditorAndViewPart(this);
		innerViewPart.addPartPropertyListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				firePartPropertyChanged(event.getProperty(), (String)event.getOldValue(), (String)event.getNewValue());
			}
		});
	}

	@Override
	public boolean getPha() {
		return innerViewPart.getPha();
	}

	@Override
	public void setPha(final ISomethingChangeListener sender, final boolean phaState) {
		innerViewPart.setPha(sender, phaState);
	}

	@Override
	public int getPhaRadiusMin() {
		return innerViewPart.getPhaRadiusMin();
	}

	@Override
	public int getPhaRadiusSup() {
		return innerViewPart.getPhaRadiusSup();
	}

	@Override
	public boolean isPhaRadiusValid(final int value) {
		return innerViewPart.isPhaRadiusValid(value);
	}

	@Override
	public int getPhaRadius() {
		return innerViewPart.getPhaRadius();
	}

	@Override
	public void setPhaRadius(final ISomethingChangeListener sender, final int phaRadius) {
		innerViewPart.setPhaRadius(sender, phaRadius);
	}

	@Override //from IDViewerImageControllable
	public void setHKLFile(final EFile file) {
		innerViewPart.setHKLFile(file);
	}

	@Override
	public int getHMin() {
		return innerViewPart.getHMin();
	}

	@Override
	public int getHSup() {
		return innerViewPart.getHSup();
	}

	@Override
	public boolean isHValid(final int value) {
		return innerViewPart.isHValid(value);
	}

	@Override
	public int getHRangeMin() {
		return innerViewPart.getHRangeMin();
	}

	@Override
	public void setHRangeMin(final ISomethingChangeListener sender, final int hRangeMin) {
		innerViewPart.setHRangeMin(sender, hRangeMin);
	}

	@Override
	public int getHRangeMax() {
		return innerViewPart.getHRangeMax();
	}

	@Override
	public void setHRangeMax(final ISomethingChangeListener sender, final int hRangeMax) {
		innerViewPart.setHRangeMax(sender, hRangeMax);
	}

	@Override
	public int getKMin() {
		return innerViewPart.getKMin();
	}

	@Override
	public int getKSup() {
		return innerViewPart.getKSup();
	}

	@Override
	public boolean isKValid(final int value) {
		return innerViewPart.isKValid(value);
	}

	@Override
	public int getKRangeMin() {
		return innerViewPart.getKRangeMin();
	}

	@Override
	public void setKRangeMin(final ISomethingChangeListener sender, final int kRangeMin) {
		innerViewPart.setKRangeMin(sender, kRangeMin);
		
	}

	@Override
	public int getKRangeMax() {
		return innerViewPart.getKRangeMax();
	}

	@Override
	public void setKRangeMax(final ISomethingChangeListener sender, final int kRangeMax) {
		innerViewPart.setKRangeMax(sender, kRangeMax);
	}

	@Override
	public int getLMin() {
		return innerViewPart.getLMin();
	}

	@Override
	public int getLSup() {
		return innerViewPart.getLSup();
	}

	@Override
	public boolean isLValid(final int value) {
		return innerViewPart.isLValid(value);
	}

	@Override
	public int getLRangeMin() {
		return innerViewPart.getLRangeMin();
	}

	@Override
	public void setLRangeMin(final ISomethingChangeListener sender, final int lRangeMin) {
		innerViewPart.setLRangeMin(sender, lRangeMin);
	}

	@Override
	public int getLRangeMax() {
		return innerViewPart.getLRangeMax();
	}

	@Override
	public void setLRangeMax(final ISomethingChangeListener sender, final int lRangeMax) {
		innerViewPart.setLRangeMax(sender, lRangeMax);
	}

	@Override
	public DownsampleType getDownsampleType() {
		return innerViewPart.getDownsampleType();
	}

	@Override
	public void setDownsampleType(final ISomethingChangeListener sender,
			final DownsampleType downsampleType) {
		innerViewPart.setDownsampleType(sender, downsampleType);
	}

	@Override
	public Point2DD getMouseAxisPos() {
		return innerViewPart.getMouseAxisPos();
	}

	@Override
	public String getStatusText() {
		return innerViewPart.getStatusText();
	}

	@Override
	public void requestDViewerView() {
		innerViewPart.requestDViewerView();
	}

	@Override
	public void requestDViewerControls() {
		innerViewPart.requestDViewerControls();
	}

	@Override //from ITitledEditor
	public void setPartTitle(final String name) {
		innerViewPart.setPartTitle(name);
	}

	@Override //from IEditorPart/IWorkbenchPart
	public void addPropertyListener(final IPropertyListener listener) {
		super.addPropertyListener(listener);
	}

	@Override //from IEditorPart/IWorkbenchPart
	public void createPartControl(final Composite parent) {
		innerViewPart.createPartControl(parent);
	}

	@Override //from IEditorPart/IWorkbenchPart
	public void dispose() {
		innerViewPart.dispose();
	}

	@Override //from IEditorPart/IWorkbenchPart
	public IWorkbenchPartSite getSite() {
		return innerViewPart.getSite();
	}

	@Override //from IEditorPart/IWorkbenchPart
	public Image getTitleImage() {
		return innerViewPart.getTitleImage();
	}

	@Override //from IEditorPart/IWorkbenchPart
	public String getTitleToolTip() {
		return innerViewPart.getTitleToolTip();
	}

	@Override //from IEditorPart/IWorkbenchPart
	public void removePropertyListener(final IPropertyListener listener) {
		super.removePropertyListener(listener);
	}

	@Override //from IEditorPart/IWorkbenchPart
	public void setFocus() {
		innerViewPart.setFocus();
	}

	@Override //from IEditorPart/IWorkbenchPart/IAdaptable
	public Object getAdapter(@SuppressWarnings("rawtypes") final Class clazz) {
		return innerViewPart.getAdapter(clazz);
	}

	@Override //from IViewPart
	public IViewSite getViewSite() {
		return innerViewPart.getViewSite();
	}

	@Override //from IViewPart
	public void init(final IViewSite site) throws PartInitException {
		innerViewPart.init(site);
	}

	@Override //from IViewPart
	public void init(final IViewSite site, final IMemento memento) throws PartInitException {
		innerViewPart.init(site, memento);
	}

	@Override //from IViewPart
	public void saveState(final IMemento memento) {
		innerViewPart.saveState(memento);
	}

	//from IEditorPart, comfortable method in ViewPart as well
	public IEditorInput getEditorInput() {
		return editorInput;
	}

	//from IEditorPart, comfortable method in ViewPart as well
	public void setInput(final IEditorInput input) {
		final boolean firstInput = getEditorInput() == null;
		Assert.isLegal(input != null);
		editorInput = input;
		if( firstInput ) //This case is for this being ViewPart
			innerViewPart.setRemotedByInput();
		setPartTitle(getEditorInput().getName());
	}

	//from IEditorPart, comfortable method in ViewPart as well
	public void setInputWithNotify(final IEditorInput input) {
		setInput(input);
		firePropertyChange(IEditorPart.PROP_INPUT);
	}

	@Override //from WorkbenchPart
	public void setInitializationData(final IConfigurationElement cfig,
			final String propertyName, final Object data) {
		innerViewPart.setInitializationData(cfig, propertyName, data);
	}

	@Override //from WorkbenchPart
	public void showBusy(final boolean busy) {
		innerViewPart.showBusy(busy);
	}

	@Override //from IEditorPartHost (originated from IWorkbenchPart2)
	public String getPartName() {
		return super.getPartName();
	}

	@Override //from IEditorPartHost (originated from WorkbenchPart)
	public void setPartName(final String partName) {
		super.setPartName(partName);
	}

	@Override //from IEditorPartHost (originated from IEditorPart/IWorkbenchPart)
	public String getTitle() {
		return super.getTitle();
	}

	@Override //from IEditorPartHost (originated from WorkbenchPart)
	@Deprecated
	public void setTitle(String title) {
		super.setTitle(title);
	}

	@Override //from IEditorPartHost (originated from IWorkbenchPart2)
	public String getContentDescription() {
		return super.getContentDescription();
	}

	@Override //from IEditorPartHost (originated from WorkbenchPart)
	public void setContentDescription(final String description) {
		super.setContentDescription(description);
	}

	@Override //from IEditorPart/IWorkbenchPartOrientation
	public int getOrientation() {
		return innerViewPart.getOrientation();
	}

	@Override //from IEditorPart/IWorkbenchPart3
	public void addPartPropertyListener(final IPropertyChangeListener listener) {
		innerViewPart.addPartPropertyListener(listener);
	}

	@Override //from IEditorPart/IWorkbenchPart3
	public void removePartPropertyListener(final IPropertyChangeListener listener) {
		innerViewPart.removePartPropertyListener(listener);
	}

	@Override //from IEditorPart/IWorkbenchPart3
	public void setPartProperty(final String key, final String value) {
		innerViewPart.setPartProperty(key, value);
	}

	@Override //from IEditorPart/IWorkbenchPart3
	public String getPartProperty(final String key) {
		return innerViewPart.getPartProperty(key);
	}

	@SuppressWarnings("rawtypes")
	@Override //from IEditorPart/IWorkbenchPart3
	public Map getPartProperties() {
		return innerViewPart.getPartProperties();
	}

	/**
	 * Not implemented method.
	 */
	@Override
	// from IEditorPartHost
	public void setToolbarParent(final Composite toolbarParent) {
//		innerViewPart.setToolbarParent(toolbarParent);
	}

	@Override //from IEditorPartHost
	public Composite getContainer() {
		return innerViewPart.getContainer();
	}

	//from IShowEditorInput, comfortable method in ViewPart as well
	public void showEditorInput(final IEditorInput editorInput) {
		innerViewPart.showEditorInput(editorInput);
	}

	@Override //from IDViewerControllable
	public void addSomethingListener(ISomethingChangeListener listener) {
		innerViewPart.addSomethingListener(listener);
	}

	@Override //from IDViewerControllable
	public void removeSomethingListener(ISomethingChangeListener listener) {
		innerViewPart.removeSomethingListener(listener);
	}

	@Override //from IDViewerControllable
	public boolean getAutoSelectLatestNewImage() {
		return innerViewPart.getAutoSelectLatestNewImage();
	}

	@Override //from IDViewerControllable
	public void setAutoSelectLatestNewImage(ISomethingChangeListener sender,
			boolean autoFollow) {
		innerViewPart.setAutoSelectLatestNewImage(sender, autoFollow);
	}

	@Override //from IDViewerControllable
	public boolean isRemoted() {
		return innerViewPart.isRemoted();
	}

	@Override //from IDViewerControllable
	public boolean isDisplayingImageByRemoteRequest() {
		return innerViewPart.isDisplayingImageByRemoteRequest();
	}

	@Override //from IDViewerControllable
	public void toggleAutoDisplayRemotedImage() throws IllegalStateException {
		innerViewPart.toggleAutoDisplayRemotedImage();
	}

	@Override //from IDViewerControllable
	public int getShowEachNthImageMin() {
		return innerViewPart.getShowEachNthImageMin();
	}

	@Override //from IDViewerControllable
	public int getShowEachNthImageSup() {
		return innerViewPart.getShowEachNthImageSup();
	}

	@Override //from IDViewerControllable
	public boolean isShowEachNthImageValid(final int showEachNthImage) {
		return innerViewPart.isShowEachNthImageValid(showEachNthImage);
	}

	@Override //from IDViewerControllable
	public int getShowEachNthImage() {
		return innerViewPart.getShowEachNthImage();
	}

	@Override //from IDViewerControllable
	public void setShowEachNthImage(final ISomethingChangeListener sender,
			final int showEachNthImage) {
		innerViewPart.setShowEachNthImage(sender, showEachNthImage);
	}

	@Override //from IDViewerControllable
	public void displayRemotedImageDedicated() {
		innerViewPart.displayRemotedImageDedicated();
	}

	@Override //from IDViewerControllable
	public int getImageArrayMin() {
		return innerViewPart.getImageArrayMin();
	}

	@Override //from IDViewerControllable
	public int getImageArraySup() {
		return innerViewPart.getImageArraySup();
	}

	@Override //from IDViewerControllable
	public int getImageArrayBatchIndex() {
		return innerViewPart.getImageArrayBatchIndex();
	}

	@Override //from IDViewerControllable
	public int getImageArrayBatchSize() {
		return innerViewPart.getImageArrayBatchSize();
	}

	@Override //from IDViewerControllable
	public boolean isBatchIndexValid(final int value) {
		return innerViewPart.isBatchSizeValid(value);
	}

	@Override //from IDViewerControllable
	public void setBatchIndex(final ISomethingChangeListener sender, final int batchIndex) {
		innerViewPart.setBatchIndex(sender, batchIndex);
	}

	@Override //from IDViewerControllable
	public boolean isBatchSizeValid(final int value) {
		return innerViewPart.isBatchSizeValid(value);
	}

	@Override //from IDViewerControllable
	public void setBatchSize(final ISomethingChangeListener sender, final int batchSize) {
		innerViewPart.setBatchSize(sender, batchSize);
	}

}
