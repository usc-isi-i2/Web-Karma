package edu.isi.karma.er.compare.impl;

import java.util.Set;

import edu.isi.karma.er.compare.StringComparator;
import edu.isi.karma.er.compare.StringSetComparator;



public class StringSetMaxComparatorImpl implements StringSetComparator {

	public float getSimilarity(Set<String> srcSet, Set<String> dstSet,
			StringComparator comp) {
		/*
		 * if one of the set is null or empty, then result 0 which represents
		 * total unmatched.
		 */
		if (srcSet == null || dstSet == null || srcSet.size() <= 0
				|| dstSet.size() <= 0) {
			return 0;
		}

		float max = 0, degree = 0;

		for (String str1 : srcSet) {
			for (String str2 : dstSet) {
				degree = comp.getSimilarity(str1, str2);
				if (degree > max) {
					max = degree;
				}
			}
		}
		return max;

	}

}
