package org.embl.cca.utils.datahandling.file;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.embl.cca.utils.datahandling.AbstractDatasetAndFileDescriptor;
import org.embl.cca.utils.datahandling.EFile;
import org.embl.cca.utils.datahandling.FileWithTag;
import org.embl.cca.utils.datahandling.FilenameCaseInsensitiveComparator;
import org.embl.cca.utils.datahandling.PatternFileFilter;

public class VirtualCollectionFile extends FileWithTag implements ICollectionFile {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5975706930930502307L;

	public final static String VirtualCollectionFileProtocolID = "vcf";

	protected long totalLength = 0; // Sum of length of files in allImageFiles
	protected long lastModified = 0; // Last modified (date) value of last file
										// in allImageFiles
	protected String filesOrigin = null; //The file path which produced the filename template
	protected String filenameTemplate = null; //The filename template produced by filesOrigin. Valid when filesOrigin != null
	protected Pattern filenameTemplatePattern = null; //The compiled template. Valid when filesOrigin != null
	protected Vector<FileWithTag> allFiles = null; //All image files with names matching the filename template. Valid when !filenameTemplate.isEmpty()
	protected String filePathTemplate = null; //Valid when filenameTemplate != null
	/**
	 * Override this method if this class works with a different file protocol
	 * than its parent class.
	 * @return the accepted protocol by this kind of File
	 */
	public static String getAcceptedProtocol() {
		return VirtualCollectionFileProtocolID;
	}

	// Must override this, because the created file must not be
	// VirtualCollectionFile if it is directory,
	// or reversed: if a file is VirtualCollectionFile, then the created file
	// must be same.
	@Override
	protected EFile create(String pathname) {
		EFile result = super.create(FileWithTag.class, pathname);
		if (!result.isDirectory())
			result = super.create(this.getClass(), pathname);
		return result;
	}

	// Must override this, because the created file must not be
	// VirtualCollectionFile if it is directory,
	// or reversed: if a file is VirtualCollectionFile, then the created file
	// must be same.
	@Override
	protected EFile create(EFile file, String pathname) {
		EFile result = super.create(FileWithTag.class, file, pathname);
		if (!result.isDirectory())
			result = super.create(this.getClass(), file, pathname);
		return result;
	}

	/* -- Constructor supporters -- */

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

	protected boolean isDifferentFiles(FileWithTag[] otherImageFiles ) {
		return allFiles == null || allFiles.equals(otherImageFiles);
	}

	/**
	 * Check and refresh new files, replacing the already processed files.
	 * @return true if the set of files differs from previously determined set.
	 */
	@Override
	public synchronized boolean refreshAllFiles() {
		FileWithTag[] resultArray = null;
		String parentFolder = getParent();//getFilesOriginWithoutProtocol().removeLastSegments(1);
		resultArray = listFiles(new FileWithTag(parentFolder), new PatternFileFilter(filenameTemplatePattern) );
		if( resultArray == null )
			resultArray = new FileWithTag[0];
		Arrays.sort( resultArray, new FilenameCaseInsensitiveComparator() );
		long currentTotalLength = 0;
		int iSup = resultArray.length;
		for( int i = 0; i < iSup; i++ )
			currentTotalLength += resultArray[ i ].length();
		long currentLastModified = iSup > 0 ? resultArray[ iSup - 1 ].lastModified() : 0;
		if( currentLastModified == lastModified && currentTotalLength == totalLength && !isDifferentFiles(resultArray) )
			return false;
		for( int i = 0; i < iSup; i++ )
			resultArray[ i ].setTag(new AbstractDatasetAndFileDescriptor(filenameTemplate, getIndexFromPathByTemplate(resultArray[ i ]), currentTotalLength, currentLastModified));
		Vector<FileWithTag> result = new Vector<FileWithTag>(Arrays.asList(resultArray));
		totalLength = currentTotalLength;
		lastModified = currentLastModified;
		allFiles = result;
		return true;
	}

	/**
	 * Check and refresh only new files, not touching the already processed files (thus their
	 * file descriptor remains the same, even if should be updated).
	 * This way the loading of files based on already processed files is not disturbed at all.
	 * @return true if some new files were added.
	 */
	@Override
	public synchronized boolean refreshNewAllFiles() {
		FileWithTag[] resultArray = null;
		String parentFolder = getParent();//getFilesOriginWithoutProtocol().removeLastSegments(1);
		resultArray = listFiles(new FileWithTag(parentFolder), new PatternFileFilter(filenameTemplatePattern) );
		if( resultArray == null )
			throw new RuntimeException("The parent folder is not available: " + parentFolder);
		Arrays.sort( resultArray, new FilenameCaseInsensitiveComparator() );
		Vector<FileWithTag> result = new Vector<FileWithTag>(Arrays.asList(resultArray));
		if( allFiles != null )
			result.removeAll(allFiles);
		int iSup = result.size();
		if( iSup == 0 ) //If there is no new file, nothing to do
			return false;
		//Considering the old files into calculations, though their file descriptor will not be updated here 
		long currentTotalLength = totalLength;
		for( int i = 0; i < iSup; i++ )
			currentTotalLength += result.get(i).length();
		long currentLastModified = result.get( iSup - 1 ).lastModified();
		//Here we know that result has at least one element, allFiles might be empty
		for( int i = 0; i < iSup; i++ )
			result.get( i ).setTag(new AbstractDatasetAndFileDescriptor(filenameTemplate, getIndexFromPathByTemplate(result.get( i )), currentTotalLength, currentLastModified));
		totalLength = currentTotalLength;
		lastModified = currentLastModified;
		if( allFiles != null )
			allFiles.addAll(result);
		else
			allFiles = result;
		return true;
	}

	protected void completeConstructing() {
		filesOrigin = getAbsolutePath();
		if( VirtualCollectionFile.VirtualCollectionFileProtocolID.equals(getProtocol()) ) {
			filenameTemplate = getName();
			filenameTemplatePattern = Pattern.compile(filenameTemplate);
			filePathTemplate = filesOrigin; 
		} else {
			filenameTemplate = getDatasetFilePathTemplate();
			if( filenameTemplate == null )
				throw new RuntimeException("The processed version of specified filename is not recognised with collection template: " + filesOrigin + " does not match " + getFilenameTemplate()[0]);
			protocolID = VirtualCollectionFileProtocolID;
			filenameTemplatePattern = Pattern.compile(filenameTemplate);
			filePathTemplate = getParentWithProtocol() + EFile.separator + filenameTemplate; 
		}
		refreshAllFiles();
	}

	public boolean acceptPath(String pathname) {
		return acceptProtocol(this.getClass(), getProtocolFromPath(pathname)) || getDatasetFilePathTemplate() != null;
	}

	/* -- Constructors -- */

//	public VirtualCollectionFile(File file) {
//		super(file);
//	}
//
//	public VirtualCollectionFile(EFile file) {
//		this((File) file);
//	}
//
//	public VirtualCollectionFile(FileWithTag file) {
//		super(file);
//	}

	public VirtualCollectionFile(VirtualCollectionFile file) {
//		this((FileWithTag) file);
		super(file);
		completeConstructing();
	}

	/**
	 * Creates a new <code>VirtualCollectionFile</code> instance by converting
	 * the given pathname string into an abstract pathname. If the given string
	 * is the empty string, then the result is the empty abstract pathname.
	 * 
	 * @param pathname
	 *            A pathname string
	 * @throws NullPointerException
	 *             If the <code>pathname</code> argument is <code>null</code>
	 */
	public VirtualCollectionFile(String pathname) {
		super(pathname);
		if( !acceptPath(pathname) )
			throw new IllegalArgumentException("This class (" + getClass().getName() + ") does not accept the protocol of specified path: " + pathname);
		completeConstructing();
	}

	/**
	 * Creates a new <code>VirtualCollectionFile</code> instance by converting
	 * the given pathname string into an abstract pathname. If the given string
	 * is the empty string, then the result is the empty abstract pathname.
	 * 
	 * @param pathname
	 *            A pathname string
	 * @param dummy
	 *            Not used
	 * @param tag
	 *            Caller defined object
	 * @throws NullPointerException
	 *             If the <code>pathname</code> argument is <code>null</code>
	 */
	public VirtualCollectionFile(String pathname, boolean dummy, Object tag) {
		super(pathname, dummy, tag);
		if( !acceptPath(this.getClass(), pathname) )
			throw new IllegalArgumentException("This class (" + getClass().getName() + ") does not accept the protocol of specified path: " + pathname);
		completeConstructing();
	}

	/*
	 * Note: The two-argument VirtualCollectionFile constructors do not
	 * interpret an empty parent abstract pathname as the current user
	 * directory. An empty parent instead causes the child to be resolved
	 * against the system-dependent directory defined by the
	 * FileSystem.getDefaultParent method. On Unix this default is "/", while on
	 * Microsoft Windows it is "\\". This is required for compatibility with the
	 * original behavior of this class.
	 */

	/**
	 * Creates a new <code>VirtualCollectionFile</code> instance from a parent
	 * pathname string and a child pathname string.
	 * 
	 * <p>
	 * If <code>parent</code> is <code>null</code> then the new
	 * <code>VirtualCollectionFile</code> instance is created as if by invoking
	 * the single-argument <code>VirtualCollectionFile</code> constructor on the
	 * given <code>child</code> pathname string.
	 * 
	 * <p>
	 * Otherwise the <code>parent</code> pathname string is taken to denote a
	 * directory, and the <code>child</code> pathname string is taken to denote
	 * either a directory or a file. If the <code>child</code> pathname string
	 * is absolute then it is converted into a relative pathname in a
	 * system-dependent way. If <code>parent</code> is the empty string then the
	 * new <code>VirtualCollectionFile</code> instance is created by converting
	 * <code>child</code> into an abstract pathname and resolving the result
	 * against a system-dependent default directory. Otherwise each pathname
	 * string is converted into an abstract pathname and the child abstract
	 * pathname is resolved against the parent.
	 * 
	 * @param parent
	 *            The parent pathname string
	 * @param child
	 *            The child pathname string
	 * @throws NullPointerException
	 *             If <code>child</code> is <code>null</code>
	 */
	public VirtualCollectionFile(String parent, String child) {
		super(parent, child);
		if( !acceptPath(this.getClass(), child) )
			throw new IllegalArgumentException("This class (" + getClass().getName() + ") does not accept the protocol of specified path: " + child);
		completeConstructing();
	}

	/**
	 * Creates a new <code>VirtualCollectionFile</code> instance from a parent
	 * pathname string and a child pathname string.
	 * 
	 * <p>
	 * If <code>parent</code> is <code>null</code> then the new
	 * <code>VirtualCollectionFile</code> instance is created as if by invoking
	 * the single-argument <code>VirtualCollectionFile</code> constructor on the
	 * given <code>child</code> pathname string.
	 * 
	 * <p>
	 * Otherwise the <code>parent</code> pathname string is taken to denote a
	 * directory, and the <code>child</code> pathname string is taken to denote
	 * either a directory or a file. If the <code>child</code> pathname string
	 * is absolute then it is converted into a relative pathname in a
	 * system-dependent way. If <code>parent</code> is the empty string then the
	 * new <code>VirtualCollectionFile</code> instance is created by converting
	 * <code>child</code> into an abstract pathname and resolving the result
	 * against a system-dependent default directory. Otherwise each pathname
	 * string is converted into an abstract pathname and the child abstract
	 * pathname is resolved against the parent.
	 * 
	 * @param parent
	 *            The parent pathname string
	 * @param child
	 *            The child pathname string
	 * @param tag
	 *            Caller defined object
	 * @throws NullPointerException
	 *             If <code>child</code> is <code>null</code>
	 */
	public VirtualCollectionFile(String parent, String child, Object tag) {
		super(parent, child, tag);
		if( !acceptPath(this.getClass(), child) )
			throw new IllegalArgumentException("This class (" + getClass().getName() + ") does not accept the protocol of specified path: " + child);
		completeConstructing();
	}

	/**
	 * Creates a new <code>VirtualCollectionFile</code> instance from a parent
	 * abstract pathname and a child pathname string.
	 * 
	 * <p>
	 * If <code>parent</code> is <code>null</code> then the new
	 * <code>VirtualCollectionFile</code> instance is created as if by invoking
	 * the single-argument <code>VirtualCollectionFile</code> constructor on the
	 * given <code>child</code> pathname string.
	 * 
	 * <p>
	 * Otherwise the <code>parent</code> abstract pathname is taken to denote a
	 * directory, and the <code>child</code> pathname string is taken to denote
	 * either a directory or a file. If the <code>child</code> pathname string
	 * is absolute then it is converted into a relative pathname in a
	 * system-dependent way. If <code>parent</code> is the empty abstract
	 * pathname then the new <code>VirtualCollectionFile</code> instance is
	 * created by converting <code>child</code> into an abstract pathname and
	 * resolving the result against a system-dependent default directory.
	 * Otherwise each pathname string is converted into an abstract pathname and
	 * the child abstract pathname is resolved against the parent.
	 * 
	 * @param parent
	 *            The parent abstract pathname
	 * @param child
	 *            The child pathname string
	 * @throws NullPointerException
	 *             If <code>child</code> is <code>null</code>
	 */
	public VirtualCollectionFile(VirtualCollectionFile parent, String child) {
		super(parent, child);
		if( !acceptPath(this.getClass(), child) )
			throw new IllegalArgumentException("This class (" + getClass().getName() + ") does not accept the protocol of specified path: " + child);
		completeConstructing();
	}

	/**
	 * Creates a new <code>VirtualCollectionFile</code> instance from a parent
	 * abstract pathname and a child pathname string.
	 * 
	 * <p>
	 * If <code>parent</code> is <code>null</code> then the new
	 * <code>VirtualCollectionFile</code> instance is created as if by invoking
	 * the single-argument <code>VirtualCollectionFile</code> constructor on the
	 * given <code>child</code> pathname string.
	 * 
	 * <p>
	 * Otherwise the <code>parent</code> abstract pathname is taken to denote a
	 * directory, and the <code>child</code> pathname string is taken to denote
	 * either a directory or a file. If the <code>child</code> pathname string
	 * is absolute then it is converted into a relative pathname in a
	 * system-dependent way. If <code>parent</code> is the empty abstract
	 * pathname then the new <code>VirtualCollectionFile</code> instance is
	 * created by converting <code>child</code> into an abstract pathname and
	 * resolving the result against a system-dependent default directory.
	 * Otherwise each pathname string is converted into an abstract pathname and
	 * the child abstract pathname is resolved against the parent.
	 * 
	 * @param parent
	 *            The parent abstract pathname
	 * @param child
	 *            The child pathname string
	 * @param tag
	 *            Caller defined object
	 * @throws NullPointerException
	 *             If <code>child</code> is <code>null</code>
	 */
	public VirtualCollectionFile(VirtualCollectionFile parent, String child,
			Object tag) {
		super(parent, child, tag);
		if( !acceptPath(this.getClass(), child) )
			throw new IllegalArgumentException("This class (" + getClass().getName() + ") does not accept the protocol of specified path: " + child);
		completeConstructing();
	}

	/**
	 * Creates a new <tt>VirtualCollectionFile</tt> instance by converting the
	 * given <tt>file:</tt> URI into an abstract pathname.
	 * 
	 * <p>
	 * The exact form of a <tt>file:</tt> URI is system-dependent, hence the
	 * transformation performed by this constructor is also system-dependent.
	 * 
	 * <p>
	 * For a given abstract pathname <i>f</i> it is guaranteed that
	 * 
	 * <blockquote><tt>
	 * new VirtualCollectionFile(</tt><i>&nbsp;f</i>
	 * <tt>.{@link #toURI() toURI}()).equals(</tt><i>&nbsp;f</i>
	 * <tt>.{@link #getAbsoluteFile() getAbsoluteFile}())
	 * </tt></blockquote>
	 * 
	 * so long as the original abstract pathname, the URI, and the new abstract
	 * pathname are all created in (possibly different invocations of) the same
	 * Java virtual machine. This relationship typically does not hold, however,
	 * when a <tt>file:</tt> URI that is created in a virtual machine on one
	 * operating system is converted into an abstract pathname in a virtual
	 * machine on a different operating system.
	 * 
	 * @param uri
	 *            An absolute, hierarchical URI with a scheme equal to
	 *            <tt>"file"</tt>, a non-empty path component, and undefined
	 *            authority, query, and fragment components
	 * 
	 * @throws NullPointerException
	 *             If <tt>uri</tt> is <tt>null</tt>
	 * 
	 * @throws IllegalArgumentException
	 *             If the preconditions on the parameter do not hold
	 * 
	 * @see #toURI()
	 * @see java.net.URI
	 * @since 1.4
	 */
	public VirtualCollectionFile(URI uri) {
		super(uri);
		if( !acceptPath(this.getClass(), uri.getPath()) ) //TODO Test this, and get protocolID from URI, maybe
			throw new IllegalArgumentException("This class (" + getClass().getName() + ") does not accept the protocol of specified path: " + uri.getPath());
		completeConstructing();
	}

	/**
	 * Creates a new <tt>VirtualCollectionFile</tt> instance by converting the
	 * given <tt>file:</tt> URI into an abstract pathname.
	 * 
	 * <p>
	 * The exact form of a <tt>file:</tt> URI is system-dependent, hence the
	 * transformation performed by this constructor is also system-dependent.
	 * 
	 * <p>
	 * For a given abstract pathname <i>f</i> it is guaranteed that
	 * 
	 * <blockquote><tt>
	 * new VirtualCollectionFile(</tt><i>&nbsp;f</i>
	 * <tt>.{@link #toURI() toURI}()).equals(</tt><i>&nbsp;f</i>
	 * <tt>.{@link #getAbsoluteFile() getAbsoluteFile}())
	 * </tt></blockquote>
	 * 
	 * so long as the original abstract pathname, the URI, and the new abstract
	 * pathname are all created in (possibly different invocations of) the same
	 * Java virtual machine. This relationship typically does not hold, however,
	 * when a <tt>file:</tt> URI that is created in a virtual machine on one
	 * operating system is converted into an abstract pathname in a virtual
	 * machine on a different operating system.
	 * 
	 * @param uri
	 *            An absolute, hierarchical URI with a scheme equal to
	 *            <tt>"file"</tt>, a non-empty path component, and undefined
	 *            authority, query, and fragment components
	 * @param tag
	 *            Caller defined object
	 * 
	 * @throws NullPointerException
	 *             If <tt>uri</tt> is <tt>null</tt>
	 * 
	 * @throws IllegalArgumentException
	 *             If the preconditions on the parameter do not hold
	 * 
	 * @see #toURI()
	 * @see java.net.URI
	 * @since 1.4
	 */
	public VirtualCollectionFile(URI uri, Object tag) {
		super(uri, tag);
		if( !acceptPath(this.getClass(), uri.getPath()) ) //TODO Test this, and get protocolID from URI, maybe
			throw new IllegalArgumentException("This class (" + getClass().getName() + ") does not accept the protocol of specified path: " + uri.getPath());
		completeConstructing();
	}

	@Override
	public VirtualCollectionFile[] listFiles() {
		return null;
	}

	@Override
	public VirtualCollectionFile[] listFiles(FilenameFilter filter) {
		return null;
	}

	@Override
	public VirtualCollectionFile[] listFiles(FileFilter filter) {
		return null;
	}

	@Override
	public boolean canRead() {
		return true; // TODO maybe add canRead(int index) method
	}

	@Override
	public boolean canWrite() {
		return false; // TODO maybe add canWrite(int index) method
	}

	@Override
	public boolean exists() {
		return !allFiles.isEmpty();
	}

	@Override
	public boolean isDirectory() {
		return false;
	}

	@Override
	public boolean isFile() {
		return true;
	}

	@Override
	public boolean isHidden() {
		return false; // TODO maybe add isHidden(int index) method
	}

	@Override
	public long lastModified() {
		return lastModified; // TODO maybe add lastModified(int index) method
	}

	@Override
	public long length() {
		return totalLength; // TODO maybe add length(int index) method
	}

	/* -- File operations -- */

	@Override
	public boolean createNewFile() throws IOException {
		throw new SecurityException("Creating this file is not supported");
	}

	@Override
	public boolean delete() {
		throw new SecurityException("Deleting this file is not supported");
	}

	@Override
	public void deleteOnExit() {
		throw new SecurityException(
				"Deleting this file on exit is not supported");
	}

	@Override
	public String[] list() {
		return null;
	}

	@Override
	public boolean mkdir() {
		return false;
	}

	@Override
	public boolean mkdirs() {
		return false;
	}

	@Override
	public boolean renameTo(File dest) {
		throw new SecurityException("Renaming this file is not supported");
	}

	@Override
	public boolean setLastModified(long time) {
		this.lastModified = time;
		return true;
	}

	public boolean setLength(long length) {
		this.totalLength = length;
		return true;
	}

	@Override
	public boolean setReadOnly() {
		throw new SecurityException(
				"Setting this file readonly is not supported");
	}

	@Override
	public boolean setWritable(boolean writable, boolean ownerOnly) {
		throw new SecurityException(
				"Setting this file writable is not supported");
	}

	@Override
	public boolean setReadable(boolean readable, boolean ownerOnly) {
		throw new SecurityException(
				"Setting this file readable is not supported");
	}

	@Override
	public boolean setExecutable(boolean executable, boolean ownerOnly) {
		throw new SecurityException(
				"Setting this file executable is not supported");
	}

	@Override
	public boolean canExecute() {
		throw new SecurityException(
				"Getting this file executable is not supported");
	}

//-------------------------------

	/**
	 * Returns human readable name of collection
	 * @return the name of collection
	 */
	@Override
	public synchronized String getCollectionName() {
		return filenameTemplate;
	}

	public synchronized Integer getFirstIndex() {
		return allFiles.size() > 0 ? getIndexFromPathByTemplate(allFiles.get(0)) : null;
	}

//	public boolean setFirstIndex(Integer index) {
//		this.firstIndex = index;
//		return true;
//	}

	public synchronized Integer getLastIndex() {
		return allFiles.size() > 1 ? getIndexFromPathByTemplate(allFiles.get(allFiles.size() - 1)) : getFirstIndex();
	}

//	public boolean setLastIndex(Integer index) {
//		this.lastIndex = index;
//		return true;
//	}

	public synchronized void setFilenameTemplatePattern(Pattern filenameTemplatePattern) {
		this.filenameTemplatePattern = filenameTemplatePattern;
	}

	public synchronized void setAllFiles(Vector<FileWithTag> allFiles) {
		this.allFiles = allFiles;
	}

	/**
	 * Return the number of files in the collection
	 * @return number of files in the collection
	 */
	@Override
	public synchronized int getAllLength() {
		return allFiles.size();
	}

	public synchronized FileWithTag getFileOfAll(int i) {
		if( allFiles == null )
			throw new NoSuchElementException("The container is null, thus can not get " + i + ". element");
		return allFiles.get(i);
	}

	public synchronized FileWithTag getFirstFileOfAll() {
		if( allFiles == null )
			throw new NoSuchElementException("The container is null, thus can not get first element");
		return allFiles.firstElement();
	}

	public synchronized FileWithTag getLastFileOfAll() {
		if( allFiles == null )
			throw new NoSuchElementException("The container is null, thus can not get last element");
		return allFiles.lastElement();
	}

	/**
	 * Returns the Vector containing the files of collection.
	 * The resulting Vector is affected by subsequent refreshes.
	 * <p>
	 * Note: Callers of this method <b>must not</b> modify the elements of returned Vector. 
	 *
	 * @return the Vector containing the files of collection.
	 */
	@Override
	public synchronized Vector<FileWithTag> getFilesFromAll() {
		return allFiles;
	}

	/**
	 * Returns the Vector containing the files in the specified range of collection.
	 * The resulting Vector is affected by subsequent refreshes.
	 * <p>
	 * Note: Callers of this method <b>must not</b> modify the returned Vector. 
	 *
	 * @return the Vector containing the files in the specified range of collection.
	 */
	@Override
	public synchronized Vector<FileWithTag> getFilesFromAll(int from, int amount) {
		Vector<FileWithTag> result = new Vector<FileWithTag>( amount );
		int iSup = amount;
		for( int i = 0; i < iSup; i++ )
			result.add(getFileOfAll( from + i ));
		return result;
	}

	/**
	 * 
	 * @return String array[3], where
	 * array[0] is the string of generic template,
	 * array[1] is the string to capture the indifferent part of filename,
	 * array[2] is the string to capture the different part of filename,
	 * array[3] is the string of file specific template.
	 */
	public String[] getFilenameTemplate() {
		return new String[] { "^(.+?)([0-9]+)(\\..*)$", //Characters reluctantly then the digits greedily and finally a dot and the rest (for example extension)
				"$1",
				"$2",
				"$1([0-9]+)$3" //This must contain exactly one group (within '(' and ')'), which matches the different part of filename
		};
	}

	protected synchronized String getDatasetFilePathTemplate() {
		String[] template = getFilenameTemplate();
		Matcher match = Pattern.compile(template[0]).matcher(getName());
		if( !match.find() )
			return null;
		return match.replaceAll(template[3]);
	}

	protected synchronized int getIndexFromPathByTemplate( FileWithTag filePath ) {
		Matcher match = filenameTemplatePattern.matcher(filePath.getName());
		if( !match.find() )
			throw new RuntimeException("Could not match the file path by its template: " + filenameTemplatePattern);
		return Integer.valueOf(match.group(1)); //Why 1? See getFilenameTemplate() of FileLoader
	}

//	protected synchronized int getIndexFromPathByTemplate( FileWithTag filePath, int defaultIndex ) {
//		try {
//			return getIndexFromPathByTemplate( filePath );
//		} catch(IndexOutOfBoundsException e) {
//			return defaultIndex;
//		}
//	}
//
	/**
	 * 
	 * @param filePath
	 * @return index of file in all files. If index is less than 0, then file is not found. Anyway it would be at -(index+1) position considering the ordering.
	 */
	@Override
	public synchronized int getIndexOfFile(String filePath) {
//		int localIndex2 = Collections.binarySearch(allImageFiles, new FileWithTag(filePath), new Comparator<FileWithTag>() {
//			@Override
//			public int compare(FileWithTag o1, FileWithTag o2) {
//				return ((AbstractDatasetAndFileDescriptor)o1.getTag()).getIndexInCollection() - ((AbstractDatasetAndFileDescriptor)o2.getTag()).getIndexInCollection();
//			}
//		});
		int localIndex = Collections.binarySearch(allFiles, new FileWithTag(filePath), new Comparator<FileWithTag>() {
			@Override
			public int compare(FileWithTag o1, FileWithTag o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		//If the filePath is not found, because it is the template name itself, we return index of last file
		if( localIndex < 0 && filePath.equals(filePathTemplate))
			localIndex = allFiles.size() - 1;
		return localIndex;
		
	}

//	/**
//	 * 
//	 * @param filePath
//	 * @return index of file determined from its path. If index is -1, then file is not found.
//	 */
//	public synchronized int getIndexFromPathFromAll(IPath filePath) {
//		int index = getIndexOfFile(filePath);
//		if( index < 0 )
//			return -1;
//		return ((AbstractDatasetAndFileDescriptor)allFiles.get(index).getTag()).getIndexInCollection();
//	}

}
