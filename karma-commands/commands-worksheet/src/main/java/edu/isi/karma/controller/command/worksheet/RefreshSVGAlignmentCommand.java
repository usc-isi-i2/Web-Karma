package edu.isi.karma.controller.command.worksheet;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.update.AlignmentSVGVisualizationUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Workspace;

public class RefreshSVGAlignmentCommand extends WorksheetCommand {
	private String alignmentId;
	
	public RefreshSVGAlignmentCommand(String id, String model, String worksheetId, String alignmentId) {
		super(id, model, worksheetId);
		this.alignmentId = alignmentId;
	}
	
	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Refresh Alignment";
	}

	@Override
	public String getDescription() {
		return worksheetId;
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notInHistory;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		UpdateContainer container = new UpdateContainer();
		container.add(new AlignmentSVGVisualizationUpdate(worksheetId));
		return container;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		// TODO Auto-generated method stub
		return null;
	}

}
