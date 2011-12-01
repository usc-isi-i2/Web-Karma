package edu.isi.karma.modeling.alignment;

import java.util.Comparator;

public class EdgeComparatorByLabel implements Comparator<LabeledWeightedEdge> {

	public int compare(LabeledWeightedEdge o1, LabeledWeightedEdge o2) {
		return o1.getLabel().compareTo(o2.getLabel());
	}

}
