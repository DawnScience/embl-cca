/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.embl.cca.dviewer.ui.editors;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.StringTokenizer;
import java.util.TreeSet;

import javax.vecmath.Vector3d;

import org.dawb.common.services.ILoaderService;
import org.dawb.common.services.ServiceManager;
import org.dawb.common.ui.editors.IEditorExtension;
import org.dawb.common.ui.menu.CheckableActionGroup;
import org.dawb.common.ui.menu.MenuAction;
import org.dawb.common.ui.plot.AbstractPlottingSystem;
import org.dawb.common.ui.plot.AbstractPlottingSystem.ColorOption;
import org.dawb.common.ui.plot.PlotType;
import org.dawb.common.ui.plot.PlottingFactory;
import org.dawb.common.ui.plot.region.IRegion;
import org.dawb.common.ui.plot.region.IRegion.RegionType;
import org.dawb.common.ui.plot.region.ROIEvent;
import org.dawb.common.ui.plot.region.RegionEvent;
import org.dawb.common.ui.plot.region.RegionUtils;
import org.dawb.common.ui.plot.tool.IToolPageSystem;
import org.dawb.common.ui.plot.trace.IImageTrace;
import org.dawb.common.ui.plot.trace.IImageTrace.DownsampleType;
import org.dawb.common.ui.plot.trace.ITrace;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.ui.util.GridUtils;
import org.dawb.common.ui.views.HeaderTablePage;
import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawb.workbench.plotting.system.swtxy.selection.AbstractSelectionRegion;
import org.dawb.workbench.plotting.tools.InfoPixelLabelProvider;
import org.dawb.workbench.plotting.tools.InfoPixelTool;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.MouseEvent;
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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
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
import org.eclipse.ui.ide.FileStoreEditorInput;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.Page;
import org.embl.cca.dviewer.Activator;
import org.embl.cca.dviewer.plotting.tools.PSFTool;
import org.embl.cca.dviewer.ui.editors.preference.EditorConstants;
import org.embl.cca.dviewer.ui.editors.preference.EditorPreferenceHelper;
import org.embl.cca.utils.general.Disposable;
import org.embl.cca.utils.imageviewer.FilenameCaseInsensitiveComparator;
import org.embl.cca.utils.imageviewer.MemoryImageEditorInput;
import org.embl.cca.utils.imageviewer.WildCardFileFilter;
import org.embl.cca.utils.threading.CommonThreading;
import org.embl.cca.utils.threading.ExecutableManager;
import org.embl.cca.utils.threading.TrackableJob;
import org.embl.cca.utils.threading.TrackableRunnable;
import org.embl.cca.utils.ui.widget.SpinnerSlider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.FloatDataset;
import uk.ac.diamond.scisoft.analysis.diffraction.DetectorProperties;
import uk.ac.diamond.scisoft.analysis.diffraction.DetectorPropertyEvent;
import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
import uk.ac.diamond.scisoft.analysis.diffraction.IDetectorPropertyListener;
import uk.ac.diamond.scisoft.analysis.diffraction.Resolution;
import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
import uk.ac.diamond.scisoft.analysis.io.IMetaData;
import uk.ac.diamond.scisoft.analysis.rcp.AnalysisRCPActivator;
import uk.ac.diamond.scisoft.analysis.rcp.preference.PreferenceConstants;
import uk.ac.diamond.scisoft.analysis.roi.LinearROI;
import uk.ac.diamond.scisoft.analysis.roi.ROIBase;
import uk.ac.diamond.scisoft.analysis.roi.ResolutionRing;
import uk.ac.diamond.scisoft.analysis.roi.ResolutionRingList;
import uk.ac.diamond.scisoft.analysis.roi.SectorROI;

/**
 * ImageEditor
 * 
 * @author Gábor Náray
 * 
 */
public class ImageEditor extends EditorPart implements IReusableEditor, IEditorExtension, IShowEditorInput, IPropertyChangeListener, IDetectorPropertyListener /*, MouseListener, IROIListener, IRegionListener*/
	, Disposable {
	/**
	 * Plug-in ID.
	 */
	public static final String ID = "org.embl.cca.dviewer.ui.editors.ImageEditor";
	
	private static Logger logger = LoggerFactory.getLogger(ImageEditor.class);

	protected boolean disposed = false;

	final String prefPage = "org.embl.cca.dviewer.ui.editors.preference.EditorPreferencePage";

	// This view is a composite of two other views.
	protected AbstractPlottingSystem plottingSystem;	
	protected IImageTrace imageTrace;
	private boolean editorInputChanged = false;
//	protected IPropertyChangeListener propertyChangeListener;
//	protected IDetectorPropertyListener detectorPropertyListener;
	IDiffractionMetadata localDiffractionMetaData;
	DetectorProperties detConfig;
	DiffractionCrystalEnvironment diffEnv;

//	protected IRegion xHair, yHair;
	protected InfoPixelTool infoPixelTool;
//	protected double cursorImagePosX, cursorImagePosY; 
	protected InfoPixelLabelProvider infoPixelToolLabelResolution;
	protected Label point;
	protected Composite top, topLeft, topRight;

//	protected PSF psf;
	protected PSFTool psfTool;
	protected Action psfAction;
	protected Action dviewerPrefAction;

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
	private File[] allImageFiles;
	private TreeSet<File> loadedImageFiles; //Indices in loadedImagesFiles which are loaded
	boolean autoFollow;
	Button imageFilesAutoLatestButton;
	private SpinnerSlider psfRadiusUI;
	private Label psfRadiusSliderLabel;
	ExecutableManager psfRadiusManager = null;

	AbstractDataset resultImageModel = null;
	static private NumberFormat decimalFormat = NumberFormat.getNumberInstance();

	ExecutableManager imageLoaderManager = null;
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

	public ImageEditor() {
		try {
//	        psf = new PSF( Activator.getDefault().getPreferenceStore().getInt(EditorConstants.PREFERENCE_PSF_RADIUS) );
	        this.plottingSystem = PlottingFactory.createPlottingSystem();
	        plottingSystem.setColorOption(ColorOption.NONE); //this for 1D, not used in this editor
		} catch (Exception ne) {
			logger.error("Cannot locate any plotting systems!", ne);
		}
	}

	public void setInput(IEditorInput input) {
		super.setInput(input);
		setPartName(input.getName());
		editorInputChanged();
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
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

		top = new Composite(main, SWT.NONE); ////earlier tools
		top.setLayout(new GridLayout(2, false));
		top.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        GridUtils.removeMargins(top);
        
		topLeft = new Composite(top, SWT.NONE);
		topLeft.setLayout(new GridLayout(1, false));
		topLeft.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
        GridUtils.removeMargins(topLeft);

		point = new Label(topLeft, SWT.LEFT);
		point.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		GridUtils.setVisible(point, true);
		point.setBackground(topLeft.getBackground());

		topRight = new Composite(top, SWT.NONE); ////earlier tools
		topRight.setLayout(new GridLayout(1, false));
		topRight.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        GridUtils.removeMargins(topRight);

	    final ToolBarManager toolMan = new ToolBarManager(SWT.FLAT|SWT.RIGHT);
	    final ToolBar toolBar = toolMan.createControl(topRight);
	    toolBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		psfTool = new PSFTool() {
		};
		psfTool.setPlottingSystem(plottingSystem);

		loadedImageFiles = new TreeSet<File>();
		imageFilesWindowWidth = 1;
		/* Top line containing image selector sliders */
		createImageSelectorUI(main);

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

		psfAction = new Action("PSF", IAction.AS_CHECK_BOX ) {
	    	@Override
	    	public void run() {
	    		psfStateSelected();
	    	}
        };
        psfAction.setId(getClass().getName()+".psf");
        psfAction.setText("Apply PSF");
        psfAction.setToolTipText("Apply PointSpreadFunction (PSF) on the image");
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
//        IMenuManager menuManager = wrapper.getMenuManager();
//        IContributionItem[] mmItems = menuManager.getItems();
//		for( IContributionItem mmItem : mmItems) {
//			logger.info("id=" + mmItem.getId() + ", str=" + mmItem.toString());
//			menuMan.add(mmItem);
//		}

	    MenuAction dviewerDownsamplingAction = new MenuAction("Downsampling type");
	    dviewerDownsamplingAction.setId(getClass().getName()+".downsamplingType");
//	    dviewerDownsamplingAction.setImageDescriptor(Activator.getImageDescriptor("icons/origins.png"));
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

		if (toolMan != null)
			toolMan.update(true);

		getEditorSite().setSelectionProvider(plottingSystem.getSelectionProvider());

    	infoPixelTool = new InfoPixelTool() {
    		@Override
    		public void roiDragged(ROIEvent evt) {
    			IRegion region = (IRegion) evt.getSource();
    			RegionType rt = region.getRegionType();
    			if (rt == RegionType.POINT) {
    				return;
    			}
    			if( rt == RegionType.XAXIS_LINE ) {
    				xValues[0] = evt.getROI().getPointX();
			  	} else if( rt == RegionType.YAXIS_LINE ) {
    				yValues[0] = evt.getROI().getPointY();
			  	}
//    			System.out.println("updateRegion:" + region.toString() + ", x=" + region.getROI().getPointX() + ", y=" + region.getROI().getPointY());
    			if( originalSet != null ) { //Checking because rarely it is null at starting (startup problem somewhere)
    				if( (int)xValues[0] < 0 || (int)yValues[0] < 0 )
    					System.out.println( "Too small! " + (int)xValues[0] + ", " + (int)yValues[0] );
    				if( (int)xValues[0] < originalSet.getShape()[1] && (int)yValues[0] < originalSet.getShape()[0] ) {
    					Object oriValue = originalSet.getObject(new int[] {(int)yValues[0], (int)xValues[0]});
//    					Object psfValue = psfSet.getObject(new int[] {(int)cursorImagePosY, (int)cursorImagePosX});
    	    			ROIBase rb = evt.getROI();
//    					point.setText( String.format("x=%d y=%d oriValue=%s psfValue=%s, res=%s",
//    							(int)cursorImagePosX, (int)cursorImagePosY, oriValue.toString(), psfValue.toString(), infoPixelToolLabelResolution.getText(region)));
    					point.setText( String.format("x=%d y=%d oriValue=%s, res=%s",
    							(int)xValues[0], (int)yValues[0], oriValue.toString(), infoPixelToolLabelResolution.getText(region)));
    					top.layout(true);
    				} else //invalid position received, it is bug in underlying layer, happens after panning ended and mouse released outside
    					System.out.println( "Too big! " + (int)xValues[0] + ", " + (int)yValues[0] );
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

    		@Override
    		public void mousePressed(MouseEvent evt) {
    		}

    		@Override
    		public void mouseReleased(MouseEvent me) {
    		}

    		@Override
    		public void mouseDoubleClicked(MouseEvent me) {
    		}
    	};
//    	infoPixelTool.createControl(top);
		infoPixelTool.setToolSystem(plottingSystem);
    	infoPixelTool.setPlottingSystem(plottingSystem);
		infoPixelToolLabelResolution = new InfoPixelLabelProvider(infoPixelTool, 8); //Resolution ID = 8
//		infoPixelTool.setPart(plottingSystem.getPart());


//        plottingSystem.addRegionListener(this);

		psfStateSelected();
        editorInputChanged();
   	}

/*
	private void initSlider( int amount ){ 
		//if(!label.isDisposed() && label !=null){  
		imageSlider.setValues( 1, 1, amount+1, 1, 1, Math.max(1, amount/5) );
		totalSliderImageLabel.setText( "1" + "/" + amount );
		totalSliderImageLabel.getParent().pack();
		//}  
	}  
*/
	private void updateSliderByUser( int sel ) {
		if( imageSlider == null || imageSlider.isDisposed() )
			return;
		if( imageSliderSelection == sel )
			return;
		updateSlider( sel );
	}

	private void updateSlider( int sel ) {
		if( imageSlider == null || imageSlider.isDisposed() )
			return;
		synchronized (imageSlider) {
			final int min = 1;
			final int total = allImageFiles.length;
			final int selection = Math.max(Math.min(sel,total + 1),min);
			
			try {  
//			if( imageSlider.getSelection() == selection )
//				return;
				imageSliderSelection = selection;
				imageFilesWindowWidth = imageSlider.getThumb();
				imageSlider.setValues(selection, min, total+1, imageFilesWindowWidth, 1, Math.max(imageFilesWindowWidth, total/5));
				totalSliderImageLabel.setText( "" + selection + "/" + total + "   ");
				totalSliderImageLabel.getParent().pack();
				sliderMoved( selection );
			} catch (SWTException e) {  
				//eat it!  
			}  
		}
	}
	
	private void updateBatchAmount( int amount ) {
		if( imageSlider == null || imageSlider.isDisposed() )
			return;
		synchronized (imageSlider) {
			if( imageFilesWindowWidth == amount )
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
			if( oldSel != newSel )
				imageSlider.setSelection( newSel );
			else
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

	private void sliderMoved( int pos ) {
		File[] toLoadImageFiles = null;
		synchronized (allImageFiles) {
			int iMax = imageFilesWindowWidth;
			toLoadImageFiles = new File[iMax];
			for( int i = 0; i < iMax; i++ )
				toLoadImageFiles[ i ] = allImageFiles[ pos - 1 + i ];
		}
		loadFilesForPlotting(toLoadImageFiles);
	}

	public void onImageFilesAutoLatestButtonSelected() {
		if( autoFollow != imageFilesAutoLatestButton.getSelection() ) {
			autoFollow = imageFilesAutoLatestButton.getSelection();
			if( autoFollow ) {
	//			imageSlider.setEnabled( false );
				imageFilesAutoLatestThread = new Thread() {
					ExecutableManager imageFilesAutoLatestManager = null;
					protected boolean checkDirectory() {
						final IPath imageFilename = getPath( getEditorInput() );
						final File[] currentAllImageFiles = listIndexedFilesOf( imageFilename );
						TreeSet<File> currentAllImageFilesSet = new TreeSet<File>( Arrays.asList(currentAllImageFiles) );
						TreeSet<File> allImageFilesSet = new TreeSet<File>( Arrays.asList(allImageFiles) );
						if( currentAllImageFilesSet.containsAll(allImageFilesSet)
								&& allImageFilesSet.containsAll(currentAllImageFilesSet) )
							return false;
						if( imageLoaderManager.isAlive() )
							return false;
						final TrackableRunnable runnable = new TrackableRunnable(imageFilesAutoLatestManager) {
							@Override
							public void runThis() {
								synchronized (imageSlider) {
									allImageFiles = currentAllImageFiles; 
									updateSlider( allImageFiles.length - imageFilesWindowWidth + 1 );
								}
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
	
	private void createImageSelectorUI(Composite parent) {
		final Composite sliderMain = new Composite(parent, SWT.NONE);
		sliderMain.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
		sliderMain.setLayout(new GridLayout(7, false));
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
		imageFilesAutoLatestButton = new Button(sliderMain, SWT.CHECK);
		imageFilesAutoLatestButton.setText("Auto latest");
		imageFilesAutoLatestButton.setToolTipText("Automatically scan directory and display last batch");
		autoFollow = false;
		imageFilesAutoLatestButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				onImageFilesAutoLatestButtonSelected();
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		psfRadiusUI = new SpinnerSlider(sliderMain, SWT.HORIZONTAL);
		psfRadiusUI.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, true, 1, 1));
		psfRadiusUI.setToolTipText("PSF radius selector");
//		psfRadiusUI.setThumb(1);
		psfRadiusUI.setValues("PSF Radius", (Integer)EditorPreferenceHelper.getStoreValue(Activator.getDefault().getPreferenceStore(), EditorConstants.PREFERENCE_PSF_RADIUS),
				1, 100, 0, 1, 10, 1, 10);
//		psfRadiusUI.setBounds(115, 50, 25, 15);
		psfRadiusSliderLabel = new Label(sliderMain, SWT.NONE);
		psfRadiusSliderLabel.setToolTipText("Selected PSF radius");
		psfRadiusSliderLabel.setText("0");
		psfRadiusUI.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				updatePsfRadiusSlider( psfRadiusUI.getSelection() );
			}
		});
		updatePsfRadiusSlider( Activator.getDefault().getPreferenceStore().getInt(EditorConstants.PREFERENCE_PSF_RADIUS) );

	}

	protected IPath getPath( IEditorInput editorInput ) {
		final IPath imageFilename;
		if( editorInput instanceof FileEditorInput )
			imageFilename = new Path( ((FileEditorInput)editorInput).getURI().getPath() ); 
		else if( editorInput instanceof FileStoreEditorInput )
			imageFilename = new Path( ((FileStoreEditorInput)editorInput).getURI().getPath() ); 
		else {
			IFile iF = (IFile)editorInput.getAdapter(IFile.class);
			if( iF != null )
				imageFilename = iF.getLocation().makeAbsolute();
			else {
				logger.error("Cannot determine full path of requested file");
				return null;
			}
		}
		return imageFilename;
	}

	protected File[] listIndexedFilesOf( IPath imageFilename ) {
		File[] result = null;
		String q = imageFilename.removeFileExtension().lastSegment().toString();
		String r = q.replaceAll("[0-9]*$", "");
		int len = q.length() - r.length();
		for( int i = 0; i < len; i++ )
		  r += "?";
		r += "." + imageFilename.getFileExtension();
		result = new File(imageFilename.removeLastSegments(1).toString()).listFiles( new WildCardFileFilter(r) );
		Arrays.sort( result, new FilenameCaseInsensitiveComparator() );
		return result;
	}


	private void editorInputChanged() {
		editorInputChanged = true;
		if (getEditorInput() instanceof MemoryImageEditorInput) {
			MemoryImageEditorInput miei = (MemoryImageEditorInput)getEditorInput();
			AbstractDataset set = new FloatDataset(miei.getData(), new int[] {miei.getWidth(), miei.getHeight()});			
//			ImageModel imageModel = new ImageModel("", miei.getWidth(), miei.getHeight(), miei.getData(), 0);
			if ("ExpSimImgInput".equals(getEditorInput().getName())) {
			} else {
/*
				System.out.println("First block of received image (imageModel):");
				for( int j = 0; j < 10; j++ ) {
					for( int i = 0; i < 10; i++ ) {
						System.out.print( " " + Integer.toHexString( (int)imageModel.getData(i, j) ) );
					}
					System.out.println();
				}
*/
			}
			updatePlot(set);
		} else {
			final IPath imageFilename = getPath( getEditorInput() );
			allImageFiles = listIndexedFilesOf( imageFilename );
			String actFname = imageFilename.lastSegment().toString();
			int pos;
			for (pos = 0; pos < allImageFiles.length; pos++ )
				if (allImageFiles[pos].getName().equals(actFname))
					break;				
			updateSlider( pos + 1 ); //it calls (and must call) createPlot()
		}
 	}

	private void setDownsampleType(final DownsampleType downsampleType) {
		if( imageTrace != null && imageTrace.getDownsampleType() != downsampleType ) {
			System.out.println( "Setting DownsampleType from " + imageTrace.getDownsampleType().getLabel() + " to " + downsampleType.getLabel() );
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

	private void createPlot(final AbstractDataset set, boolean contentChanged, IProgressMonitor monitor) {
		originalSet = set;
		final ITrace trace = plottingSystem.updatePlot2D( set, null, monitor );
		if (trace instanceof IImageTrace) {
//			imageTrace = (IImageTrace) trace;
//			psfTool.updatePSFState(psfAction.isChecked());
//		if( editorInputChanged || contentChanged /*originalSet != set*/) {
//			long t0 = System.nanoTime();
//			originalSet = set;
//			psfSet = set.synchronizedCopy(); 
//			long t1 = System.nanoTime();
//			System.out.println( "DEBUG: Copying data image took [msec]= " + ( t1 - t0 ) / 1000000 );
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
//		System.out.println( "DEBUG: Update plot2D took [msec]= " + ( t1 - t0 ) / 1000000 );
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
		return psfTool.isActive();
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
		return infoPixelTool.isActive();
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

		region.setLabel(labelText);
		((AbstractSelectionRegion)region).setShowLabel(true);

		plottingSystem.addRegion(region);
		region.setMobile(false); // NOTE: Must be done **AFTER** calling the addRegion method.
//		region.setLabel(label);  // NOTE: Must be done **AFTER** calling the addRegion method in order for the label colour to be used.

		return region;
	}
	
	protected double[] getBeamCentreAndLength() {
		double[] beamCentreAndLength;
		if (detConfig != null) {
			double[] beamLocation = detConfig.getBeamLocation();
			int beamLocationLength = beamLocation.length;
			beamCentreAndLength = Arrays.copyOf(beamLocation, beamLocationLength + 1);
			beamCentreAndLength[ beamLocationLength ] = (1 + Math.sqrt(detConfig.getPx() * detConfig.getPx() + detConfig.getPy() * detConfig.getPy()) * 0.01);
		} else {
			final AbstractDataset image = imageTrace.getData();
			beamCentreAndLength = new double[] { image.getShape()[1]/2d, image.getShape()[0]/2d, image.getShape()[1]/100 };
		}
		return beamCentreAndLength;
	}

	protected void drawBeamCentre() {
		if (!beamCentre.isChecked()) {
			if (beamCentreRegion != null) {
				plottingSystem.removeRegion(beamCentreRegion);
				beamCentreRegion = null;
			}
			return;
		}
		double[] beamCentreAndLength = getBeamCentreAndLength();
		DecimalFormat df = new DecimalFormat("#.##");
		String label = df.format(beamCentreAndLength[0]) + "px, " + df.format(beamCentreAndLength[1])+"py";
    	beamCentreRegion = drawBeamCentreCrosshairs(beamCentreAndLength, beamCentreAndLength[beamCentreAndLength.length - 1], ColorConstants.red, ColorConstants.black, "beam centre", label);
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
	
	protected IRegion drawRing(int[] beamCentre, double innerRadius, double outerRadius, Color colour, Color labelColour, String nameStub, String labelText) {
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
		int[] beamCentre = detConfig.pixelCoords(detConfig.getBeamPosition());
		double radius = Resolution.circularResolutionRingRadius(detConfig, diffEnv, ring.getResolution());
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
		if (!standardRings.isChecked()) {
			if (standardRingsRegionList != null && standardRingsList != null)
				removeRings(standardRingsRegionList, standardRingsList);
			return;
		}
		if (diffEnv!= null && detConfig != null) {
			standardRingsList = new ResolutionRingList();
			Double numberEvenSpacedRings = 6.0;
			double lambda = diffEnv.getWavelength();
			Vector3d longestVector = detConfig.getLongestVector();
			double step = longestVector.length() / numberEvenSpacedRings; 
			double d, twoThetaSpacing;
			Vector3d toDetectorVector = new Vector3d();
			Vector3d beamVector = detConfig.getBeamPosition();
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
		if (!iceRings.isChecked()) {
			if (iceRingsRegionList!=null && iceRingsList!=null)
				removeRings(iceRingsRegionList, iceRingsList);
			return;
		}
		iceRingsList = new ResolutionRingList();
		
		for (double res : iceResolution) {
			iceRingsList.add(new ResolutionRing(res, true, ColorConstants.blue, true, false, false));
		}
		iceRingsRegionList = drawResolutionRings(iceRingsList, "ice");
	}
	
	protected void drawCalibrantRings() {
		// Remove rings if unchecked
		if (!calibrantRings.isChecked()) {
			if (calibrantRingsRegionList!=null && calibrantRingsList != null) {
				removeRings(calibrantRingsRegionList, calibrantRingsList);
			}
		}
		else {
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
	
	private void loadFilesForPlotting(final File[] toLoadImageFiles) {
		final TrackableJob job = new TrackableJob(imageLoaderManager, "Read image data") {
			TreeSet<File> toLoadImageFilesJob = new TreeSet<File>( Arrays.asList(toLoadImageFiles) );
//			ImageModel imageModel = null;
			AbstractDataset set = null;

			public IStatus processImage(File imageFile, boolean add) {
				if( add || loadedImageFiles.size() > 1 ) {
					final String filePath = imageFile.getAbsolutePath();
					try {
//						imageModel = ImageModelFactory.getImageModel(filePath);
						final ILoaderService service = (ILoaderService)ServiceManager.getService(ILoaderService.class);
						set = service.getDataset(filePath).clone();
					} catch (Throwable e) {
						logger.error("Cannot load file "+filePath, e);
						return Status.CANCEL_STATUS;
					}
					if( isAborting() )
						return Status.OK_STATUS;
					try {
						IMetaData localMetaData = set.getMetadata();
						if (localMetaData instanceof IDiffractionMetadata) {
							localDiffractionMetaData = (IDiffractionMetadata)localMetaData;
							//TODO handling metadata when multiloading
							detConfig = localDiffractionMetaData.getDetector2DProperties();
							diffEnv = localDiffractionMetaData.getDiffractionCrystalEnvironment();
							detConfig.addDetectorPropertyListener(ImageEditor.this);
						} else {
							//TODO handling metadata when multiloading
							releaseDetConfig();
							detConfig = null;
							diffEnv = null;
						}
					} catch (Exception e) {
						logger.error("Could not create diffraction experiment objects");
					}
					if( loadedImageFiles.size() == 0 ) {
						if( add )
							resultImageModel = set;
					} else {
						if( add )
							resultImageModel.iadd( set );
						else
							resultImageModel.isubtract( set );
					}
				}
				if( add )
					loadedImageFiles.add( imageFile );
				else {
					loadedImageFiles.remove( imageFile );
					if( loadedImageFiles.size() == 0 )
						resultImageModel = null;
				}
				return Status.OK_STATUS;
			}
				
			public IStatus runThis(IProgressMonitor monitor) {
				/* Since running this and others aswell through imageLoaderManager,
				 * the single access of loading data is guaranteed.
				 */
				IStatus result = Status.CANCEL_STATUS;
				do {
					if( isAborting() )
						break;
					TreeSet<File> adding = new TreeSet<File>( toLoadImageFilesJob );
					adding.removeAll( loadedImageFiles );
					TreeSet<File> removing = new TreeSet<File>( loadedImageFiles );
					removing.removeAll( toLoadImageFilesJob );
					if( adding.size() + removing.size() > toLoadImageFilesJob.size() ) {
						adding = toLoadImageFilesJob;
						removing.clear();
						loadedImageFiles.clear();
					}
					for( File i : adding ) {
						if( isAborting() )
							break;
						result = processImage(i, true);
						if( result != Status.OK_STATUS )
							break;
					}
					for( File i : removing ) {
						if( isAborting() )
							break;
						result = processImage(i, false);
						if( result != Status.OK_STATUS )
							break;
					}
					if( isAborting() )
						break;
					AbstractDataset resultImageModelDivided = resultImageModel;
//					if( loadedImageFiles.size() > 1 ) {
//						resultImageModelDivided = resultImageModel.clone();
//						int divider = loadedImageFiles.size();
//						resultImageModelDivided.idivide( divider );
////						float[] fsetdata = resultImageModelDivided.getData();
////						int jMax = fsetdata.length;
////						for( int j = 0; j < jMax; j++ )
////							fsetdata[ j ] /= divider;
//					}

					if( isAborting() )
						break;
					createPlot(resultImageModelDivided, true, monitor);
					if( loadedImageFiles.size() > 0 ) { //Checking for sure
						CommonThreading.execFromUIThreadNowOrSynced(new Runnable() {
							public void run() {
								setPartName(loadedImageFiles.first().getName());
							}
						});
					}
					result = Status.OK_STATUS;
				} while( false );
				if( isAborting() ) {
					setAborted();
					return Status.CANCEL_STATUS;
				}
				return result;
			}
		};
		job.setUser(false);
		job.setPriority(Job.BUILD);
		imageLoaderManager = ExecutableManager.setRequest(job);
	}

	protected void releaseDetConfig() {
		if( detConfig != null ) {
			detConfig.removeDetectorPropertyListener(this);
			detConfig = null;
		}
	}

	@Override
	public boolean isDisposed() {
		return disposed;
	}

    @Override
    public void dispose() {
    	if( disposed ) {
        	System.out.println("ImageEditor already disposed");
    		return;
    	}
    	System.out.println("ImageEditor disposing");
		if( crossHairExists() )
			removeCrossHair();
		releaseDetConfig();
		Activator.getDefault().getPreferenceStore().removePropertyChangeListener(this);
       	if (plottingSystem != null) {
       		if( !plottingSystem.isDisposed() )
       			plottingSystem.dispose();
     		plottingSystem = null;
     	}
     	imageTrace = null;
     	super.dispose();
     	disposed = true;
    	System.out.println("ImageEditor disposed");
    }

    @Override
	public void setFocus() {
		if (plottingSystem!=null && plottingSystem.getPlotComposite()!=null) {
			plottingSystem.setFocus();
		}
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		
	}

	@Override
	public void doSaveAs() {
		
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public void showEditorInput(IEditorInput editorInput) {
		this.setInput(editorInput);		
	}

	public void setPartName(final String name) {
		super.setPartName("dViewer"); //name
	}

    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") final Class clazz) {
		
		if (clazz == Page.class) {
			return new HeaderTablePage(EclipseUtils.getFilePath(getEditorInput()));
		} else if (clazz == IToolPageSystem.class) {
			return plottingSystem;
		}
		
		return super.getAdapter(clazz);
	}
    
    public AbstractPlottingSystem getPlottingSystem() {
    	return this.plottingSystem;
    }

	@Override
	public boolean isApplicable(String filePath, String extension,
			String perspectiveId) {
		return Boolean.getBoolean("org.embl.cca.dviewer.enabled");
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		Object o = event.getOldValue();
		if (event.getProperty() == EditorConstants.PREFERENCE_DOWNSAMPLING_TYPE) {
			DownsampleType currentDType = getDownsampleType(); 
			if( currentDType != null && currentDType == DownsampleType.values()[ Integer.valueOf((String)event.getOldValue()) ])
				setDownsampleType(DownsampleType.values()[ Integer.valueOf((String)event.getNewValue()) ]);
		} else if (event.getProperty() == EditorConstants.PREFERENCE_APPLY_PSF) {
			Boolean currentApplyPsf = psfAction.isChecked();
			if( currentApplyPsf == (Boolean)event.getOldValue() ) {
				psfAction.setChecked((Boolean)event.getNewValue());
				psfAction.run();
			}
		} else if (event.getProperty() == EditorConstants.PREFERENCE_PSF_RADIUS) {
//			int currentPsfRadius = psf.getRadius();
//			if( currentPsfRadius == (Integer)event.getOldValue() ) {
//				updatePsfRadiusSlider((Integer)event.getNewValue());
//			}
		}
	}

	@Override
	public void detectorPropertiesChanged(DetectorPropertyEvent evt) {
		// TODO Auto-generated method stub
		String property = evt.getPropertyName();
		if ("Beam Center".equals(property)) {
			drawBeamCentre();
		}
	}

//	@Override
//	public void mousePressed(MouseEvent me) {
//		// TODO Auto-generated method stub
//		int a = 0;		
//		
//	}
//
//	@Override
//	public void mouseReleased(MouseEvent me) {
//		// TODO Auto-generated method stub
//		int a = 0;		
//		
//	}
//
//	@Override
//	public void mouseDoubleClicked(MouseEvent me) {
//		// TODO Auto-generated method stub
//		int a = 0;		
//		
//	}
//
//	@Override
//	public void roiDragged(ROIEvent evt) {
//		// TODO Auto-generated method stub
//		IRegion region = (IRegion) evt.getSource();
//		RegionType rt = region.getRegionType();
//		if( rt == RegionType.XAXIS_LINE )
//			cursorImagePosX = evt.getROI().getPointX();
//		else if( rt == RegionType.YAXIS_LINE )
//			cursorImagePosY = evt.getROI().getPointY();
//		if( originalSet != null ) { //Checking because rarely it is null at starting (startup problem somewhere)
//			if( (int)cursorImagePosX < 0 || (int)cursorImagePosY < 0 )
//				System.out.println( "Too small! " + (int)cursorImagePosX + ", " + (int)cursorImagePosY );
//			if( (int)cursorImagePosX < originalSet.getShape()[1] && (int)cursorImagePosY < originalSet.getShape()[0] ) {
//				Object oriValue = originalSet.getObject(new int[] {(int)cursorImagePosY, (int)cursorImagePosX});
//				Object psfValue = psfSet.getObject(new int[] {(int)cursorImagePosY, (int)cursorImagePosX});
//				point.setText( String.format("x=%d y=%d oriValue=%s psfValue=%s", (int)cursorImagePosX, (int)cursorImagePosY, oriValue.toString(), psfValue.toString()));
//				top.layout(true);
//			} else //invalid position received, it is bug in underlying layer, happens after panning ended and mouse released outside
//				System.out.println( "Too big! " + (int)cursorImagePosX + ", " + (int)cursorImagePosY );
//		}
//	}
//
//	@Override
//	public void roiChanged(ROIEvent evt) {
//		// TODO Auto-generated method stub
//		int a = 0;		
//		
//	}
//
//	@Override
//	public void regionCreated(RegionEvent evt) {
//		// TODO Auto-generated method stub
//		int a = 0;		
//		
//	}
//
//	@Override
//	public void regionAdded(RegionEvent evt) {
//		// TODO Auto-generated method stub
//		int a = 0;		
//		IRegion region = (IRegion) evt.getSource();
//		region.addMouseListener(this);
////		region.setVisible(false);
//		region.addROIListener(this);
//		
//	}
//
//	@Override
//	public void regionRemoved(RegionEvent evt) {
//		// TODO Auto-generated method stub
//		int a = 0;		
//		IRegion region = (IRegion) evt.getSource();
//		region.removeMouseListener(this);
//		region.removeROIListener(this);
//		
//	}
//
//	@Override
//	public void regionsRemoved(RegionEvent evt) {
//		// TODO Auto-generated method stub
//		int a = 0;		
//		
//	}

}
