package edu.isi.karma.controller.command.publish;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.update.InfoUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.rep.Workspace;

public class PublishJSONCommand extends WorksheetCommand {
	private static Logger logger = LoggerFactory.getLogger(PublishJSONCommand.class);
	private final String alignmentNodeId;

	public PublishJSONCommand(String newId, String alignmentNodeId, String worksheetId) {
		super(newId, worksheetId);

		logger.info("Entered PublishJSONCommand");
		this.alignmentNodeId = alignmentNodeId;
		
		addTag(CommandTag.Transformation);
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		return "Generate JSON";
	}

	@Override
	public String getDescription() {
		return "";
	}

	@Override
	public CommandType getCommandType() {
		return CommandType.notUndoable;
	}

	@Override
	public UpdateContainer doIt(Workspace workspace) throws CommandException {
		UpdateContainer container = WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId);
		container.add(new InfoUpdate("Generating JSON Complete"));
		return container;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}	
}