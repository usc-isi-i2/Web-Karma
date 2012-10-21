package edu.isi.karma.er.compare.impl;

import uk.ac.shef.wit.simmetrics.similaritymetrics.JaccardSimilarity;
import edu.isi.karma.er.compare.StringComparator;

public class StringJaccardComparatorImpl implements StringComparator {

	public float getSimilarity(String str1, String str2) {
		JaccardSimilarity method = new JaccardSimilarity();
		
		return method.getSimilarity(str1, str2);
	}

	

}
