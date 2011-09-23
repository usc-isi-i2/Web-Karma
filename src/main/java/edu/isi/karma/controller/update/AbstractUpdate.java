/**
 * 
 */
package edu.isi.karma.controller.update;

import java.io.PrintWriter;

import edu.isi.karma.view.VWorkspace;

/**
 * All update classes must inherit from this class.
 * 
 * @author szekely
 * 
 */
public abstract class AbstractUpdate {

	/**
	 * JSON keys that must appear in EVERY update object.
	 * 
	 * @author szekely
	 * 
	 */
	public enum GenericJsonKeys {
		updateType
	}

	public abstract void generateJson(String prefix, PrintWriter pw, VWorkspace vWorkspace);

	protected String getUpdateType() {
		return this.getClass().getSimpleName();
	}
}
