package org.embl.cca.utils.datahandling.file;

import java.io.File;
import java.io.FileFilter;
import java.util.regex.Pattern;

public class WildCardFileFilter implements FileFilter
{
	protected final Pattern pattern;
 
	public WildCardFileFilter(final String patternString, final boolean escapedPattern) {
		pattern = Pattern.compile( !escapedPattern ? patternString
			.replace("\\", "\\\\")
			.replace(".", "\\.")
			.replace("+", "\\+")
			.replace("(", "\\(")
			.replace(")", "\\)")
			.replace("^", "\\^")
			.replace("$", "\\$")
			.replace("{", "\\{")
			.replace("}", "\\}")
			.replace("[", "\\[")
			.replace("]", "\\]")
			.replace("|", "\\|")
			.replace("*", ".*")
			.replace("?", ".") : patternString);
	}

	public WildCardFileFilter(final Pattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public boolean accept(final File file) {
		return partialMatches(file.getName());
	}

	public boolean partialMatches(final String fileName) {
		return pattern.matcher(fileName).find();
	}

	public boolean fullMatches(final String fileName) {
		return pattern.matcher(fileName).matches();
	}
}
