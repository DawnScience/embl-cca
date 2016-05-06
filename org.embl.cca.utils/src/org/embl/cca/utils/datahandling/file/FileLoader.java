package org.embl.cca.utils.datahandling.file;

import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import org.dawb.common.util.list.ListenerList;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dawnsci.analysis.api.dataset.IDataset;
import org.eclipse.dawnsci.analysis.api.diffraction.DiffractionCrystalEnvironment;
import org.eclipse.dawnsci.analysis.api.io.ILoaderService;
import org.eclipse.dawnsci.analysis.api.metadata.IDiffractionMetadata;
import org.eclipse.dawnsci.analysis.api.metadata.IMetadata;
import org.eclipse.dawnsci.analysis.dataset.impl.Dataset;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetFactory;
import org.eclipse.dawnsci.analysis.dataset.impl.DatasetUtils;
import org.eclipse.swt.SWT;
import org.embl.cca.utils.datahandling.AbstractDatasetAndFileDescriptor;
import org.embl.cca.utils.datahandling.DatasetNumber;
import org.embl.cca.utils.datahandling.DatasetTypeSeparatedUtils;
import org.embl.cca.utils.datahandling.EFile;
import org.embl.cca.utils.datahandling.FileWithTag;
import org.embl.cca.utils.extension.CommonExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileLoader {
	class FileAndMetadata {
		protected FileWithTag file;
		protected IMetadata metadata;

		public FileAndMetadata(FileWithTag file) {
			this(file, null);
		}

		public FileAndMetadata(FileWithTag file, IMetadata metadata) {
			this.file = file;
			this.metadata = metadata;
		}

		public FileWithTag getFile() {
			return file;
		}

		public IMetadata getMetadata() {
			return metadata;
		}

		public void setMetadata(IMetadata metadata) {
			this.metadata = metadata;
		}

		public IDiffractionMetadata getDiffractionMetadata() {
			if( metadata instanceof IDiffractionMetadata)
				return (IDiffractionMetadata)metadata;
			throw new RuntimeException("Metadata is not diffraction metadata");
		}

	}

	protected static Comparator<FileAndMetadata> fileAndMetaIndexComparator = new Comparator<FileAndMetadata>() {
		@Override
		public int compare(FileAndMetadata o1, FileAndMetadata o2) {
			return ((AbstractDatasetAndFileDescriptor)o1.getFile().getTag()).getIndexInCollection() - ((AbstractDatasetAndFileDescriptor)o2.getFile().getTag()).getIndexInCollection();
		}
	};

	final double NOT_MEASURED_VALUE = -1; //TODO hardcoded, also its type should be adjusted to dataset
	final double BAD_PIXEL_VALUE = -2; //TODO hardcoded, also its type should be adjusted to dataset

	protected static Logger logger = LoggerFactory.getLogger(FileLoader.class);

	protected ICollectionFile vcFile = null;
	protected final Vector<FileAndMetadata> loadedFileAndMetadatas; //The loaded files (without content, the content is summed) and their metadata
	protected boolean resultSetNeedsUpdate; //True if the result dataset must be updated (lazily)
	protected Dataset resultSet; //Dataset of single file, or merged datasets of multiple files, see layeredImageMode
	protected boolean layeredImageMode; //False until at least 2 images are loaded, else true (becomes false on clear()).
	//If true, then the sum of images is stored as 3 things: set, not measured mask, bad mask
	protected Dataset summedSet; //Valid when layeredImageMode == true 
	protected Dataset summedNotMeasuredMask; //Valid when layeredImageMode == true
	protected Dataset summedBadMask; //Valid when layeredImageMode == true

//	protected ExecutableManager imageLoaderManager;
//	protected final Boolean fileLoadingLock = new Boolean(false);  //This is a lock, has no value. This data is used for locking by synchronising it: ... TODO what?
	protected final FileLoaderJob fileLoaderJob;

	protected class FileLoaderJob extends Job {
//		Vector<FileWithTag> toLoadImageFilesJob = new Vector<FileWithTag>( Arrays.asList(toLoadImageFiles) );
		protected final FileLoader fileLoader = FileLoader.this;
		protected int batchIndex;
		protected int batchSize;
		protected boolean newFile;

		public FileLoaderJob() {
			super("Read image data");
			setUser(false);
			setSystem(true);
			setPriority(Job.SHORT);
		}

		public void reschedule(final int batchIndex, final int batchSize, final boolean newFile) {
			cancel();
			this.batchIndex = batchIndex; //earlier from
			this.batchSize = batchSize; //earlier amount
			this.newFile = newFile;
			schedule();
		}

//		public IStatus processImage(FileWithTag imageFile, boolean add) {
//			Dataset set = null;
//			IMetadata localMetadata = null;
//			boolean loadedAtLeast2;
////			synchronized (resultDataset) {
//				if( isAborting() )
//					return Status.CANCEL_STATUS;
//				loadedAtLeast2 = loadedMetaAndFiles.size() > 1;
////			}
//			if( add || loadedAtLeast2 ) { //When removing last set, no need to load it
//				final String filePath = imageFile.getAbsolutePath();
//				try {
////					imageModel = ImageModelFactory.getImageModel(filePath);
//					final ILoaderService service = (ILoaderService)ServiceManager.getService(ILoaderService.class);
//					set = service.getDataset(filePath).clone(); //TODO check if set is null
//					localMetadata = set.getMetadata();
//					if (!(localMetadata instanceof IDiffractionMetadata))
//						throw new RuntimeException("File has no diffraction metadata");
//				} catch (Throwable e) {
//					logger.error("Cannot load file "+filePath, e);
//					return Status.CANCEL_STATUS;
//				}
//			}
//			if( isAborting() )
//				return Status.CANCEL_STATUS;
//			final int ind = 143336;
//			//Warning: getMergedDataset updates the dataset, which slows down execution if batch amount is big
////			if( resultDataset.getMergedDataset() == null )
////				logger.debug("HRMM, before [add=" + add + "], s[ind]=" + set.getElementLongAbs(ind));
////			else
////				logger.debug("HRMM, before [add=" + add + "], rD[ind]=" + resultDataset.getMergedDataset().getElementLongAbs(ind) + ", s[ind]=" + set.getElementLongAbs(ind));
//			Double cutoff = Double.NaN;
//			try {
//				IDiffractionMetadata localDiffractionMetadata2 = (IDiffractionMetadata)localMetadata;
////				Serializable s = localDiffractionMetadata2.getMetaValue("NXdetector:pixel_overload"); //This would be if GDAMetadata would be available in CBFLoader
//				cutoff = Double.parseDouble(localDiffractionMetadata2.getMetaValue("Count_cutoff").split("counts")[0]); //TODO little bit hardcoded
//				logger.debug("Converting values above cutoff=" + cutoff);
////				convertAboveCutoffToError(set, threshold);
//			} catch (Exception e) {
//			}
//			final double BAD_PIXEL_VALUE = -1; //TODO hardcoded
////			synchronized (resultDataset) {
//				if( isAborting() )
//					return Status.CANCEL_STATUS;
//				if( add ) {
//					add( set, imageFile, cutoff, BAD_PIXEL_VALUE );
//				} else {
//					remove( set, imageFile, cutoff, BAD_PIXEL_VALUE );
//				}
//				//Warning: getMergedDataset updates the dataset, which slows down execution if batch amount is big
////				logger.debug("HRMM, after  [add=" + add + "], rD[ind]=" + resultDataset.getMergedDataset().getElementLongAbs(ind) + ", s[ind]=" + set.getElementLongAbs(ind));
////			}
//			return Status.OK_STATUS;
//		}

		@Override
		public IStatus run(final IProgressMonitor monitor) {
			/* Since running this and others as well through imageLoaderManager,
			 * the single thread of this loading is guaranteed.
			 */
			IStatus result = Status.CANCEL_STATUS;
			//We have to load files into fileLoader with imageLoaderManager from from the amount of amount
			final Vector<FileWithTag> adding = new Vector<FileWithTag>(0);
			final Vector<FileWithTag> removing = new Vector<FileWithTag>(0);
			final IProgressMonitor thisMonitor = monitor == null ? new NullProgressMonitor() : monitor;
			thisMonitor.beginTask("Loading file(s)", adding.size() + removing.size() + 1); //+1 for fireLoading*
			try {
				do {
//					synchronized (fileLoadingLock) {
						if( thisMonitor.isCanceled() )
							break;
						getFilesToAddAndRemove(batchIndex, batchSize, adding, removing);
						System.out.println("batchIndex=" + batchIndex + ", batchSize=" + batchSize + ", adding=" + adding + ", removing=" + removing);
						if( thisMonitor.isCanceled() ) //Better check ASAP if must abort here, because if yes, the adding and removing are not valid
							break;
						if( removing.isEmpty() && adding.isEmpty() ) //This means nothing to add or remove, then return without doing anything
							return Status.OK_STATUS;
//					}
					result = Status.OK_STATUS;
					for( FileWithTag i : adding ) {
						if( thisMonitor.isCanceled() )
							break;
//						logger.debug("EHM, adding " + ((AbstractDatasetAndFileDescriptor)i.getTag()).getIndexInCollection() + ". file");
//						result = processImage(i, true);
						try {
							loadAndAddFile(i);
							thisMonitor.worked(1);
						} catch (Exception e) { //IOException, RuntimeException
							result = Status.CANCEL_STATUS;
							logger.error("Can not load the file: " + i.toString(), e);
							break;
						}
					}
					if( result != Status.OK_STATUS || thisMonitor.isCanceled() )
						break;
					for( FileWithTag i : removing ) {
						if( thisMonitor.isCanceled() )
							break;
//						logger.debug("EHM, removing " + ((AbstractDatasetAndFileDescriptor)i.getTag()).getIndexInCollection() + ". file");
//						result = processImage(i, false);
						try {
							loadAndRemoveFile(i);
							thisMonitor.worked(1);
						} catch (Exception e) { //IOException, RuntimeException
							logger.error("Can not load the file: " + i.toString(), e);
							result = Status.CANCEL_STATUS;
							break;
						}
					}
					if( result != Status.OK_STATUS || thisMonitor.isCanceled() )
						break;
					result = Status.OK_STATUS;
				} while( false );
				if( thisMonitor.isCanceled() ) {
					result = Status.CANCEL_STATUS;
					fireLoadingCancelled(newFile);
				} else if( result == Status.CANCEL_STATUS ) {
					fireLoadingFailed(newFile);
				} else if( result == Status.OK_STATUS )
					fireLoadingDone(newFile, new SubProgressMonitor(thisMonitor, 1));
				//TODO else fire error
				return result;
			} finally {
				thisMonitor.done();
			}
		}
	};

	public FileLoader() {
		loadedFileAndMetadatas = new Vector<FileAndMetadata>();
//		imageLoaderManager = null;
		clearLoaded();
		fileLoaderJob = new FileLoaderJob();
	}

	/* VirtualCollectionFile related methods, probably should not duplicate them here, instead
	 * should let the caller use the vcFile's methods.
	 */
	/**
	 * Method to list files and directories of a parent folder, only those satisfying the filter.
	 * Override this method to act like a virtual filesystem supporting the files and directories.
	 * @param parent the parent folder
	 * @param filter the filter to ignore files and directories, it can be null
	 * @return
	 */
	protected FileWithTag[] listFiles(FileWithTag parent, FileFilter filter) {
		return parent.listFiles(filter);
	}

	public synchronized boolean refreshAllFiles() throws FileNotFoundException {
		final boolean result = vcFile.refreshAllFiles();
		if( result )
			cancelLoading();
//		if( result && imageLoaderManager != null )
//			imageLoaderManager.interrupt(); //Because vcFile was refreshed, stop loading previously started crap
		return result;
	}

	/**
	 * Check and refresh only new files, not touching the already processed files (thus their
	 * file descriptor remains the same, even if should be updated).
	 * This way the loading of files is not disturbed at all.
	 * @return true if some new files were added.
	 * @throws FileNotFoundException
	 */
	public synchronized boolean refreshNewAllFiles() throws FileNotFoundException {
		final boolean result = vcFile.refreshNewAllFiles();
		if( result )
			cancelLoading();
//		if( result && imageLoaderManager != null )
//			imageLoaderManager.interrupt(); //Because vcFile was refreshed, stop loading previously started crap
		return result;
	}

	public void setFilePath(String filePath) {
		if( filePath == null ) {
			vcFile = null;
			return;
		}
		if( vcFile != null && vcFile.getAbsolutePath().equals(filePath))
			return;
		try {
			vcFile = new VirtualCollectionFile(filePath)
//			{
//				/**
//				 * 
//				 */
//				private static final long serialVersionUID = 835303686617142439L;
//
////				public FileLoader$2(String pathname) {
////					super(pathname);
////				}
//				
//				protected FileWithTag[] listFiles(FileWithTag parent, FileFilter filter) {
//					return FileLoader.this.listFiles(parent, filter);
//				}
//			}
			;
		} catch( IllegalArgumentException e ) {
			vcFile = new EmulatedCollectionFile(filePath);
		}
	}

	/**
	 * Returns the currently loaded collection file, which as a file looks like the file by which the collection was made.
	 * @return the currently loaded collection file
	 */
	public ICollectionFile getFile() {
		return vcFile;
	}

//	protected synchronized IPath getFilesOriginWithoutProtocol() {
//		if( filesOrigin == null )
//			return null;
//		return new Path(EFile.getPathWithoutProtocol(filesOrigin));
//	}
//
	protected synchronized String getParentPathWithTrailingSeparator() {
		return EFile.getPathWithTrailingSeparator(vcFile.getParent());
	}

	public synchronized boolean isCollection() {
		return vcFile instanceof VirtualCollectionFile;
	}

	public synchronized String getCollectionName() {
		return vcFile.getCollectionName();
	}

	public synchronized String getCollectionAbsoluteName() {
		return getParentPathWithTrailingSeparator() + getCollectionName();
	}

	public synchronized FileWithTag getCollectionDelegate() {
		VirtualCollectionFile result = new VirtualCollectionFile(VirtualCollectionFile.VirtualCollectionFileProtocolID + VirtualCollectionFile.FileProtocolSeparator + getCollectionAbsoluteName());
		return result;
	}

	/* The real methods of FileLoader. */
	/**
	 * 
	 * @param filePath
	 * @return index of file in all files. If index is less than 0, then file is not found. Anyway it would be at -(index+1) position considering the ordering.
	 */
//	public synchronized int getIndexOfFile(String filePath) {
//		return vcFile.getIndexOfFile(filePath);
//	}

	public synchronized String getFirstLoadedLegacyPath() {
		return loadedFileAndMetadatas.firstElement().getFile().getAbsolutePathWithoutProtocol();
	}

	/**
	 * Return the number of files in the collection, or 0 if the collection is null.
	 * 
	 * @return number of files in the collection, or 0 if collection is null.
	 */
	public int getAllLength() {
		return vcFile == null ? 0 : vcFile.getAllLength();
	}

	protected synchronized Vector<FileWithTag> getFilesFromLoaded() {
		Vector<FileWithTag> result = new Vector<FileWithTag>(loadedFileAndMetadatas.size());
		for( FileAndMetadata fAM : loadedFileAndMetadatas)
			result.add(fAM.getFile());
		return result;
	}

	protected synchronized void getFilesToAddAndRemove(int from, int amount, final Vector<FileWithTag> adding, final Vector<FileWithTag> removing) {
		Vector<FileWithTag> toLoadImageFiles = vcFile.getFilesFromAll(from, amount);
		Vector<FileWithTag> filesFromLoaded = getFilesFromLoaded();
		removing.clear();
		removing.addAll( filesFromLoaded );
		removing.removeAll( toLoadImageFiles );
		adding.clear();
		adding.addAll( toLoadImageFiles );
		adding.removeAll( filesFromLoaded );
		if( adding.size() + removing.size() > amount ) {
			adding.clear();
			adding.addAll(toLoadImageFiles);
			removing.clear();
			clearLoaded();
			logger.debug("Optimizing => clearing, adding.size=" + adding.size() + ", removing.size=" + removing.size() + ", toLoadImageFiles.size=" + amount);
		}
	}

	public synchronized void clearLoaded() {
		loadedFileAndMetadatas.clear();
//		loadedTotalLength = 0;
//		loadedLastModified = 0;
		resultSet = null;
		layeredImageMode = false;
		summedSet = null;
		summedNotMeasuredMask = null;
		summedBadMask = null;
		resultSetNeedsUpdate = false;
	}

	public synchronized int getLoadedLength() {
		return loadedFileAndMetadatas.size();
	}

	protected void addSplitImage(Dataset set, Number maxValidNumber, Number badNumber, Number notMeasuredValue) {
		if( loadedFileAndMetadatas.size() == 1 && !layeredImageMode) {
			summedSet = DatasetFactory.zeros(resultSet);
			summedNotMeasuredMask = DatasetFactory.zeros(resultSet);
			summedBadMask = DatasetFactory.zeros(resultSet);
			DatasetTypeSeparatedUtils.splitAddSet(resultSet, summedSet, summedBadMask, summedNotMeasuredMask, maxValidNumber, badNumber, notMeasuredValue); //TODO save threshold into set, because here we use threshold of 2nd image on 1st image, not the best
			layeredImageMode = true;
		}
		DatasetTypeSeparatedUtils.splitAddSet(set, summedSet, summedBadMask, summedNotMeasuredMask, maxValidNumber, badNumber, notMeasuredValue);
	}

	protected void removeSplitImage(Dataset set, Number maxValidNumber, Number badNumber, Number notMeasuredValue) {
		DatasetTypeSeparatedUtils.splitRemoveSet(set, summedSet, summedBadMask, summedNotMeasuredMask, maxValidNumber, badNumber, notMeasuredValue);
	}

	protected void add(IDataset set, FileWithTag imageFile, Number maxValidNumber, Number badNumber, Number notMeasuredValue) {
		IMetadata metadata = set.getMetadata();
		FileAndMetadata fAM = new FileAndMetadata(imageFile, metadata);
		int localIndex = 0;
		if( loadedFileAndMetadatas.size() == 0 ) {
			resultSet = DatasetUtils.convertToDataset(set);
			fAM.setMetadata(metadata != null ? metadata.clone() : null);
			DatasetTypeSeparatedUtils.splitJoinIntoSelf(resultSet, maxValidNumber, badNumber, notMeasuredValue);
		} else {
			IMetadata resultMetadata = resultSet.getMetadata();
			if( (resultMetadata == null && metadata != null) || (resultMetadata != null && metadata == null)
					|| (resultMetadata != null && metadata != null && resultMetadata.getClass() != metadata.getClass() ) )
				throw new RuntimeException("Metadata type of first loaded file differs from metadata type of this file: " + imageFile.getAbsolutePath() );
			localIndex = Collections.binarySearch(loadedFileAndMetadatas, fAM, fileAndMetaIndexComparator);
			if( localIndex < 0 )
				localIndex = -(localIndex + 1);
			addSplitImage(DatasetUtils.convertToDataset(set), maxValidNumber, badNumber, notMeasuredValue);
		}
		loadedFileAndMetadatas.add( localIndex, fAM );
		resultSetNeedsUpdate = true;
	}

	protected void remove(IDataset set, FileWithTag imageFile, Number maxValidNumber, Number badNumber, Number notMeasuredValue) {
		IMetadata metadata = set.getMetadata();
		FileAndMetadata fAM = new FileAndMetadata(imageFile, metadata);
		int localIndex = Collections.binarySearch(loadedFileAndMetadatas, fAM, fileAndMetaIndexComparator);
		if( localIndex < 0 )
			throw new RuntimeException("Can not remove the file from this container because could not found it: " + imageFile.getAbsolutePath());
		/*FileAndMetadata qwe = */loadedFileAndMetadatas.remove( localIndex );
		if( loadedFileAndMetadatas.size() == 0 ) {
			clearLoaded();
		} else {
			removeSplitImage(DatasetUtils.convertToDataset(set), maxValidNumber, badNumber, notMeasuredValue);
			resultSetNeedsUpdate = true;
		}
	}

	protected Dataset loadFileInternal(final FileWithTag imageFile) throws IOException {
		final String filePath = imageFile.getAbsolutePathWithoutProtocol();
		try {
			ILoaderService service = CommonExtension.getService(ILoaderService.class);
			Dataset set = DatasetUtils.convertToDataset(service.getDataset(filePath, null));
			if( set == null )
				throw new IOException("The loader returned null dataset for file: " + imageFile.getAbsolutePath()); //should the loader throw an exception?
			return set;
		} catch (Throwable e) {
			throw new IOException("Can not load the file: " + imageFile.getAbsolutePath() + " because: " + e.getMessage());
		}
	}

	protected void loadAndAddFile(FileWithTag imageFile) throws IOException {
		Dataset set = loadFileInternal(imageFile).clone();
		Number maxValidNumber = DatasetNumber.getMaxValidNumber(set, true);
		add( set, imageFile, maxValidNumber, DatasetNumber.getNumber(set, BAD_PIXEL_VALUE, true), DatasetNumber.getNumber(set, NOT_MEASURED_VALUE, true) ); //TODO little bit hardcoded: CBF images typically have cutoff and notMeasuredValue (-1), but what about others?
	}

	protected void loadAndRemoveFile(FileWithTag imageFile) throws IOException {
		Dataset set = loadFileInternal(imageFile).clone();
		Number maxValidNumber = DatasetNumber.getMaxValidNumber(set, true);
		remove( set, imageFile, maxValidNumber, DatasetNumber.getNumber(set, BAD_PIXEL_VALUE, true), DatasetNumber.getNumber(set, NOT_MEASURED_VALUE, true) ); //TODO little bit hardcoded: CBF images typically have cutoff and notMeasuredValue (-1), but what about others?
	}

	public boolean isLoading() {
		return fileLoaderJob.getState() != Job.NONE;
//		return imageLoaderManager != null && imageLoaderManager.isAlive();
	}

	public void cancelLoading() {
		fileLoaderJob.cancel();
//		imageLoaderManager.interrupt();
	}

	/**
	 * 
	 * @param from the index of first file in allImageFiles
	 * @param amount the amount of files in allImageFiles
	 */
	public void loadFiles(final int from, final int amount, final boolean newFile) {
		fileLoaderJob.reschedule(from, amount, newFile);
	}

	public void loadFile() {
		loadFiles(vcFile.getAllLength() - 1, 1, false);
//		loadFiles(getIndexFromPathFromAll(filesOrigin), 1);
	}

	public synchronized void updateMetadata() {
		if( resultSetNeedsUpdate ) {
			IMetadata metadata = resultSet.getMetadata();
			if( metadata instanceof IDiffractionMetadata ) { //This (and loader's algorithm) guarantees that each file has this kind of metadata
				IDiffractionMetadata mergedDiffractionMetadata = (IDiffractionMetadata)metadata;
				//oscStart = first.oscStart, oscRange = last.oscStart + last.oscRange - first.oscStart, oscGap = summa(this.oscStart - prev.oscEnd) + summa this.oscGap, exposure time = summa exposure time
				//TODO implement the algorithm which considers "interleaved" set (for example: oscStart={0,90,180,270,1,91,181,271,2,...})
				double exposureTime = 0;
				double oscGap = 0;
				DiffractionCrystalEnvironment previousDCE = null;
				int iSup = loadedFileAndMetadatas.size();
				for( int i = 0; i < iSup; i++ ) {
					DiffractionCrystalEnvironment currentDCE = loadedFileAndMetadatas.get(i).getDiffractionMetadata().getDiffractionCrystalEnvironment();
					double currentExposureTime = currentDCE.getExposureTime();
					if( !Double.isNaN(currentExposureTime) )
						exposureTime += currentExposureTime;
					double currentOscGap= currentDCE.getOscGap();
					if( !Double.isNaN(currentOscGap) )
						oscGap += currentOscGap;
					if( i > 0 )
						oscGap += currentDCE.getPhiStart() - previousDCE.getPhiStart() - previousDCE.getPhiRange();
					previousDCE = currentDCE;
				}
				DiffractionCrystalEnvironment firstDCE = loadedFileAndMetadatas.firstElement().getDiffractionMetadata().getDiffractionCrystalEnvironment();
				DiffractionCrystalEnvironment lastDCE = loadedFileAndMetadatas.lastElement().getDiffractionMetadata().getDiffractionCrystalEnvironment();
	//			DetectorProperties mergedDetConfig = mergedDiffractionMetadata.getDetector2DProperties();
				DiffractionCrystalEnvironment resultDCE = mergedDiffractionMetadata.getDiffractionCrystalEnvironment();
				resultDCE.setPhiStart(firstDCE.getPhiStart());
				double oscRange = Double.isNaN(lastDCE.getPhiStart()) || Double.isNaN(lastDCE.getPhiRange()) || Double.isNaN(firstDCE.getPhiStart()) ?
						Double.NaN : lastDCE.getPhiStart() + lastDCE.getPhiRange() - firstDCE.getPhiStart();
				resultDCE.setPhiRange(oscRange);
				resultDCE.setExposureTime(exposureTime);
				resultDCE.setOscGap(oscGap);
			}
			String name = loadedFileAndMetadatas.firstElement().getFile().getName();
			if( loadedFileAndMetadatas.size() > 1 )
				name += " [" + loadedFileAndMetadatas.size() + "]";
			resultSet.setName( name );
			if( layeredImageMode ) {
				//TODO little bit hardcoded: CBF images typically have badPixelValue (-2) and notMeasuredValue (-1), but what about others?
				DatasetTypeSeparatedUtils.joinSplittedSets(resultSet, summedSet, summedBadMask, summedNotMeasuredMask, DatasetNumber.getNumber(resultSet, BAD_PIXEL_VALUE, true), DatasetNumber.getNumber(resultSet, NOT_MEASURED_VALUE, true));
			}
			resultSetNeedsUpdate = false;
		}
	}

	/**
	 * Returns an Dataset containing all the loaded files merged.
	 * The resulting Dataset is affected by subsequent adds or removes, i.e. loading other files.
	 * <p>
	 * Note: Callers of this method <b>must not</b> modify the returned array. 
	 *
	 * @return the merged dataset
	 */
	public synchronized Dataset getMergedDataset() {
		updateMetadata();
		return resultSet;
	}

	protected final ListenerList<IFileLoaderListener> fileLoaderListeners = new ListenerList<IFileLoaderListener>();

	public void addFileLoaderListener(IFileLoaderListener listener) {
		if (listener == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		fileLoaderListeners.add(listener);
	}

	public void removeFileLoaderListener(IFileLoaderListener listener) {
		if (listener == null)
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		fileLoaderListeners.remove(listener);
	}

	public void fireLoadingDone(final boolean newFile, final IProgressMonitor monitor) {
		for( final IFileLoaderListener listener : fileLoaderListeners )
			listener.fileLoadingDone(this, newFile, monitor);
	}

	public void fireLoadingCancelled(final boolean newFile) {
		for( final IFileLoaderListener listener : fileLoaderListeners )
			listener.fileLoadingCancelled(this, newFile);
	}

	public void fireLoadingFailed(final boolean newFile) {
		for( final IFileLoaderListener listener : fileLoaderListeners )
			listener.fileLoadingFailed(this, newFile);
	}

}
