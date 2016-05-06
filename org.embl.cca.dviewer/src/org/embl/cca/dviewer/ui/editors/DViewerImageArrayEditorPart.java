package org.embl.cca.dviewer.ui.editors;

import java.util.Map;

import org.dawnsci.common.widgets.editor.ITitledEditor;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace.DownsampleType;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IShowEditorInput;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.embl.cca.dviewer.ui.editors.utils.Point2DD;
import org.embl.cca.dviewer.ui.views.DViewerImagePage;
import org.embl.cca.utils.datahandling.EFile;
import org.embl.cca.utils.extension.PartAdapter;
import org.embl.cca.utils.general.ISomethingChangeListener;
//public class DViewerImageArrayEditorAndViewPart extends EditorPart implements
//ITitledEditor, IReusableEditor, IShowEditorInput, IDViewerControllable, IFileLoaderListener {
import org.embl.cca.utils.threading.CommonThreading;

public class DViewerImageArrayEditorPart extends EditorPart implements IEditorPartHost, IReusableEditor, ITitledEditor, ISaveablePart,
	IShowEditorInput, IDViewerControllable, IDViewerControlsPageAdaptable {

	public static final String ID = "org.embl.cca.dviewer.ui.editors.DViewerImageArrayEditorPart";

	final DViewerImageArrayEditorAndViewPart innerEditorPart;

	public final static String REMOTED_IMAGE = "Remoted Image";

	public DViewerImageArrayEditorPart() {
		innerEditorPart = new DViewerImageArrayEditorAndViewPart(this);
		innerEditorPart.addPartPropertyListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				firePartPropertyChanged(event.getProperty(), (String)event.getOldValue(), (String)event.getNewValue());
			}
		});
	}

	@Override
	public boolean getPha() {
		return innerEditorPart.getPha();
	}

	@Override
	public void setPha(final ISomethingChangeListener sender, final boolean phaState) {
		innerEditorPart.setPha(sender, phaState);
	}

	@Override
	public int getPhaRadiusMin() {
		return innerEditorPart.getPhaRadiusMin();
	}

	@Override
	public int getPhaRadiusSup() {
		return innerEditorPart.getPhaRadiusSup();
	}

	@Override
	public boolean isPhaRadiusValid(final int value) {
		return innerEditorPart.isPhaRadiusValid(value);
	}

	@Override
	public int getPhaRadius() {
		return innerEditorPart.getPhaRadius();
	}

	@Override
	public void setPhaRadius(final ISomethingChangeListener sender, final int phaRadius) {
		innerEditorPart.setPhaRadius(sender, phaRadius);
	}

	@Override //from IDViewerImageControllable
	public void setHKLFile(final EFile file) {
		innerEditorPart.setHKLFile(file);
	}

	@Override
	public int getHMin() {
		return innerEditorPart.getHMin();
	}

	@Override
	public int getHSup() {
		return innerEditorPart.getHSup();
	}

	@Override
	public boolean isHValid(final int value) {
		return innerEditorPart.isHValid(value);
	}

	@Override
	public int getHRangeMin() {
		return innerEditorPart.getHRangeMin();
	}

	@Override
	public void setHRangeMin(final ISomethingChangeListener sender, final int hRangeMin) {
		innerEditorPart.setHRangeMin(sender, hRangeMin);
	}

	@Override
	public int getHRangeMax() {
		return innerEditorPart.getHRangeMax();
	}

	@Override
	public void setHRangeMax(final ISomethingChangeListener sender, final int hRangeMax) {
		innerEditorPart.setHRangeMax(sender, hRangeMax);
	}

	@Override
	public int getKMin() {
		return innerEditorPart.getKMin();
	}

	@Override
	public int getKSup() {
		return innerEditorPart.getKSup();
	}

	@Override
	public boolean isKValid(final int value) {
		return innerEditorPart.isKValid(value);
	}

	@Override
	public int getKRangeMin() {
		return innerEditorPart.getKRangeMin();
	}

	@Override
	public void setKRangeMin(final ISomethingChangeListener sender, final int kRangeMin) {
		innerEditorPart.setKRangeMin(sender, kRangeMin);
		
	}

	@Override
	public int getKRangeMax() {
		return innerEditorPart.getKRangeMax();
	}

	@Override
	public void setKRangeMax(final ISomethingChangeListener sender, final int kRangeMax) {
		innerEditorPart.setKRangeMax(sender, kRangeMax);
	}

	@Override
	public int getLMin() {
		return innerEditorPart.getLMin();
	}

	@Override
	public int getLSup() {
		return innerEditorPart.getLSup();
	}

	@Override
	public boolean isLValid(final int value) {
		return innerEditorPart.isLValid(value);
	}

	@Override
	public int getLRangeMin() {
		return innerEditorPart.getLRangeMin();
	}

	@Override
	public void setLRangeMin(final ISomethingChangeListener sender, final int lRangeMin) {
		innerEditorPart.setLRangeMin(sender, lRangeMin);
	}

	@Override
	public int getLRangeMax() {
		return innerEditorPart.getLRangeMax();
	}

	@Override
	public void setLRangeMax(final ISomethingChangeListener sender, final int lRangeMax) {
		innerEditorPart.setLRangeMax(sender, lRangeMax);
	}

	@Override
	public DownsampleType getDownsampleType() {
		return innerEditorPart.getDownsampleType();
	}

	@Override
	public void setDownsampleType(final ISomethingChangeListener sender,
			final DownsampleType downsampleType) {
		innerEditorPart.setDownsampleType(sender, downsampleType);
	}

	@Override
	public Point2DD getMouseAxisPos() {
		return innerEditorPart.getMouseAxisPos();
	}

	@Override
	public String getStatusText() {
		return innerEditorPart.getStatusText();
	}

	@Override
	public void requestDViewerView() {
		innerEditorPart.requestDViewerView();
	}

	@Override
	public void requestDViewerControls() {
		innerEditorPart.requestDViewerControls();
	}

	@Override //from ITitledEditor
	public void setPartTitle(final String name) {
		innerEditorPart.setPartTitle(name);
	}

	@Override //from IEditorPart/IWorkbenchPart
	public void addPropertyListener(final IPropertyListener listener) {
		super.addPropertyListener(listener);
	}

	IPartListener2 partCreatedAndActivatedListener;
	@Override //from IEditorPart/IWorkbenchPart
	public void createPartControl(final Composite parent) {
		innerEditorPart.createPartControl(parent);
		partCreatedAndActivatedListener = new PartAdapter() {
			@Override
			public void partActivated(final IWorkbenchPartReference partRef) {
				if( DViewerImageArrayEditorPart.this.equals(partRef.getPart(false))) {
					getSite().getPage().removePartListener(partCreatedAndActivatedListener);
					CommonThreading.execUIAsynced(new Runnable() {
						@Override
						public void run() {
							requestDViewerView();
						}
					});
				}
			}
		};
		getSite().getPage().addPartListener(partCreatedAndActivatedListener);
	}

	@Override //from IEditorPart/IWorkbenchPart
	public void dispose() {
		innerEditorPart.dispose();
	}

	@Override //from IEditorPart/IWorkbenchPart
	public IWorkbenchPartSite getSite() {
		return innerEditorPart.getSite();
	}

	@Override //from IEditorPart/IWorkbenchPart
	public Image getTitleImage() {
		return innerEditorPart.getTitleImage();
	}

	@Override //from IEditorPart/IWorkbenchPart
	public String getTitleToolTip() {
		return innerEditorPart.getTitleToolTip();
	}

	@Override //from IEditorPart/IWorkbenchPart
	public void removePropertyListener(final IPropertyListener listener) {
		super.removePropertyListener(listener);
	}

	@Override //from IEditorPart/IWorkbenchPart
	public void setFocus() {
		innerEditorPart.setFocus();
	}

	@Override //from IEditorPart/IWorkbenchPart/IAdaptable
	public <T> T getAdapter(final Class<T> clazz) {
		if (DViewerImagePage.class.equals(clazz)) {
			return clazz.cast(DViewerImagePage.getPageFor(this));
		}
		return innerEditorPart.getAdapter(clazz);
	}

	@Override //from IEditorPart
	public IEditorSite getEditorSite() {
		return innerEditorPart.getEditorSite();
	}

	@Override //from IEditorPart
	public void init(final IEditorSite site, final IEditorInput input)
			throws PartInitException {
		innerEditorPart.init(site, input);
	}

	@Override //from ISaveablePart
	public void doSave(final IProgressMonitor monitor) {
		innerEditorPart.doSave(monitor);
	}

	@Override //from ISaveablePart
	public void doSaveAs() {
		innerEditorPart.doSaveAs();
	}

	@Override //from ISaveablePart
	public boolean isDirty() {
		return innerEditorPart.isDirty();
	}

	@Override //from ISaveablePart
	public boolean isSaveAsAllowed() {
		return innerEditorPart.isSaveAsAllowed();
	}

	@Override //from ISaveablePart
	public boolean isSaveOnCloseNeeded() {
		return innerEditorPart.isSaveOnCloseNeeded();
	}

	@Override //from IEditorPart
	public IEditorInput getEditorInput() {
		return super.getEditorInput();
	}

	@Override //from IReusableEditor
	public void setInput(final IEditorInput input) {
		final boolean firstInput = getEditorInput() == null;
		super.setInput(input);
		setPartTitle(getEditorInput().getName());
		if( firstInput ) //This case is for this being ViewPart
			innerEditorPart.setRemotedByInput();
	}

	@Override //from EditorPart
	public void setInputWithNotify(final IEditorInput input) {
		final boolean firstInput = getEditorInput() == null;
		super.setInputWithNotify(input);
		if( firstInput ) //This case is for this being ViewPart
			innerEditorPart.setRemotedByInput();
		setPartTitle(getEditorInput().getName());
	}

	@Override //from WorkbenchPart
	public void setInitializationData(final IConfigurationElement cfig,
			final String propertyName, final Object data) {
		innerEditorPart.setInitializationData(cfig, propertyName, data);
	}

	@Override //from WorkbenchPart
	public void showBusy(final boolean busy) {
		innerEditorPart.showBusy(busy);
	}

	@Override //from IEditorPartHost (originated from IWorkbenchPart2)
	public String getPartName() {
		return super.getPartName();
	}

	@Override //from IEditorPartHost (originated from WorkbenchPart)
	public void setPartName(final String partName) {
		final String flaggedName = isRemoted() ? (isDisplayingImageByRemoteRequest() ? "▶" : "❙❙") + partName : partName;
		super.setPartName(flaggedName);
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
		return innerEditorPart.getOrientation();
	}

	@Override //from IEditorPart/IWorkbenchPart3
	public void addPartPropertyListener(final IPropertyChangeListener listener) {
		innerEditorPart.addPartPropertyListener(listener);
	}

	@Override //from IEditorPart/IWorkbenchPart3
	public void removePartPropertyListener(final IPropertyChangeListener listener) {
		innerEditorPart.removePartPropertyListener(listener);
	}

	@Override //from IEditorPart/IWorkbenchPart3
	public void setPartProperty(final String key, final String value) {
		innerEditorPart.setPartProperty(key, value);
	}

	@Override //from IEditorPart/IWorkbenchPart3
	public String getPartProperty(final String key) {
		return innerEditorPart.getPartProperty(key);
	}

	@Override //from IEditorPart/IWorkbenchPart3
	public Map<String,String> getPartProperties() {
		return innerEditorPart.getPartProperties();
	}

	/**
	 * Not implemented method.
	 */
	@Override
	// from IEditorPartHost
	public void setToolbarParent(final Composite toolbarParent) {
//		innerEditorPart.setToolbarParent(toolbarParent);
	}

	@Override //from IEditorPartHost
	public Composite getContainer() {
		return innerEditorPart.getContainer();
	}

	@Override //from IShowEditorInput
	public void showEditorInput(final IEditorInput editorInput) {
		innerEditorPart.showEditorInput(editorInput);
	}

	@Override //from IDViewerControllable
	public void addSomethingListener(ISomethingChangeListener listener) {
		innerEditorPart.addSomethingListener(listener);
	}

	@Override //from IDViewerControllable
	public void removeSomethingListener(ISomethingChangeListener listener) {
		innerEditorPart.removeSomethingListener(listener);
	}

	@Override //from IDViewerControllable
	public boolean getAutoSelectLatestNewImage() {
		return innerEditorPart.getAutoSelectLatestNewImage();
	}

	@Override //from IDViewerControllable
	public void setAutoSelectLatestNewImage(ISomethingChangeListener sender,
			boolean autoFollow) {
		innerEditorPart.setAutoSelectLatestNewImage(sender, autoFollow);
	}

	@Override //from IDViewerControllable
	public boolean isRemoted() {
		return innerEditorPart.isRemoted();
	}

	@Override //from IDViewerControllable
	public boolean isDisplayingImageByRemoteRequest() {
		return innerEditorPart.isDisplayingImageByRemoteRequest();
	}

	@Override //from IDViewerControllable
	public void toggleAutoDisplayRemotedImage() throws IllegalStateException {
		innerEditorPart.toggleAutoDisplayRemotedImage();
	}

	@Override //from IDViewerControllable
	public void displayRemotedImageDedicated() {
		innerEditorPart.displayRemotedImageDedicated();
	}

	@Override //from IDViewerControllable
	public int getShowEachNthImageMin() {
		return innerEditorPart.getShowEachNthImageMin();
	}

	@Override //from IDViewerControllable
	public int getShowEachNthImageSup() {
		return innerEditorPart.getShowEachNthImageSup();
	}

	@Override //from IDViewerControllable
	public boolean isShowEachNthImageValid(final int showEachNthImage) {
		return innerEditorPart.isShowEachNthImageValid(showEachNthImage);
	}

	@Override //from IDViewerControllable
	public int getShowEachNthImage() {
		return innerEditorPart.getShowEachNthImage();
	}

	@Override //from IDViewerControllable
	public void setShowEachNthImage(final ISomethingChangeListener sender,
			final int showEachNthImage) {
		innerEditorPart.setShowEachNthImage(sender, showEachNthImage);
	}

	@Override //from IDViewerControllable
	public int getImageArrayMin() {
		return innerEditorPart.getImageArrayMin();
	}

	@Override //from IDViewerControllable
	public int getImageArraySup() {
		return innerEditorPart.getImageArraySup();
	}

	@Override //from IDViewerControllable
	public int getImageArrayBatchIndex() {
		return innerEditorPart.getImageArrayBatchIndex();
	}

	@Override //from IDViewerControllable
	public int getImageArrayBatchSize() {
		return innerEditorPart.getImageArrayBatchSize();
	}

	@Override //from IDViewerControllable
	public boolean isBatchIndexValid(final int value) {
		return innerEditorPart.isBatchIndexValid(value);
	}

	@Override //from IDViewerControllable
	public void setBatchIndex(ISomethingChangeListener sender, int batchIndex) {
		innerEditorPart.setBatchIndex(sender, batchIndex);
	}

	@Override //from IDViewerControllable
	public boolean isBatchSizeValid(final int value) {
		return innerEditorPart.isBatchSizeValid(value);
	}

	@Override //from IDViewerControllable
	public void setBatchSize(ISomethingChangeListener sender, int batchSize) {
		innerEditorPart.setBatchSize(sender, batchSize);
	}

}
