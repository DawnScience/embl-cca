package org.embl.cca.utils.datahandling.text;

public class StringUtil {
	public static String replaceRange(String text, String replacement, int beginIndex, int endIndex ) {
		//TODO error handling (invalid indices)
		String result = "";
		if( beginIndex > 0 )
			result += text.substring(0, beginIndex);
		result += replacement;
		if( endIndex < text.length() )
			result += text.substring(endIndex);
		return result;
	}

}
