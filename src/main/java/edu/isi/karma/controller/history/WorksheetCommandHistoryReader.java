package edu.isi.karma.controller.history;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.Command.CommandTag;
import edu.isi.karma.rep.Workspace;

public class WorksheetCommandHistoryReader {
	private final String worksheetId;
	private final Workspace workspace;
	
	private static Logger logger = LoggerFactory.getLogger(WorksheetCommandHistoryReader.class);
	
	public WorksheetCommandHistoryReader(String worksheetId, Workspace workspace) {
		super();
		this.worksheetId = worksheetId;
		this.workspace = workspace;
	}

	private File findHistoryFile() {
		String worksheetName = workspace.getWorksheet(worksheetId).getTitle();
		File historyFile = new File(HistoryJsonUtil.constructWorksheetHistoryJsonFilePath(worksheetName, workspace.getCommandPreferencesId()));
		return historyFile;
	}
	
	public JSONArray readCommandsByTag(List<CommandTag> tag) {
	
		JSONArray filteredHistoryJson = new JSONArray();
		try {
			JSONArray historyJson = HistoryJsonUtil.readCommandsFromFile(findHistoryFile());
			filteredHistoryJson = HistoryJsonUtil.filterCommandsByTag(tag, historyJson);
		} catch (FileNotFoundException e) {
			logger.error("History file not found!", e);
			return filteredHistoryJson;
		} catch (JSONException e) {
			logger.error("Error occured while working with JSON!", e);
		}
		
		return filteredHistoryJson;
	}

	
}
