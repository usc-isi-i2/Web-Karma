package edu.isi.karma.controller.command.worksheet;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.update.AllWorksheetHeadersUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Workspace;

public class GetAllWorksheetHeadersCommand extends WorksheetCommand {

	protected GetAllWorksheetHeadersCommand(String id, String model, String worksheetId) {
		super(id, model, worksheetId);
		// TODO Auto-generated constructor stub
	}

	@Override
	public String getCommandName() {
		return GetAllWorksheetHeadersCommand.class.getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Get All Worksheet Headers";
	}

	@Override
	public String getDescription() {
		return null;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notInHistory;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		return new UpdateContainer(new AllWorksheetHeadersUpdate(worksheetId, false));
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		// TODO Auto-generated method stub
		return null;
	}

}
