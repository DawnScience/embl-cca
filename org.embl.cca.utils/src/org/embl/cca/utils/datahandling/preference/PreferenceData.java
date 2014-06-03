package org.embl.cca.utils.datahandling.preference;

import org.eclipse.jface.preference.IPreferenceStore;
import org.embl.cca.utils.datahandling.IDataValidator;
import org.embl.cca.utils.general.Util;

public class PreferenceData {

	protected final String name;
	protected final Class<?> clazz;
	protected final PreferenceStoreExtension storeExtension;
	protected final IDataValidator validator;

	public PreferenceData(final String name, final Class<?> clazz, final IPreferenceStore prefStore, final IDataValidator validator) {
		this.name = name;
		this.clazz = clazz;
		this.validator = validator;
		this.storeExtension = new PreferenceStoreExtension(prefStore);
	}

	public String getName() {
		return name;
	}

	public Class<?> getClazz() {
		return clazz;
	}

	public IDataValidator getValidator() {
		return validator;
	}

	public IPreferenceStore getPreferenceStore() {
		return storeExtension.store;
	}

	public Object getValue() {
		Object value = storeExtension.getValue(name, clazz);
		if( !isValid(value) )
			value = getDefault();
		return value;
	}

	public void setValue(final Object value) {
		validate(value);
		storeExtension.setValue(name, value);
	}

	public void resetValue() {
		setValue(getDefault());
	}

	public Object getDefault() {
		final Object value = storeExtension.getDefault(name, clazz);
		validate(value);
		return value;
	}

	public void setDefault(final Object defaultValue) {
		validate(defaultValue);
		storeExtension.setDefault(name, defaultValue);
	}

	public boolean isValid() {
		return validator == null ? true : validator.isValid(getValue());
	}

	public boolean isValid(final Object value) {
		return validator == null ? true : validator.isValid(value);
	}

	public void validate(final Object value) {
		if( validator != null )
			validator.validate(value);
	}

	@Override
	public boolean equals(final Object object) {
		if( this == object )
			return true;
		if( !(object instanceof PreferenceData) )
			return false;
		final PreferenceData o = (PreferenceData)object;
		if( !this.getName().equals(o.getName()) || !this.getClazz().equals(o.getClazz())
			|| !this.getPreferenceStore().equals(o.getPreferenceStore()))
			return false;
		return org.eclipse.jface.util.Util.equals(this.getValidator(), o.getValidator());
	}

	@Override
	public int hashCode() {
		return Util.hashCode(new Object[] {name, clazz, storeExtension, validator});
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("PreferenceData {")
			.append(name).append(", ").append(clazz.toString()).append(", ")
			.append(storeExtension.toString());
		if( validator != null )
			sb.append(", ").append(validator.toString());
		sb.append("}");
		return sb.toString();
	}
}
