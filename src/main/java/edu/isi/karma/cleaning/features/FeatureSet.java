/**
 * 
 */
package edu.isi.karma.cleaning.features;

import java.util.Collection;

/**
 * @author szekely
 * 
 */
public interface FeatureSet {

	/**
	 * @param examples
	 *            , the set of strings on which we want to compute features.
	 * @return the features for the set of strings.
	 */
	public Collection<Feature> computeFeatures(Collection<String> examples,Collection<String> oexamples);
	
	/**
	 * @return the name of all the features.
	 */
	public Collection<String> getFeatureNames();
}
