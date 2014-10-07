package org.embl.cca.dviewer.ui.editors;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import javax.imageio.ImageIO;

import org.dawb.common.services.ServiceManager;
import org.dawb.common.ui.menu.CheckableActionGroup;
import org.dawb.common.ui.menu.MenuAction;
import org.dawb.common.ui.util.GridUtils;
import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawb.common.util.io.FileUtils;
import org.dawb.workbench.ui.editors.PlotDataEditor;
import org.dawb.workbench.ui.editors.preference.EditorConstants;
import org.dawb.workbench.ui.editors.zip.ZipUtils;
import org.dawnsci.common.widgets.editor.ITitledEditor;
import org.dawnsci.plotting.AbstractPlottingSystem;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.AssertionFailedException;
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
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.plotting.api.IPlottingSystem;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.PlottingFactory;
import org.eclipse.dawnsci.plotting.api.axis.IAxis;
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
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.embl.cca.dviewer.DViewerActivator;
import org.embl.cca.dviewer.ui.editors.preference.DViewerEditorConstants;
import org.embl.cca.dviewer.ui.editors.preference.EditorPreferenceInitializer;
import org.embl.cca.dviewer.ui.editors.utils.PHA;
import org.embl.cca.utils.datahandling.MemoryDatasetEditorInput;
import org.embl.cca.utils.datahandling.file.SmarterJavaImageSaver;
import org.embl.cca.utils.datahandling.file.SmarterJavaImageScaledSaver;
import org.embl.cca.utils.datahandling.file.XDSHKLRecord;
import org.embl.cca.utils.datahandling.file.XDSIntegrationReader;
import org.embl.cca.utils.errorhandling.ExceptionUtils;
import org.embl.cca.utils.general.ISomethingChangeListener;
import org.embl.cca.utils.general.SomethingChangeEvent;
import org.embl.cca.utils.threading.CommonThreading;
import org.embl.cca.utils.ui.widget.SaveFileDialog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;

public class DViewerImageEditorPart extends EditorPart implements IReusableEditor, ITitledEditor/*, ISlicablePlottingPart, ISelectedPlotting*/,
	IDViewerImageControllable, IPropertyChangeListener {

	public static final String ID = "org.embl.cca.dviewer.ui.editors.DViewerImageEditorPart";

	private static final Logger logger = LoggerFactory.getLogger(PlotDataEditor.class);

	protected final IPlottingSystem plottingSystem;
	protected final PlotType defaultPlotType;
//	/**
//	 * Required for createPlotSeparateAxes, which is required if this class
//	 * implements ISlicablePlottingPart, and receiving 1D selection.
//	 * Now it does not, and not planned.
//	 */
//	protected final Map<Integer, IAxis> axisMap;
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
	protected Action hklAction; //TODO temporary action
	/**
	 * The current radius of PHA which is applied on dataSetOriginal when requested.
	 */
	protected int phaRadius;
	/**
	 * The job to apply PHA on dataSetOriginal, resulting in dataSetPHA.
	 */
	protected final CreatePHAPlotJob createPHAPlotJob;

	protected DownsampleType requiredDownsampleType = null;

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

	public DViewerImageEditorPart(final PlotType defaultPlotType, final DViewerListenerManager listenerManager) {
		
//		this.axisMap = new HashMap<Integer, IAxis>(4);
//		this.plotJob = new PlotJob();
////		this.lock    = new ReentrantLock();
		this.listenerManager = listenerManager;
		createPHAPlotJob = new CreatePHAPlotJob();
		this.defaultPlotType= defaultPlotType;
		final IPlottingSystem pS;
		try {
			pS = PlottingFactory.createPlottingSystem();
		} catch (final Exception e) {
//			ExceptionUtils.logError(logger, "Cannot locate any plotting systems", e, this);
			throw new RuntimeException("Cannot locate any plotting systems", e);
		}
		this.plottingSystem = pS;
	}

	@Override
	public void init(final IEditorSite site, final IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input, false);
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	public IPlottingSystem getPlottingSystem() {
		return plottingSystem;
	}

	@Override //Required by IDatasetEditor, IReusableEditor
	public void setInput(final IEditorInput input) {
		logger.debug("setInput(IEditorInput) called");
		setInput(input, true);
	}

	protected void setInput(final IEditorInput input, final boolean createData) {
		super.setInput(input);
		setPartName(getEditorInput().getName());
		if( createData ) {
			editorInputChanged();
		}
		dirty = false;
		firePropertyChange(IEditorPart.PROP_INPUT);
		logger.debug("setInput(IEditorInput, boolean=" + createData + ") called");
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

	@Override //Required by ITitledEditor
	public void setPartTitle(final String name) {
		setPartName(name);	
	}

	@Override
	public void doSave(final IProgressMonitor monitor) {
		if( isDirty() ) {
			; //TODO Currently the plot can not be changed, thus it can not be dirty
			setDirty(false);
		}
	}

	@Override
	public void doSaveAs() {
		do {
			saveAs((Dataset)imageTrace.getData(), false, 0, 0);
		} while( false );
	}

	protected void saveAs(Dataset ds, boolean autoscale, double min, double max) {
		final Shell shell = getSite().getWorkbenchWindow().getShell();
		Assert.isNotNull(shell, "Environment error: can not find shell");
		if( saveAsDialog == null ) {
			saveAsDialog = new SaveFileDialog(shell, DViewerActivator.getLocalPreferenceStore(), ID);
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
				MessageDialog.open(MessageDialog.ERROR, shell,
					"File Saving Error", new StringBuilder("No writer for \"")
					.append(newFilePath).append("\" of type \"").append(fileType).append("\".\n\nPlease specify a different file type.").toString(), MessageDialog.NONE );
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
					final boolean ovr = MessageDialog.open(MessageDialog.ERROR, shell,
						"File Saving Error", sb.toString(), MessageDialog.NONE );
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
			saveAs((Dataset)imageTrace.getData(), true, imageTrace.getMin().doubleValue(), imageTrace.getMax().doubleValue());
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

	@Override
	public boolean isDirty() {
		return dirty;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	protected void setDirty(final boolean value) {
		if( this.dirty != value ) {
			this.dirty = value;
			firePropertyChange(PROP_DIRTY);
		}
	}

	public void setToolbarsVisible(boolean isVisible) {
		wrapper.setVisible(isVisible);
	}

	public InputStream getDeepestInputStreamForFile(String fileName, final String fileExtension) throws FileNotFoundException  {
		InputStream fis = new FileInputStream(new File(fileName));
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
				throw new UnsupportedOperationException("Can not handle the  extension: " + ext);
			fileName = fileName.substring(0, fileName.length() - ext.length() - 1);
		} while( true );
		return fis;
	}

	@Override
	public void createPartControl(final Composite parent) { //By PlotDataEditor
		container = new Composite(parent, SWT.NONE);
		getContainer().setLayout(new GridLayout(1, false));
//		getContainer().setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW));
		if (getContainer().getLayout() instanceof GridLayout) //For sure
			GridUtils.removeMargins(getContainer());

		DViewerActivator.getLocalPreferenceStore().addPropertyChangeListener(this); 

		final IActionBars bars = this.getEditorSite().getActionBars();
		wrapper = ActionBarWrapper.createActionBars(getContainer(),(IActionBars2)bars);

		final Composite plot  = new Composite(getContainer(), SWT.NONE);
//		plot.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_CYAN));
		plot.setLayout(new FillLayout()); //layout must be FillLayout for showing plotting
		plot.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true)); //grabExcessVerticalSpace must be true for showing plotting
		plottingSystem.createPlotPart(plot, getEditorInput().getName(), wrapper, defaultPlotType, this);

//		final IPlotActionSystem plotActionSystem = plottingSystem.getPlotActionSystem();
//		if(!(this instanceof ISlicablePlottingPart)) //Hyper2d requires slicing, more exactly axes
//			plotActionSystem.remove(new StringBuilder(ToolPageRole.ROLE_2D.getId()).append('/').append("org.dawnsci.plotting.tools.plotting_tool_hyper2d").toString());
		
		final IToolBarManager toolMan = wrapper.getToolBarManager();

//		this.phaRadius = (Integer)EditorPreferenceHelper.getStoreValue(DViewerActivator.getLocalPreferenceStore(), DViewerEditorConstants.PREFERENCE_PHA_RADIUS);
		this.phaRadius = (int)EditorPreferenceInitializer.PhaRadius.getValue();

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

		//TODO temporary code here, to develop HKL loader
		final String hklFeatureName = "HKL";
		final String hklFeatureFullName = "Load and apply HKL";
		final String hklFeatureIdentifierName = hklFeatureName.toLowerCase();
		hklAction = new Action(hklFeatureName, IAction.AS_PUSH_BUTTON ) {
			@Override
			public void run() {
				//TODO Developing HKL loader here, but obviously it will be initiated from somewhere else
				try {
					final XDSIntegrationReader xdsIR = new XDSIntegrationReader(getDeepestInputStreamForFile("/home/naray/bigstorage/naray/STAC.test/xds_t1w1_run1_1/INTEGRATE.HKL.gz.bz2", "HKL"));
					try {
						final List<XDSHKLRecord> records = xdsIR.readAllHKLRecords();
						System.out.println(records);
					} finally {
						try {
							xdsIR.close();
						} catch (final IOException e) {
						}
					}
				} catch (final Exception e1) { //From constructing, or IOException
					e1.printStackTrace();
				}
			}
		};
		hklAction.setId(getClass().getName() + "." + hklFeatureIdentifierName);
		hklAction.setText("Apply " + hklFeatureName);
		hklAction.setToolTipText("Apply " + hklFeatureFullName + " (" + hklFeatureName + ") on the image");
		hklAction.setImageDescriptor(DViewerActivator.getImageDescriptor("/icons/apply.gif"));
		toolMan.insertAfter( phaAction.getId(), hklAction );

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

		((AbstractPlottingSystem)plottingSystem).addPropertyChangeListener(new IPropertyChangeListener() {
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
		getEditorSite().setSelectionProvider(plottingSystem.getSelectionProvider());

		plottingSystem.setRescale(org.dawb.workbench.ui.Activator.getDefault().getPreferenceStore().getBoolean(org.dawb.workbench.ui.editors.preference.EditorConstants.RESCALE_SETTING));
//		phaStateSelected();
		editorInputChanged();
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

	@Override
	public void setFocus() {
		if (plottingSystem!=null) {
			plottingSystem.setFocus();
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

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") final Class clazz) {
		if (IToolPageSystem.class.equals(clazz) || IPlottingSystem.class.equals(clazz)) { //Mandatory for tools
			return getPlottingSystem();
//		} else if (clazz == Page.class) {
//			return PlotDataPage.getPageFor(this); //Mandatory for PlotDataPage, PlotDataView
//		} else if (clazz == ISliceSystem.class) {
//			return getSliceComponent();
//		} else if (clazz == IVariableManager.class) {
//			return getDataSetComponent();
		}
		return super.getAdapter(clazz);
	}

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
			service = (IDatasetMathsService)ServiceManager.getService(IDatasetMathsService.class);
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
				if (plottingSystem.isRescale())
					plottingSystem.repaint(); //Better than autoscaleAxes(), because it also repaints, and thread safe
			}
			imageTrace = (IImageTrace) trace;
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

	@Override
	public boolean getPha() {
		return phaAction.isChecked();
	}

	@Override
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

	@Override
	public DownsampleType getDownsampleType() {
		return imageTrace != null ? imageTrace.getDownsampleType() : null;
	}

	@Override
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

	@Override
	public int getPhaRadiusMin() {
		return DViewerEditorConstants.PHA_RADIUS_MIN;
	}

	@Override
	public int getPhaRadiusSup() {
		return DViewerEditorConstants.PHA_RADIUS_MAX + 1;
	}

	@Override
	public boolean isPhaRadiusValid(final int value) {
		return value>=DViewerEditorConstants.PHA_RADIUS_MIN && value<=DViewerEditorConstants.PHA_RADIUS_MAX;
	}

	@Override
	public int getPhaRadius() {
		return phaRadius;
	}

	@Override
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

	@Override
	public void propertyChange(final PropertyChangeEvent event) {
		if (DViewerEditorConstants.PREFERENCE_DOWNSAMPLING_TYPE.equals(event.getProperty())) {
			setDownsampleType(null, DownsampleType.values()[ Integer.valueOf((String)event.getNewValue()) ]);
		} else if (DViewerEditorConstants.PREFERENCE_APPLY_PHA.equals(event.getProperty())) {
			setPha(null, (Boolean)event.getNewValue());
		} else if (DViewerEditorConstants.PREFERENCE_PHA_RADIUS.equals(event.getProperty())) {
			setPhaRadius(null, (Integer)event.getNewValue());
		}
	}

}
