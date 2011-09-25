/**
 * 
 */
package edu.isi.karma.rep;

import java.io.PrintWriter;
import java.io.StringWriter;


/**
 * @author szekely All entities in the representation that have ids and get
 *         translated into data sent to the client.
 */
public abstract class RepEntity extends Entity {
	public abstract void prettyPrint(String prefix, PrintWriter pw,
			RepFactory factory);

	public String prettyPrint(RepFactory factory) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		prettyPrint("", pw, factory);
		return sw.toString();
	}

	protected RepEntity(String id) {
		super(id);
	}
}
