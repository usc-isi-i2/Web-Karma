package edu.isi.karma.controller.command.publish;

import java.io.PrintWriter;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.command.CommandType;
import edu.isi.karma.controller.command.WorksheetCommand;
import edu.isi.karma.controller.history.CommandHistory;
import edu.isi.karma.controller.history.HistoryJsonUtil;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Workspace;
import edu.isi.karma.view.VWorkspace;

public class PublishWorksheetHistoryCommand extends WorksheetCommand {
	
	private enum JsonKeys {
		updateType, fileUrl, worksheetId
	}
	
	private static Logger logger = LoggerFactory
			.getLogger(PublishWorksheetHistoryCommand.class);

	protected PublishWorksheetHistoryCommand(String id, String worksheetId) {
		super(id, worksheetId);
	}

	@Override
	public String getCommandName() {
		return this.getClass().getSimpleName();
	}

	@Override
	public String getTitle() {
		// TODO Auto-generated method stub
		return "Publish Worksheet History";
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
		
		final String workspaceId = workspace.getId();
		
		if(!HistoryJsonUtil.historyExists(workspaceId, worksheetId)) {
			return new UpdateContainer(new ErrorUpdate("No history exists for the worksheet!"));
		}
		
		// Copy the history file to a location accessible from web
//		File existingHistoryFile = new File(HistoryJsonUtil.constructWorksheetHistoryJsonFilePath(wkName, wsPreferenceId));
//		final File newFileName = new File("./src/main/webapp/History/" 
//				+ HistoryJsonUtil.constructWorksheetHistoryJsonFileName(wkName, wsPreferenceId));
//		try {
//			FileUtil.copyFiles(newFileName, existingHistoryFile);
//		} catch (Exception e1) {
//			logger.error("Error occured while copying the history file to server!", e1);
//			return new UpdateContainer(new ErrorUpdate("Error occured while publishing history for worksheet!"));
//		}
		
		
		return new UpdateContainer(new AbstractUpdate() {
			@Override
			public void generateJson(String prefix, PrintWriter pw,
					VWorkspace vWorkspace) {
				JSONObject outputObject = new JSONObject();
				try {
					outputObject.put(JsonKeys.updateType.name(),
							"PublishWorksheetHistoryUpdate");
					outputObject.put(JsonKeys.fileUrl.name(),
							CommandHistory.getHistorySaver(workspaceId).getHistoryFilepath(worksheetId));
					outputObject.put(JsonKeys.worksheetId.name(),
							worksheetId);
					pw.println(outputObject.toString(4));
				} catch (JSONException e) {
					logger.error("Error occured while generating JSON!");
				}
			}
		});
	}

	@Override
	public UpdateContainer undoIt(Workspace workspace) {
		// TODO Auto-generated method stub
		return null;
	}

}
