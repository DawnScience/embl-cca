package org.embl.cca.utils.datahandling.text;

import java.math.RoundingMode;
import java.text.AttributedCharacterIterator;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.util.Arrays;
import java.util.Currency;

public class DecimalPaddedFormat extends NumberFormat {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7996106740287037411L;

	protected DecimalFormat df;

	public DecimalPaddedFormat() {
		df = new DecimalFormat();
	}

	public DecimalPaddedFormat(final String pattern) {
		df = new DecimalFormat(pattern);
	}

	public DecimalPaddedFormat(final String pattern,
			final DecimalFormatSymbols symbols) {
		df = new DecimalFormat(pattern, symbols);
	}

	protected int extStringBufferIndexOf(final StringBuffer sb, final char c) {
		final int iSup = sb.length();
		for( int i = 0; i < iSup; i++ )
			if( sb.charAt(i) == c )
				return i;
		return -1;
	}

	public StringBuffer padResult(final StringBuffer sb) {
		final int maxIntegerLength = df.getMaximumIntegerDigits();
		final int maxFractionLength = df.getMaximumFractionDigits();
		if( maxIntegerLength < Integer.MAX_VALUE || maxFractionLength < Integer.MAX_VALUE ) {
			final DecimalFormatSymbols dfSymbols = df.getDecimalFormatSymbols();
			//Unfortunately DecimalFormat does not tell if using currency format
			int resultDotPos = extStringBufferIndexOf(sb, dfSymbols.getDecimalSeparator());
			if( resultDotPos == -1 )
				resultDotPos = extStringBufferIndexOf(sb, dfSymbols.getMonetaryDecimalSeparator());
			final boolean resultContainsDot = resultDotPos >= 0;
			if( !resultContainsDot )
				resultDotPos = sb.length();
			final int resultLength = sb.length();
			if( maxIntegerLength < Integer.MAX_VALUE ) {
				int leadingPadLength = maxIntegerLength - resultDotPos;
				if( leadingPadLength > 0 ) {
					char[] leadingPad = new char[leadingPadLength];
					Arrays.fill(leadingPad, ' ');
					sb.insert(0, leadingPad);
				}
			}
			if( resultContainsDot && maxFractionLength < Integer.MAX_VALUE ) {
				int resultFractionLength = resultLength - resultDotPos;
				if( resultFractionLength > 0 ) //Then we counted separator as well
					resultFractionLength--;
				int trailingPadLength = maxFractionLength - resultFractionLength;
				if( trailingPadLength > 0 ) {
					char[] trailingPad = new char[trailingPadLength];
					Arrays.fill(trailingPad, ' ');
					sb.append(trailingPad);
				}
			}
		}
		return sb;
	}

	@Override
	public StringBuffer format(Object number, StringBuffer toAppendTo,
			FieldPosition pos) {
		return padResult(df.format(number, toAppendTo, pos));
	}

	@Override
	public StringBuffer format(double number, StringBuffer result,
			FieldPosition fieldPosition) {
		return padResult(df.format(number, result, fieldPosition));
	}

	@Override
	public StringBuffer format(long number, StringBuffer result,
			FieldPosition fieldPosition) {
		return padResult(df.format(number, result, fieldPosition));
	}

	@Override
	public AttributedCharacterIterator formatToCharacterIterator(Object obj) {
		return df.formatToCharacterIterator(obj);
	}

	@Override
	public Number parse(String source, ParsePosition parsePosition) {
		return df.parse(source, parsePosition);
	}

	public DecimalFormatSymbols getDecimalFormatSymbols() {
		return df.getDecimalFormatSymbols();
	}

	public void setDecimalFormatSymbols(DecimalFormatSymbols newSymbols) {
		df.setDecimalFormatSymbols(newSymbols);
	}

	public String getPositivePrefix() {
		return df.getPositivePrefix();
	}

	public void setPositivePrefix(String newValue) {
		df.setPositivePrefix(newValue);
	}

	public String getNegativePrefix() {
		return df.getNegativePrefix();
	}

	public void setNegativePrefix(String newValue) {
		df.setNegativePrefix(newValue);
	}

	public String getPositiveSuffix() {
		return df.getPositiveSuffix();
	}

	public void setPositiveSuffix(String newValue) {
		df.setPositiveSuffix(newValue);
	}

	public String getNegativeSuffix() {
		return df.getNegativeSuffix();
	}

	public void setNegativeSuffix(String newValue) {
		df.setNegativeSuffix(newValue);
	}

	public int getMultiplier() {
		return df.getMultiplier();
	}

	public void setMultiplier(int newValue) {
		df.setMultiplier(newValue);
	}

	public int getGroupingSize() {
		return df.getGroupingSize();
	}

	public void setGroupingSize(int newValue) {
		df.setGroupingSize(newValue);
	}

	public boolean isDecimalSeparatorAlwaysShown() {
		return df.isDecimalSeparatorAlwaysShown();
	}

	public void setDecimalSeparatorAlwaysShown(boolean newValue) {
		df.setDecimalSeparatorAlwaysShown(newValue);
	}

	public boolean isParseBigDecimal() {
		return df.isParseBigDecimal();
	}

	public void setParseBigDecimal(boolean newValue) {
		df.setParseBigDecimal(newValue);
	}

	@Override
	public Object clone() {
		final DecimalPaddedFormat other = (DecimalPaddedFormat) super.clone();
		other.df = (DecimalFormat)df.clone();
		return other;
	}

	@Override
	public boolean equals(Object obj) {
		return df.equals(obj);
	}

	@Override
	public int hashCode() {
		return df.hashCode();
	}

	public String toPattern() {
		return df.toPattern();
	}

	public String toLocalizedPattern() {
		return df.toLocalizedPattern();
	}

	public void applyPattern(String pattern) {
		df.applyPattern(pattern);
	}

	public void applyLocalizedPattern(String pattern) {
		df.applyLocalizedPattern(pattern);
	}

	@Override
	public void setMaximumIntegerDigits(int newValue) {
		df.setMaximumIntegerDigits(newValue);
	}

	@Override
	public void setMinimumIntegerDigits(int newValue) {
		df.setMinimumIntegerDigits(newValue);
	}

	@Override
	public void setMaximumFractionDigits(int newValue) {
		df.setMaximumFractionDigits(newValue);
	}

	@Override
	public void setMinimumFractionDigits(int newValue) {
		df.setMinimumFractionDigits(newValue);
	}

	@Override
	public int getMaximumIntegerDigits() {
		return df.getMaximumIntegerDigits();
	}

	@Override
	public int getMinimumIntegerDigits() {
		return df.getMinimumIntegerDigits();
	}

	@Override
	public int getMaximumFractionDigits() {
		return df.getMaximumFractionDigits();
	}

	@Override
	public int getMinimumFractionDigits() {
		return df.getMinimumFractionDigits();
	}

	@Override
	public Currency getCurrency() {
		return df.getCurrency();
	}

	@Override
	public void setCurrency(Currency currency) {
		df.setCurrency(currency);
	}

	@Override
	public RoundingMode getRoundingMode() {
		return df.getRoundingMode();
	}

	@Override
	public void setRoundingMode(RoundingMode roundingMode) {
		df.setRoundingMode(roundingMode);
	}

}
