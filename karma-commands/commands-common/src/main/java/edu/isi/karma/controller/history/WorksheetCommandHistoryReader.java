package edu.isi.karma.controller.history;

import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.isi.karma.controller.command.ICommand.CommandTag;
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

	
	public JSONArray readCommandsByTag(List<CommandTag> tag) {
	
		JSONArray filteredHistoryJson = new JSONArray();
		try {
			String filename = CommandHistory.getHistorySaver(workspace.getId()).getHistoryFilepath(worksheetId);
			JSONArray historyJson = CommandHistory.getHistorySaver(workspace.getId()).loadHistory(filename);
			filteredHistoryJson = HistoryJsonUtil.filterCommandsByTag(tag, historyJson);
		} catch (JSONException e) {
			logger.error("Error occured while working with JSON!", e);
		} catch(Exception e) {
			logger.error("Error reading from history file!", e);
		}
		
		return filteredHistoryJson;
	}

	
}
