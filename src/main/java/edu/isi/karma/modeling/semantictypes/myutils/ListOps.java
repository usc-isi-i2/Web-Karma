package edu.isi.karma.modeling.semantictypes.myutils;

import java.util.ArrayList;

public class ListOps {

	// Sorts values in descending order
	// Rearranges the list so that the objects are at the same index as their
	// corresponding values
	// So, the item that had highest corresponding score will be at position
	// zero and
	// item with the lowest score will be at the end of the list
	public static <T> void sortListOnValues(ArrayList<T> list,
			ArrayList<Double> values) {
		int pos = 0;

		for (int i = 0; i < list.size() - 1; i++) {
			pos = i;
			for (int j = i + 1; j < list.size(); j++) {
				if (values.get(j) > values.get(pos)) {
					pos = j;
				}
			}
			double maxVal = values.get(pos);
			values.set(pos, values.get(i));
			values.set(i, maxVal);

			T obj = list.get(pos);
			list.set(pos, list.get(i));
			list.set(i, obj);
		}
	}

}
