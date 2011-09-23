/**
 * 
 */
package edu.isi.karma.controller.update;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.isi.karma.util.Util;
import edu.isi.karma.view.VWorkspace;

/**
 * Container class to carry multiple update objects.
 * 
 * @author szekely
 * 
 */
public class UpdateContainer {

	private final List<AbstractUpdate> updates = new LinkedList<AbstractUpdate>();

	public enum JsonKeys {
		elements, workspaceId
	}

	public UpdateContainer() {
	}

	public UpdateContainer(AbstractUpdate update) {
		updates.add(update);
	}

	public void add(AbstractUpdate update) {
		updates.add(update);
	}

	public void append(UpdateContainer updateContainer) {
		updates.addAll(updateContainer.updates);
	}

	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		pw.println(prefix + "{");
		String newPref = prefix + "  ";
		pw.println(newPref
				+ Util.json(JsonKeys.workspaceId, vWorkspace.getWorkspace()
						.getId()));
		pw.println(newPref + Util.jsonStartList(JsonKeys.elements));
		Iterator<AbstractUpdate> it = updates.iterator();
		while (it.hasNext()) {
			it.next().generateJson(newPref + "  ", pw, vWorkspace);
			if (it.hasNext()) {
				pw.println(newPref + "  ,");
			}
		}
		pw.println(newPref + "]");
		pw.println(prefix + "}");
	}

	/**
	 * @param vWorkspace
	 * @return the JSON as a String.
	 */
	public String generateJson(VWorkspace vWorkspace) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		generateJson("", pw, vWorkspace);
		return sw.toString();
	}
}
