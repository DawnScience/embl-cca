package org.embl.cca.utils.imageviewer;

import java.util.HashMap;

public class HeaderData {
	HashMap<String, String> keyAndValuePairs = null;

	public HeaderData( HashMap<String, String> map ) {
		keyAndValuePairs = new HashMap<String, String>( map );
	}

	public HeaderData( String[] keys, String[] values ) {
		if (keys == null || values == null) {
			keyAndValuePairs = null;
		} else {
	        if (keys.length != values.length)
	        	throw new IllegalArgumentException("'keys' and 'values' arrays differ in size");
			final HashMap<String, String> map = new HashMap<String, String>((int) (keys.length * 2));
			for (int i = 0; i < keys.length; i++)
				map.put(keys[ i ], values[ i ]);
			keyAndValuePairs = map;
		}
	}

	public HashMap<String, String> getKeyAndValuePairs() {
		return keyAndValuePairs;
	}
}
