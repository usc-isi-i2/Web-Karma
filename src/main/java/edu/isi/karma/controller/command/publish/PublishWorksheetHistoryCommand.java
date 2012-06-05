package edu.isi.karma.controller.command.publish;

import java.io.File;
import java.io.PrintWriter;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command;
import edu.isi.karma.controller.command.CommandException;
import edu.isi.karma.controller.history.HistoryJsonUtil;
import edu.isi.karma.controller.update.AbstractUpdate;
import edu.isi.karma.controller.update.ErrorUpdate;
import edu.isi.karma.controller.update.UpdateContainer;
import edu.isi.karma.rep.Worksheet;
import edu.isi.karma.util.FileUtil;
import edu.isi.karma.view.VWorkspace;

public class PublishWorksheetHistoryCommand extends Command {
	private final String vWorksheetId;
	
	private enum JsonKeys {
		updateType, fileUrl, vWorksheetId
	}
	
	private static Logger logger = LoggerFactory
			.getLogger(PublishWorksheetHistoryCommand.class);

	protected PublishWorksheetHistoryCommand(String id, String vWorksheetId) {
		super(id);
		this.vWorksheetId = vWorksheetId;
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
	public UpdateContainer doIt(VWorkspace vWorkspace) throws CommandException {
		Worksheet worksheet = vWorkspace.getViewFactory().getVWorksheet(vWorksheetId).getWorksheet();
		final String wkName = worksheet.getTitle();
		final String wsPreferenceId = vWorkspace.getPreferencesId();
		if(!HistoryJsonUtil.historyExists(wkName, wsPreferenceId)) {
			return new UpdateContainer(new ErrorUpdate("No history exists for the worksheet!"));
		}
		
		// Copy the history file to a location accessible from web
		File existingHistoryFile = new File(HistoryJsonUtil.constructWorksheetHistoryJsonFilePath(wkName, wsPreferenceId));
		final File newFileName = new File("./src/main/webapp/History/" 
				+ HistoryJsonUtil.constructWorksheetHistoryJsonFileName(wkName, wsPreferenceId));
		try {
			FileUtil.copyFiles(newFileName, existingHistoryFile);
		} catch (Exception e1) {
			logger.error("Error occured while copying the history file to server!", e1);
			return new UpdateContainer(new ErrorUpdate("Error occured while publishing history for worksheet!"));
		}
		
		return new UpdateContainer(new AbstractUpdate() {
			@Override
			public void generateJson(String prefix, PrintWriter pw,
					VWorkspace vWorkspace) {
				JSONObject outputObject = new JSONObject();
				try {
					outputObject.put(JsonKeys.updateType.name(),
							"PublishWorksheetHistoryUpdate");
					outputObject.put(JsonKeys.fileUrl.name(),
							"History/" + HistoryJsonUtil.constructWorksheetHistoryJsonFileName(wkName, wsPreferenceId));
					outputObject.put(JsonKeys.vWorksheetId.name(),
							vWorksheetId);
					pw.println(outputObject.toString(4));
				} catch (JSONException e) {
					logger.error("Error occured while generating JSON!");
				}
			}
		});
	}

	@Override
	public UpdateContainer undoIt(VWorkspace vWorkspace) {
		// TODO Auto-generated method stub
		return null;
	}

}
