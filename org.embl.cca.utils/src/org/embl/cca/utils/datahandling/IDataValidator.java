package org.embl.cca.utils.datahandling;

public interface IDataValidator {
	public boolean isValid(final Object value);
	public void validate(final Object value);
}
