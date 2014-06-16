package org.embl.cca.utils.datahandling.collection;

import java.util.ArrayList;
import java.util.List;

public class ListUtils {

	public static <T> List<T> split(final List<T> list, final int i) {
		final List<T> secondPart = list.subList(i, list.size());
		final List<T> returnValue = new ArrayList<T>(secondPart);
		secondPart.clear();
		return returnValue;
	}

}
