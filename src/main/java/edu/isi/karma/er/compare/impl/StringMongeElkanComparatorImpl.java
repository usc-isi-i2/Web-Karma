package edu.isi.karma.er.compare.impl;

import uk.ac.shef.wit.simmetrics.similaritymetrics.MongeElkan;
import edu.isi.karma.er.compare.StringComparator;

public class StringMongeElkanComparatorImpl implements StringComparator {

	@Override
	public float getSimilarity(String str1, String str2) {
		
		MongeElkan comp = new MongeElkan();
		return comp.getSimilarity(str1, str2);
	}

}
