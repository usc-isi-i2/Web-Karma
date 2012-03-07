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
package edu.isi.karma.controller.command;

import java.io.PrintWriter;

import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Entity;
import edu.isi.karma.util.JSONUtil;
import edu.isi.karma.view.VWorkspace;

/**
 * Abstract class for all commands.
 * 
 * @author szekely
 * 
 */
public abstract class Command extends Entity {

	public enum HistoryType {
		undo, redo
	}

	public enum CommandType {
		undoable, notUndoable, notInHistory
	}

	public enum JsonKeys {
		commandId, title, description, commandType, historyType
	}

	/**
	 * @return the internal name of this command. Used to communicate between
	 *         the server and the browser.
	 */
	public abstract String getCommandName();

	/**
	 * @return the label shown in the user interface.
	 */
	public abstract String getTitle();

	/**
	 * @return a description of what the command does or did, if it was
	 *         executed.
	 */
	public abstract String getDescription();

	/**
	 * @return the type of this command.
	 */
	public abstract CommandType getCommandType();

	public abstract UpdateContainer doIt(VWorkspace vWorkspace)
			throws CommandException;

	public abstract UpdateContainer undoIt(VWorkspace vWorkspace);

	/**
	 * Has this command been executed already?
	 */
	private boolean isExecuted = false;

	protected Command(String id) {
		super(id);

	}

	public boolean isExecuted() {
		return isExecuted;
	}

	public void setExecuted(boolean isExecuted) {
		this.isExecuted = isExecuted;
	}

	/**
	 * @param prefix
	 * @param pw
	 * @param vWorkspace
	 * @param historyType of the lists where the command is, either undo or redo.
	 */
	public void generateJson(String prefix, PrintWriter pw,
			VWorkspace vWorkspace, HistoryType historyType) {
		pw.println(prefix + "{");
		String newPref = prefix + "  ";
		pw.println(newPref + JSONUtil.json(JsonKeys.commandId, getId()));
		pw.println(newPref + JSONUtil.json(JsonKeys.title, getTitle()));
		pw.println(newPref + JSONUtil.json(JsonKeys.description, getDescription()));
		pw.println(newPref
				+ JSONUtil.json(JsonKeys.historyType, historyType.name()));
		pw.println(newPref
				+ JSONUtil.jsonLast(JsonKeys.commandType, getCommandType().name()));
		pw.println(prefix + "}");
	}
}
