package loaders.pilatus;

import java.util.HashMap;

import org.embl.cca.utils.imageviewer.HeaderData;

public class PilatusHeader extends HeaderData {

	public PilatusHeader(HashMap<String, String> map) {
		super(map);
	}

	public PilatusHeader(String[] keys, String[] values) {
		super(keys, values);
	}

}
