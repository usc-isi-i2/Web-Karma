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

import edu.isi.karma.controller.history.CommandHistory;
import edu.isi.karma.util.JSONUtil;
import edu.isi.karma.view.VWorkspace;

/**
 * Reports all the commands in the history including undo and redo commands.
 * They are ordered so that undo commands come first and redo commands are last.
 * 
 * @author szekely
 * 
 */
public class HistoryUpdate extends AbstractUpdate {

	public enum JsonKeys {
		commands
	}

	private final CommandHistory commandHistory;

	public HistoryUpdate(CommandHistory commandHistory) {
		super();
		this.commandHistory = commandHistory.clone();
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		pw.println(prefix + "{");
		String newPref = prefix + "  ";
		pw.println(newPref
				+ JSONUtil.json(GenericJsonKeys.updateType, getUpdateType()));
		pw.println(newPref + JSONUtil.jsonStartList(JsonKeys.commands));
		commandHistory.generateFullHistoryJson(newPref + "  ", pw, vWorkspace);
		pw.println(newPref + "]");
		pw.println(prefix + "}");
	}
	
	public boolean equals(Object o) {
		if (o instanceof HistoryUpdate) {
			return true;
		}
		return false;
	}

}
