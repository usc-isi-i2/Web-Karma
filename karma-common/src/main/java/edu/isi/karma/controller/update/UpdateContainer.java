/*******************************************************************************
 * Copyright 2012 University of Southern California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This code was developed by the Information Integration Group as part 
 * of the Karma project at the Information Sciences Institute of the 
 * University of Southern California.  For more information, publications, 
 * and related projects, please see: http://www.isi.edu/integration
 ******************************************************************************/
/**
 * 
 */
package edu.isi.karma.controller.update;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import edu.isi.karma.util.JSONUtil;
import edu.isi.karma.view.VWorkspace;

/**
 * Container class to carry multiple update objects.
 * 
 * @author szekely
 * 
 */
public class UpdateContainer {

	private final List<AbstractUpdate> updates = new LinkedList<>();

	public enum JsonKeys {
		elements, workspaceId
	}

	public UpdateContainer() {
	}

	public UpdateContainer(AbstractUpdate update) {
		updates.add(update);
	}

	public void add(AbstractUpdate update) {
		append(new UpdateContainer(update));
	}

	public void append(UpdateContainer updateContainer) {
		Set<AbstractUpdate> needToDeleted = new HashSet<>();
		for (AbstractUpdate update : updateContainer.updates) {
			for (AbstractUpdate update2 : updates) {
				if (update2.equals(update)) {
					needToDeleted.add(update2);
					break;
				}
			}
		}
		updates.removeAll(needToDeleted);
		updates.addAll(updateContainer.updates);
	}

	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		pw.println(prefix + "{");
		String newPref = prefix + "  ";
		pw.println(newPref
				+ JSONUtil.json(JsonKeys.workspaceId, vWorkspace.getWorkspace()
						.getId()));
		pw.println(newPref + JSONUtil.jsonStartList(JsonKeys.elements));
		Iterator<AbstractUpdate> it = updates.iterator();
		while (it.hasNext()) {
			AbstractUpdate update = it.next();
			update.generateJson(newPref + "  ", pw, vWorkspace);
			if (it.hasNext()) {
				pw.println(newPref + "  ,");
			}
		}
		pw.println(newPref + "]");
		pw.println(prefix + "}");
	}

	public void applyUpdates(VWorkspace vWorkspace)
	{
		Iterator<AbstractUpdate> it = updates.iterator();
		while (it.hasNext()) {
			it.next().applyUpdate(vWorkspace);
		}
	}
	
	public void removeUpdateByClass(Class<? extends AbstractUpdate> clazz) {
		Iterator<AbstractUpdate> it = updates.iterator();
		while (it.hasNext()) {
			AbstractUpdate t = it.next();
			if (t.getClass().equals(clazz))
				it.remove();
		}
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
