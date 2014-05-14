package edu.isi.karma.modeling.alignment.learner;

import java.util.Comparator;

public class SteinerNodesConfidenceComparator implements Comparator<SteinerNodes> {

	@Override
	public int compare(SteinerNodes sn1, SteinerNodes sn2) {
	
		if (sn1.getConfidence() == null && sn2.getConfidence() == null)
			return 0;
		else if (sn1.getConfidence() == null)
			return -1;
		else if (sn2.getConfidence() == null)
			return 1;
		else {
			return Double.compare(sn1.getConfidence().getConfidenceValue(),
					sn2.getConfidence().getConfidenceValue());
		}
	}

}