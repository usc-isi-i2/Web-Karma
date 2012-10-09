package edu.isi.karma.er.compare.impl;

import uk.ac.shef.wit.simmetrics.similaritymetrics.NeedlemanWunch;
import edu.isi.karma.er.compare.StringComparator;

public class StringNeedlemanWunchComparatorImpl implements StringComparator {

	@Override
	public float getSimilarity(String str1, String str2) {
		NeedlemanWunch comp = new NeedlemanWunch();
		
		return comp.getSimilarity(str1, str2);
	}

}
