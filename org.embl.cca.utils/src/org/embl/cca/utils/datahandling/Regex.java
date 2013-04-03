package org.embl.cca.utils.datahandling;

public class Regex {

	public static String escapeForRegex(String text) {
		return text
				.replace("\\", "\\\\")
				.replace(".", "\\.")
				.replace("?", "\\?")
				.replace("+", "\\+")
				.replace("*", "\\*")
				.replace("^", "\\^")
				.replace("$", "\\$")
				.replace("(", "\\(")
				.replace(")", "\\)")
				.replace("{", "\\{")
				.replace("}", "\\}")
				.replace("[", "\\[")
				.replace("]", "\\]")
				.replace("|", "\\|");
	}

}
