package edu.isi.karma.research.modeling;

import java.util.Comparator;

public class PatternLabelComparator implements Comparator<Pattern> {

	public PatternLabelComparator() {
	}
	
	
	@Override
	public int compare(Pattern p1, Pattern p2) {
		
		if (p1.getLength() < p2.getLength())  
			return -1;
		else if (p1.getLength() > p2.getLength()) 
			return 1;
		else {
			return p1.getLabel().compareTo(p2.getLabel());
		}
	}

}
