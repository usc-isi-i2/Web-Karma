package edu.isi.karma.er.compare.impl;

import uk.ac.shef.wit.simmetrics.similaritymetrics.Soundex;
import edu.isi.karma.er.compare.StringComparator;

public class StringSoundExComparatorImpl implements StringComparator {

	@Override
	public float getSimilarity(String str1, String str2) {
		Soundex comp = new Soundex();
		return comp.getSimilarity(str1, str2);
	}

}
