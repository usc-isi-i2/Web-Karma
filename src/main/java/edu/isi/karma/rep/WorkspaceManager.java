/**
 * 
 */
package edu.isi.karma.rep;

/**
 * @author szekely
 *
 */
public class WorkspaceManager {

	/**
	 * One factory for all the objects for all users.
	 */
	private final RepFactory factory = new RepFactory();
	
	public RepFactory getFactory() {
		return factory;
	}
}
