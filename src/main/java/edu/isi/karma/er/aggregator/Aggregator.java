package edu.isi.karma.er.aggregator;

import org.json.JSONArray;

import com.hp.hpl.jena.rdf.model.Resource;

import edu.isi.karma.er.helper.entity.MultiScore;

public interface Aggregator {

	/**
	 * Compare 2 resources with specific properties and comparators which are configed in JSON Array.
	 * @param confArr	a Json array stored the configurations used in matching.
	 * @param res1	the source resource to match
	 * @param res2	the target resource to match with
	 * @return an object contains final score and details of matching 
	 */
	public MultiScore match(JSONArray confArr, Resource res1, Resource res2);
}
