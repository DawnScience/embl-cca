package org.embl.cca.utils.datahandling;

import org.eclipse.core.runtime.Assert;

public abstract class ADataValidator implements IDataValidator {

	public void validate(final Object value) {
		Assert.isLegal(isValid(value), new StringBuilder("The value (").append(value.toString()).append(") is invalid.").toString());
	}

}
