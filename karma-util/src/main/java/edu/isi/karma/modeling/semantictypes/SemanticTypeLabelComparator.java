package edu.isi.karma.modeling.semantictypes;

import java.util.Comparator;

public class SemanticTypeLabelComparator implements Comparator {
	
	public int compare(Object o1, Object o2) {
		SemanticTypeLabel l1 = (SemanticTypeLabel)o1;
		SemanticTypeLabel l2 = (SemanticTypeLabel)o2;
		if(l1.getScore() > l2.getScore()) {
			return -1;
		}
		else if(l1.getScore() == l2.getScore()) {
			return 0;
		}
		else return 1;
	}
}
