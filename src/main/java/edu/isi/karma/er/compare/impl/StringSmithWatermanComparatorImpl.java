package edu.isi.karma.er.compare.impl;

import uk.ac.shef.wit.simmetrics.similaritymetrics.SmithWaterman;
import edu.isi.karma.er.compare.StringComparator;

public class StringSmithWatermanComparatorImpl implements StringComparator {

	public float getSimilarity(String str1, String str2) {
		
		SmithWaterman method = new SmithWaterman();
		
		return method.getSimilarity(str1, str2);
	}

	
}
