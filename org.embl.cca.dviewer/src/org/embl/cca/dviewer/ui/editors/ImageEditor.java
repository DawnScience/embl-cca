package org.embl.cca.dviewer.ui.editors;

import gda.analysis.io.ScanFileHolderException;

import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.StringTokenizer;

import javax.vecmath.Vector3d;

import org.dawb.common.services.ServiceManager;
import org.dawb.common.ui.editors.IEditorExtension;
import org.dawb.common.ui.menu.CheckableActionGroup;
import org.dawb.common.ui.menu.MenuAction;
import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.AbstractPlottingSystem.ColorOption;
import org.dawb.common.ui.plot.PlottingFactory;
import org.dawb.common.ui.plot.roi.ResolutionRing;
import org.dawb.common.ui.plot.roi.ResolutionRingList;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.ui.util.GridUtils;
import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.plotting.api.histogram.IImageService;
import org.dawnsci.plotting.api.region.IRegion;
import org.dawnsci.plotting.api.region.ROIEvent;
import org.dawnsci.plotting.api.region.RegionEvent;
import org.dawnsci.plotting.api.region.RegionUtils;
import org.dawnsci.plotting.api.region.IRegion.RegionType;
import org.dawnsci.plotting.api.trace.IImageTrace;
import org.dawnsci.plotting.api.trace.ITrace;
import org.dawnsci.plotting.api.trace.ITraceListener;
import org.dawnsci.plotting.api.trace.TraceEvent;
import org.dawnsci.plotting.api.trace.TraceWillPlotEvent;
import org.dawnsci.plotting.api.trace.IImageTrace.DownsampleType;
import org.dawnsci.plotting.draw2d.swtxy.selection.AbstractSelectionRegion;
import org.dawnsci.plotting.tools.InfoPixelLabelProvider;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IActionBars2;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IShowEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.part.EditorPart;
import org.embl.cca.dviewer.Activator;
import org.embl.cca.dviewer.plotting.tools.InfoPixelTool;
import org.embl.cca.dviewer.plotting.tools.PSFTool;
import org.embl.cca.dviewer.ui.editors.preference.EditorConstants;
import org.embl.cca.dviewer.ui.editors.preference.EditorPreferenceHelper;
import org.embl.cca.dviewer.ui.editors.utils.PSF;
import org.embl.cca.utils.datahandling.FilePathEditorInput;
import org.embl.cca.utils.datahandling.JavaSystem;
import org.embl.cca.utils.datahandling.file.FileLoader;
import org.embl.cca.utils.datahandling.file.IFileLoaderListener;
import org.embl.cca.utils.datahandling.text.StringUtil;
import org.embl.cca.utils.extension.CommonExtension;
import org.embl.cca.utils.general.Disposable;
import org.embl.cca.utils.imageviewer.ConverterUtils;
import org.embl.cca.utils.imageviewer.MemoryImageEditorInput;
import org.embl.cca.utils.threading.CommonThreading;
import org.embl.cca.utils.threading.ExecutableManager;
import org.embl.cca.utils.threading.TrackableRunnable;
import org.embl.cca.utils.ui.widget.SpinnerSlider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.FloatDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
import uk.ac.diamond.scisoft.analysis.diffraction.DSpacing;
import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
import uk.ac.diamond.scisoft.analysis.diffraction.DetectorPropertyEvent;
import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironmentEvent;
import uk.ac.diamond.scisoft.analysis.diffraction.IDetectorPropertyListener;
import uk.ac.diamond.scisoft.analysis.diffraction.IDiffractionCrystalEnvironmentListener;
import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
import uk.ac.diamond.scisoft.analysis.io.IMetaData;
import uk.ac.diamond.scisoft.analysis.io.PNGScaledSaver;
import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
import uk.ac.diamond.scisoft.analysis.rcp.preference.PreferenceConstants;
import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
import uk.ac.diamond.scisoft.analysis.roi.SectorROI;
import uk.ac.diamond.sda.meta.page.DiffractionMetadataCompositeEvent;
import uk.ac.diamond.sda.meta.page.IDiffractionMetadataCompositeListener;
//import org.dawb.workbench.plotting.tools.InfoPixelTool;

/**
 * ImageEditor
 * 
 * @author Gábor Náray
 * 
 */
/**
 * The <code>ImageEditor</code> class is an image editor extension.
 * <p>
 *
 * @author  Gábor Náray
 * @version 1.00 01/06/2012
 * @since   20120601
 */
public class ImageEditor extends EditorPart implements IReusableEditor, IEditorExtension, IShowEditorInput, IPropertyChangeListener /*, MouseListener, IROIListener, IRegionListener*/
	, IDetectorPropertyListener, IDiffractionCrystalEnvironmentListener, IDiffractionMetadataCompositeListener
	, IFileLoaderListener, Disposable {
	protected final static EnumSet<ImageEditorRemotedDisplayState> notPlayingSet = EnumSet.of(ImageEditorRemotedDisplayState.NOT_PLAYING_AND_REMOTE_UPDATED, ImageEditorRemotedDisplayState.NOT_PLAYING);
	protected final static EnumSet<ImageEditorRemotedDisplayState> playingSet = EnumSet.complementOf(notPlayingSet);
	protected final static EnumSet<ImageEditorRemotedDisplayState> notUpdatedSet = EnumSet.of(ImageEditorRemotedDisplayState.NOT_PLAYING, ImageEditorRemotedDisplayState.PLAYING);
	protected final static EnumSet<ImageEditorRemotedDisplayState> updatedSet = EnumSet.complementOf(notUpdatedSet);
	protected static enum ImageEditorRemotedDisplayState {
		//PLAYING_UPDATING is only theoretical case, because when playing, updating is automatic, thus can not be UPDATED
		NOT_PLAYING_AND_REMOTE_UPDATED, PLAYING_AND_REMOTE_UPDATED, NOT_PLAYING, PLAYING;
		public static ImageEditorRemotedDisplayState togglePlaying(ImageEditorRemotedDisplayState imageEditorRemotedDisplayState) {
			switch (imageEditorRemotedDisplayState) {
				case NOT_PLAYING_AND_REMOTE_UPDATED:
					return PLAYING_AND_REMOTE_UPDATED;
				case PLAYING_AND_REMOTE_UPDATED:
					return NOT_PLAYING_AND_REMOTE_UPDATED;
				case NOT_PLAYING:
					return PLAYING;
				case PLAYING:
					return NOT_PLAYING;
			}
			return null;
		}

		public static ImageEditorRemotedDisplayState setRemoteUpdated(ImageEditorRemotedDisplayState imageEditorRemotedDisplayState) {
			switch (imageEditorRemotedDisplayState) {
				case NOT_PLAYING_AND_REMOTE_UPDATED:
					return NOT_PLAYING_AND_REMOTE_UPDATED;
				case PLAYING_AND_REMOTE_UPDATED:
					return PLAYING_AND_REMOTE_UPDATED;
				case NOT_PLAYING:
					return NOT_PLAYING_AND_REMOTE_UPDATED;
				case PLAYING:
					return PLAYING_AND_REMOTE_UPDATED;
			}
			return null;
		}
		public static boolean isRemoteUpdated(ImageEditorRemotedDisplayState imageEditorRemotedDisplayState) {
			return updatedSet.contains(imageEditorRemotedDisplayState);
		}
		public static boolean isNotPlaying(ImageEditorRemotedDisplayState imageEditorRemotedDisplayState) {
			return notPlayingSet.contains(imageEditorRemotedDisplayState);
		}
	}

	/**
	 * Plug-in ID.
	 */
	public static final String ID = "org.embl.cca.dviewer.ui.editors.ImageEditor";
	
	private static Logger logger = LoggerFactory.getLogger(ImageEditor.class);

	final int BAD_PIXEL_VALUE = -2; //Value of bad pixel

	protected boolean disposed = false;

	final String prefPage = "org.embl.cca.dviewer.ui.editors.preference.EditorPreferencePage";
	final String saveAsId = "org.embl.cca.dviewer.ui.editors.ImageEditor.saveAs";
	final String saveAsOriginalId = "org.embl.cca.dviewer.ui.editors.ImageEditor.saveAsOriginal";

	// This view is a composite of two other views.
	protected AbstractPlottingSystem plottingSystem;	
	protected IImageTrace imageTrace;
	private boolean editorInputChanged = false;
	protected ImageEditorRemotedDisplayState imageEditorRemotedDisplayState;
//	protected IPropertyChangeListener propertyChangeListener;
//	protected IDetectorPropertyListener detectorPropertyListener;
	IDiffractionMetadata localDiffractionMetaData;
	DetectorProperties detConfig;
	DiffractionCrystalEnvironment diffEnv;

//	protected IRegion xHair, yHair;
	protected InfoPixelTool infoPixelTool;
//	protected double cursorImagePosX, cursorImagePosY; 
	protected InfoPixelLabelProvider infoPixelToolLabelResolution;
	protected InfoPixelLabelProvider infoPixelToolLabelQ;
	protected Label point;
	protected Composite infoLine;

//	protected PSF psf;
	protected PSFTool psfTool;
	protected Action psfAction;
	protected Action dviewerPrefAction;
	protected Action dviewerSaveAsAction;
	protected Action dviewerSaveAsOriginalAction;
	
	/**
	 * The objects which contain the image.
	 */
	AbstractDataset originalSet;
//	AbstractDataset psfSet; 

	private Label totalSliderImageLabel;
	private Slider imageSlider;
	private int imageSliderSelection;
	private Text imageFilesWindowWidthText;
	private int imageFilesWindowWidth; //aka batchAmount
	boolean autoFollow;
	Button imageFilesAutoLatestButton;
	private SpinnerSlider psfRadiusUI;
	ExecutableManager psfRadiusManager = null;

//	AbstractDatasetAndFileSet resultDataset = null; //TODO will be removed soon
	static private NumberFormat decimalFormat = NumberFormat.getNumberInstance();

//	ExecutableManager imageLoaderManager = null; //TODO will be removed soon
	Thread imageFilesAutoLatestThread = null;

	//Rings from MXPlotImageEditor [begin]
	protected final static double[] iceResolution = new double[] { 3.897, 3.669, 3.441, 2.671, 2.249, 2.072, 1.948,
		1.918, 1.883, 1.721 };// angstrom

	// Standard rings
	ResolutionRingList standardRingsList;
	ArrayList<IRegion> standardRingsRegionList;

	// Ice rings
	ResolutionRingList iceRingsList;
	ArrayList<IRegion> iceRingsRegionList;

	// Calibrant rings
	ResolutionRingList calibrantRingsList;
	ArrayList<IRegion> calibrantRingsRegionList;

	IRegion beamCentreRegion;

	Action standardRings, iceRings, calibrantRings, beamCentre;
	//Rings from MXPlotImageEditor [end]

	protected Composite controlComposite = null;
	private Text minValueText = null;
//	private LogScale userMinimumScale = null;
	private SpinnerSlider userMinimumScale = null;
	private Text suggestedMinimumText = null;
	private Text maxValueText = null;
//	private LogScale userMaximumScale = null;
	private SpinnerSlider userMaximumScale = null;
	private Text suggestedMaximumText = null;
	protected ExecutableManager imageDisplayTracker = null;
	protected double lastUserMinimum;
	protected double lastUserMaximum;

	protected FileLoader fileLoader;

	public final static String REMOTED_IMAGE = "Remoted Image";
	protected boolean remotedImageEditor = false;
	protected Button automaticDisplayRemotedImage;
	protected boolean selectedDisplayImageByRemoteRequest = true;
	protected Button displayRemotedImageDedicated;

	ITraceListener traceListener;

	public ImageEditor() {
//		resultDataset = new AbstractDatasetAndFileSet();
		fileLoader = new FileLoader();
		fileLoader.addFileLoaderListener(this);
		try {
//			psf = new PSF( Activator.getDefault().getPreferenceStore().getInt(EditorConstants.PREFERENCE_PSF_RADIUS) );
			this.plottingSystem = PlottingFactory.createPlottingSystem();
			plottingSystem.setColorOption(ColorOption.NONE); //this for 1D, not used in this editor
		} catch (Exception ne) {
			logger.error("Cannot locate any plotting systems!", ne);
		}
		traceListener = new ITraceListener.Stub() {
			@Override
			public void traceWillPlot(TraceWillPlotEvent evt) {
				// Does not all update(...) intentionally.
			}
			
			@Override
			public void tracesUpdated(TraceEvent evt) {
//				update(evt);
			}

			@Override
			public void tracesRemoved(TraceEvent evt) {
//				update(evt);
			}

			@Override
			public void tracesAdded(TraceEvent evt) {
//				update(evt);
			}

			@Override
			public void traceCreated(TraceEvent evt) {
//				update(evt);
			}

			@Override
			public void traceUpdated(TraceEvent evt) {
				update(evt);
			}

			@Override
			public void traceAdded(TraceEvent evt) {
//				update(evt);
			}

			@Override
			public void traceRemoved(TraceEvent evt) {
//				update(evt);
			}
			
			@Override
			protected void update(TraceEvent evt) {
				if (evt.getSource() instanceof IImageTrace) {
					System.out.println("ImageEditor: ImageTrace updated!!!");
					imageUpdated( (IImageTrace)evt.getSource() );
				}
			}
		};
	}

	protected void imageUpdated(IImageTrace image) {
		image.repaint();
	}

	protected boolean updateInputIfFilePathEditorInput(IEditorInput input) {
		boolean result = false;
		if( input instanceof FilePathEditorInput ) {
			FilePathEditorInput fPEI = (FilePathEditorInput)input;
			if( fPEI.equalityIDEquals(REMOTED_IMAGE)) {
				fPEI.setName((selectedDisplayImageByRemoteRequest ? "=" : "#") + input.getName());
				result = true;
			}
		}
		return result;
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
//		if( updateInputIfFilePathEditorInput(input) )
//			remotedImageEditor = true;
		if( input instanceof FilePathEditorInput ) {
			FilePathEditorInput fPEI = (FilePathEditorInput)input;
			if( fPEI.equalityIDEquals(REMOTED_IMAGE)) {
				remotedImageEditor = true;
			}
		}
		setSite(site);
		super.setInput(input); //Must not call this.setInput, because there must call editorInputChanged, and for that GUI must be ready which is not ready at this point
		setPartName(input.getName());
	}

	public void setInput(IEditorInput input) {
//		updateInputIfFilePathEditorInput(input);
		super.setInput(input);
		editorInputChanged(); //Must call here, because when content of editor changes, this setInput is called
//		setPartName(input.getName()); //This is done in parent editor by input.getName() after returning from here.
	}

	@Override
	public boolean isDisposed() {
		return disposed;
	}

    @Override
    public void dispose() {
    	if( disposed ) {
    		logger.debug("DEBUG: ImageEditor already disposed");
    		return;
    	}
    	logger.debug("DEBUG: ImageEditor disposing");

		if( crossHairExists() )
			removeCrossHair();
		releaseDetConfig();
		releaseDiffEnvConfig();
		Activator.getDefault().getPreferenceStore().removePropertyChangeListener(this);
       	if (plottingSystem != null) {
       		if( !plottingSystem.isDisposed() ) {
       			plottingSystem.removeTraceListener(traceListener); //Although its dispose clears listeners
       			plottingSystem.dispose();
       		}
     		plottingSystem = null;
     	}
     	imageTrace = null;
     	super.dispose();
     	disposed = true;
     	logger.debug("DEBUG: ImageEditor disposed");
    }

    @Override
	public void setFocus() {
		if (plottingSystem!=null && plottingSystem.getPlotComposite()!=null) {
			plottingSystem.setFocus();
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		int a = 0;
	}

	protected void saveAs(AbstractDataset ds, double min, double max) {
		do {
			FileDialog saveAsDialog = new FileDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.SAVE);
//			Object a = Display.getDefault().getShells()[0];
			saveAsDialog.setFilterNames(new String[] {"PNG Files (*.png)", "All Files (*.*)"});
			saveAsDialog.setFilterExtensions(new String[] {"*.png", "*.*"});
			String path = saveAsDialog.open();
			if( path == null || path.isEmpty() )
				break;
			DataHolder dh = new DataHolder();
			dh.addDataset("image", ds);
			PNGScaledSaver pngSaver = new PNGScaledSaver(path, min, max);
			try {
				pngSaver.saveFile(dh);
			} catch (ScanFileHolderException e) {
				e.printStackTrace();
			}
		} while( false );
	}

	public void doSaveAsOriginal() {
		do {
//			System.out.println("Min and max=" + originalSet.min().doubleValue() + ", " + originalSet.max().doubleValue());
			saveAs(originalSet, originalSet.min().doubleValue(), originalSet.max().doubleValue());
		} while( false );
	}

	@Override
	public void doSaveAs() {
		do {
//			System.out.println("IT.Min and IT.max=" + imageTrace.getMin().doubleValue() + ", " + imageTrace.getMax().doubleValue());
			saveAs((AbstractDataset)imageTrace.getData(), imageTrace.getMin().doubleValue(), imageTrace.getMax().doubleValue());
		} while( false );
	}

	@Override
	public boolean isSaveAsAllowed() {
//		return false;
		return true;
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public void showEditorInput(IEditorInput editorInput) {
		this.setInput(editorInput);
	}

	@Override
	public void setPartName(final String name) {
		//This works only at first, later does not, because the parent should listen
		//to IWorkbenchPartConstants.PROP_PART_NAME and act accordingly.
		String flaggedName = remotedImageEditor ? (selectedDisplayImageByRemoteRequest ? "▶" : "❙❙") + name : name;
		super.setPartName(flaggedName); //Well, now it seems working, since calling it from fileIsReady
	}

    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") final Class clazz) {
//		System.out.println("getAdapter for " + clazz.toString());
		return super.getAdapter(clazz);
	}
    
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	public void createPartControl(Composite parent) {

		final Composite main = new Composite(parent, SWT.NONE);
		final GridLayout gridLayout = new GridLayout(1, false);
		main.setLayout(gridLayout);
		main.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true));
		GridUtils.removeMargins(main);

//		parent.setLayout(new GridLayout(1,false)); ////earlier main
		
//		this.tools = new Composite(main, SWT.RIGHT);
//		tools.setLayout(new GridLayout(2, false));
//		GridUtils.removeMargins(tools);
//		tools.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));

		final ToolBarManager toolMan = new ToolBarManager(SWT.FLAT|SWT.RIGHT);
		final ToolBar toolBar = toolMan.createControl(main);
		toolBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		psfTool = new PSFTool() {
		};
		psfTool.setPlottingSystem(plottingSystem);

		imageFilesWindowWidth = 1;
		/* Top line containing image selector sliders */
		createImageSelectorUI(main);
		/* bottom line containing status and load image controls */
		createImageControlUI(main);

		infoLine = new Composite(main, SWT.NONE); ////earlier tools
		infoLine.setLayout(new GridLayout(1, false));
		infoLine.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		GridUtils.removeMargins(infoLine);
		point = new Label(infoLine, SWT.LEFT);
		point.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		GridUtils.setVisible(point, true);
		point.setBackground(infoLine.getBackground());

		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(this); 

		final MenuManager menuMan = new MenuManager();
		final IActionBars bars = this.getEditorSite().getActionBars();
		//If we specify toolMan, plottingSystem fills it with all tools, and must not modify it.
		//If we do not specify toolMan, plottingSystem creates its own, so our toolMan can be modified,
		//but the main toolManager will also display the tools what we do not want.
		ActionBarWrapper wrapper = new ActionBarWrapper(toolMan,menuMan,null,(IActionBars2)bars);
//		ActionBarWrapper wrapper = new ActionBarWrapper(null, menuMan, null, (IActionBars2)bars);

		final Composite plotComposite = new Composite(main, SWT.NONE);
		plotComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		plotComposite.setLayout(new FillLayout());

		plottingSystem.createPlotPart(plotComposite, getEditorInput().getName(), wrapper, PlotType.IMAGE, this);
		//FYI: plottingSystem.getPlotComposite() is not the plotComposite, it is the canvas on it (lame naming)

		psfAction = new Action(PSF.featureName, IAction.AS_CHECK_BOX ) {
			@Override
			public void run() {
				psfStateSelected();
			}
		};
		psfAction.setId(getClass().getName() + "." + PSF.featureIdentifierName);
		psfAction.setText("Apply " + PSF.featureName);
		psfAction.setToolTipText("Apply " + PSF.featureFullName + " (" + PSF.featureName + ") on the image");
		psfAction.setImageDescriptor(Activator.getImageDescriptor("/icons/psf.png"));
		psfAction.setChecked(Activator.getDefault().getPreferenceStore().getBoolean(EditorConstants.PREFERENCE_APPLY_PSF));

//        IPlotActionSystem actionsys = plottingSystem.getPlotActionSystem();
//        actionsys.fillZoomActions(toolMan);
//        actionsys.fillRegionActions(toolMan);
//        actionsys.fillToolActions(toolMan, ToolPageRole.ROLE_2D);
//        actionsys.fillAnnotationActions(toolMan); //Not implemented
//        actionsys.fillPrintActions(toolMan); //Not implemented
//        actionsys.fillUndoActions(toolMan); //Not implemented

        //The problem with this solution is it does not consider order of tools and separators
//        IToolBarManager toolBarManager = wrapper.getToolBarManager();
//        IContributionItem[] tbmItems = toolBarManager.getItems();
//		for( IContributionItem tbmItem : tbmItems) {
//			if( !(tbmItem instanceof Separator) && toolMan.find(tbmItem.getId()) == null ) {
//				logger.info("id=" + tbmItem.getId() + ", str=" + tbmItem.toString());
//				toolMan.add(tbmElem);
//			}
//		}

		toolMan.add( psfAction );

		MenuAction dropdown = new MenuAction("Resolution rings");
		dropdown.setImageDescriptor(Activator.getImageDescriptor("/icons/resolution_rings.png"));

		standardRings = new Action("Standard rings", Activator.getImageDescriptor("/icons/standard_rings.png")) {
			@Override
			public void run() {
				drawStandardRings();
			}
		};
		standardRings.setChecked(false);
		iceRings = new Action("Ice rings", Activator.getImageDescriptor("/icons/ice_rings.png")) {
			@Override
			public void run() {
				drawIceRings();
			}
		};
		iceRings.setChecked(false);
		calibrantRings = new Action("Calibrant", Activator.getImageDescriptor("/icons/calibrant_rings.png")) {
			@Override
			public void run() {
				drawCalibrantRings();
			}
		};
		calibrantRings.setChecked(false);
		beamCentre = new Action("Beam centre", Activator.getImageDescriptor("/icons/beam_centre.png")) {
			@Override
			public void run() {
				drawBeamCentre();
			}
		};
		beamCentre.setChecked(false);
		
		dropdown.add(standardRings);
		dropdown.add(iceRings);
		dropdown.add(calibrantRings);
		dropdown.add(beamCentre);

		toolMan.add(dropdown);

		//The problem with this solution is it does not consider order of tools and separators
//		IMenuManager menuManager = wrapper.getMenuManager();
//		IContributionItem[] mmItems = menuManager.getItems();
//		for( IContributionItem mmItem : mmItems) {
//			logger.info("id=" + mmItem.getId() + ", str=" + mmItem.toString());
//			menuMan.add(mmItem);
//		}

		MenuAction dviewerDownsamplingAction = new MenuAction("Downsampling type");
		dviewerDownsamplingAction.setId(getClass().getName()+".downsamplingType");
//		dviewerDownsamplingAction.setImageDescriptor(Activator.getImageDescriptor("icons/origins.png"));
		CheckableActionGroup group = new CheckableActionGroup();
		DownsampleType downsampleType = (DownsampleType.values()[ Activator.getDefault().getPreferenceStore().getInt(EditorConstants.PREFERENCE_DOWNSAMPLING_TYPE) ]);
        IAction selectedAction  = null;
        
        String dTypeNames[] = new String[DownsampleType.values().length];
        for (final DownsampleType dType : DownsampleType.values())
        	dTypeNames[dType.getIndex()] = dType.name();
        Arrays.sort(dTypeNames, Collections.reverseOrder());
//        for (final DownsampleType dType : DownsampleType.values()) {
        for (final String dTypeName : dTypeNames) {
        	final DownsampleType dType = DownsampleType.valueOf(dTypeName);
        	final IAction action = new Action(dType.getLabel(), IAction.AS_CHECK_BOX) {
        		public void run() {
        			//We do not store this selection neither as last nor as default (currently only default exists anyway)
//        			Activator.getDefault().getPreferenceStore().setValue(EditorConstants.PREFERENCE_DOWNSAMPLING_TYPE, dType.getLabel());
        			setDownsampleType(dType);
       			    setChecked(true);
        		}
        	};
        	dviewerDownsamplingAction.add(action);
        	group.add(action);
        	
        	if (downsampleType == dType)
        		selectedAction = action;
		}
        
        if (selectedAction!=null)
        	selectedAction.setChecked(true);
        
        menuMan.add(dviewerDownsamplingAction);
        menuMan.insertAfter(dviewerDownsamplingAction.getId(), new Separator(dviewerDownsamplingAction.getId()+".group"));
		
//		if( Activator.getDefault().getPreferenceStore().contains(prefPage) ) {
	    	dviewerPrefAction = new Action("dViewer Preferences", null) {
		    	@Override
		    	public void run() {
					PreferenceDialog pref = PreferencesUtil.createPreferenceDialogOn(PlatformUI.getWorkbench().
							getActiveWorkbenchWindow().getShell(), prefPage, 
							new String[] {}, null, PreferencesUtil.OPTION_FILTER_LOCKED); //uk.ac.diamond.scisoft.analysis.rcp.diffractionViewerPreferencePage
					//uk.ac.diamond.scisoft.analysis.rcp.preference.DiffractionViewerPreferencePage
					if (pref != null)
						pref.open();
		    	}
			};
			dviewerPrefAction.setId(prefPage);
			menuMan.add(dviewerPrefAction);
//		}
    	dviewerSaveAsAction = new Action("Save image as...", null) {
	    	@Override
	    	public void run() {
	    		doSaveAs();
	    	}
		};
		dviewerSaveAsAction.setId(saveAsId);
		menuMan.add(dviewerSaveAsAction);

		dviewerSaveAsOriginalAction = new Action("Save original image as...", null) {
	    	@Override
	    	public void run() {
	    		doSaveAsOriginal();
	    	}
		};
		dviewerSaveAsOriginalAction.setId(saveAsOriginalId);
		menuMan.add(dviewerSaveAsOriginalAction);

		if( menuMan.getSize() > 0 ) {
		    Action menuAction = new Action("", Activator.getImageDescriptor("/icons/DropDown.png")) {
		        @Override
		        public void run() {
	                final Menu mbar = menuMan.createContextMenu(toolBar);
	       		    mbar.setVisible(true);
		        }
		    };
		    menuAction.setId(getClass().getName()+".dropdownMenu");
			toolMan.add(menuAction);
		}

		toolMan.remove("org.csstudio.swt.xygraph.undo.ZoomType.HORIZONTAL_ZOOM");
		toolMan.remove("org.csstudio.swt.xygraph.undo.ZoomType.ZOOM_OUT");
		toolMan.remove("org.csstudio.swt.xygraph.undo.ZoomType.RUBBERBAND_ZOOM");
		toolMan.remove("org.dawb.common.ui.plot.tool.ROLE_1D");
		//site.getActionBars().getToolBarManager().remove("org.csstudio.swt.xygraph.autoscale");
//		toolMan.remove(IToolPage.PlotTool.PERFORM_AUTO_SCALE.getId());
		toolMan.remove("org.csstudio.swt.xygraph.undo.ZoomType.VERTICAL_ZOOM");
		toolMan.remove("org.csstudio.swt.xygraph.undo.ZoomType.ZOOM_IN");
		toolMan.remove("org.csstudio.swt.xygraph.undo.ZoomType.NONE");
		toolMan.remove("org.csstudio.swt.xygraph.undo.ZoomType.PANNING");

		if (toolMan != null)
			toolMan.update(true);

		getEditorSite().setSelectionProvider(plottingSystem.getSelectionProvider());

    	infoPixelTool = new InfoPixelTool(plottingSystem, 1.0) {
    		@Override
    		public void setVisible(boolean visible) {
    			super.setVisible(visible);
    			if( point != null && !point.isDisposed() )
    				point.setVisible(isVisible());
    		}
    		@Override
    		public void roiDragged(ROIEvent evt) {
    			IRegion region = (IRegion) evt.getSource();
    			RegionType rt = region.getRegionType();
    			ROIBase rb = (ROIBase)evt.getROI();
    			if( rt == RegionType.XAXIS_LINE ) {
    				xValues[0] = evt.getROI().getPointX();
			  	} else if( rt == RegionType.YAXIS_LINE ) {
    				yValues[0] = evt.getROI().getPointY();
			  	} else //POINT or whatever
			  		return;
//    			logger.debug("DEBUG: updateRegion:" + region.toString() + ", x=" + region.getROI().getPointX() + ", y=" + region.getROI().getPointY());
    			if( originalSet != null ) { //Checking because rarely it is null at starting (startup problem somewhere)
    				if( (int)xValues[0] < 0 || (int)yValues[0] < 0 )
    					logger.debug( "DEBUG: Too small! " + (int)xValues[0] + ", " + (int)yValues[0] );
    				if( (int)xValues[0] < originalSet.getShape()[1] && (int)yValues[0] < originalSet.getShape()[0] ) {
    					Object oriValue = originalSet.getObject(new int[] {(int)yValues[0], (int)xValues[0]});
//    					Object psfValue = psfSet.getObject(new int[] {(int)cursorImagePosY, (int)cursorImagePosX});
//    					point.setText( String.format("x=%d y=%d oriValue=%s psfValue=%s, res=%s",
//    							(int)cursorImagePosX, (int)cursorImagePosY, oriValue.toString(), psfValue.toString(), infoPixelToolLabelResolution.getText(region)));
    					point.setText( String.format("x=%d y=%d intensity=%s resolution=%s S=%s",
//    							(int)xValues[0], (int)yValues[0], oriValue.toString(), infoPixelToolLabelResolution.getText(region), infoPixelToolLabelQ.getText(region)));
    							(int)xValues[0], (int)yValues[0], oriValue.toString(), getText(region, 8), getText(region, 20)));
    					infoLine.layout(true);
    				} else //invalid position received, it is bug in underlying layer, happens after panning ended and mouse released outside
    					logger.debug( "DEBUG: Too big! " + (int)xValues[0] + ", " + (int)yValues[0] );
    			}
    		}
/*
    		@Override
    		protected void addRegion(IRegion region) {
    			if( getPlottingSystem().getRegion(region.getName()) == null ) {
    				getPlottingSystem().addRegion(region);
//    				getPlottingSystem().removeRegion(region);
    			}
    		}
*/
    		@Override
    		public void regionAdded(RegionEvent evt) {
    			int a = 0;
    		}

    		@Override
    		public void regionRemoved(RegionEvent evt) {
    			int a = 0;
    		}
/*
    		@Override
    		public void mousePressed(MouseEvent evt) {
    			logger.info("button clicked: " + evt.button);
    		}

    		@Override
    		public void mouseReleased(MouseEvent me) {
    		}

    		@Override
    		public void mouseDoubleClicked(MouseEvent me) {
    		}
*/
    	};
//    	infoPixelTool.createControl(top);

//		infoPixelTool.setToolSystem(plottingSystem);
//TODO    	infoPixelTool.setPlottingSystem(plottingSystem);
//TODO		infoPixelToolLabelResolution = new InfoPixelLabelProvider(infoPixelTool, 8); //Resolution ID = 8
//TODO		infoPixelToolLabelQ = new InfoPixelLabelProvider(infoPixelTool, 20); //Q vector = 8
//TODO		infoPixelToolLabelQ.setQscale(1.0);
//		infoPixelTool.setPart(plottingSystem.getPart());


//        plottingSystem.addRegionListener(this);

		psfStateSelected();
		plottingSystem.addTraceListener(traceListener);
        editorInputChanged();
   	}
/*	//TODO
	private void setThreshold() {
		IMetaData md = data.getMetadata();
		if (md != null) {
			if (mainPlotter instanceof DataSetPlotter) {
				try {
					Serializable s = md.getMetaValue("NXdetector:pixel_overload");
					Double threshold = null;

					if (s instanceof String) {
						threshold = Double.valueOf((String) s);
					} else if (s instanceof Number) {
						threshold = ((Number) s).doubleValue();
					}

					if (threshold != null) {
						((DataSetPlotter) mainPlotter).setOverloadThreshold(threshold);
						diffViewMetadata.setThreshold(threshold);
					}
					return;
				} catch (Exception e) {
				}
			}
		}

		IPreferenceStore preferenceStore = AnalysisRCPActivator.getDefault().getPreferenceStore();
		double thresholdFromPrefs;
		if (preferenceStore.isDefault(PreferenceConstants.DIFFRACTION_VIEWER_PIXELOVERLOAD_THRESHOLD))
			thresholdFromPrefs = preferenceStore
					.getDefaultDouble(PreferenceConstants.DIFFRACTION_VIEWER_PIXELOVERLOAD_THRESHOLD);
		else
			thresholdFromPrefs = preferenceStore
					.getDouble(PreferenceConstants.DIFFRACTION_VIEWER_PIXELOVERLOAD_THRESHOLD);
		if (mainPlotter instanceof DataSetPlotter) {
			((DataSetPlotter) mainPlotter).setOverloadThreshold(thresholdFromPrefs);
			diffViewMetadata.setThreshold(thresholdFromPrefs);
		}
	}
*/
/*
	private void initSlider( int amount ){ 
		//if(!label.isDisposed() && label !=null){  
		imageSlider.setValues( 1, 1, amount+1, 1, 1, Math.max(1, amount/5) );
		totalSliderImageLabel.setText( "1" + "/" + amount );
		totalSliderImageLabel.getParent().pack();
		//}  
	}  
*/
	private void sliderMoved( int pos ) {
//		FileWithTag[] toLoadImageFiles = null;
//		synchronized (resultDataset) {
//			int iMax = imageFilesWindowWidth;
//			int firstIndex = pos - 1;
//			toLoadImageFiles = new FileWithTag[iMax];
//			for( int i = 0; i < iMax; i++ )
//				toLoadImageFiles[ i ] = resultDataset.getAllImageFilesElement( firstIndex + i );
//		}
//		loadFilesForPlotting(toLoadImageFiles);
		loadFilesForPlotting(pos - 1, imageFilesWindowWidth);
	}

	private void updateSlider( int sel ) {
		if( imageSlider == null || imageSlider.isDisposed() )
			return;
		synchronized (imageSlider) {
				final int min = 1;
//				final int total = resultDataset.getAllImageFilesLength();
				final int total = fileLoader.getFile().getAllLength();
				
				if( imageFilesWindowWidth > imageSlider.getMaximum() - sel && sel > 1 ) {
					sel = imageSlider.getMaximum() - imageFilesWindowWidth;
				}
				final int selection = Math.max(Math.min(sel,total + 1),min);
//				if( imageSlider.getSelection() == selection )
//					return;
				try {
					imageSliderSelection = selection;
					imageSlider.setValues(selection, min, total+1, imageFilesWindowWidth, 1, Math.max(imageFilesWindowWidth, total/5));
					totalSliderImageLabel.setText( "" + selection + "/" + total + "   ");
					totalSliderImageLabel.getParent().pack();
					sliderMoved( selection );
				} catch (SWTException e) {
					//eat it!  
				}
		}
	}

	private void updateSlider( String filePath ) {
		if( imageSlider == null || imageSlider.isDisposed() )
			return;
		synchronized (imageSlider) {
//				resultDataset.setFilesOrigin(filePath);
//				int pos = resultDataset.getLogicalIndexOfFile(filePath/*, true*/);
//				try {
					fileLoader.setFilePath(filePath);
					int pos = fileLoader.getFile().getIndexOfFile(filePath);
					updateSlider( pos + 1 );
//				} catch (FileNotFoundException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
		}
	}

	private void updateSlider( /*FileWithTag[] files*/ ) {
		if( imageSlider == null || imageSlider.isDisposed() )
			return;
		synchronized (imageSlider) {
//				resultDataset.setAllImageFiles( files ); 
//				updateSlider( resultDataset.getAllImageFilesLength() - imageFilesWindowWidth + 1 );
				updateSlider( fileLoader.getFile().getAllLength() - imageFilesWindowWidth + 1 );
		}
	}

	private void updateSliderByUser( int sel ) {
		if( imageSliderSelection == sel )
			return;
		updateSlider( sel );
	}

	protected boolean isBatchAmountValid( int amount ) {
		return amount >= 1 && amount <= fileLoader.getFile().getAllLength();
	}

	private void updateBatchAmount( int amount ) {
		if( imageSlider == null || imageSlider.isDisposed() )
			return;
		synchronized (imageSlider) {
			if( imageFilesWindowWidth == amount || !isBatchAmountValid(amount) )
				return;
			int oldSel = imageSlider.getSelection();
			int newSel = oldSel;
			if( amount < 1 )
				amount = 1;
			else if( amount > imageSlider.getMaximum() - oldSel && oldSel > 1 ) {
				newSel = imageSlider.getMaximum() - amount;
				if( newSel < 1 ) {
					newSel = 1;
					amount = imageSlider.getMaximum() - newSel;
				}
//				amount = imageSlider.getMaximum() - imageSlider.getSelection();
			}
			imageSlider.setThumb( amount );
			imageFilesWindowWidth = imageSlider.getThumb();
//			if( oldSel != newSel ) //No idea why this case was here. If not used anymore, can be deleted
//				imageSlider.setSelection( newSel );
//			else
				updateSlider( newSel );
/*
			imageSlider.setSelection(imageSlider.getSelection());

			imageFilesWindowWidth = amount;
			imageFilesWindowWidthText.setText( "" + amount );
			imageFilesWindowWidthText.getParent().pack();
			sliderMoved( imageSlider.getSelection() ); //Updates loaded files and draw image
*/
		}
	}

	public void onImageFilesAutoLatestButtonSelected() {
		if( autoFollow != imageFilesAutoLatestButton.getSelection() ) {
			autoFollow = imageFilesAutoLatestButton.getSelection();
			if( autoFollow ) {
	//			imageSlider.setEnabled( false );
				imageFilesAutoLatestThread = new Thread() {
					ExecutableManager imageFilesAutoLatestManager = null;
					protected boolean checkDirectory() {
//						final IPath imageFilename = getPath( getEditorInput() );
						if( fileLoader.isLoading() ) //Not updating slider while any file is loading (else addRequest could lag)
							return false;
//						final FileWithTag[] currentAllImageFiles = AbstractDatasetAndFileSet.listIndexedFilesOf( imageFilename );
//						if( !resultDataset.isDifferentImageFiles(currentAllImageFiles) )
						try {
							if( !fileLoader.refreshNewAllFiles() ) //There was not any change
								return false;
						} catch (FileNotFoundException e) {
							return false;
						}
						final TrackableRunnable runnable = new TrackableRunnable(imageFilesAutoLatestManager) {
							@Override
							public void runThis() {
								updateSlider( /*currentAllImageFiles*/ );
							}
						};
						imageFilesAutoLatestManager = ExecutableManager.addRequest(runnable);
						return true;
					}
					@Override
					public void run() {
						do {
							int sleepTime = 10; //Sleeping some even if directory updated, so user can move slider (and abort this thread)
							if( !checkDirectory() )
								sleepTime = 100;
							try {
								sleep(sleepTime);
							} catch (InterruptedException e) {
								break;
							}
						} while( true );
					}
					@Override
					public void interrupt() {
						if( imageFilesAutoLatestManager != null )
							imageFilesAutoLatestManager.interrupt();
						super.interrupt();
					}
				};
				imageFilesAutoLatestThread.start();
			} else {
				imageFilesAutoLatestThread.interrupt();
	//			imageSlider.setEnabled( true );
			}
		}
	}

	private void updatePsfRadiusSlider(int sel) {
		if (psfRadiusUI == null || psfRadiusUI.isDisposed())
			return;
		synchronized (psfRadiusUI) {
			psfTool.updatePSFRadius(sel);
		}
	}

	public void updateAutomaticDisplayRemotedImage() {
		automaticDisplayRemotedImage.setText(selectedDisplayImageByRemoteRequest ? "❙❙" : "▶");
		automaticDisplayRemotedImage.setToolTipText((selectedDisplayImageByRemoteRequest ? "Do not d" : "D") + "isplay image by remote request");
	}

	public void toggleDisplayImageByRemoteRequest() {
		selectedDisplayImageByRemoteRequest = !selectedDisplayImageByRemoteRequest;
		imageEditorRemotedDisplayState = ImageEditorRemotedDisplayState.togglePlaying(imageEditorRemotedDisplayState);
		updateAutomaticDisplayRemotedImage();
		if( imageEditorRemotedDisplayState.equals(ImageEditorRemotedDisplayState.PLAYING_AND_REMOTE_UPDATED)) {
			editorInputChanged();
		} else {
			fileLoader.interrupt();
			setPartName(getEditorInput().getName());
		}
	}

	private void createImageSelectorUI(Composite parent) {
		final Composite sliderMain = new Composite(parent, SWT.NONE);
		sliderMain.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		sliderMain.setLayout(new GridLayout(8, false));
		GridUtils.removeMargins(sliderMain);
		
		imageSlider = new Slider(sliderMain, SWT.HORIZONTAL);
		imageSlider.setToolTipText("Image selector");
		imageSlider.setThumb(imageFilesWindowWidth);
		imageSliderSelection = imageSlider.getSelection();
//		imageSlider.setBounds(115, 50, 25, 15);
		totalSliderImageLabel = new Label(sliderMain, SWT.NONE);
		totalSliderImageLabel.setToolTipText("Selected image/Number of images");
		totalSliderImageLabel.setText("0/0");
		imageSlider.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
//				imageSlider.setSelection( imageSlider.getSelection() );
				if( autoFollow ) {
					imageFilesAutoLatestButton.setSelection( false );
					//setSelection does not trigger the Selection event because we are in Selection event here already,
					onImageFilesAutoLatestButtonSelected(); //so we have to call it manually, which is lame.
				}
				updateSliderByUser( imageSlider.getSelection() );
			}
		});
		final Label imageFilesWindowWidthLabel = new Label(sliderMain, SWT.NONE);
		imageFilesWindowWidthLabel.setToolTipText("Number of images to sum up");
		imageFilesWindowWidthLabel.setText("Batch Amount");
		imageFilesWindowWidthText = new Text(sliderMain, SWT.BORDER | SWT.RIGHT);
		imageFilesWindowWidthText.setToolTipText(imageFilesWindowWidthLabel.getToolTipText());
		imageFilesWindowWidthText.setText( "" + imageFilesWindowWidth );
		imageFilesWindowWidthText.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		imageFilesWindowWidthText.addVerifyListener(new VerifyListener() {
			@Override
			public void verifyText(VerifyEvent e) {
				try {
					String newValue = StringUtil.replaceRange(imageFilesWindowWidthText.getText(), e.text, e.start, e.end );
					e.doit = isBatchAmountValid( decimalFormat.parse( newValue ).intValue() );
				} catch (ParseException e1) {
					e.doit = e.text.isEmpty();
				}
			}
		});
		imageFilesWindowWidthText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if( imageFilesWindowWidthText == null || imageFilesWindowWidthText.isDisposed() ) return;
				if( !imageFilesWindowWidthText.isEnabled() || imageFilesWindowWidthText.getText().isEmpty() )
					return;
				try {
					updateBatchAmount( decimalFormat.parse( imageFilesWindowWidthText.getText() ).intValue() );
				} catch (ParseException exc) {
					logger.error("Unable to parse batch amount value: " + imageFilesWindowWidthText.getText(), exc);
				}
			}
		});
		autoFollow = false;
		if( !remotedImageEditor ) {
			imageFilesAutoLatestButton = new Button(sliderMain, SWT.CHECK);
			imageFilesAutoLatestButton.setText("Auto latest");
			imageFilesAutoLatestButton.setToolTipText("Automatically scan directory and display last batch");
			imageFilesAutoLatestButton.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					onImageFilesAutoLatestButtonSelected();
				}
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
		} else {
			automaticDisplayRemotedImage = new Button(sliderMain, SWT.PUSH);
//			selectedDisplayImageByRemoteRequest = !selectedDisplayImageByRemoteRequest; //for toggling
			imageEditorRemotedDisplayState = ImageEditorRemotedDisplayState.PLAYING_AND_REMOTE_UPDATED; //for toggling
			updateAutomaticDisplayRemotedImage();
			automaticDisplayRemotedImage.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					toggleDisplayImageByRemoteRequest();
				}
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}
			});
			displayRemotedImageDedicated = new Button(sliderMain, SWT.PUSH);
			displayRemotedImageDedicated.setText("O");
			displayRemotedImageDedicated.setToolTipText("Display image in a dedicated image editor");
			displayRemotedImageDedicated.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetSelected(SelectionEvent e) {
//					filePath = fileLoader.getCollectionDelegate().getAbsolutePath(); //This is nicer from vcf aspect, because vcf://regexp, but we do not know which file to display first
//					filePath = fileLoader.getFile().getAbsolutePath(); //This is bad, because vcf://singlefile, which causes exception when looking for index
					//If we would pass vcf, we should pass it with FileEditorInput instead of FilePathEditorInput
					FilePathEditorInput fPEI = new FilePathEditorInput(fileLoader.getFile().getAbsolutePathWithoutProtocol(), null, fileLoader.getFile().getName());
					try {
						CommonExtension.openEditor(EclipseUtils.getPage(), fPEI, ID, false, false);
					} catch (Exception ex) { //PartInitException, and Exception from uk.ac.diamond.scisoft.analysis.rcp.editors.ImageEditor.createFile
						logger.error("Can not open editor. " + ex.getMessage());
					}
				}
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}
			});
		}

		psfRadiusUI = new SpinnerSlider(sliderMain, SWT.HORIZONTAL);
		psfRadiusUI.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
//		psfRadiusUI.setToolTipText(PSF.featureName + " radius selector");
//		psfRadiusUI.setThumb(1);
		psfRadiusUI.setValues(PSF.featureName + " Radius", (Integer)EditorPreferenceHelper.getStoreValue(Activator.getDefault().getPreferenceStore(), EditorConstants.PREFERENCE_PSF_RADIUS),
				1, 100, 0, 1, 10, 1, 10);
//		psfRadiusUI.setBounds(115, 50, 25, 15);
		psfRadiusUI.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updatePsfRadiusSlider( psfRadiusUI.getSelectionAsInteger() );
			}
		});
		updatePsfRadiusSlider( Activator.getDefault().getPreferenceStore().getInt(EditorConstants.PREFERENCE_PSF_RADIUS) );

	}

	private void createImageControlUI(Composite parent) {
		/**
		 * A text to adjust 7 sized width of GUI displaying value.
		 */
		final String GUIValue7WidthSetter = "0000000";
		final int logScaleMin = 0;
		final int logScaleMax = 31;

//		final boolean isAutoScale = Activator.getDefault().getPreferenceStore().getBoolean(PreferenceConstants.P_AUTOSCALE);

//		Display display = parent.getDisplay();
		controlComposite = new Composite(parent, SWT.NONE);
		controlComposite.setLayout(new GridLayout(7, false));
		controlComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		GridUtils.removeMargins(controlComposite);

		// Minimum original
		Label label = new Label(controlComposite, SWT.NONE); //Column 1
		label.setText("Min Value=");
		label.setToolTipText("The minimum value found in image");
		minValueText = new Text(controlComposite, SWT.RIGHT); //Column 2
		minValueText.setText(GUIValue7WidthSetter);
		minValueText.setEditable(false);
		minValueText.setToolTipText(label.getToolTipText());

		// Suggested minimum
		label = new Label(controlComposite, SWT.NONE); //Column 3
		label.setText("Suggested=");
		label.setToolTipText("The suggested minimum intensity used by the palette");
		suggestedMinimumText = new Text(controlComposite, SWT.RIGHT); //Column 4
		suggestedMinimumText.setText(GUIValue7WidthSetter);
		suggestedMinimumText.setEditable(false);
		suggestedMinimumText.setToolTipText(label.getToolTipText());

		//Empty place
		label = new Label(controlComposite, SWT.NONE); //Column 5
		label.setVisible(false);

		// Minimum current
//		label = new Label(controlComposite, SWT.NONE); //Column 6
//		label.setText("Min Intensity:");
//		label.setToolTipText("The minimum intensity used by the palette");
//		userMinimumScale = new LogScale(controlComposite, SWT.NONE); //Column 7
//		userMinimumScale.setMinimum(logScaleMin);
//		userMinimumScale.setMaximum(logScaleMax);
//		userMinimumScale.setToolTipText("The currently set minimum intensity used by the palette");
//		userMinimumScale.addSelectionListener(new SelectionListener() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				if( userMinimumScale == null || userMinimumScale.isDisposed()) return;
//				final float v = (float)userMinimumScale.getLogicalSelection();
//				updateIntensityMin(v);
//			}
//
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e) {
//				widgetSelected(e);
//			}
//		});
//		label = new Label(controlComposite, SWT.NONE); //Column 8
//		label.setText("Current=");
//		label.setToolTipText(userMinimumScale.getToolTipText());
//		userMinimumText = new Text(controlComposite, SWT.BORDER | SWT.RIGHT); //Column 7
//		userMinimumText.setText(GUIValue7WidthSetter);
//		userMinimumText.setToolTipText(userMinimumScale.getToolTipText());
//		userMinimumText.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
//		userMinimumText.addModifyListener(new ModifyListener() {
//			@Override
//			public void modifyText(ModifyEvent e) {
//				if( userMinimumText == null || userMinimumText.isDisposed()) return;
//				if( !userMinimumText.isEnabled() || userMinimumText.getText().isEmpty() ) return;
//				try {
//					updateIntensityMin(decimalFormat.parse(userMinimumText.getText()).floatValue());
//				} catch (ParseException ex) {
//					logger.warn("Unable to parse minimum value: "+ userMinimumText.getText());
//				}
//			}
//		});
////		userMinimumText.setEnabled(!isAutoScale);
////		userMinimumScale.setEnabled(!isAutoScale);
		userMinimumScale = new SpinnerSlider( controlComposite, SWT.NONE ); //Column 6
		userMinimumScale.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 2, 1));
		userMinimumScale.setToolTipText("The minimum threshold used by the palette");
		userMinimumScale.setValues("Min Threshold", 0,
				logScaleMin, logScaleMax, 3, 1, 10, 1, 10, 0, 11, false); //TODO want digits=3, but does not work in SpinnerSlider yet
		userMinimumScale.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
//				if( userMinimumScale == null || userMinimumScale.isDisposed()) return;
				final double v = (double)userMinimumScale.getSelectionAsDouble();
				System.out.println("GRRR: userMinimumScale.widgetSelected: updateIntensityMin(getSelectionAsDouble=" + v + ")");
				updateIntensityMin(v);
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		userMinimumScale.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
//				System.out.println("GRRR: userMinimumScale.modifyText, doing nothing!");
				final double v = (double)userMinimumScale.getSelectionAsDouble();
				System.out.println("GRRR: userMinimumScale.modifyText, updateIntensityMin(" + v + ")");
				updateIntensityMin(v);
//				userMinimumScale.selectCurrentValue(); //Updating selection (and its dependants) when text changes
//				userMinimumScale.setSelectionAsDouble(userMinimumScale.getSelectionAsDouble()); //Looks funny, but that is the way
			}
		});

		// Maximum original
		label = new Label(controlComposite, SWT.NONE); //Column 1
		label.setText("Max Value=");
		label.setToolTipText("The maximum intensity used by the palette");
		maxValueText = new Text(controlComposite, SWT.RIGHT); //Column 2
		maxValueText.setText(GUIValue7WidthSetter);
		maxValueText.setEditable(false);
		maxValueText.setToolTipText(label.getToolTipText());

		// Suggested maximum
		label = new Label(controlComposite, SWT.NONE); //Column 3
		label.setText("Suggested=");
		label.setToolTipText("The suggested maximum intensity used by the palette");
		suggestedMaximumText = new Text(controlComposite, SWT.RIGHT); //Column 4
		suggestedMaximumText.setText(GUIValue7WidthSetter);
		suggestedMaximumText.setEditable(false);
		suggestedMaximumText.setToolTipText(label.getToolTipText());

		//Use suggested
		Button button = new Button(controlComposite, SWT.PUSH); //Column 5
		button.setText("Use suggested");
		button.setToolTipText("Use suggested value");
		button.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
//				setUserMinimum(getSuggestedMinimum());
//				setUserMaximum(getSuggestedMaximum());
				setIntensityMinMax(getSuggestedMinimum(), getSuggestedMaximum());
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		// Maximum current
//		label = new Label(controlComposite, SWT.NONE); //Column 6
//		label.setText("Max Intensity:");
//		label.setToolTipText("The maximum intensity used by the palette");
//		userMaximumScale = new LogScale(controlComposite, SWT.NONE); //Column 7
//		userMaximumScale.setMinimum(logScaleMin);
//		userMaximumScale.setMaximum(logScaleMax);
//		userMaximumScale.setToolTipText("The currently set maximum intensity used by the palette");
//		label = new Label(controlComposite, SWT.NONE); //Column 8
//		label.setText("Current=");
//		label.setToolTipText(userMaximumScale.getToolTipText());
//		userMaximumScale.addSelectionListener(new SelectionListener() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				if( userMaximumScale == null || userMaximumScale.isDisposed()) return;
//				final float v = (float)userMaximumScale.getLogicalSelection();
//				updateIntensityMax(v);
//			}
//
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e) {
//				widgetSelected(e);
//			}
//		});
//		userMaximumText = new Text(controlComposite, SWT.BORDER | SWT.RIGHT); //Column 9
//		userMaximumText.setToolTipText(userMaximumScale.getToolTipText());
//		userMaximumText.setText(GUIValue7WidthSetter);
//		userMaximumText.setBackground(display.getSystemColor(SWT.COLOR_WHITE));
//		userMaximumText.addModifyListener(new ModifyListener() {
//			@Override
//			public void modifyText(ModifyEvent e) {
//				if( userMaximumText == null || userMaximumText.isDisposed()) return;
//				if( !userMaximumText.isEnabled() || userMaximumText.getText().isEmpty() ) return;
//				try {
//					updateIntensityMax(decimalFormat.parse(userMaximumText.getText()).floatValue());
//				} catch (ParseException ex) {
//					logger.warn("Unable to parse maximum value: "+ userMaximumText.getText());
//				}
//			}
//			;
//		});
////		userMaximumText.setEnabled(!isAutoScale);
////		userMaximumScale.setEnabled(!isAutoScale);
		userMaximumScale = new SpinnerSlider( controlComposite, SWT.NONE ); //Column 6
		userMaximumScale.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 2, 1));
		userMaximumScale.setToolTipText("The maximum threshold used by the palette");
		userMaximumScale.setValues("Max Threshold", 0,
				logScaleMin, logScaleMax, 3, 1, 10, 1, 10, 0, 1, false); //TODO want digits=3, but does not work in SpinnerSlider yet
		userMaximumScale.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
//				if( userMaximumScale == null || userMaximumScale.isDisposed()) return;
				final double v = (double)userMaximumScale.getSelectionAsDouble();
				System.out.println("GRRR: userMaximumScale.widgetSelected: updateIntensityMax(getSelectionAsDouble=" + v + ")");
				updateIntensityMax(v);
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		userMaximumScale.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
//				System.out.println("GRRR: userMinimumScale.modifyText, doing nothing!");
				final double v = (double)userMaximumScale.getSelectionAsDouble();
				System.out.println("GRRR: userMaximumScale.modifyText, updateIntensityMax(" + v + ")");
				updateIntensityMax(v);
//				userMaximumScale.selectCurrentValue(); //Updating selection (and its dependants) when text changes
//				userMaximumScale.setSelectionAsDouble(userMaximumScale.getSelectionAsDouble()); //Looks funny, but that is the way
			}
		});
	}

	public boolean setUserMinimum(final double userMinimum) {
//		if( /*userMinimumText == null || userMinimumText.isDisposed() ||*/ userMinimumScale == null || userMinimumScale.isDisposed() )
//			return false;
//		System.out.println("GRRR: setUserMinimum: userMinimum=" + userMinimum + ", lastUserMinimum=" + lastUserMinimum);
//		if( userMinimum == lastUserMinimum )
//			return false;
//		lastUserMinimum = userMinimum;
////		userMinimumText.setText(ConverterUtils.doubleAsString(userMinimum));
////		userMinimumScale.setLogicalSelection(userMinimum);
		System.out.println("GRRR: setUserMinimum, setSelection(" + userMinimum + ")");
		if( userMinimum == userMinimumScale.getSelectionAsDouble() )
			return false;
		userMinimumScale.setSelection((int)userMinimum); //TODO
//		controlComposite.layout();
		return true;
	}

	public boolean setUserMaximum(final double userMaximum) {
//		if( /*userMaximumText == null || userMaximumText.isDisposed() ||*/ userMaximumScale == null || userMaximumScale.isDisposed() )
//			return false;
//		if( userMaximum == lastUserMaximum )
//			return false;
//		lastUserMaximum = userMaximum;
////		userMaximumText.setText(ConverterUtils.doubleAsString(userMaximum));
////		userMaximumScale.setLogicalSelection(userMaximum);
		System.out.println("GRRR: setUserMaximum, setSelection(" + userMaximum + ")");
		if( userMaximum == userMaximumScale.getSelectionAsDouble() )
			return false;
		userMaximumScale.setSelection((int)userMaximum); //TODO
//		controlComposite.layout();
		return true;
	}

	public double getSuggestedMinimum() { //TODO could check if suggestedMinimumText is null or disposed
		return Double.valueOf(suggestedMinimumText.getText());
	}

	public void setSuggestedMinimum(double suggestedMin) {
		String text = ConverterUtils.doubleAsString(suggestedMin);
		if (suggestedMinimumText != null && !suggestedMinimumText.isDisposed()
				&& !suggestedMinimumText.getText().equals( text )) {
			suggestedMinimumText.setText(text);
			controlComposite.layout();
		}
	}

	public double getSuggestedMaximum() { //TODO could check if suggestedMaximumText is null or disposed
		return Double.valueOf(suggestedMaximumText.getText());
	}

	public void setSuggestedMaximum(double suggestedMax) {
		String text = ConverterUtils.doubleAsString(suggestedMax);
		if (suggestedMaximumText != null && !suggestedMaximumText.isDisposed()
				&& !suggestedMaximumText.getText().equals( text )) {
			suggestedMaximumText.setText(text);
			controlComposite.layout();
		}
	}

	public synchronized void updateImageHistogram() {
		imageDisplayTracker = ExecutableManager.addRequest(new TrackableRunnable(imageDisplayTracker) {
			public void runThis() {
				if( imageTrace != null ) {
					try {
						imageTrace.setImageUpdateActive(false);
						imageTrace.setMin(lastUserMinimum);
						imageTrace.setMax(lastUserMaximum);
					} finally {
						imageTrace.setImageUpdateActive(true);
					}
				}
			}
		});
	}
	
	private void updateIntensityMin(final double v) {
//		if( !setUserMinimum(v) )
//			return;
		if( v == lastUserMinimum )
			return;
		lastUserMinimum = v;
		updateImageHistogram();
	}

	private void updateIntensityMax(final double v) {
//		if( !setUserMaximum(v) )
//			return;
		if( v == lastUserMaximum )
			return;
		lastUserMaximum = v;
		updateImageHistogram();
	}

	private boolean setIntensityMinMax(final double min, final double max) {
		System.out.println("GRRR: setIntensityMinMax, setUserMinimum(" + min + ")");
		boolean minSet = setUserMinimum(min);
		System.out.println("GRRR: setIntensityMinMax, setUserMaximum(" + max + ")");
		boolean maxSet = setUserMaximum(max);
//		if( !minSet && !maxSet )
//			return false;
//		lastUserMinimum = min;
//		lastUserMaximum = max;
//		updateImageHistogram();
		return minSet || maxSet;
	}

//	protected IPath getPath( IEditorInput editorInput ) {
//		final IPath imageFilename;
//		if( editorInput instanceof FileEditorInput )
//			imageFilename = new Path( ((FileEditorInput)editorInput).getURI().getPath() ); 
//		else if( editorInput instanceof FileStoreEditorInput )
//			imageFilename = new Path( ((FileStoreEditorInput)editorInput).getURI().getPath() ); 
//		else {
//			IFile iF = (IFile)editorInput.getAdapter(IFile.class);
//			if( iF != null )
//				imageFilename = iF.getLocation().makeAbsolute();
//			else {
//				logger.error("Cannot determine full path of requested file");
//				return null;
//			}
//		}
//		return imageFilename;
//	}

	protected String getPath( IEditorInput editorInput ) {
		final String result;
		IFile iF = (IFile)editorInput.getAdapter(IFile.class);
		if( iF != null )
			result = iF.getLocation().toOSString();
		else {
			result = EclipseUtils.getFilePath(editorInput);
			if( result == null ) {
				logger.error("Cannot determine the input of this editor: " + editorInput.getName());
				return null;
			}
		}
		return result;
	}


	private void editorInputChanged() {
		setPartName(getEditorInput().getName());
		if( remotedImageEditor ) {
			if( !selectedDisplayImageByRemoteRequest ) {
				if( !ImageEditorRemotedDisplayState.isRemoteUpdated(imageEditorRemotedDisplayState))
					imageEditorRemotedDisplayState = ImageEditorRemotedDisplayState.setRemoteUpdated(imageEditorRemotedDisplayState);
				return;
			}
		}
		editorInputChanged = true;
		if (getEditorInput() instanceof MemoryImageEditorInput) {
			MemoryImageEditorInput miei = (MemoryImageEditorInput)getEditorInput();
			AbstractDataset set = new FloatDataset(miei.getData(), new int[] {miei.getWidth(), miei.getHeight()});
//			ImageModel imageModel = new ImageModel("", miei.getWidth(), miei.getHeight(), miei.getData(), 0);
			if (getEditorInput().getName().startsWith("ExpSimImgInput")) {
			} else {
/*
				logger.debug("DEBUG: First block of received image (imageModel):");
				for( int j = 0; j < 10; j++ ) {
					for( int i = 0; i < 10; i++ ) {
						logger.debug( "DEBUG: " + Integer.toHexString( (int)imageModel.getData(i, j) ) );
					}
					logger.debug();
				}
*/
			}
			updatePlot(set);
		} else {
			if( !remotedImageEditor || selectedDisplayImageByRemoteRequest )
				updateSlider( getPath( getEditorInput() ) );
		}
 	}

	private void setDownsampleType(final DownsampleType downsampleType) {
		if( imageTrace != null && imageTrace.getDownsampleType() != downsampleType ) {
			logger.debug( "DEBUG: Setting DownsampleType from " + imageTrace.getDownsampleType().getLabel() + " to " + downsampleType.getLabel() );
			CommonThreading.execFromUIThreadNowOrSynced(new Runnable() {
				public void run() {
					imageTrace.setDownsampleType(downsampleType);
				}
			});
		}
	}

	private DownsampleType getDownsampleType() {
		if( imageTrace != null ) {
			return imageTrace.getDownsampleType();
		} else
			return null;
	}

	protected void psfStateSelected() {
		psfTool.updatePSFState(psfAction.isChecked());
	}

	private void updatePlot(final AbstractDataset set) {
		createPlot( set, true, null );
	}

	/**
	 * @param contentChanged  true if the content of set changed. Currently it is always true, because why createPlot when content did not change? 
	 */
	private void createPlot(final AbstractDataset set, final boolean contentChanged, IProgressMonitor monitor) {
		originalSet = set;
		try {
			logger.debug("DEBUG: min=" + ((Number)set.min(true)).doubleValue() + ", max=" + ((Number)set.max(true)).doubleValue()
				+ ", mean=" + ((Number)set.mean(true)).doubleValue());
		} catch( NullPointerException e ) {
			logger.debug("Dataset has no minimum, maximum");
		}
		boolean imageCreation = plottingSystem.getTraces().size() == 0; //Dirty fix
//		final ITrace trace = plottingSystem.updatePlot2D( set, null, monitor, editorInputChanged ); //PerformAuto = true => rehistogram on loading
//FIXME I want the 4 parameters updatePlot2D!
		final ITrace trace = plottingSystem.updatePlot2D( set, null, monitor );
		if (trace instanceof IImageTrace) {
			imageTrace = (IImageTrace) trace;
			double min;
			double max;
			final double realMax;
			final double realMean;
			
			realMax = imageTrace.getData().max().doubleValue();
			realMean = ((Number)imageTrace.getData().mean()).doubleValue();
			
			if( editorInputChanged ) { //If input changed, updatePlot2D has already calculated the stats
				min = imageTrace.getMin().doubleValue();
				max = imageTrace.getMax().doubleValue(); //In real, max() is a (weird) mean
			} else {
				try {
					IImageService service = (IImageService)ServiceManager.getService(IImageService.class);
					float stats[] = service.getFastStatistics(imageTrace.getImageServiceBean());
					min = stats[0];
					max = stats[1]; //In real, max() is a (weird) mean
				} catch (Exception ne) {
					logger.error("Internal error cannot process Image histogram!", ne);
					min = imageTrace.getMin().doubleValue();
					max = imageTrace.getMax().doubleValue(); //In real, max() is a (weird) mean
				}
			}
			final double suggestedMin = min;
			final double ourMean = Math.min(Math.ceil(realMean * 6), realMax); //6 is an experimental number, should find out by algorithm
			logger.debug( "createPlot min=" + min + ", max=" + max + ", realMax=" + realMax + ", realMean=" + realMean + ", ourMean=" + ourMean);
			final double suggestedMax = ourMean;
			if( imageCreation ) //If creation, then PSFTool updated trigger is not called, we do it here then
				psfTool.updatePSFMinValue(((Number)imageTrace.getMax()).doubleValue()); //In real, max() is a (weird) mean
//			psfTool.updatePSFState(psfAction.isChecked());
//		if( editorInputChanged || contentChanged /*originalSet != set*/) {
//			long t0 = System.nanoTime();
//			originalSet = set;
//			psfSet = set.synchronizedCopy(); 
//			long t1 = System.nanoTime();
//			logger.debug( "DEBUG: Copying data image took [msec]= " + ( t1 - t0 ) / 1000000 );
//			psf.applyPSF(psfSet, new Rectangle(0, 0, originalSet.getShape()[1], originalSet.getShape()[0]));
//		}
//		Number n = null;
//		if( !editorInputChanged && imageTrace != null ) {
//			//Next line is optimal solution (keeps intensity), but requires changes in DLS source
////			plottingSystem.setPlot2D( !psfAction.isChecked() ? originalSet : psfSet, null, null );
//			//This (updatePlot2D) would be the solution (for keeping intensity) by official DLS source, problem is it calculates and paints the image two times
//			n = imageTrace.getMax();
//		}
//		long t0 = System.nanoTime();
//		final ITrace trace = plottingSystem.updatePlot2D( !psfAction.isChecked() ? originalSet : psfSet, null, monitor );
//		long t1 = System.nanoTime();
//		logger.debug( "DEBUG: Update plot2D took [msec]= " + ( t1 - t0 ) / 1000000 );
//		if (trace instanceof IImageTrace) {
//			if( imageTrace != trace) {
//				imageTrace = (IImageTrace) trace;
//				setDownsampleType(DownsampleType.values()[ Activator.getDefault().getPreferenceStore().getInt(EditorConstants.PREFERENCE_DOWNSAMPLING_TYPE) ]);
//			}
//			if( n != null && !imageTrace.getMax().equals(n) ) {
//				imageTrace.setImageUpdateActive(false);
//				imageTrace.setMax(n);
//				CommonThreading.execFromUIThreadNowOrSynced(new Runnable() {
//					public void run() {
//						imageTrace.setImageUpdateActive(true);
//					}
//				});
//			}
			
			final double fmin = min;

			CommonThreading.execSynced(new Runnable() {
				public void run() {
					minValueText.setText(ConverterUtils.doubleAsString(fmin));
					maxValueText.setText(ConverterUtils.doubleAsString(realMax));
					setSuggestedMinimum(suggestedMin);
					setSuggestedMaximum(suggestedMax);
//					userMinimumScale.setLogicalMinMax(min, realMax);
					userMinimumScale.setMinMax(fmin, realMax);
//					userMaximumScale.setLogicalMinMax(min, realMax);
					userMaximumScale.setMinMax(fmin, realMax);
					if( editorInputChanged ) {
						System.out.println("GRRR: createPlot/editorInputChanged, setUserMinimum(" + suggestedMin + ")");
						setUserMinimum(suggestedMin); //Modifies histogram min, thus recolors the display
						System.out.println("GRRR: createPlot/editorInputChanged, setUserMaximum(" + suggestedMax + ")");
						setUserMaximum(suggestedMax); //Modifies histogram min, thus recolors the display
					}
				}
			});
			if( imageTrace == null ) { //TODO
				int a = 0; }
			imageTrace.setRescaleHistogram(false); //dViewer's default
			if( !crossHairExists() )
				addCrossHair();
			if( !psfToolActivated() )
				psfTool.activate();
		} else {
			if( crossHairExists() )
				removeCrossHair();
			if( psfToolActivated() )
				psfTool.deactivate();
			imageTrace = null;
		}
		editorInputChanged = false;
	}

	protected boolean psfToolActivated() {
		return psfTool != null && psfTool.isActive();
	}

//	protected void addRegion(String jobName, IRegion region) {
//		region.setVisible(false);
//		region.setTrackMouse(true);
//		region.setRegionColor(ColorConstants.red);
//		region.setUserRegion(false); // They cannot see preferences or change it!
//		getPlottingSystem().addRegion(region);
//		region.addMouseListener(this);
//		region.setVisible(false);
//		region.addROIListener(this);
//	}

	protected boolean crossHairExists() {
//		return xHair != null && yHair != null;
		return infoPixelTool != null && infoPixelTool.isActive();
	}

	protected void addCrossHair() {
		CommonThreading.execFromUIThreadNowOrSynced(new Runnable() {
			public void run() {
//		        try {
//					xHair = plottingSystem.createRegion(RegionUtils.getUniqueName("dViewer X", plottingSystem), IRegion.RegionType.XAXIS_LINE);
//			        yHair = plottingSystem.createRegion(RegionUtils.getUniqueName("dViewer Y", plottingSystem), IRegion.RegionType.YAXIS_LINE);
//				} catch (Exception ne) {
//					logger.error("Cannot locate any plotting systems!", ne);
//					xHair = null;
//					yHair = null;
//					return;
//				}
//	        	addRegion("Updating x cross hair", xHair);
//	        	addRegion("Updating y cross hair", yHair);
				infoPixelTool.activate();
			}
		});
	}

	protected void removeCrossHair() {
//		CommonThreading.execFromUIThreadNowOrSynced(new Runnable() {
//			public void run() {
//				plottingSystem.removeRegion(xHair);
//				xHair = null;
//				plottingSystem.removeRegion(yHair);
//				yHair = null;
//			}
//		});
		infoPixelTool.deactivate();
	}

	protected IRegion drawBeamCentreCrosshairs(double[] beamCentre, double length, Color colour, Color labelColour, String nameStub, String labelText) {
		IRegion region;
		try {
			final String regionName = RegionUtils.getUniqueName(nameStub, plottingSystem);
			//do not appear in lineprofile
			region = plottingSystem.createRegion(regionName, RegionType.LINE);
		} catch (Exception e) {
			logger.error("Can't create region", e);
			return null;
		}

		final LinearROI lroi = new LinearROI(length, 0);
		double dbc[] = {(double)beamCentre[0], (double)beamCentre[1]};
		lroi.setMidPoint(dbc);
		lroi.setCrossHair(true);
		region.setROI(lroi);
		region.setRegionColor(colour);
		region.setAlpha(100);
		region.setUserRegion(false);
		region.setShowPosition(false);

//		org.eclipse.draw2d.Label label = new org.eclipse.draw2d.Label(labelText);
//		label.setForegroundColor(labelColour);

		//not to display label
		region.setLabel("");//labelText);
		((AbstractSelectionRegion)region).setShowLabel(true);

		plottingSystem.addRegion(region);
		region.setMobile(false); // NOTE: Must be done **AFTER** calling the addRegion method.
//		region.setLabel(label);  // NOTE: Must be done **AFTER** calling the addRegion method in order for the label colour to be used.

		return region;
	}
	
	protected double[] getBeamCentreAndLength() {
		final int beamCentrePercent = 3;
		double[] beamCentreAndLength;
		if (detConfig != null) {
//			double[] beamLocation = detConfig.getBeamLocation();
			double[] beamLocation = detConfig.getBeamCentreCoords();
			int beamLocationLength = beamLocation.length;
			beamCentreAndLength = Arrays.copyOf(beamLocation, beamLocationLength + 1);
			beamCentreAndLength[ beamLocationLength ] = (1 + Math.sqrt(detConfig.getPx() * detConfig.getPx() + detConfig.getPy() * detConfig.getPy()) * beamCentrePercent / 100);
		} else {
			final AbstractDataset image = (AbstractDataset)imageTrace.getData();
			beamCentreAndLength = new double[] { image.getShape()[1]/2d, image.getShape()[0]/2d, image.getShape()[1] * beamCentrePercent /100.0 };
		}
		return beamCentreAndLength;
	}

	protected void drawBeamCentre() {
		if (beamCentreRegion != null) {
			plottingSystem.removeRegion(beamCentreRegion);
			beamCentreRegion = null;
		}
		if (beamCentre.isChecked()) { 
			double[] beamCentreAndLength = getBeamCentreAndLength();
			DecimalFormat df = new DecimalFormat("#.##");
			String label = df.format(beamCentreAndLength[0]) + "px, " + df.format(beamCentreAndLength[1])+" py";
	    	beamCentreRegion = drawBeamCentreCrosshairs(beamCentreAndLength, beamCentreAndLength[beamCentreAndLength.length - 1], ColorConstants.red, ColorConstants.black, "beam centre", label);
		}
	}

	/*
	 * handle ring drawing, removal and clearing
	 */
	protected void removeRings(ArrayList<IRegion> regionList, ResolutionRingList resolutionRingList) {
		for (IRegion region : regionList) {
			plottingSystem.removeRegion(region);
		}
		regionList.clear();
		resolutionRingList.clear();
	}
	
	protected IRegion drawRing(double[] beamCentre, double innerRadius, double outerRadius, Color colour, Color labelColour, String nameStub, String labelText) {
		IRegion region;
		try {
			final String regionName = RegionUtils.getUniqueName(nameStub, plottingSystem);
			region = plottingSystem.createRegion(regionName, RegionType.RING);
		} catch (Exception e) {
			logger.error("Can't create region", e);
			return null;
		}
	    final SectorROI sroi = new SectorROI(innerRadius, outerRadius);
	    sroi.setPoint(beamCentre[0], beamCentre[1]);
		region.setROI(sroi);
		region.setRegionColor(colour);
		region.setAlpha(100);
		region.setUserRegion(false);
		region.setMobile(false);
		
//		org.eclipse.draw2d.Label label = new org.eclipse.draw2d.Label(labelText);
//		label.setForegroundColor(labelColour);
//		region.setLabel(label);
		region.setLabel(labelText);
		((AbstractSelectionRegion)region).setShowLabel(true);
		((AbstractSelectionRegion)region).setForegroundColor(labelColour);
		
		region.setShowPosition(false);
		plottingSystem.addRegion(region);
		
		return region;
	}
	
	protected IRegion drawResolutionRing(ResolutionRing ring, String name) {
		if( detConfig == null || diffEnv == null ) {
			logger.error("Drawing resolution rings is not possible without metadata.");
			return null;
		}
//		double[] beamCentre = detConfig.getBeamLocation();
		double[] beamCentre = detConfig.getBeamCentreCoords();
//		double radius = Resolution.circularResolutionRingRadius(detConfig, diffEnv, ring.getResolution());
		double radius = DSpacing.radiusFromDSpacing(detConfig, diffEnv, ring.getResolution());
		DecimalFormat df = new DecimalFormat("#.00");
		return drawRing(beamCentre, radius, radius+4.0, ring.getColour(), ring.getColour(), name, df.format(ring.getResolution())+"Å");
	}
	
	protected ArrayList<IRegion> drawResolutionRings(ResolutionRingList ringList, String typeName) {
		ArrayList<IRegion> regions = new ArrayList<IRegion>(); 
		for (int i = 0; i < ringList.size(); i++) {
			IRegion region = drawResolutionRing(ringList.get(i), typeName+i);
			if( region != null )
				regions.add(region);
		}
		return regions;
	}
	
	protected void drawStandardRings() {
		if (standardRingsRegionList != null && standardRingsList != null)
			removeRings(standardRingsRegionList, standardRingsList);
		if (standardRings.isChecked() ) {
			if( diffEnv == null || detConfig == null) {
				logger.error("Drawing resolution rings is not possible without metadata.");
				return;
			}
			standardRingsList = new ResolutionRingList();
			Double numberEvenSpacedRings = 6.0;
			double lambda = diffEnv.getWavelength();
			Vector3d longestVector = detConfig.getLongestVector();
			double step = longestVector.length() / numberEvenSpacedRings; 
			double d, twoThetaSpacing;
			Vector3d toDetectorVector = new Vector3d();
//			Vector3d beamVector = detConfig.getBeamPosition();
			Vector3d beamVector = detConfig.getBeamCentrePosition();
//			double[] beamCentre = detConfig.getBeamCentreCoords();
			for (int i = 0; i < numberEvenSpacedRings - 1; i++) {
				// increase the length of the vector by step.
				longestVector.normalize();
				longestVector.scale(step + (step * i));
	
				toDetectorVector.add(beamVector, longestVector);
				twoThetaSpacing = beamVector.angle(toDetectorVector);
				d = lambda / Math.sin(twoThetaSpacing);
				standardRingsList.add(new ResolutionRing(d, true, ColorConstants.yellow, false, true, true));
			}
			standardRingsRegionList = drawResolutionRings(standardRingsList, "standard");
		}
	}

	protected void drawIceRings() {
		if (iceRingsRegionList!=null && iceRingsList!=null)
			removeRings(iceRingsRegionList, iceRingsList);
		if (iceRings.isChecked()) {
			iceRingsList = new ResolutionRingList();
		
			for (double res : iceResolution) {
				iceRingsList.add(new ResolutionRing(res, true, ColorConstants.blue, true, false, false));
			}
			iceRingsRegionList = drawResolutionRings(iceRingsList, "ice");
		}
	}
	
	protected void drawCalibrantRings() {
		if (calibrantRingsRegionList!=null && calibrantRingsList != null) {
			removeRings(calibrantRingsRegionList, calibrantRingsList);
		}
		if (calibrantRings.isChecked()) {
			calibrantRingsList = new ResolutionRingList();

			IPreferenceStore preferenceStore = AnalysisRCPActivator.getDefault().getPreferenceStore();
			@SuppressWarnings("unused")
			String standardName;
			if (preferenceStore.isDefault(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_NAME))
				standardName = preferenceStore.getDefaultString(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_NAME);
			else
				standardName = preferenceStore.getString(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_NAME);

			String standardDistances;
			if (preferenceStore.isDefault(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_DISTANCES))
				standardDistances = preferenceStore
				.getDefaultString(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_DISTANCES);
			else
				standardDistances = preferenceStore.getString(PreferenceConstants.DIFFRACTION_VIEWER_STANDARD_DISTANCES);

			ArrayList<Double> dSpacing = new ArrayList<Double>();
			StringTokenizer st = new StringTokenizer(standardDistances, ",");
			while (st.hasMoreTokens()) {
				String temp = st.nextToken();
				dSpacing.add(Double.valueOf(temp));
			}
			for (double d : dSpacing) {
				calibrantRingsList.add(new ResolutionRing(d, true, ColorConstants.red, true, false, false));
			}
			calibrantRingsRegionList = drawResolutionRings(calibrantRingsList, "calibrant");
		}
	}

	//TODO later this could be built into PHA or else where the array is fully iterated
	protected void convertAboveCutoffToError(AbstractDataset dataset, double cutoff) {
		//TODO implement this for all kind of datasets
		if( !(dataset instanceof IntegerDataset) )
			throw new RuntimeException("This kind of dataset is not supported yet");
		IntegerDataset iDS = (IntegerDataset)dataset;
		int[] data = iDS.getData();
		int iMax = data.length;
		for( int i = 0; i < iMax; i++ ) {
			if( data[ i ] > cutoff )
				data[ i ] = BAD_PIXEL_VALUE;
			if( data[ i ] > 8000000 )
				logger.debug("DEBUG: " + i + ".=" + data[i]);
		}
	}

	private void loadFilesForPlotting(int from, int amount) {
		fileLoader.loadFiles(from, amount);
	}

	private long getHashCode(AbstractDataset dataset) {
		return dataset.hashCode();
	}

	@Override
	public void fileIsReady(Object source, IProgressMonitor monitor) {
		if( source instanceof FileLoader ) {
			FileLoader fileLoader = (FileLoader)source; //Since using only 1 file loader, this must be same as this.fileLoader
			final AbstractDataset resultSet = fileLoader.getMergedDataset();
			long hashCode = getHashCode(resultSet); //TODO Calculate a hashcode of dataset and compare to previous to see if it changes!!!
			System.out.println("Dataset HashCode=" + hashCode);
			IMetaData localMetaData = resultSet.getMetadata();
			if( localMetaData instanceof IDiffractionMetadata ) {
				localDiffractionMetaData = (IDiffractionMetadata)localMetaData;
				captureDetConfig(localDiffractionMetaData.getDetector2DProperties());
				captureDiffEnvConfig(localDiffractionMetaData.getDiffractionCrystalEnvironment());
			} else {
				releaseDetConfig();
				releaseDiffEnvConfig();
			}
/* TODO Could implement something like this aborting when switching to NOT_PLAYING while loading in remote display mode,
   but have to be careful because for example at this point the file is loaded in fileloader, how to undo it?
   At the moment when opening image from remote display window, it loads the file found in fileloader, because
   the input might have been changed thus it can not be used.
   The solution could be a totally separated loader, image creator, and when everything is ready, could check if paused
   the playing, and if yes, then drop the separately created stuff, else display it as soon as possible.
*/
//			if( remotedImageEditor && ImageEditorRemotedDisplayState.isNotPlaying(imageEditorRemotedDisplayState))
//				return;
			createPlot(resultSet, true, monitor);
			logger.debug("Setting name to " + resultSet.getName());
			if( fileLoader.getLoadedLength() > 0 ) { //Checking for sure
				CommonThreading.execFromUIThreadNowOrSynced(new Runnable() {
					public void run() {
						setPartName(resultSet.getName());
					}
				});
			}
		}
	}

//	private void loadFilesForPlotting(final FileWithTag[] toLoadImageFiles) {
//		final TrackableJob job = new TrackableJob(imageLoaderManager, "Read image data") {
//			Vector<FileWithTag> toLoadImageFilesJob = new Vector<FileWithTag>( Arrays.asList(toLoadImageFiles) );
//			AbstractDataset set = null;
//
//			public IStatus processImage(FileWithTag imageFile, boolean add) {
//				IMetaData localMetaData = null;
//				boolean loadedAtLeast2;
//				synchronized (resultDataset) {
//					if( isAborting() )
//						return Status.CANCEL_STATUS;
//					loadedAtLeast2 = resultDataset.size() > 1;
//				}
//				if( add || loadedAtLeast2 ) { //When removing last set, no need to load it
//					final String filePath = imageFile.getAbsolutePath();
//					try {
////						imageModel = ImageModelFactory.getImageModel(filePath);
//						final ILoaderService service = (ILoaderService)ServiceManager.getService(ILoaderService.class);
//						set = service.getDataset(filePath).clone(); //TODO check if set is null
//						localMetaData = set.getMetadata();
//						if (!(localMetaData instanceof IDiffractionMetadata))
//							throw new RuntimeException("File has no diffraction metadata");
//					} catch (Throwable e) {
//						logger.error("Cannot load file "+filePath, e);
//						return Status.CANCEL_STATUS;
//					}
//				}
//				if( isAborting() )
//					return Status.CANCEL_STATUS;
//				final int ind = 143336;
//				//Warning: getMergedDataset updates the dataset, which slows down execution if batch amount is big
////				if( resultDataset.getMergedDataset() == null )
////					logger.debug("HRMM, before [add=" + add + "], s[ind]=" + set.getElementLongAbs(ind));
////				else
////					logger.debug("HRMM, before [add=" + add + "], rD[ind]=" + resultDataset.getMergedDataset().getElementLongAbs(ind) + ", s[ind]=" + set.getElementLongAbs(ind));
//				Double cutoff = Double.NaN;
//				try {
//					IDiffractionMetadata localDiffractionMetaData2 = (IDiffractionMetadata)localMetaData;
////					Serializable s = localDiffractionMetaData2.getMetaValue("NXdetector:pixel_overload"); //This would be if GDAMetadata would be available in CBFLoader
//					cutoff = Double.parseDouble(localDiffractionMetaData2.getMetaValue("Count_cutoff").split("counts")[0]); //TODO little bit hardcoded
//					logger.debug("Converting values above cutoff=" + cutoff);
////					convertAboveCutoffToError(set, threshold);
//				} catch (Exception e) {
//				}
//				final double BAD_PIXEL_VALUE = -1; //TODO hardcoded
//				synchronized (resultDataset) {
//					if( isAborting() )
//						return Status.CANCEL_STATUS;
//					if( add ) {
//
//						resultDataset.add( set, imageFile, cutoff, BAD_PIXEL_VALUE );
//					} else {
//						resultDataset.remove( set, imageFile, cutoff, BAD_PIXEL_VALUE );
//					}
//					//Warning: getMergedDataset updates the dataset, which slows down execution if batch amount is big
////					logger.debug("HRMM, after  [add=" + add + "], rD[ind]=" + resultDataset.getMergedDataset().getElementLongAbs(ind) + ", s[ind]=" + set.getElementLongAbs(ind));
//				}
//				return Status.OK_STATUS;
//			}
//				
//			public IStatus runThis(IProgressMonitor monitor) {
//				/* Since running this and others aswell through imageLoaderManager,
//				 * the single access of loading data is guaranteed.
//				 */
//				IStatus result = Status.CANCEL_STATUS;
//				do {
//					Vector<FileWithTag> adding;
//					Vector<FileWithTag> removing;
//					synchronized (resultDataset) {
//						if( isAborting() )
//							break;
//						adding = resultDataset.getFilesToAdd(toLoadImageFilesJob);
//						removing = resultDataset.getFilesToRemove(toLoadImageFilesJob);
//						if( adding.size() + removing.size() > toLoadImageFilesJob.size() ) {
//							adding = toLoadImageFilesJob;
//							removing.clear();
//							logger.debug("Optimizing => clearing, adding.size=" + adding.size() + ", removing.size=" + removing.size() + ", toLoadImageFilesJob.size=" + toLoadImageFilesJob.size());
//							resultDataset.clear();
//							releaseDetConfig();
//							releaseDiffEnvConfig();
//						}
//					}
//					result = Status.OK_STATUS;
//					for( FileWithTag i : adding ) {
//						if( isAborting() )
//							break;
//						logger.debug("EHM, adding " + (Integer)i.getTag() + ". file");
//						result = processImage(i, true);
//						if( result != Status.OK_STATUS )
//							break;
//					}
//					if( result != Status.OK_STATUS || isAborting() )
//						break;
//					for( FileWithTag i : removing ) {
//						if( isAborting() )
//							break;
//						logger.debug("EHM, reming " + (Integer)i.getTag() + ". file");
//						result = processImage(i, false);
//						if( result != Status.OK_STATUS )
//							break;
//					}
//					if( result != Status.OK_STATUS || isAborting() )
//						break;
//					IMetaData localMetaData = resultDataset.getMergedDataset().getMetadata();
//					localDiffractionMetaData = (IDiffractionMetadata)localMetaData;
//					detConfig = localDiffractionMetaData.getDetector2DProperties();
//					diffEnv = localDiffractionMetaData.getDiffractionCrystalEnvironment();
//					detConfig.addDetectorPropertyListener(ImageEditor.this);
//					diffEnv.addDiffractionCrystalEnvironmentListener(ImageEditor.this);
//					createPlot(resultDataset.getMergedDataset(), true, monitor);
//					logger.debug("Setting name to " + resultDataset.getName());
//					if( resultDataset.size() > 0 ) { //Checking for sure
//						CommonThreading.execFromUIThreadNowOrSynced(new Runnable() {
//							public void run() {
//								setPartName(resultDataset.getName());
//							}
//						});
//					}
//					result = Status.OK_STATUS;
//				} while( false );
//				if( isAborting() ) {
//					setAborted();
//					return Status.CANCEL_STATUS;
//				}
//				return result;
//			}
//		};
//		job.setUser(false);
//		job.setPriority(Job.BUILD);
//		imageLoaderManager = ExecutableManager.setRequest(job);
//	}

	protected void captureDetConfig(DetectorProperties newDetConfig) {
		if( newDetConfig != detConfig ) {
			releaseDetConfig();
			detConfig = newDetConfig;
			detConfig.addDetectorPropertyListener(this);
		}
	}

	protected void releaseDetConfig() {
		if( detConfig != null ) {
			detConfig.removeDetectorPropertyListener(this);
			detConfig = null;
		}
	}

	protected void captureDiffEnvConfig(DiffractionCrystalEnvironment newDiffEnv) {
		if( newDiffEnv != diffEnv ) {
			releaseDiffEnvConfig();
			diffEnv = newDiffEnv;
			diffEnv.addDiffractionCrystalEnvironmentListener(this);
		}
	}

	protected void releaseDiffEnvConfig() {
		if( diffEnv != null ) {
			diffEnv.removeDiffractionCrystalEnvironmentListener(this);
			diffEnv = null;
		}
	}

    public AbstractPlottingSystem getPlottingSystem() {
    	return this.plottingSystem;
    }

	@Override
	public boolean isApplicable(String filePath, String extension,
			String perspectiveId) {
//		IPreferenceStore preferenceStore = AnalysisRCPActivator.getDefault().getPreferenceStore();
		final String dviewerEnabled = "org.embl.cca.dviewer.enabled";
		return JavaSystem.getPropertyAsBoolean(dviewerEnabled);
	}

	@Override
	public void detectorPropertiesChanged(DetectorPropertyEvent evt) {
		if( evt.hasBeamCentreChanged() ) {
//		String property = evt.getPropertyName();
//		if ("Beam Center".equals(property)) {
			drawBeamCentre();
			drawStandardRings();
			drawIceRings();
			drawCalibrantRings();
		}
		else if( evt.hasOriginChanged() ) {
//		else if ("Origin".equals(property)) {
			drawBeamCentre();
			drawStandardRings();
			drawIceRings();
			drawCalibrantRings();
		}
	}
	
	@Override
	public void diffractionCrystalEnvironmentChanged(DiffractionCrystalEnvironmentEvent evt) {
		if( evt.hasWavelengthChanged() ) {
//		String property = evt.getPropertyName();
//		if ("Wavelength".equals(property)) {
			drawStandardRings();
			drawIceRings();
			drawCalibrantRings();
		}
	}

	@Override
	public void diffractionMetadataCompositeChanged(DiffractionMetadataCompositeEvent evt) {
		if( evt.hasBeamCentreChanged() ) {
//		String property = evt.getPropertyName();
//		if ("Beam Center".equals(property)) {
			if (beamCentre.isChecked()) {
				beamCentre.setChecked(false);
				plottingSystem.removeRegion(beamCentreRegion);
			}
			else {
				beamCentre.setChecked(true);
				drawBeamCentre();
			}
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
//		Object o = event.getOldValue();
		if (EditorConstants.PREFERENCE_DOWNSAMPLING_TYPE.equals(event.getProperty())) {
			DownsampleType currentDType = getDownsampleType(); 
			if( currentDType != null && currentDType == DownsampleType.values()[ Integer.valueOf((String)event.getOldValue()) ])
				setDownsampleType(DownsampleType.values()[ Integer.valueOf((String)event.getNewValue()) ]);
		} else if (EditorConstants.PREFERENCE_APPLY_PSF.equals(event.getProperty())) {
			Boolean currentApplyPsf = psfAction.isChecked();
			if( currentApplyPsf == (Boolean)event.getOldValue() ) {
				psfAction.setChecked((Boolean)event.getNewValue());
				psfAction.run();
			}
		} else if (EditorConstants.PREFERENCE_PSF_RADIUS.equals(event.getProperty())) {
//			int currentPsfRadius = psf.getRadius();
//			if( currentPsfRadius == (Integer)event.getOldValue() ) {
//				updatePsfRadiusSlider((Integer)event.getNewValue());
//			}
		}
	}

}
