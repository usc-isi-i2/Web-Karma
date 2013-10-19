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
import edu.isi.karma.view.VWorkspace;

public class UndoRedoCommand extends Command {

	private final String commandIdArg;

	UndoRedoCommand(String id, String commandIdArg) {
		super(id);
		this.commandIdArg = commandIdArg;
	}

	@Override
	public String getCommandName() {
		return "Undo/Redo";
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
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		UpdateContainer undoEffects = vWorkspace.getWorkspace().getCommandHistory().undoOrRedoCommandsUntil(
				vWorkspace, commandIdArg);
		UpdateContainer result = new UpdateContainer(new HistoryUpdate(
				vWorkspace.getWorkspace().getCommandHistory()));
		result.append(undoEffects);
		return result;
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		// not undoable.
		return new UpdateContainer();
	}

}
