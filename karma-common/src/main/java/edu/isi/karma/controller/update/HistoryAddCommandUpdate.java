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

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.util.JSONUtil;
import edu.isi.karma.view.VWorkspace;

/**
 * Announces a command that should be added to the bottom of the history.
 * 
 * @author szekely
 * 
 */
public class HistoryAddCommandUpdate extends AbstractUpdate {

	public enum JsonKeys {
		command
	}

	private final Command command;

	public HistoryAddCommandUpdate(Command command) {
		super();
		this.command = command;
	}

	@Override
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace) {
		pw.println(prefix + "{");
		String newPref = prefix + "  ";
		pw.println(newPref
				+ JSONUtil.json(GenericJsonKeys.updateType, getUpdateType()));
		pw.println(newPref + JSONUtil.jsonStartObject(JsonKeys.command));
		command.generateJson(newPref + "  ", pw, vWorkspace, Command.HistoryType.undo);
		pw.println(prefix + "}");
	}
	
	public boolean equals(Object o) {
		if (o instanceof HistoryAddCommandUpdate) {
			HistoryAddCommandUpdate t = (HistoryAddCommandUpdate)o;
			return t.command.equals(command);
		}
		return false;
	}

}
