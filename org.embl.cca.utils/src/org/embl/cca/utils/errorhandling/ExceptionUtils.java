package org.embl.cca.utils.errorhandling;

import org.slf4j.Logger;

public class ExceptionUtils {

	public final static String UNEXPECTED_ERROR = "Unexpected error";

	/**
	 * Logs an error in logger created by message. This method does nothing
	 * special, it is created for common usage of logError methods. Thus it
	 * is easier to change it in the caller when it gets an additional
	 * exception, for example.
	 * <p>
	 * For example: ExceptionUtils.logError(logger, "Failure");
	 * </p>
	 * @param logger the logger to log the error message in
	 * @param message the error message to log
	 */
	public static void logError(final Logger logger, final String message) {
		logger.error(message);
	}

	/**
	 * Logs an error in logger created by message, e, and where. This log helps
	 * finding problems much better, than just simple logging the exception.
	 * <p>
	 * For example: ExceptionUtils.logError(logger, "Failure", e, this);
	 * </p>
	 * @param logger the logger to log the error message in
	 * @param message the optional error message to log, it can be null
	 * @param e the exception to log as the error
	 * @param where the object where the occurrence of exception is interesting
	 */
	public static void logError(final Logger logger, final String message, final Exception e, final Object where) {
		logger.error(makeErrorMessage(new StringBuilder(message), e, where).toString());
	}

	/**
	 * Makes an error message created by message, e, and where. This log helps
	 * finding problems much better, than just the simple message of exception.
	 * <p>
	 * For example: ExceptionUtils.makeErrorMessage(new StringBuilder("Failure"), e, this);
	 * </p>
	 * @param message the optional error message to log, it can be null
	 * @param e the exception to make error message from
	 * @param where the object where the occurrence of exception is interesting
	 */
	public static StringBuilder makeErrorMessage(final StringBuilder message, final Exception e, final Object where) {
		final StringBuilder thisSB = message == null ? new StringBuilder() : message;
		final StackTraceElement[] stes = e.getStackTrace();
		final int iSup = stes.length;
		int found = iSup;
		for( int i = 0; i < iSup; i++ ) {
			final String s = stes[i].getClassName();
			Class<?> cs = where.getClass().getSuperclass();
			while( cs != null ) {
				if( cs.getName().equals(s)) {
					found = i; //Looking for deepest occurrence of where class
					break;
				}
				cs = cs.getSuperclass();
			}
			if( found < iSup )
				break;
		}
		if( found == iSup )
			found--;
		thisSB.append(" [").append(e.toString()).append(" at ").append(stes[found]).append(']');
		if( e.getCause() != null )
			thisSB.append('\n').append(e.toString());
		return thisSB;
	}

}
