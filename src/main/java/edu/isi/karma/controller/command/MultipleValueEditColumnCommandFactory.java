package edu.isi.karma.controller.command;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.isi.karma.util.CommandInputJSONUtil;
import edu.isi.karma.view.VWorkspace;
import edu.isi.karma.webserver.KarmaException;

public class MultipleValueEditColumnCommandFactory extends CommandFactory implements JSONInputCommandFactory{
	private enum JSONInputArguments {
		hNodeID, vWorksheetID, rows, rowID, value
	}

	@Override
	public Command createCommand(HttpServletRequest request, VWorkspace vWorkspace) {
		return null;
	}

	public Command createCommand(JSONArray inputJson, VWorkspace vWorkspace) throws JSONException, KarmaException {
		/** Parse the input arguments and create proper data structues to be passed to the command **/
		String hNodeID = CommandInputJSONUtil.getStringValue(JSONInputArguments.hNodeID.name(), inputJson);
		String vWorksheetID = CommandInputJSONUtil.getStringValue(JSONInputArguments.vWorksheetID.name(), inputJson);
		JSONArray rows = CommandInputJSONUtil.getJSONArrayValue(JSONInputArguments.rows.name(), inputJson);
		
		Map<String, String> rowValueMap = new HashMap<String, String>();
		for (int i=0; i<rows.length(); i++) {
			JSONObject rowObj = rows.getJSONObject(i);
			rowValueMap.put(rowObj.getString(JSONInputArguments.rowID.name()), 
					rowObj.getString(JSONInputArguments.value.name()));
		}
		return new MultipleValueEditColumnCommand(getNewId(vWorkspace), vWorksheetID, hNodeID, rowValueMap);
	}

}
