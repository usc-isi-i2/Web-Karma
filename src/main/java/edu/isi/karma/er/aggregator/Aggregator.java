package edu.isi.karma.er.aggregator;


import edu.isi.karma.er.helper.entity.MultiScore;
import edu.isi.karma.er.helper.entity.SaamPerson;

public interface Aggregator {

	/**
	 * Compare 2 resources with specific properties and comparators which are configed in JSON Array.
	 * @param confArr	a Json array stored the configurations used in matching.
	 * @param res1	the source resource to match
	 * @param res2	the target resource to match with
	 * @return an object contains final score and details of matching 
	 */
	public MultiScore match(SaamPerson s1, SaamPerson s2);
}
