package org.embl.cca.dviewer.ui.editors;

import java.io.FileNotFoundException;
import java.util.EnumSet;

import org.dawb.common.ui.util.EclipseUtils;
import org.dawb.common.ui.util.GridUtils;
import org.dawnsci.common.widgets.editor.ITitledEditor;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.plotting.api.PlotType;
import org.eclipse.dawnsci.plotting.api.trace.IImageTrace.DownsampleType;
import org.eclipse.january.dataset.Dataset;
import org.eclipse.january.dataset.DatasetFactory;
import org.eclipse.january.dataset.IntegerDataset;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IReusableEditor;
import org.eclipse.ui.IShowEditorInput;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.IWorkbenchPartOrientation;
import org.eclipse.ui.part.WorkbenchPart;
import org.embl.cca.dviewer.ui.editors.preference.DViewerEditorConstants;
import org.embl.cca.dviewer.ui.editors.utils.Point2DD;
import org.embl.cca.dviewer.ui.views.DViewerControlsPage;
import org.embl.cca.dviewer.ui.views.DViewerImageView;
import org.embl.cca.utils.datahandling.EFile;
import org.embl.cca.utils.datahandling.FilePathEditorInput;
import org.embl.cca.utils.datahandling.MemoryDatasetEditorInput;
import org.embl.cca.utils.datahandling.file.FileLoader;
import org.embl.cca.utils.datahandling.file.IFileLoaderListener;
import org.embl.cca.utils.datahandling.file.VirtualCollectionFile;
import org.embl.cca.utils.datahandling.text.StringUtils;
import org.embl.cca.utils.errorhandling.ExceptionUtils;
import org.embl.cca.utils.extension.CommonExtension;
import org.embl.cca.utils.general.ISomethingChangeListener;
import org.embl.cca.utils.general.SomethingChangeEvent;
import org.embl.cca.utils.threading.CommonThreading;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DViewerImageArrayEditorAndViewPart extends WorkbenchPart
	implements ITitledEditor, IReusableEditor, IShowEditorInput,//ISaveable
	IViewPart, IDViewerControllable, IDViewerControlsPageAdaptable {

	public static final String ID = "org.embl.cca.dviewer.ui.editors.DViewerImageArrayEditorAndViewPart";

	private static final Logger logger = LoggerFactory.getLogger(DViewerImageArrayEditorAndViewPart.class);

	//Support for IEditorPart and IViewPart, beginning here
	protected final IWorkbenchPart classRole; //(IEditorPartHost or IViewPartHost) and IDViewerControllable

	//These events are managed by host: PROP_TITLE, PROP_CONTENT_DESCRIPTION, PROP_PART_NAME, PROP_INPUT
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

	public final static Dataset EMPTY_DATASET = DatasetFactory.zeros(IntegerDataset.class, 0, 0);
	public  final static MemoryDatasetEditorInput EMPTY_DATASET_INPUT = new MemoryDatasetEditorInput(EMPTY_DATASET);

	protected boolean remotedImageEditor = false;
	protected boolean autoDisplayRemotedImage = true;
	protected DViewerRemotedDisplayState remotedDisplayState = DViewerRemotedDisplayState.PLAYING_AND_REMOTE_UPDATED;
	protected int showEachNthImage = 1;
	/**
	 * This counter is decreased each time a remote image is received,
	 * and when reaches 0, remote image is displayed, and this counter
	 * is reset to showEachNthImage.
	 */
	protected int showImageCounter = 1;
	
	protected IWorkbenchPart plotDataPart;
	protected Label statusLabel;

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
	 * The container widget containing everything.
	 */
	protected Composite mainContainer;

	protected final DViewerListenerManager listenerManager;

	protected final ISomethingChangeListener somethingChangeListener = new ISomethingChangeListener() {
		/**
		 * {@inheritDoc}
		 * <p>
		 * GUI thread is assumed.
		 */
		@Override
		public void somethingChange(final SomethingChangeEvent event) {
			if( event.getSomethingName().equals(SomethingChangeEvent.MOUSE_POSITION) )
				internalUpdateStatus();
		}
	};

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
				final IProgressMonitor thisMonitor = monitor == null ? new NullProgressMonitor() : monitor;
				thisMonitor.beginTask("Selecting latest image batch", 1);
				try {
					onImageArraySizeUpdated();
					selectLastBatch();
					thisMonitor.worked(1);
					return Status.OK_STATUS;
				} finally {
					thisMonitor.done();
				}
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
			final IProgressMonitor thisMonitor = monitor == null ? new NullProgressMonitor() : monitor;
			final long time = System.currentTimeMillis();
//			System.out.println("AutoSelectLatestNewJob started at " + System.currentTimeMillis());
			thisMonitor.beginTask("Auto selecting latest new image", 1);
			try {
				final int sleepTime = checkDirectory() ? 10 : 100;
				if( thisMonitor.isCanceled() )
					return Status.CANCEL_STATUS;
				thisMonitor.worked(1);
				schedule(Math.max(sleepTime - (System.currentTimeMillis() - time), 0));
				return Status.OK_STATUS;
			} finally {
				thisMonitor.done();
			}
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

	final protected IFileLoaderListener fileLoaderListener = new IFileLoaderListener() {
		@Override
		public void fileLoadingDone(final Object source, final boolean newFile,
				final IProgressMonitor monitor) {
			final IProgressMonitor thisMonitor = monitor == null ? new NullProgressMonitor() : monitor;
			if( source instanceof FileLoader && fileLoader == source) {
				final FileLoader fileLoader = (FileLoader)source; //Equal to fileLoader
				final Dataset resultSet = fileLoader.getMergedDataset();
//				long hashCode = getHashCode(resultSet); //TODO Calculate a hashcode of dataset and compare to previous to see if it changes!!!
//				System.out.println("Dataset HashCode=" + hashCode);
//				IMetadata localMetadata = resultSet.getMetadata();
	/* TODO Could implement something like this aborting when switching to NOT_PLAYING while loading in remote display mode,
	   but have to be careful because for example at this point the file is loaded in fileloader, how to undo it?
	   At the moment when opening image from remote display window, it loads the file found in fileloader, because
	   the input might have been changed thus it can not be used.
	   The solution could be a totally separated loader, image creator, and when everything is ready, could check if paused
	   the playing, and if yes, then drop the separately created stuff, else display it as soon as possible.
	*/
//				if( isRemoted() && ImageEditorRemotedDisplayState.isNotPlaying(imageEditorRemotedDisplayState))
//					return;
				CommonThreading.execUISynced(new Runnable() {
					@Override
					public void run() {
						if( IEditorPartHost.class.isAssignableFrom(classRole.getClass()) )
							getActiveDViewerSubEditor().setInputWithNotify(new MemoryDatasetEditorInput(resultSet, fileLoader.getFirstLoadedLegacyPath(), newFile, thisMonitor));
						else if( IViewPartHost.class.isAssignableFrom(classRole.getClass()) )
							getActiveDViewerSubView().setInputWithNotify(new MemoryDatasetEditorInput(resultSet, fileLoader.getFirstLoadedLegacyPath(), newFile, thisMonitor));
					}
				});
			}
			
		}

		@Override
		public void fileLoadingCancelled(final Object source, final boolean newFile) {
			//Do not show dialog, because dragging slider also cancels
			closeActiveEditorIfFirstLoadingUnsuccessful();
		}

		@Override
		public void fileLoadingFailed(Object source, boolean newFile) {
			final String ERROR_MESSAGE = "An error occured while loading the requested file(s): " + ((FileLoader)source).getCollectionAbsoluteName();
			ExceptionUtils.logError(logger, ERROR_MESSAGE);
			if( isRemoted() )
				return; //Not showing (blocking) dialog when images arrive from remote
			CommonThreading.execUIAsynced(new Runnable() {
				@Override
				public void run() {
					MessageDialog.openError(getSite().getWorkbenchWindow().getShell(),
							"File Loading Error", ERROR_MESSAGE);
					closeActiveEditorIfFirstLoadingUnsuccessful();
				}
			});
		}
	};

	public DViewerImageArrayEditorAndViewPart(final IWorkbenchPart classRole) {
		if( !(IEditorPartHost.class.isAssignableFrom(classRole.getClass())
				|| IViewPartHost.class.isAssignableFrom(classRole.getClass()))
				|| !IDViewerControllable.class.isAssignableFrom(classRole.getClass()) )
			throw new IllegalArgumentException("classRole must be (IEditorPartHost or IViewPartHost) and IDViewerControllable");
		this.classRole = classRole;
		fileLoader = new FileLoader();
		fileLoader.addFileLoaderListener(fileLoaderListener);
		autoSelectLatestNewImageJob = new AutoSelectLatestNewImageJob();
		listenerManager = new DViewerListenerManager();
		classRole.addPropertyListener(hostPropertyListener);
		classRole.addPropertyListener(inputListener);
	}

	protected void setRemotedByInput() {
		if( getEditorInput() instanceof FilePathEditorInput ) {
			final FilePathEditorInput fPEI = (FilePathEditorInput)getEditorInput();
			if( fPEI.equalityIdEquals(DViewerImageArrayEditorPart.REMOTED_IMAGE)) {
				remotedImageEditor = true;
			}
		}
	}
	
	@Override //from IEditorPart
	public void init(final IEditorSite site, final IEditorInput input) throws PartInitException{
		setSite(site);
		setInput(input); //Also calls setRemotedByInput, because no previous input
	}

	@Override //from IViewPart
	public void init(final IViewSite site) throws PartInitException {
		setSite(site);
	}

	@Override //from IViewPart
	public void init(final IViewSite site, final IMemento memento) throws PartInitException {
		init(site);
	}

	@Override //from IViewPart
	public void saveState(final IMemento memento) {
	}

	@Override //from WorkbenchPart
	public void dispose() {
		classRole.removePropertyListener(inputListener);
		classRole.removePropertyListener(hostPropertyListener);
		getActivePart().dispose();
		listenerManager.dispose();
		super.dispose();
	}

	@Override //from IShowEditorInput
	public void showEditorInput(final IEditorInput editorInput) {
		setInputWithNotify(editorInput);
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
//	@Override
//	public void setInput(final IEditorInput input) {
//		logger.debug("setInput(IEditorInput) called");
//		setInput(input, true);
//	}

//	protected void setInput(final IEditorInput input, final boolean createData) {
//		super.setInput(input);
//		setPartName(getEditorInput().getName());
//		if (createData)
//			editorInputChanged();
//		logger.debug("setInput(IEditorInput, boolean=" + createData + ") called");
//	}
//
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
		return getActiveEditor().isDirty();
	}

	@Override //from ISaveablePart
	public boolean isSaveAsAllowed() {
		return getActiveEditor().isSaveAsAllowed();
	}

	@Override //from ISaveablePart
	public void doSave(final IProgressMonitor monitor) {
		getActiveEditor().doSave(monitor);
	}

	@Override //from ISaveablePart
	public void doSaveAs() {
		getActiveEditor().doSaveAs();
	}

	@Override //from WorkbenchPart
	public void setFocus() {
		getActivePart().setFocus();
	}

	@Override //from WorkbenchPart
	public <T> T getAdapter(final Class<T> clazz) {
		if (clazz == DViewerControlsPage.class) {
			return clazz.cast(DViewerControlsPage.getPageFor((IDViewerControllable)classRole));
		} else if (clazz == IDViewerControlsPageAdaptable.class) {
			return clazz.cast(new IDViewerControlsPageAdaptable() {});
		}
		return getActivePart().getAdapter(clazz);
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
					if( IEditorPartHost.class.isAssignableFrom(classRole.getClass()) )
						getActiveReusableEditor().setInput(input);
					else if( IViewPartHost.class.isAssignableFrom(classRole.getClass()) )
						getActiveDViewerSubView().setInput(input);
				} else {
					System.out.println("editorInputChanged, showImageCounter=" + showImageCounter + ", showEachNthImage=" + showEachNthImage);
					if( isRemoted() ) { //This is the "Show each Nth received image" feature
						if( --showImageCounter > 0 )
							return;
						showImageCounter = showEachNthImage;
					}
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
		return mainContainer;
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
	 * Returns the active nested part if there is one.
	 * <p>
	 * Subclasses should not override this method
	 * </p>
	 * 
	 * @nooverride
	 * @return the active nested part, or <code>null</code> if none
	 */
	protected IWorkbenchPart getActivePart() {
		return plotDataPart;
	}

	/**
	 * Returns the active nested view if there is one.
	 * <p>
	 * Subclasses should not override this method
	 * </p>
	 * 
	 * @nooverride
	 * @return the active nested view, or <code>null</code> if none
	 */
	protected IViewPart getActiveView() {
		return (IViewPart)plotDataPart;
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
		return (IEditorPart)plotDataPart;
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
		return (IReusableEditor)plotDataPart;
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
		return (IDViewerImageControllable)plotDataPart;
	}

	/**
	 * Returns the active nested view if there is one.
	 * <p>
	 * Subclasses should not override this method
	 * </p>
	 * 
	 * @nooverride
	 * @return the active nested view, or <code>null</code> if none
	 */
	protected DViewerImageViewPart getActiveDViewerSubView() {
		return (DViewerImageViewPart)plotDataPart;
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
		return (DViewerImageEditorPart)plotDataPart;
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
	@SuppressWarnings("restriction")
	protected void handlePropertyChange(final int propertyId) {
		org.eclipse.ui.internal.misc.UIListenerLogging.logPartReferencePropertyChange(getSite().getPage().getActivePartReference(), propertyId);
		firePropertyChange(propertyId);
	}

	@Override //from WorkbenchPart
	public void createPartControl(final Composite parent) {
		try {
			if( IEditorPartHost.class.isAssignableFrom(classRole.getClass()) )
				plotDataPart = new DViewerImageEditorPart(PlotType.IMAGE, listenerManager);
			else if( IViewPartHost.class.isAssignableFrom(classRole.getClass()) )
				plotDataPart = new DViewerImageViewPart(PlotType.IMAGE, listenerManager);
		} catch( final RuntimeException e ) {
			MessageDialog.openError(getSite().getWorkbenchWindow().getShell(),
				"dViewer Initializing Error", "Can not create WorkbenchPart\n" + e.getMessage());
			if( IEditorPartHost.class.isAssignableFrom(classRole.getClass()) )
				getSite().getPage().closeEditor((IEditorPartHost)classRole, false);
			return;
		}

		mainContainer = new Composite(parent, SWT.NONE);
		final GridLayout mainGridLayout = new GridLayout(1, false);
		mainContainer.setLayout(mainGridLayout);
//		container.setBackground(org.eclipse.swt.widgets.Display.getCurrent().getSystemColor(org.eclipse.swt.SWT.COLOR_GREEN));
		GridUtils.removeMargins(mainContainer); //valid for GridLayout only

//		final DViewerController dViewerController = new DViewerController(this, mainContainer);
//		dViewerController.createImageEditorGUI(mainContainer); //create controls basically (without initializing)

		final Composite toolbarAndStatusContainer = new Composite(mainContainer, SWT.NONE);
//		toolbarAndStatusContainer.setBackground(org.eclipse.swt.widgets.Display.getCurrent().getSystemColor(org.eclipse.swt.SWT.COLOR_YELLOW));
		toolbarAndStatusContainer.setLayout(DViewerController.createRowLayout());

		final Composite toolbarContainer = new Composite(toolbarAndStatusContainer, SWT.NONE);
		final GridLayout toolbarGridLayout = new GridLayout(1, false);
		toolbarContainer.setLayout(toolbarGridLayout);
//		toolbarContainer.setBackground(org.eclipse.swt.widgets.Display.getCurrent().getSystemColor(org.eclipse.swt.SWT.COLOR_BLUE));
		GridUtils.removeMargins(toolbarContainer); //valid for GridLayout only

		if( IEditorPartHost.class.isAssignableFrom(classRole.getClass()) )
			getActiveDViewerSubEditor().setToolbarParent(toolbarContainer);
		else if( IViewPartHost.class.isAssignableFrom(classRole.getClass()) )
			getActiveDViewerSubView().setToolbarParent(toolbarContainer);
		statusLabel = new Label(toolbarAndStatusContainer, SWT.WRAP); //create statusbar
		//Setting monospace font to avoid jumping numbers left-right
		statusLabel.setFont(JFaceResources.getFont(JFaceResources.TEXT_FONT));

		try {
			if( IEditorPartHost.class.isAssignableFrom(classRole.getClass()) )
				getActiveEditor().init(getEditorSite(), EMPTY_DATASET_INPUT);
			else if( IViewPartHost.class.isAssignableFrom(classRole.getClass()) ) {
				getActiveView().init(getViewSite());
				getActiveDViewerSubView().setInput(EMPTY_DATASET_INPUT);
			}
		} catch (final PartInitException e) {
			ExceptionUtils.logError(logger, new StringBuilder("Cannot initiate ").append(getClass().getName()).toString(), e, this);
			return;
		}

		getActivePart().createPartControl(mainContainer); //create controls fully
		getActivePart().addPropertyListener(new IPropertyListener() {
			public void propertyChanged(Object source, int propertyId) {
				handlePropertyChange(propertyId);
			}
		});
		addSomethingListener(somethingChangeListener); //activePart must already exist for this listener

		final Composite subComposite; 
		if( IEditorPartHost.class.isAssignableFrom(classRole.getClass()) )
			subComposite = getActiveDViewerSubEditor().getContainer();
		else if( IViewPartHost.class.isAssignableFrom(classRole.getClass()) )
			subComposite = getActiveDViewerSubView().getContainer();
		else
			subComposite = null;
		for( final Control c : mainContainer.getChildren() ) {
			c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, c.equals(subComposite))); //grabExcessVerticalSpace must be true for showing plotting
		}
//		dViewerController.initializeImageSelectorUI(); //initialize controls

		editorInputChanged();
		parent.pack(true);
	}

	protected void internalUpdateStatus() {
		statusLabel.setText(getStatusText());
		CommonExtension.layoutIn(statusLabel, mainContainer);
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
			CommonExtension.openEditor(fPEI, DViewerImageArrayEditorPart.ID, false, true);
		} catch (final PartInitException e) {
			ExceptionUtils.logError(logger, "Can not open editor", e, this);
		}
	}

	@Override //from IDViewerControllable
	public int getShowEachNthImageMin() {
		return DViewerEditorConstants.SHOW_EACH_NTH_IMAGE_MIN;
	}

	@Override //from IDViewerControllable
	public int getShowEachNthImageSup() {
		return DViewerEditorConstants.SHOW_EACH_NTH_IMAGE_MAX + 1;
	}

	@Override //from IDViewerControllable
	public boolean isShowEachNthImageValid(final int showEachNthImage) {
		return showEachNthImage>=getShowEachNthImageMin() && showEachNthImage<=getShowEachNthImageSup() - 1;
	}

	@Override //from IDViewerControllable
	public int getShowEachNthImage() {
		return showEachNthImage;
	}

	@Override //from IDViewerControllable
	public void setShowEachNthImage(final ISomethingChangeListener sender,
			final int showEachNthImage) {
		if( this.showEachNthImage == showEachNthImage )
			return;
		final int showEachNthImageMin = getShowEachNthImageMin();
		final int showEachNthImageMax = getShowEachNthImageSup() - 1;
		try {
			Assert.isLegal(isShowEachNthImageValid(showEachNthImage), new StringBuilder("The showEachNthImage (")
			.append(showEachNthImage).append(") is illegal, ").append(showEachNthImageMin)
			.append(" <= showEachNthImage <= ").append(showEachNthImageMax).append(" must be true.").toString());
		} catch(final IllegalArgumentException e) {
			if( sender != null )
				listenerManager.sendSomethingChanged(SomethingChangeEvent.SHOW_EACH_NTH_IMAGE, sender);
			return;
		}
		showImageCounter = Math.max(1, showImageCounter + showEachNthImage - this.showEachNthImage);
		this.showEachNthImage = showEachNthImage;
		listenerManager.fireSomethingChanged(SomethingChangeEvent.SHOW_EACH_NTH_IMAGE);
	}

	protected void closeActiveEditorIfFirstLoadingUnsuccessful() {
		CommonThreading.execUISynced(new Runnable() {
			@Override
			public void run() {
				final IEditorInput editorInput;
				if( getActivePart() instanceof IEditorPartHost )
					editorInput = ((IEditorPartHost)getActivePart()).getEditorInput();
				else if( getActivePart() instanceof IViewPart )
					editorInput = ((IViewPartHost)getActivePart()).getEditorInput();
				else
					editorInput = null;
				if( editorInput == null || editorInput.equals(EMPTY_DATASET_INPUT) ) {
					if( IEditorPartHost.class.isAssignableFrom(classRole.getClass()) )
						getSite().getPage().closeEditor((IEditorPartHost)classRole, false);
					else if( IViewPartHost.class.isAssignableFrom(classRole.getClass()) ) {
						//Have to hardcode a bit, because the IViewPartHost is
						//in a DViewerImageView PageBook, thus have to use a
						//hard reference here.
						getSite().getPage().hideView(getSite().getPage().findView(DViewerImageView.ID));
//						CommonExtension.closePart(DViewerImageView.ID, true); //Does not work, no active window context
					}
				}
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

	protected int getImageArrayBatchSizeEffective() {
		return Math.min(getImageArraySup() - getImageArrayMin(), batchSize);
	}

	protected void onImageArraySizeUpdated() {
		listenerManager.fireSomethingChanged(SomethingChangeEvent.IMAGE_ARRAY_SOMETHING);
	}

	protected static String getPath( final IEditorInput editorInput ) {
		final String result;
		do {
			final VirtualCollectionFile vCF = (VirtualCollectionFile)editorInput.getAdapter(VirtualCollectionFile.class);
			if( vCF != null ) {
				result = vCF.getAbsolutePath();
				break;
			}
			final IFile iF = (IFile)editorInput.getAdapter(IFile.class);
			if( iF != null ) {
				result = iF.getLocation().toOSString();
				break;
			}
			result = EclipseUtils.getFilePath(editorInput);
			if( result != null )
				break;
			ExceptionUtils.logError(logger, new StringBuilder("Cannot determine the input of this editor: ").append(editorInput.getName()).toString());
		} while( false );
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

	protected void selectLastBatch() {
//		updateSlider( /*currentAllImageFiles*/ );
		setBatchIndex(null, Math.max(getImageArrayMin(), getImageArrayMin() + getImageArraySup() - batchSize));
	}

	@Override
	public boolean isBatchIndexValid(final int value) {
		return value>=getImageArrayMin() && value<=Math.max(getImageArrayMin(), getImageArrayMin() + getImageArraySup() - batchSize);
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
				Assert.isLegal(isBatchIndexValid(batchIndex), new StringBuilder("The batchIndex (")
					.append(batchIndex).append(") is illegal, ").append(batchIndexMin)
					.append(" <= batchIndex <= ").append(batchIndexMax).append(" must be true.").toString());
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
	public boolean isBatchSizeValid(final int value) {
		return value>=DViewerEditorConstants.BATCH_SIZE_MIN && value<=getImageArraySup() - getImageArrayMin();
	}

	@Override
	public void setBatchSize(final ISomethingChangeListener sender, final int batchSize) {
		synchronized (imageArrayLock) {
			if( this.batchSize == batchSize )
				return;
			try {
				Assert.isLegal(isBatchSizeValid(batchSize), new StringBuilder("The batchSize (")
					.append(batchSize).append(") is illegal, ").append(DViewerEditorConstants.BATCH_SIZE_MIN)
					.append(" <= batchSize <= ").append(getImageArraySup() - getImageArrayMin()).append(" must be true.").toString());
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

	@Override //from IDViewerImageControllable
	public boolean getPha() {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		if( editor != null )
			return editor.getPha();
		return false;
	}

	@Override //from IDViewerImageControllable
	public void setPha(ISomethingChangeListener sender, boolean phaState) {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		if( editor != null )
			editor.setPha(sender, phaState);
	}

	@Override //from IDViewerImageControllable
	public int getPhaRadiusMin() {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		if( editor != null )
			return editor.getPhaRadiusMin();
		return 0;
	}

	@Override //from IDViewerImageControllable
	public int getPhaRadiusSup() {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		if( editor != null )
			return editor.getPhaRadiusSup();
		return 0;
	}

	@Override //from IDViewerImageControllable
	public boolean isPhaRadiusValid(int value) {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		if( editor != null )
			return editor.isPhaRadiusValid(value);
		return false;
	}

	@Override //from IDViewerImageControllable
	public int getPhaRadius() {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		if( editor != null )
			return editor.getPhaRadius();
		return 0;
	}

	@Override //from IDViewerImageControllable
	public void setPhaRadius(final ISomethingChangeListener sender, final int phaRadius) {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		if( editor != null )
			editor.setPhaRadius(sender, phaRadius);
	}

	@Override //from IDViewerImageControllable
	public void setHKLFile(final EFile file) {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		if( editor != null )
			editor.setHKLFile(file);
	}

	@Override //from IDViewerImageControllable
	public int getHMin() {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		if( editor != null )
			return editor.getHMin();
		return 0;
	}

	@Override //from IDViewerImageControllable
	public int getHSup() {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		if( editor != null )
			return editor.getHSup();
		return 0;
	}

	@Override //from IDViewerImageControllable
	public boolean isHValid(final int value) {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		if( editor != null )
			return editor.isHValid(value);
		return false;
	}

	@Override //from IDViewerImageControllable
	public int getHRangeMin() {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		if( editor != null )
			return editor.getHRangeMin();
		return 0;
	}

	@Override //from IDViewerImageControllable
	public void setHRangeMin(final ISomethingChangeListener sender, final int hRangeMin) {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		if( editor != null )
			editor.setHRangeMin(sender, hRangeMin);
	}

	@Override //from IDViewerImageControllable
	public int getHRangeMax() {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		if( editor != null )
			return editor.getHRangeMax();
		return 0;
	}

	@Override //from IDViewerImageControllable
	public void setHRangeMax(final ISomethingChangeListener sender, final int hRangeMax) {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		if( editor != null )
			editor.setHRangeMax(sender, hRangeMax);
	}

	@Override //from IDViewerImageControllable
	public int getKMin() {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		if( editor != null )
			return editor.getKMin();
		return 0;
	}

	@Override //from IDViewerImageControllable
	public int getKSup() {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		if( editor != null )
			return editor.getKSup();
		return 0;
	}

	@Override //from IDViewerImageControllable
	public boolean isKValid(final int value) {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		if( editor != null )
			return editor.isKValid(value);
		return false;
	}

	@Override //from IDViewerImageControllable
	public int getKRangeMin() {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		if( editor != null )
			return editor.getKRangeMin();
		return 0;
	}

	@Override //from IDViewerImageControllable
	public void setKRangeMin(final ISomethingChangeListener sender, final int kRangeMin) {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		if( editor != null )
			editor.setKRangeMin(sender, kRangeMin);
	}

	@Override //from IDViewerImageControllable
	public int getKRangeMax() {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		if( editor != null )
			return editor.getKRangeMax();
		return 0;
	}

	@Override //from IDViewerImageControllable
	public void setKRangeMax(final ISomethingChangeListener sender, final int kRangeMax) {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		if( editor != null )
			editor.setKRangeMax(sender, kRangeMax);
	}

	@Override //from IDViewerImageControllable
	public int getLMin() {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		if( editor != null )
			return editor.getLMin();
		return 0;
	}

	@Override //from IDViewerImageControllable
	public int getLSup() {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		if( editor != null )
			return editor.getLSup();
		return 0;
	}

	@Override //from IDViewerImageControllable
	public boolean isLValid(final int value) {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		if( editor != null )
			return editor.isLValid(value);
		return false;
	}

	@Override //from IDViewerImageControllable
	public int getLRangeMin() {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		if( editor != null )
			return editor.getLRangeMin();
		return 0;
	}

	@Override //from IDViewerImageControllable
	public void setLRangeMin(final ISomethingChangeListener sender, final int lRangeMin) {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		if( editor != null )
			editor.setLRangeMin(sender, lRangeMin);
	}

	@Override //from IDViewerImageControllable
	public int getLRangeMax() {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		if( editor != null )
			return editor.getLRangeMax();
		return 0;
	}

	@Override //from IDViewerImageControllable
	public void setLRangeMax(final ISomethingChangeListener sender, final int lRangeMax) {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		if( editor != null )
			editor.setLRangeMax(sender, lRangeMax);
	}

	@Override //from IDViewerImageControllable
	public DownsampleType getDownsampleType() {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		if( editor != null )
			return editor.getDownsampleType();
		return null;
	}

	@Override //from IDViewerImageControllable
	public void setDownsampleType(final ISomethingChangeListener sender,
			final DownsampleType downsampleType) {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		if( editor != null )
			editor.setDownsampleType(sender, downsampleType);
	}

	@Override //from IDViewerImageControllable
	public Point2DD getMouseAxisPos() {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		return editor.getMouseAxisPos();
	}

	@Override //from IDViewerImageControllable
	public String getStatusText() {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		return editor.getStatusText();
	}

	@Override //from IDViewerImageControllable
	public void requestDViewerView() {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		editor.requestDViewerView();
	}

	@Override //from IDViewerImageControllable
	public void requestDViewerControls() {
		final IDViewerImageControllable editor = getActiveImageControllableEditor();
		editor.requestDViewerControls();
	}

	@Override //from IDViewerImageControllable
	public void addSomethingListener(final ISomethingChangeListener listener) {
		listenerManager.addSomethingListener(listener);
	}

	@Override //from IDViewerImageControllable
	public void removeSomethingListener(final ISomethingChangeListener listener) {
		listenerManager.removeSomethingListener(listener);
	}

}
