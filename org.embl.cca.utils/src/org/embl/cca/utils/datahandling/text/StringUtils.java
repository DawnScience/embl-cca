package org.embl.cca.utils.datahandling.text;

import java.util.regex.Pattern;

public class StringUtils {
	public final static char EMPTY_CHAR_ARRAY[] = new char[0];
	public final static String EMPTY_STRING = "";

	/**
	 * Replaces the range of beginIndex, endIndex of text with replacement.
	 * Only valid beginIndex and endIndex is considered.
	 * For example: replaceRange("testing", "ank", 1, 4) results in "tanking".
	 * But: replaceRange("testing", "oops", -3, 9) results in "oops".
	 * @param text the text to replace in
	 * @param replacement the replacement string
	 * @param beginIndex the beginning of replacement range
	 * @param endIndex the ending of replacement range
	 * @return the new string containing the result of replacing
	 */
	public static String replaceRange(final String text, final String replacement,
			final int beginIndex, final int endIndex) {
		// TODO error handling (invalid indices)
		String result = "";
		if (beginIndex > 0)
			result += text.substring(0, beginIndex);
		result += replacement;
		if (endIndex < text.length())
			result += text.substring(endIndex);
		return result;
	}

	/**
	 * Converts the number to the result of <code>toString</code> method called
	 * on the number if number is not null.
	 * 
	 * @param number
	 *            the number to convert
	 * @return number.toString() if number is not null, else "null"
	 */
	public static String numberToString(final Number number) {
		return number == null ? "null" : number.toString();
	}

	/**
	 * Joins the elements of the provided Iterable into a single String
	 * containing the provided elements. No delimiter is added before or after
	 * the list. A null separator or null element is the same as an empty String
	 * ("").
	 * 
	 * @param elements
	 *            - the Iterable providing the values to join together, may be
	 *            null
	 * @param separator
	 *            - the separator character to use, null treated as ""
	 * @return the joined String, null if null iterator input
	 */
	public static String join(final Iterable<?> elements, final String separator) {
		if (elements == null)
			return null;
		final StringBuilder sb = new StringBuilder();
		if (separator != null && separator.length() > 0) {
			for (final Object e : elements) {
				if (sb.length() > 0)
					sb.append(separator);
				if (e != null)
					sb.append(e);
				else
					sb.append(EMPTY_CHAR_ARRAY);
			}
		} else {
			for (final Object e : elements) {
				if (e != null)
					sb.append(e);
				else
					sb.append(EMPTY_CHAR_ARRAY);
			}
		}
		return sb.toString();
	}

	/**
	 * Compares two strings with calling equals method on first string.
	 * However if first string is null, then compares them by reference.
	 * @param str1 first string to compare
	 * @param str2 second string to compare
	 * @return true if the two strings are null, or if they are equal
	 */
	public static boolean equalStringsEvenNulls(final String str1, final String str2) {
		if( str1 == null )
			return str2 == null;
		return str1.equals(str2);
	}

	/**
	 * Matches the pattern in text, optionally case insensitive.
	 * @param text the String to match in
	 * @param pattern the String to match
	 * @param caseInsensitive if true, then case insensitive matching is performed
	 * @return true if the matching is successful
	 */
	public static boolean matchStringWithPattern(final String text, final String pattern, final boolean caseInsensitive) {
		return Pattern.compile(pattern, caseInsensitive ? Pattern.CASE_INSENSITIVE : 0).matcher(text).matches();
	}

	/**
	 * Matches the pattern in text, case sensitive.
	 * @param text the String to match in
	 * @param pattern the String to match
	 * @return true if the matching is successful
	 */
	public static boolean matchStringWithPattern(final String text, final String pattern) {
		return matchStringWithPattern(text, pattern, false);
	}

}
