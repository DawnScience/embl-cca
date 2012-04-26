package loaders.pilatus;

import java.util.HashMap;

public class PilatusHeader {
	HashMap keyAndValuePairs = null;

	public PilatusHeader( String[] keys, String[] values ) {
//	public PilatusHeader( String keys, String values ) { //separated
		if (keys == null || values == null) {
			keyAndValuePairs = null;
		} else {
	        if (keys.length != values.length)
	        	throw new IllegalArgumentException("'keys' and 'values' arrays differ in size");
			final HashMap map = new HashMap((int) (keys.length * 1.5));
			for (int i = 0; i < keys.length; i++)
				map.put(keys[ i ], values[ i ]);
			keyAndValuePairs = map;
		}
	}

	public HashMap getKeyAndValuePairs() {
		return keyAndValuePairs;
	}

}
