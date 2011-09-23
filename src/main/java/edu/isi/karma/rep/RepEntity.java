/**
 * 
 */
package edu.isi.karma.rep;

import java.io.PrintWriter;


/**
 * @author szekely All entities in the representation that have ids and get
 *         translated into data sent to the client.
 */
public abstract class RepEntity extends Entity {
	public abstract void prettyPrint(String prefix, PrintWriter pw, RepFactory factory);
	
	protected RepEntity(String id) {
		super(id);
	}
}
