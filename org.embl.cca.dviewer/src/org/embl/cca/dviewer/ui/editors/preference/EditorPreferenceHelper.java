package org.embl.cca.dviewer.ui.editors.preference;

import java.util.Hashtable;

import org.dawb.common.ui.plot.trace.IImageTrace.DownsampleType;
import org.eclipse.jface.preference.IPreferenceStore;

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
		defaultValues.put(EditorConstants.PREFERENCE_DOWNSAMPLING_TYPE, DownsampleType.MEAN.getIndex());
		defaultValues.put(EditorConstants.PREFERENCE_APPLY_PSF, false);
		defaultValues.put(EditorConstants.PREFERENCE_PSF_RADIUS, 8);
	}

	public static Object getDefaultValue(String preferenceName) {
		if( !defaultValues.containsKey(preferenceName))
			throw new RuntimeException("PreferenceName not supported: " + preferenceName);
		return defaultValues.get(preferenceName);
	}

	public static void setStoreDefaultValue(IPreferenceStore store, String preferenceName) {
		if( preferenceName == EditorConstants.PREFERENCE_DOWNSAMPLING_TYPE ) {
			store.setDefault(preferenceName, ((Integer)getDefaultValue(preferenceName)).intValue());
		} else if( preferenceName == EditorConstants.PREFERENCE_APPLY_PSF ) {
			store.setDefault(preferenceName, ((Boolean)getDefaultValue(preferenceName)).booleanValue());
		} else if( preferenceName == EditorConstants.PREFERENCE_PSF_RADIUS ) {
			store.setDefault(preferenceName, ((Integer)getDefaultValue(preferenceName)).intValue());
		} else
			throw new RuntimeException("PreferenceName not supported: " + preferenceName);
	}

	public static Object getStoreValue(IPreferenceStore store, String preferenceName) {
		if( preferenceName == EditorConstants.PREFERENCE_DOWNSAMPLING_TYPE ) {
			return store.getInt(preferenceName);
		} else if( preferenceName == EditorConstants.PREFERENCE_APPLY_PSF ) {
			return store.getBoolean(preferenceName);
		} else if( preferenceName == EditorConstants.PREFERENCE_PSF_RADIUS ) {
			return store.getInt(preferenceName);
		} else
			throw new RuntimeException("PreferenceName not supported: " + preferenceName);
	}

	public static void setStoreValue(IPreferenceStore store, String preferenceName, Object value) {
		if( preferenceName == EditorConstants.PREFERENCE_DOWNSAMPLING_TYPE ) {
			store.setValue(preferenceName, ((Integer)value).intValue());
		} else if( preferenceName == EditorConstants.PREFERENCE_APPLY_PSF ) {
			store.setValue(preferenceName, ((Boolean)value).booleanValue());
		} else if( preferenceName == EditorConstants.PREFERENCE_PSF_RADIUS ) {
			store.setValue(preferenceName, ((Integer)value).intValue());
		} else
			throw new RuntimeException("PreferenceName not supported: " + preferenceName);
	}

	public static void setStoreValueByDefault(IPreferenceStore store, String preferenceName) {
		setStoreValue(store, preferenceName, getDefaultValue(preferenceName));
	}

	public static void fixStoreValue(IPreferenceStore store, String preferenceName) {
		if( !store.contains(preferenceName) )
			throw new RuntimeException("PreferenceName not supported: " + preferenceName);
		if( preferenceName == EditorConstants.PREFERENCE_DOWNSAMPLING_TYPE ) {
			int value = store.getInt(preferenceName); 
			if( value < 0 || value >= DownsampleType.values().length )
				store.setValue(preferenceName, store.getDefaultInt(preferenceName));
		} else if( preferenceName == EditorConstants.PREFERENCE_PSF_RADIUS ) {
			int value = store.getInt(preferenceName); 
			if( value < 1 || value > 99 ) {
				store.setValue(preferenceName, store.getDefaultInt(preferenceName));
			}
		}
	}

}
