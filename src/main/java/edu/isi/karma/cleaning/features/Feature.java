/**
 * 
 */
package edu.isi.karma.cleaning.features;

/**
 * @author szekely
 * 
 */
public interface Feature {

	/**
	 * @return the name of this feature.
	 */
	public String getName();

	/**
	 * @return the score of this feature on a set of strings.
	 */
	public double getScore();
}
