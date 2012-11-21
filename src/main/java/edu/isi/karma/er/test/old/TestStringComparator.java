package edu.isi.karma.er.test.old;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.er.compare.StringComparator;
import edu.isi.karma.er.compare.impl.StringQGramComparatorImpl;

public class TestStringComparator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Logger logger = LoggerFactory.getLogger(TestStringComparator.class);
		logger.info("TEST");
		//String str1 = "Anthony van Dyck"; //"John R. Smith";
		//String str2 = "Sir Anthony van Dyck"; //"John Richard Smith";
		String[] strs1 = {"Anthony Velonis", "Alexander Dobkin", "Alfred-Phillippe Roll", "Cassandre", "Arthur Hall", "Anderson Johnson", "Sir Anthony van Dyck", "Alfred Rudolph Waud", "Alan Stone", "Arthur North", "Alice Mason"};
		String[] strs2 = {"Anthony Fell", "Alexander Dinghas", "Alfred Philippe Roll", "A. Mouron Cassandre",  "Arthur R. Hall", "John Anderson, Jr.", "Anthony van Dyck", "Alfred Waud", "Allan Stone", "Arthur Norman", "Alice Rahon"};
		long startTime = System.currentTimeMillis();
		StringComparator comp = new StringQGramComparatorImpl();
//		StringComparator comp2 = new StringJaroWinklerComparatorImpl();
//		StringComparator comp3 = new StringLevenshteinComparatorImpl();
//		StringComparator comp4 = new StringJaccardComparatorImpl();
//		StringComparator comp5 = new StringSmithWatermanComparatorImpl();
		
//		System.out.println("QGram result:" + comp.getSimilarity(str1, str2));
//		System.out.println("JaroWinkler result:" + comp2.getSimilarity(str1, str2));
//		System.out.println("Levenshtein result:" + comp3.getSimilarity(str1, str2));
//		System.out.println("Jaccard result:" + comp4.getSimilarity(str1, str2));
//		System.out.println("SmithWaterman result:" + comp5.getSimilarity(str1, str2));

		for (int i = 0; i < strs1.length; i++) {
			System.out.println("[ " + comp.getSimilarity(strs1[i], strs2[i]) + " ]\t" + strs1[i] + "\t" + strs2[i]);
		}

		
		System.out.println("time elapsed:" + (System.currentTimeMillis() - startTime) + "ms");
	}

}
