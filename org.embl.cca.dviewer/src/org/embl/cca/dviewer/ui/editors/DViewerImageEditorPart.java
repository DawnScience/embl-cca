package org.embl.cca.dviewer.ui.editors;

import java.util.Map;

import org.dawnsci.common.widgets.editor.ITitledEditor;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace.DownsampleType;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IShowEditorInput;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.embl.cca.dviewer.ui.editors.utils.Point2DD;
import org.embl.cca.utils.general.ISomethingChangeListener;

public class DViewerImageEditorPart extends EditorPart implements IEditorPartHost, IReusableEditor, ITitledEditor, ISaveablePart,
	IShowEditorInput, IDViewerImageControllable {

	public static final String ID = "org.embl.cca.dviewer.ui.editors.DViewerImageEditorPart";

	final DViewerImageEditorAndViewPart innerEditorPart;

	public DViewerImageEditorPart(final PlotType defaultPlotType, final DViewerListenerManager listenerManager) {
		innerEditorPart = new DViewerImageEditorAndViewPart(this, defaultPlotType, listenerManager);
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

	@Override //from ITitledEditor
	public void setPartTitle(final String name) {
		innerEditorPart.setPartTitle(name);
	}

	@Override //from IShowEditorInput
	public void showEditorInput(final IEditorInput editorInput) {
		innerEditorPart.showEditorInput(editorInput);
		
	}

	@Override //from IEditorPart/IWorkbenchPart
	public void addPropertyListener(final IPropertyListener listener) {
		super.addPropertyListener(listener);
	}

	@Override //from IEditorPart/IWorkbenchPart
	public void createPartControl(final Composite parent) {
		innerEditorPart.createPartControl(parent);
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
	public Object getAdapter(@SuppressWarnings("rawtypes") final Class clazz) {
		return innerEditorPart.getAdapter(clazz);
	}

	@Override //from IEditorPart
	public IEditorInput getEditorInput() {
		return innerEditorPart.getEditorInput();
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

	@Override //from IReusableEditor
	public void setInput(final IEditorInput input) {
		innerEditorPart.setInput(input);
	}

	@Override //from EditorPart
	public void setInputWithNotify(final IEditorInput input) {
		innerEditorPart.setInputWithNotify(input);
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

	@SuppressWarnings("rawtypes")
	@Override //from IEditorPart/IWorkbenchPart3
	public Map getPartProperties() {
		return innerEditorPart.getPartProperties();
	}

	@Override //from IEditorPartHost
	public void setToolbarParent(final Composite toolbarParent) {
		innerEditorPart.setToolbarParent(toolbarParent);
	}

	@Override //from IEditorPartHost
	public Composite getContainer() {
		return innerEditorPart.getContainer();
	}

}
