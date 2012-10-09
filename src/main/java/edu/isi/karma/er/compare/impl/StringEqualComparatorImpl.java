package edu.isi.karma.er.compare.impl;

import edu.isi.karma.er.compare.StringComparator;

public class StringEqualComparatorImpl implements StringComparator {

	@Override
	public float getSimilarity(String str1, String str2) {
		if (str1 == null || str2 == null)
			return 0f;
		
		return (str1.equalsIgnoreCase(str2) ? 1f : 0f);
	}

	

}
