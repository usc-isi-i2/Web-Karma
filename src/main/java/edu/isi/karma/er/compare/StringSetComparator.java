package edu.isi.karma.er.compare;

import java.util.Set;

public interface StringSetComparator {

	/**
	 * To calculate the similarity between 2 set of string with the given comparator.
	 * @param srcSet, the first set of string to compare.
	 * @param dstSet, the second set of string to compare with.
	 * @param comp, the comparator used to compare 2 string from each set.
	 * @return the maximum matching degree between the srcSet and the dstSet.
	 */
	
	public float getSimilarity(Set<String> set1, Set<String> set2, StringComparator comp);
}
