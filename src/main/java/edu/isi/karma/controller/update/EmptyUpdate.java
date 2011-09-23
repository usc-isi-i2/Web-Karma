/**
 * 
 */
package edu.isi.karma.controller.update;

import java.io.PrintWriter;

import edu.isi.karma.view.VWorkspace;


/**
 * @author szekely
 * 
 */
public class EmptyUpdate extends AbstractUpdate {

	private static EmptyUpdate singletonInstance = new EmptyUpdate();

	public static EmptyUpdate getInstance() {
		return singletonInstance;
	}

	private EmptyUpdate() {
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw, VWorkspace vWorkspace) {
	}

}
