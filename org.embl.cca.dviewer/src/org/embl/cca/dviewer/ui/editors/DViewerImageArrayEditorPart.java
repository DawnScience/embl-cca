package org.embl.cca.dviewer.ui.editors;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumSet;
import java.util.StringTokenizer;

import org.dawb.common.ui.menu.MenuAction;
import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.ui.util.GridUtils;
import org.dawb.common.ui.widgets.ActionBarWrapper;
import org.dawb.common.util.list.ListenerList;
import org.dawb.workbench.ui.editors.zip.ZipUtils;
import org.dawnsci.common.widgets.editor.ITitledEditor;
import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.plotting.api.trace.IImageTrace.DownsampleType;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.ApplicationWindow;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IActionBars2;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IShowEditorInput;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.misc.UIListenerLogging;
import org.eclipse.ui.internal.registry.PerspectiveDescriptor;
import org.eclipse.ui.menus.AbstractContributionFactory;
import org.eclipse.ui.menus.IContributionRoot;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.part.EditorPart;
import org.eclipse.ui.part.IWorkbenchPartOrientation;
import org.eclipse.ui.services.IServiceLocator;
import org.embl.cca.dviewer.DViewerActivator;
import org.embl.cca.dviewer.ui.editors.preference.EditorPreferenceInitializer;
import org.embl.cca.utils.datahandling.FilePathEditorInput;
import org.embl.cca.utils.datahandling.MemoryDatasetEditorInput;
import org.embl.cca.utils.datahandling.file.FileLoader;
import org.embl.cca.utils.datahandling.file.IFileLoaderListener;
import org.embl.cca.utils.datahandling.text.StringUtils;
import org.embl.cca.utils.errorhandling.ExceptionUtils;
import org.embl.cca.utils.extension.CommonExtension;
import org.embl.cca.utils.general.ISomethingChangeListener;
import org.embl.cca.utils.general.SomethingChangeEvent;
import org.embl.cca.utils.threading.CommonThreading;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;

public class DViewerImageArrayEditorPart extends EditorPart implements ITitledEditor, IReusableEditor, IShowEditorInput, IDViewerControllable, IFileLoaderListener {
	public static final String ID = "org.embl.cca.dviewer.ui.editors.DViewerImageArrayEditorPart";

	private static final Logger logger = LoggerFactory.getLogger(DViewerImageArrayEditorPart.class);

	public final static int BATCH_SIZE_MAX = 999;
	public final static int BATCH_SIZE_MIN = 1;

	protected final static AbstractDataset EMPTY_DATASET = new IntegerDataset(new int [] {}, 0, 0);
	protected final static MemoryDatasetEditorInput EMPTY_DATASET_INPUT = new MemoryDatasetEditorInput(EMPTY_DATASET);

	public final static String REMOTED_IMAGE = "Remoted Image";
	protected boolean remotedImageEditor = false;
	protected boolean autoDisplayRemotedImage = true;
	protected DViewerRemotedDisplayState remotedDisplayState = DViewerRemotedDisplayState.PLAYING_AND_REMOTE_UPDATED;

	protected static IEditorPart dummyEditorPart = null;
	protected static IEditorReference dummyEditorReference = null;
	protected IEditorPart plotDataEditorPart = null;

	protected final FileLoader fileLoader;
//	protected final Thread imageFilesAutoLatestThread;
	protected final AutoSelectLatestNewImageJob autoSelectLatestNewImageJob;
	protected boolean autoSelectLatestNewImage = false; //TODO Could be property
	protected int batchSize = 1;
	protected int batchIndex = -1;
	protected final Boolean imageArrayLock = new Boolean(true); //This is a lock, has no value

	protected final static EnumSet<DViewerRemotedDisplayState> notPlayingSet = EnumSet.of(DViewerRemotedDisplayState.NOT_PLAYING_AND_REMOTE_UPDATED, DViewerRemotedDisplayState.NOT_PLAYING);
	protected final static EnumSet<DViewerRemotedDisplayState> playingSet = EnumSet.complementOf(notPlayingSet);
	protected final static EnumSet<DViewerRemotedDisplayState> notUpdatedSet = EnumSet.of(DViewerRemotedDisplayState.NOT_PLAYING, DViewerRemotedDisplayState.PLAYING);
	protected final static EnumSet<DViewerRemotedDisplayState> updatedSet = EnumSet.complementOf(notUpdatedSet);
	protected static enum DViewerRemotedDisplayState {
		NOT_PLAYING_AND_REMOTE_UPDATED, PLAYING_AND_REMOTE_UPDATED, NOT_PLAYING, PLAYING;
		public static DViewerRemotedDisplayState togglePlaying(final DViewerRemotedDisplayState remotedDisplayState) {
			switch (remotedDisplayState) {
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

		public static DViewerRemotedDisplayState setRemoteUpdated(final DViewerRemotedDisplayState remotedDisplayState) {
			switch (remotedDisplayState) {
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
		public static boolean isRemoteUpdated(final DViewerRemotedDisplayState remotedDisplayState) {
			return updatedSet.contains(remotedDisplayState);
		}
		public static boolean isNotPlaying(final DViewerRemotedDisplayState remotedDisplayState) {
			return notPlayingSet.contains(remotedDisplayState);
		}
	}

	/**
	 * The container widget.
	 */
	protected Composite container;

	protected final DViewerListenerManager listenerManager;

	protected class AutoSelectLatestNewImageJob extends Job {
		protected class SelectLatestImageBatchJob extends Job {
			public SelectLatestImageBatchJob() {
				super("Select latest image batch");
				setUser(false);
				setSystem(true);
				setPriority(Job.SHORT);
			}

			public void reschedule() {
				cancel();
				schedule();
			}
			
			public IStatus run(final IProgressMonitor monitor) {
				onImageArraySizeUpdated();
				selectLastBatch();
				return Status.OK_STATUS;
			}
		}

		protected final SelectLatestImageBatchJob selectLatestBatchImageJob = new SelectLatestImageBatchJob();

		public AutoSelectLatestNewImageJob() {
			super("Auto select latest new image");
			setUser(false);
			setSystem(true);
			setPriority(Job.SHORT);
		}

		public void reschedule() {
			cancel();
			schedule();
		}

		public IStatus run(final IProgressMonitor monitor) {
			final long time = System.currentTimeMillis();
//			System.out.println("AutoSelectLatestNewJob started at " + System.currentTimeMillis());
			monitor.beginTask("Auto selecting latest new image", IProgressMonitor.UNKNOWN);
			final int sleepTime = checkDirectory() ? 10 : 100;
			if( monitor.isCanceled() )
				return Status.CANCEL_STATUS;
			schedule(Math.max(sleepTime - (System.currentTimeMillis() - time), 0));
			return Status.OK_STATUS;
		}

		protected boolean checkDirectory() {
			if( fileLoader.isLoading() ) //Not updating slider while any file is loading (else it could lag)
				return false;
			try {
				if( !fileLoader.refreshNewAllFiles() ) //There was not any change
					return false;
			} catch (final FileNotFoundException e) {
				return false;
			}
			selectLatestBatchImageJob.reschedule();
			return true;
		}
	}

	public DViewerImageArrayEditorPart() {
		fileLoader = new FileLoader();
		fileLoader.addFileLoaderListener(this);
		autoSelectLatestNewImageJob = new AutoSelectLatestNewImageJob();
		listenerManager = new DViewerListenerManager();
	}

	@Override
	public void init(final IEditorSite site, final IEditorInput input) throws PartInitException{
		setSite(site);
		if( input instanceof FilePathEditorInput ) {
			final FilePathEditorInput fPEI = (FilePathEditorInput)input;
			if( fPEI.equalityIDEquals(REMOTED_IMAGE)) {
				remotedImageEditor = true;
			}
		}
		setInput(input, false);
	}

	@Override
	public void showEditorInput(final IEditorInput editorInput) {
		setInput(editorInput);
	}

	@Override
	public void setInput(final IEditorInput input) {
		logger.debug("setInput(IEditorInput) called");
		setInput(input, true);
	}

	protected void setInput(final IEditorInput input, final boolean createData) {
		super.setInput(input);
		setPartName(getEditorInput().getName());
		if (createData)
			editorInputChanged();
		logger.debug("setInput(IEditorInput, boolean=" + createData + ") called");
	}

	@Override
	public void setPartName(final String name) {
		final String flaggedName = isRemoted() ? (isDisplayingImageByRemoteRequest() ? "▶" : "❙❙") + name : name;
		super.setPartName(flaggedName);
	}

	@Override
	public void setPartTitle(final String name) {
		setPartName(name);	
	}

	/**
	 * Get the orientation of the editor.
	 * 
	 * @param editor
	 * @return int the orientation flag
	 * @see SWT#RIGHT_TO_LEFT
	 * @see SWT#LEFT_TO_RIGHT
	 * @see SWT#NONE
	 */
	protected int getOrientation(final IEditorPart editor) {
		if (editor instanceof IWorkbenchPartOrientation) {
			return ((IWorkbenchPartOrientation) editor).getOrientation();
		}
		return getOrientation();
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

	/**
	 * Returns the reference of dummy editor if there is one.
	 * <p>
	 * Subclasses should not override this method
	 * </p>
	 * <p>
	 * Clients should not call this method, it merely supports a bugfix hack.
	 * </p>
	 * 
	 * @nooverride
	 * @return the reference of dummy editor, or <code>null</code> if none
	 */
	public static IEditorReference getDummyEditorReference() {
		return dummyEditorReference;
	}
	
	/**
	 * Returns the active nested editor if there is one.
	 * <p>
	 * Subclasses should not override this method
	 * </p>
	 * 
	 * @nooverride
	 * @return the active nested editor, or <code>null</code> if none
	 */
	protected IEditorPart getActiveEditor() {
		return plotDataEditorPart;
	}

	/**
	 * Returns the active nested editor if there is one.
	 * <p>
	 * Subclasses should not override this method
	 * </p>
	 * 
	 * @nooverride
	 * @return the active nested editor, or <code>null</code> if none
	 */
	protected IReusableEditor getActiveReusableEditor() {
		return (IReusableEditor)plotDataEditorPart;
	}

	/**
	 * Returns the active nested editor if there is one.
	 * <p>
	 * Subclasses should not override this method
	 * </p>
	 * 
	 * @nooverride
	 * @return the active nested editor, or <code>null</code> if none
	 */
	protected IDViewerImageControllable getActiveImageControllableEditor() {
		return (IDViewerImageControllable)plotDataEditorPart;
	}

	/**
	 * Returns the active nested editor if there is one.
	 * <p>
	 * Subclasses should not override this method
	 * </p>
	 * 
	 * @nooverride
	 * @return the active nested editor, or <code>null</code> if none
	 */
	protected DViewerImageEditorPart getActiveDViewerSubEditor() {
		return (DViewerImageEditorPart)plotDataEditorPart;
	}

	/**
	 * Fires a property changed event.
	 * 
	 * @param propertyId
	 *            the id of the property that changed
	 */
	@Override
	protected void firePropertyChange(final int propertyId) {
		for( final Object listener : getListeners()) {
			try {
				((IPropertyListener)listener).propertyChanged(this, propertyId);
			} catch (final RuntimeException e) {
				ExceptionUtils.logError(logger, null, e, this);
			}
		}
	}

	/**
	 * Handles a property change notification from a nested editor. The default
	 * implementation simply forwards the change to listeners on this
	 * editor by calling <code>firePropertyChange</code> with the same
	 * property id. For example, if the dirty state of a nested editor changes
	 * (property id <code>IEditorPart.PROP_DIRTY</code>), this method handles
	 * it by firing a property change event for
	 * <code>IEditorPart.PROP_DIRTY</code> to property listeners on this
	 * editor.
	 * <p>
	 * Subclasses may extend or reimplement this method.
	 * </p>
	 * 
	 * @param propertyId
	 *            the id of the property that changed
	 */
	protected void handlePropertyChange(final int propertyId) {
		UIListenerLogging.logPartReferencePropertyChange(this.getSite().getPage().getActivePartReference(), propertyId);
		firePropertyChange(propertyId);
	}

	@Override
	public void dispose() {
		getActiveEditor().dispose();
		listenerManager.dispose();
		super.dispose();
	}

	@Override
	public void createPartControl(final Composite parent) {
		container = new Composite(parent, SWT.NONE);
//		getContainer().setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN));
		getContainer().setLayout(new GridLayout(1, false));
		if (getContainer().getLayout() instanceof GridLayout) //For sure
			GridUtils.removeMargins(getContainer());

		try {
			plotDataEditorPart = new DViewerImageEditorPart(PlotType.IMAGE, listenerManager);
		} catch( final RuntimeException e ) {
			MessageDialog.open(MessageDialog.ERROR, getSite().getWorkbenchWindow().getShell(),
				"Editor Initializing Error", e.getLocalizedMessage(), MessageDialog.NONE);
			getSite().getPage().closeEditor(DViewerImageArrayEditorPart.this, false);
		}
		Assert.isLegal(plotDataEditorPart instanceof IReusableEditor);

		//Creating similar Composite to container
		final Composite dViewerControllerParent = new Composite(getContainer(), SWT.NONE);
		dViewerControllerParent.setLayout(new GridLayout(1, false));
		if (dViewerControllerParent.getLayout() instanceof GridLayout) //For sure
			GridUtils.removeMargins(dViewerControllerParent);
//		dViewerControllerParent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		final Composite containerParent = new Composite(getContainer(), getOrientation(getActiveEditor()));
//		containerParent.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_BLUE));
		containerParent.setLayout(new FillLayout());
		containerParent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true)); //grabExcessVerticalSpace must be true for showing plotting

		try {
			getActiveEditor().init(getEditorSite(), EMPTY_DATASET_INPUT);
		} catch (PartInitException e) {
			ExceptionUtils.logError(logger, new StringBuilder("Cannot initiate ").append(getClass().getName()).toString(), e, this);
			return;
		}
		getActiveDViewerSubEditor().setInput(getEditorInput(), false);
		getActiveEditor().createPartControl(containerParent);
		getActiveEditor().addPropertyListener(new IPropertyListener() {
			public void propertyChanged(Object source, int propertyId) {
				handlePropertyChange(propertyId);
			}
		});

		final DViewerController dViewerController = new DViewerController(this);
		dViewerController.createImageEditorGUI(dViewerControllerParent);

		editorInputChanged();



		//TODO Developing HKL loader here, but obviously it will be initiated from somewhere else
//		try {
//			InputStream is = ZipUtils.getStreamForFile("/home/naray/bigspace/STAC.test/xds_t1w1_run1_1/INTEGRATE.HKL.gz");
//			byte b[] = new byte[128];
//			is.read(b);
//			is.close();
//		} catch (final Exception e1) { //From constructing, or IOException
//			e1.printStackTrace();
//		}
	}

	@Override
	public boolean isSaveAsAllowed() {
		return getActiveEditor().isSaveAsAllowed();
	}

	@Override
	public void doSaveAs() {
		getActiveEditor().doSaveAs();
	}

	@Override
	public void doSave(final IProgressMonitor monitor) {
		getActiveEditor().doSave(monitor);
	}

	@Override
	public boolean isDirty() {
		return getActiveEditor().isDirty();
	}

	@Override
	public void setFocus() {
		getActiveEditor().setFocus();
	}

	@Override
	public Object getAdapter(@SuppressWarnings("rawtypes") final Class clazz) {
		return getActiveEditor().getAdapter(clazz);
	}

	@Override
	public boolean getAutoSelectLatestNewImage() {
		return autoSelectLatestNewImage;
	}

	@Override
	public void setAutoSelectLatestNewImage(final ISomethingChangeListener sender, final boolean autoSelectLatestNewImage) {
		synchronized (imageArrayLock) {
			if( getAutoSelectLatestNewImage() == autoSelectLatestNewImage )
				return;
			if( autoSelectLatestNewImage ) {
				autoSelectLatestNewImageJob.reschedule();
//				imageFilesAutoLatestThread.start();
			} else {
				autoSelectLatestNewImageJob.cancel();
//				imageFilesAutoLatestThread.interrupt();
			}
			this.autoSelectLatestNewImage = autoSelectLatestNewImage;
			listenerManager.fireSomethingChanged(SomethingChangeEvent.AUTO_SELECT_LATEST_NEW_IMAGE);
		}
	}

	@Override
	public boolean isRemoted() {
		return remotedImageEditor;
	}

	@Override
	public boolean isDisplayingImageByRemoteRequest() {
		return autoDisplayRemotedImage;
	}

	@Override
	public void toggleAutoDisplayRemotedImage() throws IllegalStateException {
		toggleDisplayImageByRemoteRequest();
		listenerManager.fireSomethingChanged(SomethingChangeEvent.AUTO_DISPLAY_REMOTED_IMAGE);
	}

	@Override
	public void displayRemotedImageDedicated() {
//		filePath = fileLoader.getCollectionDelegate().getAbsolutePath(); //This is nicer from vcf aspect, because vcf://regexp, but we do not know which file to display first
//		filePath = fileLoader.getFile().getAbsolutePath(); //This is bad, because vcf://singlefile, which causes exception when looking for index
		//If we would pass vcf, we should pass it with FileEditorInput instead of FilePathEditorInput
		final FilePathEditorInput fPEI = new FilePathEditorInput(
			fileLoader.getFile().getAbsolutePathWithoutProtocol(), null, fileLoader.getFile().getName()); //TODO what name to pass
		try {
			CommonExtension.openEditor(fPEI, ID, false, false);
		} catch (final PartInitException e) {
			ExceptionUtils.logError(logger, "Can not open editor", e, this);
		}
	}

	private long getHashCode(final AbstractDataset dataset) {
		return dataset.hashCode();
	}

	protected void closeActiveEditorIfFirstLoadingUnsuccessful() {
		CommonThreading.execUISynced(new Runnable() {
			@Override
			public void run() {
				if( getActiveReusableEditor().getEditorInput().equals(EMPTY_DATASET_INPUT) )
					getSite().getPage().closeEditor(getActiveReusableEditor(), false);
			}
		});
	}

	@Override
	public void fileLoadingDone(final Object source, final boolean newFile,
			final IProgressMonitor monitor) {
		if( source instanceof FileLoader && fileLoader == source) {
			final FileLoader fileLoader = (FileLoader)source; //Equal to fileLoader
			final AbstractDataset resultSet = fileLoader.getMergedDataset();
//			long hashCode = getHashCode(resultSet); //TODO Calculate a hashcode of dataset and compare to previous to see if it changes!!!
//			System.out.println("Dataset HashCode=" + hashCode);
//			IMetaData localMetaData = resultSet.getMetadata();
/* TODO Could implement something like this aborting when switching to NOT_PLAYING while loading in remote display mode,
   but have to be careful because for example at this point the file is loaded in fileloader, how to undo it?
   At the moment when opening image from remote display window, it loads the file found in fileloader, because
   the input might have been changed thus it can not be used.
   The solution could be a totally separated loader, image creator, and when everything is ready, could check if paused
   the playing, and if yes, then drop the separately created stuff, else display it as soon as possible.
*/
//			if( isRemoted() && ImageEditorRemotedDisplayState.isNotPlaying(imageEditorRemotedDisplayState))
//				return;
			CommonThreading.execUISynced(new Runnable() {
				@Override
				public void run() {
					getActiveReusableEditor().setInput(new MemoryDatasetEditorInput(resultSet, fileLoader.getFirstLoadedLegacyPath(), newFile, monitor));
				}
			});
		}
		
	}

	@Override
	public void fileLoadingCancelled(final Object source, final boolean newFile,
			final IProgressMonitor monitor) {
		//Do not show dialog, because dragging slider also cancels
		closeActiveEditorIfFirstLoadingUnsuccessful();
	}

	@Override
	public void fileLoadingFailed(Object source, boolean newFile,
			IProgressMonitor monitor) { //TODO pass detailed error message and display it
		final String ERROR_MESSAGE = "An error occured while loading the requested file(s)";
		ExceptionUtils.logError(logger, ERROR_MESSAGE);
		if( isRemoted() )
			return; //Not showing (blocking) dialog when images arrive from remote
		CommonThreading.execUISynced(new Runnable() {
			@Override
			public void run() {
				MessageDialog.open(MessageDialog.ERROR, getSite().getWorkbenchWindow().getShell(),
					"File Loading Error", ERROR_MESSAGE, MessageDialog.NONE);
				closeActiveEditorIfFirstLoadingUnsuccessful();
			}
		});
	}

	@Override
	public int getImageArrayMin() {
		return 0;
	}

	@Override
	public int getImageArraySup() {
		return fileLoader.getAllLength();
	}

	@Override
	public int getImageArrayBatchIndex() {
		return batchIndex;
	}

	@Override
	public int getImageArrayBatchSize() {
		return batchSize;
	}

	@Override
	public boolean isBatchSizeValid(final int value) {
		return value>=BATCH_SIZE_MIN && value<=BATCH_SIZE_MAX;
	}

	protected int getImageArrayBatchSizeEffective() {
		return Math.min(getImageArraySup() - getImageArrayMin(), batchSize);
	}

	protected void onImageArraySizeUpdated() {
		listenerManager.fireSomethingChanged(SomethingChangeEvent.IMAGE_ARRAY_SOMETHING);
	}

	protected static String getPath( final IEditorInput editorInput ) {
		final String result;
		final IFile iF = (IFile)editorInput.getAdapter(IFile.class);
		if( iF != null )
			result = iF.getLocation().toOSString();
		else {
			result = EclipseUtils.getFilePath(editorInput);
			if( result == null ) {
				ExceptionUtils.logError(logger, new StringBuilder("Cannot determine the input of this editor: ").append(editorInput.getName()).toString());
				return null;
			}
		}
		return result;
	}

	protected void toggleDisplayImageByRemoteRequest() {
		autoDisplayRemotedImage = !autoDisplayRemotedImage;
		remotedDisplayState = DViewerRemotedDisplayState.togglePlaying(remotedDisplayState);
		if( remotedDisplayState.equals(DViewerRemotedDisplayState.PLAYING_AND_REMOTE_UPDATED)) {
			editorInputChanged();
		} else {
			fileLoader.cancelLoading();
		}
	}

	protected void editorInputChanged() {
		if( isRemoted() && !isDisplayingImageByRemoteRequest() ) {
			//not displaying in remoted editor because not wanted, but setting updated
			if( !DViewerRemotedDisplayState.isRemoteUpdated(remotedDisplayState))
				remotedDisplayState = DViewerRemotedDisplayState.setRemoteUpdated(remotedDisplayState);
			return;
		} else {
			synchronized (imageArrayLock) {
				if( getEditorInput() instanceof MemoryDatasetEditorInput ) {
					final MemoryDatasetEditorInput input = (MemoryDatasetEditorInput)getEditorInput();
					getActiveReusableEditor().setInput(input);
				} else {
//				updateSlider( getPath( getEditorInput() ) );
					//TODO import raw loader from previous version?
					final String filePath = getPath( getEditorInput() );
					fileLoader.setFilePath(filePath);
					int pos = fileLoader.getFile().getIndexOfFile(filePath);
					setBatchIndex(null, pos, true);
				}
			}
		}
	}

	protected void selectLastBatch() {
//		updateSlider( /*currentAllImageFiles*/ );
		setBatchIndex(null, Math.max(getImageArrayMin(), getImageArrayMin() + getImageArraySup() - batchSize));
	}

	@Override
	public void setBatchIndex(final ISomethingChangeListener sender, int batchIndex) {
		setBatchIndex(sender, batchIndex, false);
	}

	protected void setBatchIndex(final ISomethingChangeListener sender, final int batchIndex, final boolean newInput) {
//		updateSlider( /*currentAllImageFiles*/ );
		synchronized (imageArrayLock) {
			if( this.batchIndex == batchIndex && !newInput )
				return;
			final int batchIndexMin = getImageArrayMin();
			final int batchIndexMax = Math.max(getImageArrayMin(), batchIndexMin + getImageArraySup() - batchSize);
			try {
				Assert.isLegal(batchIndex>=batchIndexMin && batchIndex<=batchIndexMax, "The batchIndex (" + batchIndex + ") is illegal, " + batchIndexMin + " <= batchIndex <= " + batchIndexMax + " must be true.");
			} catch(final IllegalArgumentException e) {
				if( sender != null )
					listenerManager.sendSomethingChanged(SomethingChangeEvent.IMAGE_ARRAY_SOMETHING, sender);
				return;
			}
//			sliderMoved( batchIndex );
//			loadFilesForPlotting(batchIndex, batchSize);
			if( this.batchIndex != batchIndex ) {
				this.batchIndex = batchIndex;
				listenerManager.fireSomethingChanged(SomethingChangeEvent.IMAGE_ARRAY_SOMETHING);
				if( getAutoSelectLatestNewImage() && batchIndex < getImageArraySup() - 1 )
					setAutoSelectLatestNewImage(null, false);
//					imageFilesAutoLatestButton.setSelection( false );
			}
			fileLoader.loadFiles(batchIndex, getImageArrayBatchSizeEffective(), newInput); //This is a job, we do not know its result, thus doing it lastly
		}
	}

	@Override
	public void setBatchSize(final ISomethingChangeListener sender, final int batchSize) {
		synchronized (imageArrayLock) {
			if( this.batchSize == batchSize )
				return;
			try {
				Assert.isLegal(isBatchSizeValid(batchSize), new StringBuilder("The batchSize (")
					.append(batchSize).append(") is illegal, ").append(BATCH_SIZE_MIN)
					.append(" <= batchSize <= ").append(BATCH_SIZE_MAX).append(" must be true.").toString());
			} catch(final IllegalArgumentException e) {
				if( sender != null )
					listenerManager.sendSomethingChanged(SomethingChangeEvent.IMAGE_ARRAY_SOMETHING, sender);
				return;
			}
			this.batchSize = batchSize;
			batchIndex = Math.min(getImageArraySup() - getImageArrayBatchSizeEffective(), batchIndex);
			listenerManager.fireSomethingChanged(SomethingChangeEvent.IMAGE_ARRAY_SOMETHING);
			fileLoader.loadFiles(batchIndex, getImageArrayBatchSizeEffective(), false); //This is a job, we do not know its result, thus doing it lastly
		}
	}

	@Override
	public boolean getPha() {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		if( editor != null )
			return editor.getPha();
		return false;
	}

	@Override
	public void setPha(ISomethingChangeListener sender, boolean phaState) {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		if( editor != null )
			editor.setPha(sender, phaState);
	}

	@Override
	public int getPhaRadiusMin() {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		if( editor != null )
			return editor.getPhaRadiusMin();
		return 0;
	}

	@Override
	public int getPhaRadiusSup() {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		if( editor != null )
			return editor.getPhaRadiusSup();
		return 0;
	}

	@Override
	public boolean isPhaRadiusValid(int value) {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		if( editor != null )
			return editor.isPhaRadiusValid(value);
		return false;
	}

	@Override
	public int getPhaRadius() {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		if( editor != null )
			return editor.getPhaRadius();
		return 0;
	}

	@Override
	public void setPhaRadius(final ISomethingChangeListener sender, final int phaRadius) {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		if( editor != null )
			editor.setPhaRadius(sender, phaRadius);
	}

	@Override
	public DownsampleType getDownsampleType() {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		if( editor != null )
			return editor.getDownsampleType();
		return null;
	}

	@Override
	public void setDownsampleType(final ISomethingChangeListener sender,
			final DownsampleType downsampleType) {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		if( editor != null )
			editor.setDownsampleType(sender, downsampleType);
	}

	@Override
	public void addSomethingListener(final ISomethingChangeListener listener) {
		listenerManager.addSomethingListener(listener);
	}

	@Override
	public void removeSomethingListener(final ISomethingChangeListener listener) {
		listenerManager.removeSomethingListener(listener);
	}

}
