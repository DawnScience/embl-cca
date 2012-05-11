package loaders.pilatus;

import java.util.HashMap;

public class PilatusHeader {
	HashMap<String, String> keyAndValuePairs = null;

	public PilatusHeader( HashMap<String, String> map ) {
		keyAndValuePairs = new HashMap<String, String>( map );
	}

	public PilatusHeader( String[] keys, String[] values ) {
//	public PilatusHeader( String keys, String values ) { //separated
		if (keys == null || values == null) {
			keyAndValuePairs = null;
		} else {
	        if (keys.length != values.length)
	        	throw new IllegalArgumentException("'keys' and 'values' arrays differ in size");
			final HashMap<String, String> map = new HashMap<String, String>((int) (keys.length * 1.5));
			for (int i = 0; i < keys.length; i++)
				map.put(keys[ i ], values[ i ]);
			keyAndValuePairs = map;
		}
	}

	public HashMap<String, String> getKeyAndValuePairs() {
		return keyAndValuePairs;
	}

}
