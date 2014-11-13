package org.embl.cca.dviewer.ui.editors;

import java.util.Map;

import org.dawnsci.common.widgets.editor.ITitledEditor;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace.DownsampleType;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.embl.cca.dviewer.ui.editors.utils.Point2DD;
import org.embl.cca.utils.general.ISomethingChangeListener;

public class DViewerImageViewPart extends ViewPart implements IViewPartHost, ITitledEditor,
	IDViewerImageControllable {

	public static final String ID = "org.embl.cca.dviewer.ui.editors.DViewerImageViewPart";

	final DViewerImageEditorAndViewPart innerViewPart;

	public DViewerImageViewPart(final PlotType defaultPlotType, final DViewerListenerManager listenerManager) {
		innerViewPart = new DViewerImageEditorAndViewPart(this, defaultPlotType, listenerManager);
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

	@Override //from ITitledEditor
	public void setPartTitle(final String name) {
		innerViewPart.setPartTitle(name);
	}

	//from IShowEditorInput, comfortable method in ViewPart as well
	public void showEditorInput(final IEditorInput editorInput) {
		innerViewPart.showEditorInput(editorInput);
		
	}

	@Override //from IViewPart/IWorkbenchPart
	public void addPropertyListener(final IPropertyListener listener) {
		super.addPropertyListener(listener);
	}

	@Override //from IViewPart/IWorkbenchPart
	public void createPartControl(final Composite parent) {
		innerViewPart.createPartControl(parent);
	}

	@Override //from IViewPart/IWorkbenchPart
	public void dispose() {
		innerViewPart.dispose();
	}

	@Override //from IViewPart/IWorkbenchPart
	public IWorkbenchPartSite getSite() {
		return innerViewPart.getSite();
	}

	@Override //from IViewPart/IWorkbenchPart
	public Image getTitleImage() {
		return innerViewPart.getTitleImage();
	}

	@Override //from IViewPart/IWorkbenchPart
	public String getTitleToolTip() {
		return innerViewPart.getTitleToolTip();
	}

	@Override //from IViewPart/IWorkbenchPart
	public void removePropertyListener(final IPropertyListener listener) {
		innerViewPart.removePropertyListener(listener);
	}

	@Override //from IViewPart/IWorkbenchPart
	public void setFocus() {
		innerViewPart.setFocus();
	}

	@Override //from IViewPart/IWorkbenchPart/IAdaptable
	public Object getAdapter(@SuppressWarnings("rawtypes") final Class adapter) {
		return innerViewPart.getAdapter(adapter);
	}

	//from IEditorPart, comfortable method in ViewPart as well
	public IEditorInput getEditorInput() {
		return innerViewPart.getEditorInput();
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
	public void setInput(final IEditorInput input) {
		innerViewPart.setInput(input);
	}

	//from EditorPart, comfortable method in ViewPart as well
	public void setInputWithNotify(final IEditorInput input) {
		innerViewPart.setInputWithNotify(input);
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

	@Override //from IViewPartHost (originated from IWorkbenchPart2)
	public String getPartName() {
		return super.getPartName();
	}

	@Override //from IViewPartHost (originated from WorkbenchPart)
	public void setPartName(final String partName) {
		super.setPartName(partName);
	}

	@Override //from IViewPartHost (originated from IViewPart/IWorkbenchPart)
	public String getTitle() {
		return super.getTitle();
	}

	@Override //from IViewPartHost (originated from WorkbenchPart)
	@Deprecated
	public void setTitle(String title) {
		super.setTitle(title);
	}

	@Override //from IViewPartHost (originated from IWorkbenchPart2)
	public String getContentDescription() {
		return super.getContentDescription();
	}

	@Override //from IViewPartHost (originated from WorkbenchPart)
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

	@Override //from IViewPartHost
	public void setToolbarParent(final Composite toolbarParent) {
		innerViewPart.setToolbarParent(toolbarParent);
	}

	@Override //from IViewPartHost
	public Composite getContainer() {
		return innerViewPart.getContainer();
	}

}
