package org.embl.cca.utils.errorhandling;

import org.slf4j.Logger;

public class ExceptionUtils {

	public static void logError(Logger logger, String message, Exception e, Object where) {
		StackTraceElement[] stes = e.getStackTrace();
		int found = stes.length - 1;
		for( int i = found; i >= 0; i-- ) {
			if( stes[i].getClassName().startsWith(where.getClass().getName())) {
				found = i; //Looking for deepest occurance of where class
			}
		}
		logger.error(message + " [" + e.getMessage() + " at " + stes[found] + "]");
	}

}
