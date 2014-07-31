package edu.isi.karma.controller.command.worksheet;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.update.HistoryUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetDeleteUpdate;
import edu.isi.karma.controller.update.WorksheetListUpdate;
import edu.isi.karma.rep.Workspace;

public class DeleteWorksheetCommand extends WorksheetCommand {

	public DeleteWorksheetCommand(String id, String worksheetId) {
		super(id, worksheetId);
	}
	
	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Delete Worksheet";
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
	}

	@Override
	public UpdateContainer doIt(final Workspace workspace) throws CommandException {
		boolean worksheetExists = false;
		
		if(workspace.getWorksheet(worksheetId) != null) {
			worksheetExists = true;
			workspace.removeWorksheet(worksheetId);			
			workspace.getFactory().removeWorksheet(worksheetId, workspace.getCommandHistory());				
		}
		
		UpdateContainer update = new UpdateContainer();
		update.add(new WorksheetListUpdate());
		if(worksheetExists) {
			update.add(new WorksheetDeleteUpdate(worksheetId));
			update.add(new HistoryUpdate(workspace.getCommandHistory()));
		}
		return update;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		// TODO Auto-generated method stub
		return null;
	}

}
