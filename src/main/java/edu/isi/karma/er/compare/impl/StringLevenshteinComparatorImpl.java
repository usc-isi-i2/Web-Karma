package edu.isi.karma.er.compare.impl;

import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import edu.isi.karma.er.compare.StringComparator;

public class StringLevenshteinComparatorImpl implements StringComparator {

	public float getSimilarity(String str1, String str2) {
		Levenshtein method = new Levenshtein();
		return method.getSimilarity(str1, str2);
	}

	

}
