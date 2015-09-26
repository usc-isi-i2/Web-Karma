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
package edu.isi.karma.controller.command;

import edu.isi.karma.controller.update.HistoryUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Workspace;

public class UndoRedoCommand extends Command {

	private final String commandIdArg;
	private final String worksheetId;

	UndoRedoCommand(String id, String model, String commandIdArg, String worksheetId) {
		super(id, model);
		this.commandIdArg = commandIdArg;
		if (worksheetId.equals("null")) {
			this.worksheetId = null;
		}
		else {
			this.worksheetId = worksheetId;
		}
	}

	@Override
	public String getCommandName() {
		return UndoRedoCommand.class.getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Undo/Redo";
	}

	@Override
	public String getDescription() {
		return "Undo/Redo " + commandIdArg;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notInHistory;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		UpdateContainer undoEffects = workspace.getCommandHistory().undoOrRedoCommand(
				workspace, worksheetId);
		UpdateContainer result = new UpdateContainer(new HistoryUpdate(
				workspace.getCommandHistory()));
		result.append(undoEffects);
		return result;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		// not undoable.
		return null;
	}

}
