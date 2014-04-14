package org.embl.cca.dviewer.ui.editors.preference;

import java.util.Enumeration;
import java.util.Hashtable;

import org.dawnsci.plotting.api.trace.IImageTrace.DownsampleType;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.preference.IPreferenceStore;
import org.embl.cca.dviewer.ui.editors.utils.PHA;

public class EditorPreferenceHelper {
//	public abstract class ValueChecker {
//	    public abstract Object checkValue( Object value );
//	}
//	class PreferenceValue {
//		String name;
//		Class c;
//		Object defaultValue;
//		Runnable r;
//	}
	static Hashtable<String, Object> defaultValues = new Hashtable<String, Object>();
	static {
		defaultValues.put(DViewerEditorConstants.PREFERENCE_DOWNSAMPLING_TYPE, DownsampleType.MEAN.getIndex());
		defaultValues.put(DViewerEditorConstants.PREFERENCE_APPLY_PHA, false);
		defaultValues.put(DViewerEditorConstants.PREFERENCE_PHA_RADIUS, PHA.radiusDefault);
	}

	protected final static String PreferenceNameNotSupported(final String preferenceName) {
		return new StringBuilder("PreferenceName not supported: ").append(preferenceName).toString();
	}

	public static Object getDefaultValue(final String preferenceName) {
		Assert.isLegal(defaultValues.containsKey(preferenceName), PreferenceNameNotSupported(preferenceName));
		return defaultValues.get(preferenceName);
	}

	public static void setStoreDefaultValue(final IPreferenceStore store, final String preferenceName) {
		if( DViewerEditorConstants.PREFERENCE_DOWNSAMPLING_TYPE.equals(preferenceName) ) {
			store.setDefault(preferenceName, ((Integer)getDefaultValue(preferenceName)).intValue());
		} else if( DViewerEditorConstants.PREFERENCE_APPLY_PHA.equals(preferenceName) ) {
			store.setDefault(preferenceName, ((Boolean)getDefaultValue(preferenceName)).booleanValue());
		} else if( DViewerEditorConstants.PREFERENCE_PHA_RADIUS.equals(preferenceName) ) {
			store.setDefault(preferenceName, ((Integer)getDefaultValue(preferenceName)).intValue());
		} else
			throw new IllegalArgumentException(PreferenceNameNotSupported(preferenceName));
	}

//	public static void setStoreDefaultValues(final IPreferenceStore store) {
//		final Enumeration<String> preferenceNames = defaultValues.keys();
//		while( preferenceNames.hasMoreElements() ) {
//			final String preferenceName = preferenceNames.nextElement();
//			final Object preferenceValue = getDefaultValue(preferenceName);
//			preferenceValue.getClass().
//			switch(preferenceValue.getClass().)
//			store.setDefault(preferenceName, ((Integer)asd).intValue());
//			
//		}
//	}

	public static Object getStoreValue(IPreferenceStore store, String preferenceName) {
		if( DViewerEditorConstants.PREFERENCE_DOWNSAMPLING_TYPE.equals(preferenceName) ) {
			return store.getInt(preferenceName);
		} else if( DViewerEditorConstants.PREFERENCE_APPLY_PHA.equals(preferenceName) ) {
			return store.getBoolean(preferenceName);
		} else if( DViewerEditorConstants.PREFERENCE_PHA_RADIUS.equals(preferenceName) ) {
			return store.getInt(preferenceName);
		} else
			throw new IllegalArgumentException(PreferenceNameNotSupported(preferenceName));
	}

	public static void setStoreValue(IPreferenceStore store, String preferenceName, Object value) {
		if( preferenceName == DViewerEditorConstants.PREFERENCE_DOWNSAMPLING_TYPE ) {
			store.setValue(preferenceName, ((Integer)value).intValue());
		} else if( preferenceName == DViewerEditorConstants.PREFERENCE_APPLY_PHA ) {
			store.setValue(preferenceName, ((Boolean)value).booleanValue());
		} else if( preferenceName == DViewerEditorConstants.PREFERENCE_PHA_RADIUS ) {
			store.setValue(preferenceName, ((Integer)value).intValue());
		} else
			throw new IllegalArgumentException("PreferenceName not supported: " + preferenceName);
	}

	public static void setStoreValueByDefault(IPreferenceStore store, String preferenceName) {
		setStoreValue(store, preferenceName, getDefaultValue(preferenceName));
	}

	public static void fixStoreValue(IPreferenceStore store, String preferenceName) {
		if( !store.contains(preferenceName) )
			throw new IllegalArgumentException("PreferenceName not supported: " + preferenceName);
		if( preferenceName == DViewerEditorConstants.PREFERENCE_DOWNSAMPLING_TYPE ) {
			int value = store.getInt(preferenceName); 
			if( value < 0 || value >= DownsampleType.values().length )
				store.setValue(preferenceName, store.getDefaultInt(preferenceName));
		} else if( preferenceName == DViewerEditorConstants.PREFERENCE_PHA_RADIUS ) {
			int value = store.getInt(preferenceName); 
			if( value < 1 || value > 99 ) {
				store.setValue(preferenceName, store.getDefaultInt(preferenceName));
			}
		}
	}

}
