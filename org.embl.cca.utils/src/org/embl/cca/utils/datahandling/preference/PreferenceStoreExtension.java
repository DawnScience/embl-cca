package org.embl.cca.utils.datahandling.preference;

import org.eclipse.jface.preference.IPreferenceStore;

public class PreferenceStoreExtension {

	protected final IPreferenceStore store;

	public final static String PreferenceNameNotSupported(final String preferenceName) {
		return new StringBuilder("The preferenceName (").append(preferenceName)
			.append(") is not supported.").toString();
	}

	public final static String PreferenceTypeNotSupported(final String preferenceName, final Class<?> c) {
		return new StringBuilder("The preferenceName (").append(preferenceName)
			.append(") is supported, but type of preference (").append(c.getName()).append(" is not.").toString();
	}

	public PreferenceStoreExtension(final IPreferenceStore store) {
		this.store = store;
	}

	/**
	 * Gets the default value of preference as clazz type from preference store.
	 * The clazz type can be any type of these: int, Integer, boolean, Boolean,
	 * String, long, Long, float, Float, double, Double.
	 * @param preferenceName the name of preference, can not be null
	 * @param clazz the class of preference, can not be null
	 */
	public Object getDefault(final String preferenceName, final Class<? extends Object> clazz) {
		if(Integer.class.isAssignableFrom(clazz))
			return store.getDefaultInt(preferenceName);
		else if(int.class.isAssignableFrom(clazz))
			return store.getDefaultInt(preferenceName);
		else if(Boolean.class.isAssignableFrom(clazz))
			return store.getDefaultBoolean(preferenceName);
		else if(boolean.class.isAssignableFrom(clazz))
			return store.getDefaultBoolean(preferenceName);
		else if(String.class.isAssignableFrom(clazz))
			return store.getDefaultString(preferenceName);
		else if(Long.class.isAssignableFrom(clazz))
			return store.getDefaultLong(preferenceName);
		else if(long.class.isAssignableFrom(clazz))
			return store.getDefaultLong(preferenceName);
		else if(Float.class.isAssignableFrom(clazz))
			return store.getDefaultFloat(preferenceName);
		else if(float.class.isAssignableFrom(clazz))
			return store.getDefaultFloat(preferenceName);
		else if(Double.class.isAssignableFrom(clazz))
			return store.getDefaultDouble(preferenceName);
		else if(double.class.isAssignableFrom(clazz))
			return store.getDefaultDouble(preferenceName);
		else
			throw new IllegalArgumentException(PreferenceTypeNotSupported(preferenceName, clazz));
	}

	/**
	 * Sets the default value of preference in preference store.
	 * The value can be any type of these: int, Integer, boolean, Boolean,
	 * String, long, Long, float, Float, double, Double.
	 * @param preferenceName the name of preference, can not be null
	 * @param value the value of preference, can not be null
	 */
	public void setDefault(final String preferenceName, final Object value) {
		final Class<? extends Object> clazz = value.getClass();
		if(Integer.class.isAssignableFrom(clazz))
			store.setDefault(preferenceName, ((Integer)value).intValue());
		else if(int.class.isAssignableFrom(clazz))
			store.setDefault(preferenceName, (int)value);
		else if(Boolean.class.isAssignableFrom(clazz))
			store.setDefault(preferenceName, ((Boolean)value).booleanValue());
		else if(boolean.class.isAssignableFrom(clazz))
			store.setDefault(preferenceName, (boolean)value);
		else if(String.class.isAssignableFrom(clazz))
			store.setDefault(preferenceName, ((String)value));
		else if(Long.class.isAssignableFrom(clazz))
			store.setDefault(preferenceName, ((Long)value).longValue());
		else if(long.class.isAssignableFrom(clazz))
			store.setDefault(preferenceName, (long)value);
		else if(Float.class.isAssignableFrom(clazz))
			store.setDefault(preferenceName, ((Float)value).floatValue());
		else if(float.class.isAssignableFrom(clazz))
			store.setDefault(preferenceName, (float)value);
		else if(Double.class.isAssignableFrom(clazz))
			store.setDefault(preferenceName, ((Double)value).doubleValue());
		else if(double.class.isAssignableFrom(clazz))
			store.setDefault(preferenceName, (double)value);
		else
			throw new IllegalArgumentException(PreferenceTypeNotSupported(preferenceName, clazz));
	}

	public Object getValue(final String preferenceName, final Class<? extends Object> clazz) {
		if( !store.contains(preferenceName) )
			return getDefault(preferenceName, clazz);
		if(Integer.class.isAssignableFrom(clazz))
			return store.getInt(preferenceName);
		else if(int.class.isAssignableFrom(clazz))
			return store.getInt(preferenceName);
		else if(Boolean.class.isAssignableFrom(clazz))
			return store.getBoolean(preferenceName);
		else if(boolean.class.isAssignableFrom(clazz))
			return store.getBoolean(preferenceName);
		else if(String.class.isAssignableFrom(clazz))
			return store.getString(preferenceName);
		else if(Long.class.isAssignableFrom(clazz))
			return store.getLong(preferenceName);
		else if(long.class.isAssignableFrom(clazz))
			return store.getLong(preferenceName);
		else if(Float.class.isAssignableFrom(clazz))
			return store.getFloat(preferenceName);
		else if(float.class.isAssignableFrom(clazz))
			return store.getFloat(preferenceName);
		else if(Double.class.isAssignableFrom(clazz))
			return store.getDouble(preferenceName);
		else if(double.class.isAssignableFrom(clazz))
			return store.getDouble(preferenceName);
		else
			throw new IllegalArgumentException(PreferenceTypeNotSupported(preferenceName, clazz));
	}

	public void setValue(final String preferenceName, final Object value) {
		final Class<? extends Object> clazz = value.getClass();
		if(Integer.class.isAssignableFrom(clazz))
			store.setValue(preferenceName, ((Integer)value).intValue());
		else if(int.class.isAssignableFrom(clazz))
			store.setValue(preferenceName, (int)value);
		else if(Boolean.class.isAssignableFrom(clazz))
			store.setValue(preferenceName, ((Boolean)value).booleanValue());
		else if(boolean.class.isAssignableFrom(clazz))
			store.setValue(preferenceName, (boolean)value);
		else if(String.class.isAssignableFrom(clazz))
			store.setValue(preferenceName, ((String)value));
		else if(Long.class.isAssignableFrom(clazz))
			store.setValue(preferenceName, ((Long)value).longValue());
		else if(long.class.isAssignableFrom(clazz))
			store.setValue(preferenceName, (long)value);
		else if(Float.class.isAssignableFrom(clazz))
			store.setValue(preferenceName, ((Float)value).floatValue());
		else if(float.class.isAssignableFrom(clazz))
			store.setValue(preferenceName, (float)value);
		else if(Double.class.isAssignableFrom(clazz))
			store.setValue(preferenceName, ((Double)value).doubleValue());
		else if(double.class.isAssignableFrom(clazz))
			store.setValue(preferenceName, (double)value);
		else
			throw new IllegalArgumentException(PreferenceTypeNotSupported(preferenceName, clazz));
	}
}
