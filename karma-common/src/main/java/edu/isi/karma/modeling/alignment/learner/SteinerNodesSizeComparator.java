package edu.isi.karma.modeling.alignment.learner;

import java.util.Comparator;

public class SteinerNodesSizeComparator implements Comparator<SteinerNodes> {

	@Override
	public int compare(SteinerNodes sn1, SteinerNodes sn2) {

		return Integer.compare(sn1.getNonModelNodesCount(),
			sn2.getNonModelNodesCount());
	}

}
