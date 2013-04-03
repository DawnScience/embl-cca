package org.embl.cca.utils.datahandling.file;

import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import org.dawb.common.services.ILoaderService;
import org.dawb.common.services.ServiceManager;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.embl.cca.utils.datahandling.AbstractDatasetAndFileDescriptor;
import org.embl.cca.utils.datahandling.DatasetNumber;
import org.embl.cca.utils.datahandling.EFile;
import org.embl.cca.utils.datahandling.FileWithTag;
import org.embl.cca.utils.eventhandling.ListenerList;
import org.embl.cca.utils.threading.ExecutableManager;
import org.embl.cca.utils.threading.TrackableJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.BooleanDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ByteDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DoubleDataset;
import uk.ac.diamond.scisoft.analysis.dataset.FloatDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IntegerDataset;
import uk.ac.diamond.scisoft.analysis.dataset.LongDataset;
import uk.ac.diamond.scisoft.analysis.dataset.ShortDataset;
import uk.ac.diamond.scisoft.analysis.diffraction.DiffractionCrystalEnvironment;
import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
import uk.ac.diamond.scisoft.analysis.io.IMetaData;

public class FileLoader {
	class FileAndMetadata {
		protected FileWithTag file;
		protected IMetaData metadata;

		public FileAndMetadata(FileWithTag file) {
			this(file, null);
		}

		public FileAndMetadata(FileWithTag file, IMetaData metadata) {
			this.file = file;
			this.metadata = metadata;
		}

		public FileWithTag getFile() {
			return file;
		}

		public IMetaData getMetadata() {
			return metadata;
		}

		public void setMetadata(IMetaData metadata) {
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
	protected Vector<FileAndMetadata> loadedFileAndMetadatas; //The loaded files (without content, the content is summed) and their metadata
	protected boolean resultSetNeedsUpdate; //True if the result dataset must be updated (lazily)
	protected AbstractDataset resultSet; //Dataset of single file, or merged datasets of multiple files, see layeredImageMode
	protected boolean layeredImageMode; //False until at least 2 images are loaded, else true (becomes false on clear()).
	//If true, then the sum of images is stored as 3 things: set, not measured mask, bad mask
	protected AbstractDataset summedSet; //Valid when layeredImageMode == true 
	protected AbstractDataset summedNotMeasuredMask; //Valid when layeredImageMode == true
	protected AbstractDataset summedBadMask; //Valid when layeredImageMode == true

	protected ExecutableManager imageLoaderManager;
	protected Boolean fileLoadingLock; //Value does not matter, this data is used for locking by synchronising it: ... TODO 

	public FileLoader() {
		loadedFileAndMetadatas = new Vector<FileAndMetadata>();
		imageLoaderManager = null;
		clearLoaded();
//		fileLoadingLock = new Boolean(false);
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
		boolean result = vcFile.refreshAllFiles();
		if( result && imageLoaderManager != null )
			imageLoaderManager.interrupt(); //Because vcFile was refreshed, stop loading previously started crap
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
		boolean result = vcFile.refreshNewAllFiles();
		if( result && imageLoaderManager != null )
			imageLoaderManager.interrupt(); //Because vcFile was refreshed, stop loading previously started crap
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

	/**
	 * This method is similar to AbstractDataset.iadd( set ), but handles not measured values and bad values.
	 * @param set the dataset to add
	 * @param maxValidNumber the maximum valid value. Above this value the values are considered bad. 
	 * @param notMeasuredValue the value which means the value is not measured. Typically -1 in CBF files.
	 */
	protected void addSplitImageInternal(AbstractDataset set, Number maxValidNumber, Number notMeasuredNumber) {
		int type = set.getDtype();
		//In case of BOOL, maxValidNumber and notMeasuredNumber have no sense
		boolean maxValidValueValid = type != AbstractDataset.BOOL && maxValidNumber != null;
		boolean notMeasuredValueValid = type != AbstractDataset.BOOL && notMeasuredNumber != null;
		switch (type) {
			case AbstractDataset.BOOL: {
				boolean[] currentData = ((BooleanDataset)set).getData();
				boolean[] setData = ((BooleanDataset)summedSet).getData();
				int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					setData[i] |= currentData[i];
				}
				break;
			}
			case AbstractDataset.INT32: {
				int[] currentData = ((IntegerDataset)set).getData();
				int[] setData = ((IntegerDataset)summedSet).getData();
				int[] notMeasuredMaskData = ((IntegerDataset)summedNotMeasuredMask).getData();
				int[] badMaskData = ((IntegerDataset)summedBadMask).getData();
				int maxValidValue = maxValidNumber.intValue();
				int notMeasuredValue = notMeasuredNumber.intValue();
				int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					if( maxValidValueValid && currentData[ i ] > maxValidValue )
						badMaskData[ i ]++;
					else if( notMeasuredValueValid && currentData[ i ] == notMeasuredValue )
						notMeasuredMaskData[ i ]++;
					else
						setData[i] += currentData[i];
				}
				break;
			}
			case AbstractDataset.INT8: {
				byte[] currentData = ((ByteDataset)set).getData();
				byte[] setData = ((ByteDataset)summedSet).getData();
				byte[] notMeasuredMaskData = ((ByteDataset)summedNotMeasuredMask).getData();
				byte[] badMaskData = ((ByteDataset)summedBadMask).getData();
				byte maxValidValue = maxValidNumber.byteValue();
				byte notMeasuredValue = notMeasuredNumber.byteValue();
				int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					if( maxValidValueValid && currentData[ i ] > maxValidValue )
						badMaskData[ i ]++;
					else if( notMeasuredValueValid && currentData[ i ] == notMeasuredValue )
						notMeasuredMaskData[ i ]++;
					else
						setData[i] += currentData[i];
				}
				break;
			}
			case AbstractDataset.INT16: {
				short[] currentData = ((ShortDataset)set).getData();
				short[] setData = ((ShortDataset)summedSet).getData();
				short[] notMeasuredMaskData = ((ShortDataset)summedNotMeasuredMask).getData();
				short[] badMaskData = ((ShortDataset)summedBadMask).getData();
				short maxValidValue = maxValidNumber.shortValue();
				short notMeasuredValue = notMeasuredNumber.shortValue();
				int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					if( maxValidValueValid && currentData[ i ] > maxValidValue )
						badMaskData[ i ]++;
					else if( notMeasuredValueValid && currentData[ i ] == notMeasuredValue )
						notMeasuredMaskData[ i ]++;
					else
						setData[i] += currentData[i];
				}
				break;
			}
			case AbstractDataset.INT64: {
				long[] currentData = ((LongDataset)set).getData();
				long[] setData = ((LongDataset)summedSet).getData();
				long[] notMeasuredMaskData = ((LongDataset)summedNotMeasuredMask).getData();
				long[] badMaskData = ((LongDataset)summedBadMask).getData();
				long maxValidValue = maxValidNumber.longValue();
				long notMeasuredValue = notMeasuredNumber.longValue();
				int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					if( maxValidValueValid && currentData[ i ] > maxValidValue )
						badMaskData[ i ]++;
					else if( notMeasuredValueValid && currentData[ i ] == notMeasuredValue )
						notMeasuredMaskData[ i ]++;
					else
						setData[i] += currentData[i];
				}
				break;
			}
			case AbstractDataset.FLOAT32: {
				float[] currentData = ((FloatDataset)set).getData();
				float[] setData = ((FloatDataset)summedSet).getData();
				float[] notMeasuredMaskData = ((FloatDataset)summedNotMeasuredMask).getData();
				float[] badMaskData = ((FloatDataset)summedBadMask).getData();
				float maxValidValue = maxValidNumber.floatValue();
				float notMeasuredValue = notMeasuredNumber.floatValue();
				int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					if( maxValidValueValid && currentData[ i ] > maxValidValue )
						badMaskData[ i ]++;
					else if( notMeasuredValueValid && currentData[ i ] == notMeasuredValue )
						notMeasuredMaskData[ i ]++;
					else
						setData[i] += currentData[i];
				}
				break;
			}
			case AbstractDataset.FLOAT64: {
				double[] currentData = ((DoubleDataset)set).getData();
				double[] setData = ((DoubleDataset)summedSet).getData();
				double[] notMeasuredMaskData = ((DoubleDataset)summedNotMeasuredMask).getData();
				double[] badMaskData = ((DoubleDataset)summedBadMask).getData();
				double maxValidValue = maxValidNumber.doubleValue();
				double notMeasuredValue = notMeasuredNumber.doubleValue();
				int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					if( maxValidValueValid && currentData[ i ] > maxValidValue )
						badMaskData[ i ]++;
					else if( notMeasuredValueValid && currentData[ i ] == notMeasuredValue )
						notMeasuredMaskData[ i ]++;
					else
						setData[i] += currentData[i];
				}
				break;
			}
			default:
				throw new RuntimeException("Not supported dataset type: " + set.getDtype() );
		}
	}

	/**
	 * This method is similar to AbstractDataset.isubstract( set ), but handles not measured values and bad values.
	 * @param set the dataset to substract
	 * @param maxValidNumber the maximum valid value. Above this value the values are considered bad. 
	 * @param notMeasuredValue the value which means the value is not measured. Typically -1 in CBF files.
	 */
	protected void removeSplitImageInternal(AbstractDataset set, Number maxValidNumber, Number notMeasuredNumber) {
		int type = set.getDtype();
		//In case of BOOL, maxValidNumber and notMeasuredNumber have no sense
		boolean maxValidValueValid = type != AbstractDataset.BOOL && maxValidNumber != null;
		boolean notMeasuredValueValid = type != AbstractDataset.BOOL && notMeasuredNumber != null;
		switch (type) {
			case AbstractDataset.BOOL: {
				boolean[] currentData = ((BooleanDataset)set).getData();
				boolean[] setData = ((BooleanDataset)summedSet).getData();
				int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					setData[i] &= !currentData[i];
				}
				break;
			}
			case AbstractDataset.INT32: {
				int[] currentData = ((IntegerDataset)set).getData();
				int[] setData = ((IntegerDataset)summedSet).getData();
				int[] notMeasuredMaskData = ((IntegerDataset)summedNotMeasuredMask).getData();
				int[] badMaskData = ((IntegerDataset)summedBadMask).getData();
				int maxValidValue = maxValidNumber.intValue();
				int notMeasuredValue = notMeasuredNumber.intValue();
				int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					if( maxValidValueValid && currentData[ i ] > maxValidValue )
						badMaskData[ i ]--;
					else if( notMeasuredValueValid && currentData[ i ] == notMeasuredValue )
						notMeasuredMaskData[ i ]--;
					else
						setData[i] -= currentData[i];
				}
				break;
			}
			case AbstractDataset.INT8: {
				byte[] currentData = ((ByteDataset)set).getData();
				byte[] setData = ((ByteDataset)summedSet).getData();
				byte[] notMeasuredMaskData = ((ByteDataset)summedNotMeasuredMask).getData();
				byte[] badMaskData = ((ByteDataset)summedBadMask).getData();
				byte maxValidValue = maxValidNumber.byteValue();
				byte notMeasuredValue = notMeasuredNumber.byteValue();
				int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					if( maxValidValueValid && currentData[ i ] > maxValidValue )
						badMaskData[ i ]--;
					else if( notMeasuredValueValid && currentData[ i ] == notMeasuredValue )
						notMeasuredMaskData[ i ]--;
					else
						setData[i] -= currentData[i];
				}
				break;
			}
			case AbstractDataset.INT16: {
				short[] currentData = ((ShortDataset)set).getData();
				short[] setData = ((ShortDataset)summedSet).getData();
				short[] notMeasuredMaskData = ((ShortDataset)summedNotMeasuredMask).getData();
				short[] badMaskData = ((ShortDataset)summedBadMask).getData();
				short maxValidValue = maxValidNumber.shortValue();
				short notMeasuredValue = notMeasuredNumber.shortValue();
				int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					if( maxValidValueValid && currentData[ i ] > maxValidValue )
						badMaskData[ i ]--;
					else if( notMeasuredValueValid && currentData[ i ] == notMeasuredValue )
						notMeasuredMaskData[ i ]--;
					else
						setData[i] -= currentData[i];
				}
				break;
			}
			case AbstractDataset.INT64: {
				long[] currentData = ((LongDataset)set).getData();
				long[] setData = ((LongDataset)summedSet).getData();
				long[] notMeasuredMaskData = ((LongDataset)summedNotMeasuredMask).getData();
				long[] badMaskData = ((LongDataset)summedBadMask).getData();
				long maxValidValue = maxValidNumber.longValue();
				long notMeasuredValue = notMeasuredNumber.longValue();
				int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					if( maxValidValueValid && currentData[ i ] > maxValidValue )
						badMaskData[ i ]--;
					else if( notMeasuredValueValid && currentData[ i ] == notMeasuredValue )
						notMeasuredMaskData[ i ]--;
					else
						setData[i] -= currentData[i];
				}
				break;
			}
			case AbstractDataset.FLOAT32: {
				float[] currentData = ((FloatDataset)set).getData();
				float[] setData = ((FloatDataset)summedSet).getData();
				float[] notMeasuredMaskData = ((FloatDataset)summedNotMeasuredMask).getData();
				float[] badMaskData = ((FloatDataset)summedBadMask).getData();
				float maxValidValue = maxValidNumber.floatValue();
				float notMeasuredValue = notMeasuredNumber.floatValue();
				int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					if( maxValidValueValid && currentData[ i ] > maxValidValue )
						badMaskData[ i ]--;
					else if( notMeasuredValueValid && currentData[ i ] == notMeasuredValue )
						notMeasuredMaskData[ i ]--;
					else
						setData[i] -= currentData[i];
				}
				break;
			}
			case AbstractDataset.FLOAT64: {
				double[] currentData = ((DoubleDataset)set).getData();
				double[] setData = ((DoubleDataset)summedSet).getData();
				double[] notMeasuredMaskData = ((DoubleDataset)summedNotMeasuredMask).getData();
				double[] badMaskData = ((DoubleDataset)summedBadMask).getData();
				double maxValidValue = maxValidNumber.doubleValue();
				double notMeasuredValue = notMeasuredNumber.doubleValue();
				int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					if( maxValidValueValid && currentData[ i ] > maxValidValue )
						badMaskData[ i ]--;
					else if( notMeasuredValueValid && currentData[ i ] == notMeasuredValue )
						notMeasuredMaskData[ i ]--;
					else
						setData[i] -= currentData[i];
				}
				break;
			}
			default:
				throw new RuntimeException("Not supported dataset type: " + set.getDtype() );
		}
	}

	/**
	 * This method is similar to AbstractDataset.isubstract( set ), but handles not measured values and bad values.
	 * @param set the dataset to substract
	 * @param badNumber the value representing bad value. 
	 * @param notMeasuredValue the value which means the value is not measured. Typically -1 in CBF files.
	 */
	protected void mixSplitted(Number badNumber, Number notMeasuredNumber) {
		int type = resultSet.getDtype();
		//In case of BOOL, badNumber and notMeasuredNumber have no sense
		switch (type) {
			case AbstractDataset.BOOL: {
				boolean[] currentData = ((BooleanDataset)resultSet).getData();
				boolean[] setData = ((BooleanDataset)summedSet).getData();
				int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					currentData[ i ] = setData[ i ];
				}
				break;
			}
			case AbstractDataset.INT32: {
				int[] currentData = ((IntegerDataset)resultSet).getData();
				int[] setData = ((IntegerDataset)summedSet).getData();
				int[] notMeasuredMaskData = ((IntegerDataset)summedNotMeasuredMask).getData();
				int[] badMaskData = ((IntegerDataset)summedBadMask).getData();
				int badValue = badNumber.intValue();
				int notMeasuredValue = notMeasuredNumber.intValue();
				int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					if( badMaskData[ i ] > 0 )
						currentData[ i ] = badValue;
					else if( notMeasuredMaskData[ i ] > 0 )
						currentData[ i ] = notMeasuredValue;
					else
						currentData[ i ] = setData[ i ];
				}
				break;
			}
			case AbstractDataset.INT8: {
				byte[] currentData = ((ByteDataset)resultSet).getData();
				byte[] setData = ((ByteDataset)summedSet).getData();
				byte[] notMeasuredMaskData = ((ByteDataset)summedNotMeasuredMask).getData();
				byte[] badMaskData = ((ByteDataset)summedBadMask).getData();
				byte badValue = badNumber.byteValue();
				byte notMeasuredValue = notMeasuredNumber.byteValue();
				int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					if( badMaskData[ i ] > 0 )
						currentData[ i ] = badValue;
					else if( notMeasuredMaskData[ i ] > 0 )
						currentData[ i ] = notMeasuredValue;
					else
						currentData[ i ] = setData[ i ];
				}
				break;
			}
			case AbstractDataset.INT16: {
				short[] currentData = ((ShortDataset)resultSet).getData();
				short[] setData = ((ShortDataset)summedSet).getData();
				short[] notMeasuredMaskData = ((ShortDataset)summedNotMeasuredMask).getData();
				short[] badMaskData = ((ShortDataset)summedBadMask).getData();
				short badValue = badNumber.shortValue();
				short notMeasuredValue = notMeasuredNumber.shortValue();
				int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					if( badMaskData[ i ] > 0 )
						currentData[ i ] = badValue;
					else if( notMeasuredMaskData[ i ] > 0 )
						currentData[ i ] = notMeasuredValue;
					else
						currentData[ i ] = setData[ i ];
				}
				break;
			}
			case AbstractDataset.INT64: {
				long[] currentData = ((LongDataset)resultSet).getData();
				long[] setData = ((LongDataset)summedSet).getData();
				long[] notMeasuredMaskData = ((LongDataset)summedNotMeasuredMask).getData();
				long[] badMaskData = ((LongDataset)summedBadMask).getData();
				long badValue = badNumber.longValue();
				long notMeasuredValue = notMeasuredNumber.longValue();
				int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					if( badMaskData[ i ] > 0 )
						currentData[ i ] = badValue;
					else if( notMeasuredMaskData[ i ] > 0 )
						currentData[ i ] = notMeasuredValue;
					else
						currentData[ i ] = setData[ i ];
				}
				break;
			}
			case AbstractDataset.FLOAT32: {
				float[] currentData = ((FloatDataset)resultSet).getData();
				float[] setData = ((FloatDataset)summedSet).getData();
				float[] notMeasuredMaskData = ((FloatDataset)summedNotMeasuredMask).getData();
				float[] badMaskData = ((FloatDataset)summedBadMask).getData();
				float badValue = badNumber.floatValue();
				float notMeasuredValue = notMeasuredNumber.floatValue();
				int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					if( badMaskData[ i ] > 0 )
						currentData[ i ] = badValue;
					else if( notMeasuredMaskData[ i ] > 0 )
						currentData[ i ] = notMeasuredValue;
					else
						currentData[ i ] = setData[ i ];
				}
				break;
			}
			case AbstractDataset.FLOAT64: {
				double[] currentData = ((DoubleDataset)resultSet).getData();
				double[] setData = ((DoubleDataset)summedSet).getData();
				double[] notMeasuredMaskData = ((DoubleDataset)summedNotMeasuredMask).getData();
				double[] badMaskData = ((DoubleDataset)summedBadMask).getData();
				double badValue = badNumber.doubleValue();
				double notMeasuredValue = notMeasuredNumber.doubleValue();
				int iSup = currentData.length;
				for( int i = 0; i < iSup; i++ ) {
					if( badMaskData[ i ] > 0 )
						currentData[ i ] = badValue;
					else if( notMeasuredMaskData[ i ] > 0 )
						currentData[ i ] = notMeasuredValue;
					else
						currentData[ i ] = setData[ i ];
				}
				break;
			}
			default:
				throw new RuntimeException("Not supported dataset type: " + resultSet.getDtype() );
		}
	}

	protected void addSplitImage(AbstractDataset set, Number maxValidNumber, Number notMeasuredValue) {
		if( loadedFileAndMetadatas.size() == 1 && !layeredImageMode) {
			summedSet = AbstractDataset.zeros(resultSet);
			summedNotMeasuredMask = AbstractDataset.zeros(resultSet);
			summedBadMask = AbstractDataset.zeros(resultSet);
			addSplitImageInternal(resultSet, maxValidNumber, notMeasuredValue); //TODO save threshold into set, because here we use threshold of 2nd image on 1st image, not the best
			layeredImageMode = true;
		}
		addSplitImageInternal(set, maxValidNumber, notMeasuredValue);
	}

	protected void removeSplitImage(AbstractDataset set, Number maxValidNumber, Number notMeasuredValue) {
		removeSplitImageInternal(set, maxValidNumber, notMeasuredValue);
	}

	protected void add(AbstractDataset set, FileWithTag imageFile, Number maxValidNumber, Number notMeasuredValue) {
		IMetaData metadata = set.getMetadata();
		FileAndMetadata fAM = new FileAndMetadata(imageFile, metadata);
		int localIndex = 0;
		if( loadedFileAndMetadatas.size() == 0 ) {
			resultSet = set;
			fAM.setMetadata(metadata != null ? metadata.clone() : null);
		} else {
			IMetaData resultMetadata = resultSet.getMetadata();
			if( (resultMetadata == null && metadata != null) || (resultMetadata != null && metadata == null)
					|| (resultMetadata != null && metadata != null && resultMetadata.getClass() != metadata.getClass() ) )
				throw new RuntimeException("Metadata type of first loaded file differs from metadata type of this file: " + imageFile.getAbsolutePath() );
			localIndex = Collections.binarySearch(loadedFileAndMetadatas, fAM, fileAndMetaIndexComparator);
			if( localIndex < 0 )
				localIndex = -(localIndex + 1);
			addSplitImage(set, maxValidNumber, notMeasuredValue);
		}
		loadedFileAndMetadatas.add( localIndex, fAM );
		resultSetNeedsUpdate = true;
	}

	protected void remove(AbstractDataset set, FileWithTag imageFile, Number maxValidNumber, Number notMeasuredValue) {
		IMetaData metadata = set.getMetadata();
		FileAndMetadata fAM = new FileAndMetadata(imageFile, metadata);
		int localIndex = Collections.binarySearch(loadedFileAndMetadatas, fAM, fileAndMetaIndexComparator);
		if( localIndex < 0 )
			throw new RuntimeException("Can not remove the file from this container because could not found it: " + imageFile.getAbsolutePath());
		FileAndMetadata qwe = loadedFileAndMetadatas.remove( localIndex );
		if( loadedFileAndMetadatas.size() == 0 ) {
			clearLoaded();
		} else {
			removeSplitImage(set, maxValidNumber, notMeasuredValue);
			resultSetNeedsUpdate = true;
		}
	}

	protected AbstractDataset loadFileInternal(final FileWithTag imageFile) throws IOException {
		final String filePath = imageFile.getAbsolutePathWithoutProtocol();
		try {
			ILoaderService service = (ILoaderService)ServiceManager.getService(ILoaderService.class);
			AbstractDataset set = service.getDataset(filePath);
			if( set == null )
				throw new IOException("The loader returned null dataset for file: " + imageFile.getAbsolutePath()); //should the loader throw an exception?
			return set;
		} catch (Throwable e) {
			throw new IOException("Can not load the file: " + imageFile.getAbsolutePath() + " because: " + e.getMessage());
		}
	}

	protected void loadAndAddFile(FileWithTag imageFile) throws IOException {
		AbstractDataset set = loadFileInternal(imageFile).clone();
		Number maxValidNumber = DatasetNumber.getMaxValidNumber(set, true);
		add( set, imageFile, maxValidNumber, DatasetNumber.getNumber(set, NOT_MEASURED_VALUE, true) ); //TODO little bit hardcoded: CBF images typically have cutoff and notMeasuredValue (-1), but what about others?
	}

	protected void loadAndRemoveFile(FileWithTag imageFile) throws IOException {
		AbstractDataset set = loadFileInternal(imageFile).clone();
		Number maxValidNumber = DatasetNumber.getMaxValidNumber(set, true);
		remove( set, imageFile, maxValidNumber, DatasetNumber.getNumber(set, NOT_MEASURED_VALUE, true) ); //TODO little bit hardcoded: CBF images typically have cutoff and notMeasuredValue (-1), but what about others?
	}

	public boolean isLoading() {
		return imageLoaderManager != null && imageLoaderManager.isAlive();
	}

	public void interrupt() {
		imageLoaderManager.interrupt();
	}

	/**
	 * 
	 * @param from Index of first file in allImageFiles
	 * @param amount the amount of files in allImageFiles
	 */
	public void loadFiles(final int from, final int amount) {
//		FileWithTag[] toLoadImageFiles = null;
//		synchronized (resultDataset) {
//			int iSup = imageFilesWindowWidth;
//			int firstIndex = pos - 1;
//			toLoadImageFiles = new FileWithTag[iSup];
//			for( int i = 0; i < iSup; i++ )
//				toLoadImageFiles[ i ] = resultDataset.getAllImageFilesElement( firstIndex + i );
//		}
		final TrackableJob job = new TrackableJob(imageLoaderManager, "Read image data") {
//			Vector<FileWithTag> toLoadImageFilesJob = new Vector<FileWithTag>( Arrays.asList(toLoadImageFiles) );
			FileLoader fileLoader = FileLoader.this;

//			public IStatus processImage(FileWithTag imageFile, boolean add) {
//				AbstractDataset set = null;
//				IMetaData localMetaData = null;
//				boolean loadedAtLeast2;
////				synchronized (resultDataset) {
//					if( isAborting() )
//						return Status.CANCEL_STATUS;
//					loadedAtLeast2 = loadedMetaAndFiles.size() > 1;
////				}
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
////				synchronized (resultDataset) {
//					if( isAborting() )
//						return Status.CANCEL_STATUS;
//					if( add ) {
//						add( set, imageFile, cutoff, BAD_PIXEL_VALUE );
//					} else {
//						remove( set, imageFile, cutoff, BAD_PIXEL_VALUE );
//					}
//					//Warning: getMergedDataset updates the dataset, which slows down execution if batch amount is big
////					logger.debug("HRMM, after  [add=" + add + "], rD[ind]=" + resultDataset.getMergedDataset().getElementLongAbs(ind) + ", s[ind]=" + set.getElementLongAbs(ind));
////				}
//				return Status.OK_STATUS;
//			}

			public IStatus runThis(IProgressMonitor monitor) {
				/* Since running this and others as well through imageLoaderManager,
				 * the single thread of this loading is guaranteed.
				 */
				IStatus result = Status.CANCEL_STATUS;
				//We have to load files into fileLoader with imageLoaderManager from from the amount of amount
				do {
					Vector<FileWithTag> adding = new Vector<FileWithTag>(0);
					Vector<FileWithTag> removing = new Vector<FileWithTag>(0);
//					synchronized (fileLoadingLock) {
						if( isAborting() )
							break;
						System.out.println("from=" + from + ", amount=" + amount + ", adding=" + adding + ", removing=" + removing);
						getFilesToAddAndRemove(from, amount, adding, removing);
						if( isAborting() ) //Better check ASAP if must abort here, because if yes, the adding and removing are not valid
							break;
						if( removing.isEmpty() && adding.isEmpty() ) //This means nothing to add or remove, then return without doing anything
							return Status.OK_STATUS;
//					}
					result = Status.OK_STATUS;
					for( FileWithTag i : adding ) {
						if( isAborting() )
							break;
//						logger.debug("EHM, adding " + ((AbstractDatasetAndFileDescriptor)i.getTag()).getIndexInCollection() + ". file");
//						result = processImage(i, true);
						try {
							loadAndAddFile(i);
						} catch (Exception e) { //IOException, RuntimeException
							result = Status.CANCEL_STATUS;
							logger.error("Can not load the file: " + i.toString(), e);
							break;
						}
					}
					if( result != Status.OK_STATUS || isAborting() )
						break;
					for( FileWithTag i : removing ) {
						if( isAborting() )
							break;
//						logger.debug("EHM, removing " + ((AbstractDatasetAndFileDescriptor)i.getTag()).getIndexInCollection() + ". file");
//						result = processImage(i, false);
						try {
							loadAndRemoveFile(i);
						} catch (Exception e) { //IOException, RuntimeException
							logger.error("Can not load the file: " + i.toString(), e);
							result = Status.CANCEL_STATUS;
							break;
						}
					}
					if( result != Status.OK_STATUS || isAborting() )
						break;
					result = Status.OK_STATUS;
				} while( false );
				if( isAborting() ) {
					setAborted();
					return Status.CANCEL_STATUS;
				}
				if( result == Status.OK_STATUS )
					fireFileIsReady(monitor);
				//TODO else fire error
				return result;
			}
		};
		job.setUser(false);
		job.setPriority(Job.BUILD);
		imageLoaderManager = ExecutableManager.setRequest(job);
	}

	public void loadFile() {
		loadFiles(vcFile.getAllLength() - 1, 1);
//		loadFiles(getIndexFromPathFromAll(filesOrigin), 1);
	}

	public synchronized void updateMetadata() {
		if( resultSetNeedsUpdate ) {
			IMetaData metadata = resultSet.getMetadata();
			if( metadata instanceof IDiffractionMetadata ) { //This (and loader's algorithm) guarantees that each file has this kind of metadata
				IDiffractionMetadata mergedDiffractionMetaData = (IDiffractionMetadata)metadata;
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
	//			DetectorProperties mergedDetConfig = mergedDiffractionMetaData.getDetector2DProperties();
				DiffractionCrystalEnvironment resultDCE = mergedDiffractionMetaData.getDiffractionCrystalEnvironment();
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
				mixSplitted(DatasetNumber.getNumber(resultSet, BAD_PIXEL_VALUE, true), DatasetNumber.getNumber(resultSet, NOT_MEASURED_VALUE, true));
			}
			resultSetNeedsUpdate = false;
		}
	}

	/**
	 * Returns an AbstractDataset containing all the loaded files merged.
	 * The resulting AbstractDataset is affected by subsequent adds or removes, i.e. loading other files.
	 * <p>
	 * Note: Callers of this method <b>must not</b> modify the returned array. 
	 *
	 * @return the merged dataset
	 */
	public synchronized AbstractDataset getMergedDataset() {
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

	public void fireFileIsReady(IProgressMonitor monitor) {
		for( IFileLoaderListener listener : fileLoaderListeners )
			listener.fileIsReady(this, monitor);
	}

}
