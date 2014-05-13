package edu.isi.karma.controller.history;


import org.json.JSONArray;


public interface IHistorySaver {

		public void saveHistory(String workspaceId, String worksheetId, JSONArray history) throws Exception;
		public JSONArray loadHistory(String filename) throws Exception;
		public String getHistoryFilepath(String worksheetId);

}
