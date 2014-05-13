package org.embl.cca.utils.ui.widget.support;

import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.embl.cca.utils.datahandling.text.StringUtils;

/**
 * An example for a FileDialogFilter:
 * new FileDialogFilter("PNG", Arrays.asList(new String[]{"*.png", "*.PNG"}), "png");
 */
public class FileDialogFileNameFilter {

	/**
	 * Ordering by lexicographically, ignoring case differences.
	 * The nulls are the biggest.
	 */
	public static class FileDialogFilterComparator implements Comparator<FileDialogFileNameFilter> {
		@Override
		public int compare(final FileDialogFileNameFilter o1, final FileDialogFileNameFilter o2) { //nulls are the biggest
			return o1 == null ? (o2 == null ? 0 : 1 ) :
				(o2 == null ? -1 : compareNotNulls(o1, o2));
		}
		public int compareNotNulls(final FileDialogFileNameFilter o1, final FileDialogFileNameFilter o2) {
			if( o1.equals(o2))
				return 0;
			return o1.shortFormatName.compareToIgnoreCase(o2.shortFormatName);
		}
	}

	/**
	 * Ordering by ascending length and ascending size (capital > small).
	 * Those containing ' ' are bigger than others, and the nulls are the.
	 * biggest, independently on reversed. Comparing two Strings containing
	 * ' ', the one is less in which the first ' ' has less index.
	 */
	public static class FilterNameComparator implements Comparator<String> {
		final boolean reversed;

		public FilterNameComparator(final boolean reversed) {
			this.reversed = reversed;
		}
		public int compareNotNulls(final String o1, final String o2) {
			if( o1.equals(o2))
				return 0;
			if( o1.contains(" ") )
				return o2.contains(" ") ? o1.indexOf(' ') - o2.indexOf(' ') : 1;
			if( o2.contains(" ") )
				return -1;
			final int lengthDiff = o1.length() - o2.length();
			if( !reversed )
				return lengthDiff != 0 ? -lengthDiff : o1.compareTo(o2);
			else
				return lengthDiff != 0 ? lengthDiff : -o1.compareTo(o2);
		}
		@Override
		public int compare(final String o1, final String o2) { //nulls are the biggest
			return o1 == null ? (o2 == null ? 0 : 1 ) :
				(o2 == null ? -1 : compareNotNulls(o1, o2));
		}
		@Override
		public boolean equals(final Object obj) {
			return super.equals(obj);
		}
	}

	protected final String shortFormatName;
	protected final List<String> filenamePatternList;
	protected final String defaultSuffix;

	public final static FileDialogFilterComparator FILE_DIALOG_FILTER_COMPARATOR = new FileDialogFilterComparator();

	public FileDialogFileNameFilter(final String humanReadableName, final List<String> filenamePatternList) {
		this(humanReadableName, filenamePatternList, null);
	}

	public FileDialogFileNameFilter(final String shortFormatName, final List<String> filenamePatternList, final String defaultSuffix) {
		Assert.isLegal(shortFormatName != null, "The shortFormatName argument can not be null.");
		Assert.isLegal(filenamePatternList != null, "The filenameFilterList argument can not be null.");
		this.shortFormatName = shortFormatName;
		this.filenamePatternList = filenamePatternList;
		this.defaultSuffix = defaultSuffix;
	}

	/**
	 * Returns the short format name.
	 * @return the short format name
	 */
	public String getShortFormatName() {
		return shortFormatName;
	}

	/**
	 * Returns the full format name, which is intended to display in the file
	 * dialog. It typically looks like "PNG Files (*.png;*.PNG)".
	 * @return the full format name
	 */
	public String getFullFormatName() {
		return new StringBuilder(getShortFormatName()).append(" Files (").append(getFilenamePatterns()).append(")").toString();
	}

	/**
	 * Returns the default suffix.
	 * @return the default suffix
	 */
	public String getDefaultSuffix() {
		return defaultSuffix;
	}

	/**
	 * Returns the filename pattern list.
	 * @return the filename pattern list
	 */
	public List<String> getFilenamePatternList() {
		return filenamePatternList;
	}

	/**
	 * Returns the concatenated string of filename pattern list separated with ';'.
	 * @return the concatenated string of filename pattern list separated with ';'
	 */
	public String getFilenamePatterns() {
		return StringUtils.join(filenamePatternList, ";");
	}

	@Override
	public boolean equals(final Object obj) {
		if( obj == null )
			return false;
		if( this == obj )
			return true;
		if (obj instanceof FileDialogFileNameFilter) {
			final boolean dsEqual = (defaultSuffix == null && ((FileDialogFileNameFilter)obj).defaultSuffix == null)
				|| (defaultSuffix != null && defaultSuffix.equals(((FileDialogFileNameFilter)obj).defaultSuffix));
			return dsEqual && shortFormatName.equals(((FileDialogFileNameFilter)obj).shortFormatName)
				&& filenamePatternList.equals(((FileDialogFileNameFilter)obj).filenamePatternList);
		}
		return super.equals(obj);
	}

	@Override
	public int hashCode() {
		int result = shortFormatName.hashCode() * 19 + filenamePatternList.hashCode();
		if( defaultSuffix != null )
			result = result * 19 + defaultSuffix.hashCode();
		return result;
	}

}
