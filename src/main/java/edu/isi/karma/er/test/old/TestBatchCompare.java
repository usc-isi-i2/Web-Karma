package edu.isi.karma.er.test.old;

import java.util.ArrayList;
import java.util.List;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.util.FileManager;

import edu.isi.karma.er.compare.StringComparator;
import edu.isi.karma.er.compare.impl.StringJaroWinklerComparatorImpl;
import edu.isi.karma.er.compare.impl.StringLevenshteinComparatorImpl;
import edu.isi.karma.er.compare.impl.StringQGramComparatorImpl;
import edu.isi.karma.er.compare.impl.StringSmithWatermanComparatorImpl;
import edu.isi.karma.er.helper.Constants;

public class TestBatchCompare {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		String n3file = Constants.PATH_N3_FILE + "saam_fullname_birth_death_asso_state_country.n3";
		System.out.println("start loading.");
		Model model = FileManager.get().loadModel(n3file, "Turtle");
		System.out.println("finish loading." + model.size());
		
		String attr = "fullName";
		String predicate = "http://americanart.si.edu/saam/" + attr;
		
		NodeIterator iter = model.listObjectsOfProperty(ResourceFactory.createProperty(predicate));
		List<RDFNode> list1 = iter.toList();
		List<RDFNode> list2 = new ArrayList<RDFNode>();
		list2.addAll(list1);
		
		System.out.println("list1 size:" + list1.size() + "\t list2 size:" + list2.size());
		RDFNode n1 = null, n2 = null;
		float threshold = 0.6f;
		
		StringComparator comp1, comp2, comp3, comp4;
		comp1 = new StringJaroWinklerComparatorImpl();
		comp2 = new StringLevenshteinComparatorImpl();
		comp3 = new StringQGramComparatorImpl();
		comp4 = new StringSmithWatermanComparatorImpl();
		
		int count1 = 0, count2 = 0, count3 = 0, count4 = 0, MAX = 200;
		long startTime = System.currentTimeMillis();
		
		for (int i = 0; i < list1.size() && i < MAX; i++) {
			for (int j = 0; j < list2.size() && j < MAX; j++) {
				n1 = list1.get(i);
				n2 = list2.get(j);
				if (comp1.getSimilarity(n1.toString(), n2.toString()) >= threshold) {
					count1 ++;
				}
			}
		}
		
		System.out.println("JaroWinkler count:" + count1 + " in " + (System.currentTimeMillis() - startTime));
		
		startTime = System.currentTimeMillis();
		for (int i = 0; i < list1.size() && i < MAX; i++) {
			for (int j = 0; j < list2.size() && j < MAX; j++) {
				n1 = list1.get(i);
				n2 = list2.get(j);
				
				if (comp2.getSimilarity(n1.toString(), n2.toString()) >= threshold) {
					count2 ++;
				}
			}
		}
		
		System.out.println("Levenshtein count:" + count2 + " in " + (System.currentTimeMillis() - startTime));
		
		startTime = System.currentTimeMillis();
		for (int i = 0; i < list1.size() && i < MAX; i++) {
			for (int j = 0; j < list2.size() && j < MAX; j++) {
				n1 = list1.get(i);
				n2 = list2.get(j);
				
				if (comp3.getSimilarity(n1.toString(), n2.toString()) >= threshold) {
					count3 ++;
				}
			}
		}
		
		System.out.println("QGram count:" + count3 + " in " + (System.currentTimeMillis() - startTime));
		
		startTime = System.currentTimeMillis();
		for (int i = 0; i < list1.size() && i < MAX; i++) {
			for (int j = 0; j < list2.size() && j < MAX; j++) {
				n1 = list1.get(i);
				n2 = list2.get(j);
				
				if (comp4.getSimilarity(n1.toString(), n2.toString()) >= threshold) {
					count4 ++;
				}
			}
		}
		System.out.println("SmithWaterman count:" + count4 + " in " + (System.currentTimeMillis() - startTime));
		
		
		

	}

}
