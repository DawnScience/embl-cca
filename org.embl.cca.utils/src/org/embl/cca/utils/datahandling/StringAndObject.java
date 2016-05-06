package org.embl.cca.utils.datahandling;

import org.eclipse.jface.preference.IPreferenceStore;

public class StringAndObject {
	public IPreferenceStore preferenceStore;
	public String string;
	public Object object;

	public StringAndObject(final IPreferenceStore preferenceStore, final String string, final Object object) {
		this.preferenceStore = preferenceStore;
		this.string = string;
		this.object = object;
	}
}
