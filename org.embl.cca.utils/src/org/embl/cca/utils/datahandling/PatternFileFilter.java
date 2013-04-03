package org.embl.cca.utils.datahandling;

import java.io.*;
import java.util.regex.*;

public class PatternFileFilter implements FileFilter
{
	protected Pattern pattern;
 
	public PatternFileFilter(String patternString, boolean escapedPattern) {
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

	public PatternFileFilter(Pattern pattern) {
		this.pattern = pattern;
	}

	public boolean accept(File file) {
		return pattern.matcher(file.getName()).find();
	}
}
