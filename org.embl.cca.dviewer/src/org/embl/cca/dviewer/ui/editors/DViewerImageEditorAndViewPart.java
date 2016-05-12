package org.embl.cca.dviewer.ui.editors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

import org.dawb.common.ui.menu.CheckableActionGroup;
import org.dawb.common.ui.menu.MenuAction;
import org.dawb.common.ui.util.GridUtils;
import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawb.common.util.io.FileUtils;
import org.dawb.workbench.ui.editors.preference.EditorConstants;
import org.dawb.workbench.ui.editors.zip.ZipUtils;
import org.dawnsci.common.widgets.editor.ITitledEditor;
import org.dawnsci.plotting.AbstractPlottingSystem;
import org.dawnsci.plotting.tools.InfoPixelLabelProvider;
import org.dawnsci.plotting.tools.preference.InfoPixelConstants;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.AssertionFailedException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.dataset.IDatasetMathsService;
import org.eclipse.dawnsci.analysis.api.io.IFileSaver;
import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
import org.eclipse.dawnsci.plotting.api.axis.IPositionListener;
import org.eclipse.dawnsci.plotting.api.axis.PositionEvent;
import org.eclipse.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace.DownsampleType;
import org.eclipse.dawnsci.plotting.api.trace.ITrace;
import org.eclipse.dawnsci.slicing.api.system.AxisType;
import org.eclipse.dawnsci.slicing.api.system.DimsData;
import org.eclipse.dawnsci.slicing.api.system.DimsDataList;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IActionBars2;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.ISaveablePart;
import org.eclipse.ui.IShowEditorInput;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.WorkbenchPart;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.embl.cca.dviewer.DViewerActivator;
import org.embl.cca.dviewer.DViewerStartup;
import org.embl.cca.dviewer.ui.editors.preference.DViewerEditorConstants;
import org.embl.cca.dviewer.ui.editors.preference.EditorPreferenceInitializer;
import org.embl.cca.dviewer.ui.editors.utils.PHA;
import org.embl.cca.dviewer.ui.editors.utils.Point2DD;
import org.embl.cca.dviewer.ui.views.DViewerControlsView;
import org.embl.cca.dviewer.ui.views.DViewerImageView;
import org.embl.cca.utils.datahandling.EFile;
import org.embl.cca.utils.datahandling.MemoryDatasetEditorInput;
import org.embl.cca.utils.datahandling.file.SmarterJavaImageSaver;
import org.embl.cca.utils.datahandling.file.SmarterJavaImageScaledSaver;
import org.embl.cca.utils.datahandling.file.XDSASCIIHKLReader;
import org.embl.cca.utils.datahandling.file.XDSASCIIHKLRecord;
import org.embl.cca.utils.datahandling.text.DecimalPaddedFormat;
import org.embl.cca.utils.datahandling.text.StringUtils;
import org.embl.cca.utils.errorhandling.ExceptionUtils;
import org.embl.cca.utils.extension.CommonExtension;
import org.embl.cca.utils.general.ISomethingChangeListener;
import org.embl.cca.utils.general.SomethingChangeEvent;
import org.embl.cca.utils.threading.CommonThreading;
import org.embl.cca.utils.ui.nebula.AnnotationEmbl.CursorLineStyleEmbl;
import org.embl.cca.utils.ui.nebula.AnnotationWrapperEmbl;
import org.embl.cca.utils.ui.widget.SaveFileDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;

//IEditorPart = IWorkbenchPart + ISaveablePart + PROP_DIRTY + PROP_INPUT + getEditorInput + getEditorSite + init(IEditorSite, IEditorInput)
//IReusableEditor = IEditorPart + setInput(IEditorInput)
//ISaveablePart = PROP_DIRTY + doSave(IProgressMonitor) + doSaveAs + isDirty + isSaveAsAllowed + isSaveOnCloseNeeded
//ITitledEditor = setPartTitle(String)
//IViewPart = IWorkbenchPart + IPersistable + getViewSite + init(IViewSite) + init(IViewSite, IMemento) + saveState(IMemento)
//IPersistable = saveState(IMemento)
//WorkbenchPart = EventManager + IWorkbenchPart3 + IExecutableExtension + IWorkbenchPartOrientation + ...
//IWorkbenchPart = IAdaptable + PROP_TITLE + addPropertyListener(IPropertyListener) + createPartControl(Composite) + dispose
//  + getSite + getTitle + getTitleImage + getTitleToolTip  + removePropertyListener(IPropertyListener) + setFocus
public class DViewerImageEditorAndViewPart extends WorkbenchPart
	implements IEditorPart, IReusableEditor, ITitledEditor, ISaveablePart, IShowEditorInput /*, ISlicablePlottingPart, ISelectedPlotting*/,
	IViewPart, IDViewerImageControllable {

	public static final String ID = "org.embl.cca.dviewer.ui.editors.DViewerImageEditorAndViewPart";

	private static final Logger logger = LoggerFactory.getLogger(DViewerImageEditorAndViewPart.class);

	//Support for IEditorPart and IViewPart, beginning here
	protected final IWorkbenchPart classRole; //IEditorPartHost or IViewPartHost

	//These events are managed by host: PROP_TITLE, PROP_CONTENT_DESCRIPTION, PROP_PART_NAME
	protected final IPropertyListener hostPropertyListener = new IPropertyListener() {
		public void propertyChanged(final Object source, final int propId) {
			firePropertyChange(propId);
		}
	};
	//Support for IEditorPart and IViewPart, ending here

	protected final IPropertyListener inputListener = new IPropertyListener() {
		public void propertyChanged(final Object source, final int propId) {
			if (propId == IWorkbenchPartConstants.PROP_INPUT) {
				editorInputChanged();
			}
		}
	};

	protected final IPlottingSystem<Composite> plottingSystem;
	protected final PlotType defaultPlotType;
//	/**
//	 * Required for createPlotSeparateAxes, which is required if this class
//	 * implements ISlicablePlottingPart, and receiving 1D selection.
//	 * Now it does not, and not planned.
//	 */
//	protected final Map<Integer, IAxis> axisMap;
	protected Composite toolbarParent;
	protected ActionBarWrapper wrapper;
	protected IImageTrace imageTrace;
	protected boolean dirty;

	protected SaveFileDialog saveAsDialog = null;

//	/**
//	 * Required if this class implements ISlicablePlottingPart.
//	 * Now it does not, and not planned.
//	 */
//	protected final PlotJob plotJob;

	/**
	 * The original dataset received in IEditorInput.
	 */
	protected IDataset dataSetOriginal;
	/**
	 * The dataset created by applied PHA on dataSetOriginal.
	 */
	protected IDataset dataSetPHA;
	/**
	 * The radius of PHA applied on dataSetOriginal to create dataSetPHA.
	 * This value is valid only if dataSetPHA is not null.
	 */
	protected int dataSetPHARadius;
	protected final Boolean dataSetLock = new Boolean(true); //This is a lock, has no value
	protected Action phaAction;
	protected Action openDViewerViewAction;
	protected Action openDViewerControlsAction;
	/**
	 * The current radius of PHA which is applied on dataSetOriginal when requested.
	 */
	protected int phaRadius;
	/**
	 * The job to apply PHA on dataSetOriginal, resulting in dataSetPHA.
	 */
	protected final CreatePHAPlotJob createPHAPlotJob;

	protected DownsampleType requiredDownsampleType = null;

	protected EFile hklFile;
	protected int hMin, hSup, hRangeMin, hRangeMax;
	protected int kMin, kSup, kRangeMin, kRangeMax;
	protected int lMin, lSup, lRangeMin, lRangeMax;

	/**
	 * The current mouse position.
	 */
	protected Point2DD mousePos;
	final protected InfoPixelLabelProvider iPLPResolution = new InfoPixelLabelProvider(null, 8);
	final protected DecimalPaddedFormat statusXPosFormat = new DecimalPaddedFormat("#0.0##");
	final protected DecimalPaddedFormat statusYPosFormat = new DecimalPaddedFormat("#0.0##");
	final protected DecimalPaddedFormat statusDataFormat = new DecimalPaddedFormat(org.dawnsci.plotting.tools.Activator.getPlottingPreferenceStore().getString(InfoPixelConstants.DATA_FORMAT));
	final protected String STATUS_FIELD_SEPARATOR = ", ";

	/**
	 * The container widget.
	 */
	protected Composite container;

	protected final DViewerListenerManager listenerManager;

	protected class CreatePHAPlotJob extends Job {
		protected IDataset dataSet;
		protected boolean rehistogram;
		protected int phaRadius;

		public CreatePHAPlotJob() {
			super("Plot update using PHA");
			setUser(false);
			setSystem(false);
			setPriority(Job.SHORT);
		}

		public void reschedule(final IDataset dataSet, final boolean rehistogram, final int phaRadius) {
			cancel();
			this.dataSet = dataSet;
			this.rehistogram = rehistogram;
			this.phaRadius = phaRadius;
			schedule();
		}

		public IStatus run(final IProgressMonitor monitor) {
			final IProgressMonitor thisMonitor = monitor == null ? new NullProgressMonitor() : monitor;
			thisMonitor.beginTask("Updating plot using PHA", 100);
			try {
				final PHA pha = new PHA(phaRadius);
				final IDataset dataSetPHAThis = pha.applyPHA(dataSet,
						PHA.getDataSetRectangle(dataSet), new SubProgressMonitor(thisMonitor, 100));
				IDataset dataSetOriginalThis = null;
				if( dataSetPHAThis != null ) { //!monitor.isCanceled
//					final IDataset dataSetAppliedPHA = pha.calculateGaussKernel(pha.getRadius()); //TODO only for testing
					synchronized (dataSetLock) {
						if( dataSetOriginal == dataSet ) {
							dataSetPHA = dataSetPHAThis;
							dataSetPHARadius = phaRadius;
							dataSetOriginalThis = dataSetOriginal;
						} else
							thisMonitor.setCanceled(true);
					}
				}
				if( !thisMonitor.isCanceled() ) {
					if( phaAction.isChecked() )
						createPlot(dataSetPHAThis, rehistogram, thisMonitor);
					else if( !isPlotReady() ) //phaAction changed while plot not ready
						createPlot(dataSetOriginalThis, rehistogram, thisMonitor);
					return Status.OK_STATUS;
				} else {
					onPhaPlotIsCancelled(pha.getRadius());
					return Status.CANCEL_STATUS;
				}
			} finally {
				thisMonitor.done();
			}
		}
	}

	public DViewerImageEditorAndViewPart(final IWorkbenchPart classRole, final PlotType defaultPlotType, final DViewerListenerManager listenerManager) {
		
//		this.axisMap = new HashMap<Integer, IAxis>(4);
//		this.plotJob = new PlotJob();
////		this.lock    = new ReentrantLock();
		if( !(IEditorPartHost.class.isAssignableFrom(classRole.getClass())
				|| IViewPartHost.class.isAssignableFrom(classRole.getClass())) )
			throw new IllegalArgumentException("classRole must be IEditorPartHost or IViewPartHost");
		this.classRole = classRole;
		this.listenerManager = listenerManager;
		this.defaultPlotType= defaultPlotType;
		this.createPHAPlotJob = new CreatePHAPlotJob();
		this.mousePos = new Point2DD(0, 0);
		this.dirty = false;
		final IPlottingSystem<Composite> pS;
		try {
			pS = PlottingFactory.createPlottingSystem();
		} catch (final Exception e) {
//			ExceptionUtils.logError(logger, "Cannot locate any plotting systems", e, this);
			throw new RuntimeException("Cannot locate any plotting systems", e);
		}
		this.plottingSystem = pS;
		addPropertyListener(inputListener);
		classRole.addPropertyListener(hostPropertyListener);
	}

	public void setToolbarParent(final Composite toolbarParent) {
		if( container != null )
			throw new IllegalStateException("This part is already created");
		this.toolbarParent = toolbarParent;
	}

	@Override //from IEditorPart
	public void init(final IEditorSite site, final IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
	}

	@Override //from IViewPart
	public void init(IViewSite site) throws PartInitException {
		setSite(site);
	}

	@Override //from IViewPart
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		init(site);
	}

	@Override //from IViewPart
	public void saveState(IMemento memento) {
	}

	@Override //from WorkbenchPart
	public void dispose() {
		super.dispose();
	}

	@Override //from IShowEditorInput
	public void showEditorInput(final IEditorInput editorInput) {
		setInput(editorInput);
	}

	@Override //from IEditorPart
	public IEditorInput getEditorInput() {
		if( IEditorPartHost.class.isAssignableFrom(classRole.getClass()) )
			return ((IEditorPartHost)classRole).getEditorInput();
		else if( IViewPartHost.class.isAssignableFrom(classRole.getClass()) )
			return ((IViewPartHost)classRole).getEditorInput();
		return null;
	}

	@Override //from IReusableEditor
	public void setInput(final IEditorInput input) {
		if( IEditorPartHost.class.isAssignableFrom(classRole.getClass()) )
			((IEditorPartHost)classRole).setInput(input);
		else if( IViewPartHost.class.isAssignableFrom(classRole.getClass()) )
			((IViewPartHost)classRole).setInput(input);
	}

	/**
	 * Sets the input to this editor and fires a PROP_INPUT property change if
	 * the input has changed. This is the convenience method implementation.
	 * 
	 * <p>
	 * Note that firing a property change may cause other objects to reach back
	 * and invoke methods on the editor. Care should be taken not to call this
	 * method until the editor has fully updated its internal state to reflect
	 * the new input.
	 * </p>
	 * 
	 * @since 3.2
	 * 
	 * @param input
	 *            the editor input
	 */
	public void setInputWithNotify(final IEditorInput input) {
		if( IEditorPartHost.class.isAssignableFrom(classRole.getClass()) )
			((IEditorPartHost)classRole).setInputWithNotify(input);
		else if( IViewPartHost.class.isAssignableFrom(classRole.getClass()) )
			((IViewPartHost)classRole).setInputWithNotify(input);
	}

	@Override //from IEditorPart
	public IEditorSite getEditorSite() {
		return (IEditorSite) getSite();
	}

	@Override //from IViewPart
	public IViewSite getViewSite() {
		return (IViewSite) getSite();
	}

	@Override //from IWorkbenchPart
	public String getTitleToolTip() {
		if (getEditorInput() == null) {
			return super.getTitleToolTip();
		}
		return getEditorInput().getToolTipText();
	}

	@Override //from WorkbenchPart
	public void setInitializationData(final IConfigurationElement cfig,
			final String propertyName, final Object data) {
		super.setInitializationData(cfig, propertyName, data);
	}

	@Override //from WorkbenchPart
	@Deprecated
	/**
	 * @deprecated This method is not supported. Call classRole.getPartName() instead.
	 */
	public String getPartName() {
		throw new UnsupportedOperationException("Call classRole.getPartName() instead");
	}

	@Override //from WorkbenchPart
	@Deprecated
	/**
	 * @deprecated This method is not supported. Call classRole.setPartName(String) instead.
	 */
	protected void setPartName(final String partName) {
		throw new UnsupportedOperationException("Call classRole.setPartName(String) instead");
	}

	@Override //from IViewPart/IWorkbenchPart
	@Deprecated
	/**
	 * @deprecated This method is not supported. Call classRole.getTitle() instead.
	 */
	public String getTitle() {
		throw new UnsupportedOperationException("Call classRole.getTitle() instead");
	}

	@Override //from WorkbenchPart
	@Deprecated
	/**
	 * @deprecated This method is not supported. Call classRole.setTitle(String) instead.
	 */
	public void setTitle(String title) {
		throw new UnsupportedOperationException("Call classRole.setTitle(String) instead");
	}

	@Override //from WorkbenchPart
	@Deprecated
	/**
	 * @deprecated This method is not supported. Call classRole.getContentDescription() instead.
	 */
	public String getContentDescription() {
		throw new UnsupportedOperationException("Call classRole.getContentDescription() instead");
	}

	@Override //from WorkbenchPart
	@Deprecated
	/**
	 * @deprecated This method is not supported. Call classRole.setContentDescription(String) instead.
	 */
	protected void setContentDescription(final String description) {
		throw new UnsupportedOperationException("Call classRole.setContentDescription(String) instead");
	}

	@Override //from ITitledEditor
	public void setPartTitle(final String name) {
		if( IEditorPartHost.class.isAssignableFrom(classRole.getClass()) )
			((IEditorPartHost)classRole).setPartName(name);
		else if( IViewPartHost.class.isAssignableFrom(classRole.getClass()) )
			((IViewPartHost)classRole).setPartName(name);
	}

	//could be from ITitledEditor
	public String getPartTitle() {
		if( IEditorPartHost.class.isAssignableFrom(classRole.getClass()) )
			return ((IEditorPartHost)classRole).getPartName();
		else if( IViewPartHost.class.isAssignableFrom(classRole.getClass()) )
			return ((IViewPartHost)classRole).getPartName();
		return StringUtils.EMPTY_STRING;
	}

	/**
	 * Checks that the given site is valid for this type of part. The site for
	 * an editor must be an <code>IEditorSite</code> or <code>IViewSite</code>,
	 * depending on classRole.
	 * 
	 * @param site
	 *            the site to check
	 * @since 3.1
	 */
	@Override //from IEditorPart, IViewPart
	protected void checkSite(final IWorkbenchPartSite site) {
		super.checkSite(site);
		if( IEditorPartHost.class.isAssignableFrom(classRole.getClass()) )
			Assert.isTrue(site instanceof IEditorSite,
					"The site for an editor must be an IEditorSite"); //$NON-NLS-1$
		else if( IViewPartHost.class.isAssignableFrom(classRole.getClass()) )
			Assert.isTrue(site instanceof IViewSite, "The site for a view must be an IViewSite"); //$NON-NLS-1$
	}

	@Override //from ISaveablePart
	public boolean isSaveOnCloseNeeded() {
		return isDirty();
	}

	@Override //from ISaveablePart
	public boolean isDirty() {
		return dirty;
	}

	@Override //from ISaveablePart
	public boolean isSaveAsAllowed() {
		return true;
	}

	@Override //from ISaveablePart
	public void doSave(final IProgressMonitor monitor) {
		if( isDirty() ) {
			; //TODO Currently the plot can not be changed, thus it can not be dirty
			setDirty(false);
		}
	}

	@Override //from ISaveablePart
	public void doSaveAs() {
		do {
			saveAs(imageTrace.getData(), false, 0, 0);
		} while( false );
	}

	@Override //from WorkbenchPart
	public void setFocus() {
		if (plottingSystem!=null) {
			plottingSystem.setFocus();
		}
	}

	@Override //from WorkbenchPart
	public <T> T getAdapter(final Class<T> clazz) {
		if (IToolPageSystem.class.equals(clazz) || IPlottingSystem.class.equals(clazz)) { //Mandatory for tools
			//IPlottingSystem and IToolPageSystem are assumed to be implemented
			//at the same time, as done in AbstractPlottingSystem.
			//Thus the following cast is correct.
			return clazz.cast(getPlottingSystem());
//		} else if (clazz == Page.class) {
//			return import org.dawb.workbench.ui.views.PlotDataPage.getPageFor(this); //Mandatory for PlotDataPage, PlotDataView
//		} else if (clazz == ISliceSystem.class) {
//			return getSliceComponent();
//		} else if (clazz == IVariableManager.class) {
//			return getDataSetComponent();
		}
		return super.getAdapter(clazz);
	}

	protected void setDirty(final boolean value) {
		if( dirty != value ) {
			dirty = value;
			firePropertyChange(IEditorPart.PROP_DIRTY);
		}
	}

	public IPlottingSystem<Composite> getPlottingSystem() {
		return plottingSystem;
	}

	protected void editorInputChanged() {
		if( getEditorInput() instanceof MemoryDatasetEditorInput ) {
			final MemoryDatasetEditorInput input = (MemoryDatasetEditorInput)getEditorInput();
			if( input.getDataset().getSize() > 0 ) {
				createPlotNewDataset(input.getDataset(), input.isNewInput(), input.getMonitor());
			}
		}
	}

	/**
	 * Returns the composite control containing this editor's content.
	 * This should be used as the parent when creating controls for the
	 * content.
	 * <p>
	 * Warning: Clients should not assume that the container is any particular
	 * subclass of Composite. The actual class used may change in order to
	 * improve the look and feel of editors. Any code making
	 * assumptions on the particular subclass would thus be broken.
	 * </p>
	 * <p>
	 * Subclasses should not override this method
	 * </p>
	 * 
	 * @return the composite, or <code>null</code> if
	 *         <code>createPartControl</code> has not been called yet
	 */
	protected Composite getContainer() {
		return container;
	}

	
	protected void saveAs(IDataset ds, boolean autoscale, double min, double max) {
		final Shell shell = getSite().getWorkbenchWindow().getShell();
		Assert.isNotNull(shell, "Environment error: can not find shell");
		if( saveAsDialog == null ) {
			saveAsDialog = new SaveFileDialog(shell, DViewerActivator.getLocalPreferenceStore(), DViewerImageEditorPart.ID);
			saveAsDialog.setText("Save Image As");
			saveAsDialog.setOverwrite(true);
			saveAsDialog.addWritableImageFilters();
			saveAsDialog.addFilter("All", Arrays.asList(new String[]{"*.*"}));
			saveAsDialog.setFileName(ds.getName());
		}
		do {
			final String newFilePath = saveAsDialog.open();
			if( newFilePath == null )
				break;
			final File newFile = new File(newFilePath);
			final String fileType = new Path(newFilePath).getFileExtension();
			if( Arrays.asList(ImageIO.getWriterFormatNames()).indexOf(fileType) < 0 ) {
				MessageDialog.openError(shell,
						"File Saving Error", new StringBuilder("No writer for \"")
					.append(newFilePath).append("\" of type \"").append(fileType).append("\".\n\nPlease specify a different file type.").toString());
				continue;
			}
			int numBits = 16; //TODO Should be from dialog...
			boolean asUnsigned = true; //TODO Should be from dialog...
			DataHolder dh = new DataHolder();
			dh.addDataset("image", ds);
			final IFileSaver fileSaver = autoscale ?
				new SmarterJavaImageScaledSaver(newFilePath, fileType, numBits, asUnsigned) :
				new SmarterJavaImageSaver(newFilePath, fileType, numBits, asUnsigned);
			try {
				fileSaver.saveFile(dh);
				break;
			} catch (final ScanFileHolderException|UnsupportedOperationException|IllegalArgumentException e) {
				final StringBuilder sb = ExceptionUtils.makeErrorMessage(
					new StringBuilder().append("Could not save the image as '")
					.append(newFile.getAbsolutePath())
					.append("', because an error occured.\nThe error message: "), e, this);
				if( e instanceof IllegalArgumentException) {
					logger.info(sb.toString());
					/*final boolean ovr = */MessageDialog.open(MessageDialog.ERROR, shell,
						"File Saving Error", sb.toString(), MessageDialog.NONE );
//					MessageDialog.openError(shell, "File Saving Error", sb.toString()); //shorter, but no user response
					//TODO recover if chosen so
					continue;
				} else {
					ExceptionUtils.logError(logger, sb.toString());
					MessageDialog.open(MessageDialog.ERROR, shell,
						"File Saving Error", sb.toString(), MessageDialog.NONE );
				}
				break;
			}
		} while(true);
	}

	public void doSaveAsScaled() {
		do {
//			System.out.println("IT.Min and IT.max=" + imageTrace.getMin().doubleValue() + ", " + imageTrace.getMax().doubleValue());
			saveAs(imageTrace.getData(), true, imageTrace.getMin().doubleValue(), imageTrace.getMax().doubleValue());
		} while( false );
	}

	public void doSaveAsOriginal() {
		do {
//			saveAs(originalSet, false, 0, 0);
		} while( false );
	}

	public void doSaveAsScaledOriginal() {
		do {
//			System.out.println("Min and max=" + originalSet.min().doubleValue() + ", " + originalSet.max().doubleValue());
//			saveAs(originalSet, true, originalSet.min().doubleValue(), originalSet.max().doubleValue());
		} while( false );
	}

	public void setToolbarsVisible(boolean isVisible) {
		wrapper.setVisible(isVisible);
	}

	public InputStream getDeepestInputStreamForFile(final File file, final String fileExtension) throws FileNotFoundException  {
		InputStream fis = new FileInputStream(file);
		String fileName = file.getName();
		do {
			final String ext = FileUtils.getFileExtension(fileName);
			if( ext.equals(fileExtension) )
				break;
			if( ZipUtils.isExtensionSupported(ext)) {
				try {
					fis = ZipUtils.getStreamForStream(fis, ext);
				} catch (final Exception e) {
					throw new UnsupportedOperationException(e);
				}
			} else //else if() {} //Here could add other streamers
				throw new UnsupportedOperationException("Can not handle the extension: " + ext);
			fileName = fileName.substring(0, fileName.length() - ext.length() - 1);
		} while( true );
		return fis;
	}

	public InputStream getDeepestInputStreamForFile(final String fileName, final String fileExtension) throws FileNotFoundException  {
		return getDeepestInputStreamForFile(new File(fileName), fileExtension);
	}

	@Override //from WorkbenchPart
	public void createPartControl(final Composite parent) { //By PlotDataEditor
		container = new Composite(parent, SWT.NONE);
		getContainer().setLayout(new GridLayout(1, false));
//		getContainer().setBackground(org.eclipse.swt.widgets.Display.getCurrent().getSystemColor(org.eclipse.swt.SWT.COLOR_RED));
		if (getContainer().getLayout() instanceof GridLayout) //For sure
			GridUtils.removeMargins(getContainer());

		DViewerActivator.getLocalPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (DViewerEditorConstants.PREFERENCE_DOWNSAMPLING_TYPE.equals(event.getProperty())) {
					setDownsampleType(null, DownsampleType.values()[ Integer.valueOf((String)event.getNewValue()) ]);
				} else if (DViewerEditorConstants.PREFERENCE_APPLY_PHA.equals(event.getProperty())) {
					setPha(null, (Boolean)event.getNewValue());
				} else if (DViewerEditorConstants.PREFERENCE_PHA_RADIUS.equals(event.getProperty())) {
					setPhaRadius(null, (Integer)event.getNewValue());
				}
			}
		}); 

		final IActionBars bars;
		if( IEditorPartHost.class.isAssignableFrom(classRole.getClass()) )
			bars = getEditorSite().getActionBars();
		else //ViewPart does not give IActionBars2, only IActionBars
			bars = null;

		wrapper = ActionBarWrapper.createActionBars(toolbarParent != null ? toolbarParent : getContainer(),(IActionBars2)bars);

		final Composite plot  = new Composite(getContainer(), SWT.NONE);
//		plot.setBackground(org.eclipse.swt.widgets.Display.getCurrent().getSystemColor(org.eclipse.swt.SWT.COLOR_CYAN));
		plot.setLayout(new FillLayout()); //layout must be FillLayout for showing plotting
		plot.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true)); //grabExcessVerticalSpace must be true for showing plotting
		plottingSystem.createPlotPart(plot, getEditorInput().getName(), wrapper, defaultPlotType, classRole);

//		final IPlotActionSystem plotActionSystem = plottingSystem.getPlotActionSystem();
//		if(!(this instanceof ISlicablePlottingPart)) //Hyper2d requires slicing, more exactly axes
//			plotActionSystem.remove(new StringBuilder(ToolPageRole.ROLE_2D.getId()).append('/').append("org.dawnsci.plotting.tools.plotting_tool_hyper2d").toString());
		
		final IToolBarManager toolMan = wrapper.getToolBarManager();

//		this.phaRadius = (Integer)EditorPreferenceHelper.getStoreValue(DViewerActivator.getLocalPreferenceStore(), DViewerEditorConstants.PREFERENCE_PHA_RADIUS);
		phaRadius = (int)EditorPreferenceInitializer.PhaRadius.getValue();

		phaAction = new Action(PHA.featureShortName, IAction.AS_CHECK_BOX ) {
			@Override
			public void run() {
				onPhaStateSelected();
			}
		};
		phaAction.setId(getClass().getName() + "." + PHA.featureIdentifierName);
		phaAction.setText("Apply " + PHA.featureShortName);
		phaAction.setToolTipText("Apply " + PHA.featureFullName + " (" + PHA.featureShortName + ") on the image");
		phaAction.setImageDescriptor(DViewerActivator.getImageDescriptor("/icons/pha.png"));
//		phaAction.setChecked((Boolean)EditorPreferenceHelper.getStoreValue(DViewerActivator.getLocalPreferenceStore(), DViewerEditorConstants.PREFERENCE_APPLY_PHA));
		phaAction.setChecked((boolean)EditorPreferenceInitializer.ApplyPha.getValue());
		toolMan.insertAfter( "org.dawb.workbench.plotting.histo", phaAction );

		final String openDViewerControlsAfter;
		if( IEditorPartHost.class.isAssignableFrom(classRole.getClass()) ) {
			final String dViewerViewFeatureName = "dViewerView";
			final String dViewerViewFeatureFullName = "dViewer View";
			final String dViewerViewFeatureIdentifierName = dViewerViewFeatureName.toLowerCase();
			openDViewerViewAction = new Action(dViewerViewFeatureName, IAction.AS_PUSH_BUTTON ) {
				@Override
				public void run() {
					requestDViewerView();
				}
			};
			openDViewerViewAction.setId(getClass().getName() + "." + dViewerViewFeatureIdentifierName);
			openDViewerViewAction.setText("Show " + dViewerViewFeatureName);
			openDViewerViewAction.setToolTipText("Show " + dViewerViewFeatureFullName);
			openDViewerViewAction.setImageDescriptor(DViewerActivator.getImageDescriptor("/icons/dViewer-view-16x16-icon.png"));
			toolMan.insertAfter( phaAction.getId(), openDViewerViewAction );
			openDViewerControlsAfter = openDViewerViewAction.getId();
		} else
			openDViewerControlsAfter = phaAction.getId();

		final String dViewerControlsFeatureName = "dViewerControls";
		final String dViewerControlsFeatureFullName = "dViewer Controls View";
		final String dViewerControlsFeatureIdentifierName = dViewerControlsFeatureName.toLowerCase();
		openDViewerControlsAction = new Action(dViewerControlsFeatureName, IAction.AS_PUSH_BUTTON ) {
			@Override
			public void run() {
				requestDViewerControls();
			}
		};
		openDViewerControlsAction.setId(getClass().getName() + "." + dViewerControlsFeatureIdentifierName);
		openDViewerControlsAction.setText("Show " + dViewerControlsFeatureName);
		openDViewerControlsAction.setToolTipText("Show " + dViewerControlsFeatureFullName);
		openDViewerControlsAction.setImageDescriptor(DViewerActivator.getImageDescriptor("/icons/dViewer-controls-16x16-icon.png"));
		toolMan.insertAfter( openDViewerControlsAfter, openDViewerControlsAction );

//TODO temporary code here, to develop HKL loader
//		hklAction = new Action(hklFeatureName, IAction.AS_PUSH_BUTTON ) {
//			@Override
//			public void run() {
//				//TODO Developing HKL loader here, but obviously it will be initiated from somewhere else
//				try {
//					final XDSIntegrationReader xdsIR = new XDSIntegrationReader(getDeepestInputStreamForFile("/home/naray/bigstorage/naray/STAC.test/xds_t1w1_run1_1/INTEGRATE.HKL", "HKL"));
//					try {
//						final List<XDSHKLRecord> records = xdsIR.readAllHKLRecords();
//						for( final XDSHKLRecord record : records) {
//							final AnnotationWrapperEmbl ann1 = (AnnotationWrapperEmbl)AnnotationWrapperEmbl.replaceCreateAnnotation(plottingSystem, "" + record.getH() + "," + record.getK() + "," + record.getL());
//							ann1.setCursorLineStyle(CursorLineStyleEmbl.NOCURSOR);
//							ann1.setShowArrow(false);
//							ann1.setShowPosition(false);
//							ann1.setShowInfo(false);
//							ann1.setdxdy(0, -5);
//							ann1.setLocation(record.getX(), record.getY());
//							plottingSystem.addAnnotation(ann1);
//						}
//					} finally {
//						try {
//							xdsIR.close();
//						} catch (final IOException e) {
//						}
//					}
//				} catch (final Exception e1) { //From constructing, or IOException
//					e1.printStackTrace();
//				}
//			}
//		};
//		hklAction.setId(getClass().getName() + "." + hklFeatureIdentifierName);
//		hklAction.setText("Apply " + hklFeatureName);
//		hklAction.setToolTipText("Apply " + hklFeatureFullName + " (" + hklFeatureName + ") on the image");
//		hklAction.setImageDescriptor(DViewerActivator.getImageDescriptor("/icons/apply.gif"));
//		toolMan.insertAfter( phaAction.getId(), hklAction );

		//TODO temporary code here, to develop HKL loader
//		final String openViewFeatureName = "OpenView";
//		final String openViewFeatureFullName = "Load and apply OpenView";
//		final String openViewFeatureIdentifierName = openViewFeatureName.toLowerCase();
//		openViewAction = new Action(openViewFeatureName, IAction.AS_PUSH_BUTTON ) {
//			@Override
//			public void run() {
//			}
//		};
//		openViewAction.setId(getClass().getName() + "." + openViewFeatureIdentifierName);
//		openViewAction.setText("Apply " + openViewFeatureName);
//		openViewAction.setToolTipText("Apply " + openViewFeatureFullName + " (" + openViewFeatureName + ") on the image");
//		openViewAction.setImageDescriptor(DViewerActivator.getImageDescriptor("/icons/apply.gif"));
//		toolMan.insertAfter( hklAction.getId(), openViewAction );

		IPreferenceStore ip = new ScopedPreferenceStore(InstanceScope.INSTANCE, "duk.ac.diamond.sda.polling"); //TODO
		ip.getString("xpathPreference");

		MenuAction dviewerDownsamplingAction = new MenuAction("Downsampling type");
		dviewerDownsamplingAction.setId(getClass().getName()+".downsamplingType");
//		dviewerDownsamplingAction.setImageDescriptor(Activator.getImageDescriptor("icons/origins.png"));
		CheckableActionGroup group = new CheckableActionGroup();
//		DownsampleType downsampleType = (DownsampleType.values()[ (Integer)EditorPreferenceHelper.getStoreValue(DViewerActivator.getLocalPreferenceStore(), DViewerEditorConstants.PREFERENCE_DOWNSAMPLING_TYPE) ]);
		DownsampleType downsampleType = DownsampleType.values()[ Integer.valueOf((String)EditorPreferenceInitializer.DownsamplingType.getValue()) ];
		IAction selectedAction = null;

		final String dTypeNames[] = DViewerEditorConstants.getSortedDownsampleTypeNames();
		for (final String dTypeName : dTypeNames) {
			final DownsampleType dType = DownsampleType.valueOf(dTypeName);
			final IAction action = new Action(dType.getLabel(),
					IAction.AS_CHECK_BOX) {
				public void run() {
					//We do not store this selection neither as last nor as default (currently only default exists anyway)
//					Activator.getDefault().getPreferenceStore().setValue(EditorConstants.PREFERENCE_DOWNSAMPLING_TYPE, dType.getLabel());
					setDownsampleType(null, dType);
//					setChecked(true);
				}
			};
			dviewerDownsamplingAction.add(action);
			group.add(action);

			if (downsampleType == dType)
				selectedAction = action;
		}

		if (selectedAction != null)
			selectedAction.setChecked(true);

		wrapper.getMenuManager().add(dviewerDownsamplingAction);
		wrapper.getMenuManager().insertAfter(dviewerDownsamplingAction.getId(),
				new Separator(dviewerDownsamplingAction.getId() + ".group"));

		if (wrapper != null)
			wrapper.update(true);

		((AbstractPlottingSystem<?>)plottingSystem).addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent event) {
				if (IPlottingSystem.RESCALE_ID.equals(event.getProperty())) {
					final boolean newValue = (Boolean)event.getNewValue();
					org.dawb.workbench.ui.Activator.getDefault().getPreferenceStore().setValue(org.dawb.workbench.ui.editors.preference.EditorConstants.RESCALE_SETTING, newValue);
				} else {
					setAxisSettings(EditorConstants.XAXIS_PROP_STUB, plottingSystem.getSelectedXAxis());
					setAxisSettings(EditorConstants.YAXIS_PROP_STUB, plottingSystem.getSelectedYAxis());
				}
			}
		});
		getAxisSettings(EditorConstants.XAXIS_PROP_STUB, plottingSystem.getSelectedXAxis());
		getAxisSettings(EditorConstants.YAXIS_PROP_STUB, plottingSystem.getSelectedYAxis());

//		axisMap.put(1, plottingSystem.getSelectedYAxis());
//		// FIX to http://jira.diamond.ac.uk/browse/DAWNSCI-380 remove axes until they work
//		//TODO Update this when FIX will be fixed
//		for (int i = 2; i <=2; i++) { //(Y4)
//			final IAxis yAxis = plottingSystem.createAxis("Y"+i, true, i==3||i==4?SWT.RIGHT:SWT.LEFT);
//			yAxis.setVisible(false);
//			yAxis.setTitle("Y"+i);
//			axisMap.put(i, yAxis);
//		}

		// We ensure that the view for choosing data sets is created, but not visible
//		CommonThreading.execAsynced(new Runnable() {
//			@Override
//			public void run() {
//				try {
//					final IViewPart plotDataView = EclipseUtils.getActivePage().showView(PlotDataView.ID, null, IWorkbenchPage.VIEW_CREATE);
//					CommonThreading.execAsynced(new Runnable() {
//						@Override
//						public void run() {
//							EclipseUtils.getActivePage().hideView(plotDataView);
//						}});
//				} catch (final PartInitException e) {
//					logger.error("Cannot open "+PlotDataView.ID);
//				}
//			}});
		if( IEditorPartHost.class.isAssignableFrom(classRole.getClass()) )
			getEditorSite().setSelectionProvider(plottingSystem.getSelectionProvider());
		else if( IViewPartHost.class.isAssignableFrom(classRole.getClass()) )
			getViewSite().setSelectionProvider(plottingSystem.getSelectionProvider());

		plottingSystem.setRescale(org.dawb.workbench.ui.Activator.getDefault().getPreferenceStore().getBoolean(org.dawb.workbench.ui.editors.preference.EditorConstants.RESCALE_SETTING));
		plottingSystem.addPositionListener(new IPositionListener() {
			@Override
			public void positionChanged(final PositionEvent evt) {
				mousePos.x = evt.x;
				mousePos.y = evt.y;
				listenerManager.fireSomethingChanged(SomethingChangeEvent.MOUSE_POSITION);
			}
		});
//		phaStateSelected();
		editorInputChanged();
		parent.pack(true);
	}

	protected void setAxisSettings(final String propertyStub, final IAxis axis) {
		final IPreferenceStore store = org.dawb.workbench.ui.Activator.getDefault().getPreferenceStore();
		final boolean isDateTime = axis.isDateFormatEnabled();
		store.setValue(propertyStub+"isDateTime", isDateTime);
		if (isDateTime) {
			final String format = axis.getFormatPattern();
			store.setValue(propertyStub + "dateFormat", format);
		}
		boolean isLog = axis.isLog10();
		store.setValue(propertyStub + "log10", isLog);
	}

	protected void getAxisSettings(final String propertyStub, final IAxis axis) {
		final IPreferenceStore store = org.dawb.workbench.ui.Activator.getDefault().getPreferenceStore();
		boolean isDateTime = false;
		if (store.contains(propertyStub+"isDateTime")) {
			isDateTime = store.getBoolean(propertyStub+"isDateTime");
			axis.setDateFormatEnabled(isDateTime);
		}
		if (isDateTime && store.contains(propertyStub+"dateFormat")) {
			axis.setFormatPattern(store.getString(propertyStub+"dateFormat"));
		}
		if (store.contains(propertyStub+"log10")) {
			axis.setLog10(store.getBoolean(propertyStub+"log10"));
		}
	}

//	public IVariableManager getDataSetComponent() {
//		final IWorkbenchPage wb =EclipseUtils.getActivePage();
//		if (wb==null) return null;
//		final PlotDataView view = (PlotDataView)wb.findView(PlotDataView.ID);
//		if (view==null) return null;
//		final IPage page = view.getCurrentPage();
//		if (!(page instanceof PlotDataPage)) return null;
//		return ((PlotDataPage)page).getDataSetComponent();
//	}
//	
//	public ISliceSystem getSliceComponent() {
//		final IWorkbenchPage wb =EclipseUtils.getActivePage();
//		if (wb==null) return null;
//		final PlotDataView view = (PlotDataView)wb.findView(PlotDataView.ID);
//		if (view==null) return null;
//		final IPage page = view.getCurrentPage();
//		if (!(page instanceof PlotDataPage)) return null;
//		return ((PlotDataPage)page).getSliceComponent();
//	}

//	protected class PlotJob extends Job {
////		protected IDataset data;
//		protected ITransferableDataObject[] selections;
//		protected ISliceSystem system;
//
//		public PlotJob() {
//			super("Plot update");
//			setUser(false);
//			setSystem(true);
//			setPriority(Job.SHORT);
//		}
//
////		public void reschedule(final IDataset data) {
////			cancel();
////			this.data  = data;
////			schedule();
////		}
//		public void reschedule(final ITransferableDataObject[] selections, final ISliceSystem system) {
//			cancel();
//			this.selections  = selections;
//			this.system = system;
//			schedule();
//		}
//
//		public IStatus run(final IProgressMonitor monitor) {
//			monitor.beginTask("Updating plot", 100);
//			createPlot(selections, system, monitor);
//			return Status.OK_STATUS;
//		}
//	}
//
//	private boolean doingUpdate = false;
//
////	@Override
//	public void updatePlot(final ITransferableDataObject[] selections,
//			final ISliceSystem sliceSystem, final boolean useTask) {
//		System.out.println("$$$$ updatePlot called, doingUpdate=" + doingUpdate + ", useTask=" + useTask + " $$$$");
//		if (doingUpdate) return;
//
//		if (selections==null || selections.length<1) {
//			if (sliceSystem!=null) sliceSystem.setVisible(false);
//			plottingSystem.reset();
//			return;
//		}
//
//		try {
//			doingUpdate = true;
//			final int[] shape = selections[0].getShape(true);
//			final String path = EclipseUtils.getFilePath(getEditorInput());
//			System.out.println("$$$$ updatePlot called, filename is " + selections[0].getFileName() + " $$$$");
//			if (selections.length==1 && shape.length!=1) {
//				final ITransferableDataObject object = selections[0];
//				sliceSystem.setVisible(true);
//				final IVariableManager man = (IVariableManager)getAdapter(IVariableManager.class);
//				final ILazyDataset lazy = selections[0].getLazyData(null);
//				/* I simply deny to pass filepath of editorInput, because by
//				 * the time we get here, the editorInput might have been
//				 * changed. Thus I either pass null (then sliceSystem crashes),
//				 * or empty string, then who know what does not work.
//				 */
//				sliceSystem.setData(new SliceSource(man, lazy,
//					object.getName(), "", object.isExpression()));
//				return;
//			}
//			if (sliceSystem!=null) sliceSystem.setVisible(false);
//
//			if (useTask) {
//				plotJob.reschedule(selections, sliceSystem);
//			} else {
//				createPlot(selections, sliceSystem, new NullProgressMonitor());
//			}
//		} finally {
//			doingUpdate = false;
//		}
//	}
//	/* This method is used from updatePlot ONLY, that is not sure it will be
//	 * ever used. Also 1D case is not adopted.
//	 */
//	protected void createPlot(final ITransferableDataObject[] selections, final ISliceSystem system, final IProgressMonitor monitor) {
//		System.out.println("##### createPlot called!!! #####");
//		if (monitor.isCanceled()) return;
//
//		final ITransferableDataObject first = selections[0];
//		IDataset data = first.getData(new ProgressMonitorWrapper(monitor));	
//		if (data==null) return;
//		data = data.squeeze();
//		try {
//			if (data.getSize()<0) return;
//		} catch (Exception ne) {
//			return;
//		}
//		if (data.getRank()>2) return; // Cannot plot more that 2 dims!
//		
//		if (data.getRank()==2) {
//			final boolean newInput = ((MemoryDatasetEditorInput)getEditorInput()).isNewInput();
//			System.out.println("##### createPlot called with newInput=" + newInput + " #####");
//			createPlot(data, false, monitor); //newInput is false from here (which means selection change)
//		} else {
//			throw new RuntimeException("createPlot called with 1 rank");
////			List<ITransferableDataObject> sels = new ArrayList<ITransferableDataObject>(Arrays.asList(selections));
////
////			final IDataset x;
////			if (plottingSystem.isXFirst() && sels.size()>1) {
////				x  = data;
////				sels.remove(0);
////			} else {
////				x = null;
////			}
////			
////			if (sels.isEmpty() || (!plottingSystem.isXFirst() && sels.size()==1)) {
////				
////				// TODO Data Name
////				final List<ITrace> traces = plottingSystem.updatePlot1D(x, Arrays.asList(data), Arrays.asList(first.getName()), monitor);
////				removeOldTraces(traces);
////		        sync(sels,traces);
////		        if (plottingSystem.isRescale()) plottingSystem.repaint();
////				return;
////			}
////			
////
////            final Map<Integer,List<IDataset>> ys = sels.isEmpty()
////                                                 ? null
////                                		         : new HashMap<Integer,List<IDataset>>(4);
////
////            final Map<Integer,List<String>> dataNames = sels.isEmpty()
////                                                 ? null
////   		                                         : new HashMap<Integer,List<String>>(4);
////
////            // Sort ys by axes (for 2D there is one y)
////            if (!sels.isEmpty()) {
////        	   for (int i = 1; i <= 4; i++) {
////        		   getYS(i, sels, monitor, ys, dataNames);
////         	   }
////            }
////
////    		final List<ITrace> traces = createPlotSeparateAxes(x,ys,dataNames,monitor);
////	        sync(sels,traces);
////
//		}
//		
////		plottingSystem.repaint();
//		
//		monitor.done();
//	}

//	@Override
//	public Map<String, IDataset> getSelected() {
//		final PlotDataComponent dataSetComponent = (PlotDataComponent)getDataSetComponent();
//		final Map<String,IDataset> ret = new HashMap<String, IDataset>(3);
//		if (dataSetComponent==null) return ret;
//		
//		final List<ITransferableDataObject> selectedNames = dataSetComponent.getSelections();
//		for (final ITransferableDataObject object : selectedNames) {
//			final IDataset set = object.getData(null);
//			if (set==null) continue;
//			ret.put(set.getName(), set);
//		}
//		return ret;
//	}

	/**
	 * Simple but good enough axis creator.
	 * @param data the dataset to create axis for
	 * @return axis in a list
	 */
	protected List<IDataset> getAxesForPlot(final IDataset data) {
		final DimsDataList dimsDataList;
		final IDatasetMathsService service;
		try {
			service = CommonExtension.getService(IDatasetMathsService.class);
			dimsDataList = new DimsDataList(data.getShape());
		} catch (final Exception e) {
			throw new RuntimeException(e);
		}
		IDataset x = null, y = null;
		for (int i = 0; i < dimsDataList.size(); i++) {
			final DimsData dimsData = dimsDataList.getDimsData(i);
			if (dimsData.getPlotAxis() == AxisType.X) {
				x = service.createRange(data.getShape()[i], IDatasetMathsService.INT);
			}
			if (dimsData.getPlotAxis() == AxisType.Y) {
				y = service.createRange(data.getShape()[i], IDatasetMathsService.INT);
			}
		}
		//According to SliceUtils.plotSlice, swapping x,y for {create|update}Plot2D
		return Arrays.asList(new IDataset[]{y,x}); //Intentionally y,x!
	}

	public boolean isPlotReady() {
		return imageTrace != null;
	}

	protected void createPlot(IDataset data, final boolean rehistogram, final IProgressMonitor monitor) {
		Assert.isNotNull(data, "The data is null, it must not be null.");
		//We go for 2 dims, does not matter if one of them is 1 long
		if( data.getRank() > 2 )
			data = data.squeeze(true); //drops beginning 1 long dims
		if( data.getRank() > 2 )
			data = data.squeeze(false); //drops all 1 long dims
		if (data.getRank()!=2) return; //Accepting only plot with 2 dims!
		Assert.isLegal(data.getSize() > 0, "The size of data must be greater than 0");
		final IProgressMonitor thisMonitor = monitor == null ? new NullProgressMonitor() : monitor;
		thisMonitor.beginTask("Creating the plot", 1);
		try {
			final IProgressMonitor plottingMonitor = new SubProgressMonitor(thisMonitor, 1);
			final ITrace trace;
			if( rehistogram ) {
				plottingSystem.clear();
				trace = plottingSystem.createPlot2D(data, getAxesForPlot(data), data.getName(), plottingMonitor);
			} else {
				trace = plottingSystem.updatePlot2D(data, getAxesForPlot(data), data.getName(), plottingMonitor);
				if (trace != null && plottingSystem.isRescale())
					plottingSystem.repaint(); //Better than autoscaleAxes(), because it also repaints, and thread safe
			}
			if( trace == null ) //thisMonitor is cancelled
				return;
			imageTrace = (IImageTrace) trace;
			final int[] setShape = trace.getData().getShape();
			statusXPosFormat.setMaximumIntegerDigits(1+(int)Math.floor(Math.log10(setShape[1])));
			statusYPosFormat.setMaximumIntegerDigits(1+(int)Math.floor(Math.log10(setShape[0])));
			statusDataFormat.setMaximumIntegerDigits(1+(int)Math.floor(Math.log10(trace.getData().max(true, true).doubleValue())));
			imageTrace.setRescaleHistogram(false); //dViewer's default, Histogram tool overrides
			onPlotIsReady();
		} finally {
			thisMonitor.done();
		}
	}

	protected void onPlotIsReady() {
		if( requiredDownsampleType != null ) {
			setDownsampleType(null, requiredDownsampleType);
			requiredDownsampleType = null;
		}
	}

	protected void onPhaPlotIsCancelled(final int cancelledRadius) {
		System.out.println("onPhaPlotIsCancelled (" + phaAction.isChecked() + ")!");
		synchronized (dataSetLock) {
			if( imageTrace == null )
				createPlotWithPhaCheck(false, null);
			else {
				setPha(null, imageTrace.getData() != dataSetOriginal); //sets the state only
				if( cancelledRadius == phaRadius ) //Means cancellation only (not dragging slider)
					setPhaRadius(null, dataSetPHARadius); //Ensuring in case of cancelling
			}
		}
	}

	@Override //from IDViewerImageControllable
	public boolean getPha() {
		return phaAction.isChecked();
	}

	@Override //from IDViewerImageControllable
	public void setPha(final ISomethingChangeListener sender, final boolean phaState) {
		if( phaState == phaAction.isChecked() )
			return;
		phaAction.setChecked(phaState);
		if( isPlotReady() )
			onPhaStateSelected();
	}

	protected void onPhaStateSelected() {
		System.out.println("PHA State Selected (" + phaAction.isChecked() + ")!");
//		psfTool.updatePSFState(phaAction.isChecked()); //TODO
		if( createPHAPlotJob.getState() != Job.RUNNING) {
			synchronized (dataSetLock) {
				if( (!phaAction.isChecked() && imageTrace.getData() == dataSetOriginal)
					|| (phaAction.isChecked() && imageTrace.getData() == dataSetPHA && dataSetPHARadius == phaRadius) )
						return; //Already displaying the appropriate dataset
				createPlotWithPhaCheck(false, null);
			}
		} //else see onPhaPlotIsCancelled
	}

	@Override //from IDViewerImageControllable
	public DownsampleType getDownsampleType() {
		return imageTrace != null ? imageTrace.getDownsampleType() : null;
	}

	@Override //from IDViewerImageControllable
	public void setDownsampleType(final ISomethingChangeListener sender, final DownsampleType downsampleType) {
		final DownsampleType currentDownsampleType = getDownsampleType();
		if( imageTrace == null ) {
			requiredDownsampleType = downsampleType; //Set it later
			return;
		} else if( currentDownsampleType == downsampleType )
			return;
		try {
			Assert.isNotNull(downsampleType, "The downsampleType is null, it must not be null.");
		} catch(final AssertionFailedException e) {
			if( sender != null )
				listenerManager.sendSomethingChanged(SomethingChangeEvent.DOWNSAMPLE_TYPE, sender);
			return;
		}
		logger.debug( "DEBUG: Setting DownsampleType from " + currentDownsampleType.getLabel() + " to " + downsampleType.getLabel() );
		if( isPlotReady() )
			CommonThreading.execUISynced(new Runnable() {
				public void run() {
					imageTrace.setDownsampleType(downsampleType);
				}
			});
		listenerManager.fireSomethingChanged(SomethingChangeEvent.DOWNSAMPLE_TYPE);
	}

	@Override //from IDViewerImageControllable
	public int getPhaRadiusMin() {
		return DViewerEditorConstants.PHA_RADIUS_MIN;
	}

	@Override //from IDViewerImageControllable
	public int getPhaRadiusSup() {
		return DViewerEditorConstants.PHA_RADIUS_MAX + 1;
	}

	@Override //from IDViewerImageControllable
	public boolean isPhaRadiusValid(final int value) {
		return value>=getPhaRadiusMin() && value<getPhaRadiusSup();
	}

	@Override //from IDViewerImageControllable
	public int getPhaRadius() {
		return phaRadius;
	}

	@Override //from IDViewerImageControllable
	public void setPhaRadius(final ISomethingChangeListener sender, final int phaRadius) {
		if( getPhaRadius() == phaRadius )
			return;
		final int phaRadiusMin = getPhaRadiusMin();
		final int phaRadiusSup = getPhaRadiusSup();
		try {
			Assert.isLegal(isPhaRadiusValid(phaRadius), "The phaRadius (" + phaRadius + ") is illegal, " + phaRadiusMin + " <= phaRadius < " + phaRadiusSup + " must be true.");
		} catch(final IllegalArgumentException e) {
			if( sender != null )
				listenerManager.sendSomethingChanged(SomethingChangeEvent.PHA_RADIUS, sender);
			return;
		}
		this.phaRadius = phaRadius;
		synchronized (dataSetLock) {
			if( isPlotReady() )
				phaRadiusChanged();
		}
		listenerManager.fireSomethingChanged(SomethingChangeEvent.PHA_RADIUS);
	}

	protected void phaRadiusChanged() {
		synchronized (dataSetLock) {
			createPHAPlotJob.cancel();
			if( !phaAction.isChecked() )
				return; //Already displaying the original set
			createPlotWithPhaCheck(false, null);
		}
	}

	protected void createPlotNewDataset(final IDataset data, final boolean newInput, final IProgressMonitor monitor) {
		synchronized (dataSetLock) {
			dataSetOriginal = data;
			dataSetPHA = null;
			createPHAPlotJob.cancel();
			createPlotWithPhaCheck(newInput, monitor);
		}
	}

	/**
	 * Currently, rehistogramming also resets zooming.
	 * @param rehistogram
	 * @param monitor
	 */
	protected void createPlotWithPhaCheck(final boolean rehistogram, final IProgressMonitor monitor) {
		synchronized (dataSetLock) {
			final IDataset dataSetToDisplay;
			if( !phaAction.isChecked() )
				dataSetToDisplay = dataSetOriginal;
			else
				dataSetToDisplay = dataSetPHARadius == phaRadius ? dataSetPHA : null;
			if( dataSetToDisplay != null )
				createPlot(dataSetToDisplay, rehistogram, monitor);
			else {
				createPHAPlotJob.reschedule(dataSetOriginal, rehistogram, phaRadius);
			}
		}
	}

	HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, XDSASCIIHKLRecord>>>> imageIndexMap;
	@Override //from IDViewerImageControllable
	public void setHKLFile(final EFile file) {
		hklFile = file;
		if( hklFile != null ) {
			try {
				final XDSASCIIHKLReader xdsReader = new XDSASCIIHKLReader(getDeepestInputStreamForFile(file, "HKL"));
				try {
					xdsReader.readHeader();
					final List<XDSASCIIHKLRecord> records = xdsReader.readAllHKLRecords();
					imageIndexMap = new HashMap<Integer, HashMap<Integer, HashMap<Integer, HashMap<Integer, XDSASCIIHKLRecord>>>>(100); //TODO HashMap is not good, we h ave duplicate keys
					for( final XDSASCIIHKLRecord record : records) {
						int i = record.getStartImageIndex();
						final HashMap<Integer, HashMap<Integer, HashMap<Integer, XDSASCIIHKLRecord>>> hMap;
						if( !imageIndexMap.containsKey(i) ) {
							hMap = new HashMap<Integer, HashMap<Integer, HashMap<Integer, XDSASCIIHKLRecord>>>();
							imageIndexMap.put(i, hMap);
						} else
							hMap = imageIndexMap.get(i);
						final HashMap<Integer, HashMap<Integer, XDSASCIIHKLRecord>> kMap;
						if( !hMap.containsKey(record.getH()) ) {
							kMap = new HashMap<Integer, HashMap<Integer, XDSASCIIHKLRecord>>();
							hMap.put(record.getH(), kMap);
						} else
							kMap = hMap.get(record.getH());
						final HashMap<Integer, XDSASCIIHKLRecord> lMap;
						if( !kMap.containsKey(record.getK()) ) {
							lMap = new HashMap<Integer, XDSASCIIHKLRecord>();
							kMap.put(record.getK(), lMap);
						} else
							lMap = kMap.get(record.getK());
						lMap.put(record.getL(), record);
//						final AnnotationWrapperEmbl ann1 = (AnnotationWrapperEmbl)AnnotationWrapperEmbl.replaceCreateAnnotation(plottingSystem, "" + record.getH() + "," + record.getK() + "," + record.getL());
//						ann1.setCursorLineStyle(CursorLineStyleEmbl.NOCURSOR);
//						ann1.setShowArrow(false);
//						ann1.setShowPosition(false);
//						ann1.setShowInfo(false);
//						ann1.setdxdy(0, -5);
//						ann1.setLocation(record.getX(), record.getY());
//						plottingSystem.addAnnotation(ann1);
					}
				} finally {
					try {
						xdsReader.close();
					} catch (final IOException e) {
					}
				}
			} catch (final Exception e1) { //From constructing, or IOException
				e1.printStackTrace();
			}
		} else {
			imageIndexMap = null;//TODO remove the annotations and the stored HKL values
		}
	}

	@Override //from IDViewerImageControllable
	public int getHMin() {
		return hMin;
	}

	@Override //from IDViewerImageControllable
	public int getHSup() {
		return hSup;
	}

	@Override //from IDViewerImageControllable
	public boolean isHValid(final int value) {
		return value>=getHMin() && value<getHSup();
	}

	@Override //from IDViewerImageControllable
	public int getHRangeMin() {
		return hRangeMin;
	}

	@Override //from IDViewerImageControllable
	public void setHRangeMin(final ISomethingChangeListener sender, final int hRangeMin) {
		if( getHRangeMin() == hRangeMin )
			return;
		final int hMin = getHMin();
		final int hSup = getHSup();
		try {
			Assert.isLegal(isHValid(hRangeMin), "The hRangeMin (" + hRangeMin + ") is illegal, " + hMin + " <= hRangeMin < " + hSup + " must be true.");
		} catch(final IllegalArgumentException e) {
			if( sender != null )
				listenerManager.sendSomethingChanged(SomethingChangeEvent.H_RANGE_MIN, sender);
			return;
		}
		this.hRangeMin = hRangeMin;
		synchronized (dataSetLock) {
			if( isPlotReady() )
				; //phaRadiusChanged(); //TODO update HKL display (lock should be changed probably)
		}
		listenerManager.fireSomethingChanged(SomethingChangeEvent.H_RANGE_MIN);
	}

	@Override //from IDViewerImageControllable
	public int getHRangeMax() {
		return hRangeMax;
	}

	@Override //from IDViewerImageControllable
	public void setHRangeMax(final ISomethingChangeListener sender, final int hRangeMax) {
		if( getHRangeMax() == hRangeMax )
			return;
		final int hMin = getHMin();
		final int hSup = getHSup();
		try {
			Assert.isLegal(isHValid(hRangeMax), "The hRangeMax (" + hRangeMax + ") is illegal, " + hMin + " <= hRangeMax < " + hSup + " must be true.");
		} catch(final IllegalArgumentException e) {
			if( sender != null )
				listenerManager.sendSomethingChanged(SomethingChangeEvent.H_RANGE_MAX, sender);
			return;
		}
		this.hRangeMax = hRangeMax;
		synchronized (dataSetLock) {
			if( isPlotReady() )
				; //phaRadiusChanged(); //TODO update HKL display (lock should be changed probably)
		}
		listenerManager.fireSomethingChanged(SomethingChangeEvent.H_RANGE_MAX);
	}

	@Override //from IDViewerImageControllable
	public int getKMin() {
		return kMin;
	}

	@Override //from IDViewerImageControllable
	public int getKSup() {
		return kSup;
	}

	@Override //from IDViewerImageControllable
	public boolean isKValid(final int value) {
		return value>=getKMin() && value<getKSup();
	}

	@Override //from IDViewerImageControllable
	public int getKRangeMin() {
		return kRangeMin;
	}

	@Override //from IDViewerImageControllable
	public void setKRangeMin(final ISomethingChangeListener sender, final int kRangeMin) {
		if( getKRangeMin() == kRangeMin )
			return;
		final int kMin = getKMin();
		final int kSup = getKSup();
		try {
			Assert.isLegal(isKValid(kRangeMin), "The kRangeMin (" + kRangeMin + ") is illegal, " + kMin + " <= kRangeMin < " + kSup + " must be true.");
		} catch(final IllegalArgumentException e) {
			if( sender != null )
				listenerManager.sendSomethingChanged(SomethingChangeEvent.K_RANGE_MIN, sender);
			return;
		}
		this.kRangeMin = kRangeMin;
		synchronized (dataSetLock) {
			if( isPlotReady() )
				; //phaRadiusChanged(); //TODO update HKL display (lock should be changed probably)
		}
		listenerManager.fireSomethingChanged(SomethingChangeEvent.K_RANGE_MIN);
	}

	@Override //from IDViewerImageControllable
	public int getKRangeMax() {
		return kRangeMax;
	}

	@Override //from IDViewerImageControllable
	public void setKRangeMax(final ISomethingChangeListener sender, final int kRangeMax) {
		if( getKRangeMax() == kRangeMax )
			return;
		final int kMin = getKMin();
		final int kSup = getKSup();
		try {
			Assert.isLegal(isKValid(kRangeMax), "The kRangeMax (" + kRangeMax + ") is illegal, " + kMin + " <= kRangeMax < " + kSup + " must be true.");
		} catch(final IllegalArgumentException e) {
			if( sender != null )
				listenerManager.sendSomethingChanged(SomethingChangeEvent.K_RANGE_MAX, sender);
			return;
		}
		this.kRangeMax = kRangeMax;
		synchronized (dataSetLock) {
			if( isPlotReady() )
				; //phaRadiusChanged(); //TODO update HKL display (lock should be changed probably)
		}
		listenerManager.fireSomethingChanged(SomethingChangeEvent.K_RANGE_MAX);
	}

	@Override //from IDViewerImageControllable
	public int getLMin() {
		return lMin;
	}

	@Override //from IDViewerImageControllable
	public int getLSup() {
		return lSup;
	}

	@Override //from IDViewerImageControllable
	public boolean isLValid(final int value) {
		return value>=getLMin() && value<getLSup();
	}

	@Override //from IDViewerImageControllable
	public int getLRangeMin() {
		return lRangeMin;
	}

	@Override //from IDViewerImageControllable
	public void setLRangeMin(final ISomethingChangeListener sender, final int lRangeMin) {
		if( getLRangeMin() == lRangeMin )
			return;
		final int lMin = getLMin();
		final int lSup = getLSup();
		try {
			Assert.isLegal(isLValid(lRangeMin), "The lRangeMin (" + lRangeMin + ") is illegal, " + lMin + " <= lRangeMin < " + lSup + " must be true.");
		} catch(final IllegalArgumentException e) {
			if( sender != null )
				listenerManager.sendSomethingChanged(SomethingChangeEvent.L_RANGE_MIN, sender);
			return;
		}
		this.lRangeMin = lRangeMin;
		synchronized (dataSetLock) {
			if( isPlotReady() )
				; //phaRadiusChanged(); //TODO update HKL display (lock should be changed probably)
		}
		listenerManager.fireSomethingChanged(SomethingChangeEvent.L_RANGE_MIN);
	}

	@Override //from IDViewerImageControllable
	public int getLRangeMax() {
		return lRangeMax;
	}

	@Override //from IDViewerImageControllable
	public void setLRangeMax(final ISomethingChangeListener sender, final int lRangeMax) {
		if( getLRangeMax() == lRangeMax )
			return;
		final int lMin = getLMin();
		final int lSup = getLSup();
		try {
			Assert.isLegal(isLValid(lRangeMax), "The lRangeMax (" + lRangeMax + ") is illegal, " + lMin + " <= lRangeMax < " + lSup + " must be true.");
		} catch(final IllegalArgumentException e) {
			if( sender != null )
				listenerManager.sendSomethingChanged(SomethingChangeEvent.L_RANGE_MAX, sender);
			return;
		}
		this.lRangeMax = lRangeMax;
		synchronized (dataSetLock) {
			if( isPlotReady() )
				; //phaRadiusChanged(); //TODO update HKL display (lock should be changed probably)
		}
		listenerManager.fireSomethingChanged(SomethingChangeEvent.L_RANGE_MAX);
	}

	@Override //from IDViewerImageControllable
	public Point2DD getMouseAxisPos() {
		try {
			if (imageTrace!=null)
				return new Point2DD(imageTrace.getPointInAxisCoordinates(new double[] { mousePos.x, mousePos.y }));
		} catch (final Throwable ignored) {
		}
		return mousePos; // Normal position
	}

	@Override //from IDViewerImageControllable
	public String getStatusText() {
		final Point2DD mouseAxisPos = getMouseAxisPos();
		final StringBuilder result = new StringBuilder("x:").append(statusXPosFormat.format(mouseAxisPos.x)).append(STATUS_FIELD_SEPARATOR).append("y:").append(statusYPosFormat.format(mouseAxisPos.y));
		if (imageTrace!=null) {
			final IDataset dataSet = imageTrace.getData();
			try {
				//Copied from InfoPixelTool, because much faster than calculating all kind of stuff
				result.append(STATUS_FIELD_SEPARATOR).append("value:").append(statusDataFormat.format(dataSet.getDouble((int)Math.floor(mousePos.y), (int)Math.floor(mousePos.x))));
			} catch (final Throwable ignored) { //Probably never happens
			}
			try {
				result.append(STATUS_FIELD_SEPARATOR).append("res[\u00c5]:").append(iPLPResolution.getText(mousePos.x, mousePos.y, Double.NaN, Double.NaN, dataSet, null));
			} catch (final Throwable ignored) { //Probably divided by q=0
			}
		}
		return result.toString();
	}

	@Override
	public void requestDViewerView() {
		DViewerStartup.requestDViewerView();
	}

	@Override
	public void requestDViewerControls() {
		DViewerStartup.requestDViewerControls();
	}

}
