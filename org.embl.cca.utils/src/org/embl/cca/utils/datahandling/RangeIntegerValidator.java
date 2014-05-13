package org.embl.cca.utils.datahandling;

import org.apache.commons.lang.math.IntRange;

public class RangeIntegerValidator extends ADataValidator {
	protected final IntRange intRange;

	public RangeIntegerValidator(final int min, final int max) {
		intRange = new IntRange(min, max);
	}

	public int getMin() {
		return intRange.getMinimumInteger();
	}

	public int getMax() {
		return intRange.getMaximumInteger();
	}

	@Override
	public boolean isValid(final Object value) {
		return intRange.containsInteger((int)value);
	}
}
