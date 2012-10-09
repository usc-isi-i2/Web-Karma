package edu.isi.karma.er.ratio;

import java.util.Map;

import com.hp.hpl.jena.rdf.model.Model;

/**
 * A ratio tool to calculate the frequency of each item occurred in the model with the given predicate.
 * @author isi
 *
 */
public interface IRatio {

	/**
	 * To calculate the ratio of items resided in the model with the given predicate.
	 * @param model , contains items to be calculated.
	 * @param predicateUri, the predicate used to retrieve objects in the model.
	 * @return a map with a pair of item name and its frequency.
	 */
	public Map<String, Integer> calcRatio(Model model, String predicateUri) ;
}
