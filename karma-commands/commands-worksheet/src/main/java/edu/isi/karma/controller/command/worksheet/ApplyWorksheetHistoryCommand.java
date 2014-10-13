package edu.isi.karma.controller.command.worksheet;

import java.io.File;

import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.command.selection.SuperSelectionManager;
import edu.isi.karma.controller.history.CommandHistory;
import edu.isi.karma.controller.history.WorksheetCommandHistoryExecutor;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.InfoUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.controller.update.WorksheetUpdateFactory;
import edu.isi.karma.rep.Workspace;

public class ApplyWorksheetHistoryCommand extends WorksheetCommand {
	private final File historyFile;
	
	private static Logger logger = LoggerFactory.getLogger(ApplyWorksheetHistoryCommand.class);
	
	protected ApplyWorksheetHistoryCommand(String id, File uploadedFile, String worksheetId) {
		super(id,worksheetId);
		this.historyFile = uploadedFile;
	}

	@Override
	public String getCommandName() {
		return ApplyWorksheetHistoryCommand.class.getName();
	}

	@Override
	public String getTitle() {
		return "Apply Command History";
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
		
		try {
			JSONArray historyJSON = CommandHistory.getHistorySaver(workspace.getId()).loadHistory(historyFile.getAbsolutePath());
			WorksheetCommandHistoryExecutor histExecutor = new WorksheetCommandHistoryExecutor(worksheetId, workspace);
			histExecutor.executeAllCommands(historyJSON);
		} catch (Exception e) {
			String msg = "Error occured while applying history!";
			logger.error(msg, e);
			return new UpdateContainer(new ErrorUpdate(msg));
		}
		
		// Add worksheet updates that could have resulted out of the transformation commands
		UpdateContainer c = WorksheetUpdateFactory.createRegenerateWorksheetUpdates(worksheetId, SuperSelectionManager.DEFAULT_SELECTION);
		c.append(computeAlignmentAndSemanticTypesAndCreateUpdates(workspace));
		c.add(new InfoUpdate("History successfully applied!"));
		return c;
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		return null;
	}

}
