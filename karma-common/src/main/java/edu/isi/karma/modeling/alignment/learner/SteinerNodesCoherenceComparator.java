package edu.isi.karma.modeling.alignment.learner;

import java.util.Comparator;
import java.util.List;

public class SteinerNodesCoherenceComparator implements Comparator<SteinerNodes> {

	@Override
	public int compare(SteinerNodes sn1, SteinerNodes sn2) {
	
		if (sn1.getCoherence() == null && sn2.getCoherence() == null)
			return 0;
		else if (sn1.getCoherence() == null)
			return -1;
		else if (sn2.getCoherence() == null)
			return 1;
		else {
			List<CoherenceItem> l1 = sn1.getCoherence().getItems();
			List<CoherenceItem> l2 = sn2.getCoherence().getItems();
			
			if ( (l1 == null || l1.isEmpty()) && (l2 == null || l2.isEmpty()) )
				return 0;
			else if (l1 == null || l1.isEmpty())
				return -1;
			else if (l2 == null || l2.isEmpty())
				return 1;
			else {
				int i = 0;
				while (i < l1.size() && i < l2.size()) {
					if (l1.get(i).compareTo(l2.get(i)) == 0)
						i++;
					else
						return l1.get(i).compareTo(l2.get(i));
				}
				
				if (i < l2.size()) return -1; // l1 has less coherence items
				else if (i < l1.size()) return 1; // l2 has less coherence items
//				else return 0;
				else {
					if (sn1.getConfidence().getConfidenceValue() < sn2.getConfidence().getConfidenceValue())
						return -1;
					else if (sn1.getConfidence().getConfidenceValue() > sn2.getConfidence().getConfidenceValue())
						return 1;
					else {
						if (sn1.getNonModelNodesCount() > sn2.getNonModelNodesCount())
							return -1;
						else if (sn1.getNonModelNodesCount() < sn2.getNonModelNodesCount())
							return 1;
						else return 0;
					}
				}
			}
		}
	}

}
