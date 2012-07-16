package edu.isi.karma.controller.command;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.history.WorksheetCommandHistoryReader;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.InfoUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.view.VWorkspace;

public class ApplyWorksheetHistoryCommand extends Command {
	private final File historyFile;
	private final String vWorksheetId;
	
	private static Logger logger = LoggerFactory.getLogger(ApplyWorksheetHistoryCommand.class);
	
	protected ApplyWorksheetHistoryCommand(String id, File uploadedFile, String vWorksheetId) {
		super(id);
		this.historyFile = uploadedFile;
		this.vWorksheetId = vWorksheetId;
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
		return CommandType.notUndoable;
	}

	@Override
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		WorksheetCommandHistoryReader histReader = new WorksheetCommandHistoryReader(vWorksheetId, vWorkspace);
		try {
			histReader.readAndExecuteAllCommandsFromFile(historyFile);
		} catch (Exception e) {
			String msg = "Error occured while applying history!";
			logger.error(msg, e);
			return new UpdateContainer(new ErrorUpdate(msg));
		}
		
		return new UpdateContainer(new InfoUpdate("History successfully applied!"));
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		return null;
	}

}
