package edu.isi.karma.er.compare.impl;

import uk.ac.shef.wit.simmetrics.similaritymetrics.JaroWinkler;
import edu.isi.karma.er.compare.StringComparator;

public class StringJaroWinklerComparatorImpl implements StringComparator {

	public float getSimilarity(String str1, String str2) {
		
		JaroWinkler method = new JaroWinkler();
		return method.getSimilarity(str1, str2);
	}

	

}
