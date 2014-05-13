package org.embl.cca.utils.ui.widget;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.spi.ImageWriterSpi;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.embl.cca.utils.datahandling.file.WildCardFileFilter;
import org.embl.cca.utils.datahandling.text.StringUtils;
import org.embl.cca.utils.ui.widget.support.FileDialogFileNameFilter;

import uk.ac.diamond.scisoft.analysis.io.JavaImageSaver;

/**
 * Instances of this class allow the user to navigate
 * the file system and select or enter a file name.
 * <dl>
 * <dt><b>Styles:</b></dt>
 * <dd>MULTI</dd>
 * <dt><b>Events:</b></dt>
 * <dd>(none)</dd>
 * </dl>
 * <p>
 * Note: This class sets SAVE style by default.
 * </p><p>
 * IMPORTANT: This class is <em>not</em> intended to be subclassed.
 * It can not be forced, so be careful!
 * </p>
 *
 * @see FileDialog
 * @see SaveFileDialog#checkSubclass
 */
public class SaveFileDialog {
	public final static String DEFAULT_NAMESPACE = "default";
	public final static String INITIAL_FILTER_PATH = "initial.filter.path";
	public final static String INITIAL_FILTER_PATTERN  = "initial.filter.pattern";

	protected final FileDialog fileDialog;
	protected boolean overwrite = false;
	/**
	 * The concept is storing FileDialogFilter list, and syncing to the
	 * FileDialog when it is changed and before open(). This way we can
	 * use filter much easier.
	 * @see FileDialogFileNameFilter
	 */
	protected final List<FileDialogFileNameFilter> fileDialogFilterList;
	protected boolean filterListSynced;

	protected String initialSelectedPattern = null;

	/**
	 * The preference store to store values in.
	 */
	protected final IPreferenceStore store;
	/**
	 * The namespace in the preference store to store values in.
	 */
	protected final String namespace;

	/**
	 * Constructs a new instance of this class given only its parent.
	 *
	 * @param parent a shell which will be the parent of the new instance
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
	 *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
	 * </ul>
	 */
	public SaveFileDialog(final Shell parent) {
		this(parent, SWT.APPLICATION_MODAL);
	}

	public SaveFileDialog(final Shell parent, final IPreferenceStore store, final String namespace) {
		this(parent, SWT.APPLICATION_MODAL, store, namespace);
	}

	/**
	 * Constructs a new instance of this class given its parent
	 * and a style value describing its behavior and appearance.
	 * <p>
	 * The style value is either one of the style constants defined in
	 * class <code>SWT</code> which is applicable to instances of this
	 * class, or must be built by <em>bitwise OR</em>'ing together 
	 * (that is, using the <code>int</code> "|" operator) two or more
	 * of those <code>SWT</code> style constants. The class description
	 * lists the style constants that are applicable to the class.
	 * Style bits are also inherited from superclasses.
	 * </p>
	 *
	 * @param parent a shell which will be the parent of the new instance
	 * @param style the style of dialog to construct
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the parent</li>
	 *    <li>ERROR_INVALID_SUBCLASS - if this class is not an allowed subclass</li>
	 * </ul>
	 * 
	 * @see SWT#SAVE
	 * @see SWT#OPEN
	 * @see SWT#MULTI
	 */
	public SaveFileDialog(final Shell parent, final int style) {
		this(parent, style, null, null);
	}

	public SaveFileDialog(final Shell parent, final int style, final IPreferenceStore store, final String namespace) {
		fileDialog = new FileDialog(parent, style | SWT.SAVE);
		this.store = store;
		this.namespace = namespace == null ? DEFAULT_NAMESPACE : namespace;
		if( store != null ) {
			final String filterPath = store.getString(getIdAppendedNamespace(INITIAL_FILTER_PATH));
			setFilterPath(filterPath.isEmpty() ? System.getProperty("user.dir") /*File.listRoots()[0].getAbsolutePath()*/ : filterPath);
			final String filterPattern = store.getString(getIdAppendedNamespace(INITIAL_FILTER_PATTERN));
			if( !filterPattern.isEmpty() )
				initialSelectedPattern = filterPattern;
		}
		fileDialogFilterList = new ArrayList<FileDialogFileNameFilter>();
		filterListSynced = true;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean equals(final Object obj) {
		return fileDialog.equals(obj);
	}

	/**
	 * {@inheritDoc}
	 */
	public int hashCode() {
		return fileDialog.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	public String toString() {
		return fileDialog.toString();
	}

	protected String getIdAppendedNamespace(final String id) {
		return new StringBuilder(namespace).append('/').append(id).toString();
	}

	/**
	 * Returns the flag that the dialog will use to
	 * determine whether to prompt the user for file
	 * overwrite if the selected file already exists.
	 * <p>
	 * Since using overwrite feature is buggy in FileDialog class, we implement
	 * that feature in this class.
	 * </p>
	 * @return true if the dialog will prompt for file overwrite, false otherwise
	 * 
	 * @since 3.4
	 */
	public boolean getOverwrite() {
		return overwrite;
	}

	/**
	 * Sets the flag that the dialog will use to
	 * determine whether to prompt the user for file
	 * overwrite if the selected file already exists.
	 * <p>
	 * Since using overwrite feature is buggy in FileDialog class, we implement
	 * that feature in this class.
	 * </p>
	 * @param overwrite true if the dialog will prompt for file overwrite, false otherwise
	 * 
	 * @since 3.4
	 */
	public void setOverwrite(final boolean overwrite) {
		this.overwrite = overwrite;
	}

	/**
	 * Returns the size of filter list, i.e. the amount of filters.
	 * @return the size of filter list, i.e. the amount of filters
	 */
	public int getFiltersSize() {
		return fileDialogFilterList.size();
	}

	/**
	 * Returns the unmodifiable list of filters.
	 * @return the unmodifiable list of filters
	 */
	public List<FileDialogFileNameFilter> getFilters() {
		return Collections.unmodifiableList(fileDialogFilterList);
	}

	/**
	 * Adds the filter created from formatName and filenamePatternList to the
	 * filter list.
	 * @param formatName the name of format, for human reading
	 * @param filenamePatternList the list of filename patterns
	 * @see List#add(Object)
	 */
	public void addFilter(final String formatName, final List<String> filenamePatternList) {
		addFilter( formatName, filenamePatternList, null );
	}

	/**
	 * Adds the filter created from formatName, filenamePatternList and suffix
	 * to the filter list.
	 * @param formatName the name of format, for human reading
	 * @param filenamePatternList the list of filename patterns
	 * @param suffix the default suffix
	 * @see List#add(Object)
	 */
	public void addFilter(final String formatName, final List<String> filenamePatternList, final String suffix) {
		addFilter( new FileDialogFileNameFilter(formatName, filenamePatternList, suffix ) );
	}

	/**
	 * Adds the fileDialogFilter to the filter list.
	 * @param fileDialogFilter the filter
	 * @see List#add(Object)
	 */
	public void addFilter(final FileDialogFileNameFilter fileDialogFilter) {
		if( !fileDialogFilterList.contains(fileDialogFilter)) {
			fileDialogFilterList.add(fileDialogFilter);
			filterListSynced = false;
		}
	}

	/**
	 * Removes the fileDialogFilter from the filter list.
	 * @param fileDialogFilter the filter
	 * @return true if the filter list contained the specified filter
	 * @see List#remove(Object)
	 */
	public boolean removeFilter(final FileDialogFileNameFilter fileDialogFilter) {
		final boolean result = fileDialogFilterList.remove(fileDialogFilter);
		if( result )
			filterListSynced = false;
		return result;
	}

	/**
	 * Removes the filter at index from the filter list.
	 * @param index the index
	 * @return the removed FileDialogFilter
	 * @see List#remove(int)
	 */
	public FileDialogFileNameFilter removeFilter(final int index) {
		final FileDialogFileNameFilter result = fileDialogFilterList.remove(index);
		filterListSynced = false;
		return result;
	}

    /**
     * Removes all of the filters from the filter list.
     * The list will be empty after this call returns.
	 * @see List#clear()
     */
	public void clearFilters() {
		fileDialogFilterList.clear();
		filterListSynced = false;
	}

	/**
	 * Determines the suffixes of image formats that can be written by ImageIO,
	 * and adds the result to the filter list.
	 */
	public void addWritableImageFilters() {
		/* Tricky solution: loading and updating ImageIO writers by
		 * activating both the plugin containing the writers and the
		 * writer plugins by the plugin's JavaImageSaver's static method.
		 */
		new JavaImageSaver(null, null, 0, false);
		boolean needSync = false;
		final String writerFileSuffixes[] = ImageIO.getWriterFileSuffixes();
		final HashSet<String> uniqueSuffixSet = new HashSet<String>(writerFileSuffixes.length);
		final HashSet<ImageWriterSpi> uniqueWriters = new HashSet<ImageWriterSpi>(writerFileSuffixes.length);
		for( final String fileSuffix : writerFileSuffixes ) {
			final Iterator<ImageWriter> writers = ImageIO.getImageWritersBySuffix(fileSuffix);
			while( writers.hasNext() ) {
				final ImageWriterSpi provider = writers.next().getOriginatingProvider();
				if( uniqueWriters.contains(provider) )
					continue;
				uniqueWriters.add(provider);
				final List<String> formatNames = Arrays.asList(provider.getFormatNames());
				if( uniqueSuffixSet.containsAll(formatNames) )
					continue;
				uniqueSuffixSet.addAll(formatNames);
				final ArrayList<String> filterSuffixList = new ArrayList<String>(formatNames);
				Collections.sort(filterSuffixList, new FileDialogFileNameFilter.FilterNameComparator(true));
				Collections.sort(formatNames, new FileDialogFileNameFilter.FilterNameComparator(false));
				final ArrayList<String> filenamePatternList = new ArrayList<String>(filterSuffixList.size());
				for( final String fnfFilter : filterSuffixList ) {
					filenamePatternList.add(new StringBuilder("*.").append(fnfFilter).toString());
				}
				final FileDialogFileNameFilter newFileDialogFilter = new FileDialogFileNameFilter(formatNames.get(0), filenamePatternList, filterSuffixList.get(0));
				if( !fileDialogFilterList.contains(newFileDialogFilter)) {
					fileDialogFilterList.add(newFileDialogFilter);
					needSync = true;
				}
			}
		}
		if( needSync )
			filterListSynced = false;
	}

	/**
	 * Determines the suffixes of image formats that can be written by ImageIO,
	 * and sets the result in the saveFileDialog.
	 * @param saveFileDialog the file dialog to set the suffixes in
	 */
	protected void syncFilterList() {
		final String prevFilterPattern = initialSelectedPattern != null ? initialSelectedPattern : getSelectedFilterPattern();

		Collections.sort(fileDialogFilterList, FileDialogFileNameFilter.FILE_DIALOG_FILTER_COMPARATOR);
		final ArrayList<String> filterNames = new ArrayList<String>(fileDialogFilterList.size());
		final ArrayList<String> filterPatternList = new ArrayList<String>(fileDialogFilterList.size());
		for( final FileDialogFileNameFilter fdFilter : fileDialogFilterList ) {
			filterPatternList.add(fdFilter.getFilenamePatterns());
			filterNames.add(fdFilter.getFullFormatName());
		}
		final String filterNamesArray[] = new String[filterNames.size()];
		setFilterNames(filterNames.toArray(filterNamesArray));
		final String filterPatternArray[] = new String[filterPatternList.size()];
		setFilterPatterns(filterPatternList.toArray(filterPatternArray));

		setSelectedFilterPattern(prevFilterPattern);
		filterListSynced = true;
	}

	protected boolean matchSuffixedPattern(final String fileName) {
		for( final FileDialogFileNameFilter fdFilter : fileDialogFilterList ) {
			if( fdFilter.getDefaultSuffix() == null )
				continue;
			for( final String filenamePattern : fdFilter.getFilenamePatternList() ) {
				if( new WildCardFileFilter(filenamePattern, false).fullMatches(fileName))
					return true;
			}
		}
		return false;
	}
	/**
	 * {@inheritDoc}
	 * <p>
	 * Since the underlying layer is buggy, this method contains a workaround
	 * for the following case. When the overwrite flag is set, and the user
	 * specified a file without suffix, and that file does not exist, the
	 * overwriting confirmation dialog does not appear. This would be right,
	 * if the FileDialog would not append the selected suffix to the specified
	 * file name without suffix. If the file with this concatenated file name
	 * exists, the confirmation dialog does not appear, and this is the bug.
	 * </p>
	 */
	public String open() {
		String newFilePath = null;
		do {
			if( !filterListSynced )
				syncFilterList();
			if( fileDialog.open() == null ) //Cancelled
				break;
			if( store != null ) {
				store.setValue(getIdAppendedNamespace(INITIAL_FILTER_PATH), getFilterPath());
				final String selectedFilterPattern = getSelectedFilterPattern();
				store.setValue(getIdAppendedNamespace(INITIAL_FILTER_PATTERN),
					selectedFilterPattern == null ? StringUtils.EMPTY_STRING :selectedFilterPattern);
			}
			if( getFilterIndex() >= 0 ) {
				final String defaultSuffix = fileDialogFilterList.get(getFilterIndex()).getDefaultSuffix();
				if( defaultSuffix != null && !matchSuffixedPattern(getFileName()))
					setFileName(new StringBuilder(getFileName()).append('.').append(defaultSuffix).toString());
			}
			final File newFile = new File(getFilterPath(), getFileName());
			newFilePath = newFile.toString();
			if( !getOverwrite() )
				break;
			if (!newFile.exists() || OverwriteConfirmationDialog.open(getParent(), newFile))
				break;
		} while( true );
		return newFilePath;
	}

	/**
	 * Returns the path of the first file that was
	 * selected in the dialog relative to the filter path, or an
	 * empty string if no such file has been selected.
	 * 
	 * @return the relative path of the file
	 */
	public String getFileName() {
		return fileDialog.getFileName();
	}

	/**
	 * Returns a (possibly empty) array with the paths of all files
	 * that were selected in the dialog relative to the filter path.
	 * 
	 * @return the relative paths of the files
	 */
	public String[] getFileNames() {
		return fileDialog.getFileNames();
	}

	/**
	 * Returns the file patterns which the dialog will
	 * use to filter the files it shows.
	 *
	 * @return the file name filter patterns
	 */
	protected String[] getFilterPatterns() {
		return fileDialog.getFilterExtensions();
	}

	/**
	 * Get the 0-based index of the file name filter
	 * which was selected by the user, or -1 if no filter
	 * was selected.
	 * <p>
	 * This is an index into the FilterPatterns, FilterNames, and Filters.
	 * </p>
	 *
	 * @return index the file name filter index
	 * 
	 * @see #getFilterPatterns
	 * @see #getFilterNames
	 * 
	 * @since 3.4
	 */
	public int getFilterIndex () {
		return fileDialog.getFilterIndex();
	}

	/**
	 * Returns the names that describe the file name filters
	 * which the dialog will use to filter the files it shows.
	 *
	 * @return the list of filter names
	 */
	protected String[] getFilterNames() {
		return fileDialog.getFilterNames();
	}

	/**
	 * Returns the directory path that the dialog will use, or an empty
	 * string if this is not set.  File names in this path will appear
	 * in the dialog, filtered according to the file name filter.
	 *
	 * @return the directory path string
	 * 
	 * @see #setFilterPatterns
	 */
	public String getFilterPath() {
		return fileDialog.getFilterPath();
	}

	/**
	 * Set the initial filename which the dialog will
	 * select by default when opened to the argument,
	 * which may be null.  The name will be prefixed with
	 * the filter path when one is supplied.
	 * 
	 * @param string the file name
	 */
	public void setFileName(final String string) {
		fileDialog.setFileName(string);
	}

	/**
	 * Set the file patterns which the dialog will
	 * use to filter the files it shows to the argument,
	 * which may be null.
	 * <p>
	 * The strings are platform specific. For example, on
	 * some platforms, a name filter pattern is typically
	 * of the form "*.extension", where "*.*" matches all files.
	 * For filters with multiple patterns, use ';' as
	 * a separator, e.g. "*.jpg;*.png".
	 * </p>
	 *
	 * @param multiPatterns the patterns of file name filter
	 * 
	 * @see #setFilterNames to specify the user-friendly
	 * names corresponding to the patterns
	 */
	protected void setFilterPatterns(final String[] multiPatterns) {
		fileDialog.setFilterExtensions(multiPatterns);
	}

	/**
	 * Set the 0-based index of the file name filter
	 * which the dialog will use initially to filter the files
	 * it shows to the argument.
	 * <p>
	 * This is an index into the FilterPatterns, FilterNames, and Filters.
	 * </p>
	 *
	 * @param index the file name filter index
	 * 
	 * @see #setFilterPatterns
	 * @see #setFilterNames
	 * 
	 * @since 3.4
	 */
	public void setFilterIndex(final int index) {
		fileDialog.setFilterIndex(index);
		initialSelectedPattern = null;
	}

	/**
	 * Sets the names that describe the file name filters
	 * which the dialog will use to filter the files it shows
	 * to the argument, which may be null.
	 * <p>
	 * Each name is a user-friendly short description shown for
	 * its corresponding filter. The <code>names</code> array must
	 * be the same length as the <code>patterns</code> array.
	 * </p>
	 *
	 * @param names the list of filter names, or null for no filter names
	 * 
	 * @see #setFilterPatterns
	 */
	protected void setFilterNames(final String[] names) {
		fileDialog.setFilterNames(names);
	}

	/**
	 * Sets the directory path that the dialog will use
	 * to the argument, which may be null. File names in this
	 * path will appear in the dialog, filtered according
	 * to the file name filter. If the string is null,
	 * then the operating system's default filter path
	 * will be used.
	 * <p>
	 * Note that the path string is platform dependent.
	 * For convenience, either '/' or '\' can be used
	 * as a path separator.
	 * </p>
	 *
	 * @param string the directory path
	 * 
	 * @see #setFilterPatterns
	 */
	public void setFilterPath(final String string) {
		fileDialog.setFilterPath(string);
	}

	/**
	 * Returns the receiver's parent, which must be a <code>Shell</code>
	 * or null.
	 *
	 * @return the receiver's parent
	 *
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public Shell getParent() {
		return fileDialog.getParent();
	}

	/**
	 * Returns the receiver's style information.
	 * <p>
	 * Note that, the value which is returned by this method <em>may
	 * not match</em> the value which was provided to the constructor
	 * when the receiver was created. 
	 * </p>
	 *
	 * @return the style bits
	 *
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public int getStyle() {
		return fileDialog.getStyle();
	}

	/**
	 * Returns the receiver's text, which is the string that the
	 * window manager will typically display as the receiver's
	 * <em>title</em>. If the text has not previously been set, 
	 * returns an empty string.
	 *
	 * @return the text
	 *
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public String getText() {
		return fileDialog.getText();
	}

	/**
	 * Sets the receiver's text, which is the string that the
	 * window manager will typically display as the receiver's
	 * <em>title</em>, to the argument, which must not be null. 
	 *
	 * @param string the new text
	 *
	 * @exception IllegalArgumentException <ul>
	 *    <li>ERROR_NULL_ARGUMENT - if the text is null</li>
	 * </ul>
	 * @exception SWTException <ul>
	 *    <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
	 *    <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
	 * </ul>
	 */
	public void setText(final String string) {
		fileDialog.setText(string);
	}

	/**
	 * Returns the name of file name filter which was selected
	 * by the user, or null if no filter was selected.
	 *
	 * @return the name of file name filter
	 * 
	 * @see #setSelectedFilterName
	 * @see #getFilterNames
	 * @see #getFilterPatterns
	 */
	public String getSelectedFilterName() {
		final int prevFilterIndex = getFilterIndex();
		return prevFilterIndex >= 0 ? getFilterNames()[prevFilterIndex] : null;
	}

	/**
	 * Sets the name of file name filter which the dialog will
	 * use initially to filter the files it shows to the argument,
	 * which may be null.
	 * @param filterName the name of file name filter
	 * 
	 * @see #getSelectedFilterName
	 * @see #setFilterNames
	 * @see #setFilterPatterns
	 */
	public void setSelectedFilterName(final String filterName) {
		if( filterName != null ) {
			final int newFilterIndex = Arrays.asList(getFilterNames()).indexOf(filterName);
			if( newFilterIndex >= 0 )
				setFilterIndex(newFilterIndex);
		}
	}

	/**
	 * Returns the pattern of file name filter which was selected
	 * by the user, or null if no filter was selected. The pattern might
	 * contain more patterns separated by ';'.
	 *
	 * @return the pattern of file name filter
	 * 
	 * @see #setSelectedFilterPattern
	 * @see #getFilterPatterns
	 * @see #getFilterNames
	 */
	public String getSelectedFilterPattern() {
		final int prevFilterIndex = getFilterIndex();
		return prevFilterIndex >= 0 ? getFilterPatterns()[prevFilterIndex] : null;
	}

	/**
	 * Set the pattern of file name filter which the dialog will
	 * use initially to filter the files it shows to the argument,
	 * which may be null.
	 * <p>
	 * The string is platform specific. For example, on
	 * some platforms, a name filter pattern is typically
	 * of the form "*.extension", where "*.*" matches all files.
	 * For filters with multiple patterns, use ';' as
	 * a separator, e.g. "*.jpg;*.png".
	 * </p>
	 *
	 * @param filterPattern the pattern of file name filter
	 * 
	 * @see #getSelectedFilterPattern
	 * @see #setFilterPatterns
	 * @see #setFilterNames
	 */
	public void setSelectedFilterPattern(final String filterPattern) {
		if( filterPattern != null ) {
			final int newFilterIndex = Arrays.asList(getFilterPatterns()).indexOf(filterPattern);
			if( newFilterIndex >= 0 )
				setFilterIndex(newFilterIndex);
		} else
			setFilterIndex(-1);
	}

}
