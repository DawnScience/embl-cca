package org.embl.cca.utils.datahandling.file;

import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import org.embl.cca.utils.datahandling.AbstractDatasetAndFileDescriptor;
import org.embl.cca.utils.datahandling.EFile;
import org.embl.cca.utils.datahandling.FileWithTag;

public class EmulatedCollectionFile extends FileWithTag implements ICollectionFile {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1065543995066725876L;

	protected Vector<FileWithTag> allFiles = new Vector<FileWithTag>(0);

	// Must override this, because the created file must not be
	// EmulatedCollectionFile if it is directory,
	// or reversed: if a file is EmulatedCollectionFile, then the created file
	// must be same.
	@Override
	protected EFile create(String pathname) {
		EFile result = super.create(FileWithTag.class, pathname);
		if (!result.isDirectory())
			result = super.create(this.getClass(), pathname);
		return result;
	}

	// Must override this, because the created file must not be
	// EmulatedCollectionFile if it is directory,
	// or reversed: if a file is EmulatedCollectionFile, then the created file
	// must be same.
	@Override
	protected EFile create(EFile file, String pathname) {
		EFile result = super.create(FileWithTag.class, file, pathname);
		if (!result.isDirectory())
			result = super.create(this.getClass(), file, pathname);
		return result;
	}

	protected void completeConstructing() {
		FileWithTag file = new FileWithTag(this);
		long currentTotalLength = file.length();
		long currentLastModified = file.lastModified();
		file.setTag(new AbstractDatasetAndFileDescriptor(file.getName(), 0, currentTotalLength, currentLastModified));
		allFiles.add(file);
	}

	/* -- Constructors -- */

	public EmulatedCollectionFile(VirtualCollectionFile file) {
		super(file);
		completeConstructing();
	}

	public EmulatedCollectionFile(String pathname) {
		super(pathname);
		if( !acceptPath(this.getClass(), pathname) )
			throw new IllegalArgumentException("This class (" + getClass().getName() + ") does not accept the protocol of specified path: " + pathname);
		completeConstructing();
	}

	@Override
	public synchronized boolean refreshAllFiles() {
		return false;
	}

	@Override
	public synchronized boolean refreshNewAllFiles() {
		return false;
	}

	@Override
	public synchronized String getCollectionName() {
		return getName();
	}

	@Override
	public synchronized int getAllLength() {
		return allFiles.size();
	}

	@Override
	public synchronized Vector<FileWithTag> getFilesFromAll() {
		return allFiles;
	}

	@Override
	public synchronized Vector<FileWithTag> getFilesFromAll(int from, int amount) {
		Vector<FileWithTag> result = new Vector<FileWithTag>( amount );
		int iSup = amount;
		for( int i = 0; i < iSup; i++ )
			result.add(allFiles.get( from + i ));
		return result;
	}

	@Override
	public synchronized int getIndexOfFile(String filePath) {
		int localIndex = Collections.binarySearch(allFiles, new FileWithTag(filePath), new Comparator<FileWithTag>() {
			@Override
			public int compare(FileWithTag o1, FileWithTag o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		return localIndex;
	}

}
