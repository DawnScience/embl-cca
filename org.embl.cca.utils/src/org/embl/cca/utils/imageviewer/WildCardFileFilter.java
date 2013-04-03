package org.embl.cca.utils.imageviewer;

import java.io.*;
import java.util.regex.*;

public class WildCardFileFilter implements FileFilter
{
	protected Pattern pattern;
 
	public WildCardFileFilter(String patternString, boolean escapedPattern) {
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

	public WildCardFileFilter(Pattern pattern) {
		this.pattern = pattern;
	}

	public boolean accept(File file) {
		return pattern.matcher(file.getName()).find();
	}
}
