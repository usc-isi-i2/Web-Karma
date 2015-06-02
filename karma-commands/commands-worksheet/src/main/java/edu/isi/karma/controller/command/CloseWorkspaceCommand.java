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

import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetDeleteUpdate;
import edu.isi.karma.modeling.alignment.AlignmentManager;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.rep.WorkspaceManager;
import edu.isi.karma.view.VWorkspaceRegistry;
import edu.isi.karma.webserver.WorkspaceRegistry;

public class CloseWorkspaceCommand extends Command {
	private final String workspaceId;

	protected CloseWorkspaceCommand(String id, String model, String workspaceId) {
		super(id, model);
		this.workspaceId = workspaceId;
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Close Workspace";
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		UpdateContainer uc = new UpdateContainer();
		WorkspaceManager mgr = WorkspaceManager.getInstance();
		// Remove it from the rep factory
		mgr.removeWorkspace(workspaceId);
		
		// Remove any alignments from the AlignmentManager
		AlignmentManager.Instance().removeWorkspaceAlignments(workspace.getId());
		
		// Remove it from the workspace registry
		WorkspaceRegistry.getInstance().deregister(workspaceId);
		VWorkspaceRegistry.getInstance().deregisterVWorkspace(workspaceId);
		for (Worksheet worksheet : workspace.getWorksheets()) {
			workspace.getFactory().removeWorksheet(worksheet.getId(), workspace.getCommandHistory());
			workspace.removeWorksheet(worksheet.getId());
			uc.add(new WorksheetDeleteUpdate(worksheet.getId()));
		}
		return uc;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		// Not required
		return null;
	}

}
