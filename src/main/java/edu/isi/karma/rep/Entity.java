/**
 * 
 */
package edu.isi.karma.rep;


/**
 * @author szekely
 *
 */
public abstract class Entity {

	protected final String id;

	protected Entity(String id) {
		super();
		this.id = id;
	}
	
	public String getId() {
		return id;
	}

}
